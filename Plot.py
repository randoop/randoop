# If you get an error saying that there is no module matplotlib
# you will need to install it, run `pip install matplotlib`,
# if you don't have pip, run `sudo yum install python-pip python-wheel`
import matplotlib.pyplot as plt
import numpy, re

# Plots will be in the form plt.boxplot(data, labels=labels)
# Data will be a list of lists, each inner list is the data for one
# boxplot, and each value in the list labels, is the label for that boxplot

# plt.boxplot(data, labels=labels)
plt.title('Title')

plt.show()

projects = ['Chart', 'Math', 'Time', 'Lang']

def generatePlot(fileName, project, exp, condition, metric):
	f = open(fileName, 'r')

	if exp == 'Complete':
		pass	
	elif exp == 'Individual':
		data = {2:[0, 0], 10:[0, 0], 30:[0, 0], 60:[0, 0]}

	lines = f.readlines()
	time = lines[0]
	for i in range(0, len(lines), 2):
		if lines[i] == '\n':
			i += 1
			time = lines[i]

			i += 1
		
		data[time][0] += int(lines[i])
		data[time][1] += int(lines[i + 1])

def main():
	fileName = sys.argv[1]

	# Extract infor for plot being generated from filename
	generatePlot(fileName, re.split('[_.]', filename))

if __name__ == '__main__':
    main()
