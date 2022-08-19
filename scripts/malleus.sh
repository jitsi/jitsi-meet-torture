#!/bin/sh

if [ -n "$DEBUG" ]; then
  set -x
fi

usage() {
  echo "Usage: $0 [--conferences=MALLEUS_CONFERENCES] [--participants=MALLEUS_PARTICIPANTS] [--senders=MALLEUS_SENDERS] [--audio-senders=MALLEUS_AUDIO_SENDERS] [--senders-per-tab=MALLEUS_SENDERS_PER_NODE] [--receivers-per-tab=MALLEUS_RECEIVERS_PER_NODE] [--senders-per-node=MALLEUS_SENDERS_PER_NODE] [--receivers-per-node=MALLEUS_RECEIVERS_PER_NODE] [--duration=MALLEUS_DURATION (s)] [--join-delay=MALLEUS_JOIN_DELAY (ms)] [--room-name-prefix=MALLEUS_ROOM_NAME_PREFIX] [--hub-url=MALLEUS_HUB_URL] [--instance-url=MALLEUS_INSTANCE_URL] [--regions=MALLEUS_REGIONS] [--use-node-types] [--use-load-test] [--max-disrupted-bridges-pct=MALLEUS_MAX_DISRUPTED_BRIDGES_PCT] [--extra-sender-params=EXTRA_SENDER_PARAMS] [--extra-receiver-params=EXTRA_RECEIVER_PARAMS] [--debug] [--switch-speakers] [--use-stage-view] [--headless]" >&2
  exit 1
}

set_defaults() {
    if [ -z "$MALLEUS_ALLOW_INSECURE_CERTS" ]; then
      MALLEUS_ALLOW_INSECURE_CERTS=false
    fi

    if [ -z "$MALLEUS_CONFERENCES" ]; then
      MALLEUS_CONFERENCES=1
    fi

    if [ -z "$MALLEUS_PARTICIPANTS" ]; then
      MALLEUS_PARTICIPANTS=3
    fi

    if [ -z "$MALLEUS_SENDERS" ]; then
      MALLEUS_SENDERS=$MALLEUS_PARTICIPANTS
    fi

    if [ -z "$MALLEUS_AUDIO_SENDERS" ]; then
      MALLEUS_AUDIO_SENDERS=$MALLEUS_PARTICIPANTS
    fi

    if [ -z "$MALLEUS_SENDERS_PER_TAB" ]; then
      MALLEUS_SENDERS_PER_TAB=1
    fi

    if [ -z "$MALLEUS_RECEIVERS_PER_TAB" ]; then
      MALLEUS_RECEIVERS_PER_TAB=1
    fi

    if [ -z "$MALLEUS_SENDERS_PER_NODE" ]; then
      MALLEUS_SENDERS_PER_NODE=1
    fi

    if [ -z "$MALLEUS_RECEIVERS_PER_NODE" ]; then
      MALLEUS_RECEIVERS_PER_NODE=1
    fi

    if [ -z "$MALLEUS_DURATION" ]; then
      MALLEUS_DURATION=60
    fi

    if [ -z "$MALLEUS_JOIN_DELAY" ]; then
      MALLEUS_JOIN_DELAY=0
    fi

    if [ -z "$MALLEUS_ROOM_NAME_PREFIX" ]; then
      MALLEUS_ROOM_NAME_PREFIX='loadtest'
    fi

    if [ -z "$MALLEUS_HUB_URL" ]; then
      MALLEUS_HUB_URL='http://localhost:4444/wd/hub'
    fi

    if [ -z "$MALLEUS_INSTANCE_URL" ]; then
      MALLEUS_INSTANCE_URL='https://meet.jit.si'
    fi

    if [ -z "$MALLEUS_USE_NODE_TYPES" ]; then
      MALLEUS_USE_NODE_TYPES=false
    fi

    if [ -z "$MALLEUS_USE_LOAD_TEST" ]; then
      MALLEUS_USE_LOAD_TEST=false
    fi

    if [ -z "$MALLEUS_MAX_DISRUPTED_BRIDGES_PCT" ]; then
      MALLEUS_MAX_DISRUPTED_BRIDGES_PCT=0
    fi

    if [ -z "$MALLEUS_SWITCH_SPEAKERS" ]; then
      MALLEUS_SWITCH_SPEAKERS=false
    fi

    if [ -z "$MALLEUS_USE_STAGE_VIEW" ]; then
      MALLEUS_USE_STAGE_VIEW=false
    fi

    # Null is a fine default for MALLEUS_EXTRA_SENDER_PARAMS and MALLEUS_EXTRA_RECEIVER_PARAMS
}

