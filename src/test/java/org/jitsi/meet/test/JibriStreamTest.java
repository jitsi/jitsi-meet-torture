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

import org.jitsi.meet.test.base.*;
import org.jitsi.meet.test.util.*;
import org.jitsi.meet.test.web.*;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;
import org.testng.*;
import org.testng.annotations.*;

import static org.testng.Assert.*;
import static org.jitsi.meet.test.util.TestUtils.*;

/**
 * Test for Jibri recorder.
 *
 * @author Hristo Terezov
 */
public class JibriStreamTest
    extends WebTestBase
{
    /**
     * The youtube stream key
     */
    public static String streamKey = null;

    /**
     * The property to get streamKey variable.
     */
    public static final String STREAM_KEY_PROP = "jibri.stream_key";

    /**
     * The youtube public URL
     */
    public static String publicUrl = null;

    /**
     * The property to change publicUrl variable.
     */
    public static final String PUBLIC_URL_PROP = "jibri.public_url";

    /**
     * Lists the possible statuses for the youtube stream.
     *
     */
    enum YOUTUBE_STATUS
    {
        ONLINE, OFFLINE
    }

    @Override
    public void setupClass()
    {
        super.setupClass();

        streamKey = System.getProperty(STREAM_KEY_PROP);
        publicUrl = System.getProperty(PUBLIC_URL_PROP);
        if (streamKey == null || publicUrl == null)
        {
            cleanupClass();
            throw new SkipException(
                "no streamKey or publicUrl");
        }

        ensureTwoParticipants();
    }

    /**
     * Checks if the recording button exists.
     */
    @Test
    public void checkJibriEnabled()
    {
        assertTrue(
            getParticipant1().getToolbar().hasRecordButton(),
            "Jibri button is missing");
    }

    /**
     * Starts the streaming.
     */
    @Test(dependsOnMethods = { "checkJibriEnabled" })
    public void startLiveStreaming()
    {
        getParticipant1().getToolbar().clickRecordButton();

        WebDriver driver1 = getParticipant1().getDriver();

        // fill in the dialog
        TestUtils.waitForElementByXPath(
            driver1, "//input[@name='streamId']", 5);
        driver1.findElement(By.xpath("//input[@name='streamId']"))
            .sendKeys(streamKey);

        driver1
            .findElement(By
                .xpath("//button[span/@data-i18n='dialog.startLiveStreaming']"))
            .click();
    }

    /**
     * Check the jibri text label after starting the recording. It should be
     * first pending and than on.
     */
    @Test(dependsOnMethods = { "startLiveStreaming" })
    public void checkJibriStatusOn()
    {
        WebDriver driver1 = getParticipant1().getDriver();
        WebDriver driver2 = getParticipant2().getDriver();
        try
        {
            TestUtils.waitForElementByXPath(
                driver1,
                "//span[@id='recordingLabelText' and @data-i18n='liveStreaming.pending']",
                10);
        }
        catch (org.openqa.selenium.TimeoutException e)
        {
            fail("Status label pending is missing for participant1");
        }

        try
        {
            TestUtils.waitForElementByXPath(
                driver2,
                "//span[@id='recordingLabelText' and @data-i18n='liveStreaming.pending']",
                5);
        }
        catch (org.openqa.selenium.TimeoutException e)
        {
            fail("Status label pending is missing for participant1");
        }

        try
        {
            TestUtils.waitForElementByXPath(
                driver1,
                "//span[@id='recordingLabelText' and @data-i18n='liveStreaming.on']",
                300);
        }
        catch (org.openqa.selenium.TimeoutException e)
        {
            fail("Status label on is missing for participant1");
        }

        try
        {
            TestUtils.waitForElementByXPath(
                driver2,
                "//span[@id='recordingLabelText' and @data-i18n='liveStreaming.on']",
                5);
        }
        catch (org.openqa.selenium.TimeoutException e)
        {
            fail("Status label on is missing for participant1");
        }
    }

    /**
     * Check the jibri text label after stopping the recording. It should be
     * off.
     */
    @Test(dependsOnMethods = { "stopLiveStreaming" })
    public void checkJibriStatusOff()
    {
        WebDriver driver1 = getParticipant1().getDriver();
        WebDriver driver2 = getParticipant2().getDriver();
        try
        {
            TestUtils.waitForElementByXPath(
                driver1,
                "//span[@id='recordingLabelText' and @data-i18n='liveStreaming.off']",
                10);
        }
        catch (org.openqa.selenium.TimeoutException e)
        {
            fail("Status label off is missing for participant1");
        }

        try
        {
            TestUtils.waitForElementByXPath(
                driver2,
                "//span[@id='recordingLabelText' and @data-i18n='liveStreaming.off']",
                5);
        }
        catch (org.openqa.selenium.TimeoutException e)
        {
            fail("Status label off is missing for participant1");
        }
    }

    /**
     * Checks that there is not jibri thumbnail
     */
    @Test(dependsOnMethods = { "checkJibriStatusOn" })
    public void checkForJibriParticipant()
    {
        WebDriver driver1 = getParticipant1().getDriver();
        assertEquals(
            MeetUIUtils.getVisibleThumbnails(driver1).size(),
            2,
            "number of visible thumbnails for participant1");

        WebDriver driver2 = getParticipant2().getDriver();
        assertEquals(
            MeetUIUtils.getVisibleThumbnails(driver2).size(),
            2,
            "number of visible thumbnails for participant2");
    }

    /**
     * Opens the public URL for the video and checks that the stream is live.
     */
    @Test(dependsOnMethods = { "checkForJibriParticipant" })
    public void testOnlineYoutubeStream()
    {
        try
        {
            testYoutubeStatus(YOUTUBE_STATUS.ONLINE);
        }
        catch (Throwable error)
        {
            fail("Expected youtube status: ONLINE, received: OFFLINE");
        }
    }

    /**
     * Stops the recording.
     */
    @Test(dependsOnMethods = { "testOnlineYoutubeStream" })
    public void stopLiveStreaming()
    {
        WebDriver driver1 = getParticipant1().getDriver();
        getParticipant1().getToolbar().clickRecordButton();

        // fill in the dialog
        TestUtils.waitForElementByXPath(
            driver1,
            "//button[span/@data-i18n='dialog.stopLiveStreaming']",
            5);
        driver1
            .findElement(By
                .xpath("//button[span/@data-i18n='dialog.stopLiveStreaming']"))
            .click();
    }

    /**
     * Opens the public URL for the video and checks that the stream is offline.
     */
    @Test(dependsOnMethods = { "checkJibriStatusOff" })
    public void testOfflineYoutubeStream()
    {
        try
        {
            testYoutubeStatus(YOUTUBE_STATUS.OFFLINE);
        }
        catch (Throwable error)
        {
            fail("Expected youtube status: OFFLINE, received: ONLINE");
        }
    }

    /**
     * Gets the youtube status of the stream and compares it with the passed
     * status.
     * 
     * @param expectedStatus the expected status.
     */
    private void testYoutubeStatus(final YOUTUBE_STATUS expectedStatus)
    {
        Participant participant = createParticipant(publicUrl);
        WebDriver driver = participant.getDriver();
        print("testYoutubeStatus wait for the status");
        TestUtils.waitForCondition(driver, 300,
            (ExpectedCondition<Boolean>) w ->
                getYoutubeStatus(driver) == expectedStatus);
        print("testYoutubeStatus close");
        participant.close();
        print("testYoutubeStatus done");
    }

    /**
     * Creates a {@link Participant} and opens a specific URL.
     * @param url the URL to be opened.
     * @return the {@code Participant} which was created.
     */
    private Participant createParticipant(String url)
    {
        print("Opening URL: " + url);

        WebParticipant participant
            = participants.createParticipant("web.participantOther");

        WebDriver driver = participant.getDriver();

        driver.get(url);
        MeetUtils.waitForPageToLoad(driver);

        participant.executeScript(
                "document.title='" + participant.getName() + "'");

        return participant;
    }

    /**
     * Returns the status of the youtube stream
     * 
     * @param driver the driver for the public URL of the youtube stream
     * @return returns the status of the youtube stream.
     */
    private YOUTUBE_STATUS getYoutubeStatus(WebDriver driver)
    {
        String statusText =
            driver.findElement(By.xpath("//h3[@class='ytp-fresca-countdown']"))
                .getText();
        return statusText.equalsIgnoreCase("offline") ? YOUTUBE_STATUS.OFFLINE
            : YOUTUBE_STATUS.ONLINE;
    }
}
