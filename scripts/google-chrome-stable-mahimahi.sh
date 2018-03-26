#!/bin/sh
#
# Copyright @ 2018 Atlassian Pty Ltd
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# description: wraps chrome and enables it to run connected to an emulated link
# with user-specified packet-delivery schedule. The emulation is performed using
# mm-link(1) which is part of the mahimahi(1) project.
#
# The two additional parameters that this script adds are --uplink and
# --downlink which are packet delivery trace files for the uplink direction and
# downlink direction respectively.
#
# This script is designed to run on a selenium grid node, so it handles the
# case where a remote-debugging-port is specified.
#
# author: George Politis
# requires: mahimahi, socat

CHROME=/usr/bin/google-chrome-stable

finish() {
  # kill own descendants
  pkill -P $$
}
trap finish INT TERM

for arg in "$@"; do
  name=$(echo $arg|cut -d'=' -f1)
  value=$(echo $arg|cut -d'=' -f2)
  case $name in
    --uplink)
      readonly UPLINK="${value}"
      ;;
    --downlink)
      readonly DOWNLINK="${value}"
      ;;
    --remote-debugging-port)
      readonly DEBUG_PORT="${value}"
      ;;
  esac
done

if [ -f "$UPLINK" -a -f "$DOWNLINK" ]; then
  if [ ! -z "$DEBUG_PORT" ]; then

    # open a tunnel to forward localhost:debug_port -> chrome-ip:debug_port
    socat tcp-listen:$DEBUG_PORT,reuseaddr,fork tcp:100.64.0.2:$DEBUG_PORT &

    # XXX we need to run Chrome headless to convince the DevTools to bind to
    # an arbitrary port.
    mm-link "$UPLINK" "$DOWNLINK" -- "$CHROME" --headless --remote-debugging-address=0.0.0.0 "$@" &
    wait $!
  else
    exec mm-link "$UPLINK" "$DOWNLINK" -- "$CHROME" "$@"
  fi
else
  exec "$CHROME" "$@"
fi

finish
