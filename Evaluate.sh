#!/bin/bash

log() {
    echo "\n[DIGDOG] $1"
}

usage() {
    log "Usage: ./Evaluate.sh [-i][-b]"
}



log "Running DigDog Evaluation Script"
# Read the flag options that were passed in when the script was run.
# Options include:
    # -i (init): If set, will re-do all initialization work, including cloning the defects4j repository, initializing the defects4j projects, and creating the classlists and jarlists for each project.
    # -b (build): If set, randoop will be built using the gradle wrapper
    # TODO: Add more options.
while [[ $# -gt 0 ]]; do
	key="$1"
	case $key in
		-i|--init)
			init=true
			log "Found command line option: -i"
			;;
		-b|--build)
			build=true
			log "Found command line option: -b"
			;;
		*)
			log "Unknown flag: ${key}"
			exit 1
			;;
	esac
	shift
done

# Set up some fixed values to be used throughout the script
work_dir=proj
projects=("Chart" "Lang" "Math" "Time")
# "Chart" "Closure" "Lang" "Math" "Time"
# Chart: 501
# Lang: 86
# Math: 520
# Time: 79

time_limits=(2 10 30 60 120)
project_sizes=(501 86 520 79)
randoop_path=`pwd`"/build/libs/randoop-baseline-3.0.8.jar"
digdog_path=`pwd`"/build/libs/randoop-all-3.0.8.jar"

# If the build flag was set or if there is no randoop jar
# Build the randoop jar
if [ $build ] || [ ! -f $digdog_path ]; then
	log "Building Randoop jar"
	./gradlew clean
	./gradlew assemble
fi

# Get 3.0.8 release of randoop, which will be used as one of the test generation tools
if [ ! -f $randoop_path ]; then
	wget https://github.com/randoop/randoop/releases/download/v3.0.8/randoop-3.0.8.zip
	mkdir tmp
	unzip randoop-3.0.8.zip -d tmp
	rm -f randoop-3.0.8.zip

	mv tmp/randoop-all-3.0.8.jar build/libs/randoop-baseline-3.0.8.jar
	rm -rf tmp
fi

# Go up one level to the directory that contains this repository
cd ..
log "Stepping up to the containing directory"

# If the init flag is set, we want to re-start the initial process, so
# we remove the defects 4j repository if it already exists. This is necessary
# since we will be re-cloning the repository.
if [ $init ]; then
    if [ -d "defects4j" ]; then
        log "Init flag was set and defects4j repository existed, removing..."
        rm -rf defects4j
    fi
fi

# If there is no defects4j repository sitting alongside our randoop repository, we need to perform initial set up.
if [ ! -d "defects4j" ] ; then
    log "No defects4j repository found, setting init to true."
    init=true
fi

# Perform initialization process, cloning the defects4j repository,
# initializing the repository, and installing the perl DBI.
if [ $init ]; then
    log "Preparing the defects4j repository..."
    # Clone the defects4j repository, and run the init script
	git clone https://github.com/rjust/defects4j
	cd defects4j
	./init.sh

	# Install Perl DBI
	printf 'y\ny\n\n' | perl -MCPAN -e 'install Bundle::DBI'
else
	# If we already have the defects4j repository cloned, we just step inside
	log "Defects4j repository already exists, assuming that set up has already been performed. If this is in error, re-run this script with the -i option"
	cd defects4j
fi
export PATH=$PATH:./framework/bin

