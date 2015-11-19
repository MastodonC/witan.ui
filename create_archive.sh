#!/usr/bin/env bash

#
lein cljsbuild once prod
lein garden once
find resources/public/ -path '*/.*' -prune -o -type f -print | zip witan-ui.zip -@
