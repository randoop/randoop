
# This makefile contains targets to build and test Randoop
#
# ONE-LINE INSTRUCTIONS: before doing a commit, run target "all".
#
# Other notable test targets:

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
	@echo "tests          run tests."
	@echo "update-goals   update test goal files."
	@echo "distribution-files  create distribution zip and jar files."

# Put user-specific changes in your own Makefile.user.
# Make will silently continue if that file does not exist.
-include Makefile.user

RANDOOP_HOME ?= $(shell pwd)

# Sets common variables.
include common.mk

JAVAC ?= javac
JAVAC_JAR ?= ${HOME}/research/types/jsr308-langtools/dist/lib/javac.jar
# User may set JAVAC_EXTRA_ARGS
JAVAC_COMMAND ?= ${JAVAC} ${JAVAC_EXTRA_ARGS}

############################################################
# Targets for compiling and doing basic tests on Randoop.

# All the source files.
RANDOOP_FILES = $(shell find src/ tests/ -name '*.java')
RANDOOP_SRC_FILES = $(shell find src/ -name '*.java')
RANDOOP_TESTS_FILES = $(shell find tests/ -name '*.java')

all: clean build tests results 

# Remove Randoop classes.
clean:
	rm -rf bin

# Build Randoop.
build: bin randoop_agent.jar

