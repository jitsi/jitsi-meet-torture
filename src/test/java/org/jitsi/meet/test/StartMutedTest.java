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
import org.jitsi.meet.test.pageobjects.web.*;
import org.jitsi.meet.test.util.*;
import org.jitsi.meet.test.web.*;

import org.testng.annotations.*;

/**
 * Start muted tests
 * @author Hristo Terezov
 * @author Pawel Domas
 */
public class StartMutedTest
    extends WebTestBase
{
    /**
     * Restarts the second participant tab and checks start muted checkboxes.
     * Test if the second participant is muted and participant1 is unmuted.
     */
    @Test
    public void checkboxesTest()
    {
        JitsiMeetUrl url = getJitsiMeetUrl().appendConfig("config.p2p.enabled=true");

        ensureOneParticipant(url);

        WebParticipant participant1 = getParticipant1();

        // Disable this test on Firefox becasue of a browser bug where media stops intermittently.
        if (participant1.getType().isFirefox())
        {
            return;
        }

        participant1.getToolbar().clickSettingsButton();

        SettingsDialog settingsDialog = participant1.getSettingsDialog();
        settingsDialog.waitForDisplay();
        settingsDialog.setStartAudioMuted(true);
        settingsDialog.setStartVideoMuted(true);
        settingsDialog.submit();

        WebParticipant participant2 = joinSecondParticipant(url);
        participant2.waitToJoinMUC();
        participant2.waitForIceConnected();
        participant2.waitForSendReceiveData(false, true);

        participant2.getFilmstrip().assertAudioMuteIcon(participant2, true);
        participant2.getParticipantsPane().assertIsParticipantVideoMuted(participant2, true);

        MeetUIUtils.waitForAudioMuted(
                participant1.getDriver(),
                participant2.getDriver(),
                "participant2",
                true);

        participant2.getFilmstrip().assertAudioMuteIcon(participant1, false);
        participant2.getParticipantsPane().assertIsParticipantVideoMuted(participant1, false);

        // Enable video on p2 and check if p2 appears unmuted on p1.
        participant2.getToolbar().clickAudioUnmuteButton();
        participant2.getToolbar().clickVideoUnmuteButton();

        participant2.getFilmstrip().assertAudioMuteIcon(participant2, false);
        participant2.getParticipantsPane().assertIsParticipantVideoMuted(participant2, false);

        MeetUIUtils.waitForAudioMuted(
                participant1.getDriver(),
                participant2.getDriver(),
                "participant2",
                false);

        // Add a third participant and check p3 is able to receive audio and video from p2.
        WebParticipant participant3 = joinThirdParticipant(url, null);
        participant3.waitToJoinMUC();
        participant3.waitForIceConnected();
        participant3.waitForSendReceiveData(false, true);

        participant3.getFilmstrip().assertAudioMuteIcon(participant2, false);
        participant3.getParticipantsPane().assertIsParticipantVideoMuted(participant2, false);
    }

    /**
     * Opens new room and sets start muted config parameters trough the URL.
     * Test if the second participant is muted and participant1 is unmuted.
     */
    @Test(dependsOnMethods = { "checkboxesTest" })
    public void configOptionsTest()
    {
        hangUpAllParticipants();

        ensureOneParticipant(getJitsiMeetUrl()
                        .appendConfig("config.startAudioMuted=2")
                        .appendConfig("config.debugAudioLevels=true")
                        .appendConfig("config.startVideoMuted=2"));

        WebParticipant participant2 = joinSecondParticipant();
        participant2.waitToJoinMUC();
        participant2.waitForIceConnected();
        participant2.waitForSendReceiveData(false, true);

        WebParticipant participant3 = joinThirdParticipant();
        participant3.waitToJoinMUC();
        participant3.waitForIceConnected();
        participant3.waitForSendReceiveData(false, true);

        WebParticipant participant1 = getParticipant1();
        participant1.waitForIceConnected();

        consolePrint(participant1,
            "Start configOptionsTest, second participant: " + participant2.getEndpointId());

        // On the PR testing machine it seems that some audio is leaking before
        // we mute. The audio is muted when 'session-initiate' is received, but
        // seems like a bit of sound goes through in random cases. Let's wait
        // here a bit, before checking the audio levels.
        TestUtils.waitMillis(500);

        // Participant 3 should be muted, 1 and 2 unmuted.
        participant3.getFilmstrip().assertAudioMuteIcon(participant3, true);
        participant3.getParticipantsPane().assertIsParticipantVideoMuted(participant3, true);

        MeetUIUtils.waitForAudioMuted(
                participant1.getDriver(),
                participant3.getDriver(),
                "participant3",
                true);
        MeetUIUtils.waitForAudioMuted(
                participant2.getDriver(),
                participant3.getDriver(),
                "participant3",
                true);

        participant3.getFilmstrip().assertAudioMuteIcon(participant1, false);
        participant3.getFilmstrip().assertAudioMuteIcon(participant2, false);
        participant3.getParticipantsPane().assertIsParticipantVideoMuted(participant1, false);
        participant3.getParticipantsPane().assertIsParticipantVideoMuted(participant2, false);

        // Unmute and see if the audio works
        participant3.getToolbar().clickAudioUnmuteButton();
        consolePrint(participant1,
            "configOptionsTest, unmuted third participant");
        MeetUIUtils.waitForAudioMuted(
            participant1.getDriver(),
            participant3.getDriver(),
            "participant3",
            false /* unmuted */);
    }

    /**
     * Join the conference while video muted and unmute the second participant.
     */
    @Test(dependsOnMethods = { "configOptionsTest" })
    public void startWithVideoMutedCanUnmute()
    {
        WebParticipant participant1 = getParticipant1();
        WebParticipant participant2 = getParticipant2();

        ParticipantType participant1Type = participant1.getType();
        ParticipantType participant2Type = participant2.getType();


        // Firefox is known to have problems unmuting when starting muted.
        // Safari does not support video.
        if (participant1Type.isFirefox()
            || participant2Type.isFirefox()
            || participant1Type.isSafari()
            || participant2Type.isSafari())
        {
            return;
        }

        hangUpAllParticipants();

        // Explicitly enable P2P due to a regression with unmute not updating
        // large video while in P2P.
        JitsiMeetUrl url
            = getJitsiMeetUrl().appendConfig(
                "config.startWithVideoMuted=true&" +
                    "config.p2p.enabled=true");
        ensureTwoParticipants(url, url);

        participant1.getParticipantsPane().assertIsParticipantVideoMuted(participant2, true);

        participant2.getParticipantsPane().assertIsParticipantVideoMuted(participant1, true);

        MeetUIUtils.waitsForLargeVideoSwitch(
            participant1.getDriver(),
            participant2.getEndpointId());

        MeetUIUtils.waitsForLargeVideoSwitch(
            participant2.getDriver(),
            participant1.getEndpointId());

        MeetUIUtils.unmuteVideoAndCheck(participant2, participant1);

        participant1.getLargeVideo().isVideoPlaying();
    }

    /**
     * Join the conference while audio muted and unmute the second participant.
     */
    @Test(dependsOnMethods = { "startWithVideoMutedCanUnmute" })
    public void startWithAudioMutedCanUnmute()
    {
        hangUpAllParticipants();

        JitsiMeetUrl meetUrl = getJitsiMeetUrl().appendConfig(
                "config.startWithAudioMuted=true");

        // Do not use ensureTwoParticipants() because it checks for send/rec
        // bitrate which should be 0 if both participants start audio muted.
        ensureOneParticipant(meetUrl);

        WebParticipant participant1 = getParticipant1();
        WebParticipant participant2 = joinSecondParticipant(meetUrl);

        participant2.waitToJoinMUC();
        participant2.waitForIceConnected();

        MeetUIUtils.waitForAudioMuted(
            participant1.getDriver(),
            participant2.getDriver(),
            "participant2",
            true);

        MeetUIUtils.waitForAudioMuted(
            participant2.getDriver(),
            participant1.getDriver(),
            "participant1",
            true);

        participant1.getToolbar().clickAudioUnmuteButton();

        MeetUIUtils.waitForAudioMuted(
            participant1.getDriver(),
            participant2.getDriver(),
            "participant2",
            true);

        MeetUIUtils.waitForAudioMuted(
            participant2.getDriver(),
            participant1.getDriver(),
            "participant1",
            false);
    }

    /**
     * Join the conference while both audio and video are muted, unmute both the participants.
     */
    @Test(dependsOnMethods = { "startWithAudioMutedCanUnmute" })
    public void startWithAudioVideoMutedCanUnmute()
    {
        hangUpAllParticipants();

        JitsiMeetUrl meetUrl = getJitsiMeetUrl().appendConfig(
                "config.startWithAudioMuted=true&" +
                "config.startWithVideoMuted=true&" +
                "config.p2p.enabled=true");

        // Do not use ensureTwoParticipants() because it checks for send/rec
        // bitrate which should be 0 if both participants start audio muted.
        ensureOneParticipant(meetUrl);

        WebParticipant participant1 = getParticipant1();
        WebParticipant participant2 = joinSecondParticipant(meetUrl);

        participant2.waitToJoinMUC();
        participant2.waitForIceConnected();

        MeetUIUtils.waitForAudioMuted(
            participant1.getDriver(),
            participant2.getDriver(),
            "participant2",
            true);

        MeetUIUtils.waitForAudioMuted(
            participant2.getDriver(),
            participant1.getDriver(),
            "participant1",
            true);

        participant1.getParticipantsPane().assertIsParticipantVideoMuted(participant2, true);

        participant2.getParticipantsPane().assertIsParticipantVideoMuted(participant1, true);

        // Unmute p1's both audio and video and check on p2.
        participant1.getToolbar().clickAudioUnmuteButton();
        MeetUIUtils.unmuteVideoAndCheck(participant1, participant2);

        participant2.getLargeVideo().isVideoPlaying();

        MeetUIUtils.waitForAudioMuted(
            participant2.getDriver(),
            participant1.getDriver(),
            "participant1",
            false);

        // Unmute p2's audio and video and check on p1.
        MeetUIUtils.unmuteVideoAndCheck(participant2, participant1);
        participant2.getToolbar().clickAudioUnmuteButton();

        participant1.getLargeVideo().isVideoPlaying();
        MeetUIUtils.waitForAudioMuted(
            participant1.getDriver(),
            participant2.getDriver(),
            "participant2",
            false);
    }

    @Test(dependsOnMethods = { "startWithAudioVideoMutedCanUnmute" })
    public void testp2pJvbSwitchAndSwitchBack()
    {
        JitsiMeetUrl meetUrl = getJitsiMeetUrl();

        WebParticipant participant1 = getParticipant1();
        WebParticipant participant2 = getParticipant2();

        // Mute p2's video just before p3 joins.
        participant2.getToolbar().clickVideoMuteButton();

        WebParticipant participant3 = joinThirdParticipant(meetUrl, null);
        participant3.waitToJoinMUC();
        participant3.waitForIceConnected();
        participant3.waitForSendReceiveData();
        
        // Unmute p2 and check if its video is being received by p1 and p3.
        participant2.getToolbar().clickVideoUnmuteButton();

        participant1.getParticipantsPane().assertIsParticipantVideoMuted(participant2, false);
        participant3.getParticipantsPane().assertIsParticipantVideoMuted(participant2, false);

        // Mute p2's video just before p3 leaves.
        participant2.getToolbar().clickVideoMuteButton();

        participant3.hangUp();

        participant1.getParticipantsPane().assertIsParticipantVideoMuted(participant2, true);
        participant2.getToolbar().clickVideoUnmuteButton();

        // Check if p2's video is playing on p1.
        participant1.getParticipantsPane().assertIsParticipantVideoMuted(participant2, false);
        participant1.getLargeVideo().isVideoPlaying();
    }
}
