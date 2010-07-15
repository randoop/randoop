
# Carlos's scratch makefile.

include ../common.mk

daikon:
	make class-daikon-daikon.inv.binary.twoScalar.LinearBinaryCore

daikon-df:
	make df-daikon-daikon.inv.binary.twoScalar.LinearBinaryCore

arraylist-prep:
	make prepare-java_collections

arraylist:
	make class-java_collections-java2.util2.HashMap

arraylist-df:
	make df-java_collections-java2.util2.HashMap

arraylist-genbd:
	make genbd-java_collections-java2.util2.HashMap

execute:
	java -ea -classpath $(RANDOOP_HOME)/systemtests/java_collections-covinst:$(CLASSPATH) \
	   randoop.experiments.Execute \
	   resources/df-bdgen-covclasses.txt \
	   carlos-temp.txt

stats:
	java -ea -classpath $(RANDOOP_HOME)/systemtests/java_collections-covinst:$(CLASSPATH) \
	   randoop.main.PrintStats \
	   resources/df-bdgen-covclasses.txt \
	   java_collections.stats.ser
