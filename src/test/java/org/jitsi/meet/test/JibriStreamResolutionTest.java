/*
 * Copyright @ 2018 8x8 Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.jitsi.meet.test;

import org.jitsi.meet.test.util.*;
import org.jitsi.meet.test.web.*;
import org.json.*;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;
import org.testng.*;
import org.testng.annotations.*;

import java.util.*;
import java.util.logging.*;

/**
 * Starts a live stream on the YouTube key provided and checks the received stats for the download that is used
 * on the jibri side, to detect low resolution streaming.
 *
 * @author Damian Minkov
 */
public class JibriStreamResolutionTest
    extends WebTestBase
{
    /**
     * The video file to use as input for the sender participant.
     */
    private static final String INPUT_VIDEO_FILE = "resources/FourPeople_1280x720_30.y4m";

    /**
     * The property to get streamKey variable.
     */
    public static final String STREAM_KEY_PROP = "jibri.stream_key";

    /**
     * 720p received(download) is around 1500
     */
    private static final int RECEIVED_DOWNLOAD_THRESHOLD = 1400;

    /**
     * The YouTube stream key to use.
     */
    public static String streamKey = null;

    /**
     * Accumulate the stats, so we can print them on failure.
     */
    private final List<Map> receivedStats = new LinkedList<>();

    @Override
    public void setupClass()
    {
        super.setupClass();

        streamKey = System.getProperty(STREAM_KEY_PROP);
        if (streamKey == null || streamKey.trim().length() == 0)
        {
            throw new SkipException("no streamKey");
        }
    }

    /**
     * Checks if the recording button exists.
     */
    @Test
    public void checkRecordingResolution()
    {
        ensureOneParticipant(null, new WebParticipantOptions().setFakeStreamVideoFile(INPUT_VIDEO_FILE));

        WebParticipant participant1 = getParticipant1();
        WebDriver driver1 = participant1.getDriver();

        if (!participant1.getToolbar().hasLiveStreamButton())
        {
            new SkipException("No recording enabled");
        }

        participant1.getToolbar().clickLiveStreamingButton();

        String streamIdInputXpath = "//input[@name='streamId']";
        String dialogOkButtonXpath = "//button[@id='modal-dialog-ok-button']";

        TestUtils.waitForElementByXPath(driver1, streamIdInputXpath, 5);
        driver1.findElement(By.xpath(streamIdInputXpath)).sendKeys(streamKey);

        driver1.findElement(By.xpath(dialogOkButtonXpath)).click();

        String filterJibriFromParticipantsJS
            = "APP.conference._room.getParticipants().filter(p => p._hidden).map(p => p._id)";

        // Waits for jibri to arrive
        TestUtils.waitForCondition(driver1, "Jibri did not join", 40, (ExpectedCondition<Boolean>) d -> {
            ArrayList<String> o =  (ArrayList<String>)((JavascriptExecutor) d)
                .executeScript(String.format("return %s;", filterJibriFromParticipantsJS));
            return o.size() == 1;
        });

        // Extract the id of jibri
        String jibriId =  (String)participant1.executeScript(
            String.format("return %s[0];", filterJibriFromParticipantsJS));

        // Listens for jibri stats received and store them in the window object
        participant1.executeScript("APP.conference._room.on('conference.endpoint_stats_received', " +
            "(participant, payload) => {" +
            "if(participant._id === '" + jibriId + "') {window.jibriLastStats = payload;}});");

        // 10 seconds is the stats interval, let's wait for at least three
        TestUtils.waitForCondition(
            driver1, "Jibri did not send stats or its download is below threshold",
            30, (ExpectedCondition<Boolean>) d ->
        {
            // get the stored stats from the window object
            Map stats =  (Map)((JavascriptExecutor) d).executeScript("return window.jibriLastStats;");

            // no stats yet
            if (stats == null)
            {
                return false;
            }

            receivedStats.add(stats);

            return ((Map<String, Map<String, Long>>)stats.get("bitrate"))
                .get("video").get("download") > RECEIVED_DOWNLOAD_THRESHOLD;
        });

        // Now let's stop it
        participant1.getToolbar().clickLiveStreamingButton();

        TestUtils.waitForElementByXPath(driver1, dialogOkButtonXpath, 5);
        driver1.findElement(By.xpath(dialogOkButtonXpath)).click();

        TestUtils.waitForNotDisplayedElementByXPath(driver1, dialogOkButtonXpath, 5);
    }

    @AfterMethod
    public void tearDown(ITestResult result)
    {
        if (result.getStatus() == ITestResult.FAILURE)
        {
            Logger.getGlobal().log(Level.SEVERE, "The received stats:" + new JSONArray(receivedStats).toString(4));
        }
    }
}
