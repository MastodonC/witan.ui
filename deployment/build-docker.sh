#!/usr/bin/env bash

RELEASE_TIME=$(date +%Y-%m-%dT%H-%M-%S)
PROFILE=staging
API=witan-1256724457.eu-west-1.elb.amazonaws.com
DOMAIN=amazonaws.com
#Secure should be empty if you want false, any other value for true
SECURE=true
GIT_COMMIT=$(git rev-parse HEAD)

./scripts/build.sh $API $VIZ $DOMAIN $PROFILE $GIT_COMMIT $SECURE

# put sha in a file
echo $GIT_COMMIT $RELEASE_TIME > target/build/sha

# build docker
docker build -t mattford63/witan_ui:latest -f deployment/Dockerfile .

# push docker
docker push mattford63 /witan_ui:latest
