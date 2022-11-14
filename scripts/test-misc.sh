#!/bin/bash

# This is the "misc" job of the pull request.

set -e
set -o pipefail
set -o verbose
set -o xtrace
export SHELLOPTS

# Download dependencies, trying a second time if there is a failure.
(./gradlew --write-verification-metadata sha256 help --dry-run ||
     (sleep 60 && ./gradlew --write-verification-metadata sha256 help --dry-run))

./gradlew javadoc
./gradlew manual

if grep -n -r --exclude-dir=test --exclude-dir=testInput --exclude="*~" '^\(import .*\*;$\)'; then
  echo "Don't use wildcard import"
  exit 1
fi

if [ -d "/tmp/$USER/plume-scripts" ] ; then
  git -C "/tmp/$USER/plume-scripts" pull -q > /dev/null 2>&1
else
  mkdir -p "/tmp/$USER" && git -C "/tmp/$USER" clone --depth 1 -q https://github.com/plume-lib/plume-scripts.git
fi
(./gradlew requireJavadoc > /tmp/warnings.txt 2>&1) || true
"/tmp/$USER/plume-scripts/ci-lint-diff" /tmp/warnings.txt
