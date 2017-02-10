# If you get an error saying that there is no module matplotlib
# you will need to install it, run `pip install matplotlib`,
# if you don't have pip, run `sudo yum install python-pip python-wheel`
import matplotlib.pyplot as plt
import numpy, re, sys


projects = ['Chart', 'Math', 'Time', 'Lang']
times = [50, 100, 150, 200, 250]

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
		print line
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

	title = '%s Coverage Percentage'
	return ('L', data)

def toPercent(y, position):
	s = str(100 * y)

	# The percent symbol needs escaping in latex
	if matplotlib.rcParams['text.usetex'] is True:
		return s + r'$\%$'
	else:
		return s + '%'

def main():
	fileName = sys.argv[1]

	# Extract infor for plot being generated from filename
	data = readData(fileName)


	# Plots will be in the form plt.boxplot(data, labels=labels)
	# Data will be a list of lists, each inner list is the data for one
	# boxplot, and each value in the list labels, is the label for that boxplot

	# plt.boxplot(data, labels=labels)

	plt.figure()
	plt.title('Title')
	plt.xlabel('Global Time Limit (s)')
	plt.ylabel('Coverage (%)')
	plt.boxplot(data)

	# Set Percent Formatter
	plt.savefig(fileName, fromat='png')
	plt.show()

if __name__ == '__main__':
    main()