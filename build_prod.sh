#!/usr/bin/env bash

# build
lein clean
lein cljsbuild once prod
lein garden once

# move
rm -rf build
mkdir build
cp -r resources/public/* build/
