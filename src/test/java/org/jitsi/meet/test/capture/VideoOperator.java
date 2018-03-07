/*
 * Copyright @ 2015 Atlassian Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jitsi.meet.test.capture;

import org.jitsi.meet.test.util.*;

import org.jitsi.meet.test.web.*;

import java.util.*;

/**
 * Java wrapper for PSNRVideoOperator.js script used to capture video frames.
 *
 * @author Pawel Domas
 */
public class VideoOperator
{
    /**
     * JS utility which allows to capture frames from the video.
     */
    private static final String PSNR_JS_SCRIPT
        = "resources/PSNRVideoOperator.js";

    /**
     * <tt>WebDriver</tt> used by this instance.
     */
    private final WebParticipant participant;

    /**
     * Creates new instance of <tt>VideoOperator</tt>.
     * @param participant the <tt>WebDriver</tt> instance where recorder script
     * will be running.
     */
    public VideoOperator(WebParticipant participant)
    {
        this.participant = Objects.requireNonNull(participant);
    }

    /**
     * Releases resources allocated by the underlying recorder script.
     */
    public void dispose()
    {
        participant.executeScript(
                "window._operator.cleanup();" +
                "window._operator = null;");
    }

    /**
     * Gets an audio level.
     * @param videoId the ID of the video element
     * @param frameIdx the index of captured framed for which audio level will
     * be obtained
     * @return a <tt>Double</tt> with audio level value from 0.0 to 1.0
     */
    public Double getAudioLevel(String videoId, int frameIdx)
    {
        Object levelObj
            = participant.executeScript(
                    "return window._operator.getAudioLevel(" +
                        "arguments[0], arguments[1])",
                    videoId,
                    frameIdx);

        return Double.valueOf(String.valueOf(levelObj));
    }

    /**
     * Gets raw frame data
     * @param videoId the id of the video element
     * @param frameIdx the index of the frame for which raw data will be
     * retrieved
     * @return a byte array with raw image data of the frame. The format is
     * specified in PSNRVideoOperator.js.
     */
    public byte[] getFrame(String videoId, int frameIdx)
    {
        String frameBase64 = (String) participant.executeScript(
                "return window._operator.getFrame(arguments[0], arguments[1])",
                videoId, frameIdx);

        // Convert it to binary
        // Java 8 has a Base64 class.
        return org.apache.commons.codec.binary.Base64.decodeBase64(frameBase64);
    }

    /**
     * Tells how many frames have been captured for the video.
     * @param videoId the id of the video element.
     * @return a <tt>Long</tt> with number of frames.
     */
    public Long getFramesCount(String videoId)
    {
        return (Long) participant.executeScript(
                "return window._operator.getFramesCount(arguments[0])",
                videoId);
    }

    /**
     * Returns raw data size of all frames in MB.
     * @return a <tt>Number</tt> or <tt>Long</tt> value in MB.
     */
    public Number getRawDataSize()
    {
        return (Number) participant.executeScript(
                "return window._operator.getRawDataSize() / 1024 / 1024");
    }

    /**
     * Returns "real FPS" as defined in PSNRVideoOperator.js
     * @return a <tt>Number</tt> with the FPS value calculated in
     * PSNRVideoOperator.js
     */
    public Number getRealFPS()
    {
        return (Number) participant.executeScript(
                "return window._operator.getRealFPS()");
    }

    /**
     * Gets RGBA values for the center pixel of the video frame.
     * @param videoId the id of video element
     * @param frameIdx the frame index
     * @return a list of <tt>Long</tt> with red, green, blue and alpha values
     * positioned one after another in the list
     */
    @SuppressWarnings("unchecked") // ok to fail the tests with cast exception
    public List<Long> getRGBAatTheCenter(String videoId, int frameIdx)
    {
        Object rgbaObj = participant.executeScript(
                "return window._operator.getRGBAatTheCenter(" +
                    "arguments[0], arguments[1])",
                videoId,
                frameIdx);

        return (List<Long>) rgbaObj;
    }

    /**
     * Gets frame timestamp
     * @param videoId the id of video element
     * @param frameIdx the frame index
     * @return a <tt>Long</tt> with the frame timestamp obtained with JavaScript
     * Date.now().
     */
    public Long getTimestamp(String videoId, int frameIdx)
    {
        Object tsObj = participant.executeScript(
                "return window._operator.getTimestamp(" +
                    "arguments[0], arguments[1])",
                videoId,
                frameIdx);
        return (Long) tsObj;
    }

    /**
     * Injects {@link #PSNR_JS_SCRIPT}.
     */
    public void init()
    {
        TestUtils.injectScript(participant.getDriver(), PSNR_JS_SCRIPT);
    }

    /**
     * Starts the recording for videos which ids are on the <tt>videoIDs</tt>
     * list.
     * @param videoIDs the list of video elements IDs which will be recorded.
     */
    public void recordAll(List<String> videoIDs)
    {
        participant.executeScript(
                "window._operator = new window.VideoOperator();" +
                    "window._operator.recordAll(arguments[0]);",
                videoIDs);
    }

    /**
     * Starts the recording for videos which ids are on the <tt>videoIDs</tt>
     * list. Audio levels will also be recorded.
     * @param videoIDs the list of video elements IDs which will be recorded.
     * @param fps recording frame rate
     * @param aLvlResources the list with MUC resource JIDs for which audio
     * levels will be recorded. Audio levels are matched with video frames based
     * the appearance order on <tt>videoIDs</tt> and <tt>aLvlResources</tt>
     * lists.
     */
    public void recordAll(List<String>    videoIDs,
                          int             fps,
                          List<String>    aLvlResources)
    {
        participant.executeScript(
                "window._operator = new window.VideoOperator();" +
                    "window._operator.recordAll(" +
                    "arguments[0], arguments[1], arguments[2]);",
                videoIDs, fps, aLvlResources);
    }

    /**
     * Stops the recording.
     */
    public void stopRecording()
    {
        participant.executeScript("window._operator.stop()");
    }
}
