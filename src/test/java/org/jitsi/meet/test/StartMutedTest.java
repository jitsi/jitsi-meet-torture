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

import org.openqa.selenium.*;
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
        ensureOneParticipant();

        // we are seeing some UI crashes when clicking too early on the page
        // and as it reloads, no logs are available to investigate
        TestUtils.waitMillis(1000);

        WebParticipant participant1 = getParticipant1();

        participant1.getToolbar().clickSettingsButton();

        SettingsDialog settingsDialog = participant1.getSettingsDialog();
        settingsDialog.waitForDisplay();
        settingsDialog.setStartAudioMuted(true);
        settingsDialog.setStartVideoMuted(true);
        settingsDialog.submit();

        WebParticipant participant2 = joinSecondParticipant();
        participant2.waitToJoinMUC();
        participant2.waitForIceConnected();
        participant2.waitForSendReceiveData(false, true);

        // On the PR testing machine it seems that some audio is leaking before
        // we mute. The audio is muted when 'session-initiate' is received, but
        // seems like a bit of sound goes through in random cases. Let's wait
        // here a bit, before checking the audio levels.
        TestUtils.waitMillis(500);
        checkParticipant2ForMute();
    }

    /**
     * Opens new room and sets start muted config parameters trough the URL.
     * Test if the second participant is muted and participant1 is unmuted.
     */
    @Test(dependsOnMethods = { "checkboxesTest" })
    public void configOptionsTest()
    {
        hangUpAllParticipants();

        ensureOneParticipant(getJitsiMeetUrl().appendConfig(
            "config.startAudioMuted=1&" +
                "config.debugAudioLevels=true&" +
                "config.startVideoMuted=1"));

        WebParticipant participant2 = joinSecondParticipant();
        participant2.waitToJoinMUC();
        participant2.waitForIceConnected();
        participant2.waitForSendReceiveData(false, true);

        WebParticipant participant1 = getParticipant1();

        final WebDriver driver2 = participant2.getDriver();
        consolePrint(participant1,
            "Start configOptionsTest, second participant: " + participant2.getEndpointId());

        participant1.waitForIceConnected();

        // On the PR testing machine it seems that some audio is leaking before
        // we mute. The audio is muted when 'session-initiate' is received, but
        // seems like a bit of sound goes through in random cases. Let's wait
        // here a bit, before checking the audio levels.
        TestUtils.waitMillis(500);

        checkParticipant2ForMute();

        // Unmute and see if the audio works
        participant2.getToolbar().clickAudioMuteButton();
        consolePrint(participant1,
            "configOptionsTest, unmuted second participant");
        MeetUIUtils.waitForAudioMuted(
            participant1.getDriver(),
            driver2,
            "participant2",
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

        /**
         * Firefox is known to have problems unmuting when starting muted.
         * Safari does not support video.
         */
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

        participant1.getToolbar().clickAudioMuteButton();

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
     * Tests if the second participant is muted and the first participant is
     * unmuted.
     */
    private void checkParticipant2ForMute()
    {
        WebParticipant participant1 = getParticipant1();
        WebParticipant participant2 = getParticipant2();

        getParticipant2().getFilmstrip()
            .assertAudioMuteIcon(getParticipant2(), true);
        getParticipant2().getParticipantsPane().assertIsParticipantVideoMuted(getParticipant2(), true);

        MeetUIUtils.waitForAudioMuted(
            participant1.getDriver(),
            participant2.getDriver(),
            "participant2",
            true);

        participant2.getFilmstrip().assertAudioMuteIcon(participant1, false);
        participant2.getParticipantsPane().assertIsParticipantVideoMuted(participant1, false);
    }
}
