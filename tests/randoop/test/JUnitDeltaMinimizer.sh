#!/bin/sh

JAVA_HOME=/afs/csail/group/pag/software/pkg/jdk

CLASSPATH=.:/afs/csail.mit.edu/u/s/spal/research/invariants/java:/afs/csail/group/pag/software/pkg/jdk/jre/lib/rt.jar:/afs/csail/group/pag/software/pkg/jdk/lib/tools.jar:/afs/csail.mit.edu/u/s/spal/research/invariants/java/lib/bcel.jar:/afs/csail.mit.edu/u/s/spal/research/invariants/java/lib/java-getopt.jar:/afs/csail.mit.edu/u/s/spal/research/invariants/java/lib/junit.jar:/afs/csail.mit.edu/group/pag/projects/joe/clover-ant-1.3.11/lib/clover.jar:/afs/csail.mit.edu/u/s/spal/workspace/joe2/bin

jUnitFile=$1
javaMode=java6-beta

javac  $jUnitFile.java
if [ $? -ne 0 ]   #--check if everything is OK
then
   exit 1	# Error during compilation
fi

echo "Compiled successfully...."


#Remove all tests that pass and rewrite to same file
$javaMode -cp $CLASSPATH joe.test.JUnitPassingTestRemover $jUnitFile
echo "Removed passing tests..."


#Save away original failures for future comparisons in /deltatests as origFailures.txt
$javaMode -cp $CLASSPATH joe.test.SaveOriginalFailures $jUnitFile
echo "Saved away failures in origFailures.txt"


#Call Delta Debugger 
#cd deltatests/
#/afs/csail.mit.edu/u/s/spal/workspace/joe2/lib/delta-2005.09.13/delta -test=UnitDeltaChecker.sh $jUnitFile.java

echo "DONE"
