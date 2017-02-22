#!/bin/bash

log() {
    echo "[DIGDOG] $1"
    echo
}

usage() {
    log "Usage: ./Evaluate.sh [-i][-b]"
}

#initialize some default options before parsing the command line arguments
specified_experiments=("Randoop" "Orienteering")
projects=("Chart" "Lang" "Math" "Time")
# "Chart" "Lang" "Math" "Time"

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
		-o|--overwrite)
			overwrite=true
			log "Found command line option: -o"
			;;
        -t|--time)
            time_arg=true
            shift
            oldIFS=$IFS
            IFS=","
            declare -a specified_times=(${1})
            IFS=$oldIFS
            log "Times set to: [${specified_times[*]}]"
            ;;
        -e|--exp|--experiments)
            shift
            oldIFS=$IFS
            IFS=","
            declare -a specified_experiments=(${1})
            IFS=$oldIFS
            log "Experiments set to [${specified_experiments[*]}]"
            ;;
        -p|--proj|--projects)
            projects_arg=true
            shift
            oldIFS=$IFS
            IFS=","
            declare -a projects=(${1})
            IFS=$oldIFS
            log "Projects set to [${projects[*]}]"
            ;;
        -c|--complete|--complete)
            run_complete_experiment=true
            log "Complete experiments set"
            ;;
        -f|--faults)
            run_fault_detection=true
            log "Setting fault detection to true"
            ;;
		*)
			log "Unknown flag: ${key}"
			exit 1
			;;
	esac
	shift
done

if [ $overwrite ]; then
    log "Overwrite enabled, will remove data files before metrics are recorded."
fi

# Set up some fixed values to be used throughout the script
work_dir=proj

randoop_path=`pwd`"/experiments/lib/randoop-baseline-3.0.9.jar"
digdog_path=`pwd`"/build/libs/randoop-all-3.0.8.jar"
java_path=`pwd`"/experiments/lib/jdk1.7.0/bin/java"
plot_path=`pwd`"/Plot.py"

chmod u+x $java_path
# If the build flag was set or if there is no digdog jar
# Build the jar from the local files
if [ $build ] || [ ! -f $digdog_path ]; then
	log "Building Randoop jar"
	./gradlew clean
	./gradlew assemble
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
    printf 'y\n\n' | perl -MCPAN -e 'install DBD::CSV'
else
	# If we already have the defects4j repository cloned, we just step inside
	log "Defects4j repository already exists, assuming that set up has already been performed. If this is in error, re-run this script with the -i option"
	cd defects4j
fi
export PATH=$PATH:`pwd`/framework/bin

# Check out the project for fault detection
checkoutProject() {
    curr_dir=$work_dir$project
    test_dir=${curr_dir}/gentests


    # If our project directory already exists, we remove it so we can start fresh
    if [ -d "${curr_dir}" ]; then
        rm -rf $curr_dir
    fi
    log "Initializing working directory for ${project}${version}..."
    mkdir $curr_dir

    # Checkout and compile current project
    defects4j checkout -p $project -v ${version}${1} -w $curr_dir
    defects4j compile -w $curr_dir
}

