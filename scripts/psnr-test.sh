#!/bin/sh
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
OUTPUT_FRAME="$1"
INPUT_FRAME_DIR="$2"
RESIZED_FRAME_DIR="$3"
QR_CODE_DECODER="zbarimg"


PATH_TO_QR_DECODER=$(which $QR_CODE_DECODER)
if [ ! -x "$PATH_TO_QR_DECODER" ]
then
  echo "Unable to find the qr decoder executable: $QR_CODE_DECODER. please make sure it's in your path"
  exit 1
fi

FRAME_NUMBER=$($PATH_TO_QR_DECODER -q "$OUTPUT_FRAME" | tr -d ["QR\-Code:"])
if [ "$FRAME_NUMBER" = "" ]
then
    FRAME_NUMBER="-1"
fi

PSNR=-1
if [ "$FRAME_NUMBER" != "-1" ]
then
    INPUT_FRAME=$INPUT_FRAME_DIR/$FRAME_NUMBER.png
    dimensions=$(identify -format "%wx%h" "$OUTPUT_FRAME")
    if [ ${dimensions} != "1280x720" ]
    then
        mkdir -p $RESIZED_FRAME_DIR
        # Newer versions of compare can do resizing automatically.
        RESIZED_FRAME="$RESIZED_FRAME_DIR/$FRAME_NUMBER.png"
        convert "$OUTPUT_FRAME" -resize 1280x720 $RESIZED_FRAME
        PSNR=$(compare "$RESIZED_FRAME_DIR/$FRAME_NUMBER.png" "$INPUT_FRAME" -metric PSNR /tmp/psnr_diff.png 2>&1 || true)
        rm -rf $RESIZED_FRAME
    else
        PSNR=$(compare "$OUTPUT_FRAME" "$INPUT_FRAME" -metric PSNR /tmp/psnr_diff.png 2>&1 || true)
    fi
fi
echo $FRAME_NUMBER $PSNR
