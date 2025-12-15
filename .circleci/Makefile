.PHONY: all default clean

all default: config.yml

config.yml: config.yml.m4 defs.m4 ../.azure/jobs.m4
	@chmod -f +w $@ || true
	m4 $< > $@
# Remove leading blank lines.
	sed '/[^[:blank:]]/,$$!d' $@ > $@.tmp
	mv -f $@.tmp $@
	@chmod -w $@

validate: config.yml
	cd .. && circleci config validate

clean:
	rm -f config.yml
