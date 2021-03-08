#!/bin/bash

# This is the "misc" job of the pull request.

set -e
set -o pipefail
set -o verbose
set -o xtrace
export SHELLOPTS

./gradlew javadoc
./gradlew checkstyle checkstyleMain checkstyleCoveredTest checkstyleReplacecallTest
./gradlew manual

if [ -d "/tmp/$USER/plume-scripts" ] ; then
  git -C /tmp/$USER/plume-scripts pull -q > /dev/null 2>&1
else
  mkdir -p "/tmp/$USER" && git -C "/tmp/$USER" clone --depth 1 -q https://github.com/plume-lib/plume-scripts.git
fi
(./gradlew requireJavadocPrivate > /tmp/warnings.txt 2>&1) || true
/tmp/$USER/plume-scripts/ci-lint-diff /tmp/warnings.txt
