
# This makefile contains targets to build and test Randoop and
# DynComp.
#
# ONE-LINE INSTRUCTIONS: before doing a commit, run target "all".
#
# Other notable test targets:
#
#   + clean-tests: removes autom-generated files.
#
#   + results : shows diffs between output and goal files.
#
#   + update-goals : replaces goal files with current files.
#
# Other notable build targets:
#
#   + build: builds randoop.
#
#   + dc : builds dataflow (requires daikon; see README.txt).
#
# UPDATING RANDOOP'S WEB PAGE
# ===========================
#
# 0. Update version number file src/randoop/version.txt.
#
# 1. Create dir ~/public_html/randoop/VERSION
#    where VERSION is the new version number.
#
# 2. Run "make zip" and "make command-help"
#
# 3. Copy zip file to web page:
#    cp randoop.zip ~/public_html/randoop/VERSION/randoop.zip
#
# 4. Copy web page files to web page:
#    cp -r doc ~/public_html/randoop/1.2/
#
# 5. Make sure everything works:
#    download it, run it
#    open as eclipse project (this tests the .project file)
#
# 6. Finally, change ~/public_html/randoop/index.php to
#    point to the new version dir ~/public_html/randoop/VERSION

# Sets common variables.
include common.mk

default:
	@echo "================"
	@echo "Randoop Makefile"
	@echo "================"
	@echo "Main targets:"
	@echo ""
	@echo "all            do everything (build and run test)."
	@echo "build          compile Randoop."
	@echo "clean          remove build-related auto-generated files."
	@echo "clean-tests    remove test-related auto-generated files."
	@echo "results        display results of tests."
	@echo "update-goals   update test goal files."
	@echo "zip            Create a distribution zip file (randoop.zip)"

############################################################
# Targets for compiling and doing basic tests on Randoop.

# All the source files.
RANDOOP_FILES = $(shell find src/ tests/ -name '*.java')

temp:
	java -Xmx1700m -classpath $(CLASSPATH) randoop.main.RunISSTA06Containers randoop.test.issta2006.BinomialHeap directed

all: clean build tests results

# Remove Randoop classes.
clean:
	-rm -r bin

# Build Randoop.
build: bin randoop_agent.jar

