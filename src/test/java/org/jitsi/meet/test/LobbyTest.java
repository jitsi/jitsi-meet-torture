/*
 * Copyright @ 2018 8x8 Pty Ltd
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

import static org.testng.Assert.*;

/**
 * Lobby room tests.
 */
public class LobbyTest
    extends WebTestBase
{
    /**
     * Javascript to check whether lobby is enabled and supported on tested environment.
     */
    public static final String IS_LOBBY_SUPPORTED_CHECK_SCRIPT =
        "return APP.conference._room.isLobbySupported()";

    @Override
    public void setupClass()
    {
        super.setupClass();

        ensureOneParticipant();
        if (!TestUtils.executeScriptAndReturnBoolean(getParticipant1().getDriver(), IS_LOBBY_SUPPORTED_CHECK_SCRIPT))
        {
            cleanupClass();
            throw new SkipException("Skip Lobby tests as it is not enabled.");
        }
    }

    /**
     * Checks if the info dialog shows the current conference URL.
     */
    @Test
    public void testEnableLobby()
    {
        ensureTwoParticipants();

        enableLobby();
    }

    /**
     * This requires at least two participants in the room.
     */
    private void enableLobby()
    {
        WebParticipant participant1 = getParticipant1();

        // we set the name so we can check it on the notifications
        participant1.setDisplayName(participant1.getName());

        SecurityDialog securityDialog1 = participant1.getSecurityDialog();
        securityDialog1.open();

        assertFalse(securityDialog1.isLobbyEnabled());

        securityDialog1.toggleLobby();
        waitForLobbyEnabled(participant1, true);

        WebParticipant participant2 = getParticipant2();
        String notificationEnabled2 = participant2.getNotifications().getLobbyEnabledText();

        assertNotNull(notificationEnabled2);
        assertTrue(notificationEnabled2.contains(participant1.getName()));

        participant2.getNotifications().closeLobbyEnabled();

        SecurityDialog securityDialog2 = participant2.getSecurityDialog();
        securityDialog2.open();

        // lobby is visible to moderators only, this depends whether deployment is all moderators or not
        if (participant2.isModerator())
        {
            waitForLobbyEnabled(participant2, true);
        }
        else
        {
            assertFalse(securityDialog2.isLobbySectionPresent());
        }

        // let's close the security dialog or we will not be able to click
        // on popups for allow/deny participants
        securityDialog1.close();
        securityDialog2.close();
    }

    /**
     * Waits for lobby to be enabled or disabled.
     *
     * @param participant the participants drive to check.
     */
    public static void waitForLobbyEnabled(WebParticipant participant, boolean enabled)
    {
        SecurityDialog securityDialog = participant.getSecurityDialog();
        securityDialog.open();

        TestUtils.waitForCondition(
            participant.getDriver(),
            10,
            (ExpectedCondition<Boolean>) d -> {
                boolean currentState = securityDialog.isLobbyEnabled();

                return enabled == currentState;
            });
    }

    /**
     * Expects that lobby is enabled for the room we will try to join.
     * Lobby UI is shown, enter display name and join.
     * Checks Lobby UI and also that when joining the moderator sees corresponding notifications.
     *
     * @param moderator The participant that is moderator in the meeting.
     * @param enterDisplayName whether to enter display name. We need to enter display name only the first time when
     * a participant sees the lobby screen, next time visiting the page display name will be pre-filled
     * from local storage.
     * @return the participant name knocking.
     */
    private String enterLobby(WebParticipant moderator, boolean enterDisplayName)
    {
        joinThirdParticipant(null, new WebParticipantOptions().setSkipDisplayNameSet(true));

        WebParticipant participant3 = getParticipant3();
        LobbyScreen lobbyScreen = participant3.getLobbyScreen();

        // participant 3 should be now on pre-join screen
        lobbyScreen.waitForLoading();

        WebElement displayNameInput = lobbyScreen.getDisplayNameInput();
        assertNotNull(displayNameInput);
        // check display name is visible
        assertTrue(displayNameInput.isDisplayed());

        WebElement joinButton = lobbyScreen.getJoinButton();
        assertNotNull(joinButton);

        if (enterDisplayName)
        {
            // check join button is disabled
            String classes = joinButton.getAttribute("class");
            assertTrue(classes.contains("disabled"), "The join button should be disabled");

            // TODO check that password is hidden as the room does not have password
            // this check needs to be added once the functionality exists

            // enter display name
            lobbyScreen.enterDisplayName(participant3.getName());

            // check join button is enabled
            classes = joinButton.getAttribute("class");
            assertFalse(classes.contains("disabled"), "The join button should be enabled");
        }

        // click join button
        lobbyScreen.join();

        TestUtils.waitForCondition(
            participant3.getDriver(),
            5,
            (ExpectedCondition<Boolean>) d -> lobbyScreen.isLobbyRoomJoined());

        // check no join button
        assertFalse(lobbyScreen.hasJoinButton(), "Join button should be hidden after clicking it");

        // new screen, is password button shown
        WebElement passwordButton = lobbyScreen.getPasswordButton();
        assertNotNull(passwordButton, "Password button not found");
        assertTrue(passwordButton.isDisplayed(), "Password button not displayed.");
        assertTrue(passwordButton.isEnabled(), "Password disabled.");

        // check that moderator (participant 1) sees notification about participant in lobby
        String name = moderator.getNotifications().getKnockingParticipantName();
        assertEquals(name, participant3.getName(), "Wrong name for the knocking participant or participant is missing");

        assertTrue(lobbyScreen.isLobbyRoomJoined(), "Lobby room not joined");

        return name;
    }

    /**
     * Checks that third tries to enter, sees lobby, and rest notifications are correct and the access is denied.
     */
    @Test(dependsOnMethods = {"testEnteringInLobbyAndApprove"})
    public void testEnteringInLobbyAndDeny()
    {
        WebParticipant participant1 = getParticipant1();

        // the first time tests is executed we need to enter display name,
        // for next execution that will be locally stored
        String knockingParticipant = enterLobby(participant1, false);

        // moderator rejects access
        participant1.getNotifications().rejectLobbyParticipant(knockingParticipant);

        // deny notification on 2nd participant
        WebParticipant participant2 = getParticipant2();
        String notificationText = participant2.getNotifications().getLobbyParticipantAccessDenied();

        assertTrue(
            notificationText.contains(participant1.getName()),
            "Notification for second participant need to have actor of the deny access operation");
        WebParticipant participant3 = getParticipant3();
        assertTrue(
            notificationText.contains(participant3.getName()),
            "Notification for second participant need to have the name pf the participant that access was denied");

        participant2.getNotifications().closeLobbyParticipantAccessDenied();

        // check the denied one is out of lobby, sees the notification about it
        assertTrue(
            participant3.getNotifications().hasLobbyAccessDenied(),
            "The third participant should see a warning that his access to the room was denied");

        assertFalse(participant3.getLobbyScreen().isLobbyRoomJoined(), "Lobby room not left");

        getParticipant3().hangUp();
    }

    /**
     * Checks that third tries to enter, sees lobby, and rest notifications are correct and the access is approved.
     * This check needs to be before any check (testEnteringInLobbyAndDeny) the modifies/stores the display name in
     * local storage. There was a case where the page stores the display name in localstaorage on the first run
     * but does not update that state in the conference when allowed to join and is joining with no display name.
     */
    @Test(dependsOnMethods = {"testEnableLobby"})
    public void testEnteringInLobbyAndApprove()
    {
        WebParticipant participant1 = getParticipant1();
        String knockingParticipant = enterLobby(participant1, true);

        // moderator allows access
        participant1.getNotifications().allowLobbyParticipant(knockingParticipant);

        // granted notification on 2nd participant
        WebParticipant participant2 = getParticipant2();
        String notificationText = participant2.getNotifications().getLobbyParticipantAccessGranted();

        assertTrue(
            notificationText.contains(participant1.getName()),
            "Notification for second participant need to have actor of the granted access operation");
        WebParticipant participant3 = getParticipant3();
        assertTrue(
            notificationText.contains(participant3.getName()),
            "Notification for second participant need to have the name pf the participant that access was granted");

        participant2.getNotifications().closeLobbyParticipantAccessGranted();

        // ensure 3 participants in the call will check for the third one that muc is joined, ice connected,
        // media is being receiving and there are two remote streams
        joinThirdParticipant(null, new WebParticipantOptions().setSkipDisplayNameSet(true));

        participant3.waitForIceConnected();
        participant3.waitForSendReceiveData();
        participant3.waitForRemoteStreams(2);

        // now check third one display name in the room, is the one set in the prejoin screen
        String name = MeetUIUtils.getRemoteDisplayName(participant1.getDriver(), participant3.getEndpointId());
        assertEquals(name, participant3.getName());

        getParticipant3().hangUp();
    }

    @Test(dependsOnMethods = {"testEnteringInLobbyAndDeny"})
    public void testApproveFromParticipantsPane()
    {
        WebParticipant participant1 = getParticipant1();
        String knockingParticipant = enterLobby(getParticipant1(), false);

        // moderator allows access
        ParticipantsPane participantsPane = participant1.getParticipantsPane();
        participantsPane.open();
        participantsPane.admitLobbyParticipant(knockingParticipant);
        participantsPane.close();

        WebParticipant participant3 = getParticipant3();
        // ensure 3 participants in the call will check for the third one that muc is joined, ice connected,
        // media is being receiving and there are two remote streams
        joinThirdParticipant(null, new WebParticipantOptions().setSkipDisplayNameSet(true));

        participant3.waitForIceConnected();
        participant3.waitForSendReceiveData();
        participant3.waitForRemoteStreams(2);

        // now check third one display name in the room, is the one set in the prejoin screen
        String name = MeetUIUtils.getRemoteDisplayName(participant1.getDriver(), participant3.getEndpointId());
        assertEquals(name, participant3.getName());

        getParticipant3().hangUp();
    }

    @Test(dependsOnMethods = {"testApproveFromParticipantsPane"})
    public void testRejectFromParticipantsPane()
    {
        WebParticipant participant1 = getParticipant1();
        String knockingParticipant = enterLobby(participant1, false);

        // moderator rejects access
        ParticipantsPane participantsPane = participant1.getParticipantsPane();
        participantsPane.open();
        participantsPane.rejectLobbyParticipant(knockingParticipant);
        participantsPane.close();

        WebParticipant participant3 = getParticipant3();
        // check the denied one is out of lobby, sees the notification about it
        // The third participant should see a warning that his access to the room was denied
        TestUtils.waitForCondition(participant3.getDriver(), 5,
                (ExpectedCondition<Boolean>) d -> participant3.getNotifications().hasLobbyAccessDenied());

        // check Lobby room not left
        TestUtils.waitForCondition(participant3.getDriver(), 5,
                (ExpectedCondition<Boolean>) d -> !participant3.getLobbyScreen().isLobbyRoomJoined());

        getParticipant3().hangUp();
    }

    /**
     * Checks that third tries to enter, sees lobby, and rest notifications are correct and the access is approved.
     */
    @Test(dependsOnMethods = {"testRejectFromParticipantsPane"})
    public void testLobbyUserLeaves()
    {
        enterLobby(getParticipant1(), false);

        getParticipant3().hangUp();

        // check that moderator (participant 1) no longer sees notification about participant in lobby
        assertTrue(getParticipant1().getNotifications().waitForHideOfKnockingParticipants(),
            "Moderator should not see participant in knocking participants list after that participant leaves Lobby.");
    }

    /**
     * Checks that third is informed that main meeting had ended.
     */
    @Test(dependsOnMethods = {"testLobbyUserLeaves"})
    public void testConferenceEndedInLobby()
    {
        enterLobby(getParticipant1(), false);

        getParticipant1().hangUp();
        getParticipant2().hangUp();

        WebParticipant participant3 = getParticipant3();

        assertTrue(
            participant3.getNotifications().hasMeetingEndedNotification(),
            "The third participant should see a warning that meeting has ended.");

        assertFalse(participant3.getLobbyScreen().isLobbyRoomJoined(), "Lobby room not left");

        participant3.hangUp();

        hangUpAllParticipants();

        // waits just wait a little to be sure the conference had been destroyed
        // we see sometimes the first participant not able to join in next test as maybe the conference is still there
        // with lobby enabled and waiting for a minute the ghost to expire and destroy the conference
        TestUtils.waitMillis(500);
    }

    /**
     * Checks that third joins main room after lobby is disabled.
     */
    @Test(dependsOnMethods = {"testConferenceEndedInLobby"})
    public void testDisableWhileParticipantInLobbyLobby()
    {
        testEnableLobby();

        enterLobby(getParticipant1(), false);

        WebParticipant participant1 = getParticipant1();
        SecurityDialog securityDialog1 = participant1.getSecurityDialog();
        securityDialog1.open();

        securityDialog1.toggleLobby();
        waitForLobbyEnabled(participant1, false);

        WebParticipant participant3 = getParticipant3();
        participant3.waitToJoinMUC();

        assertFalse(participant3.getLobbyScreen().isLobbyRoomJoined(), "Lobby room not left");
    }

    /**
     * Checks that when we change moderators they still see knocking participants from lobby.
     */
    @Test(dependsOnMethods = {"testDisableWhileParticipantInLobbyLobby"})
    public void testChangeOfModeratorsInLobby()
    {
        hangUpAllParticipants();

        ensureTwoParticipants();

        // hanging up the first one, which is moderator and second one should be
        getParticipant1().hangUp();

        WebParticipant participant2 = getParticipant2();
        assertTrue(participant2.isModerator(), "Second participant is not moderator after first leaves");

        SecurityDialog securityDialog2 = participant2.getSecurityDialog();
        securityDialog2.open();

        securityDialog2.toggleLobby();
        waitForLobbyEnabled(participant2, true);

        // here the important check is whether the moderator sees the knocking participant
        enterLobby(getParticipant2(), false);

        hangUpAllParticipants();
    }

    /**
     * Checks shared password bypasses lobby.
     */
    @Test(dependsOnMethods = {"testChangeOfModeratorsInLobby"})
    public void testLobbySharedPassword()
    {
        testEnableLobby();

        WebParticipant participant1 = getParticipant1();
        SecurityDialog securityDialog1 = participant1.getSecurityDialog();

        String roomPasscode = String.valueOf((int) (Math.random() * 1_000));

        assertFalse(securityDialog1.isLocked());

        securityDialog1.addPassword(roomPasscode);

        TestUtils.waitForCondition(
            participant1.getDriver(),
            2,
            (ExpectedCondition<Boolean>) d -> securityDialog1.isLocked());

        enterLobby(participant1, false);

        WebParticipant participant3 = getParticipant3();

        // now fill in password
        LobbyScreen lobbyScreen = participant3.getLobbyScreen();

        lobbyScreen.enterPassword(roomPasscode);

        participant3.waitToJoinMUC();
        participant3.waitForIceConnected();
        participant3.waitForSendReceiveData();
    }

    /**
     * Three participants enter a room and moderator enables lobby. All should stay in room.
     * This tests a problem we were seeing where all participants except the first two a kicked out.
     */
    @Test(dependsOnMethods = {"testLobbySharedPassword"})
    public void testLobbyEnableWithMoreThanTwoParticipants()
    {
        hangUpAllParticipants();

        ensureThreeParticipants();

        enableLobby();

        // we need to check remote participants as isInMuc has not changed its value as
        // the bug is triggered by presence with status 322 which is not handled correctly
        WebParticipant participant1 = getParticipant1();
        participant1.waitForRemoteStreams(2);
        WebParticipant participant2 = getParticipant2();
        participant2.waitForRemoteStreams(2);
        WebParticipant participant3 = getParticipant3();
        participant3.waitForRemoteStreams(2);
    }

    /**
     * Tests the case where there are two participants in the call, on is moderator and
     * one is not. The moderator leaves and we check that the seconnd one becomes moderator
     * and joins lobby.
     */
    @Test(dependsOnMethods = {"testLobbyEnableWithMoreThanTwoParticipants"})
    public void testModeratorLeavesWhileLobbyEnabled()
    {
        ensureTwoParticipants();

        getParticipant1().hangUp();

        WebParticipant participant2 = getParticipant2();

        TestUtils.waitForCondition(
            participant2.getDriver(),
            4,
            (ExpectedCondition<Boolean>) d -> participant2.isModerator());

        LobbyScreen lobbyScreen = participant2.getLobbyScreen();

        TestUtils.waitForCondition(
            participant2.getDriver(),
            5,
            (ExpectedCondition<Boolean>) d -> lobbyScreen.isLobbyRoomJoined());
    }

    @Override
    public boolean skipTestByDefault()
    {
        return true;
    }
}
