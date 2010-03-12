
This file contains instructions for building Randoop at CSAIL.

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
   script like Daikon above. For example:

     export RANDOOP_HOME=$HOME/randoop

3. Build Randoop.

   Run "make build", or it should build of the box in Eclipse.

4. Run the tests.

   Run "make tests".

