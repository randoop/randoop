.PHONY: all default

default: style-check
all: style-fix style-check

# Code style; defines `style-check` and `style-fix`.
ifeq (,$(wildcard .plume-scripts))
dummy := $(shell git clone --depth=1 -q https://github.com/plume-lib/plume-scripts.git .plume-scripts)
endif
include .plume-scripts/code-style.mak

style-check: sorting-style-check
style-fix: sorting-style-fix
sorting-style-check:
	@LC_ALL=C sort -c -u src/systemTest/resources/test-covgoals/CollectionsTest.covgoals
	@LC_ALL=C sort -c -u src/systemTest/resources/test-covgoals/JDKTest.covgoals
	@LC_ALL=C sort -c -u src/systemTest/resources/test-covgoals/NaiveCollectionsTest.covgoals
sorting-style-fix:
	@(cd src/systemTest/resources/test-covgoals/ && LC_ALL=C sort -u -o CollectionsTest.covgoals CollectionsTest.covgoals)
	@(cd src/systemTest/resources/test-covgoals/ && LC_ALL=C sort -u -o JDKTest.covgoals JDKTest.covgoals)
	@(cd src/systemTest/resources/test-covgoals/ && LC_ALL=C sort -u -o NaiveCollectionsTest.covgoals NaiveCollectionsTest.covgoals)
