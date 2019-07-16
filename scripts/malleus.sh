#!/bin/sh

# Runs a test with:
#  $1 conferences, with
#  $2 participants each, with
#  $3 of them sending video and the rest having video muted.
#  $4 seconds the participants will stay in the conference.
#  $5 will be the conference room name prefix.
#  $6 will be the selenium grid address (for example http://grid.example.com:4444/wd/hub)

mvn \
-Dthreadcount=1 \
-Dorg.jitsi.malleus.conferences=$1 \
-Dorg.jitsi.malleus.participants=$2 \
-Dorg.jitsi.malleus.senders=$3 \
-Dorg.jitsi.malleus.duration=$4 \
-Dorg.jitsi.malleus.room_name_prefix=$5 \
-Dremote.address=$6 \
-Djitsi-meet.tests.toRun=MalleusJitsificus \
-Dwdm.gitHubTokenName=jitsi-jenkins \
-Dremote.resource.path=/usr/share/jitsi-meet-torture \
-Djitsi-meet.instance.url=https://meet.jit.si \
-Djitsi-meet.isRemote=true \
test
