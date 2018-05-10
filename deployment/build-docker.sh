#!/usr/bin/env bash

RELEASE_TIME=$(date +%Y-%m-%dT%H-%M-%S)
PROFILE=kops-spike
API=witan-gateway.kops-spike.mc-ops-sandpit.mastodonc.net
DOMAIN=mastodonc.net
#Secure should be empty if you want false, any other value for true
SECURE=
GIT_COMMIT=$(git rev-parse HEAD)

./scripts/build.sh $API $VIZ $DOMAIN $PROFILE $GIT_COMMIT $SECURE

# put sha in a file
echo $GIT_COMMIT $RELEASE_TIME > target/build/sha

# build docker
docker build -t acron0/witan.ui -f deployment/Dockerfile .

# push docker
docker push acron0/witan.ui:latest
