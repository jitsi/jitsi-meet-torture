#!/bin/sh

SSH="ssh -F $HOME/.ssh/config.d/jitsi64.config"

fix_node() {
  if ! $SSH $node unzip -l /tmp/webdriver/3.141.59.jar > /dev/null 2>&1; then
    echo "$node is broken. Worry not, however! I'll fix it for ya."
    $SSH $node "sudo wget https://bit.ly/2TlkRyu -O /tmp/webdriver/3.141.59.jar; sudo chown selenium:selenium /tmp/webdriver/3.141.59.jar; sudo systemctl restart selenium-grid-extras-node"
  else
    echo "$node looks good."
  fi
}

if [ -z "$1" ]; then
  GRID=perf
  echo "Querying $region..." 1>&2
  for node in `aws ec2 describe-instances --region 'us-west-2' --filters Name=tag:Environment,Values=prod Name=tag:grid-role,Values=node Name=tag:grid,Values=$GRID --query "Reservations[].Instances[][PrivateIpAddress]" --output text`; do
    fix_node
  done
else
  node=$1
  fix_node
fi
