#!/bin/sh -e
#
# Copyright @ 2015 Atlassian Pty Ltd
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

SCRIPT_DIR=$(dirname $0)
OUTPUT_FRAME_DIR=$1
INPUT_FRAME_DIR=$2
RESIZED_FRAME_DIR=$3

for OUTPUT_FRAME in $OUTPUT_FRAME_DIR/*.png
do
    FRAME_NUMBER=$(java -jar $SCRIPT_DIR/javase-3.2.2-SNAPSHOT-jar-with-dependencies.jar $OUTPUT_FRAME |head -3|tail -1)
    if [ "$FRAME_NUMBER" = "" ]
    then
        FRAME_NUMBER="-1"
    fi

    PSNR=-1
    if [ "$FRAME_NUMBER" != "-1" ]
    then
        INPUT_FRAME=$INPUT_FRAME_DIR/$FRAME_NUMBER.png
        dimensions=$(identify -format "%wx%h" $OUTPUT_FRAME)
        if [ ${dimensions} != "1280x720" ]
        then
            mkdir -p $RESIZED_FRAME_DIR
            convert "$OUTPUT_FRAME" -resize 1280x720 "$RESIZED_FRAME_DIR/$FRAME_NUMBER.png"
            PSNR=$(compare "$RESIZED_FRAME_DIR/$FRAME_NUMBER.png" "$INPUT_FRAME" -metric PSNR /tmp/psnr_diff.png 2>&1)
        else
            PSNR=$(compare "$OUTPUT_FRAME" "$INPUT_FRAME" -metric PSNR /tmp/psnr_diff.png 2>&1)
        fi
   fi
    echo $FRAME_NUMBER $PSNR
done
