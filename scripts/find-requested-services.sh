#!/bin/bash

grep -Ero "venue/request!\s+[a-z\-]+\s+:([a-z\-\/]+)\s+:([a-z\-]+)" src/cljs/ | awk '{print $3, $4}'
