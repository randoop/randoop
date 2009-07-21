
# Make will silently continue if file does not exist.
-include ../Makefile.user
-include Makefile.user

ifndef RANDOOP_HOME
$(error Variable RANDOOP_HOME not found. This variable must be defined)
endif

ifndef INV
$(error Variable INV not found. This variable must be defined)
endif

ifndef inv
$(error Variable "inv" not found. This variable must be defined)
endif

CLASSPATH := $(RANDOOP_HOME)/bin:$(RANDOOP_HOME)/tests:$(RANDOOP_HOME)/lib/asm-2.2.1.jar:$(RANDOOP_HOME)/lib/asm-tree-2.2.1.jar:$(RANDOOP_HOME)/lib/jakarta-oro-2.0.8.jar:$(RANDOOP_HOME)/lib/junit-4.3.1.jar:$(RANDOOP_HOME)/lib/log4j-1.2.9.jar:$(RANDOOP_HOME)/systemtests/clover-ant-2.3.1/lib/clover.jar:$(RANDOOP_HOME)/lib/org.eclipse.core.contenttype_3.2.100.v20070319.jar:$(RANDOOP_HOME)/lib/org.eclipse.core.jobs_3.3.1.R33x_v20070709.jar:$(RANDOOP_HOME)/lib/org.eclipse.core.resources_3.3.1.R33x_v20080205.jar:$(RANDOOP_HOME)/lib/org.eclipse.core.runtime_3.3.100.v20070530.jar:$(RANDOOP_HOME)/lib/org.eclipse.equinox.common_3.3.0.v20070426.jar:$(RANDOOP_HOME)/lib/org.eclipse.equinox.preferences_3.2.101.R33x_v20080117.jar:$(RANDOOP_HOME)/lib/org.eclipse.jdt.core_3.3.3.v_793_R33x.jar:$(RANDOOP_HOME)/lib/org.eclipse.osgi_3.3.2.R33x_v20080105.jar:$(RANDOOP_HOME)/lib/org.eclipse.text_3.3.0.v20070606-0010.jar:$(RANDOOP_HOME)/lib/iText-2.1.1.jar:$(RANDOOP_HOME)/lib/jcommon-1.0.13.jar:$(RANDOOP_HOME)/lib/jfreechart-1.0.10-swt.jar:$(RANDOOP_HOME)/lib/jfreechart-1.0.10.jar:$(RANDOOP_HOME)/lib/servlet.jar:$(RANDOOP_HOME)/lib/swtgraphics2d.jar:$(CLASSPATH):$(INV)/java:$(INV)/java/lib/bcel.jar
