#!/bin/sh -e

if [ $# -eq 0 ]; then
    echo "Usage: $0 [download location]"
    exit 1
fi

exec wget -r --no-parent -nH -P $1 --cut-dirs=2 --reject "index.html*" http://jitsi.org/psnr/output/
