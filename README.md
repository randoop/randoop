## DigDog

### Getting Started
Welcome to the DigDog Repository! To get started, clone this repository on your system:

`git clone https://github.com/jkotalik/randoop.git`

Then, step into the newly cloned directory, and run the evaluation script:

`cd randoop`

`./Evaluate.sh -i -b -o`

A few notes:
- the `-i` flag tells the script to perform the initial set up, which includes cloning the defects4j repository in the parent directory. If you have a folder named defects4j in the directory alongside this repository, it will remove that folder! It will then checkout the necessary projects from defects4j.
- the `-b` flag ensures that the local files will be packaged into a .jar file to be used in the experiment which gathers metrics from DigDog
- the `-o` flag tells the script to overwrite any existing data files for the experiments that it will run
- you may need to run `chmod u+x ./Evaluate.sh` to get the script to execute the first time

This will run both Randoop and DigDog (so far, just the orienteering feature that has been implemented) over the Chart defects4j project. It will run 10 trials of each tool with varying time limits (currently ranging from 50 to 250 seconds). During each run, the coverage metrics are recorded and stored in files in the `experiments/` directory. Specifically, the following files will be created:
- `experiments/Chart_Individual_Randoop_Line.txt`
- `experiments/Chart_Individual_Randoop_Branch.txt`
- `experiments/Chart_Individual_Orienteering_Line.txt`
- `experiments/Chart_Individual_Orienteering_Branch.txt`

Each file will include the coverage information for each trial that was performed for that condition, separated by the time limit. Files store data in [# covered, total #] line pairs.

This data can be easily used to create graphs, which will be generated in the `experiments/plots/` directory, named `Branch Coverage Percentage.png` and `Line Coverage Percentage.png`. Please note that running this script will take a long time. In order for the plots to be generated successfully you may need to install matplotlib and numpy for python, this can be done by calling `pip install matplotlib` and `pip install numpy`.

### Evaluation Script
To run the script and gather data on the performance of DigDog: `./Evaluate.sh`. You may need to change the permissions with `chmod u+x ./Evaluate.sh` first.

Currently accepted flags:
- `-i (--init)` Performs first-time set up of the defects4j repository, including cloning, checking out the projects, and setting up the Perl DBI. Only needs to be included if the defects4j repository already exists.
- `-b (--build)` Builds the digdog .jar file based on the current state of your local files.
- More coming, primarily for specifying which experiments to run.

#### Experiments/Output:

The output from the evaluation will be placed into intermediate result files to be processed by a graphing/tabling script. The files will be located in a `/experiments` directory at the root of this repository. The files will be named according to the specific experiment, the names will be of the form `project_experiment_condition_type.csv`.

**Complete**

Measures the metrics across the whole repository (the 4 defects4j projects that we are considering), averaging the coverage metrics across 5 trials. Time limit is number of seconds/class. The files are named according to the following grammar:
```
- Filename ::= proj_exp_condition_type.txt
- proj ::= 'Chart' | 'Math' | 'Time' | 'Lang'
- exp ::= 'Complete'
- condition ::= 'Randoop' | 'DigDog'
- type ::= 'Line' | 'Branch' | 'Faults'
```

The file will have a section for each time limit (2, 10, 30, and 60/class). Each section will start with a line that contains
`TIME x`, where x is the time limit for that section. Within each section, the data will be stored as pairs of lines, each pair starting with # covered/found on the first line, and number representing the total on the second line.

**Individual**

Measures the metrics across each of the 4 defects4j projects, considering each as its own code base. Averages the coverage metrics across 10 trials, using a global time limit.
```
- Filename ::= proj_exp_condition_time_type.txt
- proj ::= 'Chart' | 'Math' | 'Time' | 'Lang'
- exp ::= 'Individual'
- condition ::= 'Randoop' |  'Orienteering' | 'ConstantMining' | 'Bloodhound' | 'Combined'
- type ::= 'Line' | 'Branch'
```

The file will have a section for each time limit (50, 100, ..., 600 seconds). Each section will start with a line that contains
`TIME x`, where x is the time limit for that section. Within each section, the data will be stored as pairs of lines, each pair starting with # covered/found on the first line, and number representing the total on the second line.

## Randoop

Randoop is a unit test generator for Java.
It automatically creates unit tests for your classes, in JUnit format.

More about Randoop:

* [Randoop homepage](https://randoop.github.io/randoop/).
* [Randoop manual](https://randoop.github.io/randoop/manual/index.html)
* [Randoop release](https://github.com/randoop/randoop/releases/latest)
* [Randoop developer's manual](https://randoop.github.io/randoop/manual/dev.html)
* [Randoop Javadoc](https://randoop.github.io/randoop/api/)
