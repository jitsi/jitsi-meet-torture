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
import org.openqa.selenium.support.ui.ExpectedCondition;
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

        checkAudioParticipantUnmute(participant3, true);

        participantsPane.clickContextMenuButton();

        avModerationMenu.clickStopAudioModeration();

        participantsPane.close();

        // we don't have a UI change when modertion is enabled/disabled, so let's just give it a second
        TestUtils.waitMillis(1000);

        checkAudioParticipantUnmute(participant3, false);
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

        checkVideoParticipantUnmute(participant3, true);

        participantsPane.clickContextMenuButton();

        avModerationMenu.clickStopVideoModeration();

        participantsPane.close();

        // we don't have a UI change when modertion is enabled/disabled, so let's just give it a second
        TestUtils.waitMillis(1000);

        checkVideoParticipantUnmute(participant3, false);
    }

    /**
     * Checks user can unmute after being asked by moderator.
     */
    @Test(dependsOnMethods = { "testCheckVideoModerationEnableDisable" })
    public void testUnmuteByModerator()
    {
        unmuteByModerator(participant1, participant3, false, true);

        // we don't have a UI change when modertion is enabled/disabled, so let's just give it a second
        TestUtils.waitMillis(1000);

        // moderation is stopped at this point, make sure participants 1 & 2 are also unmuted,
        // participant3 was unmuted by unmuteByModerator
        checkAudioVideoParticipantUnmute(participant2);
        checkAudioVideoParticipantUnmute(participant1);
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

        if (participant2.isModerator())
        {
            participant2.getParticipantsPane().open();

            participant2.getParticipantsPane().askToUnmute(participant3);

            participant3.getNotifications().getAskToUnmuteNotification();

            checkAudioVideoParticipantUnmute(participant3);

            participant2.getParticipantsPane().clickContextMenuButton();

            participant2.getAVModerationMenu().clickStopAudioModeration();

            participant2.getAVModerationMenu().clickStopVideoModeration();

            participant2.getParticipantsPane().close();
        }

        if (participant3.isModerator())
        {
            participant3.getParticipantsPane().open();

            participant3.getParticipantsPane().askToUnmute(participant2);

            participant2.getNotifications().getAskToUnmuteNotification();

            checkAudioVideoParticipantUnmute(participant2);

            participant3.getParticipantsPane().clickContextMenuButton();

            participant3.getAVModerationMenu().clickStopAudioModeration();

            participant3.getAVModerationMenu().clickStopVideoModeration();

            participant3.getParticipantsPane().close();
        }
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

        unmuteByModerator(participant3, participant2, true, true);
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

        participantsPane.close();
    }

    /**
     * Test that mute removes participant from whitelist.
     */
    @Test(dependsOnMethods = { "testAskToUnmute" })
    public void testRemoveFromWhitelist()
    {

        unmuteByModerator(participant1, participant2, false, false);

        ParticipantsPane participantsPane = participant1.getParticipantsPane();

        // mute audio and check
        participantsPane.muteParticipant(participant2);
        checkAudioParticipantUnmute(participant2, true);

        // stop video and check
        participant1
            .getRemoteParticipantById(participant2.getEndpointId())
            .stopVideo();
        checkVideoParticipantUnmute(participant2, true);
    }

    /**
     * Checks a user can unmute after being asked by moderator.
     * @param moderator - The participant that is moderator.
     * @param participant - The participant being asked to unmute.
     * @param moderationOn - Whether or not moderation is enabled.
     * @param stopModeration - Whether or not to stop moderation when done.
     */
    private void unmuteByModerator(WebParticipant moderator, WebParticipant participant,
                                   Boolean moderationOn, Boolean stopModeration)
    {
        ParticipantsPane participantsPane = moderator.getParticipantsPane();
        AVModerationMenu avModerationMenu = moderator.getAVModerationMenu();

        participantsPane.open();

        if (!moderationOn)
        {
            participantsPane.clickContextMenuButton();

            avModerationMenu.clickStartAudioModeration();

            avModerationMenu.clickStartVideoModeration();
        }

        raiseHandToSpeak(participant);

        askParticipantToUnmute(moderator, participant);

        checkAudioVideoParticipantUnmute(participant);

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

    /**
     * Checks audio unmute state for participant
     * @param participant the participant that is checked
     */
    private void checkAudioParticipantUnmute(WebParticipant participant, Boolean isMuted)
    {
        MeetUIUtils.toggleAudioAndCheck(participant, participant1, isMuted, false);
    }

    /**
     * Checks audio unmute state for participant
     * @param participant the participant that is checked
     */
    private void checkVideoParticipantUnmute(WebParticipant participant, Boolean isMuted)
    {
        MeetUIUtils.toggleVideoAndCheck(participant, participant1, isMuted, true);
    }

    /**
     * Checks audio/video unmute state for participant
     * @param participant the participant that is checked
     */
    private void checkAudioVideoParticipantUnmute(WebParticipant participant)
    {
        MeetUIUtils.toggleAudioAndCheck(participant, participant1, false, false);

        MeetUIUtils.unmuteVideoAndCheck(participant, participant1);
    }
}
