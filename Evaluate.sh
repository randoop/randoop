#!/bin/bash

work_dir=tmp
projects=("Chart" "Closure" "Lang" "Math" "Time")

# Set up defects4j repo
cd ..

if [ ! -d "defects4j" ]; then

	git clone https://github.com/rjust/defects4j
	cd defects4j
	./init.sh
	export PATH=$PATH:./framework/bin

	# Get 3.0.8 release of randoop for running tests
	# TODO: figure out how to get compile a jar from our version of randoop in order to use that
	wget https://github.com/randoop/randoop/releases/download/v3.0.8/randoop-3.0.8.zip
	unzip randoop-3.0.8.zip

	# Install Perl DBI
	yes | sudo perl -MCPAN -e 'install Bundle::DBI'
else
	cd defects4j
fi

# Create working directory for running tests on Defects4j projects
if [ ! -d "tmp" ]; then
	mkdir $work_dir
fi

# Compile Defects4j projects and then run generated tests on them
for project in $projects
do
	defects4j checkout -p $project -v 1b -w $work_dir
	defects4j compile -w $work_dir
	defects4j coverage -w $work_dir
done

# TODO: specify package so that defects4j can correctly run the tests
#java -ea -classpath randoop-all-3.0.8.jar randoop.main.Main gentests --classlist=myclasses.txt --literals-level=CLASS
