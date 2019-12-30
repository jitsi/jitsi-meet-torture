#!/bin/sh

GRID=$1
if [ -z "$GRID" ]
then
  echo "Usage: $0 GRID_NAME" 1>&2
  exit 1
fi

if [ -z "$JITSI_SSH_CONFIG" ]
then
  JITSI_SSH_CONFIG="$HOME/.ssh/config.d/jitsi64.config"
fi

SSH="ssh -F $JITSI_SSH_CONFIG"

fix_node() {
  if ! $SSH $1 unzip -l /tmp/webdriver/3.141.59.jar > /dev/null 2>&1; then
    echo "$1 is broken. Worry not, however! I'll fix it for ya."
    $SSH $1 "sudo wget https://bit.ly/2TlkRyu -O /tmp/webdriver/3.141.59.jar; sudo chown selenium:selenium /tmp/webdriver/3.141.59.jar; sudo systemctl restart selenium-grid-extras-node"
  else
    echo "$1 looks good."
  fi
}

echo "Querying $region..." 1>&2
for node in `aws ec2 describe-instances --region 'us-west-2' --filters Name=tag:Environment,Values=prod Name=tag:grid-role,Values=node Name=tag:grid,Values=$GRID --query "Reservations[].Instances[][PrivateIpAddress]" --output text`; do
  if [ $node != 'None' ]
  then
    fix_node $node
  fi
done

wait
