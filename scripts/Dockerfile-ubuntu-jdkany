# Create a Docker image that is ready to run the full Randoop tests,
# including building the manual and Javadoc.
# But it's used primarily for running miscellaneous tests such as the manual
# and Javadoc.

FROM ubuntu
MAINTAINER Michael Ernst <mernst@cs.washington.edu>

# According to
# https://docs.docker.com/engine/userguide/eng-image/dockerfile_best-practices/:
#  * Put "apt-get update" and "apt-get install" in the same RUN command.
#    Also, minimize the number of RUN commands, to minimize te number of layers.
#  * Do not run "apt-get upgrade"; instead get upstream to update.
# certs lines are temporary, from https://stackoverflow.com/a/50103533/173852 .

RUN export DEBIAN_FRONTEND=noninteractive \
&& apt-get -qqy update \
&& apt-get -qqy install \
  git \
  gradle \
  xvfb \
  openjdk-8-jdk \
&& apt-get -qqy update \
&& apt-get -qqy install \
  perl \
  python-pip \
  wget \
&& apt-get clean \
&& rm -rf /var/lib/apt/lists/* \
&& pip install html5validator \
&& /usr/bin/printf '\xfe\xed\xfe\xed\x00\x00\x00\x02\x00\x00\x00\x00\xe2\x68\x6e\x45\xfb\x43\xdf\xa4\xd9\x92\xdd\x41\xce\xb6\xb2\x1c\x63\x30\xd7\x92' > /etc/ssl/certs/java/cacerts \
&& /var/lib/dpkg/info/ca-certificates-java.postinst configure
