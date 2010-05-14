
This file contains information geared for Randoop developers.

For a tutorial and more information on using Randoop, also see the
Randoop wiki (http://code.google.com/p/randoop/w/).

=================
TABLE OF CONTENTS
=================

1   COMPILING AND TESTING
1.1 Prerequisites
1.2 Compiling Randoop
1.3 Running unit and system tests
1.4 Making a new distribution

2   DEVELOPER NOTES

======================================================================
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

1. Increase version number
   (modify variable RANDOOP_VERSION in src/randoop/Globals.java).

2. Compile and test: run "make clean" followed by "make all". Make
   sure all tests pass.

3. Upload the zip and jar files under dist/ directory to google code.


======================================================================
2. DEVELOPER NOTES

[Please contribute to this section any information you find useful
when developing Randoop].

[We should eventually move this as a section in the manual]

----------------------------------------------------------------------
Most command line options are specified in GenInputsAbstract.java

----------------------------------------------------------------------
Output capture is implemented in ExecutableSequence.executeStatement.
This is accomplished by changing stdout and stderr to a memory
based printsteam and recording the results.

----------------------------------------------------------------------
Comparing observations to see if they return consistent results is
implementedin ExecutableSequence.compare_observations()

----------------------------------------------------------------------
GenTests is the main class for Randoop as it is normally used.
There are other mains for other purposes.

Method "handle" in src/randoop/main/GenTests.java is the main
entrypoint for Randoop. This is not strictly true, as Randoop's true
entrypoint is class randoop.main.Main. But GenTests is where all the
action starts with test generation. The "handle" method is long and
mostly deals with setting up things before the generation process, and
doing things like outputting tests after generation. 

----------------------------------------------------------------------
ForwardGenerator is the generator for Randoop's normal operation.

----------------------------------------------------------------------
How to run Randoop?  The Makefile has targets for building and running
unit/system tests. How to "just run" Randoop on some arbitrary classes
for daily purposes? All you need to do to run Randoop is make sure the
bin/ directory and the files under lib/ are part of the classpath.

* To avoid repeatedly setting up the classpath, some of us add the
  bin/ directory and the files under lib/ to our (global) CLASSPATH
  variable that we specify in a configuration file (e.g. .bashrc).

* Others create a temporary shell script or mini-Makefile for ad-hoc
  use, for instance, a Make file such as:

  RANDOOP_HOME ?= <LOCATION OF RANDOOP PROJECT>
  include $(RANDOOP_HOME)/common.mk
  run:
	java -ea randoop.main.Main help gentests

----------------------------------------------------------------------

Places to look for when modifying the JUnit code that is output:

Class randoop.JunitFileWriter figures out how many JUnit classes/files
to write, how many tests to put in each, what to name them, etc.

Methods
  randoop.ExecutableSequence.toCodeString(),
  randoop.ExecutableSequence.toCodeString(),
  and the various implementationgs of randoop.StatementKind.appendCode(...)

Are responsible for writing the code for a single unit test.

----------------------------------------------------------------------

Modifying the manual.

To modify the Randoop manual, edit directly the HTML files:

 index.html has the main "user" manual with instructions for using the tool
 dev.html has the developer manual with instructions for hacking Randoop

To create the table of contents, we use a utility called
"html-update-toc" (should be under directory <tt>utils/plume-lib</tt>,
assuming you have run <tt>make manual</tt> at least once). This
utility uses [...]
