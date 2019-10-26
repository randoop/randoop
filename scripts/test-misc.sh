#!/bin/bash

# This is the "misc" job of the pull request.

set -e
set -o pipefail
set -o verbose
set -o xtrace
export SHELLOPTS

./gradlew clean assemble -PuseCheckerFramework=true
./gradlew javadoc
./gradlew checkstyle checkstyleMain checkstyleCoveredTest checkstyleReplacecallTest
./gradlew manual

git -C /tmp/plume-scripts pull > /dev/null 2>&1 \
  || git -C /tmp clone --depth 1 -q https://github.com/plume-lib/plume-scripts.git
(./gradlew requireJavadocPrivate > /tmp/warnings.txt 2>&1) || true
/tmp/plume-scripts/ci-lint-diff /tmp/warnings.txt
