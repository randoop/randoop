
TABLE OF CONTENTS
=================

1   COMPILING AND TESTING
1.1 Prerequisites
1.2 Compiling Randoop
1.3 Running unit and system tests
1.4 Making a new distribution

2   DEVELOPER NOTES

================================================================================
1. COMPILING AND TESTING

1.1 Prerequites
---------------

Randoop requires Java 5.0 or greater. To run system tests, you must be
in an Unix-like environment (Linux, Mac OS X, etc.).

1.2 Compiling Randoop
---------------------

From a command prompt, type "make build" under Randoop's home
directory (the directory containing this file). That's it!

In Eclipse, Randoop compiles out of the box.

1.3 Running unit and system tests
---------------------------------

From a command prompt, type "make tests" under Randoop's home directory.

Other useful testing commands:

all            do everything (build and run test).
clean          build from scratch (removes build-related auto-generated files).

1.4 Making a new distribution
-----------------------------

1. Increase version number (modify variable RANDOOP_VERSION in src/randoop/Globals.java).
2. Compile and test: run "make clean" followed by "make all". Make sure all tests pass.
3. Upload the zip and jar files under dist/ directory to google code.


================================================================================
2. DEVELOPER NOTES

[Please contribute to this section any information you find useful
when developing Randoop].

Most command line options are specified in GenInputsAbstract.java

Output capture is implemented in ExecutableSequence.executeStatement.
This is accomplished by changing stdout and stderr to a memory
based printsteam and recording the results.

Comparing observations to see if they return consistent results is
implementedin ExecutableSequence.compare_observations()

GenTests is the main for Randoop as it is normally used.  There are
other mains for other purposes.

ForwardGenerator is the generator for Randoop's normal operation.
