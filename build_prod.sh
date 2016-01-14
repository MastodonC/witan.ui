#!/usr/bin/env bash

# build
lein clean
LOG_LEVEL=2 lein cljsbuild once prod
lein garden once

# cp
mkdir target/build
cp -r resources/public/* target/build/

#
echo "Files written to target/build"
