#!/usr/bin/env bash
lein garden once
aws s3 sync resources/public/css s3://witan-ui/
