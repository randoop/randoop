PLUME_SCRIPTS ?= ../build/utils/plume-scripts
ifeq (,$(wildcard ${PLUME_SCRIPTS}))
dummy := $(shell git clone --depth=1 -q https://github.com/plume-lib/plume-scripts.git ${PLUME_SCRIPTS})
endif

.PHONY: all default clean

all default: config.yml

%.yml: %.yml.m4 defs.m4 ../.azure/jobs.m4
	@chmod -f +w $@ || true
	m4 $< > $@
	${PLUME_SCRIPTS}/squeeze-blank-lines $@
	@chmod -w $@

validate: config.yml
	cd .. && circleci config validate

clean:
	rm -f config.yml
