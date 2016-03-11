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


# TODO return the worst PSNR value.
for REMOTE_VIDEO_FRAME in /tmp/remoteVideo_*
do
    FRAME_NUMBER=$(zbarimg --quiet $REMOTE_VIDEO_FRAME|cut -d: -f2)
    if [ "$FRAME_NUMBER" != "" ]
    then

      ORIGIN_IMAGE=resources/psnr/output/stamped/stamped-FourPeople_1280x720_60-$FRAME_NUMBER.png

        # Test quality/success of conversion by looking at PSNR
        # TODO We might want to avoid resizing the video
        PSNR=$(convert "$REMOTE_VIDEO_FRAME" -resize 1280x720\! "$ORIGIN_IMAGE" -metric PSNR -format "%[distortion]" -compare info:)
        echo DEBUG: PSNR=$PSNR

        # PSNR above 20 is pretty indicative of good similarity - use "bc" as shell doesn't do floats
        if [ $(echo "$PSNR>20" | bc) -eq 1 ]; then
           echo $ORIGIN_IMAGE looks good
        else
           echo $ORIGIN_IMAGE something wrong
        fi
    else
        echo something went really wrong!
    fi
    
done
