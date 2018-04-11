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
# specific port. This script requires HTB(8) to be configured on the host
# machine with: tc qdisc add dev ${DEV} root htb default 1
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

qdiscid=
flowid=

# Prints TC(8) debugging information.
tc_internals() {
    for i in {qdisc,class,filter}; do
        ${TC} -s -d ${i} show dev ${DEV}
    done
}

# Resets the HTB(8) state.
finish() {
    if [ -n "${qdiscid}" -a -n "${flowid}" ]; then
        local filterid=$(tc filter show dev ${DEV}|grep ${qdiscid}:${flowid}|cut -d' ' -f10)
    fi

    if [ -n "${filterid}" ]; then
        ${TC} filter del dev ${DEV} parent ${qdiscid}: \
            handle ${filterid} prio 1 protocol ip u32
    fi

    if [ -n "${flowid}" ]; then
        ${TC} class del dev ${DEV} classid ${qdiscid}:${flowid}
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

    if [ -n "${flowid}" ]; then
        ${TC} class change dev ${DEV} parent ${qdiscid}: \
            classid ${qdiscid}:${flowid} htb rate ${rate}
    else
        qdiscid=$(tc qdisc show dev ${DEV}|grep 'qdisc htb'|cut -d' ' -f3|cut -d':' -f1)
        flowid=3
        while ! ${TC} class add dev ${DEV} parent ${qdiscid}: classid ${qdiscid}:${flowid} htb rate ${rate}
        do
            flowid=$((flowid+1))
        done

        ${TC} filter add dev ${DEV} protocol ip parent ${qdiscid}: prio 1 u32 \
            match ip dport ${DST_PORT} 0xffff classid ${qdiscid}:${flowid}
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
