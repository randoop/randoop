
# PLEASE IGNORE THIS FILE. This makefile is under construction and
# will not work outside Carlos's computer.

containers:
	java-1.5 -classpath $(CLASSPATH) randoop.main.Main issta-containers randoop.test.issta2006.BinomialHeap directed

jpf:
	java-1.5 -classpath $(CLASSPATH) randoop.main.Main run-jpf randoop.test.issta2006.BinomialHeap directed

temp:
	java -classpath $(CLASSPATH) randoop.main.Main genfailures --timelimit=1000 --inputlimit=1000 --classlist=ubstack.txt


tags:
	-rm TAGS
	find ./src -name "*.java" | xargs etags --language=java
	find ./tests -name "*.java" | xargs etags --append --language=java
