# Witan.UI
     _    _ _ _               _   _ _____
    | |  | (_) |             | | | |_   _|
    | |  | |_| |_ __ _ _ __  | | | | | |
    | |/\| | | __/ _` | '_ \ | | | | | |
    \  /\  / | || (_| | | | || |_| |_| |_
     \/  \/|_|\__\__,_|_| |_(_)___/ \___/

## Overview

This is the frontend application that powers Witan, the open city planning tool, using...

[ClojureScript](https://github.com/clojure/clojurescript)
[Venue](https://github.com/mastodonc/venue)
[Om](https://github.com/omcljs/om)
[Sablono](https://github.com/r0man/sablono)
[Figwheel](https://github.com/bhauman/lein-figwheel)
[Secretary](https://github.com/gf3/secretary)
[Schema](https://github.com/Prismatic/schema)
[Datascript](https://github.com/tonsky/datascript)
[Garden](https://github.com/noprompt/garden)

## To Develop

To get an interactive development environment run:

    WITAN_API_URL=http://{host:port} lein figwheel

and open your browser at [localhost:3449](http://localhost:3449/). The
environmental variable `WITAN_API_URL` is used to specify a location for [the API](https://github.com/MastodonC/witan.app).
This will compile and send all changes to the browser without the
need to reload. After the compilation process is complete, you will
get a Browser Connected REPL. An easy way to try it is:

    (js/alert "Am I connected?")

and you should see an alert in the browser window.

In a separate terminal, run:

    lein garden auto

This will update any chances to CSS styles (in the `styles` directory) and figwheel will automatically apply them.

To clean all compiled files:

lein clean

## To Build

To create a production build run:

    ./build_prod.sh

Copyright © 2014 Mastodon C

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.
