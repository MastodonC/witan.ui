# Witan.UI
     _    _ _ _               _   _ _____
    | |  | (_) |             | | | |_   _|
    | |  | |_| |_ __ _ _ __  | | | | | |
    | |/\| | | __/ _` | '_ \ | | | | | |
    \  /\  / | || (_| | | | || |_| |_| |_
     \/  \/|_|\__\__,_|_| |_(_)___/ \___/

## Overview

This is the frontend application that powers Witan, the open city planning tool.

## To Develop

To get an interactive development environment run:

    WITAN_API_URL=http://{host:port} lein figwheel

and open your browser at [localhost:3449](http://localhost:3449/). The
environmental variable `WITAN_API_URL` is used to specify a location for [the API](https://github.com/MastodonC/witan.app).
This will auto compile and send all changes to the browser without the
need to reload. After the compilation process is complete, you will
get a Browser Connected REPL. An easy way to try it is:

    (js/alert "Am I connected?")

and you should see an alert in the browser window.

In a separate terminal, run:

    lein garden auto

This will update any chances to CSS styles (in the `styles` directory) and figwheel will automatically apply them.

To clean all compiled files:

lein clean

## A Full REPL Experience

```
Cider -> Figwheel -> Piggiback -> CLJS -> JSload
```

A `.dirs-local.el` file is used to set the cljs repl to figwheel.

Before starting a repl make sure to set the env vars.

```
(setenv "WITAN-API-URL" "staging-api.witanforcities.com")
(setenv "WITAN-API-SECURE" "True")
```

Now you can jack into the app with `cider-jack-in-clojurescript`.
That provides two REPLS one for Clojure and one for Clojurescript.
And allows source buffer evaluation and result return. Tasty!

The env vars above are baked into the build so you can either do a
`C-x C-e` on the `(def config...` in `witan.ui.data` to force the
configuration *OR* before launching the REPL's do a `lein clean` to
clear things down.

## To Build

To create a production build run:

    ./build_prod.sh

Copyright © 2014 Mastodon C

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.