# Compile Defects4j projects and then run generated tests on them
if [ $init ]; then
    for project in ${projects[@]}; do
	    # Set the directory of classes based on the structure of the project
	    case $project in
		    Chart)
			    classes_dir="build"
			    ;;
		    Closure)
			    classes_dir="build/classes"
			    ;;
		    *)
			    classes_dir="target/classes"
			    ;;
	    esac

	    # Create working directory for running tests on Defects4j projects
	    curr_dir=$work_dir$project
	    test_dir=${curr_dir}/gentests
	    log "Setting directories for new project: ${project}..."
	    # If our project directory already exists, we remove it so we can start fresh
	    if [ -d "${curr_dir}" ]; then
		    log "Working directory already existed, removing it...."
		    rm -rf $curr_dir
	    fi
	    log "Initializing working directory (${curr_dir})..."
	    mkdir $curr_dir

	    # Checkout and compile current project
	    defects4j checkout -p $project -v 1b -w $curr_dir
	    defects4j compile -w $curr_dir

	    # Create the classlist and jar list for this project.
	    log "Setting up class list for project ${project}"
	    find ${curr_dir}/${classes_dir}/ -name \*.class >${project}classlist.txt
	    sed -i 's/\//\./g' ${project}classlist.txt
	    sed -i 's/\(^.*build\.\)//g' ${project}classlist.txt
	    sed -i 's/\(^.*classes\.\)//g' ${project}classlist.txt
	    sed -i 's/\.class$//g' ${project}classlist.txt
	    sed -i '/\$/d' ${project}classlist.txt

	    # Get a list of all .jar files in this project, to be added to the
	    # classpath when running randoop/digdog.
	    log "Setting up jar list for project ${project}"
	    find $curr_dir -name \*.jar > ${project}jars.txt
    done
fi

# Iterate over each time limit. For each time limit, perform 10 iterations of test generation and coverage calculations with Randoop.
# TODO: integrate the other tools into the evaluation framework here
for time in ${time_limits[@]}; do
	for i in `seq 1 10`; do
		for project in ${projects[@]}; do
			case $project in
				Chart)
					classes_dir="build"
					;;
				Closure)
					classes_dir="build/classes"
					;;
				*)
					classes_dir="target/classes"
					;;
			esac

			log "Performing evaluation #${i} for project ${project}..."

			# Set up local variables based on the project name that we are currently evaluating
			curr_dir=$work_dir$project
			test_dir=${curr_dir}/gentests
			jars=`tr '\n' ':' < ${project}jars.txt`

			# Set up the test dir
			if [ -d "${test_dir}" ]; then
				log "Test directory ${test_dir} existed, clearing..."
				rm -rf $test_dir
			fi
			log "Setting up test directory ${test_dir}"
			mkdir $test_dir

			log "Running Randoop with time limit set to ${time}, project ${project} iteration #${i}"
			log "Randoop jar location: ${digdog_path}"
			java -ea -classpath ${jars}${curr_dir}/${classes_dir}:$digdog_path randoop.main.Main gentests --classlist=${project}classlist.txt --literals-level=CLASS --timelimit=20 --junit-reflection-allowed=false --junit-package-name=${curr_dir}.gentests --literals-file=CLASSES

			# Change the generated test handlers to end with "Tests.java"
			# so they are picked up by the ant task for running tests"
			mv $test_dir/RegressionTestDriver.java $test_dir/RegressionTests.java
			sed -i 's/RegressionTestDriver/RegressionTests/' $test_dir/RegressionTests.java
			mv $test_dir/ErrorTestDriver.java $test_dir/ErrorTests.java
			sed -i 's/ErrorTestDriver/ErrorTests/' $test_dir/ErrorTests.java

			# Package the test suite generated by Randoop (in $test_dir) to be
			# the correct format for the defects4j coverage task
			log "Packaging generated test suite into .tar.bz2 format"
			tar -cvf ${curr_dir}/randoop.tar $test_dir
			bzip2 ${curr_dir}/randoop.tar

			# Run the defects4j coverage task over the newly generated test suite.
			# Results are stored into results.txt, and the specific lines used to
			# generate coverage are put into numbers.txt
			defects4j coverage -w $curr_dir -s ${curr_dir}/randoop.tar.bz2 > results.txt
			grep 'Lines total' results.txt > numbers.txt
			grep 'Lines covered' results.txt >> numbers.txt
			grep 'Conditions total' results.txt >> numbers.txt
			grep 'Conditions covered' results.txt >> numbers.txt

			# Remove everything but the digits from the numbers.txt file. This leaves
			# a set of 4 lines, displaying:
				# Total number of lines
				# Number of lines covered
				# Total number of conditions
				# Number of conditions covered
			sed -i 's/[^0-9]//g' numbers.txt
			cat numbers.txt

			# Remove test suite archive so we can generate again on the next iteration
			log "Removing archive of the generated test suite..."
			rm -f "${curr_dir}/randoop.tar.bz2"
		done
	done
done
