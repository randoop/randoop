
This file contains information geared for Randoop developers.

For a tutorial and more information on using Randoop, also see the
Randoop wiki (http://code.google.com/p/randoop/w/).



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

Randoop requires Java 5.0 or greater.

To run system tests, you must be in an Unix-like environment (Linux,
Mac OS X, etc.).

1.2 Compiling Randoop
---------------------

Under Unix:

- From a command prompt, type "make build" under Randoop's home
  directory (the directory containing this file).
- If you want to rebuild from scratch, type "make clean" first.
- The generated classes are placed in bin/ directory.

Under Eclipse:

- If you are in Eclipse, Randoop compiles out of the box.
- The generated classes are also placed in bin/ directory.

1.3 Running unit and system tests
---------------------------------

From a command prompt, type "make tests" under Randoop's home directory.

To do everything (build _and_ run all tests), type "make all".

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
