# If you get an error saying that there is no module matplotlib
# you will need to install it, run `pip install matplotlib`,
# if you don't have pip, run `sudo yum install python-pip python-wheel`
import matplotlib.pyplot as plt
import numpy

# Plots will be in the form plt.boxplot(data, labels=labels)
# Data will be a list of lists, each inner list is the data for one
# boxplot, and each value in the list labels, is the label for that boxplot

# plt.boxplot(data, labels=labels)
plt.title('Title')

plt.show()
