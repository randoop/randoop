.PHONY: all default clean

all default: azure-pipelines.yml

azure-pipelines.yml: azure-pipelines.yml.m4 defs.m4 jobs.m4
	m4 $< > $@

clean:
	rm -f azure-pipelines.yml
