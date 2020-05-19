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
import org.jitsi.meet.test.web.*;

import org.openqa.selenium.*;
import org.testng.annotations.*;

import static org.testng.Assert.*;

/**
 * Tests switching video of participants.
 *
 * @author Damian Minkov
 * @author Pawel Domas
 */
public class SwitchVideoTest
    extends WebTestBase
{
    /**
     * Default constructor.
     */
    public SwitchVideoTest()
    {}

    /**
     * Constructs SwitchVideoTest with already allocated participants.
     * @deprecated see
     * {@link AbstractBaseTest#AbstractBaseTest(AbstractBaseTest)}
     */
    public SwitchVideoTest(AbstractBaseTest baseTest)
    {
        super(baseTest);
    }

    @Override
    public void setupClass()
    {
        super.setupClass();

        ensureTwoParticipants();
    }

    /**
     * Click on local video thumbnail and checks whether the large video
     * is the local one.
     */
    @Test
    public void participant1ClickOnLocalVideoAndTest()
    {
        MeetUIUtils.selectLocalVideo(getParticipant1().getDriver());
    }

    /**
     * Clicks on the remote video thumbnail and checks whether the large video
     * is the remote one.
     */
    @Test(dependsOnMethods = {"participant1ClickOnLocalVideoAndTest"})
    public void participant1ClickOnRemoteVideoAndTest()
    {
        closeToolbarDialogs();

        MeetUIUtils.selectRemoteVideo(
            getParticipant1().getDriver(),
            getParticipant2().getEndpointId());
    }

    /**
     * Unpins remote video in participant1 and verifies that the operation
     * succeeded.
     */
    @Test(dependsOnMethods = {"participant1ClickOnRemoteVideoAndTest"})
    public void participant1UnpinRemoteVideoAndTest()
    {
        unpinRemoteVideoAndTest(
            getParticipant1(),
            getParticipant2().getEndpointId());
    }

    /**
     * Unpins remote video in participant2 and verifies that the operation
     * succeeded.
     */
    @Test(dependsOnMethods = { "participantClickOnRemoteVideoAndTest" })
    public void participant2UnpinRemoteVideo()
    {
        unpinRemoteVideoAndTest(
            getParticipant2(),
            getParticipant1().getEndpointId());
    }

    /**
     * Unpins the remote video with a given endpoint ID.
     *
     * @param participant the {@link Participant} which will do the unpinning.
     * @param endpointId the endpoint ID to unpin.
     */
    private void unpinRemoteVideoAndTest(Participant participant, String endpointId)
    {
        // Remote video with 'videoContainerFocused' is the pinned one
        String pinnedThumbXpath
            = "//span[ @id='participant_" + endpointId + "'" +
              "        and contains(@class,'videoContainerFocused') ]";

        WebElement pinnedThumb
            = participant.getDriver().findElement(By.xpath(pinnedThumbXpath));

        assertNotNull(
            pinnedThumb,
            "Pinned remote video not found for " + endpointId);

        // click on remote
        participant.getDriver().findElement(By.xpath(pinnedThumbXpath)).click();

        // Wait for the video to unpin
        try
        {
            TestUtils.waitForElementNotPresentByXPath(
                participant.getDriver(), pinnedThumbXpath, 2);
        }
        catch (TimeoutException exc)
        {
            fail("Failed to unpin video for " + endpointId);
        }
    }

    /**
     * Click on local video thumbnail and checks whether the large video
     * is the local one.
     */
    @Test(dependsOnMethods = {"participant1UnpinRemoteVideoAndTest"})
    public void participantClickOnLocalVideoAndTest()
    {
        MeetUIUtils.selectLocalVideo(getParticipant2().getDriver());
    }

    /**
     * Clicks on the remote video thumbnail and checks whether the large video
     * is the remote one.
     */
    @Test(dependsOnMethods = { "participantClickOnLocalVideoAndTest" })
    public void participantClickOnRemoteVideoAndTest()
    {
        MeetUIUtils.selectRemoteVideo(
            getParticipant2().getDriver(),
            getParticipant1().getEndpointId());
    }

    /**
     * Ensures all participants do not have toolbar related dialogs open that
     * could obstruct clicking of the filmstrip.
     */
    private void closeToolbarDialogs()
    {
        getParticipant1().getToolbar().closeOverflowMenu();
        getParticipant2().getToolbar().closeOverflowMenu();
    }
}
