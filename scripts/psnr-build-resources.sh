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

# This script requires ffmpeg and qrencode. You can grab some test
# sequences from here: https://media.xiph.org/video/derf/y4m/

VIDEO_SZ=1280x720
VIDEO_FPS=30
VIDEO_FMT=yuv4mpegpipe 
VIDEO_PIXEL_FMT=yuv420p
VIDEO_IN_FILE="$1"
VIDEO_IN_FILE_BASENAME=$(basename "$VIDEO_IN_FILE")
VIDEO_OUT_FILE=output/psnr-input.y4m
IMAGE_EXTENSION=png
QR_IMAGE_FILE_PREFIX="qrcode"
QR_IMAGE_DIRECTORY=output/qrcode
RAW_FRAME_DIRECTORY=output/raw
RAW_FRAMES_PATTERN=$RAW_FRAME_DIRECTORY/%03d.$IMAGE_EXTENSION
STAMPED_FRAME_DIRECTORY=output/stamped
STAMPED_FRAMES_PATTERN=$STAMPED_FRAME_DIRECTORY/%03d.$IMAGE_EXTENSION
FFMPEG=ffmpeg
FFMPEG_VIDEO_IN_ARGS="-i $VIDEO_IN_FILE"
FFMPEG_IMAGE_OUT_ARGS="-r $VIDEO_FPS -s $VIDEO_SZ -f image2 $RAW_FRAMES_PATTERN"
FFMPEG_IMAGE_IN_ARGS="-f image2 -framerate $VIDEO_FPS -i $STAMPED_FRAMES_PATTERN"
FFMPEG_VIDEO_OUT_ARGS="-s $VIDEO_SZ -f $VIDEO_FMT -pix_fmt $VIDEO_PIXEL_FMT $VIDEO_OUT_FILE"

# Create output directories.
mkdir -p $QR_IMAGE_DIRECTORY $RAW_FRAME_DIRECTORY $STAMPED_FRAME_DIRECTORY

# Extract images from video
RAW_FRAME_FILES_COUNT=$(ls -1 $RAW_FRAME_DIRECTORY/*.$IMAGE_EXTENSION | wc -l | tr -d '[ ]')
if [ "$RAW_FRAME_FILES_COUNT" = "0" ]
then
  $FFMPEG $FFMPEG_VIDEO_IN_ARGS $FFMPEG_IMAGE_OUT_ARGS
  RAW_FRAME_FILES_COUNT=$(ls -1 $RAW_FRAME_DIRECTORY/*.$IMAGE_EXTENSION|wc -l)
fi

# Stamp each output image.
STAMPED_FRAME_FILES_COUNT=$(ls -1 $STAMPED_FRAME_DIRECTORY/*.$IMAGE_EXTENSION | wc -l | tr -d '[ ]')
if [ "$STAMPED_FRAME_FILES_COUNT" = "0" ]
then
  for FRAME_NUMBER in $(seq -f "%03g" $RAW_FRAME_FILES_COUNT)
  do
    QR_IMAGE_FILE="$QR_IMAGE_DIRECTORY/$FRAME_NUMBER.$IMAGE_EXTENSION"
    RAW_FRAME_FILE="$RAW_FRAME_DIRECTORY/$FRAME_NUMBER.$IMAGE_EXTENSION"
    STAMPED_FRAME_FILE="$STAMPED_FRAME_DIRECTORY/$FRAME_NUMBER.$IMAGE_EXTENSION"

    qrencode --size=12 --level=H -o "$QR_IMAGE_FILE" "$FRAME_NUMBER"
    $FFMPEG -i $RAW_FRAME_FILE -i $QR_IMAGE_FILE -filter_complex overlay=10:10 $STAMPED_FRAME_FILE
  done
fi

# Creating a video from many images
$FFMPEG $FFMPEG_IMAGE_IN_ARGS $FFMPEG_VIDEO_OUT_ARGS
