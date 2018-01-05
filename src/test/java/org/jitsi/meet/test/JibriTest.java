/*
 * Copyright @ 2015 Atlassian Pty Ltd
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

import java.util.*;

import org.jitsi.meet.test.base.*;
import org.jitsi.meet.test.util.*;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;
import org.testng.*;
import org.testng.annotations.*;

import static org.testng.Assert.*;

/**
 * Test for Jibri recorder.
 *
 * @author Hristo Terezov
 */
public class JibriTest
    extends AbstractBaseTest
{
    /**
     * The youtube stream key
     */
    public static String streamKey = null;

    /**
     * The property to get streamKey variable.
     */
    public static String STREAM_KEY_PROP = "jibri.stream_key";

    /**
     * The youtube public URL
     */
    public static String publicURL = null;

    /**
     * The property to change publicURL variable.
     */
    public static String PUBLIC_URL_PROP = "jibri.public_url";

    /**
     * Lists the possible statuses for the youtube stream.
     *
     */
    enum YT_STATUS
    {
        ONLINE, OFFLINE
    }

    @Override
    public void setup()
    {
        super.setup();

        streamKey = System.getProperty(STREAM_KEY_PROP);
        publicURL = System.getProperty(PUBLIC_URL_PROP);
        if(streamKey == null || publicURL == null)
        {
            throw new SkipException(
                "no streamKey or publicURL");
        }

        ensureTwoParticipants();
    }

    /**
     * Checks if the recording button exists.
     */
    @Test
    public void checkJibriEnabled()
    {
        WebDriver owner = participant1.getDriver();
        List<WebElement> elems = owner.findElements(
            By.xpath("//a[@class='button fa " + "fa-play-circle']"));

        assertFalse(elems.isEmpty(), "Jibri button is missing");
    }

    /**
     * Starts the streaming.
     */
    @Test(dependsOnMethods = { "checkJibriEnabled" })
    public void startLiveStreaming()
    {
        WebDriver owner = participant1.getDriver();
        MeetUIUtils.clickOnToolbarButton(owner, "toolbar_button_record");

        // fill in the dialog
        TestUtils.waitForElementByXPath(owner, "//input[@name='streamId']", 5);
        owner.findElement(By.xpath("//input[@name='streamId']"))
            .sendKeys(streamKey);

        owner
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
        WebDriver owner = participant1.getDriver();
        WebDriver secondParticipant = participant2.getDriver();
        try
        {
            TestUtils.waitForElementByXPath(owner,
                "//span[@id='recordingLabelText' and @data-i18n='liveStreaming.pending']",
                10);
        }
        catch (org.openqa.selenium.TimeoutException e)
        {
            fail("Status label pending is missing for the owner!");
        }

        try
        {
            TestUtils.waitForElementByXPath(secondParticipant,
                "//span[@id='recordingLabelText' and @data-i18n='liveStreaming.pending']",
                5);
        }
        catch (org.openqa.selenium.TimeoutException e)
        {
            fail("Status label pending is missing for the second participant!");
        }

        try
        {
            TestUtils.waitForElementByXPath(owner,
                "//span[@id='recordingLabelText' and @data-i18n='liveStreaming.on']",
                300);
        }
        catch (org.openqa.selenium.TimeoutException e)
        {
            fail("Status label on is missing for the owner!");
        }

        try
        {
            TestUtils.waitForElementByXPath(secondParticipant,
                "//span[@id='recordingLabelText' and @data-i18n='liveStreaming.on']",
                5);
        }
        catch (org.openqa.selenium.TimeoutException e)
        {
            fail("Status label on is missing for the second participant!");
        }
    }

    /**
     * Check the jibri text label after stopping the recording. It should be
     * off.
     */
    @Test(dependsOnMethods = { "stopLiveStreaming" })
    public void checkJibriStatusOff()
    {
        WebDriver owner = participant1.getDriver();
        WebDriver secondParticipant = participant2.getDriver();
        try
        {
            TestUtils.waitForElementByXPath(owner,
                "//span[@id='recordingLabelText' and @data-i18n='liveStreaming.off']",
                10);
        }
        catch (org.openqa.selenium.TimeoutException e)
        {
            fail("Status label off is missing for the owner!");
        }

        try
        {
            TestUtils.waitForElementByXPath(secondParticipant,
                "//span[@id='recordingLabelText' and @data-i18n='liveStreaming.off']",
                5);
        }
        catch (org.openqa.selenium.TimeoutException e)
        {
            fail("Status label off is missing for the second participant!");
        }
    }

    /**
     * Checks that there is not jibri thumbnail
     */
    @Test(dependsOnMethods = { "checkJibriStatusOn" })
    public void checkForJibriParticipant()
    {
        WebDriver owner = participant1.getDriver();
        assertEquals(
            2, MeetUIUtils.getVisibleThumbnails(owner).size(),
            "number of visible thumbnails for owner");
        WebDriver sencondParticipant = participant2.getDriver();
        assertEquals(
            2, MeetUIUtils.getVisibleThumbnails(sencondParticipant).size(),
            "number of visible thumbnails for second participant");
    }

    /**
     * Opens the public URL for the video and checks that the stream is live.
     */
    @Test(dependsOnMethods = { "checkForJibriParticipant" })
    public void testOnlineYoutubeStream()
    {
        try
        {
            testYTStatus(YT_STATUS.ONLINE);
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
        WebDriver owner = participant1.getDriver();
        MeetUIUtils.clickOnToolbarButton(owner, "toolbar_button_record");

        // fill in the dialog
        TestUtils.waitForElementByXPath(owner,
            "//button[span/@data-i18n='dialog.stopLiveStreaming']", 5);
        owner
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
            testYTStatus(YT_STATUS.OFFLINE);
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
    private void testYTStatus(final YT_STATUS expectedStatus)
    {
        Participant p = openURL(publicURL);
        WebDriver driver = p.getDriver();
        print("testYTStatus wait for the status");
        TestUtils.waitForCondition(driver, 300,
            (ExpectedCondition<Boolean>) w ->
                getYTStatus(driver) == expectedStatus);
        print("testYTStatus quit");
        p.quit();
        print("testYTStatus done");
    }

    /**
     * Opens URL using new WebDriver.
     * @param URL the URL to be opened
     * @return the {@code Participant} which was created.
     */
    private static Participant openURL(String URL)
    {
        print("Opening URL: " + URL);

        Participant pageParticipant
            = ParticipantFactory.getInstance()
                .createParticipant("web.participantOther");
        WebDriver driver = pageParticipant.getDriver();

        driver.get(URL);
        MeetUtils.waitForPageToLoad(driver);

        ((JavascriptExecutor) driver)
            .executeScript(
                "document.title='" + pageParticipant.getName() + "'");

        return pageParticipant;
    }

    /**
     * Returns the status of the youtube stream
     * 
     * @param driver the driver for the public URL of the youtube stream
     * @return returns the status of the youtube stream.
     */
    private YT_STATUS getYTStatus(WebDriver driver)
    {
        String statusText =
            driver.findElement(By.xpath("//h3[@class='ytp-fresca-countdown']"))
                .getText();
        return statusText.equalsIgnoreCase("offline") ? YT_STATUS.OFFLINE
            : YT_STATUS.ONLINE;
    }
}
