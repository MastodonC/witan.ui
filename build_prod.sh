#!/usr/bin/env bash

# exports
export LOG_LEVEL=2
export WITAN_API_URL=$1

# build
lein clean
lein cljsbuild once prod
lein garden once

# cp
mkdir target/build
cp -r resources/public/* target/build/

#
echo "Files written to target/build"
