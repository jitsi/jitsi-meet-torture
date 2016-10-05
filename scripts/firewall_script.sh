#!/usr/bin/env bash

disable_port() {
clear_fw
sudo iptables -A INPUT -p udp --dport $1 -j DROP
sudo iptables -A OUTPUT -p udp --sport $1 -j DROP
sudo iptables -A OUTPUT -p tcp --dport 4443 -j DROP
sudo iptables -L
}

clear_fw() {
sudo iptables -F INPUT
sudo iptables -F OUTPUT
sudo iptables -L
}

echo "cmd $1 port $2";

case $1 in
    --block-port)
        disable_port $2;;
    --clear-rules)
        clear_fw;;
    *)
        echo "invalid command: $1";;
esac

