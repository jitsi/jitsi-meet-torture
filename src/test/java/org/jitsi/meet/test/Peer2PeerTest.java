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
 * Tests switching between P2P and JVB connections.
 *
 * @author Pawel Domas
 */
public class Peer2PeerTest
    extends TestCase
{
    /**
     * Constructs test
     * @param name the method name for the test.
     */
    public Peer2PeerTest(String name)
    {
        super(name);
    }

    /**
     * Orders the tests.
     * @return the suite with order tests.
     */
    public static Test suite()
    {
        TestSuite suite = new TestSuite();

        suite.addTest(new Peer2PeerTest("disposeIfAny"));
        suite.addTest(new Peer2PeerTest("testSwitchToP2P"));
        suite.addTest(new Peer2PeerTest("testSwitchBackToJVB"));
        suite.addTest(new Peer2PeerTest("disposeIfAny"));
        suite.addTest(new Peer2PeerTest("testManualP2PSwitch"));
        suite.addTest(new Peer2PeerTest("disposeIfAny"));
        suite.addTest(new Peer2PeerTest("testP2PSwitchWhenMuted"));
        suite.addTest(new DisposeConference("testDispose"));

        return suite;
    }

    /**
     * Mutes the owner and checks at other participant is this is visible.
     */
    public void disposeIfAny()
    {
        // secondParticipant
        WebDriver participant
            = ConferenceFixture.getSecondParticipantInstance();

        if(participant != null)
            ConferenceFixture.quit(participant);

        // thirdParticipant
        participant = ConferenceFixture.getThirdParticipantInstance();
        if(participant != null)
            ConferenceFixture.quit(participant);

        // owner
        participant = ConferenceFixture.getOwnerInstance();
        if (participant != null)
            ConferenceFixture.quit(participant);
    }

    /**
     * Mutes the owner and checks at other participant is this is visible.
     */
    public void testSwitchToP2P()
    {
        ConferenceFixture.startOwner("config.p2p.enabled=true");
        ConferenceFixture.startSecondParticipant("config.p2p.enabled=true");

        ConferenceFixture.waitForOwnerToJoinMUC();
        MeetUtils.waitForParticipantToJoinMUC(
            ConferenceFixture.getSecondParticipant());

        WebDriver owner = ConferenceFixture.getOwner();
        WebDriver participant = ConferenceFixture.getSecondParticipant();

        MeetUtils.waitForP2PIceConnected(owner);
        MeetUtils.waitForP2PIceConnected(participant);

        // This kind of verifies stats
        // FIXME also verify resolutions that are not N/A
        MeetUtils.waitForSendReceiveData(participant);
        MeetUtils.waitForSendReceiveData(owner);

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

    public void testSwitchBackToJVB()
    {
        ConferenceFixture.waitForThirdParticipantToConnect();

        WebDriver owner = ConferenceFixture.getOwner();
        WebDriver second = ConferenceFixture.getSecondParticipantInstance();
        WebDriver third = ConferenceFixture.getThirdParticipantInstance();

        MeetUtils.waitForP2PIceDisconnected(owner);
        MeetUtils.waitForP2PIceDisconnected(second);
        MeetUtils.waitForP2PIceDisconnected(third);

        MeetUtils.waitForIceConnected(owner);
        MeetUtils.waitForIceConnected(second);
        MeetUtils.waitForIceConnected(third);

        MeetUtils.waitForSendReceiveData(owner);
        MeetUtils.waitForSendReceiveData(second);
        MeetUtils.waitForSendReceiveData(third);

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

    public void testManualP2PSwitch()
    {
        ConferenceFixture.startOwner("config.p2p.enabled=false");
        ConferenceFixture.startSecondParticipant("config.p2p.enabled=false");

        ConferenceFixture.waitForOwnerToJoinMUC();
        MeetUtils.waitForParticipantToJoinMUC(
            ConferenceFixture.getSecondParticipant());

        WebDriver owner = ConferenceFixture.getOwner();
        WebDriver participant = ConferenceFixture.getSecondParticipant();

        // Start P2P once JVB connection is established
        MeetUtils.waitForIceConnected(owner);
        MeetUtils.waitForIceConnected(participant);
        MeetUtils.waitForSendReceiveData(owner);
        MeetUtils.waitForSendReceiveData(participant);
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
        MeetUtils.waitForSendReceiveData(owner);
        MeetUtils.waitForSendReceiveData(participant);

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

        MeetUtils.waitForIceConnected(owner);
        MeetUtils.waitForIceConnected(participant);

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

    public void testP2PSwitchWhenMuted()
    {
        ConferenceFixture.startOwner("config.p2p.enabled=false");
        ConferenceFixture.startSecondParticipant("config.p2p.enabled=false");

        ConferenceFixture.waitForOwnerToJoinMUC();
        MeetUtils.waitForParticipantToJoinMUC(
            ConferenceFixture.getSecondParticipant());

        WebDriver owner = ConferenceFixture.getOwner();
        WebDriver participant = ConferenceFixture.getSecondParticipant();

        MeetUtils.waitForIceConnected(owner);
        MeetUtils.waitForIceConnected(participant);
        MeetUtils.waitForSendReceiveData(owner);
        MeetUtils.waitForSendReceiveData(participant);
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
        MeetUtils.waitForSendReceiveData(owner);
        MeetUtils.waitForSendReceiveData(participant);

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

        MeetUtils.waitForIceConnected(owner);
        MeetUtils.waitForIceConnected(participant);

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
