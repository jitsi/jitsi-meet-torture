#!/bin/sh

if [ -n "$DEBUG" ]; then
  set -x
fi

usage() {
  echo "Usage: $0 [--conferences=CONFERENCES] [--participants=PARTICIPANTS] [--senders=SENDERS] [--audio-senders=AUDIO_SENDERS] [--senders-per-node=SENDERS_PER_NODE] [--receivers-per-node=RECEIVERS_PER_NODE] [--duration=DURATION (s)] [--join-delay=JOIN_DELAY (ms)] [--room-name-prefix=ROOM_NAME_PREFIX] [--hub-url=HUB_URL] [--instance-url=INSTANCE_URL] [--regions=REGIONS] [--use-node-types] [--use-load-test] [--max-disrupted-bridges-pct=PCT] [--debug]" >&2
  exit 1
}

case $1 in
  --*)
    # new arg parsing code that includes default values for the different options.
    for arg in "$@"; do
      optname=`echo $arg | cut -d= -f1`
      optvalue=`echo $arg | cut -s -d= -f2`
      case $optname in
        --conferences) CONFERENCES=$optvalue;;
        --allow-insecure-certs) ALLOW_INSECURE_CERTS=$optvalue;;
        --participants) PARTICIPANTS=$optvalue;;
        --senders) SENDERS=$optvalue;;
        --audio-senders) AUDIO_SENDERS=$optvalue;;
        --senders-per-node) SENDERS_PER_NODE=$optvalue; USE_NODE_TYPES=true;;
        --receivers-per-node) RECEIVERS_PER_NODE=$optvalue; USE_NODE_TYPES=true;;
        --duration) DURATION=$optvalue;;
        --join-delay) JOIN_DELAY=$optvalue;;
        --room-name-prefix) ROOM_NAME_PREFIX=$optvalue;;
        --hub-url) HUB_URL=$optvalue;;
        --instance-url) INSTANCE_URL=$optvalue;;
        --regions) REGIONS=$optvalue;;
        --use-node-types) if [ -n "$optvalue" ]; then USE_NODE_TYPES=$optvalue; else USE_NODE_TYPES=true; fi;;
        --use-load-test) if [ -n "$optvalue" ]; then USE_LOAD_TEST=$optvalue; else USE_LOAD_TEST=true; fi;;
        --max-disrupted-bribges-pct) MAX_DISRUPTED_BRIDGES_PCT=$optvalue;;
        --debug) set -x;;
        *)
          usage
          ;;
      esac
    done

    if [ -z "$ALLOW_INSECURE_CERTS" ]; then
      ALLOW_INSECURE_CERTS=false
    fi

    if [ -z "$CONFERENCES" ]; then
      CONFERENCES=1
    fi

    if [ -z "$PARTICIPANTS" ]; then
      PARTICIPANTS=3
    fi

    if [ -z "$SENDERS" ]; then
      SENDERS=$PARTICIPANTS
    fi

    if [ -z "$AUDIO_SENDERS" ]; then
      AUDIO_SENDERS=$PARTICIPANTS
    fi

    if [ -z "$SENDERS_PER_NODE" ]; then
      SENDERS_PER_NODE=1
    fi

    if [ -z "$RECEIVERS_PER_NODE" ]; then
      RECEIVERS_PER_NODE=1
    fi

    if [ -z "$DURATION" ]; then
      DURATION=60
    fi

    if [ -z "$JOIN_DELAY" ]; then
      JOIN_DELAY=0
    fi

    if [ -z "$ROOM_NAME_PREFIX" ]; then
      ROOM_NAME_PREFIX='loadtest'
    fi

    if [ -z "$HUB_URL" ]; then
      HUB_URL='http://localhost:4444/wd/hub'
    fi

    if [ -z "$INSTANCE_URL" ]; then
      INSTANCE_URL='https://meet.jit.si'
    fi

    if [ -z "$USE_NODE_TYPES" ]; then
      USE_NODE_TYPES=false
    fi

    if [ -z "$USE_LOAD_TEST" ]; then
      USE_LOAD_TEST=false
    fi

    ;;
  *)
    # backwords compatible arg parsing so as to not break existing scripts that
    # use malleus.

    # Runs a test with:
    #  $1 conferences, with
    #  $2 participants each, with
    #  $3 of them sending video and the rest having video muted.
    #  $4 seconds the participants will stay in the conference.
    #  $5 will be the conference room name prefix.
    #  $6 will be the selenium grid address (for example http://grid.example.com:4444/wd/hub)

    if [ $# != 6 ]; then
      echo "Usage: $0 CONFERENCES PARTICIPANTS SENDERS DURATION ROOM_NAME_PREFIX HUB_URL" >&2
      exit 1
    fi

    CONFERENCES=$1
    PARTICIPANTS=$2
    SENDERS=$3
    DURATION=$4
    ROOM_NAME_PREFIX=$5
    HUB_URL=$6
    INSTANCE_URL='https://meet.jit.si'
    ;;
esac

# This names nodes as being a "malleusSender" or "malleusReceiver" (using the Selenium Grid
# "applicationName" parameter).  This lets us run multiple browsers on a Selenium Grid endpoint,
# scaled for the number of browsers the endpoint can handle performing the requested action.
if [ "$USE_NODE_TYPES" = "true" ]
then
    "$(dirname $0)"/mutatenodes.sh --hub-url="$HUB_URL" --num-senders="$SENDERS" \
                   --send-node-max-sessions="$SENDERS_PER_NODE" --recv-node-max-sessions="$RECEIVERS_PER_NODE"
    err=$?
    if [ $err -ne 0 ]
    then
        echo "Not running malleus: mutatenodes.sh failed with status $err"
        exit $err
    fi
fi

if [ -z "$TESTS_TO_RUN" ]; then
  TESTS_TO_RUN=MalleusJitsificus
fi

mvn \
-Dthreadcount=1 \
-Dorg.jitsi.malleus.conferences=$CONFERENCES \
-Dorg.jitsi.malleus.max_disrupted_bridges_pct=$MAX_DISRUPTED_BRIDGES_PCT \
-Dorg.jitsi.malleus.participants=$PARTICIPANTS \
-Dorg.jitsi.malleus.senders=$SENDERS \
-Dorg.jitsi.malleus.audio_senders=$AUDIO_SENDERS \
-Dorg.jitsi.malleus.duration=$DURATION \
-Dorg.jitsi.malleus.join_delay=$JOIN_DELAY \
-Dorg.jitsi.malleus.room_name_prefix=$ROOM_NAME_PREFIX \
-Dorg.jitsi.malleus.regions=$REGIONS \
-Dorg.jitsi.malleus.use_node_types=$USE_NODE_TYPES \
-Dorg.jitsi.meet.test.util.blip_script=$BLIP_SCRIPT \
-Dorg.jitsi.malleus.use_load_test=$USE_LOAD_TEST \
-Dremote.address=$HUB_URL \
-DallowInsecureCerts=$ALLOW_INSECURE_CERTS \
-Djitsi-meet.tests.toRun=$TESTS_TO_RUN \
-Dwdm.gitHubTokenName=jitsi-jenkins \
-Dremote.resource.path=/usr/share/jitsi-meet-torture \
-Djitsi-meet.instance.url=$INSTANCE_URL \
-Djitsi-meet.isRemote=true \
-Dchrome.disable.nosanbox=true \
test
