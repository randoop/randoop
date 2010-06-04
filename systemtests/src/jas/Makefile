#
# $Id: Makefile 2226 2008-11-17 22:14:00Z kredel $
#
# Makefile with default rules for the Java-Algebra-System
# by Heinz kredel
#
# created 2.6.2000 hk 
# modified 1.1.2003 hk 
# todo 

# set this to your jdk binaries path
#JDK=/usr/lib/jvm/java/bin
#JDK=/usr/java/jdk1.6.0_02/bin
#JDK=/usr/lib/jvm/java-1.5.0/bin
#JDK=/usr/java/jdk1.5.0_01/bin
#JDK=/usr/java/j2sdk1.4.1_01/bin
#JDK=/usr/java/j2sdk1.4.0_01/bin
#JDK=/opt/jdk1.4/bin
#JDK=/opt/jdk1.4.0b3/bin
#JDK=/usr/lib/jdk1.3/bin
#JDK=/opt/jdk1.2.2/bin
#JDK=/usr/lib/java/bin
JDK=/usr/java/latest/bin

JASPATH=/home/kredel/jas
SVNREPO=/home/SUBVERSION
LIBPATH=/home/kredel/java/lib
JUNITPATH=$(LIBPATH)/junit.jar
LOG4JPATH=$(LIBPATH)/log4j.jar
JOMPPATH=$(LIBPATH)/jomp1.0b.jar
TNJPATH=$(LIBPATH)/tnj.jar
LINTPATH=$(LIBPATH)/lint4j.jar

# --- syncing ----------
DRY=--dry-run
DELETE=
RSYNC=rsync -e ssh -avuz $(DRY) $(DELETE) --exclude=*~ --include=svn_change.log --exclude=*.log* --exclude=*.out* --exclude=*.txt* --exclude=.svn 
####--exclude=./test
####--exclude=*.ps --exclude=*.pdf --exclude=spin*
####--exclude=*/.jxta/
PART=jas.j16
VERSION=jas-2.2
BRANCH=2.2
SVNVERSION=`grep committed-rev .svn/entries |head -1|awk -F = '{ print $2 }'|sed 's/"//g'`

all:

home:
	$(RSYNC) krum:java/$(PART)/     .

heinz:
	$(RSYNC) ./                heinz@heinz2:$(PART)

krum:
	$(RSYNC) -e 'ssh -p 2222' ./                krum:java/$(PART)

pub:
	$(RSYNC) --exclude=*ufd* --exclude=DTD --exclude=lisa* --exclude=*xml ./ krum:htdocs/$(PART)

compute:
	$(RSYNC) ./                compute:java/$(VERSION)

# --- end syncing ----------


# no need to change below this line

# command line arguments
cl=

#.EXPORT_ALL_VARIABLES :

DEFS=$(JASPATH)/arith:$(JASPATH)/poly:$(JASPATH)/ps:$(JASPATH)/vector:$(JASPATH)/ring:$(JASPATH)/ufd:$(JASPATH)/module:$(JASPATH)/util:$(JASPATH)/application
DOCCLASSES=$(JUNITPATH):$(LOG4JPATH):$(JOMPPATH)
#:$(TNJPATH)
DOCOPTS=-package
#DOCOPTS=-package -version -author
#DOCOPTS=-public -protected -package -author -version

MYCLASSPATH = .:$(DEFS):$(JUNITPATH):$(LOG4JPATH):$(JOMPPATH)
#:$(TNJPATH)

JAVAC=$(JDK)/javac -classpath $(MYCLASSPATH) -d . -Xlint:unchecked
#-Xlint:unchecked
#JAVA=$(JDK)/java -classpath $(MYCLASSPATH) -verbose:gc 
JAVA=$(JDK)/java -classpath $(MYCLASSPATH) -server -Xms300M -Xmx600M -XX:+AggressiveHeap -XX:+UseParallelGC -XX:ParallelGCThreads=2 -verbose:gc 
#-Xbatch
#old#JAVA=$(JDK)/java -classpath $(MYCLASSPATH) -Xms300M -Xmx600M -XX:+AggressiveHeap -XX:+UseParallelGC -verbose:gc 
#JAVA=$(JDK)/java -classpath $(MYCLASSPATH) -verbose:gc -Xrunhprof:cpu=times,format=a
#JAVA=$(JDK)/java -classpath $(MYCLASSPATH) -verbose:gc -verbose:class -verbose:jni
DOC=$(JDK)/javadoc -classpath $(DOCCLASSES)

usage:
	echo; echo "usage: make <name> cl='cmd'"; echo


FIX       = fixm2j
GETC      = getc.pl
#JOPT      = -prof:gb.prof -tm
#-mx100000000


.SUFFIXES :
.SUFFIXES : .class .java 
.PRECIOUS : %.java %.class edu/jas/arith/%.class edu/jas/poly/%.class edu/jas/ps/%.class edu/jas/ring/%.class edu/jas/vector/%.class edu/jas/ufd/%.class edu/jas/module/%.class edu/jas/structure/%.class edu/jas/util/%.class edu/jas/application/%.class edu/jas/kern/%.class edu/jas/%.class

