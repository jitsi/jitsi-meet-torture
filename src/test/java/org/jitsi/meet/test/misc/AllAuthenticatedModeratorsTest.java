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
import org.jitsi.meet.test.web.*;
import org.testng.*;
import org.testng.annotations.*;

import static org.testng.Assert.*;

/**
 * This test depends on several external configs, providing valid jwt tokens and a server module that sets as moderators
 * all authenticated participants.
 * - personal room 2 authenticated are moderators + one guest
 * - random room all authenticated are moderators + one guest
 *
 * @author Damian Minkov
 */
public class AllAuthenticatedModeratorsTest
    extends WebTestBase
{
    /**
     * Required configuration keys.
     */
    private static final String TENANT_NAME_PNAME = "org.jitsi.misc.all.tenant_name";
    private static final String MODERATOR1_TOKEN_PNAME = "org.jitsi.misc.all.moderator1.token";
    private static final String MODERATOR2_TOKEN_PNAME = "org.jitsi.misc.all.moderator2.token";
    private static final String MODERATOR_ROOM_NAME_PNAME = "org.jitsi.misc.all.moderator.room_name";

    /**
     * The config values.
     */
    private String tenantName;
    private String roomName;
    private String moderator1Token;
    private String moderator2Token;

    @Override
    public void setupClass()
    {
        super.setupClass();

        tenantName = System.getProperty(TENANT_NAME_PNAME);
        roomName = System.getProperty(MODERATOR_ROOM_NAME_PNAME);
        moderator1Token = System.getProperty(MODERATOR1_TOKEN_PNAME);
        moderator2Token = System.getProperty(MODERATOR2_TOKEN_PNAME);

        if (Utils.checkForMissingConfig(tenantName, roomName, moderator1Token, moderator2Token))
        {
            cleanupClass();
            throw new SkipException("missing configuration");
        }
    }

    @Test
    public void testPersonalRoom()
    {
        JitsiMeetUrl url = getJitsiMeetUrl();
        url.setRoomName(tenantName + '/' + roomName);

        // moderator + guest
        ensureThreeParticipants(
            url.copy().setRoomParameters("jwt=" + moderator1Token),
            url.copy().setRoomParameters("jwt=" + moderator2Token),
            url);

        // FIXME: Showing remote video menu in tileview does not work
        getParticipant1().getToolbar().clickTileViewButton();
        getParticipant2().getToolbar().clickTileViewButton();
        getParticipant3().getToolbar().clickTileViewButton();

        assertTrue(getParticipant1().isModerator(), "Participant 1 must be moderator");
        assertTrue(getParticipant2().isModerator(), "Participant 2 must be moderator");
        assertFalse(getParticipant3().isModerator(), "Participant 3 must not be moderator");

        ModeratedRoomsTest.checkModeratorMenuItems(getParticipant1(), getParticipant2());
        ModeratedRoomsTest.checkModeratorMenuItems(getParticipant1(), getParticipant3());
        ModeratedRoomsTest.checkModeratorMenuItems(getParticipant2(), getParticipant1());
        ModeratedRoomsTest.checkModeratorMenuItems(getParticipant2(), getParticipant3());
        ModeratedRoomsTest.checkModeratorMenuItems(getParticipant3(), getParticipant1());
        ModeratedRoomsTest.checkModeratorMenuItems(getParticipant3(), getParticipant2());
    }

    @Test(dependsOnMethods = { "testPersonalRoom" })
    public void testRandomRoom()
    {
        hangUpAllParticipants();

        JitsiMeetUrl url = getJitsiMeetUrl();
        // we set a room name with capital letters
        url.setRoomName(tenantName + '/' + url.getRoomName() + "RandomRoom");

        // moderator + guest
        ensureThreeParticipants(
            url.copy().setRoomParameters("jwt=" + moderator1Token),
            url.copy().setRoomParameters("jwt=" + moderator2Token),
            url);

        // FIXME: Showing remote video menu in tileview does not work
        getParticipant1().getToolbar().clickTileViewButton();
        getParticipant2().getToolbar().clickTileViewButton();
        getParticipant3().getToolbar().clickTileViewButton();

        assertTrue(getParticipant1().isModerator(), "Participant 1 must be moderator");
        assertTrue(getParticipant2().isModerator(), "Participant 2 must be moderator");
        assertFalse(getParticipant3().isModerator(), "Participant 3 must not be moderator");

        ModeratedRoomsTest.checkModeratorMenuItems(getParticipant1(), getParticipant2());
        ModeratedRoomsTest.checkModeratorMenuItems(getParticipant1(), getParticipant3());
        ModeratedRoomsTest.checkModeratorMenuItems(getParticipant2(), getParticipant1());
        ModeratedRoomsTest.checkModeratorMenuItems(getParticipant2(), getParticipant3());
        ModeratedRoomsTest.checkModeratorMenuItems(getParticipant3(), getParticipant1());
        ModeratedRoomsTest.checkModeratorMenuItems(getParticipant3(), getParticipant2());
    }
}
