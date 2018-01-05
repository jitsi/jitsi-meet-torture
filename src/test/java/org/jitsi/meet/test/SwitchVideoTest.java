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

import org.jitsi.meet.test.base.*;
import org.jitsi.meet.test.util.*;

import org.openqa.selenium.*;
import org.testng.annotations.*;

import static org.testng.Assert.*;

/**
 * Tests switching video of participants.
 * @author Damian Minkov
 * @author Pawel Domas
 */
public class SwitchVideoTest
    extends AbstractBaseTest
{
    /**
     * Default constructor.
     */
    public SwitchVideoTest()
    {}

    /**
     * Constructs SwitchVideoTest with already allocated participants.
     * @param participant1 the first participant
     * @param participant2 the second participant
     */
    public SwitchVideoTest(Participant participant1, Participant participant2)
    {
        this.participant1 = participant1;
        this.participant2 = participant2;
    }

    @Override
    public void setup()
    {
        super.setup();

        ensureTwoParticipants();
    }

    /**
     * Click on local video thumbnail and checks whether the large video
     * is the local one.
     */
    @Test
    public void ownerClickOnLocalVideoAndTest()
    {
        clickOnLocalVideoAndTest(participant1.getDriver());
    }

    /**
     * Click on local video thumbnail and checks whether the large video
     * is the local one.
     */
    private void clickOnLocalVideoAndTest(WebDriver driver)
    {
        MeetUIUtils.selectLocalVideo(driver);
    }

    /**
     * Clicks on the remote video thumbnail and checks whether the large video
     * is the remote one.
     */
    @Test(dependsOnMethods = { "ownerClickOnLocalVideoAndTest" })
    public void ownerClickOnRemoteVideoAndTest()
    {
        clickOnRemoteVideoAndTest(
            participant1.getDriver(),
            participant2.getDriver());
    }

    /**
     * Unpins remote video in owner and verifies if the operation has succeeded.
     */
    @Test(dependsOnMethods = { "ownerClickOnRemoteVideoAndTest" })
    public void ownerUnpinRemoteVideoAndTest()
    {
        unpinRemoteVideoAndTest(
            participant1.getDriver(),
            participant2.getDriver());
    }

    /**
     * Unpins remote video in the 2nd participant and verifies if the operation
     * has succeeded.
     */
    @Test(dependsOnMethods = { "participantClickOnRemoteVideoAndTest" })
    public void participantUnpinRemoteVideo()
    {
        unpinRemoteVideoAndTest(
            participant2.getDriver(),
            participant1.getDriver());
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
            pinnedThumb,
            "Pinned remote video not found for " + peerEndpointId);

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
     * @param where the driver to operate over.
     */
    public static void clickOnRemoteVideoAndTest(WebDriver where, WebDriver who)
    {
        MeetUIUtils.selectRemoteVideo(where, who);
    }

    /**
     * Click on local video thumbnail and checks whether the large video
     * is the local one.
     */
    @Test(dependsOnMethods = { "ownerUnpinRemoteVideoAndTest" })
    public void participantClickOnLocalVideoAndTest()
    {
        clickOnLocalVideoAndTest(participant2.getDriver());
    }

    /**
     * Clicks on the remote video thumbnail and checks whether the large video
     * is the remote one.
     */
    @Test(dependsOnMethods = { "participantClickOnLocalVideoAndTest" })
    public void participantClickOnRemoteVideoAndTest()
    {
        clickOnRemoteVideoAndTest(
            participant2.getDriver(),
            participant1.getDriver());
    }

}
