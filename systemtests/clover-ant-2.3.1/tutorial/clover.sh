#!/bin/sh

###
# A sample shell script to demonstrate how to run clover from the command line.
# This script does the following:
#  1) runs CloverInstr over all source files in the src directory
#  2) compiles the instrumented sources
#  3) runs the unit tests
#  4) generates a Clover HTML Report, XML Report, PDF Report and a Console Report
##

CLOVER=../lib/clover.jar
INITSTR=build/cli/db/clover.db
JUNIT=lib/junit-3.8.2.jar
CLASSES=build/cli/classes
INSTRDIR=build/cli/instr/
REPORT=build/cli/report

#1
java -cp $CLOVER com.cenqua.clover.CloverInstr -i $INITSTR -s src -d $INSTRDIR

#2
mkdir $CLASSES
javac -classpath $CLOVER:$JUNIT  -d $CLASSES $INSTRDIR/com/cenqua/samples/money/*.java

#3
java -cp $CLOVER:$JUNIT:$CLASSES com.cenqua.samples.money.MoneyTest

#4
java -cp $CLOVER com.cenqua.clover.reporters.html.HtmlReporter -i $INITSTR -o $REPORT
java -cp $CLOVER com.cenqua.clover.reporters.xml.XMLReporter   -i $INITSTR -o $REPORT/clover.xml
java -cp $CLOVER com.cenqua.clover.reporters.pdf.PDFReporter   -i $INITSTR -o $REPORT/clover.pdf
java -cp $CLOVER com.cenqua.clover.reporters.console.ConsoleReporter -i $INITSTR -t "Money Bags Console Report"