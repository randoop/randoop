default: style-check
all: style-fix style-check

# Code style; defines `style-check` and `style-fix`.
ifeq (,$(wildcard .plume-scripts))
dummy != git clone -q https://github.com/plume-lib/plume-scripts.git .plume-scripts
endif
include .plume-scripts/code-style.mak
