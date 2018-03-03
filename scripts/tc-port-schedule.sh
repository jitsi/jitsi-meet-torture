#!/bin/bash -e
#
# Copyright @ 2017 Atlassian Pty Ltd
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
# description: This script uses HTB(8) to schedule the egress bitrate towards a
# specific port.
#
# author: George Politis

readonly DEBUG=${DEBUG:-false}

# Name of the traffic control command.
if ! [ `id -u` = 0 ]; then
    # TC requires root priviledges to run.
    readonly TC="sudo /sbin/tc"
else
    readonly TC=/sbin/tc
fi

# The network interface we're planning on limiting bandwidth.
readonly DEV=`ip addr | grep 2: | cut -d' ' -f2 | cut -d: -f1`

# Filter options for limiting the intended interface.
readonly DST_PORT=${1}
shift

initialized=false

# Prints TC(8) debugging information.
tc_internals() {
    ${TC} -s -d qdisc show dev ${DEV}
    ${TC} -s -d class show dev ${DEV} parent 1:
    ${TC} -s -d filter show dev ${DEV} parent 1:
}

# Resets the TC(8) state.
finish() {
    if ${initialized}; then

        if ${DEBUG}; then
            echo 'clear everything'
        fi

        ${TC} qdisc del dev ${DEV} root
        initialized=false

        if ${DEBUG}; then
            tc_internals
        fi
    fi
}
# https://unix.stackexchange.com/questions/57940/trap-int-term-exit-really-necessary
trap finish INT TERM

if ${DEBUG}; then
    tc_internals
fi

for i in $*; do
    IFS=',' read rate duration <<< "${i}"

    if ${DEBUG}; then
        echo ts=`date +%s`,dev=${DEV},dport=${DST_PORT},rate=${rate},duration=${duration}
    fi

    if ${initialized}; then
        ${TC} class change dev ${DEV} parent 1: classid 1:3 htb rate ${rate}
    else
        ${TC} qdisc add dev ${DEV} root handle 1: htb default 1
        initialized=true
        ${TC} class add dev ${DEV} parent 1: classid 1:3 htb rate ${rate}
        ${TC} filter add dev ${DEV} protocol ip parent 1:0 prio 1 u32 \
            match ip dport ${DST_PORT} 0xffff classid 1:3
    fi

    if ${DEBUG}; then
        tc_internals
    fi

    if [ "${duration}" -gt 0 ]; then
        if ${DEBUG}; then
            echo "Waiting for ${duration} seconds."
        fi
        sleep ${duration}
    fi
done

finish
