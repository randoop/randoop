#!/bin/sh

JAVA_HOME=/afs/csail/group/pag/software/pkg/jdk

CLASSPATH=.:/afs/csail.mit.edu/u/s/spal/research/invariants/java:/afs/csail/group/pag/software/pkg/jdk/jre/lib/rt.jar:/afs/csail/group/pag/software/pkg/jdk/lib/tools.jar:/afs/csail.mit.edu/u/s/spal/research/invariants/java/lib/bcel.jar:/afs/csail.mit.edu/u/s/spal/research/invariants/java/lib/java-getopt.jar:/afs/csail.mit.edu/u/s/spal/research/invariants/java/lib/junit.jar:/afs/csail.mit.edu/group/pag/projects/joe/clover-ant-1.3.11/lib/clover.jar:/afs/csail.mit.edu/u/s/spal/workspace/joe2/bin:/afs/csail.mit.edu/u/s/spal/workspace/joe2/tests

jUnitName=JoeRegressions1
jUnitFile=JoeRegressions1.java
javaMode=java6-beta


echo $1
mv $1 $jUnitFile
javac $jUnitFile

if [ $? -eq 0 ]   #--check if everything is OK
then
    if [ -s $jUnitFile ] 
    then	
	$javaMode -cp $CLASSPATH joe.test.GenerateNewFailures $jUnitName
	diff /scratch/deltadebugger/delta-2005.09.13/testJunit_delta/origFailures.txt newFailures.txt
	if [ $? -eq 0 ]   #--check if everything is OK
	then
	    exit 0
	fi
    fi
fi

exit 1

