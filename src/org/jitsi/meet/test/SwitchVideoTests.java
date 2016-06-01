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
package org.jitsi.meet.test;

import junit.framework.*;
import org.jitsi.meet.test.util.*;
import org.openqa.selenium.*;

/**
 * Tests switching video of participants.
 * @author Damian Minkov
 */
public class SwitchVideoTests
    extends TestCase
{
    /**
     * Constructs test.
     * @param name the method name for the test.
     */
    public SwitchVideoTests(String name)
    {
        super(name);
    }

    /**
     * Orders the tests.
     * @return the suite with order tests.
     */
    public static junit.framework.Test suite()
    {
        TestSuite suite = new TestSuite();

        suite.addTest(new SwitchVideoTests(
            "ownerClickOnLocalVideoAndTest"));
        suite.addTest(new SwitchVideoTests(
            "ownerClickOnRemoteVideoAndTest"));
        suite.addTest(new SwitchVideoTests(
            "ownerUnpinRemoteVideoAndTest"));
        suite.addTest(new SwitchVideoTests(
            "participantClickOnLocalVideoAndTest"));
        suite.addTest(new SwitchVideoTests(
            "participantClickOnRemoteVideoAndTest"));
        suite.addTest(new SwitchVideoTests(
            "participantUnpinRemoteVideo"));

        return suite;
    }

    /**
     * Click on local video thumbnail and checks whether the large video
     * is the local one.
     */
    public void ownerClickOnLocalVideoAndTest()
    {
        System.err.println("Start ownerClickOnLocalVideoAndTest.");

        clickOnLocalVideoAndTest(ConferenceFixture.getOwner());
    }

    /**
     * Click on local video thumbnail and checks whether the large video
     * is the local one.
     */
    private void clickOnLocalVideoAndTest(WebDriver driver)
    {
        System.err.println("Start clickOnLocalVideoAndTest.");

        WebElement localVideoElem = driver.findElement(
            By.xpath("//span[@id='localVideoWrapper']/video"));

        String localVideoSrc = localVideoElem.getAttribute("src");

        // click on local
        driver.findElement(By.className("focusindicator")).click();

        TestUtils.waitMillis(1000);

        if(ConferenceFixture.getBrowserType(driver).equals(
            ConferenceFixture.BrowserType.firefox))
        {
            String localVideoId = localVideoElem.getAttribute("id");

            assertTrue("Video didn't change to local",
                MeetUIUtils.firefoxCheckVideoDisplayedOnLarge(
                        driver, localVideoId));
        }
        else
        {
            // test is this the video seen
            assertEquals("Video didn't change to local",
                localVideoSrc, MeetUIUtils.getLargeVideoSource(driver));
        }
    }

    /**
     * Clicks on the remote video thumbnail and checks whether the large video
     * is the remote one.
     */
    public void ownerClickOnRemoteVideoAndTest()
    {
        System.err.println("Start ownerClickOnRemoteVideoAndTest.");

        clickOnRemoteVideoAndTest(ConferenceFixture.getOwner());
    }

    /**
     * Unpins remote video in owner and verifies if the operation has succeeded.
     */
    public void ownerUnpinRemoteVideoAndTest()
    {
        System.err.println("Start ownerUnpinRemoteVideoAndTest.");

        unpinRemoteVideoAndTest(
            ConferenceFixture.getOwner(),
            ConferenceFixture.getSecondParticipant());
    }

    /**
     * Unpins remote video in the 2nd participant and verifies if the operation
     * has succeeded.
     */
    public void participantUnpinRemoteVideo()
    {
        System.err.println("Start participantUnpinRemoteVideo.");

        unpinRemoteVideoAndTest(
            ConferenceFixture.getSecondParticipant(),
            ConferenceFixture.getOwner());
    }

    /**
     * Unpins remote video of given <tt>peer</tt> from given <tt>host</tt>
     * perspective.
     *
     * @param host the <tt>WebDriver</tt> instance where unpinning operation
     *             will be performed
     * @param peer the <tt>WebDriver</tt> instance to whom remote video to be
     *             unpinned belongs to.
     */
    private void unpinRemoteVideoAndTest(WebDriver host, WebDriver peer)
    {
        System.err.println("Start unpinRemoteVideoAndTest.");

        // Peer's endpoint ID
        String peerEndpointId = MeetUtils.getResourceJid(peer);

        // Remote video with 'videoContainerFocused' is the pinned one
        String pinnedThumbXpath
            = "//span[ @id='participant_" + peerEndpointId + "'" +
              "        and contains(@class,'videoContainerFocused') ]";

        WebElement pinnedThumb = host.findElement(By.xpath(pinnedThumbXpath));

        assertNotNull(
            "Pinned remote video not found for " + peerEndpointId, pinnedThumb);

        // click on remote
        host.findElement(By.xpath(pinnedThumbXpath))
            .click();

        // Wait for the video to unpin
        try
        {
            TestUtils.waitForElementNotPresentByXPath(
                host, pinnedThumbXpath, 2);
        }
        catch (TimeoutException exc)
        {
            fail("Failed to unpin video for " + peerEndpointId);
        }
    }

    /**
     * Clicks on the remote video thumbnail and checks whether the large video
     * is the remote one.
     *
     * @param driver
     */
    public static void clickOnRemoteVideoAndTest(WebDriver driver)
    {
        System.err.println("Start clickOnRemoteVideoAndTest.");

        // first wait for remote video to be visible
        String remoteThumbXpath
            = "//span[starts-with(@id, 'participant_') " +
            " and contains(@class,'videocontainer')]";
        // has the src attribute, those without it is the videobridge video tag
        // which is not displayed, it is used for rtcp
        String remoteThumbVideoXpath
            = remoteThumbXpath
                + "/video[starts-with(@id, 'remoteVideo_')";

        if(ConferenceFixture.getBrowserType(driver).equals(
                ConferenceFixture.BrowserType.firefox))
            remoteThumbVideoXpath += "]";
        else
            remoteThumbVideoXpath += " and @src]";

        TestUtils.waitForElementByXPath(
            driver,
            remoteThumbVideoXpath,
            5
        );

        WebElement remoteThumb = driver
            .findElement(By.xpath(remoteThumbVideoXpath));

        assertNotNull("Remote video not found", remoteThumb);

        // click on remote
        driver.findElement(By.xpath(remoteThumbXpath))
            .click();

        TestUtils.waitMillis(1000);

        if(ConferenceFixture.getBrowserType(driver).equals(
            ConferenceFixture.BrowserType.firefox))
        {
            String remoteVideoId = remoteThumb.getAttribute("id");

            assertTrue("Video didn't change to remote one",
                MeetUIUtils.firefoxCheckVideoDisplayedOnLarge(
                        driver, remoteVideoId));
        }
        else
        {
            // Obtain the remote video src *after* we have clicked the thumbnail
            // and have waited. With simulcast enabled, the remote stream may
            // change.
            String remoteVideoSrc = remoteThumb.getAttribute("src");

            // test is this the video seen
            assertEquals("Video didn't change to remote one",
                remoteVideoSrc, MeetUIUtils.getLargeVideoSource(driver));
        }
    }

    /**
     * Click on local video thumbnail and checks whether the large video
     * is the local one.
     */
    public void participantClickOnLocalVideoAndTest()
    {
        System.err.println("Start participantClickOnLocalVideoAndTest.");

        clickOnLocalVideoAndTest(ConferenceFixture.getSecondParticipant());
    }

    /**
     * Clicks on the remote video thumbnail and checks whether the large video
     * is the remote one.
     */
    public void participantClickOnRemoteVideoAndTest()
    {
        System.err.println("Start participantClickOnRemoteVideoAndTest.");

        clickOnRemoteVideoAndTest(ConferenceFixture.getSecondParticipant());
    }

}
