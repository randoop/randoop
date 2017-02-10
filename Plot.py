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

def main():
	fileName = sys.argv[1]
	fileName2 = sys.argv[2]

	# Extract infor for plot being generated from filename
	title, data = readData(fileName)
	title2, data2 = readData(fileName2)

	# Print out median coverage %
	# data = [sorted(lst) for lst in data]
	# data = [lst[5] + lst[6] / 2.0 for lst in data]
	# print data
	# data2 = [sorted(lst) for lst in data2]
	# data2 = [lst[5] + lst[6] / 2.0 for lst in data2]
	# print data2

	# Plots will be in the form plt.boxplot(data, labels=labels)
	# Data will be a list of lists, each inner list is the data for one
	# boxplot, and each value in the list labels, is the label for that boxplot

	# plt.boxplot(data, labels=labels)

	plt.figure()
	plt.title(title)
	plt.xlabel('Global Time Limit (s)')
	plt.ylabel('Coverage (%)')
	plt.ylim(0, 30)

	combined = []
	for i in range(len(data)):
		combined.append(data[i])
		combined.append(data2[i])

	bplot = plt.boxplot(combined, labels=labels, patch_artist=True)

	print bplot['boxes']
	for i in range(len(bplot['boxes'])):
		patch = bplot['boxes'][i]
		if i % 2 == 0:
			patch.set_facecolor('pink')
		else:
			patch.set_facecolor('lightblue')


	randoop = mpatches.Patch(color='pink', label='Randoop')
	orienteering = mpatches.Patch(color='lightblue', label='Orienteering')

	plt.legend(handles=[randoop, orienteering])

	# Set Percent Formatter
	plt.savefig('experiments/%s' % title, fromat='png')
	plt.show()

if __name__ == '__main__':
    main()