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
 * all authenticated participants from the same group/tenant.
 *
 * - personal room 2 participants from same tenant are moderators + one is guest
 * - personal room 2 participants from different tenants and one guest, there is only one moderator
 *
 * @author Damian Minkov
 */
public class TenantModeratorsTest
    extends WebTestBase
{
    /**
     * Required configuration keys.
     */
    private static final String TENANT_NAME_PNAME = "org.jitsi.misc.tenant.tenant_name";
    private static final String MODERATOR1_TOKEN_PNAME = "org.jitsi.misc.tenant.moderator1.token";
    private static final String MODERATOR2_TOKEN_PNAME = "org.jitsi.misc.tenant.moderator2.token";
    private static final String MODERATOR_ROOM_NAME_PNAME = "org.jitsi.misc.tenant.moderator1.room_name";
    private static final String NON_MODERATOR_TOKEN_PNAME = "org.jitsi.misc.tenant.non.moderator.token";

    /**
     * The config values.
     */
    private String tenantName;
    private String roomName;
    private String moderator1Token;
    private String moderator2Token;
    private String nonModeratorToken;

    @Override
    public void setupClass()
    {
        super.setupClass();

        tenantName = System.getProperty(TENANT_NAME_PNAME);
        roomName = System.getProperty(MODERATOR_ROOM_NAME_PNAME);
        moderator1Token = System.getProperty(MODERATOR1_TOKEN_PNAME);
        moderator2Token = System.getProperty(MODERATOR2_TOKEN_PNAME);
        nonModeratorToken = System.getProperty(NON_MODERATOR_TOKEN_PNAME);

        if (Utils.checkForMissingConfig(tenantName, roomName, moderator1Token, moderator2Token, nonModeratorToken))
        {
            cleanupClass();
            throw new SkipException("missing configuration");
        }
    }

    @Test
    public void testTwoModeratorsFromSameTenant()
    {
        JitsiMeetUrl url = getJitsiMeetUrl();
        url.setRoomName(tenantName + '/' + roomName);

        // moderator + guest
        ensureThreeParticipants(
            url.copy().setRoomParameters("jwt=" + moderator1Token),
            url.copy().setRoomParameters("jwt=" + moderator2Token),
            url);

        // FIXME: Showing remote video menu in tile view does not work
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

        // Now let's test changing the second participant with one from different tenant
        getParticipant2().hangUp();
        ensureThreeParticipants(
            url.copy().setRoomParameters("jwt=" + moderator1Token),
            url.copy().setRoomParameters("jwt=" + nonModeratorToken),
            url);

        // FIXME: Showing remote video menu in tileview does not work
        getParticipant1().getToolbar().clickTileViewButton();
        getParticipant2().getToolbar().clickTileViewButton();
        getParticipant3().getToolbar().clickTileViewButton();

        assertFalse(getParticipant2().isModerator(), "Participant 2 must not be moderator");
        ModeratedRoomsTest.checkModeratorMenuItems(getParticipant2(), getParticipant1());
        ModeratedRoomsTest.checkModeratorMenuItems(getParticipant2(), getParticipant3());
    }
}
