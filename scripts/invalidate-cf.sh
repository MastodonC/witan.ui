#!/usr/bin/env bash
aws cloudfront create-invalidation --distribution-id ${1:?Error: provide the distribution ID as first argument} --paths /index.html /js/compiled/witan.ui.js /css/style.css
