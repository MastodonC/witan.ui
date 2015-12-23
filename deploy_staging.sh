#!/usr/bin/env bash

# using deployment service sebastopol
TAG=git-$(echo $CIRCLE_SHA1 | cut -c1-12)
sed "s/@@TAG@@/$TAG/" witan-ui.json.template > witan-ui.json

# we want curl to output something we can use to indicate success/failure
STATUS=$(curl -s -o /dev/null -w "%{http_code}" -X POST http://$DEPLOY_IP:9501/marathon/witan-ui -H "Content-Type: application/json" -H "$SEKRIT_HEADER: 123" --data-binary "@witan-ui.json")
echo "HTTP code " $STATUS
if [ $STATUS == "201" ]
then exit 0
else exit 1
fi
