## Randoop

Randoop is a unit test generator for Java.
It automatically creates unit tests for your classes, in JUnit format.

More about Randoop:

* [Randoop homepage](https://randoop.github.io/randoop/).
* [Randoop manual](https://randoop.github.io/randoop/manual/index.html)
* [Randoop release](https://github.com/randoop/randoop/releases/latest)
* [Randoop developer's manual](https://randoop.github.io/randoop/manual/dev.html)
* [Randoop Javadoc](https://randoop.github.io/randoop/api/)


## DigDog

### Evaluation Script
To run the script: `./Evaluate.sh`. You may need to change the permissions with `chmod u+x ./Evaluate.sh` first.

Currently accepted flags:
- `-i (--init)` Performs first-time set up of the defects4j repository, including cloning, checking out the projects, and setting up the Perl DBI. Only needs to be included if the defects4j repository already exists.
- `-b (--build)` Builds the digdog .jar file based on the current state of your local files.
- More coming, primarily for specifying which experiments to run.

#### Experiments/Output:

The output from the evaluation will be placed into intermediate result files to be processed by a graphing/tabling script. The files will be located in a `/experiments` directory at the root of this repository. The files will be named according to the specific experiment, the names will be of the form `project_experiment_condition_time_type.csv`.

- Filename ::= proj_exp_time_type.csv
- proj ::= 'Chart' | 'Math' | 'Time' | 'Lang'
- exp ::= 'Complete_'cCondition | 'Individual_'iCondition
- cCondition ::= 'Randoop' | 'DigDog'
- iCondition ::= 'Randoop' | 'Orienteering' | 'ConstantMining' | 'Bloodhound' | 'Combined'
- time ::= 2 | 10
- type ::= Line | Branch | Faults

**Complete**

Measures the metrics across the whole repository (the 4 defects4j projects that we are considering), averaging the coverage metrics across 5 trials. Time limit is number of seconds/class. The files are named according to the following grammar:
```
- Filename ::= proj_exp_condition_time_type.txt
- proj ::= 'Chart' | 'Math' | 'Time' | 'Lang'
- exp ::= 'Complete'
- condition ::= 'Randoop' | 'DigDog'
- time ::= '2' | '10' | '30' | '60'
- type ::= 'Line' | 'Branch' | 'Faults'
```

The file will have a pair of lines for each trial's data, starting with # covered/found, with the second number representing the total.

**Individual**

Measures the metrics across each of the 4 defects4j projects, considering each as its own code base. Averages the coverage metrics across 10 trials, using a global time limit.
```
- Filename ::= proj_exp_condition_time_type.txt
- proj ::= 'Chart' | 'Math' | 'Time' | 'Lang'
- exp ::= 'Individual'
- condition ::= 'Randoop' |  'Orienteering' | 'ConstantMining' | 'Bloodhound' | 'Combined'
- time ::= '50' | '100' | '150' | '200' | '250' | '300' | '350' | '400' | '450' | '500' | '550' | '600'
- type ::= 'Line' | 'Branch'
```

The file will have a pair of lines for each trial's data, starting with # covered/found, with the second number representing the total.
