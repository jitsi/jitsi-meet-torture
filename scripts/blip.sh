#!/bin/sh

# This is a non-functional placeholder script that needs to be replaced to match
# your specific infrastructure. The command line parameters that are necessary
# for the BlipTest to function properly are listed bellow.

usage() {
    echo "Usage: $0 --duration=seconds --max-disrupted-pct=percent [--bridge-ips=IP1[,IP2,...]] [--xmpp-client-port=5222] [--debug]" >&2
    exit 1
}

usage
