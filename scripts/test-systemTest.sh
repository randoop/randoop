#!/bin/bash

# This runs `./gradlew systemTest`.

set -e
set -o pipefail
set -o verbose
set -o xtrace
export SHELLOPTS

./gradlew assemble

# Need GUI for running runDirectSwingTest.
# Run xvfb.
export DISPLAY=:99.0
XVFB=/usr/bin/Xvfb
XVFBARGS="$DISPLAY -ac -screen 0 1024x768x16 +extension RANDR"
PIDFILE=/tmp/xvfb_${DISPLAY:1}.pid
/sbin/start-stop-daemon --start --quiet --pidfile $PIDFILE --make-pidfile --background --exec $XVFB -- $XVFBARGS
sleep 3 # give xvfb some time to start

# There is no need for checkstyle targets here; they are checked in "misc" job.
./gradlew --info --stacktrace systemTest \
  -x checkstyle -x checkstyleMain -x checkstyleCoveredTest -x checkstyleReplacecallTest