case $1 in
  "")
    # no args provided, falling back to environment variables and default values
    set_defaults
    ;;
  --*)
    # new arg parsing code that includes default values for the different options.
    for arg in "$@"; do
      optname=`echo $arg | cut -d= -f1`
      optvalue=`echo $arg | cut -s -d= -f2-`
      case $optname in
        --conferences) MALLEUS_CONFERENCES=$optvalue;;
        --allow-insecure-certs) MALLEUS_ALLOW_INSECURE_CERTS=$optvalue;;
        --participants) MALLEUS_PARTICIPANTS=$optvalue;;
        --senders) MALLEUS_SENDERS=$optvalue;;
        --audio-senders) MALLEUS_AUDIO_SENDERS=$optvalue;;
        --senders-per-tab) MALLEUS_SENDERS_PER_TAB=$optvalue;;
        --receivers-per-tab) MALLEUS_RECEIVERS_PER_TAB=$optvalue;;
        --senders-per-node) MALLEUS_SENDERS_PER_NODE=$optvalue; MALLEUS_USE_NODE_TYPES=true;;
        --receivers-per-node) MALLEUS_RECEIVERS_PER_NODE=$optvalue; MALLEUS_USE_NODE_TYPES=true;;
        --duration) MALLEUS_DURATION=$optvalue;;
        --join-delay) MALLEUS_JOIN_DELAY=$optvalue;;
        --room-name-prefix) MALLEUS_ROOM_NAME_PREFIX=$optvalue;;
        --hub-url) MALLEUS_HUB_URL=$optvalue;;
        --instance-url) MALLEUS_INSTANCE_URL=$optvalue;;
        --regions) MALLEUS_REGIONS=$optvalue;;
        --use-node-types) if [ -n "$optvalue" ]; then MALLEUS_USE_NODE_TYPES=$optvalue; else MALLEUS_USE_NODE_TYPES=true; fi;;
        --use-load-test) if [ -n "$optvalue" ]; then MALLEUS_USE_LOAD_TEST=$optvalue; else MALLEUS_USE_LOAD_TEST=true; fi;;
        --switch-speakers) if [ -n "$optvalue" ]; then MALLEUS_SWITCH_SPEAKERS=$optvalue; else MALLEUS_SWITCH_SPEAKERS=true; fi;;
        --use-stage-view) if [ -n "$optvalue" ]; then MALLEUS_USE_STAGE_VIEW=$optvalue; else MALLEUS_USE_STAGE_VIEW=true; fi;;
        --headless) if [ -n "$optvalue" ]; then MALLEUS_USE_HEADLESS=$optvalue; else MALLEUS_USE_HEADLESS=true; fi;;
        --extra-sender-params) MALLEUS_EXTRA_SENDER_PARAMS=$optvalue;;
        --extra-receiver-params) MALLEUS_EXTRA_RECEIVER_PARAMS=$optvalue;;
        --max-disrupted-bridges-pct) MALLEUS_MAX_DISRUPTED_BRIDGES_PCT=$optvalue;;
        --debug) set -x;;
        *)
          usage
          ;;
      esac
    done

    set_defaults
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
      usage
    fi

    MALLEUS_CONFERENCES=$1
    MALLEUS_PARTICIPANTS=$2
    MALLEUS_SENDERS=$3
    MALLEUS_DURATION=$4
    MALLEUS_ROOM_NAME_PREFIX=$5
    MALLEUS_HUB_URL=$6
    MALLEUS_INSTANCE_URL='https://meet.jit.si'

    set_defaults
    ;;
