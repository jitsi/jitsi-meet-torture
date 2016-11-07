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

import org.jitsi.meet.test.util.*;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;

import junit.framework.*;

/**
 * Test for Jibri recorder.
 *
 * @author Hristo Terezov
 */
public class JibriTest
    extends TestCase
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
     * Constructs test
     * 
     * @param name the method name for the test.
     */
    public JibriTest(String name)
    {
        super(name);
    }

    /**
     * Lists the possible statuses for the youtube stream.
     *
     */
    enum YT_STATUS
    {
        ONLINE, OFFLINE
    };

    /**
     * Orders the tests.
     * 
     * @return the suite with order tests.
     */
    public static junit.framework.Test suite()
    {
        TestSuite suite = new TestSuite();
        streamKey = System.getProperty(STREAM_KEY_PROP);
        publicURL = System.getProperty(PUBLIC_URL_PROP);
        if(streamKey == null || publicURL == null)
        {
            return suite;
        }
        
        suite.addTest(new JibriTest("checkJibriEnabled"));
        suite.addTest(new JibriTest("startLiveStreaming"));
        suite.addTest(new JibriTest("checkJibriStatusOn"));
        suite.addTest(new JibriTest("checkForJibriParticipant"));
        suite.addTest(new JibriTest("testOnlineYoutubeStream"));
        suite.addTest(new JibriTest("stopLiveStreaming"));
        suite.addTest(new JibriTest("checkJibriStatusOff"));
        suite.addTest(new JibriTest("testOfflineYoutubeStream"));

        return suite;
    }


    /**
     * Checks if the recording button exists.
     */
    public void checkJibriEnabled()
    {
        System.err.println("Start checkJibriEnabled.");
        WebDriver owner = ConferenceFixture.getOwner();
        List<WebElement> elems = owner.findElements(
            By.xpath("//a[@class='button fa " + "fa-play-circle']"));

        assertFalse("Jibri button is missing", elems.isEmpty());

    }

    /**
     * Starts the streaming.
     */
    public void startLiveStreaming()
    {
        System.err.println("Start startLiveStreaming.");
        WebDriver owner = ConferenceFixture.getOwner();
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
    public void checkJibriStatusOn()
    {
        System.err.println("Start checkJibriStatusOn.");
        WebDriver owner = ConferenceFixture.getOwner();
        WebDriver secondParticipant = ConferenceFixture.getSecondParticipant();
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
    public void checkJibriStatusOff()
    {
        System.err.println("Start checkJibriStatusOff.");
        WebDriver owner = ConferenceFixture.getOwner();
        WebDriver secondParticipant = ConferenceFixture.getSecondParticipant();
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
    public void checkForJibriParticipant()
    {
        System.err.println("Start checkForJibriParticipant.");
        WebDriver owner = ConferenceFixture.getOwner();
        assertEquals("number of visible thumbnails for owner", 2,
            MeetUIUtils.getVisibleThumbnails(owner).size());
        WebDriver sencondParticipant = ConferenceFixture.getSecondParticipant();
        assertEquals("number of visible thumbnails for second participant", 2,
            MeetUIUtils.getVisibleThumbnails(sencondParticipant).size());
    }

    /**
     * Opens the public URL for the video and checks that the stream is live.
     */
    public void testOnlineYoutubeStream()
    {
        System.err.println("Start testOnlineYoutubeStream.");
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
    public void stopLiveStreaming()
    {
        System.err.println("Start stopLiveStreaming.");
        WebDriver owner = ConferenceFixture.getOwner();
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
    public void testOfflineYoutubeStream()
    {
        System.err.println("Start testOfflineYoutubeStream.");
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
        System.err.println("Start testYTStatus.");
        final WebDriver driver = ConferenceFixture.openURL(publicURL);
        System.err.println("testYTStatus wait for the status");
        TestUtils.waitForCondition(driver, 300, new ExpectedCondition<Boolean>()
        {

            @Override
            public Boolean apply(WebDriver w)
            {
                return getYTStatus(driver) == expectedStatus;
            }
        });
        System.err.println("testYTStatus quit");
        ConferenceFixture.quit(driver, false);
        System.err.println("testYTStatus done");
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
