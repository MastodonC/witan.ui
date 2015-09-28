#!/bin/bash

grep -Ero "venue/publish!\s+:([a-z\-]*)" src/cljs/ | awk '{print $2}'
