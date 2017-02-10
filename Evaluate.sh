#!/bin/bash

log() {
    echo "[DIGDOG] $1"
    echo
}

usage() {
    log "Usage: ./Evaluate.sh [-i][-b]"
}

log "Running DigDog Evaluation Script"
# Read the flag options that were passed in when the script was run.
# Options include:
    # -i (init): If set, will re-do all initialization work, including cloning the defects4j repository, initializing the defects4j projects, and creating the classlists and jarlists for each project.
    # -b (build): If set, randoop will be built using the gradle wrapper
    # -t (time): If set, will only run the experiment for the given time value.
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
        -t|--time)
            time_arg=true
            shift
            oldIFS=$IFS
            IFS=","
            declare -a specified_times=(${1})
            IFS=$oldIFS
            log "Found command line option: -t"
            log "Times set to: ${specified_times[@]}"
            ;;
        -e|--exp|--experiments)
            exp_arg=true
            shift
            oldIFS=$IFS
            IFS=","
            declare -a specified_experiments=(${1})
            IFS=$oldIFS
            log "Experiments set to ${specified_experiments[@]}"
            ;;
		*)
			log "Unknown flag: ${key}"
			exit 1
			;;
	esac
	shift
done

if [! $exp_arg ]; then
    specified_experiments=("Randoop" "Orienteering")
fi

# Set up some fixed values to be used throughout the script
work_dir=proj
projects=("Chart" "Time" "Lang" "Math")
# "Chart" "Closure" "Lang" "Math" "Time"
# Chart: 501
# Lang: 86
# Math: 520
# Time: 79

time_limits=(2 10 30 60 120)
project_sizes=(501 86 520 79)
randoop_path=`pwd`"/experiments/randoop-baseline-3.0.9.jar"
digdog_path=`pwd`"/build/libs/randoop-all-3.0.8.jar"

# If the build flag was set or if there is no digdog jar
# Build the jar from the local files
if [ $build ] || [ ! -f $digdog_path ]; then
	log "Building Randoop jar"
	./gradlew clean
	./gradlew assemble
fi

# Get 3.0.8 release of randoop, which will be used as one of the test generation tools
#if [ ! -f $randoop_path ]; then
#	wget https://github.com/randoop/randoop/releases/download/v3.0.8/randoop-3.0.8.zip
#	mkdir tmp
#	unzip randoop-3.0.8.zip -d tmp
#	rm -f randoop-3.0.8.zip

#	mv tmp/randoop-all-3.0.8.jar build/libs/randoop-baseline-3.0.8.jar
#	rm -rf tmp

#fi

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

# Determines the correct filename for the generated test handlers based
# on the current project, then changes the generated files to use that
# file name.
adjustTestNames() {
      case $project in 
        Lang|Math)
            mv $test_dir/RegressionTestDriver.java $test_dir/RegressionTest.java
            sed -i 's/RegressionTestDriver/RegressionTest/' $test_dir/RegressionTest.java
            mv $test_dir/ErrorTestDriver.java $test_dir/ErrorTest.java
            sed -i 's/ErrorTestDriver/ErrorTest/' $test_dir/ErrorTest.java
            ;;
        *)
            mv $test_dir/RegressionTestDriver.java $test_dir/RegressionTests.java
            sed -i 's/RegressionTestDriver/RegressionTests/' $test_dir/RegressionTests.java
            mv $test_dir/ErrorTestDriver.java $test_dir/ErrorTests.java
            sed -i 's/ErrorTestDriver/ErrorTests/' $test_dir/ErrorTests.java
            ;;
    esac
}

# Performs the necessary prep for a project before the test generation tool is run.
# This includes setting up the test directory (where tests will be output to), pointing the
# classpath toward the correct directory and jars, and setting variables accordingly.
prepProjectForGeneration() {
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

    # Set up local directories and jars based on the project
    # that we are currently evaluating
    curr_dir=$work_dir$project
    test_dir=${curr_dir}/gentests
    jars=`tr '\n' ':' < ${project}jars.txt`

    # Set up the test dir
    if [ -d "${test_dir}" ]; then
        rm -rf $test_dir
    fi
    mkdir $test_dir
}

# Package the test suite generated by Randoop (in $test_dir) to be
# the correct format for the defects4j coverage task
packageTests() {
    log "Packaging generated test suite into .tar.bz2 format"
    if [ -f ${curr_dir}/randoop.tar ]; then
        rm -f ${curr_dir}/randoop.tar
    fi
    tar -cvf ${curr_dir}/randoop.tar $test_dir
    if [ -f ${curr_dir}/randoop.tar.bz2 ]; then
        rm -f ${curr_dir}/randoop.tar.bz2
    fi
    bzip2 ${curr_dir}/randoop.tar
}

