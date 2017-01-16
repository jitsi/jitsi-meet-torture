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
 * @author Pawel Domas
 */
public class SwitchVideoTest
    extends TestCase
{
    /**
     * Constructs test.
     * @param name the method name for the test.
     */
    public SwitchVideoTest(String name)
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

        suite.addTest(new SwitchVideoTest(
            "ownerClickOnLocalVideoAndTest"));
        suite.addTest(new SwitchVideoTest(
            "ownerClickOnRemoteVideoAndTest"));
        suite.addTest(new SwitchVideoTest(
            "ownerUnpinRemoteVideoAndTest"));
        suite.addTest(new SwitchVideoTest(
            "participantClickOnLocalVideoAndTest"));
        suite.addTest(new SwitchVideoTest(
            "participantClickOnRemoteVideoAndTest"));
        suite.addTest(new SwitchVideoTest(
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

        MeetUIUtils.selectLocalVideo(driver);
    }

    /**
     * Clicks on the remote video thumbnail and checks whether the large video
     * is the remote one.
     */
    public void ownerClickOnRemoteVideoAndTest()
    {
        System.err.println("Start ownerClickOnRemoteVideoAndTest.");

        clickOnRemoteVideoAndTest(
            ConferenceFixture.getOwnerInstance(),
            ConferenceFixture.getSecondParticipantInstance());
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
     * @param where
     */
    public static void clickOnRemoteVideoAndTest(WebDriver where, WebDriver who)
    {
        System.err.println("Start clickOnRemoteVideoAndTest.");

        MeetUIUtils.selectRemoteVideo(where, who);
    }

    /**
     * Click on local video thumbnail and checks whether the large video
     * is the local one.
     */
    public void participantClickOnLocalVideoAndTest()
    {
        System.err.println("Start participantClickOnLocalVideoAndTest.");

        clickOnLocalVideoAndTest(
            ConferenceFixture.getSecondParticipantInstance());
    }

    /**
     * Clicks on the remote video thumbnail and checks whether the large video
     * is the remote one.
     */
    public void participantClickOnRemoteVideoAndTest()
    {
        System.err.println("Start participantClickOnRemoteVideoAndTest.");

        clickOnRemoteVideoAndTest(
            ConferenceFixture.getSecondParticipantInstance(),
            ConferenceFixture.getOwnerInstance());
    }

}
