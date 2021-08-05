/*
 * Copyright @ 2017 Atlassian Pty Ltd
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


import org.jitsi.meet.test.pageobjects.web.AVModerationMenu;
import org.jitsi.meet.test.pageobjects.web.ParticipantsPane;
import org.jitsi.meet.test.util.MeetUIUtils;
import org.jitsi.meet.test.util.TestUtils;
import org.jitsi.meet.test.web.WebParticipant;
import org.jitsi.meet.test.web.WebTestBase;
import org.testng.annotations.Test;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Tests the A-V moderation functionality.
 *
 * @author Calin Chitu
 */
public class AudioVideoModerationTest extends WebTestBase
{
    /**
     * The participant.
     */
    private WebParticipant participant1;
    private WebParticipant participant2;
    private WebParticipant participant3;

    @Override
    public void setupClass()
    {
        super.setupClass();

        ensureThreeParticipants();
        participant1 = getParticipant1();
        participant2 = getParticipant2();
        participant3 = getParticipant3();
    }

    /**
     * Opens the context menu from the participants pane
     * and enables moderation
     */
    @Test
    public void AVModeration()
    {

        ParticipantsPane participantsPane = participant1.getParticipantsPane();
        AVModerationMenu avModerationMenu = participant1.getAVModerationMenu();

        participantsPane.open();

        assertTrue(participant1.isModerator(), "Participant 1 must be moderator");
        assertFalse(participant2.isModerator(), "Participant 2 must not be moderator");
        assertFalse(participant3.isModerator(), "Participant 3 must not be moderator");

        TestUtils.waitMillis(2000);

        participantsPane.clickContextMenuButton();

        checkModerationEnableDisable(participant2);

        avModerationMenu.clickStartModeration();

        startModerationForParticipant(participant3);

        checkAudioVideoParticipant(participant3);

        TestUtils.waitMillis(2000);

        participantsPane.clickContextMenuButton();

        avModerationMenu.clickStopModeration();

        TestUtils.waitMillis(2000);

        assertTrue(
                participant3.getNotifications().hasModerationStopNotification(),
                "The participant should see a notification that moderation stopped.");
    }

    /**
     * Checks moderation by enabling and disabling it
     * @param participant the participant to unmute
     */
    private void checkModerationEnableDisable(WebParticipant participant)
    {
        AVModerationMenu avModerationMenu = participant1.getAVModerationMenu();

        avModerationMenu.clickStartModeration();

        TestUtils.waitMillis(2000);

        checkAudioVideoParticipantMute(participant);

        avModerationMenu.clickStopModeration();

        TestUtils.waitMillis(2000);

        checkAudioVideoParticipantUnmute(participant);
    }

    /**
     * Participant raises hand to speak during moderation
     * @param participant the participant that wants to speak
     */
    private void raiseHandToSpeak(WebParticipant participant)
    {
        participant.getToolbar().clickAudioMuteButton();

        TestUtils.waitMillis(2000);

        assertTrue(
                participant.getNotifications().hasAudioModerationNotification(),
                "The participant should see a notification that has to raise his hand.");

        participant.getToolbar().clickRaiseHandButton();

        TestUtils.waitMillis(2000);

        assertTrue(
                participant1.getNotifications().hasRaisedHandNotification(),
                "The moderator should see a notification that a participant wants to unmute.");
    }

    /**
     * Moderator asks participant to unmute
     * @param participant the participant to unmute
     */
    private void askParticipantToUnmute(WebParticipant participant)
    {
        ParticipantsPane participantsPane = participant1.getParticipantsPane();

        participantsPane.askToUnmute(participant1);

        TestUtils.waitMillis(2000);

        assertTrue(
                participant.getNotifications().hasUnmuteNotification(),
                "The participant should see a notification that the moderator requests to unmute.");
    }

    /**
     * Checks audio/video mute state for participant
     * @param participant the participant to unmute
     */
    private void checkAudioVideoParticipantMute(WebParticipant participant)
    {
        MeetUIUtils.toggleAudioAndCheck(participant, participant1, true, false);

        MeetUIUtils.muteVideoAndCheck(participant, participant1);
    }

    /**
     * Checks audio/video unmute state for participant
     * @param participant the participant to unmute
     */
    private void checkAudioVideoParticipantUnmute(WebParticipant participant)
    {
        MeetUIUtils.toggleAudioAndCheck(participant, participant1, false, false);

        MeetUIUtils.unmuteVideoAndCheck(participant, participant1);
    }

    /**
     * Starts moderation for participant
     * @param participant the moderated participant
     */
    private void startModerationForParticipant(WebParticipant participant)
    {
        TestUtils.waitMillis(2000);

        assertTrue(
                participant.getNotifications().hasModerationStartNotification(),
                "The participant should see a notification that moderation started.");

        raiseHandToSpeak(participant);

        askParticipantToUnmute(participant);
    }

    /**
     * Checks audio/video state for participant
     * @param participant the participant that is checked
     */
    private void checkAudioVideoParticipant(WebParticipant participant)
    {
        TestUtils.waitMillis(2000);

        checkAudioVideoParticipantUnmute(participant);

        TestUtils.waitMillis(2000);

        checkAudioVideoParticipantMute(participant);
    }
}
