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
 * Tests switching between P2P and JVB connections.
 *
 * @author Pawel Domas
 */
public class Peer2PeerTest
    extends WebTestBase
{
    /**
     * The config fragment which enables P2P test mode.
     */
    public final static String MANUAL_P2P_MODE_FRAGMENT
        = "config.testing.p2pTestMode=true";

    /**
     * Mutes participant1 and checks at other participant is this is visible.
     */
    @Test
    public void testSwitchToP2P()
    {
        JitsiMeetUrl url
            = getJitsiMeetUrl().appendConfig("config.p2p.enabled=true");
        ensureTwoParticipants(url, url);

        WebDriver driver1 = getParticipant1().getDriver();
        WebDriver driver2 = getParticipant2().getDriver();
        String endpointId1 = getParticipant1().getEndpointId();
        String endpointId2 = getParticipant2().getEndpointId();

        MeetUtils.waitForP2PIceConnected(driver1);
        MeetUtils.waitForP2PIceConnected(driver2);

        // This kind of verifies stats
        // FIXME also verify resolutions that are not N/A
        getParticipant2().waitForSendReceiveData();
        getParticipant1().waitForSendReceiveData();

        // FIXME verify if video is displayed on the thumbnails ?

        // Verify video mute feature
        // Check participant1 mute
        MeetUIUtils.muteVideoAndCheck(getParticipant1(), getParticipant2());
        MeetUIUtils.verifyUserConnStatusIndication(
            driver2, endpointId1 , true);

        MeetUIUtils.unmuteVideoAndCheck(getParticipant1(), getParticipant2());
        // Participant conn status kind of tells if video is playing
        MeetUIUtils.verifyUserConnStatusIndication(
            driver2, endpointId1 , true);

        // Check participant mute
        // FIXME refactor to preserve the order of observer and subject
        MeetUIUtils.muteVideoAndCheck(getParticipant2(), getParticipant1());
        MeetUIUtils.verifyUserConnStatusIndication(
            driver1, endpointId2, true);

        MeetUIUtils.unmuteVideoAndCheck(getParticipant2(), getParticipant1());
        MeetUIUtils.verifyUserConnStatusIndication(
            driver1, endpointId2, true);
    }

    /**
     * FIXME this test fails randomly too often
     */
    //@Test(dependsOnMethods = { "testSwitchToP2P" })
    public void testSwitchBackToJVB()
    {
        hangUpAllParticipants();
        ensureThreeParticipants();

        WebDriver driver1 = getParticipant1().getDriver();
        WebDriver driver2 = getParticipant2().getDriver();
        WebDriver driver3 = getParticipant3().getDriver();
        String endpointId1 = getParticipant1().getEndpointId();
        String endpointId2 = getParticipant2().getEndpointId();
        String endpointId3 = getParticipant3().getEndpointId();

        MeetUtils.waitForP2PIceDisconnected(driver1);
        MeetUtils.waitForP2PIceDisconnected(driver2);
        MeetUtils.waitForP2PIceDisconnected(driver3);

        getParticipant1().waitForIceConnected();
        getParticipant1().waitForSendReceiveData();

        // During development I noticed that participants are disconnected
        MeetUIUtils.verifyUserConnStatusIndication(
            driver2, endpointId1, true);
        MeetUIUtils.verifyUserConnStatusIndicationLong(
            driver2, endpointId1 , true);
        MeetUIUtils.verifyUserConnStatusIndicationLong(
            driver3, endpointId1 , true);

        MeetUIUtils.verifyUserConnStatusIndicationLong(
            driver1, endpointId2, true);
        MeetUIUtils.verifyUserConnStatusIndicationLong(
            driver3, endpointId2, true);

        MeetUIUtils.verifyUserConnStatusIndicationLong(
            driver1, endpointId3, true);
        MeetUIUtils.verifyUserConnStatusIndicationLong(
            driver2, endpointId3, true);

    }

    @Test(dependsOnMethods = { "testSwitchToP2P" })
    public void testManualP2PSwitch()
    {
        hangUpAllParticipants();

        JitsiMeetUrl url
            = getJitsiMeetUrl().appendConfig(MANUAL_P2P_MODE_FRAGMENT);
        ensureTwoParticipants(url, url);

        Participant participant1 = getParticipant1();
        Participant participant2 = getParticipant2();
        WebDriver driver1 = participant1.getDriver();
        WebDriver driver2 = participant2.getDriver();
        String endpointId1 = participant1.getEndpointId();
        String endpointId2 = participant2.getEndpointId();

        // Start P2P once JVB connection is established
        participant1.waitForIceConnected();
        participant1.waitForSendReceiveData();

        // Check if not in P2P
        assertFalse(participant1.isP2pConnected());
        assertFalse(participant2.isP2pConnected());
        // Start P2P
        //FIXME make throw error if 3 persons in the room
        MeetUtils.startP2P(driver1);

        MeetUtils.waitForP2PIceConnected(driver1);
        MeetUtils.waitForP2PIceConnected(driver2);

        // This kind of verifies stats
        // FIXME also verify resolutions that are not N/A
        participant1.waitForSendReceiveData();
        participant2.waitForSendReceiveData();

        // FIXME verify if video is displayed on the thumbnails ?
        MeetUIUtils.verifyUserConnStatusIndicationLong(
            driver2, endpointId1 , true);
        MeetUIUtils.verifyUserConnStatusIndicationLong(
            driver1, endpointId2, true);

        // Verify that the audio is coming through
        MeetUIUtils.waitForAudioMuted(
            driver1, driver2, "participant2", false);
        MeetUIUtils.waitForAudioMuted(
            driver2, driver1, "participant1", false);

        // Now stop the P2P and see if it goes back to P2P
        MeetUtils.stopP2P(driver1);

        MeetUtils.waitForP2PIceDisconnected(driver1);
        MeetUtils.waitForP2PIceDisconnected(driver2);

        participant1.waitForIceConnected();
        getParticipant2().waitForIceConnected();

        // FIXME verify if video is displayed on the thumbnails ?
        MeetUIUtils.verifyUserConnStatusIndicationLong(
            driver2, endpointId1 , true);
        MeetUIUtils.verifyUserConnStatusIndicationLong(
            driver1, endpointId2, true);

        // Verify video mute feature
        // Check participant1 mute
        MeetUIUtils.muteVideoAndCheck(getParticipant1(), getParticipant2());
        MeetUIUtils.verifyUserConnStatusIndicationLong(
            driver2, endpointId1 , true);

        MeetUIUtils.unmuteVideoAndCheck(getParticipant1(), getParticipant2());
        // Wait for connection to restore
        MeetUIUtils.verifyUserConnStatusIndication(
            driver2, endpointId1, true);
        // Participant conn status kind of tells if video is playing
        MeetUIUtils.verifyUserConnStatusIndicationLong(
            driver2, endpointId1 , true);

        // Check participant mute
        // FIXME refactor to preserve the order of observer and subject
        MeetUIUtils.muteVideoAndCheck(getParticipant2(), getParticipant1());
        MeetUIUtils.verifyUserConnStatusIndicationLong(
            driver1, endpointId2, true);

        MeetUIUtils.unmuteVideoAndCheck(getParticipant2(), getParticipant1());
        MeetUIUtils.verifyUserConnStatusIndication(
            driver1, endpointId2, true);
        MeetUIUtils.verifyUserConnStatusIndicationLong(
            driver1, endpointId2, true);
    }

    @Test(dependsOnMethods = { "testManualP2PSwitch" })
    public void testP2PSwitchWhenMuted()
    {
        hangUpAllParticipants();

        JitsiMeetUrl url
            = getJitsiMeetUrl().appendConfig(MANUAL_P2P_MODE_FRAGMENT);
        ensureTwoParticipants(url, url);

        Participant participant1 = getParticipant1();
        Participant participant2 = getParticipant2();
        WebDriver driver1 = getParticipant1().getDriver();
        WebDriver driver2 = getParticipant2().getDriver();
        String endpointId1 = participant1.getEndpointId();
        String endpointId2 = participant2.getEndpointId();

        participant1.waitForIceConnected();
        participant1.waitForSendReceiveData();

        // Check if not in P2P
        assertFalse(participant1.isP2pConnected());
        assertFalse(participant2.isP2pConnected());

        // Video mute participant1
        // Verify video mute feature
        // Check participant1 mute
        MeetUIUtils.muteVideoAndCheck(getParticipant1(), getParticipant2());
        MeetUIUtils.verifyUserConnStatusIndicationLong(
            driver2, endpointId1 , true);

        // Start P2P
        //FIXME make throw error if 3 persons in the room
        MeetUtils.startP2P(driver1);

        MeetUtils.waitForP2PIceConnected(driver1);
        MeetUtils.waitForP2PIceConnected(driver2);

        // This kind of verifies stats
        // FIXME also verify resolutions that are not N/A
        participant1.waitForSendReceiveData();
        participant2.waitForSendReceiveData();

        // Verify that the audio is coming through
        MeetUIUtils.waitForAudioMuted(driver1, driver2, "participant2", false);
        MeetUIUtils.waitForAudioMuted(driver2, driver1, "participant1", false);

        // Unmute and see if that works
        MeetUIUtils.unmuteVideoAndCheck(getParticipant1(), getParticipant2());
        // Wait for connection to restore b
        // Participant conn status kind of tells if video is playing
        MeetUIUtils.verifyUserConnStatusIndicationLong(
            driver2, endpointId1 , true);


        // Now stop the P2P and see if it goes back to P2P
        MeetUtils.stopP2P(driver1);

        MeetUtils.waitForP2PIceDisconnected(driver1);
        MeetUtils.waitForP2PIceDisconnected(driver2);

        getParticipant1().waitForIceConnected();
        getParticipant2().waitForIceConnected();

        // FIXME verify if video is displayed on the thumbnails ?
        MeetUIUtils.verifyUserConnStatusIndicationLong(
            driver2, endpointId1 , true);
        MeetUIUtils.verifyUserConnStatusIndicationLong(
            driver1, endpointId2, true);

        // Verify video mute feature
        // Check participant1 mute
        MeetUIUtils.muteVideoAndCheck(getParticipant1(), getParticipant2());
        MeetUIUtils.verifyUserConnStatusIndicationLong(
            driver2, endpointId1 , true);

        MeetUIUtils.unmuteVideoAndCheck(getParticipant1(), getParticipant2());
        // Wait for connection to restore
        MeetUIUtils.verifyUserConnStatusIndication(
            driver2, endpointId1, true);
        // Participant conn status kind of tells if video is playing
        MeetUIUtils.verifyUserConnStatusIndicationLong(
            driver2, endpointId1 , true);

        // Check participant mute
        // FIXME refactor to preserve the order of observer and subject
        MeetUIUtils.muteVideoAndCheck(getParticipant2(), getParticipant1());
        MeetUIUtils.verifyUserConnStatusIndicationLong(
            driver1, endpointId2, true);

        MeetUIUtils.unmuteVideoAndCheck(getParticipant2(), getParticipant1());
        MeetUIUtils.verifyUserConnStatusIndication(
            driver1, endpointId2, true);
        MeetUIUtils.verifyUserConnStatusIndicationLong(
            driver1, endpointId2, true);
    }
}
