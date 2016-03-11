#!/bin/sh -e

for i in /tmp/remoteVideo_*
do
    FRAME_NUMBER=$(zbarimg --quiet $i|cut -d: -f2)
    if [ "$FRAME_NUMBER" != "" ]
    then

      ORIGIN_IMAGE=resources/psnr/output/stamped/stamped-FourPeople_1280x720_60-$FRAME_NUMBER.png

        # Test quality/success of conversion by looking at PSNR
        PSNR=$(convert "$i" -resize 1280x720\! "$ORIGIN_IMAGE" -metric PSNR -format "%[distortion]" -compare info:)
        echo DEBUG: PSNR=$PSNR

        # PSNR above 20 is pretty indicative of good similarity - use "bc" as shell doesn't do floats
        if [ $(echo "$PSNR>20" | bc) -eq 1 ]; then
           echo $ORIGIN_IMAGE looks good
        else
           echo $ORIGIN_IMAGE something wrong
        fi
    fi
done