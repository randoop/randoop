import sys, numpy, os

header1 = ''
header2 = ''
tables = []

def readCsv(filePrefix, numFiles):
	lineFile = open('%s Line Coverage Percentage.csv' % filePrefix, 'r')
	branchFile = open('%s Branch Coverage Percentage.csv' % filePrefix, 'r')

	lineLines = lineFile.readlines()
	branchLines = branchFile.readlines()

	headerParts = lineLines[0].lstrip().rstrip().split(',')[:-1]
	header1 = headerParts[0]
	header2 = ','

	for i in range(2):
		for j in range(1, len(headerParts)):
			header1 = '%s, %s' % (header1, ('Line Coverage (%)' if i == 0 else 'Branch Coverage (%)') if j == 1 else '')
			
			header2 = header2 + ('%s,' % headerParts[j])

	data = []
	for i in range(1, len(lineLines)):
		data.append([])
		for j in range(2):
			dataPoints = lineLines[i].lstrip().rstrip().split(',')
			if j == 1:
				# Ignore time value from branchLine
				dataPoints = branchLines[i].lstrip().rstrip().split(',')[1:]

			# Remove empty string at the end of dataPoints
			dataPoints = dataPoints[:-1]

			for dataPoint in dataPoints:
				data[i - 1].append(float(dataPoint))

	data = [numpy.array(x) for x in data]
	data = numpy.array(data)

	return (header1, header2, data)

def writeCsv(header1, header2, table):
	try:
		os.remove('csv/Average.csv')
	except OSError:
		pass

	f = open('csv/Average.csv', 'w+')

	print >> f, header1
	print >> f, header2

	for row in table:
		for i in range(len(row)):
			if i == 0:
				print >> f, '%d,' % int(row[i]),
			else:
				print >> f, '%f,' % (int(row[i] * 100) / 100.0),
		print >> f

def main():
	numFiles = len(sys.argv) - 1

	for i in range(1, len(sys.argv)):
		filePrefix = sys.argv[i]
		
		header1, header2, table = readCsv(filePrefix, numFiles)
		tables.append(table)

	# Cut down all datasets to the size of the smallest dataset
	minLength = len(tables[0])
	for i in range(len(tables)):
		minLength = min(minLength, len(tables[i]))

	for i in range(len(tables)):
		tables[i] = tables[i][:minLength]

	resultTable = tables[0]
	for i in range(1, len(tables)):
		resultTable = resultTable + tables[i]

	resultTable = resultTable / len(tables)

	writeCsv(header1, header2, resultTable)

if __name__ == '__main__':
    main()