bin: $(RANDOOP_FILES)
	mkdir -p bin
	@echo javac -nowarn -g -d bin ...
	@javac -nowarn -g -d bin $(RANDOOP_FILES)
	mkdir -p bin/randoop/test/resources
	cp tests/randoop/test/resources/*.txt bin/randoop/test/resources
	cp src/randoop/version.txt bin/randoop/
	touch bin

# Run all tests.
tests: clean-tests $(DYNCOMP) bin prepare randoop-tests covtest arraylist df3 bdgen2  df1  df2 bdgen  results

# Runs pure Randoop-related tests.
randoop-tests: unit randoop1 randoop2 randoop-contracts

# build pre-agent instrumentation jar
AGENT_JAVA_FILES = $(wildcard src/randoop/instrument/*.java)
randoop_agent.jar : $(AGENT_JAVA_FILES) src/randoop/instrument/manifest.txt
	javac -g -d bin $(AGENT_JAVA_FILES)
	jar cfm randoop_agent.jar src/randoop/instrument/manifest.txt \
	  bin/randoop/instrument/Premain.class

jdoc javadoc:
	mkdir -p jdoc
	find src/randoop -name "*.java" \
		| xargs javadoc -d jdoc -quiet -noqualifier all

tags: $(RANDOOP_FILES)
	find src/ tests/ -name "*.java" | xargs etags

############################################################
# Targets to test Randoop.

# The tests run correctly under Java 1.6. Using an earlier version of
# Java may result in test failures.
unit: bin
	java -Xmx1700m -ea -classpath $(CLASSPATH) \
	  junit.textui.TestRunner \
	   randoop.test.AllRandoopTests

perf: perf1 perf2

# -Xrunhprof:cpu=samples,depth=30
perf1: bin
	java -Xmx1700m -ea -classpath $(CLASSPATH) \
	  junit.textui.TestRunner \
	  randoop.test.RandoopPerformanceTest

perf2: bin
	java -Xmx1700m -ea -classpath $(CLASSPATH) \
	  junit.textui.TestRunner \
	  randoop.test.NaivePerformanceTest

# Runs Randoop on Collections and TreeSet.
randoop1: bin
	-rm -r randoop-scratch
	java -ea -classpath $(RANDOOP_HOME)/systemtests/src/java_collections:$(CLASSPATH) \
	  randoop.main.Main gentests \
	   --use-object-cache \
   --output-tests=all \
	   --check-object-contracts=false \
	   --inputlimit=500 \
	   --testclass=java2.util2.TreeSet \
	   --testclass=java2.util2.Collections \
	   --junit-classname=TestClass \
	   --junit-package-name=foo.bar \
	   --junit-output-dir=randoop-scratch \
	   --log=randoop-log.txt
	cd randoop-scratch && \
	  javac  -cp .:$(RANDOOP_HOME)/systemtests/src/java_collections:$(CLASSPATH) \
	  foo/bar/TestClass*.java
	cd randoop-scratch && \
	  java  -cp .:$(RANDOOP_HOME)/systemtests/src/java_collections:$(CLASSPATH) \
	  foo.bar.TestClass
	cp randoop-scratch/foo/bar/TestClass0.java systemtests/resources/TestClass0.java

# Runs Randoop on Collections and TreeSet.
randoop2: bin
	-rm -r randoop-scratch
	java -ea -classpath $(RANDOOP_HOME)/systemtests/src/java_collections:$(CLASSPATH) \
	  randoop.main.Main gentests \
	   --dontexecute \
	   --output-tests=all \
	   --check_reps=true \
	   --component-based=false \
	   --check-object-contracts=false \
	   --inputlimit=10000 \
	   --testclass=java2.util2.TreeSet \
	   --testclass=java2.util2.ArrayList \
	   --testclass=java2.util2.LinkedList \
	   --testclass=java2.util2.Collections \
	   --junit-classname=Naive \
	   --output-nonexec=true \
	   --junit-package-name=foo.bar \
	   --junit-output-dir=randoop-scratch \
	   --log=randoop-log.txt
	cp randoop-scratch/foo/bar/Naive0.java systemtests/resources/Naive0.java

# Runs Randoop on Collections and TreeSet.
randoop3: bin
	-rm -r randoop-scratch
	java -ea -classpath $(RANDOOP_HOME)/systemtests/src/java_collections:$(CLASSPATH) \
	  randoop.main.Main gentests \
	   --inputlimit=1000 \
	   --testclass=java2.util2.TreeSet \
	   --testclass=java2.util2.Collections \
	   --junit-classname=Naive2_ \
	   --junit-package-name=foo.bar \
	   --junit-output-dir=randoop-scratch
	cp randoop-scratch/foo/bar/Naive2_0.java systemtests/resources/Naive2_0.java

randoop-contracts: bin
	cd systemtests/resources/randoop && javac examples/Buggy.java
	-rm -r randoop-contracts-scratch
	java -ea -classpath $(RANDOOP_HOME)/systemtests/resources/randoop:$(CLASSPATH) \
	  randoop.main.Main gentests \
	   --output-tests=fail \
	   --timelimit=5 \
	   --classlist=systemtests/resources/randoop/examples/buggyclasses.txt \
	   --junit-classname=BuggyTest \
	   --junit-output-dir=randoop-contracts-scratch \
	   --log=randoop-contracts-log.txt
	cd randoop-contracts-scratch && \
	  javac -cp .:$(RANDOOP_HOME)/systemtests/resources/randoop:$(CLASSPATH) BuggyTest.java
# We expect this to fail, so add a "-" so the target doesn't fail.
	cd randoop-contracts-scratch && \
	  java  -cp .:$(RANDOOP_HOME)/systemtests/resources/randoop:$(CLASSPATH) \
	  randoop.main.RandoopContractsTest

randoop-jdk: randoop-jdk-gen randoop-jdk-comp randoop-jdk-run

randoop-jdk-gen: bin
	-rm -r randoop-jdk-scratch
	java -ea -Xmx1700m -classpath \
	   $(RANDOOP_HOME)/systemtests/java_collections-covinst:$(CLASSPATH) \
	   randoop.main.Main gentests \
	   --output-tests=all \
	   --inputlimit=10000 \
	   --helpers=true \
	   --coverage-instrumented-classes=systemtests/java_collections.covinstclasslist.txt \
	   --classlist=systemtests/java_collections.classlist.txt \
	   --omitmethods="nCopies|randomUUID|IllegalFormatCodePointException" \
	   --junit-output-dir=randoop-jdk-scratch \
	   --junit-classname=RandoopOnJDK \
	   --forbid-null=true \
	   --stats-coverage=true \
	   --usethreads=false \
	   --component-based=true \
	   --offline=false \
	   --use-object-cache \
	   --alias-ratio=0.5 \
	   --maxsize=50 \
	   --randomseed=1

randoop-jdk-comp:
	cd randoop-jdk-scratch && \
	  javac -cp .:$(RANDOOP_HOME)/systemtests/java_collections-covinst:$(CLASSPATH) \
	  *.java

randoop-jdk-run:
	cd randoop-jdk-scratch && \
	  java -cp .:$(RANDOOP_HOME)/systemtests/java_collections-covinst:$(CLASSPATH) \
	  RandoopOnJDK

############################################################
# Targets for compiling DataFlow.

# Build dyncomp
DYNCOMP			= $(INV)/java/dcomp_premain.jar
DYNCOMP_JAVA	= $(INV)/java/daikon/dcomp/*.java
dc : $(DYNCOMP)
$(DYNCOMP) : $(DYNCOMP_JAVA)
	make -C $$inv/java dcomp_premain.jar

############################################################
# Targets for testing Randoop/Dyncomp's dataflow analysis.

# Runs Randoop and Dataflow analysis on arraylist.
# Order matters: df1 should follow randoop, and bdgen should follow df2.
arraylist: randoop-df df

# Compiles and coverage-instruments the java_collections subject program.
prepare:
	cd systemtests && make prepare-jc

prepare-ds:
	cd systemtests && make prepare-simple_ds

df-ds: $(DYNCOMP) bin
	java -ea -classpath $(RANDOOP_HOME)/systemtests/src/simple_ds:$(CLASSPATH) \
	   randoop.main.DataFlow \
	   --scratchdir=df-scratch \
	   --overwrite \
	   --outputfile=temp.txt \
	   systemtests/resources/simple_ds.dfin.txt


# Runs Randoop on arraylist.
# Compares the results with the goal results.
#
# Its output is the input to target df.
randoop-df: bin
	-rm frontier*
	java -ea -classpath $(RANDOOP_HOME)/systemtests/jc-covinst:$(CLASSPATH) \
	  randoop.main.Main gentests \
	   --usethreads=false \
	   --use-object-cache \
	   --check-object-contracts=false \
	   --inputlimit=1000 \
	   --testclass=java2.util2.ArrayList \
	   --dont-output-tests \
	   --forbid-null=false \
	   --coverage-instrumented-classes=systemtests/resources/arraylist.covclasses.txt \
	   --output-covmap=covmap.gz \
	   --output-cov-witnesses \
	   --output-branches=systemtests/resources/arraylist.branches_covered.txt
	java -ea -classpath $(RANDOOP_HOME)/systemtests/jc-covinst:$(CLASSPATH) \
	   randoop.main.ComputeFrontierBranches \
	   --input-map=covmap.gz \
	   --experiment=test \
	   --seqs-per-method=1 \
	   --print-coderep-comments=true
	gunzip frontier*.gz
	cat frontier1 frontier2 frontier3 frontier4 frontier5 frontier6 \
	  > systemtests/resources/arraylist.dfin.txt

# Runs dataflow on the results of Randoop on arraylist.
#
# Its input is the output of target randoop.
df: $(DYNCOMP) bin
	java -ea -classpath $(RANDOOP_HOME)/systemtests/src/java_collections:$(CLASSPATH) \
	   randoop.main.DataFlow \
	   --scratchdir=df-scratch \
	   --overwrite \
	   --outputfile=systemtests/resources/arraylist.dfout.txt \
	   systemtests/resources/arraylist.dfin.txt.goal

# NOT A TEST! I use this target to communicate problems to Jeff.
dferr%: $(DYNCOMP) bin
	-rm -r df-scratch
	java -ea -classpath $(RANDOOP_HOME)/systemtests/src/java_collections:$(CLASSPATH) \
	   randoop.main.DataFlow --debug_df \
	   --scratchdir=df-scratch \
	   --overwrite \
	   systemtests/resources/$@.txt

execerr:
	java -ea -classpath $(RANDOOP_HOME)/systemtests/src/java_collections:$(CLASSPATH) \
	randoop.main.Main \
	exec systemtests/resources/dferr2-seq-only.txt

# Runs dataflow on various test inputs (see systemtests/resources/df1.txt).
#
# Its input was manually generated.
df1: $(DYNCOMP) bin
	-rm systemtests/resources/df1.txt.output
	java -ea -classpath $(RANDOOP_HOME)/systemtests/src/java_collections:$(CLASSPATH) \
	   randoop.main.DataFlow \
	   --scratchdir=df-scratch \
	   --overwrite \
	   systemtests/resources/df1.txt

# Runs dataflow on a set of inputs.
#
# Its input was manually generated to be sequences which bdgen can
# successfully modify to cover a frontier branch.
#
# Its output is used as input by target bdgen.
df2:
	java -ea -classpath $(RANDOOP_HOME)/systemtests/src/java_collections:$(CLASSPATH) \
	   randoop.main.DataFlow \
	   --scratchdir=df-scratch \
	   --overwrite \
	   --outputfile=systemtests/resources/df2-output.txt \
	   systemtests/resources/df2-input.txt

# Runs bdgen on a collection of (sequence, frontier branch,
# interesting vars) triples, and checks that bdgen can successfully
# creates new sequences to cover the frontier branches.
#
# Its input is the output of target df2.
bdgen: bin
	java -ea -classpath $(RANDOOP_HOME)/systemtests/jc-covinst:$(CLASSPATH) \
	   randoop.main.GenBranchDir \
	   --many-branches \
	   --input-df-results=systemtests/resources/df2-output.txt.goal \
	   --input-covinst-classes=systemtests/resources/df-bdgen-covclasses.txt \
	   --input-covmap=covmap.gz \
	   --output-new-sequences=systemtests/resources/bdgen-output.txt \
	   --output-failures=systemtests/resources/bdgen-failures.txt \
	   --output-new-branches=systemtests/resources/bdgen-branches.txt \
	   --output-new-branches-sorted \
	   --logfile=bdgen-log.txt

# Runs bdgen on a collection of manually-generated cases, for which it
# should successfully generate sequences that cover frontier
# branches.
bdgen2: bin
	java -ea -classpath $(RANDOOP_HOME)/systemtests/jc-covinst:$(CLASSPATH) \
	   randoop.main.GenBranchDir \
	   --many-branches \
	   --input-df-results=systemtests/resources/bdgen2-input.txt \
	   --input-covinst-classes=systemtests/resources/df-bdgen-covclasses.txt \
	   --input-components-txt=systemtests/resources/bdgen_components.txt \
	   --output-new-sequences=systemtests/resources/bdgen2-output.txt \
	   --output-failures=systemtests/resources/bdgen2-failures.txt \
	   --output-new-branches=systemtests/resources/bdgen2-branches.txt \
	   --output-new-branches-sorted \
	   --logfile=bdgen2-log.txt
# There is nondeterminism in HashMap. Don't consider branchs in regression tests.
	grep -v "util2\.HashMap" systemtests/resources/bdgen2-branches.txt > tmp.txt
	mv tmp.txt systemtests/resources/bdgen2-branches.txt

# Test the coverage instrumenter.
# Runs the instrumenter on a test file, and diffs the result
# with a goal file.
covtest: bin
	-rm -r covtest-scratch
	-rm covtest-scratch/cov/TestClass.java
	java -ea -classpath $(RANDOOP_HOME)/systemtests/jc-covinst:$(CLASSPATH) \
	  cov.Instrument \
	  --destination=covtest-scratch \
	  --files=systemtests/resources/cov/classlist.txt
	cd covtest-scratch && javac cov/*.java
	cp covtest-scratch/cov/TestClass.java \
	   systemtests/resources/cov/TestClass-instrumented

df3: $(DYNCOMP) bin
	java -ea -classpath $(RANDOOP_HOME)/systemtests/src/java_collections:$(CLASSPATH) \
	   randoop.main.DataFlow \
	   --scratchdir=df-scratch \
	   --overwrite \
	   --outputfile=systemtests/resources/df3-output.txt \
	   systemtests/resources/df3.txt

############################################################
# Targets for creating and printing the results of test diffs.


goal_files = $(shell find systemtests/resources -name "*.goal")

# Contains the goal file names, without the .goal suffix.
goal_files_bases = $(basename $(goal_files))

# Contains the diff file names. Diff files written to base directory.
diff_files = $(foreach base, $(goal_files_bases), $(base).diff)

update_goal_targets = $(foreach base, $(goal_files_bases), $(base)-update-goal)

# Calls Make recursively to update the goal files.
update-goals:
	make $(update_goal_targets)

# Given target F-update-goal, copies F -> F.goal.
%-update-goal:
	cp $* $*.goal

# Removes any previously-generated diff or auto-generated files.
clean-tests:
	-rm $(diff_files)
	-rm $(goal_files_bases)

# Calls Make recursively to make the necessary .diff files.
diffs:
	make $(diff_files)

# Creates a .diff file by comparing a file F with F.goal.  If F does
# not exist, creates a F.diff file with a message saying that the F
# file does not exist.
%.diff: .FORCE
	if [ -e $* ]; then \
	  (diff -u $*.goal $* > $*.diff) || true; \
	else \
	  echo "Target file does not exist: $*" > $*.diff; \
	fi

# output display; can be called from children (they will set BASE)
BASE?=.
# removes fields before the size (ie, permissions, owner, group).
PERL_CLEANUP_LS_OUTPUT = perl -ne 'BEGIN { $$failure=0; } /^\S+\s+\S+\s+\S+\s+\S+\s+(\d+)\s+(.*)\s+(\S+)$$/; print "$$1\t$$2\t$$3\n"; if ($$1 > 0) { $$failure++; } END { if ($$failure == 1) { print "1 test failed.\n"; exit 1; } elsif ($$failure) { print "$$failure tests failed.\n"; exit 1; } else { print "All tests succeeded.\n"; } }'
# args to 'find' program, to find files containg results
RESULTS_PATTERN :=    -name '*.diff'

results: diffs results_header summary

results_header:
	@echo ""
	@echo "=== RESULTS ==="
	@echo ""

summary:
	@ls -l $(diff_files) \
	  | perl -pe 's|\Q${BASE}|.|;' \
	  | ${PERL_CLEANUP_LS_OUTPUT}


############################################################
# Targets for updating Randoop's web page.

# Creates the zip file for other people to download.
zip:
	rm -rf jrandoop
	mkdir jrandoop
# Copy sources. Remove sources we don't want in the distribution.
	cp -R src jrandoop/src
	find ./jrandoop/src -name ".svn" | xargs rm -rf
# 	rm -r jrandoop/src/randoop/experiments
# 	rm jrandoop/src/randoop/Naive*.java
# 	rm jrandoop/src/randoop/DataFlow*.java
# 	rm jrandoop/src/randoop/DFResultsOneSeq.java
# 	rm jrandoop/src/randoop/main/DataFlow.java
# 	rm jrandoop/src/randoop/main/GenBranchDir.java
# 	rm jrandoop/src/randoop/main/GenInputsNaive.java
# 	rm jrandoop/src/randoop/main/Run*.java
# 	rm jrandoop/src/randoop/main/Universal*.java
#	rm jrandoop/src/cov/Instrument.java
#	rm jrandoop/src/cov/TestClass.java
#	rm jrandoop/src/cov/FilesUtil.java
#	rm jrandoop/src/cov/CountCoverage.java
#	rm jrandoop/src/cov/ASTUtil.java
# Copy test sources.
	cp -R tests jrandoop/tests
	find ./jrandoop/tests -name ".svn" | xargs rm -rf
	rm jrandoop/tests/randoop/test/Naive*.java
# Copy required libraries.
	mkdir jrandoop/lib
	cp lib/bcel.jar jrandoop/lib
	cp lib/jakarta-oro-2.0.8.jar jrandoop/lib
	cp lib/jakarta-oro-license.txt jrandoop/lib
	cp lib/junit-4.3.1.jar jrandoop/lib
	cp lib/jfreechart-1.0.10.jar jrandoop/lib
	cp lib/jcommon-1.0.13.jar jrandoop/lib
	cp lib/*eclipse* jrandoop/lib
# Copy license.
	cp license.txt jrandoop/
# Copy README file
	cp README.dist jrandoop/README
# Copy eclipse project files.
	cp .project jrandoop/.project
	cp .classpath-dist jrandoop/.classpath
# Make sure everything works.
	cd jrandoop && \
	  find src/ tests/ -name "*.java" \
	  | xargs javac -cp 'lib/*'

# Make randoop.jar.
	mkdir jrandoop/tmp
	cp -r jrandoop/src/* jrandoop/tmp
	cp -r jrandoop/tests/* jrandoop/tmp
	cd jrandoop/tmp && jar xf ../lib/jakarta-oro-2.0.8.jar
	cd jrandoop/tmp && jar xf ../lib/junit-4.3.1.jar
	cd jrandoop/tmp && jar cf randoop.jar *
	mv jrandoop/tmp/randoop.jar jrandoop/
	rm -r jrandoop/tmp

# Copy in the instrumentation agent jar file
	cp randoop_agent.jar jrandoop

# Zip everything up.
	rm -f randoop.zip
	zip -r randoop jrandoop
	rm -r jrandoop

# Creates autogenerated php file with Randoop commands documentation.
command-help:
	java -classpath $(CLASSPATH) randoop.main.Main html-help
	mv randoop_commands.php doc/
	mv randoop_commands_list.php doc/

.FORCE:

showvars:
	@echo CLASSPATH = $(CLASSPATH)
	jwhich SingleMemberAnnotation