.PHONY    : clean doc

all:
	$(JAVAC) */*.java

%.class: %.java
	$(JAVAC) $<


edu/jas/%.class: %.java
	$(JAVAC) $<

edu/jas/arith/%.class: %.java
	$(JAVAC) $<

edu/jas/poly/%.class: %.java
	$(JAVAC) $<

edu/jas/ps/%.class: %.java
	$(JAVAC) $<

edu/jas/ring/%.class: %.java
	$(JAVAC) $<

edu/jas/ufd/%.class: %.java
	$(JAVAC) $<

edu/jas/vector/%.class: %.java
	$(JAVAC) $<

edu/jas/module/%.class: %.java
	$(JAVAC) $<

edu/jas/structure/%.class: %.java
	$(JAVAC) $<

edu/jas/util/%.class: %.java
	$(JAVAC) $<

edu/jas/application/%.class: %.java
	$(JAVAC) $<

edu/jas/kern/%.class: %.java
	$(JAVAC) $<

edu/mas/kern/%.class: %.java
	$(JAVAC) $<


edu.jas.%: edu/jas/%.class
	$(JAVA) $@ $(cl)

edu.jas.arith.%: edu/jas/arith/%.class
	$(JAVA) $@ $(cl)

edu.jas.poly.%: edu/jas/poly/%.class
	$(JAVA) $@ $(cl)

edu.jas.ps.%: edu/jas/ps/%.class
	$(JAVA) $@ $(cl)

edu.jas.ring.%: edu/jas/ring/%.class
	$(JAVA) $@ $(cl)

edu.jas.ufd.%: edu/jas/ufd/%.class
	$(JAVA) $@ $(cl)

edu.jas.vector.%: edu/jas/vector/%.class
	$(JAVA) $@ $(cl)

edu.jas.module.%: edu/jas/module/%.class
	$(JAVA) $@ $(cl)

edu.jas.structure.%: edu/jas/structure/%.class
	$(JAVA) $@ $(cl)

edu.jas.util.%: edu/jas/util/%.class
	$(JAVA) $@ $(cl)

edu.jas.application.%: edu/jas/application/%.class
	$(JAVA) $@ $(cl)

edu.jas.kern.%: edu/jas/kern/%.class
	$(JAVA) $@ $(cl)

edu.mas.kern.%: edu/mas/kern/%.class
	$(JAVA) $@ $(cl)


#FILES=$(wildcard src/edu/jas/structure/*.java src/edu/jas/arith/*.java src/edu/jas/poly/*.java src/edu/jas/ps/*.java src/edu/jas/ring/*.java src/edu/jas/ufd/*.java src/edu/jas/application/*.java src/edu/jas/vector/*.java src/edu/jas/module/*.java src/edu/jas/util/*.java src/edu/jas/kern/*.java)
FILES=$(wildcard src/edu/jas/structure/*.java src/edu/jas/arith/*.java src/edu/jas/poly/*.java src/edu/jas/ps/*.java src/edu/jas/ring/*.java src/edu/jas/application/*.java src/edu/jas/vector/*.java src/edu/jas/module/*.java src/edu/jas/util/*.java src/edu/jas/ufd/*.java src/edu/jas/kern/*.java)

LIBS=$(JUNITPATH) $(LOG4JPATH) $(JOMPPATH) $(TNJPATH)

#CLASSES=$(wildcard edu/jas/structure/*.java edu/jas/arith/*.class edu/jas/poly/*.class edu/jas/ps/*.class edu/jas/ring/*.class edu/jas/ufd/*.class edu/jas/application/*.class edu/jas/vector/*.class edu/jas/module/*.class edu/jas/util/*.class edu/jas/kern/*.class)
#CLASSES=edu/jas

#CLASSES=edu/jas/structure/ edu/jas/arith/ edu/jas/poly/ edu/jas/ps/ edu/jas/ring/ edu/jas/ufd/ edu/jas/application/ edu/jas/vector/ edu/jas/module/ edu/jas/util/ edu/jas/kern/

CLASSES=edu/jas/structure/ edu/jas/arith/ edu/jas/poly/ edu/jas/ps/ edu/jas/ring/ edu/jas/application/ edu/jas/vector/ edu/jas/module/ edu/jas/util/ edu/jas/ufd/ edu/jas/kern/

PYS=$(wildcard *.py)
EXAMPY=$(wildcard examples/*.py)

DOCU=$(wildcard jas-log.html index.html problems.html design.html COPYING* sample.jythonrc overview.html)
# */package.html 

doc: $(FILES)
	$(DOC) $(DOCOPTS) -d doc $(FILES) 

epydoc: examples/jas.py
	epydoc -o doc/jython -n JAS -u ../../index.html examples/jas.py

ALLJAR=$(FILES) $(DOCU) Makefile build.xml log4j.properties $(PYS)

jas-all.jar: $(ALLJAR)
	$(JDK)/jar -cvf jas.jar $(ALLJAR) edu/ 
	mv jas.jar /tmp/jas-`date +%Y%j`.jar

