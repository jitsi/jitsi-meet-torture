#!/bin/bash

set -e

if [ -n "$DEBUG" ]; then
  set -x
fi

SEND_APP_NAME='malleusSender'
RECV_APP_NAME='malleusReceiver'

usage() {
    echo "Usage: $0 --hub-url=HUB_URL --num-senders=NUM_SENDERS --send-node-max-sessions=SEND_NODE_MAX_SESSIONS --recv-node-max-sessions=RECV_NODE_MAX_SESSIONS" >&2
    exit 1
}

# new arg parsing code that includes default values for the different options.
for arg in "$@"; do
    optname=`echo $arg | cut -d= -f1`
    optvalue=`echo $arg | cut -d= -f2`
    case $optname in
        --hub-url) HUB_URL=$optvalue;;
        --num-senders) NUM_SENDERS=$optvalue;;
        --send-node-max-sessions) SEND_NODE_MAX_SESSIONS=$optvalue;;
        --recv-node-max-sessions) RECV_NODE_MAX_SESSIONS=$optvalue;;
        --debug) set -x;;
        *)
            usage
            ;;
    esac
done

if [ -z "$HUB_URL" -o -z "$NUM_SENDERS" -o -z "$SEND_NODE_MAX_SESSIONS" -o -z "$RECV_NODE_MAX_SESSIONS" ]
then
    usage
fi

if [ -z "$JITSI_SSH_CONFIG" ]
then
    JITSI_SSH_CONFIG="$HOME/.ssh/config.d/jitsi64.config"
fi

if [ -z "$SSH" ]
then
    if [ -r "$JITSI_SSH_CONFIG" ]
    then
        SSH="ssh -o StrictHostKeyChecking=no -F $JITSI_SSH_CONFIG"
    else
        SSH="ssh -o StrictHostKeyChecking=no"
    fi
fi

NODE_FAILURE_LOG=$(mktemp)

mutate_node() {
    if [ $1 -gt 0 ]
    then
        # mutate the node into a sender.
        max_instances=$SEND_NODE_MAX_SESSIONS
        max_session=$SEND_NODE_MAX_SESSIONS
        application_name=$SEND_APP_NAME
    else
        # mutate the node into a receiver.
        max_instances=$RECV_NODE_MAX_SESSIONS
        max_session=$RECV_NODE_MAX_SESSIONS
        application_name=$RECV_APP_NAME
    fi

    node_config_orig=$(mktemp)
    node_config=$(mktemp)

    # Make sure we can reach $2
    set +e
    $SSH $2 true
    err=$?
    set -e
    if [ $err -ne 0 ]
    then
        echo "Can't reach $2: ssh returned status $err" 1>&2
        echo -n "F" >> $NODE_FAILURE_LOG
        exit 1
    fi

    $SSH $2 cat /opt/selenium_grid_extras/node_5555.json > $node_config_orig
    jq ".capabilities[0].maxInstances = $max_instances | .capabilities[0].applicationName = \"$application_name\" | .maxSession = $max_session" $node_config_orig > $node_config

    $SSH $2 "cat > node_5555.json" < $node_config
    $SSH $2 "sudo mv node_5555.json /opt/selenium_grid_extras/; sudo chown selenium:selenium /opt/selenium_grid_extras/node_5555.json; sudo chmod 644 /opt/selenium_grid_extras/node_5555.json; sudo systemctl restart selenium-grid-extras-node"
    rm $node_config_orig $node_config
}

HUB_CONSOLE_URL="$(echo "$HUB_URL" | sed 's,/wd/hub,/grid/console,')"

NODES="$(curl -sS "$HUB_CONSOLE_URL" | grep -o 'host: [^<]*' | awk '{print $2}')"

for node in $NODES
do
    mutate_node $NUM_SENDERS $node &
    NUM_SENDERS=$(($NUM_SENDERS-$SEND_NODE_MAX_SESSIONS))
done

wait

NUM_FAILURES=$(wc -c < $NODE_FAILURE_LOG)
rm $NODE_FAILURE_LOG

if [ $NUM_FAILURES -ne 0 ]
then
    echo "$0 failed: $NUM_FAILURES nodes could not be mutated"
    exit 1
fi

exit 0
