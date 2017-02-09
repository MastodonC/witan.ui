#!/usr/bin/env python
import os
import sys
import subprocess
import re

DATA_DIR=os.path.dirname(os.path.realpath(__file__))
SRC_DIR=os.path.join(DATA_DIR, "..", "src", "cljs")
STRINGS_PATH=os.path.join(SRC_DIR, "witan", "ui", "strings.cljs")

unused_intro = False
undefined_intro = False
found_strings = []
line_regex = re.compile('\:[0-9]+\:(.+)')
string_kws = []
string_kw_re = re.compile('(\:string\/[a-zA-Z0-9\-]+)\s(.+)')
string_kw_re_lite = re.compile('(\:string\/[a-zA-Z0-9\-]+)')

with open(STRINGS_PATH, "r") as ins:
    for line in ins:
        r = re.search (string_kw_re, line)
        if(r and r.groups()):
            string_kws.append(r[1])
            r2 = string_kw_re_lite.findall (r[2])
            for x in r2:
                found_strings.append(x)

print("Found", len(string_kws), "defined strings...")

# grep
proc = subprocess.Popen(
    ["grep",
     "-roh",
     "\:string\/[a-zA-Z0-9\-]\+",
     "--exclude=\strings.cljs",
     SRC_DIR], stdout=subprocess.PIPE)

for line in iter(proc.stdout.readline,b''):
    x = line.rstrip().decode('UTF-8')
    found_strings.append(x)
    if not x in string_kws:
        if not undefined_intro:
            undefined_intro = True
            print("The following strings are used but undefined:")
        print("", x)

for line in string_kws:
    if not line in found_strings:
        if not unused_intro:
            unused_intro = True
            print("The follwing strings are defined but not used:")
        print("", line)

if not unused_intro and not undefined_intro:
    print("All strings are accounted for!")
    sys.exit(0)
else:
    sys.exit(1)
