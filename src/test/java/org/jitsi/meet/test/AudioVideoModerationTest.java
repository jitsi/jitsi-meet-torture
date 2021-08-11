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
import org.jitsi.meet.test.pageobjects.web.UnmuteModalDialogHelper;
import org.jitsi.meet.test.util.MeetUIUtils;
import org.jitsi.meet.test.util.TestUtils;
import org.jitsi.meet.test.web.WebParticipant;
import org.jitsi.meet.test.web.WebTestBase;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.*;

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

        assertTrue(participant1.isModerator(), "Participant 1 must be moderator");
        assertFalse(participant2.isModerator(), "Participant 2 must not be moderator");
        assertFalse(participant3.isModerator(), "Participant 3 must not be moderator");
    }

    /**
     * Checks moderation by enabling and disabling it
     */
    @Test
    public void testCheckModerationEnableDisable()
    {
        ParticipantsPane participantsPane = participant1.getParticipantsPane();
        AVModerationMenu avModerationMenu = participant1.getAVModerationMenu();

        participantsPane.open();

        participantsPane.clickContextMenuButton();

        avModerationMenu.clickStartModeration();

        participant2.getNotifications().getModerationStartNotification();

        participantsPane.clickContextMenuButton();

        avModerationMenu.clickStopModeration();

        participant2.getNotifications().getModerationStopNotification();

        checkAudioVideoParticipantUnmute(participant3);
    }

    /**
     * Opens the context menu from the participants pane
     * and enables moderation
     */
    @Test(dependsOnMethods = { "testCheckModerationEnableDisable" })
    public void testUnmuteByModeratorDialog()
    {
        ParticipantsPane participantsPane = participant1.getParticipantsPane();
        AVModerationMenu avModerationMenu = participant1.getAVModerationMenu();
        WebDriver driver1 = participant1.getDriver();

        participantsPane.clickContextMenuButton();

        avModerationMenu.clickStartModeration();

        participant3.getNotifications().getModerationStartNotification();

        raiseHandToSpeak(participant3);

        askParticipantToUnmute(participant3);

        checkAudioVideoParticipantUnmute(participant3);

        participantsPane.close();

        raiseHandToSpeak(participant2);

        TestUtils.waitForElementDisplayToBe(
                driver1,
                By.id("notification-participant-list"),
                3,
                true);

        UnmuteModalDialogHelper.clickUnmuteButton(driver1);

        participant2.getNotifications().getAskToUnmuteNotification();

        checkAudioVideoParticipantUnmute(participant2);

        participantsPane.open();

        participantsPane.clickContextMenuButton();

        avModerationMenu.clickStopModeration();

        participant2.getNotifications().getModerationStopNotification();

        participantsPane.close();
    }


    /**
     * Initial moderator reloads and next participant becomes the new moderator
     */
    @Test(dependsOnMethods = { "testUnmuteByModeratorDialog" })
    public void testHangUpAndChangeModerator()
    {
        participant2.hangUp();
        participant3.hangUp();

        joinSecondParticipant();
        joinThirdParticipant();

        participant2.muteAudio(true);
        participant3.muteAudio(true);

        participant1.getParticipantsPane().open();

        participant1.getParticipantsPane().clickContextMenuButton();

        participant1.getAVModerationMenu().clickStartModeration();

        participant2.getToolbar().clickRaiseHandButton();

        participant3.getToolbar().clickRaiseHandButton();

        participant1.hangUp();

        joinFirstParticipant();

        if (participant2.isModerator())
        {
            participant2.getParticipantsPane().open();

            participant2.getParticipantsPane().askToUnmuteAfterReload(participant2);

            participant3.getNotifications().getAskToUnmuteNotification();

            checkAudioVideoParticipantUnmute(participant3);

            participant2.getParticipantsPane().clickContextMenuButton();

            participant2.getAVModerationMenu().clickStopModeration();

            participant3.getNotifications().getModerationStopNotification();

            participant2.getParticipantsPane().close();
        }

        if (participant3.isModerator())
        {
            participant3.getParticipantsPane().open();

            participant3.getParticipantsPane().askToUnmuteAfterReload(participant3);

            participant2.getNotifications().getAskToUnmuteNotification();

            checkAudioVideoParticipantUnmute(participant2);

            participant3.getParticipantsPane().clickContextMenuButton();

            participant3.getAVModerationMenu().clickStopModeration();

            participant2.getNotifications().getModerationStopNotification();

            participant3.getParticipantsPane().close();
        }
    }

    /**
     * Participant raises hand to speak during moderation
     * @param participant the participant that wants to speak
     */
    private void raiseHandToSpeak(WebParticipant participant)
    {
        participant.getToolbar().clickAudioMuteButton();

        participant.getNotifications().getAudioModerationNotification();


        participant.getToolbar().clickRaiseHandButton();

        participant1.getNotifications().getRaisedHandNotification();
    }

    /**
     * Moderator asks participant to unmute
     * @param participant the participant who is requested to unmute
     */
    private void askParticipantToUnmute(WebParticipant participant)
    {
        ParticipantsPane participantsPane = participant1.getParticipantsPane();

        participantsPane.askToUnmute(participant1);

        participant.getNotifications().getAskToUnmuteNotification();
    }

    /**
     * Checks audio/video unmute state for participant
     * @param participant the participant that is checked
     */
    private void checkAudioVideoParticipantUnmute(WebParticipant participant)
    {
        TestUtils.waitMillis(2000);

        MeetUIUtils.toggleAudioAndCheck(participant, participant1, false, false);

        TestUtils.waitMillis(2000);

        MeetUIUtils.unmuteVideoAndCheck(participant, participant1);
    }
}
