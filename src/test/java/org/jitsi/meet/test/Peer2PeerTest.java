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
    private final static String MANUAL_P2P_MODE_FRAGMENT
        = "config.testing.p2pTestMode=true";

    /**
     * Mutes the owner and checks at other participant is this is visible.
     */
    @Test
    public void testSwitchToP2P()
    {
        ensureOneParticipant("config.p2p.enabled=true");
        ensureTwoParticipants("config.p2p.enabled=true");

        WebDriver owner = getParticipant1().getDriver();
        WebDriver participant = getParticipant2().getDriver();

        MeetUtils.waitForP2PIceConnected(owner);
        MeetUtils.waitForP2PIceConnected(participant);

        // This kind of verifies stats
        // FIXME also verify resolutions that are not N/A
        getParticipant2().waitForSendReceiveData();
        getParticipant1().waitForSendReceiveData();

        // FIXME verify if video is displayed on the thumbnails ?

        // Verify video mute feature
        // Check owner mute
        MeetUIUtils.muteVideoAndCheck(owner, participant);
        MeetUIUtils.verifyUserConnStatusIndication(
            participant, owner , true);

        MeetUIUtils.unmuteVideoAndCheck(owner, participant);
        // Participant conn status kind of tells if video is playing
        MeetUIUtils.verifyUserConnStatusIndication(
            participant, owner , true);

        // Check participant mute
        // FIXME refactor to preserve the order of observer and subject
        MeetUIUtils.muteVideoAndCheck(participant, owner);
        MeetUIUtils.verifyUserConnStatusIndication(
            owner, participant, true);

        MeetUIUtils.unmuteVideoAndCheck(participant, owner);
        MeetUIUtils.verifyUserConnStatusIndication(
            owner, participant, true);
    }

    /**
     * FIXME this test fails randomly too often
     */
    //@Test(dependsOnMethods = { "testSwitchToP2P" })
    public void testSwitchBackToJVB()
    {
        hangUpAllParticipants();
        ensureThreeParticipants();

        WebDriver owner = getParticipant1().getDriver();
        WebDriver second = getParticipant2().getDriver();
        WebDriver third = getParticipant3().getDriver();

        MeetUtils.waitForP2PIceDisconnected(owner);
        MeetUtils.waitForP2PIceDisconnected(second);
        MeetUtils.waitForP2PIceDisconnected(third);

        getParticipant1().waitForIceConnected();
        getParticipant1().waitForSendReceiveData();

        // During development I noticed that peers are disconnected
        MeetUIUtils.verifyUserConnStatusIndication(second, owner, true);
        MeetUIUtils.verifyUserConnStatusIndicationLong(
            second, owner , true);
        MeetUIUtils.verifyUserConnStatusIndicationLong(
            third, owner , true);

        MeetUIUtils.verifyUserConnStatusIndicationLong(
            owner, second, true);
        MeetUIUtils.verifyUserConnStatusIndicationLong(
            third, second, true);

        MeetUIUtils.verifyUserConnStatusIndicationLong(
            owner, third, true);
        MeetUIUtils.verifyUserConnStatusIndicationLong(
            second, third, true);

    }

    @Test(dependsOnMethods = { "testSwitchToP2P" })
    public void testManualP2PSwitch()
    {
        hangUpAllParticipants();
        ensureOneParticipant(MANUAL_P2P_MODE_FRAGMENT);
        ensureTwoParticipants(MANUAL_P2P_MODE_FRAGMENT);

        WebDriver owner = getParticipant1().getDriver();
        WebDriver participant = getParticipant2().getDriver();

        // Start P2P once JVB connection is established
        getParticipant1().waitForIceConnected();
        getParticipant1().waitForSendReceiveData();

        // Check if not in P2P
        assertFalse(MeetUtils.isP2PConnected(owner));
        assertFalse(MeetUtils.isP2PConnected(owner));
        // Start P2P
        //FIXME make throw error if 3 persons in the room
        MeetUtils.startP2P(owner);

        MeetUtils.waitForP2PIceConnected(owner);
        MeetUtils.waitForP2PIceConnected(participant);

        // This kind of verifies stats
        // FIXME also verify resolutions that are not N/A
        getParticipant1().waitForSendReceiveData();
        getParticipant2().waitForSendReceiveData();

        // FIXME verify if video is displayed on the thumbnails ?
        MeetUIUtils.verifyUserConnStatusIndicationLong(
            participant, owner , true);
        MeetUIUtils.verifyUserConnStatusIndicationLong(
            owner, participant, true);

        // Verify that the audio is coming through
        MeetUIUtils.waitForAudioMuted(owner, participant, "2nd peer", false);
        MeetUIUtils.waitForAudioMuted(participant, owner, "owner", false);

        // Now stop the P2P and see if it goes back to P2P
        MeetUtils.stopP2P(owner);

        MeetUtils.waitForP2PIceDisconnected(owner);
        MeetUtils.waitForP2PIceDisconnected(participant);

        getParticipant1().waitForIceConnected();
        getParticipant2().waitForIceConnected();

        // FIXME verify if video is displayed on the thumbnails ?
        MeetUIUtils.verifyUserConnStatusIndicationLong(
            participant, owner , true);
        MeetUIUtils.verifyUserConnStatusIndicationLong(
            owner, participant, true);

        // Verify video mute feature
        // Check owner mute
        MeetUIUtils.muteVideoAndCheck(owner, participant);
        MeetUIUtils.verifyUserConnStatusIndicationLong(
            participant, owner , true);

        MeetUIUtils.unmuteVideoAndCheck(owner, participant);
        // Wait for connection to restore
        MeetUIUtils.verifyUserConnStatusIndication(
            participant, owner, true);
        // Participant conn status kind of tells if video is playing
        MeetUIUtils.verifyUserConnStatusIndicationLong(
            participant, owner , true);

        // Check participant mute
        // FIXME refactor to preserve the order of observer and subject
        MeetUIUtils.muteVideoAndCheck(participant, owner);
        MeetUIUtils.verifyUserConnStatusIndicationLong(
            owner, participant, true);

        MeetUIUtils.unmuteVideoAndCheck(participant, owner);
        MeetUIUtils.verifyUserConnStatusIndication(owner, participant, true);
        MeetUIUtils.verifyUserConnStatusIndicationLong(
            owner, participant, true);
    }

    @Test(dependsOnMethods = { "testManualP2PSwitch" })
    public void testP2PSwitchWhenMuted()
    {
        hangUpAllParticipants();
        ensureOneParticipant(MANUAL_P2P_MODE_FRAGMENT);
        ensureTwoParticipants(MANUAL_P2P_MODE_FRAGMENT);

        WebDriver owner = getParticipant1().getDriver();
        WebDriver participant = getParticipant2().getDriver();

        getParticipant1().waitForIceConnected();
        getParticipant1().waitForSendReceiveData();

        // Check if not in P2P
        assertFalse(MeetUtils.isP2PConnected(owner));
        assertFalse(MeetUtils.isP2PConnected(owner));

        // Video mute owner
        // Verify video mute feature
        // Check owner mute
        MeetUIUtils.muteVideoAndCheck(owner, participant);
        MeetUIUtils.verifyUserConnStatusIndicationLong(
            participant, owner , true);

        // Start P2P
        //FIXME make throw error if 3 persons in the room
        MeetUtils.startP2P(owner);

        MeetUtils.waitForP2PIceConnected(owner);
        MeetUtils.waitForP2PIceConnected(participant);

        // This kind of verifies stats
        // FIXME also verify resolutions that are not N/A
        getParticipant1().waitForSendReceiveData();
        getParticipant2().waitForSendReceiveData();

        // Verify that the audio is coming through
        MeetUIUtils.waitForAudioMuted(owner, participant, "2nd peer", false);
        MeetUIUtils.waitForAudioMuted(participant, owner, "owner", false);

        // Unmute and see if that works
        MeetUIUtils.unmuteVideoAndCheck(owner, participant);
        // Wait for connection to restore b
        // Participant conn status kind of tells if video is playing
        MeetUIUtils.verifyUserConnStatusIndicationLong(
            participant, owner , true);


        // Now stop the P2P and see if it goes back to P2P
        MeetUtils.stopP2P(owner);

        MeetUtils.waitForP2PIceDisconnected(owner);
        MeetUtils.waitForP2PIceDisconnected(participant);

        getParticipant1().waitForIceConnected();
        getParticipant2().waitForIceConnected();

        // FIXME verify if video is displayed on the thumbnails ?
        MeetUIUtils.verifyUserConnStatusIndicationLong(
            participant, owner , true);
        MeetUIUtils.verifyUserConnStatusIndicationLong(
            owner, participant, true);

        // Verify video mute feature
        // Check owner mute
        MeetUIUtils.muteVideoAndCheck(owner, participant);
        MeetUIUtils.verifyUserConnStatusIndicationLong(
            participant, owner , true);

        MeetUIUtils.unmuteVideoAndCheck(owner, participant);
        // Wait for connection to restore
        MeetUIUtils.verifyUserConnStatusIndication(participant, owner, true);
        // Participant conn status kind of tells if video is playing
        MeetUIUtils.verifyUserConnStatusIndicationLong(
            participant, owner , true);

        // Check participant mute
        // FIXME refactor to preserve the order of observer and subject
        MeetUIUtils.muteVideoAndCheck(participant, owner);
        MeetUIUtils.verifyUserConnStatusIndicationLong(
            owner, participant, true);

        MeetUIUtils.unmuteVideoAndCheck(participant, owner);
        MeetUIUtils.verifyUserConnStatusIndication(owner, participant, true);
        MeetUIUtils.verifyUserConnStatusIndicationLong(
            owner, participant, true);
    }
}
