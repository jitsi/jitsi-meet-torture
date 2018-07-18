#!/bin/sh

# Runs a test with:
#  $1 conferences, with
#  $2 participants each, with
#  $3 of them sending video and the rest having video muted.

mvn \
-Dthreadcount=1 \
-Dmulti.conf.timeout=600 \
-Dmulti.conf.num.conferences=$1 \
-Dmulti.conf.num.participants=$2 \
-Dmulti.conf.num.senders=$3 \
-Djitsi-meet.tests.toRun=MalleusJitsificus \
-Dremote.address=http://grid.example.com:4444/wd/hub \
-Dwdm.gitHubTokenName=jitsi-jenkins \
-Dremote.resource.path=/usr/share/jitsi-meet-torture \
-Djitsi-meet.instance.url=https://beta.meet.jit.si \
-Dweb.participant1.isRemote=true \
-Dweb.participant2.isRemote=true \
-Dweb.participant3.isRemote=true \
-Dweb.participant4.isRemote=true \
-Dweb.participant5.isRemote=true \
-Dweb.participant6.isRemote=true \
-Dweb.participant7.isRemote=true \
-Dweb.participant8.isRemote=true \
-Dweb.participant9.isRemote=true \
-Dweb.participant10.isRemote=true \
-Dweb.participant11.isRemote=true \
-Dweb.participant12.isRemote=true \
-Dweb.participant13.isRemote=true \
-Dweb.participant14.isRemote=true \
-Dweb.participant15.isRemote=true \
-Dweb.participant16.isRemote=true \
-Dweb.participant17.isRemote=true \
-Dweb.participant18.isRemote=true \
-Dweb.participant19.isRemote=true \
-Dweb.participant20.isRemote=true \
test
