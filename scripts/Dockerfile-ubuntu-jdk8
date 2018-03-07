# Create a Docker image that is ready to run the main Randoop tests,
# using JDK 8.

FROM ubuntu
MAINTAINER Michael Ernst <mernst@cs.washington.edu>

# According to
# https://docs.docker.com/engine/userguide/eng-image/dockerfile_best-practices/:
#  * Put "apt-get update" and "apt-get install" in the same RUN command.
#  * Do not run "apt-get upgrade"; instead get upstream to update.
RUN apt-get -qqy update \
&& apt-get -qqy install \
  git \
  gradle \
  xvfb \
  default-jdk \
&& apt-get -qqy update \
&& apt-get -qqy install \
  python-pip \
&& apt-get clean \
&& rm -rf /var/lib/apt/lists/* \
&& pip install html5validator
