#!/usr/bin/env bash

# exports
export WITAN_API_URL=$1
export WITAN_VIZ_URL=$2
export ENVIRONMENT=$3

if [ "$ENVIRONMENT" == "production" ]
then
    export LOG_LEVEL=2
fi

# build
lein clean
lein cljsbuild once prod
lein garden once

# cp
mkdir target/build
cp -r resources/public/* target/build/

#
echo "Files written to target/build"