esac

if [ "$MALLEUS_SENDERS_PER_TAB" -gt 1 -o "$MALLEUS_RECEIVERS_PER_TAB" -gt 1 ] && [ "$MALLEUS_USE_LOAD_TEST" != "true" ]; then
    echo "Senders-per-tab and/or receivers-per-tab parameters require load-test"
    exit 1
fi

# This names nodes as being a "malleusSender" or "malleusReceiver" (using the Selenium Grid
# "applicationName" parameter).  This lets us run multiple browsers on a Selenium Grid endpoint,
# scaled for the number of browsers the endpoint can handle performing the requested action.
if [ "$MALLEUS_USE_NODE_TYPES" = "true" ]
then
    "$(dirname $0)"/mutatenodes.sh --hub-url="$MALLEUS_HUB_URL" --num-senders="$MALLEUS_SENDERS" \
                   --send-node-max-sessions="$MALLEUS_SENDERS_PER_NODE" --recv-node-max-sessions="$MALLEUS_RECEIVERS_PER_NODE"
    err=$?
    if [ $err -ne 0 ]
    then
        echo "Not running malleus: mutatenodes.sh failed with status $err"
        exit $err
    fi
fi

if [ -z "$MALLEUS_TESTS_TO_RUN" ]; then
  MALLEUS_TESTS_TO_RUN=MalleusJitsificus
fi

mvn \
-Dthreadcount=1 \
-Dorg.jitsi.malleus.conferences=$MALLEUS_CONFERENCES \
-Dorg.jitsi.malleus.max_disrupted_bridges_pct=$MALLEUS_MAX_DISRUPTED_BRIDGES_PCT \
-Dorg.jitsi.malleus.participants=$MALLEUS_PARTICIPANTS \
-Dorg.jitsi.malleus.senders=$MALLEUS_SENDERS \
-Dorg.jitsi.malleus.audio_senders=$MALLEUS_AUDIO_SENDERS \
-Dorg.jitsi.malleus.duration=$MALLEUS_DURATION \
-Dorg.jitsi.malleus.join_delay=$MALLEUS_JOIN_DELAY \
-Dorg.jitsi.malleus.room_name_prefix=$MALLEUS_ROOM_NAME_PREFIX \
-Dorg.jitsi.malleus.regions=$MALLEUS_REGIONS \
-Dorg.jitsi.malleus.use_node_types=$MALLEUS_USE_NODE_TYPES \
-Dorg.jitsi.malleus.senders_per_tab=$MALLEUS_SENDERS_PER_TAB \
-Dorg.jitsi.malleus.receivers_per_tab=$MALLEUS_RECEIVERS_PER_TAB \
-Dorg.jitsi.meet.test.util.blip_script=$MALLEUS_BLIP_SCRIPT \
-Dorg.jitsi.malleus.use_load_test=$MALLEUS_USE_LOAD_TEST \
-Dorg.jitsi.malleus.switch_speakers=$MALLEUS_SWITCH_SPEAKERS \
-Dorg.jitsi.malleus.use_stage_view=$MALLEUS_USE_STAGE_VIEW \
-Dorg.jitsi.malleus.enable.headless=$MALLEUS_USE_HEADLESS \
-Dorg.jitsi.malleus.extra_sender_params=$MALLEUS_EXTRA_SENDER_PARAMS \
-Dorg.jitsi.malleus.extra_receiver_params=$MALLEUS_EXTRA_RECEIVER_PARAMS \
-Dremote.address=$MALLEUS_HUB_URL \
-DallowInsecureCerts=$MALLEUS_ALLOW_INSECURE_CERTS \
-Djitsi-meet.tests.toRun=$MALLEUS_TESTS_TO_RUN \
-Dwdm.gitHubTokenName=jitsi-jenkins \
-Dremote.resource.path=/usr/share/jitsi-meet-torture \
-Djitsi-meet.instance.url=$MALLEUS_INSTANCE_URL \
-Djitsi-meet.isRemote=false \
-Dchrome.disable.nosanbox=true \
test