# Compile Defects4j projects and then run generated tests on them
if [ $init ] || [ $projects_arg ]; then
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

	    # Checkout and compile current project
        version=1
	    checkoutProject "b"

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
    jars=`tr '\n' ':' < $1`

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

    # remove the existing tests.
    # If Randoop finishes early (ie, because of a flaky test)
    # failing to delete the generated tests would cause them to
    # be mistakenly re-used on the next coverage evaluation
    rm $test_dir/*
}

packageTestsForFaultDetection() {
    rm -f $test_dir/*Regression*
    packageTests

    log "Renaming packaged tests for fault detection task"
    log "${project}-${version}f-${time}.tar.bz2"
    fault_suite_path=${curr_dir}/${project}-${version}f-${time}.tar.bz2
    mv ${curr_dir}/randoop.tar.bz2 $fault_suite_path
}

countFaultDetection() {
	perl ./framework/util/fix_test_suite.pl -p $project -d $curr_dir
    log "finished fixing test suite"
    rm -rf ../randoop/experiments/fault_detection
    perl ./framework/bin/run_bug_detection.pl -p $project -d ${curr_dir} -o ../randoop/experiments/fault_detection -v ${version}f
    fault_data=`cat ../randoop/experiments/fault_detection/bug_detection`
    echo "${fault_data}" >> $fault_file
}

recordCoverage() {
    # Run the defects4j coverage task over the newly generated test suite.
    # Results are stored into results.txt, and the specific lines used to
    # generate coverage are put into numbers.txt
    defects4j coverage -i ${project}classlist.txt -w $curr_dir -s ${curr_dir}/randoop.tar.bz2 > ${curr_dir}/results.txt
    grep 'Lines total' ${curr_dir}/results.txt > ${curr_dir}/numbers.txt
    grep 'Lines covered' ${curr_dir}/results.txt >> ${curr_dir}/numbers.txt
    grep 'Conditions total' ${curr_dir}/results.txt >> ${curr_dir}/numbers.txt
    grep 'Conditions covered' ${curr_dir}/results.txt >> ${curr_dir}/numbers.txt

    # Remove everything but the digits from the numbers.txt file. This leaves
    # a set of 4 lines, displaying:
        # Total number of lines
        # Number of lines covered
        # Total number of conditions
        # Number of conditions covered
    sed -i 's/[^0-9]//g' ${curr_dir}/numbers.txt
    cat ${curr_dir}/numbers.txt
    nums=()
    while read num; do
        log "num = $num"
        nums+=("${num}")
    done <${curr_dir}/numbers.txt
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

doCompleteExperiment() {
    doCoverage $1 "Complete" 5
}

doIndividualExperiment() {
    doCoverage $1 "Individual" 10
}

doCoverage() {
    if [ $time_arg ]; then
        time_limits=$specified_times
    elif [ $2 = "Complete" ]; then
        time_limits=(2 10 30 60)
    else
        time_limits=(50 100 150 200 250 300 350 400 450 500 550 600)
    fi

    log "Running ${2} Experiment with $1"
    log "Times are: [${time_limits[*]}]"
    exp_dir="../randoop/experiments"
    failure_file="${exp_dir}/failure_counts.txt"

    if [ -f ${failure_file} ]; then
        rm -f ${failure_file}
    fi
    if [ ! -d ${exp_dir} ]; then
        mkdir ${exp_dir}
    fi

    for project in ${projects[@]}; do
        line_file="${exp_dir}/${project}_${2}_${1}_Line.txt"
        log "Line file is: ${line_file}"
        branch_file="${exp_dir}/${project}_${2}_${1}_Branch.txt"
        log "Branch file is: ${branch_file}"

        if [ $overwrite ];then
            if [ -f $line_file ]; then
                rm $line_file
            fi
            if [ -f $branch_file ]; then
                rm $branch_file
            fi
        fi

        prepProjectForGeneration ${project}jars.txt
        for time in ${time_limits[@]}; do
            echo "TIME ${time}" >> ${line_file}
            echo "TIME ${time}" >> ${branch_file}
            i=1

# Chart: 501
# Lang: 86
# Math: 520
# Time: 79
            if [ $2 = "Complete" ]; then
                case $project in
                    Chart)
                        time=$((time*501))
                        ;;
                    Math)
                        time=$((time*520))
                        ;;
                    Time)
                        time=$((time*79))
                        ;;
                    Lang)
                        time=$((time*86))
                        ;;
                esac
            fi
            while [ $i -le $3 ]; do
                case $1 in
                    Randoop)
                        log "Running base Randoop with time limit=${time}, ${project} #${i}"
			            $java_path -ea -classpath ${jars}${curr_dir}/${classes_dir}:$randoop_path randoop.main.Main gentests --classlist=${project}classlist.txt --literals-level=CLASS --literals-file=CLASSES --timelimit=${time} --junit-reflection-allowed=false --junit-package-name=${curr_dir}.gentests --randomseed=$RANDOM --ignore-flaky-tests=true
                        ;;
                    Orienteering)
                        log "Running digDog with orienteering, time limit=${time}, ${project} #${i}"
			            $java_path -ea -classpath ${jars}${curr_dir}/${classes_dir}:$digdog_path randoop.main.Main gentests --classlist=${project}classlist.txt --literals-level=CLASS --literals-file=CLASSES --timelimit=${time} --junit-reflection-allowed=false --junit-package-name=${curr_dir}.gentests --randomseed=$RANDOM --orienteering=true --ignore-flaky-tests=true
                        ;;
                    *)
                        log "Unkown experiment condition"
                        exit 1
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

initFaultDetectionClasses() {
    # Create the classlist and jar list for this project.
    log "Setting up class list for project ${project}_${version}b"
    cd ${curr_dir}
    defects4j export -p tests.trigger -o ${project}_${version}b_relevant_tests.txt
    log "exported tests"
    test_file=`cat ${project}_${version}b_relevant_tests.txt`
    class_list_file="${project}_${version}_classlist.txt"
    if [ -f $class_list_file ]; then
        rm -f $class_list_file
    fi
    for line in $test_file; do
        defects4j monitor.test -t $line
        cat loaded_classes.src >> $class_list_file
    done
    cd ..
    log "Now displaying all relevant classes:"
    cat $curr_dir/$class_list_file
    if [ ! -d "classList" ]; then
        mkdir classList
    fi
    if [ ! -d "jarList" ]; then
        mkdir jarList
    fi
    mv $curr_dir/$class_list_file classList
    # Get a list of all .jar files in this project, to be added to the
    # classpath when running randoop/digdog.
    log "Setting up jar list for project ${project}_${version}"
    find $curr_dir -name \*.jar > jarList/${project}_${version}_jars.txt
}

checkForEmptyRandoopExec () {
    log "checking if randoop execution was empty"
    cat $randoop_output_file
    if grep -Fxq "No classes to test" $randoop_output_file ; then
        randoop_empty=true
    else
        randoop_empty=false
    fi
    log "randoop_empty is $randoop_empty"
}

doFaultDetection() {
    if [ $time_arg ]; then
        time_limits=$specified_times
    else
        time_limits=(120)
    fi

    log "Running Fault Detection with $1"
    exp_dir="../randoop/experiments"

    if [ ! -d ${exp_dir} ]; then
        mkdir ${exp_dir}
    fi

    for project in ${projects[@]}; do
        #TODO: introduce some logic to not clobber files, incrementing a counter
        # and appending that value to the filename until we find a filename that doesn't conflict
        fault_file="${exp_dir}/${project}_Fault_${1}.txt"
        log "Fault file is: ${fault_file}"
        randoop_output_file="${exp_dir}/Randoop_output.txt"

        if [ $overwrite ];then
            if [ -f $fault_file ]; then
                rm $fault_file
            fi
        fi

        case $project in
            Chart)
                num_versions=26
                ;;
            Math)
                num_versions=106
                ;;
            Lang)
                num_versions=65
                ;;
            Time)
                num_versions=27
                ;;
            *)
                log "Unknown project"
                exit 1
                ;;
        esac

        version=2
        while [ "$version" -le "$num_versions" ]; do
            jar_path="jarList/${project}_${version}_jars.txt"
            classlist_path="classList/${project}_${version}_classlist.txt"
            if [ ! -f $jar_path ] || [ ! -f $classlist_path ]; then
                checkoutProject "f"
                initFaultDetectionClasses
            fi
            
            checkoutProject "b"
            prepProjectForGeneration ${jar_path}
            for time in ${time_limits[@]}; do
                echo "TIME ${time}" >> ${fault_file}
                i=1
                while [ $i -le 5 ]; do
                    case $1 in
                        Randoop)
                            log "Running base Randoop with time limit=${time}, ${project} #${i}"
                            $java_path -ea -classpath ${jars}${curr_dir}/${classes_dir}:$randoop_path randoop.main.Main gentests --classlist=${classlist_path} --literals-level=CLASS --literals-file=CLASSES --timelimit=${time} --junit-reflection-allowed=false --junit-package-name=${curr_dir}.gentests --randomseed=$RANDOM --ignore-flaky-tests=true > $randoop_output_file
                            checkForEmptyRandoopExec
                            if [ "$randoop_empty" = true ]; then
                                log "Randoop was empty for project ${project} v ${version}, moving on"
                                i=5
                            fi
                            ;;
                        *)
                            log "Unknown condition in fault detection experiment"
                            exit 1
                            ;;
                    esac
                    if [ "$randoop_empty" = false ]; then
                        log "randoop was not empty for project ${project} v ${version}, attempting fault detection."
                        adjustTestNames
                        packageTestsForFaultDetection
                        countFaultDetection
                        if grep -Fxq "Fail" "$fault_data" ; then
                            log "found failing test on ${project} ${version}"
                            i=5
                        fi
                    fi
                    i=$((i+1))
                done
            done

            version=$((version+1))
        done
    done
}

if [ $run_fault_detection ]; then
    doFaultDetection "Randoop"
    exit 0
fi

if [ $run_complete_experiment ]; then
    for exp in ${specified_experiments[@]}; do
        doCompleteExperiment $exp
    done
else
    # Perform each experiment that was specified
    for exp in ${specified_experiments[@]}; do
        doIndividualExperiment $exp
    done
fi
