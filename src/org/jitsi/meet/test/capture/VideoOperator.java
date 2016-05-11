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

import org.openqa.selenium.*;

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
    private final WebDriver participant;

    /**
     * Creates new instance of <tt>VideoOperator</tt>.
     * @param participant the <tt>WebDriver</tt> instance where recorder script
     * will be running.
     */
    public VideoOperator(WebDriver participant)
    {
        this.participant = participant;
    }

    /**
     * Releases resources allocated by the underlying recorder script.
     */
    public void dispose()
    {
        getJSExecutor().executeScript(
                "window._operator.cleanup();" +
                "window._operator = null;");
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
        String frameBase64 = (String) getJSExecutor().executeScript(
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
        return (Long) getJSExecutor().executeScript(
                "return window._operator.getFramesCount(arguments[0])",
                videoId);
    }

    private JavascriptExecutor getJSExecutor()
    {
        return (JavascriptExecutor) participant;
    }

    /**
     * Returns raw data size of all frames in MB.
     * @return a <tt>Double</tt> value in MB.
     */
    public Double getRawDataSize()
    {
        return (Double) getJSExecutor().executeScript(
                "return window._operator.getRawDataSize() / 1024 / 1024");
    }

    /**
     * Returns "real FPS" as defined in PSNRVideoOperator.js
     * @return a <tt>Double</tt> with the FPS value calculated in
     * PSNRVideoOperator.js
     */
    public Double getRealFPS()
    {
        return (Double) getJSExecutor().executeScript(
                "return window._operator.getRealFPS()");
    }

    /**
     * Injects {@link #PSNR_JS_SCRIPT}.
     */
    public void init()
    {
        TestUtils.injectScript(participant, PSNR_JS_SCRIPT);
    }

    /**
     * Starts the recording for videos which ids are on the <tt>videoIDs</tt>
     * list.
     * @param videoIDs the list of video elements IDs which will be recorded.
     */
    public void recordAll(List<String> videoIDs)
    {
        getJSExecutor().executeScript(
                "window._operator = new window.VideoOperator();" +
                    "window._operator.recordAll(arguments[0]);",
                videoIDs);
    }

    /**
     * Stops the recording.
     */
    public void stopRecording()
    {
        getJSExecutor().executeScript("window._operator.stop()");
    }
}
