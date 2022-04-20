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


import org.jitsi.meet.test.pageobjects.web.*;
import org.jitsi.meet.test.util.*;
import org.jitsi.meet.test.web.*;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;
import org.testng.*;
import org.testng.annotations.*;

/**
 * Tests the A-V moderation functionality.
 *
 * @author Calin Chitu
 */
public class AudioVideoModerationTest
    extends WebTestBase
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

        ensureOneParticipant();
        participant1 = getParticipant1();

        try
        {
            TestUtils.waitForCondition(participant1.getDriver(), 2,
                (ExpectedCondition<Boolean>) d -> participant1.isModerator());
        }
        catch(TimeoutException e)
        {
            cleanupClass();
            throw new SkipException("Skipping as anonymous participants are not moderators.");
        }

        ensureThreeParticipants();
        participant2 = getParticipant2();
        participant3 = getParticipant3();

        try
        {
            TestUtils.waitForCondition(participant1.getDriver(), 2,
                (ExpectedCondition<Boolean>) d
                    -> !(participant1.isModerator() && participant2.isModerator() && participant3.isModerator()));
        }
        catch(TimeoutException e)
        {
            cleanupClass();
            throw new SkipException("Skipping as all participants are moderators.");
        }
    }

    /**
     * Checks audio moderation by enabling and disabling it
     */
    @Test
    public void testCheckAudioModerationEnableDisable()
    {
        ParticipantsPane participantsPane = participant1.getParticipantsPane();
        AVModerationMenu avModerationMenu = participant1.getAVModerationMenu();

        participantsPane.open();

        participantsPane.clickContextMenuButton();

        avModerationMenu.clickStartAudioModeration();

        MeetUIUtils.toggleAudioAndCheck(participant3, participant1, true);

        participantsPane.clickContextMenuButton();

        avModerationMenu.clickStopAudioModeration();

        participantsPane.close();

        // we don't have a UI change when modertion is enabled/disabled, so let's just give it a second
        TestUtils.waitMillis(1000);

        MeetUIUtils.toggleAudioAndCheck(participant3, participant1, false);
    }

    /**
     * Checks video moderation by enabling and disabling it
     */
    @Test(dependsOnMethods = { "testCheckAudioModerationEnableDisable" })
    public void testCheckVideoModerationEnableDisable()
    {
        ParticipantsPane participantsPane = participant1.getParticipantsPane();
        AVModerationMenu avModerationMenu = participant1.getAVModerationMenu();

        participantsPane.open();

        participantsPane.clickContextMenuButton();

        avModerationMenu.clickStartVideoModeration();

        MeetUIUtils.toggleVideoAndCheck(participant3, participant1, true);

        participantsPane.clickContextMenuButton();

        avModerationMenu.clickStopVideoModeration();

        participantsPane.close();

        // we don't have a UI change when modertion is enabled/disabled, so let's just give it a second
        TestUtils.waitMillis(1000);

        MeetUIUtils.toggleVideoAndCheck(participant3, participant1, false);
    }

    /**
     * Checks user can unmute after being asked by moderator.
     */
    @Test(dependsOnMethods = { "testCheckVideoModerationEnableDisable" })
    public void testUnmuteByModerator()
    {
        unmuteByModerator(participant1, participant3, true, true);

        // we don't have a UI change when moderation is enabled/disabled, so let's just give it a second
        TestUtils.waitMillis(1000);

        // moderation is stopped at this point, make sure participants 1 & 2 are also unmuted,
        // participant3 was unmuted by unmuteByModerator
        MeetUIUtils.toggleAudioAndCheck(participant2, participant1, false);
        MeetUIUtils.unmuteVideoAndCheck(participant2, participant1);
        MeetUIUtils.toggleAudioAndCheck(participant1, participant2, false);
        MeetUIUtils.unmuteVideoAndCheck(participant1, participant2);
    }

    /**
     * Initial moderator reloads and next participant becomes the new moderator
     */
    @Test(dependsOnMethods = { "testUnmuteByModerator" })
    public void testHangUpAndChangeModerator()
    {
        participant2.hangUp();
        participant3.hangUp();

        ensureThreeParticipants();

        participant2.muteAudio(true);
        participant3.muteAudio(true);

        participant1.getParticipantsPane().open();

        participant1.getParticipantsPane().clickContextMenuButton();

        participant1.getAVModerationMenu().clickStartAudioModeration();

        participant1.getAVModerationMenu().clickStartVideoModeration();

        participant2.getToolbar().clickRaiseHandButton();

        participant3.getToolbar().clickRaiseHandButton();

        participant1.hangUp();
        joinFirstParticipant();

        // After p1 re-joins either p2 or p3 is promoted to moderator. They should still be muted.
        WebParticipant moderator = participant2.isModerator() ? participant2 : participant3;
        moderator.getParticipantsPane().assertIsParticipantVideoMuted(moderator, true);
        WebParticipant nonModerator = participant2.isModerator() ? participant3 : participant2;
        nonModerator.getParticipantsPane().assertIsParticipantVideoMuted(nonModerator, true);

        moderator.getParticipantsPane().open();
        moderator.getParticipantsPane().askToUnmute(nonModerator);
        nonModerator.getNotifications().getAskToUnmuteNotification();
        MeetUIUtils.toggleAudioAndCheck(nonModerator, participant1, false);
        MeetUIUtils.unmuteVideoAndCheck(nonModerator, participant1);
        moderator.getParticipantsPane().clickContextMenuButton();
        moderator.getAVModerationMenu().clickStopAudioModeration();
        moderator.getAVModerationMenu().clickStopVideoModeration();
        moderator.getParticipantsPane().close();
    }

    /**
     * Test that after granting moderator to a participant, that new moderator can ask to unmute.
     */
    @Test(dependsOnMethods = { "testHangUpAndChangeModerator" })
    public void testGrantModerator()
    {
        hangUpAllParticipants();

        ensureThreeParticipants();

        ParticipantsPane participantsPane = participant1.getParticipantsPane();
        AVModerationMenu avModerationMenu = participant1.getAVModerationMenu();

        participantsPane.open();

        participantsPane.clickContextMenuButton();

        avModerationMenu.clickStartAudioModeration();

        avModerationMenu.clickStartVideoModeration();

        participantsPane.close();

        participant1
            .getRemoteParticipantById(participant3.getEndpointId())
            .grantModerator();

        participant3.waitToBecomeModerator();

        unmuteByModerator(participant3, participant2, false, true);
    }

    /**
     * Test that Ask to Unmute works with moderation off.
     */
    @Test(dependsOnMethods = { "testGrantModerator" })
    public void testAskToUnmute()
    {
        hangUpAllParticipants();

        ensureTwoParticipants();

        WebDriver driver2 = participant2.getDriver();
        ParticipantsPane participantsPane = participant1.getParticipantsPane();

        // mute participant2
        participant2.getToolbar().clickAudioMuteButton();

        // ask participant2 to unmute
        participantsPane.open();

        participantsPane.askToUnmute(participant2);

        TestUtils.waitForCondition(driver2, 5, (ExpectedCondition<Boolean>) d ->
                participant2.getNotifications().hasAskToUnmuteNotification());

        participant2.getNotifications().closeAskToUnmuteNotification();

        participantsPane.close();
    }

    /**
     * Test that mute removes participant from whitelist.
     */
    @Test(dependsOnMethods = { "testAskToUnmute" })
    public void testRemoveFromWhitelist()
    {
        unmuteByModerator(participant1, participant2, true, false);

        ParticipantsPane participantsPane = participant1.getParticipantsPane();

        // mute audio and check
        participantsPane.muteParticipant(participant2);

        participant1.getFilmstrip().assertAudioMuteIcon(participant2, true);
        participant2.getFilmstrip().assertAudioMuteIcon(participant2, true);

        // make sure we close the notifications to avoid covering the mute button
        participant2.getNotifications().closeRemoteMuteNotification();

        // we try to unmute and test it that it was still muted
        MeetUIUtils.toggleAudioAndCheck(participant2, participant1, true);

        // stop video and check
        participant1.getRemoteParticipantById(participant2.getEndpointId()).stopVideo();

        participant1.getParticipantsPane().assertIsParticipantVideoMuted(participant2, true);
        participant2.getParticipantsPane().assertIsParticipantVideoMuted(participant2, true);

        participant2.getNotifications().closeRemoteVideoMuteNotification();

        MeetUIUtils.toggleVideoAndCheck(participant2, participant1, true);
    }

    /**
     * Test that checks join after moderation started.
     */
    @Test(dependsOnMethods = { "testRemoveFromWhitelist" })
    public void testJoinModerated()
    {
        hangUpAllParticipants();

        ensureOneParticipant();

        //enable moderation
        ParticipantsPane participantsPane = participant1.getParticipantsPane();
        AVModerationMenu avModerationMenu = participant1.getAVModerationMenu();

        participantsPane.open();

        participantsPane.clickContextMenuButton();

        avModerationMenu.clickStartAudioModeration();
        avModerationMenu.clickStartVideoModeration();

        participantsPane.close();

        // join with second participant and check
        WebParticipant participant2 = joinSecondParticipant();
        participant2.waitToJoinMUC();
        participant2.waitForIceConnected();

        MeetUIUtils.toggleAudioAndCheck(participant2, participant1, true);
        MeetUIUtils.toggleVideoAndCheck(participant2, participant1, true);

        // asked to unmute and check
        unmuteByModerator(participant1, participant2, false, false);

        // mute and check
        participantsPane.muteParticipant(participant2);
        participant2.getNotifications().closeAskToUnmuteNotification();

        MeetUIUtils.toggleAudioAndCheck(participant2, participant1, true);
    }

    /**
     * Checks a user can unmute after being asked by moderator.
     * @param moderator - The participant that is moderator.
     * @param participant - The participant being asked to unmute.
     * @param turnOnModeration - <tt></tt> if we want to turn on moderation before testing (when it is currently off).
     * @param stopModeration - <tt>true</tt> if moderation to be stopped when done.
     */
    private void unmuteByModerator(WebParticipant moderator, WebParticipant participant,
                                   Boolean turnOnModeration, Boolean stopModeration)
    {
        ParticipantsPane participantsPane = moderator.getParticipantsPane();
        AVModerationMenu avModerationMenu = moderator.getAVModerationMenu();

        participantsPane.open();

        if (turnOnModeration)
        {
            participantsPane.clickContextMenuButton();

            avModerationMenu.clickStartAudioModeration();

            avModerationMenu.clickStartVideoModeration();
        }

        raiseHandToSpeak(participant);

        askParticipantToUnmute(moderator, participant);

        MeetUIUtils.toggleAudioAndCheck(participant, participant1, false);
        MeetUIUtils.unmuteVideoAndCheck(participant, participant1);

        if (stopModeration)
        {
            participantsPane.clickContextMenuButton();

            avModerationMenu.clickStopAudioModeration();

            avModerationMenu.clickStopVideoModeration();

            participantsPane.close();
        }
    }

    /**
     * Participant raises hand to speak during moderation
     * @param participant the participant that wants to speak
     */
    private void raiseHandToSpeak(WebParticipant participant)
    {
        participant.getToolbar().clickRaiseHandButton();

        participant1.getNotifications().getRaisedHandNotification();
    }

    /**
     * Moderator asks participant to unmute
     * @param participant the participant who is requested to unmute
     */
    private void askParticipantToUnmute(WebParticipant moderator, WebParticipant participant)
    {
        ParticipantsPane participantsPane = moderator.getParticipantsPane();

        participantsPane.askToUnmute(participant);

        participant.getNotifications().getAskToUnmuteNotification();
    }
}
