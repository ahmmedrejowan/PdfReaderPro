#!/usr/bin/env bash
# Runs a Gradle command and, if it is still going after the given number of
# minutes, prints every thread and the heap of the Java processes once.
# The step's own timeout-minutes kills Gradle (it runs without a daemon), so
# this is the only point where a hung build can still be inspected.
#
# Usage: gradle-watchdog.sh <minutes> <gradle args...>
set -u
minutes="$1"
shift

./gradlew "$@" &
gradle_pid=$!
deadline=$((SECONDS + minutes * 60))
dumped=false

while kill -0 "$gradle_pid" 2>/dev/null; do
  if [ "$dumped" = false ] && [ "$SECONDS" -ge "$deadline" ]; then
    echo "::warning::Gradle still running after ${minutes} minutes, dumping its state"
    jps -lv || true
    for pid in $(jps -q); do
      echo "===== $pid ====="
      jcmd "$pid" GC.heap_info || true
      jcmd "$pid" Thread.print || true
    done
    dumped=true
  fi
  sleep 5
done

wait "$gradle_pid"
