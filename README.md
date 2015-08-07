# Witan.UI

Styles and Pattern Library for the Witan project.

## Overview

Clojure and ClojureScript are the languages of choice for Witan and so we decided to extend this to our styling as well. This project uses [garden](https://github.com/noprompt/garden) to define CSS styles which are automatically recompiled and presented in an Om application.
## Setup

To get an interactive development environment run:

    lein figwheel

and open your browser at [localhost:3449](http://localhost:3449/).
This will auto compile and send all changes to the browser without the
need to reload. After the compilation process is complete, you will
get a Browser Connected REPL. An easy way to try it is:

    (js/alert "Am I connected?")

and you should see an alert in the browser window.

To clean all compiled files:

    lein clean

To create a production build run:

    lein cljsbuild once min

And open your browser in `resources/public/index.html`. You will not
get live reloading, nor a REPL.

## CSS

CSS in this project uses [garden](https://github.com/noprompt/garden).  
To build CSS, run:

    lein garden <once|auto>

Auto-mode will watch for changes in the code and recompile on the fly.

## License

Copyright © 2014 Mastodon C

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.
