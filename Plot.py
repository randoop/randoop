# If you get an error saying that there is no module matplotlib
# you will need to install it, run `pip install matplotlib`,
# if you don't have pip, run `sudo yum install python-pip python-wheel`
import matplotlib.pyplot as plt
import numpy, re, sys
import matplotlib.patches as mpatches

projects = ['Chart', 'Math', 'Time', 'Lang']
times = [50, 100, 150, 200, 250]
labels = [50, 50, 100, 100, 150, 150, 200, 200, 250, 250]

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
			# Set time to int in header 'TIME 5'
			timeIndex = times.index(int(line.split(' ')[1]))

			i += 1
			line = lines[i].lstrip().rstrip()

		# Set totalLines for this time limit
		totalLines = int(lines[i + 1])

		# Add to lines covered
		data[timeIndex].append(float(line) * 100 / totalLines)

		i+=2

	title = '%s Coverage Percentage' % (metric,)
	return (title, data)

# Accepts a list of lists and returns a 1 dimensional interspersion of the lists
# Ex: [[1, 2, 3], [4, 5, 6], [7, 8, 9]]
# 	  returns [1, 4, 7, 2, 5, 8, 3, 6, 8]
def intersperse(lst):
	return [val for group in zip(*lst) for val in group]

# Accepts a list of lists, each inner list contains 10 lists
# These innermost lists contain the information for one boxplot
# A plot is then generated with boxplots comparing each of the datasets within lst
# This plot is then saved to title.png
def boxplot(title, lst):
	plt.figure()
	plt.title(title)
	plt.xlabel('Global Time Limit (s)')
	plt.ylabel('Coverage (%)')
	plt.ylim(0, 30)

	# TODO: Check if this is equivalent to new intersperse method
	# combined = []
	# for i in range(len(data)):
	# 	combined.append(data[i])
	# 	combined.append(data2[i])

	combined = intersperse(lst)

	bplot = plt.boxplot(combined, labels=labels, patch_artist=True)

	# TODO: Generalize to work to color more than two datasets
	#		start with list of colors, and then set colors to colors[i % numDatasets]
	# Color the boxplots in alternating colors
	for i in range(len(bplot['boxes'])):
		patch = bplot['boxes'][i]
		if i % 2 == 0:
			patch.set_facecolor('pink')
		else:
			patch.set_facecolor('lightblue')

	# TODO: Also generalize for more datasets
	randoop = mpatches.Patch(color='pink', label='Randoop')
	orienteering = mpatches.Patch(color='lightblue', label='Orienteering')

	plt.legend(handles=[randoop, orienteering])

	# Set Percent Formatter
	plt.savefig('experiments/%s' % title, fromat='png')

	# Display plot
	plt.show()

# Accepts a list of lists, the inner lists contain coverage percentages
# Returns a list of the median coverage percentage of the inner lists
def getMedians(lst):
	lst = [sorted(coverageData) for coverageData in lst]
	return [(coverageData[5] + coverageData[6]) / 2.0 for coverageData in lst]

def main():
	# TODO: Generalize to accept any number of files
	fileName = sys.argv[1]
	fileName2 = sys.argv[2]

	# Extract infor for plot being generated from filename
	title1, data1 = readData(fileName)
	title2, data2 = readData(fileName2)

	boxplot(title, [data1, data2])
	
	# Print Medians of coverage %
	print getMedians(data1)
	print getMedians(data2)
	
if __name__ == '__main__':
    main()