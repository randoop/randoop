#!/bin/bash

# This runs `./gradlew systemTest`.

set -e
set -o pipefail
set -o verbose
set -o xtrace
export SHELLOPTS

# Download dependencies, trying a second time if there is a failure.
(./gradlew --write-verification-metadata sha256 help --dry-run ||
     (sleep 60 && ./gradlew --write-verification-metadata sha256 help --dry-run))

./gradlew assemble

# Need GUI for running runDirectSwingTest.
# Run xvfb.
export DISPLAY=:99.0
XVFB=/usr/bin/Xvfb
XVFBARGS="$DISPLAY -ac -screen 0 1024x768x16 +extension RANDR"
PIDFILE=/tmp/xvfb_${DISPLAY:1}.pid
/sbin/start-stop-daemon --start --quiet --pidfile $PIDFILE --make-pidfile --background --exec $XVFB -- $XVFBARGS
sleep 3 # give xvfb some time to start

./gradlew --info --stacktrace systemTest
