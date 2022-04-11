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
package org.jitsi.meet.test.misc;

import org.jitsi.meet.test.*;
import org.jitsi.meet.test.base.*;
import org.jitsi.meet.test.pageobjects.web.*;
import org.jitsi.meet.test.web.*;
import org.openqa.selenium.*;
import org.testng.*;
import org.testng.annotations.*;

import static org.testng.Assert.*;

/**
 * This test depends on several external configs, providing valid jwt tokens and a server module that pre-sets lobby
 * for a room.
 * - personal is with disabled lobby, only that room is without lobby the rest are with lobby by default
 * - personal room with lobby
 *   - 1st guest goes into lobby
 *   - 2nd moderator is pass through and see invite for the guest
 *   - the 2nd participant (moderator) reloads and sees again the invite and is passed through the lobby
 *   - 3rd authenticated participant from another tenant goes into lobby
 * - in-meeting lobby 1 guest + 1 moderator, moderator rejoins room is not destroyed
 *
 * @author Damian Minkov
 */
public class BackendLobbyTest
    extends WebTestBase
{
    /**
     * Required configuration keys.
     */
    private static final String TENANT_NAME_PNAME = "org.jitsi.misc.lobby.tenant_name";
    private static final String MODERATOR_TOKEN_PNAME = "org.jitsi.misc.lobby.moderator.token";
    private static final String MODERATOR_ROOM_NAME_PNAME = "org.jitsi.misc.lobby.moderator.room_name";
    private static final String NON_MODERATOR1_TOKEN_PNAME = "org.jitsi.misc.lobby.non.moderator1.token";
    private static final String NON_MODERATOR1_ROOM_NAME_PNAME = "org.jitsi.misc.lobby.non.moderator1.room_name";
    private static final String NON_MODERATOR2_TOKEN_PNAME = "org.jitsi.misc.lobby.non.moderator2.token";

    /**
     * The config values.
     */
    private String tenantName;
    private String roomName;
    private String moderatorToken;
    private String nonModerator1Token;
    private String nonModerator1RoomName;
    private String nonModerator2Token;

    @Override
    public void setupClass()
    {
        super.setupClass();
        tenantName = System.getProperty(TENANT_NAME_PNAME);
        roomName = System.getProperty(MODERATOR_ROOM_NAME_PNAME);
        moderatorToken = System.getProperty(MODERATOR_TOKEN_PNAME);
        nonModerator1Token = System.getProperty(NON_MODERATOR1_TOKEN_PNAME);
        nonModerator1RoomName = System.getProperty(NON_MODERATOR1_ROOM_NAME_PNAME);
        nonModerator2Token = System.getProperty(NON_MODERATOR2_TOKEN_PNAME);

        if (Utils.checkForMissingConfig(
                tenantName, roomName, moderatorToken, nonModerator1Token, nonModerator1RoomName, nonModerator2Token))
        {
            cleanupClass();
            throw new SkipException("missing configuration");
        }
    }

    @Test
    public void testPersonalWithNoLobby()
    {
        JitsiMeetUrl url = getJitsiMeetUrl();
        url.setRoomName(tenantName + '/' + nonModerator1RoomName);

        ensureTwoParticipants(url.copy().setRoomParameters("jwt=" + nonModerator1Token), url);

        assertFalse(getParticipant1().getSecurityDialog().isLobbyEnabled());
        assertFalse(getParticipant2().getSecurityDialog().isLobbySectionPresent());
    }

    @Test(dependsOnMethods = { "testPersonalWithNoLobby" })
    public void testInMeetingLobby()
    {
        WebParticipant participant1 = getParticipant1();
        SecurityDialog securityDialog1 = participant1.getSecurityDialog();
        securityDialog1.open();

        assertFalse(securityDialog1.isLobbyEnabled());

        securityDialog1.toggleLobby();
        LobbyTest.waitForLobbyEnabled(participant1, true);

        JitsiMeetUrl url = participant1.getMeetUrl();
        participant1.hangUp();

        ensureTwoParticipants(url, getParticipant2().getMeetUrl());

        participant1.waitForParticipants(1);
    }

    @Test(dependsOnMethods = { "testPersonalWithNoLobby" })
    public void testPersonalRoomWithLobby()
    {
        hangUpAllParticipants();

        JitsiMeetUrl url = getJitsiMeetUrl();
        url.setRoomName(tenantName + '/' + roomName);

        joinFirstParticipant(url, null);

        WebParticipant participant1 = getParticipant1();
        LobbyScreen lobbyScreen = participant1.getLobbyScreen();

        // participant 1 should be now on pre-join screen
        lobbyScreen.waitForLoading();

        WebElement displayNameInput = lobbyScreen.getDisplayNameInput();
        assertNotNull(displayNameInput);
        // check display name is visible
        assertTrue(displayNameInput.isDisplayed());

        WebElement joinButton = lobbyScreen.getJoinButton();
        assertNotNull(joinButton);

        // enter display name
        lobbyScreen.enterDisplayName(participant1.getName());

        lobbyScreen.join();

        joinSecondParticipant(url.copy().setRoomParameters("jwt=" + moderatorToken));
        WebParticipant moderator = getParticipant2();

        moderator.waitToJoinMUC();

        String name1 = moderator.getNotifications().getKnockingParticipantName();
        assertEquals(name1, participant1.getName(),
            "Wrong name for the knocking participant or participant is missing");

        assertTrue(lobbyScreen.isLobbyRoomJoined(), "Lobby room not joined");

        moderator.hangUp();

        joinSecondParticipant(url.copy().setRoomParameters("jwt=" + moderatorToken));
        moderator = getParticipant2();

        moderator.waitToJoinMUC();

        String name2 = moderator.getNotifications().getKnockingParticipantName();
        assertEquals(name2, participant1.getName(),
            "Wrong name for the knocking participant or participant is missing");

        joinThirdParticipant(url.copy().setRoomParameters("jwt=" + nonModerator2Token), null);

        WebParticipant participant3 = getParticipant3();
        LobbyScreen lobbyScreen3 = participant3.getLobbyScreen();

        // participant 1 should be now on pre-join screen
        lobbyScreen3.waitForLoading();

        WebElement joinButton3 = lobbyScreen3.getJoinButton();
        assertNotNull(joinButton3);
    }
}
