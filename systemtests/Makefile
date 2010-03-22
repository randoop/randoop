#
# Makefile for running Randoop experiments.
#
# The Randoop experiments are over several "subject programs,"
# e.g. the java collections.
# For each subject program named SUBJ, the file
# $RANDOOP_HOME/systemtests/experiments/SUBJ.experiment is a property
# file containing information necessary to compile and execute the
# classes under test, e.g. the sub-directory containing the sources,
# any auxiliary jar files, etc.


############################################################
#
# RANDOOP followed by data flow analysis.

RANDOOP_HOME ?= $(shell cd ..; pwd)

include ../common.mk

# Clean up the mess that accumulates when running experiments.
clean:
	find ./ -name  "org.apache.*" | perl -i -n -e 's/^.*$$/"$$&"/g; print $$_; ' | xargs rm
	-rm *.tmp
	-rm *.log
	-rm \
	*.components.gz \
	*.covmap.gz \
	*.covreport.txt \
	*.frontiers.gz \
	*.frontiers.gz.output \
	*.bdgencovmap.gz \
	*.successes.txt \
	*.componentsused.txt \
	*.bdgen.output.txt


# Example target: .df-jc-frontier9.gz
predf:
	java -ea -cp $(CLASSPATH) \
	  randoop.experiments.PreDF \
	  jc

# Example target: .df-jc-frontier9.gz
.df-%:
	java -ea -cp $(CLASSPATH) \
	  randoop.experiments.DFOneFile $*
	touch .df-$*

# Machines on which to run experiments.
machines=potato,potato,potato,rutabaga,yam,turnip,parsnip

# Runs .df-% targets in parallel.
parallel-df:
	java -ea -cp $(CLASSPATH) \
	  -DRANDOOP_HOME=$(RANDOOP_HOME) \
	  randoop.experiments.MultiMachineRunner \
	  randoop.experiments.OneTargetPerLine \
	  .df- \
	  "<none>" \
	  jc.dftargets.txt



############################################################
#

parallel-gen:
	java -ea -cp $(CLASSPATH) \
	  -DRANDOOP_HOME=$(RANDOOP_HOME) \
	  randoop.experiments.MultiMachineRunner \
	  randoop.experiments.OneTargetPerLine \
	  .gen- \
	  jc.dftargets.txt

.gen-%:
	java -ea -cp $(CLASSPATH) \
	  randoop.experiments.BDGenOneFile $*
	touch .gen-$*



all_experiments = \
java_collections \

exp1_subjects = java_collections javax_xml primitives logging jelly commons_collections chain math utilmde

default:
	$(error "No default target.")

# Builds Randoop
build:
	cd .. && make build

# Compiles and instruments (for tracking coverage) a subject program.
#
# Example concrete target: prepare-java_collections
#
prepare-%:
	@java -ea -cp $(CLASSPATH) \
	  randoop.experiments.PrepareSubjectProgram $*

# Calls prepare-SUBJ on all the subject programs.
all-prepare:
	java -ea -cp $(CLASSPATH) \
	  randoop.experiments.MultiMachineRunner \
	  randoop.experiments.PrepareTargetMaker \
	  $(exp1_subjects)

############################################################
# Calculate the size of the sequence space for each subject.

calc-sequence-space:
	java -ea -cp $(CLASSPATH) \
	  -DRANDOOP_HOME=$(RANDOOP_HOME) \
	  randoop.experiments.MultiMachineRunner \
	  randoop.experiments.OneTargetPerArgument \
	  prefix=one-subject-calc- \
	  $(exp1_subjects)

one-subject-calc-%:
	java -ea -cp $(CLASSPATH) \
	  randoop.experiments.CalculateSequenceSpace \
	  $*

############################################################
# Experiment comparing random walk vs. Randoop.

machines=jicama,jicama,jicama,mangold,mangold,mangold,horseradish,rutabaga,yam,turnip,parsnip



# e.g. parstats-omjc
parstats-%:
	find $* -name "$**data.gz" > $*.targets.txt
	perl -i -n -e 's/\.data\.gz//g; print $$_;' $*.targets.txt
	java -ea -cp $(CLASSPATH) \
	  -DRANDOOP_HOME=$(RANDOOP_HOME) \
	  -DRANDOOP_MACHINES=$(machines) \
	  randoop.experiments.MultiMachineRunner \
	  randoop.experiments.OneTargetPerLine "<none>" ".stats" $*.targets.txt

# e.g. omjc/omjc1111.stats
%.stats:
	java ${XMXHEAP} -ea -cp $(CLASSPATH) \
	  randoop.experiments.StatsWriter --writefailures $(*F)

# Combine stats. Ex target: comb-omcc
comb-%:
	java ${XMXHEAP} -ea -cp $(CLASSPATH) \
	   randoop.experiments.CombinedStats 200000000 $*

# omcc fccc fdcc
# ompr fcpr fdpr
# omjc fcjc fdjc
# omjf fcjf fdjf
# omma fcma fdma
# omje fcje fdje

exps = omcc fccc fdcc ompr fcpr fdpr omjc fcjc fdjc omjf fcjf fdjf omma fcma fdma omtr fctr fdtr

tar-stats:
	-rm stats.tgz
	tar cvfz stats.tgz  --exclude=*data*  $(exps)

exp1:
	java -ea -cp $(CLASSPATH) \
	  -DRANDOOP_HOME=$(RANDOOP_HOME) \
	  -DRANDOOP_MACHINES=$(machines) \
	  randoop.experiments.MultiMachineRunner \
	  randoop.experiments.RandWalkExpMaker 50000000 \
	  $(exps)

