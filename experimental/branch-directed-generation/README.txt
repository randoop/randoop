
This file is probably outdated, but left here in case it is helpful if
someone takes up the Randoop/DataFlow project. It contained
instructions for building Randoop when we were implementing the
dataflow analysis and dataflow-based test generation.

1. Get and compile Daikon.

   See http://groups.csail.mit.edu/pag/daikon/
   or http://groups.csail.mit.edu/pag/daikon/mit/.

   If you're working in an environment different from the PAG group's
   environment, you may need to set up some environment variables in
   your .bashrc (or .cshrc, or .profile, etc.). For example, on Mac OS
   X 10.4.11, add the following lines to ~/.profile:

     export DAIKONDIR=/Users/carlospacheco/invariants
     export JDKDIR=none
     source $DAIKONDIR/scripts/daikon.bashrc
     export INV=/Users/carlospacheco/invariants
     export inv=/Users/carlospacheco/invariants
     export CLASSPATH=$CLASSPATH:$INV/java

2. Set RANDOOP_HOME to the point to Randoop's home directory (the
   directory containing this file). You may do this in a startup
   script, as you did with Daikon above. For example:

     export RANDOOP_HOME=$HOME/randoop

3. Build Randoop.  At the command line, type

     make build

   or it should build out of the box in Eclipse.

4. Run the tests.  At the command line, type

     make tests

