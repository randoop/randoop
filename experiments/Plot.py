# If you get an error saying that there is no module matplotlib
# you will need to install it, run `pip install matplotlib`,
# if you don't have pip, run `sudo yum install python-pip python-wheel`
import matplotlib.pyplot as plt
import numpy, re, sys, os
import matplotlib.patches as mpatches

projects = ['Chart', 'Math', 'Time', 'Lang']
times = []
colors = ['pink', 'lightblue', 'thistle', 'lightgreen', 'paleturquoise', 'lightcoral']
# Marker codes for pyplot
markers = ['o', 's', 'D', '^', 'p', '*']
# Linestyles for pyplot
linestyles = ['-', '--', ':', '-.']

def readData(fileName):
	f = open(fileName, 'r')

	# Extract information from filename
	fileName = re.split('/', fileName)[-1]
	project, exp, condition, metric, ext = re.split('[_.]', fileName)

	
	# Store the data in the format timeLimit: [covered[], total]
	data = []

	lines = f.readlines()


	timeIndex = 0
	totalLines = 0
	i = 0
	while i < len(lines):
		line = lines[i].lstrip().rstrip()
		if "TIME" in line:

			# TODO: Generalize to work when different datasets have different upper time limits
			time = int(line.split(' ')[1])

			if not time in times:
				times.append(time)

			data.append([])

			# Set time to int in header 'TIME 5'
			timeIndex = times.index(time)

			i += 1
			line = lines[i].lstrip().rstrip()

		# Set totalLines for this time limit
		totalLines = int(lines[i + 1])

		# Add to lines covered
		data[timeIndex].append(float(line) * 100 / totalLines)

		i += 2

	title = '%s %s %s Coverage Percentage' % (project, exp, metric,)
	return (title, condition, data)


# list[list]	lst 			List of lists of any type
# Returns a 1 dimensional interspersion of the lists
# Ex: [[1, 2, 3], [4, 5, 6], [7, 8, 9]]
# 	  returns [1, 4, 7, 2, 5, 8, 3, 6, 8]
def intersperse(lst):
	return [val for group in zip(*lst) for val in group]


# string		title 			Title to be used in the saving of the chart
# lst[String]	seriesLabels	A list of the dataSeries' names
#								Ex: ['Randoop', 'Orienteering']
# list[list]	lst 			A list of lists, each inner list contains 10 lists
#								The innermost lists contain the information for one boxplot
# A plot is generated with boxplots comparing each of the datasets within lst
# This plot is saved to `title`.png
def boxplot(title, seriesLabels, lst):
	combined = intersperse(lst)
	positions=[i for i in range(len(times) * len(lst))]
	bplot = plt.boxplot(combined, positions=positions, patch_artist=True)

	# Set colors for dataset labels for the legend
	patches = []
	for i in range(len(lst)):
		color = color = colors[i % len(lst)]
		patches.append(mpatches.Patch(color=color, label=seriesLabels[i]))

	axes = plt.axes()
	axes.set_xticklabels(times)
	axes.set_xticks(getLabelPositions(positions, len(lst)))

	plt.legend(handles=patches)


	# Color the boxplots in colors corresponding to their dataset
	for i in range(len(bplot['boxes'])):
		patch = bplot['boxes'][i]
		color = colors[i % len(lst)]
		patch.set_facecolor(color)

def getLabelPositions(dataPositions, numSeries):
	labelPositions = []

	currSum = 0
	for i in range(len(dataPositions)):
		if i % numSeries == 0 and i != 0:
			labelPositions.append(currSum / float(numSeries))
			currSum = 0

		currSum += dataPositions[i]

	labelPositions.append(currSum / float(numSeries))
	return labelPositions

# string		title 			Title to be used in the saving of the chart
# lst[String]	seriesLabels	A list of the dataSeries' names
#								Ex: ['Randoop', 'Orienteering']
# list[list]	lst 			A list of lists, each inner list contains 10 elements
#								that are the median coverage values of that dataset
# A plot is generated with lineplots comparing each of the datasets within lst
# This plot is saved to `title`.png
def lineplot(title, seriesLabels, lst):
	# Set colors for dataset labels for the legend
	patches = []
	for i in range(len(lst)):
		color = color = colors[i % len(lst)]
		patches.append(mpatches.Patch(color=color, label=seriesLabels[i]))
	
	plt.legend(loc=4, borderaxespad=1, handles=patches, fontsize=12)

	for i in range(len(lst)):
		series = lst[i]
		seriesIdx = i % len(lst)
		plt.plot(times, series, marker=markers[seriesIdx], linestyle=linestyles[seriesIdx], color=colors[seriesIdx])

# Accepts a list of lists, the inner lists contain coverage percentages
# Returns a list of the median coverage percentage of the inner lists
def getMedians(lst):
	lst = [sorted(x) for x in lst]
	return [(((x[len(x) / 2] + x[len(x) / 2 + 1]) / 2.0) if len(x) % 2 == 0 else x[len(x) / 2]) for x in lst]

def flatten(lst):
  out = []
  for item in lst:
    if isinstance(item, (list, tuple)):
      out.extend(flatten(item))
    else:
      out.append(item)
  return out

def getMaxPoint(lst):
	return sorted(flatten(lst), reverse=True)[0]

def plot(isLinePlot, title, seriesLabels, data):
	plt.figure()
	

	if isLinePlot:
		data = [getMedians(x) for x in data]
		lineplot(title, seriesLabels, data)
	else:
		boxplot(title, seriesLabels, data)

	#plt.title(title)
	plt.xlabel('Global Time Limit (s)')
	plt.ylabel('Coverage (%)')
	plt.ylim(0, getMaxPoint(data) * 1.1)

	# Save plot
	plt.savefig('plots/%s' % title, format='png')

# Output the medians of the datasets to a csv
def outputCsv(numFiles, labels, title, data):
	try:
		os.remove('csv/%s' % (title,))
	except OSError:
		pass

	f = open('csv/%s' % (title,), 'w+')

	medians = [getMedians(x) for x in data]

	print >> f, 'Time,',
	for i in range(numFiles):
		print >> f, '%s,' % labels[i],

	print >> f

	for i in range(len(times)):
		print >> f, '%s,' % times[i],

		for j in range(numFiles):
			print >> f, '%s,' % medians[j][i],

		print >> f

def main():
	# Set line plot option
	isLinePlot = False
	if '-l' in sys.argv:
		isLinePlot = True
		sys.argv.remove('-l')
	elif '--line' in sys.argv:
		isLinePlot = True
		sys.argv.remove('--line')
	
	numFiles = len(sys.argv) - 1

	titles, seriesLabels, data = ([0 for i in range(numFiles)] for j in range(3))
	# Extract info for plot from the filename arguments
	for i in range(numFiles):
		fileName = sys.argv[i + 1]

		titles[i], seriesLabels[i], data[i] = readData(fileName)

	# Cut down all datasets to the size of the smallest dataset
	minLength = len(data[0])
	for i in range(len(data)):
		minLength = min(minLength, len(data[i]))

	for i in range(len(data)):
		global times
		times = times[:minLength]
		data[i] = data[i][:minLength]


	plot(isLinePlot, titles[0], seriesLabels, data)

	outputCsv(numFiles, seriesLabels, titles[0], data)

if __name__ == '__main__':
    main()