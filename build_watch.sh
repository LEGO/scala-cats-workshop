#!/usr/bin/env bash

set -Eeuo pipefail

if ! [ -x "$(command -v coursier)" ]; then
  echo 'Error: coursier is not installed, fetching...'
  curl -L -o coursier https://git.io/coursier
  chmod +x coursier
fi

coursier launch org.scalameta:mdoc_2.12:1.3.6 org.typelevel:cats-effect_2.12:2.0.0 -- --watch --in slides/ --out slides-out/ &
reveal-md ./slides-out/index.md --watch --preprocessor ./slides-out/preprocess.js --css ./slides-out/css/custom.css