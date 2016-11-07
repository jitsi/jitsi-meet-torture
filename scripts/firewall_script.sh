#!/usr/bin/env bash

block_port() {
sudo iptables -I INPUT 1 -p udp --dport $1 -j DROP
sudo iptables -I OUTPUT 1 -p udp --sport $1 -j DROP
sudo iptables -I OUTPUT 1 -p tcp --dport 4443 -j DROP
sudo iptables -L
}

unblock_port() {
sudo iptables -D INPUT -p udp --dport $1 -j DROP
sudo iptables -D OUTPUT -p udp --sport $1 -j DROP
sudo iptables -D OUTPUT -p tcp --dport 4443 -j DROP
sudo iptables -L
}

echo "cmd $1 port $2";

case $1 in
    --block-port)
        block_port $2;;
    --unblock-port)
        unblock_port $2;;
    *)
        echo "invalid command: $1";;
esac