bin: $(RANDOOP_FILES)
	mkdir -p bin
	@echo ${JAVAC_COMMAND} -Xlint -g -d bin ...
	@${JAVAC_COMMAND} -Xlint -g -d bin $(RANDOOP_SRC_FILES)
	@${JAVAC_COMMAND} -nowarn -g -d bin $(RANDOOP_TESTS_FILES)
	mkdir -p bin/randoop/test/resources
	cp tests/randoop/test/resources/*.txt bin/randoop/test/resources
	touch bin

# Run all tests.
tests: clean-tests $(DYNCOMP) bin prepare randoop-tests covtest arraylist df3 bdgen2  df1  df2 bdgen distribution-files results

# Runs pure Randoop-related tests.
randoop-tests: unit randoop1 randoop2 randoop-contracts

# build pre-agent instrumentation jar
AGENT_JAVA_FILES = $(wildcard src/randoop/instrument/*.java)
randoop_agent.jar : $(AGENT_JAVA_FILES) src/randoop/instrument/manifest.txt
	${JAVAC_COMMAND} -Xlint -g -d bin -cp src:$(CLASSPATH) $(AGENT_JAVA_FILES)
	jar cfm randoop_agent.jar src/randoop/instrument/manifest.txt \
	  bin/randoop/instrument/Premain.class

jdoc javadoc:
	mkdir -p jdoc
	find src/randoop -name "*.java" \
		| xargs javadoc -d jdoc -quiet -noqualifier all

.PHONY: tags
tags: TAGS
TAGS: $(RANDOOP_FILES)
	find src/ tests/ -name "*.java" | xargs etags


############################################################
# Targets to test Randoop.

# The tests run correctly under Java 1.6. Using an earlier version of
# Java may result in test failures.
unit: bin
	java ${XMXHEAP} -ea \
	  junit.textui.TestRunner \
	   randoop.test.AllRandoopTests


# Runs Randoop on Collections and TreeSet.
randoop1: bin
	rm -rf systemtests/randoop-scratch
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
	   --junit-output-dir=systemtests/randoop-scratch \
	   --log=systemtests/randoop-log.txt \
	   --nochecks=false \
	   --randooptestrun=true
	cd systemtests/randoop-scratch && \
	  ${JAVAC_COMMAND} -nowarn -cp .:$(RANDOOP_HOME)/systemtests/src/java_collections:$(CLASSPATH) \
	  foo/bar/TestClass*.java
	cd systemtests/randoop-scratch && \
	  java  -cp .:$(RANDOOP_HOME)/systemtests/src/java_collections:$(CLASSPATH) \
	  foo.bar.TestClass
	cp systemtests/randoop-scratch/foo/bar/TestClass0.java systemtests/resources/TestClass0.java

# Runs Randoop on Collections and TreeSet.
randoop2: bin
	rm -rf systemtests/randoop-scratch
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
	   --junit-output-dir=systemtests/randoop-scratch \
	   --log=systemtests/randoop-log.txt
	cp systemtests/randoop-scratch/foo/bar/Naive0.java systemtests/resources/Naive0.java

# Runs Randoop on Collections and TreeSet.
randoop3: bin
	rm -rf systemtests/randoop-scratch
	java -ea -classpath $(RANDOOP_HOME)/systemtests/src/java_collections:$(CLASSPATH) \
	  randoop.main.Main gentests \
	   --inputlimit=1000 \
	   --testclass=java2.util2.TreeSet \
	   --testclass=java2.util2.Collections \
	   --junit-classname=Naive2_ \
	   --junit-package-name=foo.bar \
	   --junit-output-dir=systemtests/randoop-scratch
	cp systemtests/randoop-scratch/foo/bar/Naive2_0.java systemtests/resources/Naive2_0.java

randoop-contracts: bin
	cd systemtests/resources/randoop && ${JAVAC_COMMAND} -nowarn examples/Buggy.java
	rm -rf systemtests/randoop-contracts-test-scratch
	java -ea -classpath $(RANDOOP_HOME)/systemtests/resources/randoop:$(CLASSPATH) \
	  randoop.main.Main gentests \
	   --output-tests=fail \
	   --timelimit=5 \
	   --classlist=systemtests/resources/randoop/examples/buggyclasses.txt \
	   --junit-classname=BuggyTest \
	   --junit-output-dir=systemtests/randoop-contracts-test-scratch \
	   --log=systemtests/randoop-contracts-log.txt
	cd systemtests/randoop-contracts-test-scratch && \
	  ${JAVAC_COMMAND} -nowarn -cp .:$(RANDOOP_HOME)/systemtests/resources/randoop:$(CLASSPATH) BuggyTest.java
# We expect this to fail, so add a "-" so the target doesn't fail.
	cd systemtests/randoop-contracts-test-scratch && \
	  java  -cp .:$(RANDOOP_HOME)/systemtests/resources/randoop:$(CLASSPATH) \
	  randoop.main.RandoopContractsTest

# Performance tests. Removed from Randoop tests because results highly dependent on machine that
# tests are run, resulting in many false positives.
perf: perf1 perf2

# -Xrunhprof:cpu=samples,depth=30
perf1: bin
	java ${XMXHEAP} -ea \
	  junit.textui.TestRunner \
	  randoop.test.RandoopPerformanceTest

perf2: bin
	java ${XMXHEAP} -ea \
	  junit.textui.TestRunner \
	  randoop.test.NaivePerformanceTest

# Targets to measure achieved coverage on container data structures.
# Did not make into Randoop tests because results are highly dependent
# on the machine that Randoop is run on.
containers:
	java ${XMXHEAP} -classpath $(CLASSPATH) randoop.main.Main issta-containers randoop.test.issta2006.BinTree directed
	java ${XMXHEAP} -classpath $(CLASSPATH) randoop.main.Main issta-containers randoop.test.issta2006.BinomialHeap directed
	java ${XMXHEAP} -classpath $(CLASSPATH) randoop.main.Main issta-containers randoop.test.issta2006.FibHeap directed
	java ${XMXHEAP} -classpath $(CLASSPATH) randoop.main.Main issta-containers randoop.test.issta2006.TreeMap directed

############################################################
# Targets for testing Randoop/Dyncomp's dataflow analysis.

# Dataflow library
DYNCOMP			= $(RANDOOP_HOME)/lib/dcomp_premain.jar

# Test the coverage instrumenter.
# Runs the instrumenter on a test file, and diffs the result
# with a goal file.
covtest: bin
	rm -rf systemtests/covtest-scratch
	java -ea -classpath $(RANDOOP_HOME)/systemtests/jc-covinst:$(CLASSPATH) \
	  cov.Instrument \
	  --destination=systemtests/covtest-scratch \
	  --files=systemtests/resources/cov/classlist.txt
	cd systemtests/covtest-scratch && ${JAVAC_COMMAND} -Xlint cov/*.java
	cp systemtests/covtest-scratch/cov/TestClass.java \
	   systemtests/resources/cov/TestClass-instrumented

# Runs Randoop and Dataflow analysis on arraylist.
# Order matters: df1 should follow randoop, and bdgen should follow df2.
arraylist: randoop-df df

# Compiles and coverage-instruments the java_collections subject program.
prepare:
	cd systemtests && make prepare-jc

# Runs Randoop on arraylist.
# Compares the results with the goal results.
#
# Its output is the input to target df.
randoop-df: bin
	rm -f frontier*
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
	cat frontier[123456] \
	  > systemtests/resources/arraylist.dfin.txt
# Cleanup scratch files
	rm frontier[123456] test.dftargets.txt

# Runs dataflow on the results of Randoop on arraylist.
#
# Its input is the output of target randoop.
df: $(DYNCOMP) bin
	java -ea -classpath $(RANDOOP_HOME)/systemtests/src/java_collections:${JAVAC_JAR}:$(CLASSPATH) \
	   randoop.main.DataFlow \
	   --scratchdir=systemtests/df-scratch \
	   --overwrite \
	   --outputfile=systemtests/resources/arraylist.dfout.txt \
	   systemtests/resources/arraylist.dfin.txt.goal

# NOT A TEST! I use this target to communicate problems to Jeff.
dferr%: $(DYNCOMP) bin
	rm -rf systemtests/df-scratch
	java -ea -classpath $(RANDOOP_HOME)/systemtests/src/java_collections:${JAVAC_JAR}:$(CLASSPATH) \
	   randoop.main.DataFlow --debug_df \
	   --scratchdir=systemtests/df-scratch \
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
	rm -f systemtests/resources/df1.txt.output
	java -ea -classpath $(RANDOOP_HOME)/systemtests/src/java_collections:${JAVAC_JAR}:$(CLASSPATH) \
	   randoop.main.DataFlow \
	   --scratchdir=systemtests/df-scratch \
	   --overwrite \
	   systemtests/resources/df1.txt

# Runs dataflow on a set of inputs.
#
# Its input was manually generated to be sequences which bdgen can
# successfully modify to cover a frontier branch.
#
# Its output is used as input by target bdgen.
df2:
	java -ea -classpath $(RANDOOP_HOME)/systemtests/src/java_collections:${JAVAC_JAR}:$(CLASSPATH) \
	   randoop.main.DataFlow \
	   --scratchdir=systemtests/df-scratch \
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
	   --logfile=systemtests/bdgen-log.txt

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
	   --logfile=systemtests/bdgen2-log.txt
# There is nondeterminism in HashMap. Don't consider branchs in regression tests.
	grep -v "util2\.HashMap" systemtests/resources/bdgen2-branches.txt > tmp.txt
	mv tmp.txt systemtests/resources/bdgen2-branches.txt

df3: $(DYNCOMP) bin
	java -ea -classpath $(RANDOOP_HOME)/systemtests/src/java_collections:${JAVAC_JAR}:$(CLASSPATH) \
	   randoop.main.DataFlow \
	   --scratchdir=systemtests/df-scratch \
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
	rm -f $(diff_files)
	rm -f $(goal_files_bases)

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
# Targets for updating Randoop's distribution.

# Creates the zip file for other people to download.
distribution-files:
	rm -rf randoop dist
	mkdir randoop
	mkdir randoop/bin
# Copy sources and required libraries.
	cp -R src randoop/src
	cp -R tests randoop/tests
# Remove sources for experimental features from the distribution.
# Primary reason for taking them out is to avoid filling the
# distribution with unnecessary extra stuff/supporting jars.
	rm randoop/src/randoop/main/DataFlow.java
	rm randoop/src/randoop/main/ComputeFrontierBranches.java
	rm randoop/src/randoop/main/GenBranchDir.java
	rm randoop/src/randoop/main/Universal*.java
	rm randoop/src/cov/Instrument.java
	rm randoop/src/cov/FilesUtil.java
	rm randoop/src/cov/CountCoverage.java
	rm randoop/src/cov/ASTUtil.java
# Copy required libraries.
	mkdir randoop/lib
	cp lib/plume.jar randoop/lib
	cp lib/jakarta-oro-2.0.8.jar randoop/lib
	cp lib/jakarta-oro-license.txt randoop/lib
# Copy license.
	cp license.txt randoop/
# Copy eclipse project files.
	cp .project randoop/.project
	cp .classpath-dist randoop/.classpath
# Make sure everything works.
	cd randoop && \
	  find src/ tests/ -name "*.java" | xargs ${JAVAC_COMMAND} -d bin -cp 'lib/*'
# Make randoop.jar.
	mkdir randoop/tmp
	cp -r randoop/bin/* randoop/tmp
	cd randoop/tmp && jar xf ../lib/plume.jar
	cd randoop/tmp && jar xf ../lib/jakarta-oro-2.0.8.jar
	cd randoop/tmp && jar cf randoop.jar *
	mv randoop/tmp/randoop.jar randoop/
	rm -r randoop/tmp
# Sanity test jar: invoking randoop terminates normally.
	java -cp randoop/randoop.jar randoop.main.Main 
# Create dist zip file.
	rm -f randoop.zip
	zip -r randoop.zip randoop
# Put zip and jar in "dist" directory.
	mkdir dist
	mv randoop/randoop.jar dist
	mv randoop.zip dist
# Remove scratch directory.
	rm -r randoop


.FORCE:
