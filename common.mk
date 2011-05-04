
# Make will silently continue if file does not exist.
-include ../Makefile.user
-include Makefile.user

CLASS_DIRS := $(RANDOOP_HOME)/bin $(RANDOOP_HOME)/tests

# A classpath like $(CLASS_DIRS):$(RANDOOP_HOME)/lib/* is easier to read,
# but the * classpath operator doesn't work under some (not well
# understood) circumstances.  More importantly, we don't want the default
# order of the files.

# Use the same order of jar files as in .classpath
CLASSPATH_WITH_SPACES := $(RANDOOP_EXTRA_CLASSPATH):$(CLASS_DIRS):$(shell perl -n -e 'if (/kind="lib" path="(.*)"\/>/) { print ":$(RANDOOP_HOME)/$$1"; }' $(RANDOOP_HOME)/.classpath)

# Convert to a standard classpath
empty:=
space:= $(empty) $(empty)
export CLASSPATH := $(subst $(space),:,$(CLASSPATH_WITH_SPACES))

XMXHEAP := -Xmx1024m 

# Path to plume-lib. Used when creating Randoop manual
# (see Makefile, target "manual")
export PATH := $(PATH):$(RANDOOP_HOME)/utils/plume-lib/bin
