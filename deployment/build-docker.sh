#!/usr/bin/env bash

RELEASE_TIME=$(date +%Y-%m-%dT%H-%M-%S)
PROFILE=kops-spike
API=witan-58f700d6b8d64c3f.elb.eu-west-1.amazonaws.com
DOMAIN=amazonaws.com
#Secure should be empty if you want false, any other value for true
SECURE=
GIT_COMMIT=$(git rev-parse HEAD)

./scripts/build.sh $API $VIZ $DOMAIN $PROFILE $GIT_COMMIT $SECURE

# put sha in a file
echo $GIT_COMMIT $RELEASE_TIME > target/build/sha

# build docker
docker build -t 201352650455.dkr.ecr.eu-west-1.amazonaws.com/witan_ui:latest -f deployment/Dockerfile .

# push docker
docker push 201352650455.dkr.ecr.eu-west-1.amazonaws.com/witan_ui:latest