progress:
	java -ea -cp $(CLASSPATH) \
	  -DRANDOOP_HOME=$(RANDOOP_HOME) \
	  -DRANDOOP_MACHINES=local \
	  randoop.experiments.MultiMachineRunner \
	  randoop.experiments.RandWalkExpMaker count \
	  $(exps)


fd%.data.gz:
	-java -ea -cp $(CLASSPATH) \
	  randoop.experiments.RandoopAllClasses \
	  --component-based=true --objectcache=true \
	  $@
	gzip $(@D)/fd$(*F).data
	make $(@D)/fd$(*F).stats

fc%.data.gz:
	-java -ea -cp $(CLASSPATH) \
	  randoop.experiments.RandoopAllClasses \
	  --component-based=true --objectcache=false \
	  $@
	gzip $(@D)/fc$(*F).data
	make $(@D)/fc$(*F).stats

om%.data.gz:
	-java -ea -cp $(CLASSPATH) \
	  randoop.experiments.RandoopAllClasses \
	  --component-based=false \
	  $@
	gzip $(@D)/om$(*F).data
	make $(@D)/om$(*F).stats


############################################################

exp100:
	make randoop100-omcc-600-0
	make randoop100-fdcc-600-0
	make randoop100-ompr-600-0
	make randoop100-fdpr-600-0
	make randoop100-omjf-600-0
	make randoop100-fdjf-600-0
	make randoop100-omma-600-1
	make randoop100-fdma-600-2
	make randoop100-omtr-600-0
	make randoop100-fdtr-450-0
	make randoop100-omjc-600-0
	make randoop100-fdjc-600-0

# 	@java -ea -cp $(CLASSPATH) randoop.experiments.Randoop100Stats randoop100/randoop100fdcc0.data
# 	@java -ea -cp $(CLASSPATH) randoop.experiments.Randoop100Stats randoop100/randoop100fdjc0.data
# 	@java -ea -cp $(CLASSPATH) randoop.experiments.Randoop100Stats randoop100/randoop100fdjf0.data
# 	@java -ea -cp $(CLASSPATH) randoop.experiments.Randoop100Stats randoop100/randoop100fdma2.data

stats100:
	@java -ea -cp $(CLASSPATH) randoop.experiments.Randoop100Stats omjc 1000000
	@java -ea -cp $(CLASSPATH) randoop.experiments.Randoop100Stats fdjc 1000000
	@echo
	@java -ea -cp $(CLASSPATH) randoop.experiments.Randoop100Stats omcc 1000000
	@java -ea -cp $(CLASSPATH) randoop.experiments.Randoop100Stats fdcc 1000000
	@echo
	@java -ea -cp $(CLASSPATH) randoop.experiments.Randoop100Stats omma 1000000
	@java -ea -cp $(CLASSPATH) randoop.experiments.Randoop100Stats fdma 1000000
	@echo
	@java -ea -cp $(CLASSPATH) randoop.experiments.Randoop100Stats ompr 1000000
	@java -ea -cp $(CLASSPATH) randoop.experiments.Randoop100Stats fdpr 1000000
	@echo
	@java -ea -cp $(CLASSPATH) randoop.experiments.Randoop100Stats omjf 1000000
	@java -ea -cp $(CLASSPATH) randoop.experiments.Randoop100Stats fdjf 1000000
	@echo
	@java -ea -cp $(CLASSPATH) randoop.experiments.Randoop100Stats omtr 1000000
	@java -ea -cp $(CLASSPATH) randoop.experiments.Randoop100Stats fdtr 1000000


# 	@java -ea -cp $(CLASSPATH) randoop.experiments.Randoop100Stats randoop100/randoop100omcc0.data
# 	@java -ea -cp $(CLASSPATH) randoop.experiments.Randoop100Stats randoop100/randoop100omjc0.data
# 	@java -ea -cp $(CLASSPATH) randoop.experiments.Randoop100Stats randoop100/randoop100omjf0.data
# 	@java -ea -cp $(CLASSPATH) randoop.experiments.Randoop100Stats randoop100/randoop100omma0.data

statsom100:
	@java -ea -cp $(CLASSPATH) randoop.experiments.Randoop100Stats randoop100/randoop100omtr0.data

#	@java -ea -cp $(CLASSPATH) randoop.experiments.Randoop100Stats randoop100/randoop100ompr0.data


randoop100%:
	-java -ea -cp $(CLASSPATH) \
	  randoop.experiments.Randoop100 \
	  $@
	gzip randoop100/randoop100$(*F).data

repro-%:
	-java -ea -cp $(CLASSPATH) \
	  randoop.experiments.Repro100 \
	  $*

############################################################
#
# JCute experiment targets.

cute:
	-rm -r jcute/src/java2/
	cd randoop-instr && cp -r . ../jcute/src/
	cd jcute && ./jcutec src/ src/dstest/MTVectorTest.java dstest.MTVectorTest -sequential
	cd jcute && ./jcute dstest.MTVectorTest -i 20 -p 1 > vector.output.txt
	java -ea -cp randoop-instr:.:$(CLASSPATH) randoop.experiments.CountCoverage jcute/vector.output.txt


jcuteds:
	java -ea ${XMXHEAP} -cp src/java_collections:$(CLASSPATH) \
	  randoop.experiments.JCuteDataStructuresExp \
	  experiments/jcute/arraylist.methods \
	  experiments/jcute/arraylist.coveragemethods \
	  cobertura.ser

############################################################
#
# Create zip file of subject programs.

subjects.zip:
	-rm -r tempzip
	mkdir tempzip
	mkdir tempzip/src
	cp -r src/ tempzip/src
	find tempzip/ -name "CVS" | xargs rm -r
	find tempzip/ -name "*.class" | xargs rm
	cd tempzip && zip -r subjects src
	mv tempzip/subjects.zip .
	rm -r tempzip
