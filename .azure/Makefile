all default: azure-pipelines.yml
# TODO: all default: azure-pipelines.yml azure-pipelines-daily.yml

azure-pipelines.yml: azure-pipelines.yml.m4 defs.m4 jobs.m4
	m4 $< > $@

clean:
	rm -f azure-pipelines.yml
