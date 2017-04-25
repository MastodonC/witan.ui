#!/usr/bin/env bash

# exports
export WITAN_API_URL=$1 # URL to contact Witan Gateway
export WITAN_VIZ_URL=$2 # URL to load Witan Viz
export WITAN_DOMAIN=$3  # The host domain (e.g. witanforcities.com)
export ENVIRONMENT=$4   # Environment
export WITAN_API_SECURE=$5 # tls for witan api
export BUILD_SHA=$6 # SHA
export BUILD_DT=$(date) # Build Date and Time
export INTERCOM=1       # Intercom enabled

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
