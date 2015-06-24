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
        clickOnLocalVideoAndTest(ConferenceFixture.getOwner());
    }

    /**
     * Click on local video thumbnail and checks whether the large video
     * is the local one.
     */
    private void clickOnLocalVideoAndTest(WebDriver driver)
    {
        // click on local
        String localVideoSrc = driver.findElement(
            By.xpath("//span[@id='localVideoWrapper']/video"))
            .getAttribute("src");

        driver.findElement(By.className("focusindicator")).click();

        TestUtils.waits(1000);

        // test is this the video seen
        assertEquals("Video didn't change to local",
            localVideoSrc, getLargeVideoSource(driver));
    }

    /**
     * Returns the source of the large video currently shown.
     * @return the source of the large video currently shown.
     */
    private String getLargeVideoSource(WebDriver driver)
    {
        return driver.findElement(By.xpath(
            "//div[@id='largeVideoContainer']/video[@id='largeVideo']"))
                .getAttribute("src");
    }

    /**
     * Clicks on the remote video thumbnail and checks whether the large video
     * is the remote one.
     */
    public void ownerClickOnRemoteVideoAndTest()
    {
        clickOnRemoteVideoAndTest(ConferenceFixture.getOwner());
    }

    /**
     * Unpins remote video in owner and verifies if the operation has succeeded.
     */
    public void ownerUnpinRemoteVideoAndTest()
    {
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
            TestUtils.waitsForElementNotPresentByXPath(
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
     */
    private void clickOnRemoteVideoAndTest(WebDriver driver)
    {
        // first wait for remote video to be visible
        String remoteThumbXpath
            = "//span[starts-with(@id, 'participant_') " +
            "           and contains(@class,'videocontainer')]";
        // has the src attribute, those without it is the videobridge video tag
        // which is not displayed, it is used for rtcp
        String remoteThumbVideoXpath
            = remoteThumbXpath
                + "/video[starts-with(@id, 'remoteVideo_') and @src]";

        TestUtils.waitsForElementByXPath(
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

        TestUtils.waits(1000);

        // Obtain the remote video src *after* we have clicked the thumbnail
        // and have waited. With simulcast enabled, the remote stream may
        // change.
        String remoteVideoSrc = remoteThumb.getAttribute("src");

        // test is this the video seen
        assertEquals("Video didn't change to remote one",
            remoteVideoSrc, getLargeVideoSource(driver));
    }

    /**
     * Click on local video thumbnail and checks whether the large video
     * is the local one.
     */
    public void participantClickOnLocalVideoAndTest()
    {
        clickOnLocalVideoAndTest(ConferenceFixture.getSecondParticipant());
    }

    /**
     * Clicks on the remote video thumbnail and checks whether the large video
     * is the remote one.
     */
    public void participantClickOnRemoteVideoAndTest()
    {
        clickOnRemoteVideoAndTest(ConferenceFixture.getSecondParticipant());
    }

}
