#!/bin/bash

init=false
usage() {
	echo "Usage: ./Evaluate.sh [-i]"
}

while getopts ":i" opt; do
	case $opt in
		i)
			init=true
			;;
		\?)
			echo "Unknown flag"
			usage
			exit 1
			;;
		:)
			echo "No flag" >&2
			;;
	esac
done

work_dir=tmp
projects=("Closure")
# "Chart" "Closure" "Lang" "Math" "Time"
time_limits=(2 10 30 60 120)

# Set up defects4j repo
cd ..

#if [ $init ]; then
#	rm -rf defects4j
#fi

if [ ! -d "defects4j" ] ; then

	git clone https://github.com/rjust/defects4j
	cd defects4j
	./init.sh
	export PATH=$PATH:./framework/bin

	# Get 3.0.8 release of randoop for running tests
	# TODO: figure out how to get compile a jar from our version of randoop in order to use that
	wget https://github.com/randoop/randoop/releases/download/v3.0.8/randoop-3.0.8.zip
	unzip randoop-3.0.8.zip

	# Install Perl DBI
	#printf 'y\ny\n\n' | perl -MCPAN -e 'install Bundle::DBI'
else
	cd defects4j
	export PATH=$PATH:./framework/bin
fi

# Compile Defects4j projects and then run generated tests on them
for project in ${projects[@]}; do
	# Create working directory for running tests on Defects4j projects
	curr_dir=$work_dir$project
	test_dir=${curr_dir}/gentests
	rm -rf $curr_dir
	mkdir $curr_dir

	# Checkout and compile current project
	defects4j checkout -p $project -v 1b -w $curr_dir
	defects4j compile -w $curr_dir

	# Run randoop on the current project, outputting the tests to $work_dir$project/test
	# TODO: generalize build/classes
	find $curr_dir/build/classes/ -name \*.class >${project}classlist.txt
	sed -i 's/\//\./g' ${project}classlist.txt
	sed -i 's/\(^.*build\.classes\.\)//g' ${project}classlist.txt
	sed -i 's/\.class$//g' ${project}classlist.txt
	sed -i '/\$/d' ${project}classlist.txt

	find $curr_dir -name \*.jar > ${project}jars.txt
done

for time in ${time_limits[@]}; do
	for i in `seq 1 10`; do
		for project in ${projects[@]}; do
			curr_dir=$work_dir$project
			test_dir=${curr_dir}/gentests
			jars=`tr '\n' ':' < ${project}jars.txt`

			# Run randoop on the current project, outputting the tests to $work_dir$project/test
			rm -rf $test_dir
			mkdir $test_dir

			# TODO: figure out why constant mining doesn't work
			java -ea -classpath ${jars}${curr_dir}/build/classes:randoop-all-3.0.8.jar randoop.main.Main gentests --classlist=${project}classlist.txt --literals-level=CLASS --timelimit=20 --junit-reflection-allowed=false --junit-package-name=${curr_dir}.gentests

			mv $test_dir/RegressionTestDriver.java $test_dir/RegressionTests.java
			sed -i 's/RegressionTestDriver/RegressionTests/' $test_dir/RegressionTests.java
			
			mv $test_dir/ErrorTestDriver.java $test_dir/ErrorTests.java
			sed -i 's/ErrorTestDriver/ErrorTests/' $test_dir/ErrorTests.java

			tar -cvf ${curr_dir}/randoop.tar $test_dir
			bzip2 ${curr_dir}/randoop.tar

			defects4j coverage -w $curr_dir -s ${curr_dir}/randoop.tar.bz2 > results.txt
			grep 'Lines total' results.txt > numbers.txt
			grep 'Lines covered' results.txt >> numbers.txt
			grep 'Conditions total' results.txt >> numbers.txt
			grep 'Conditions covered' results.txt >> numbers.txt

			sed -i 's/[^0-9]//g' numbers.txt
			cat numbers.txt

			rm "${curr_dir}/randoop.tar.bz2"
		done
	done
done