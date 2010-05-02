# INSTRUCTIONS:
#
# This file contains developer-specific variables used to compile Randoop.
# Substitute the appropriate values for your own environment

# 1. The location of your JDK.
#    We use this variable to determine the location of tools.jar, which
#    we expect to find in $(JAVA_HOME)/lib/tools.jar.

JAVA_HOME ?= /usr/lib/jvm/java-6-sun-1.6.0.12

############################################################
### YOU PROBABLY DON'T NEED TO MODIFY STUFF BELOW THIS LINE.                                                                                                                                       

# Make will silently continue if file does not exist.
-include ../Makefile.user
-include Makefile.user

CLASS_DIRS := $(RANDOOP_HOME)/bin $(RANDOOP_HOME)/tests

# The shorter version of the classpath is much easier to read in the
# command output than the full version.  But the * classpath operator
# doesn't work under some (not well understood) circumstances
ifdef CLASSPATH_SUPPORTS_STAR
  CLASS_DIRS := $(CLASS_DIRS):$(RANDOOP_HOME)/lib/*
else
  CLASS_DIRS := $(CLASS_DIRS):$(wildcard $(RANDOOP_HOME)/lib/*.jar)
endif

# Convert to a standard classpath
empty:=
space:= $(empty) $(empty)
export CLASSPATH :=     $(subst $(space),:,$(RANDOOP_EXTRA_CLASSPATH):$(CLASS_DIRS)):$(JAVA_HOME)/lib/tools.jar

XMXHEAP := -Xmx1650m 
