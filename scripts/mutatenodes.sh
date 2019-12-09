#!/bin/bash

SEND_APP_NAME='ljmSender'
RECV_APP_NAME='ljmReceiver'

usage() {
  echo 'Usage: $0 --grid-name=GRID_NAME --num-senders=NUM_SENDERS --send-node-max-sessions=SEND_NODE_MAX_SESSIONS --recv-node-max-sessions=RECV_NODE_MAX_SESSIONS' >&2
  exit 1
}

# new arg parsing code that includes default values for the different options.
for arg in "$@"; do
  optname=`echo $arg | cut -d= -f1`
  optvalue=`echo $arg | cut -d= -f2`
  case $optname in
    --grid-name) GRID=$optvalue;;
    --num-senders) NUM_SENDERS=$optvalue;;
    --send-node-max-sessions) SEND_NODE_MAX_SESSIONS=$optvalue;;
    --recv-node-max-sessions) RECV_NODE_MAX_SESSIONS=$optvalue;;
    *)
      usage
      ;;
  esac
done


if [ -z "$GRID" -o -z "$NUM_SENDERS" -o -z "$SEND_NODE_MAX_SESSIONS" -o -z "$RECV_NODE_MAX_SESSIONS" ]
then
  usage
fi

if [ -z "$SSH" ]
then
  SSH="ssh -F $HOME/.ssh/config.d/jitsi64.config"
fi

if [ -z "$SCP" ]
then
  SCP="scp -F $HOME/.ssh/config.d/jitsi64.config"
fi

mutate_node() {
  if [ $node == "None" ]
  then
    return
  fi

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

  node_config=$(mktemp)
  $SSH $node cat /opt/selenium_grid_extras/node_5555.json \
      | jq ".capabilities[0].maxInstances = $max_instances | .capabilities[0].applicationName = \"$application_name\" | .maxSession = $max_session" > $node_config

  $SCP $node_config $node:node_5555.json
  $SSH $node "sudo mv node_5555.json /opt/selenium_grid_extras/; sudo chown selenium:selenium /opt/selenium_grid_extras/node_5555.json; sudo chmod 644 /opt/selenium_grid_extras/node_5555.json; sudo systemctl restart selenium-grid-extras-node"
  rm $node_config
}

for node in `aws ec2 describe-instances --region 'us-west-2' --filters Name=tag:Environment,Values=prod Name=tag:grid-role,Values=node Name=tag:grid,Values=$GRID --query "Reservations[].Instances[][PrivateIpAddress]" --output text`
do
  mutate_node $NUM_SENDERS
  NUM_SENDERS=$(echo "$NUM_SENDERS-1" | bc)
done

wait