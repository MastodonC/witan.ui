#!/usr/bin/env python
import os
import sys
import subprocess
import re

DATA_DIR=os.path.dirname(os.path.realpath(__file__))
SRC_DIR=os.path.join(DATA_DIR, "..", "src")

# grep
proc = subprocess.Popen(["grep",
                               "-orE",
                               "\[:[a-z-]+\s:(success|failure)\]",
                                SRC_DIR],
                               stdout=subprocess.PIPE)

# regex
line_regex = re.compile(ur'^(.*.cljs):\[(:[a-z-]+)\s:(success|failure)')
results = [re.search (line_regex , line.rstrip().replace(SRC_DIR, '')).groups ()
                  for line in iter(proc.stdout.readline,'')]

groups = {}
for entry in results:
    if not groups.has_key(entry[0]):
        groups[entry[0]] = []
    groups[entry[0]].append(entry[1:])

pairs = {}
for key, handles in groups.iteritems():
    for handle, status in handles:
        check = [x for x in handles if x[0] == handle]
        if len(check) != 2:
            if not pairs.has_key(key):
                pairs[key] = []
            pairs[key].append(check)

# print
if len(pairs) != 0:
    print "Results - the following calls do NOT have complete :success/:failure pairs:"
    print "--------------------------------------------------------"
    for key, values in pairs.iteritems():
        print key
        for handles in values:
            print " - ", handles
    sys.exit(1)
else:
    sys.exit(0)
