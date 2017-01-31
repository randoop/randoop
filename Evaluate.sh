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

# Compile Defects4j projects and then run generated tests on them
for project in $projects
do
	# Create working directory for running tests on Defects4j projects
	curr_dir=$work_dir$project
	rm -rf $curr_dir
	mkdir $curr_dir

	# Checkout and compile current project
	defects4j checkout -p $project -v 1b -w $curr_dir
	defects4j compile -w $curr_dir
	defects4j coverage -w $curr_dir

	# Run randoop on the current project, outputting the tests to $work_dir$project/test
	mkdir $curr_dir/test
	find $curr_dir/build/ -name \*.class >myclasslist.txt
	sed -i 's/\//\./g' myclasslist.txt
	sed -i 's/\(^.*build\.\)//g' myclasslist.txt
	sed -i 's/\.class//g' myclasslist.txt
	java -ea -classpath ${curr_dir}/build/:randoop-all-3.0.8.jar randoop.main.Main gentests --classlist=myclasslist.txt --literals-level=CLASS --junit-output-dir=$curr_dir/test
done