recordCoverage() {
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
    nums=()
    while read num; do
        log "num = $num"
        nums+=("${num}")
    done <numbers.txt
    if [ 0 -ne ${nums[1]} ]; then
        echo "${nums[1]}" >> ${line_file}
        echo "${nums[0]}" >> ${line_file}
        echo "${nums[3]}" >> ${branch_file}
        echo "${nums[2]}" >> ${branch_file}
    else
        echo "${project}, ${time}" >> ${failure_file}
        log "i = $i"
        i=$((i-1))
    fi
}

doIndividualExperiment() {
    if [ $time_arg ]; then
        indiv_time_limits=$specified_times
    else
        indiv_time_limits=(50 100 150 200 250 300 350 400 450 500 550 600)
    fi

    log "Running Individual Experiment with $1"
    exp_dir="../randoop/experiments"
    failure_file="${exp_dir}/failure_counts.txt"

    if [ -f ${failure_file} ]; then
        rm -f ${failure_file}
    fi
    if [ ! -d ${exp_dir} ]; then
        mkdir ${exp_dir}
    fi

    for project in ${projects[@]}; do
        #TODO: introduce some logic to not clobber files, incrementing a counter
        # and appending that value to the filename until we find a filename that doesn't conflict
        line_file="${exp_dir}/${project}_Individual_${1}_Line.txt"
        log "Line file is: ${line_file}"
        branch_file="${exp_dir}/${project}_Individual_${1}_Branch.txt"
        log "Branch file is: ${branch_file}"
        
        prepProjectForGeneration
        for time in ${indiv_time_limits[@]}; do
            echo "TIME ${time}" >> ${line_file}
            echo "TIME ${time}" >> ${branch_file}
            i=0
            while [ $i -lt 10 ]; do
                case $1 in
                    Randoop)
                        log "Running base Randoop with time limit=${time}, ${project} #${i}"
			            java -ea -classpath ${jars}${curr_dir}/${classes_dir}:$randoop_path randoop.main.Main gentests --classlist=${project}classlist.txt --literals-level=CLASS --literals-file=CLASSES --timelimit=${time} --junit-reflection-allowed=false --junit-package-name=${curr_dir}.gentests --randomseed=$RANDOM
                        ;;
                    *)
			            java -ea -classpath ${jars}${curr_dir}/${classes_dir}:$digdog_path randoop.main.Main gentests --classlist=${project}classlist.txt --literals-level=CLASS --literals-file=CLASSES --timelimit=${time} --junit-reflection-allowed=false --junit-package-name=${curr_dir}.gentests --randomseed=$RANDOM
                        ;;
                esac
                adjustTestNames
                packageTests
                recordCoverage
                i=$((i+1))
            done
        done
    done
}

# Perform each experiment that was specified
for exp in ${exp_arg[@]}; do
    doIndividualExperiment $exp
done
exit 0

# Iterate over each time limit. For each time limit, perform 10 iterations of test generation and coverage calculations with Randoop.
# TODO: integrate the other tools into the evaluation framework here
for time in ${time_limits[@]}; do
	for i in `seq 1 10`; do
		for project in ${projects[@]}; do
            prepProjectForGeneration
			log "Running Randoop with time limit set to ${time}, project ${project} iteration #${i}"
			log "Randoop jar location: ${digdog_path}"
			java -ea -classpath ${jars}${curr_dir}/${classes_dir}:$digdog_path randoop.main.Main gentests --classlist=${project}classlist.txt --literals-level=CLASS --timelimit=10 --junit-reflection-allowed=false --junit-package-name=${curr_dir}.gentests --literals-file=CLASSES --randomseed=$RANDOM
            
            adjustTestNames

			# Package the test suite generated by Randoop (in $test_dir) to be
			# the correct format for the defects4j coverage task
			log "Packaging generated test suite into .tar.bz2 format"
            if [ -f ${curr_dir}/randoop.tar ]; then
                rm -f ${curr_dir}/randoop.tar
            fi
			tar -cvf ${curr_dir}/randoop.tar $test_dir
			if [ -f ${curr_dir}/randoop.tar.bz2 ]; then
                rm -f ${curr_dir}/randoop.tar.bz2
            fi
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
			rm -f "${curr_dir}/randoop.tar.bz2"
		done
	done
done