jas.tgz: $(FILES) *.html */package.html TODO
	tar -cvzf jas.tgz $(FILES) *.html */package.html TODO
	mv jas.tgz /tmp/jas-`date +%Y%j`.tgz

#	cp jas.jar ...../jas-`date +%Y%j`.jar
#jas-run.jar: GBManifest.MF $(TOJAR)
#	$(JDK)/jar -cvfm jas-run.jar GBManifest.MF $(TOJAR)


TOJAR=$(FILES) $(CLASSES) Makefile build.xml log4j.properties $(EXAMPY) examples/machines.test $(wildcard COPYING*)

jas.jar: $(FILES) Makefile build.xml log4j.properties $(EXAMPY)
	$(JDK)/jar -cf jas.jar $(TOJAR)
#	$(JDK)/jar -cf jas.jar $(filter-out %/ufd/, $(filter-out src/edu/jas/ufd/%.java, $(TOJAR)))

jas-doc.jar: $(DOCU) doc/
	$(JDK)/jar -cvf jas-doc.jar $(DOCU) doc/


dist: jas.jar jas-run.jar jas-doc.jar $(LIBS)
	tar -cvzf jas-dist.tgz jas.jar jas-run.jar jas-doc.jar $(LIBS)

#

jars: jas-run.jar jas-doc.jar
#jars: jas.jar jas-run.jar jas-doc.jar


lint:
	$(JDK)/java -jar $(LINTPATH) -v 3 -classpath $(DOCCLASSES):jas.jar -sourcepath src edu.jas.*
#	$(JDK)/java -jar $(LINTPATH) -v 5 -exact -classpath $(DOCCLASSES) -sourcepath src edu.jas.*


clean:
	find . -name "*~" -follow -print -exec rm {} \;
	rm -f application/application arith/arith kern/kern module/module poly/poly ps/ps ring/ring structure/structure ufd/ufd util/util vector/vector


#svn copy file:///$(SVNREPO)/jas/trunk file:///$(SVNREPO)/jas/tags/$(VERSION)

SVNREV=svnlook youngest $(SVNREPO)/jas
SVNDATE=svnlook date $(SVNREPO)/jas
# Jan 2008 #SVNSRT=1584 
# Jun 2008
SVNSRT=1882

export:
	rm -rf ~/jas-versions/$(VERSION)
	svn export --quiet file:///$(SVNREPO)/jas/trunk ~/jas-versions/$(VERSION)
	cd ~/jas-versions/$(VERSION); jas_dosed $(VERSION) `$(SVNREV)` download.html
#	svn log -v -r HEAD:BASE src > ~/jas-versions/$(VERSION)/svn_change.log
	svn log -v -r HEAD:$(SVNSRT) file:///$(SVNREPO)/jas/trunk src examples > ~/jas-versions/$(VERSION)/svn_change.log
	cd ~/jas-versions/; jar -cf $(VERSION).`$(SVNREV)`-src.jar $(VERSION)/
	cd ~/jas-versions/$(VERSION)/; ant compile > ant_compile.out
	cd ~/jas-versions/$(VERSION)/; jar -cf ../$(VERSION).`$(SVNREV)`-bin.jar edu/ COPYING*
	cd ~/jas-versions/$(VERSION)/; ant test > ant_test.out
	cd ~/jas-versions/$(VERSION)/; sh ./jython_tests.sh >jython_tests.out 2>&1
	cd ~/jas-versions/$(VERSION)/; ant doc > ant_doc.out
	cd ~/jas-versions/$(VERSION)/; epydoc -o doc/jython -n JAS -u ../../index.html examples/jas.py > epydoc.out
	cd ~/jas-versions/$(VERSION)/; jar -cf ../$(VERSION).`$(SVNREV)`-doc.jar doc/ *.html
	cp ~/jas-versions/$(VERSION).`$(SVNREV)`-bin.jar $(LIBPATH)/jas.jar
	cp ~/jas-versions/$(VERSION).`$(SVNREV)`-bin.jar ~/jas-versions/$(VERSION)/jas.jar
	mv ~/jas-versions/$(VERSION).`$(SVNREV)`-*.jar ~/jas-versions/$(VERSION)/
	cd ~/jas-versions/$(VERSION)/meditor; jas_dosed $(VERSION) `$(SVNREV)` manifest.mf
	cd ~/jas-versions/$(VERSION)/meditor; make > make_meditor.out


deploy:
	$(RSYNC) -e 'ssh -p 2222' --delete-after --exclude=DTD --exclude=*xml ~/jas-versions/$(VERSION)/ krum:htdocs/$(VERSION)


young:
	echo youngest revision `svnlook youngest $(SVNREPO)/jas`

subst:
	cd ~/jas-versions/$(VERSION); jas_dosed $(VERSION) `$(SVNREV)` download.html


# lines of code and number of classes
loc:
	find src -name "*.java" | wc -l
	find src -name "*.java" | grep -v Test | wc -l
	find src -name "*.java" | grep    Test | wc -l 
	find src -name "*.java" | xargs cat | wc
	find src -name "*.java" | grep -v Test | xargs cat | wc
	find src -name "*.java" | grep    Test | xargs cat | wc

# -eof-
