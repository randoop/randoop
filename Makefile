.PHONY: all default

default: style-check
all: style-fix style-check

# Code style; defines `style-check` and `style-fix`.
ifeq (,$(wildcard .plume-scripts))
dummy := $(shell git clone -q https://github.com/plume-lib/plume-scripts.git .plume-scripts)
endif
include .plume-scripts/code-style.mak

style-check: sorting-style-check
style-fix: sorting-style-fix
sorting-style-check:
	@LC_ALL=C sort -c -u src/systemTest/resources/test-methodspecs/CollectionsTest.methodspecs
	@LC_ALL=C sort -c -u src/systemTest/resources/test-methodspecs/JDKTest.methodspecs
	@LC_ALL=C sort -c -u src/systemTest/resources/test-methodspecs/NaiveCollectionsTest.methodspecs
sorting-style-fix:
	@(cd src/systemTest/resources/test-methodspecs/ && LC_ALL=C sort -u -o CollectionsTest.methodspecs CollectionsTest.methodspecs)
	@(cd src/systemTest/resources/test-methodspecs/ && LC_ALL=C sort -u -o JDKTest.methodspecs JDKTest.methodspecs)
	@(cd src/systemTest/resources/test-methodspecs/ && LC_ALL=C sort -u -o NaiveCollectionsTest.methodspecs NaiveCollectionsTest.methodspecs)
