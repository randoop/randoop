
This file contains instructions for building Randoop at CSAIL.

1. Get and compile Daikon via CVS.

   See http://groups.csail.mit.edu/pag/daikon/mit/.

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
   script like Daikon above. For example:

     export RANDOOP_HOME=/Users/carlospacheco/jrandoop

3. Build Randoop.

   It should build of the box in Eclipse.

   If you prefer to use Make, run the "build" target in
   $RANDOOP_HOME/Makefile.

4. Run the tests.

   run the "tests" target in the Makefile.

