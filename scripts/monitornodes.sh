#!/bin/bash

# Monitor load average on nodes in a Selenium grid.
# Runs forever, Control-C to quit.

set -e

if [ $# -gt 1 ]; then
    HUB_URL=$1
else
    HUB_URL="http://localhost:4444/wd/hub"
fi

HUB_CONSOLE_URL="$(echo "$HUB_URL" | sed 's,/wd/hub,/grid/console,')"

if [ -z "$JITSI_SSH_CONFIG" ]
then
    JITSI_SSH_CONFIG="$HOME/.ssh/config.d/jitsi64.config"
fi

if [ -z "$SSH" ]
then
    if [ -r "$JITSI_SSH_CONFIG" ]
    then
        SSH="ssh -F $JITSI_SSH_CONFIG"
    else
        SSH="ssh"
    fi
fi

NODES="$(curl -sS "$HUB_CONSOLE_URL" | grep -o 'host: [^<]*' | awk '{print $2}')"

while true
do
    for node in $NODES
    do
        printf "%s:\t" $node
        $SSH $node uptime
    done
    echo
    sleep 1
done
