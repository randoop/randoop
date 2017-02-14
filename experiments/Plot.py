# If you get an error saying that there is no module matplotlib
# you will need to install it, run `pip install matplotlib`,
# if you don't have pip, run `sudo yum install python-pip python-wheel`
import matplotlib.pyplot as plt
import numpy, re, sys
import matplotlib.patches as mpatches

projects = ['Chart', 'Math', 'Time', 'Lang']
times = [50, 100, 150, 200, 250]
#times = [50, 100, 150, 200, 250, 300, 350, 400, 450, 500, 550]
labels = [50, 50, 100, 100, 150, 150, 200, 200, 250, 250]
#labels = [50, 50, 100, 100, 150, 150, 200, 200, 250, 250, 300, 300, 350, 350, 400, 400, 450, 450, 500, 500, 550, 550]
colors = ['pink', 'lightblue', 'thistle', 'paletuquoise', 'lightcoral', 'lightgreen']
# Marker codes for pyplot
markers = ['o', 's', 'D', '^', 'p', '*']
# Linestyles for pyplot
linestyles = ['-', '--', ':', '_.']

def readData(fileName):
	f = open(fileName, 'r')

	# Extract information from filename
	project, exp, condition, metric, ext = re.split('[_.]', fileName)

	if exp == 'Complete':
		pass	
	elif exp == 'Individual':
		# Store the data in the format timeLimit: [covered[], total]
		data = [[] for i in range(len(times))]

	lines = f.readlines()


	timeIndex = 0
	totalLines = 0
	i = 0
	while i < len(lines):
		line = lines[i].lstrip().rstrip()
		if "TIME" in line:

			# TODO: Generalize to work when different datasets have different upper time limits
			time = int(line.split(' ')[1])
			if time > 250:
				break

			# Set time to int in header 'TIME 5'
			timeIndex = times.index(time)

			i += 1
			line = lines[i].lstrip().rstrip()

		# Set totalLines for this time limit
		totalLines = int(lines[i + 1])

		# Add to lines covered
		data[timeIndex].append(float(line) * 100 / totalLines)

		i+=2

	title = '%s %s Coverage Percentage' % (project, metric,)
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
	print labels
	bplot = plt.boxplot(combined, labels=labels, patch_artist=True)

	# Set colors for dataset labels for the legend
	patches = []
	for i in range(len(lst)):
		color = color = colors[i % len(lst)]
		patches.append(mpatches.Patch(color=color, label=seriesLabels[i]))

	plt.legend(handles=patches)


	# Color the boxplots in colors corresponding to their dataset
	for i in range(len(bplot['boxes'])):
		patch = bplot['boxes'][i]
		color = colors[i % len(lst)]
		patch.set_facecolor(color)

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
	
	plt.legend(handles=patches)

	for i in range(len(lst)):
		series = lst[i]
		seriesIdx = i % len(lst)
		plt.plot(times, series, marker=markers[seriesIdx], linestyle=linestyles[seriesIdx], color=colors[seriesIdx])

# Accepts a list of lists, the inner lists contain coverage percentages
# Returns a list of the median coverage percentage of the inner lists
def getMedians(lst):
	lst = [sorted(coverageData) for coverageData in lst]
	return [(coverageData[5] + coverageData[6]) / 2.0 for coverageData in lst]

def avg(lst):
	return sum(lst) / len(lst)

def plot(isLinePlot, title, seriesLabels, data):
	plt.figure()
	plt.title(title)
	plt.xlabel('Global Time Limit (s)')
	plt.ylabel('Coverage (%)')
	plt.ylim(0, 30)

	if isLinePlot:
		data = [[avg(y) for y in x] for x in data]
		lineplot(title, seriesLabels, data)
	else:
		boxplot(title, seriesLabels, data)

	# Save plot
	print title
	plt.savefig('plots/%s' % title, format='png')

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

	plot(isLinePlot, titles[0], seriesLabels, data)
	
	# Print Medians of coverage %
	for i in range(numFiles):
		print getMedians(data[i])
	
if __name__ == '__main__':
    main()