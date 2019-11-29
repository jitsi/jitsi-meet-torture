#!/bin/bash

GRID=$1
if [ -z "$GRID" ]
then
  echo "Usage: $0 GRID_NAME NUM_SENDERS" 1>&2
  exit 1
fi

NUM_SENDERS=$2
if [ -z "$NUM_SENDERS" ]
then
  echo "Usage: $0 GRID_NAME NUM_SENDERS" 1>&2
  exit 1
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
  # instance_type=$($SSH wget -O- http://169.254.169.254/latest/dynamic/instance-identity/document | jq .instanceType)
  # app_name=${instance_type_to_app[instance_type]}
  node_config=$(mktemp)
  if [ $NUM_SENDERS -gt 0 ]
  then
    # mutate the node into a sender.
    max_instances=1
    max_sessions=1
    application_name='ljmSender'
    NUM_SENDERS=$(echo "$NUM_SENDERS-1" | bc)
  else
    # mutate the node into a receiver.
    max_instances=20
    max_sessions=20
    application_name='ljmReceiver'
  fi
  $SSH $node cat /opt/selenium_grid_extras/node_5555.json \
      | jq ".capabilities[0].maxInstances = $max_instances | .capabilities[0].applicationName = \"$application_name\" | .maxSessions = $max_sessions" > $node_config

  $SCP $node_config $node:node_5555.json
  $SSH $node "sudo mv node_5555.json /opt/selenium_grid_extras/; sudo chown selenium:selenium /opt/selenium_grid_extras/node_5555.json; sudo chmod 644 /opt/selenium_grid_extras/node_5555.json; sudo systemctl restart selenium-grid-extras-node"
  rm $node_config
}

for node in `aws ec2 describe-instances --region 'us-west-2' --filters Name=tag:Environment,Values=prod Name=tag:grid-role,Values=node Name=tag:grid,Values=$GRID --query "Reservations[].Instances[][PrivateIpAddress]" --output text`
do
  mutate_node
done
