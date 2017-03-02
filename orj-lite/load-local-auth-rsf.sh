#!/bin/sh

set -eu

curl -v -H 'Content-Type: application/uk-gov-rsf' --data-binary @local-authorities-10.rsf http://localhost:8080/load-rsf
