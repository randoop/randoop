.PHONY: all default clean

all default: azure-pipelines.yml

azure-pipelines.yml: azure-pipelines.yml.m4 defs.m4 jobs.m4
	@chmod -f +w $@ || true
	m4 $< > $@
	@chmod -w $@

clean:
	rm -f azure-pipelines.yml
