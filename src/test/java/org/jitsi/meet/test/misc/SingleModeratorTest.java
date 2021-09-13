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
 * This test depends on several external configs, providing valid jwt tokens and a server module that allow a single
 * moderator in the room.
 * - single moderator mode is enabled and in the personal room of the participant he/she is the only moderator
 *   if anyone joins, joins as guest.
 *
 * @author Damian Minkov
 */
public class SingleModeratorTest
    extends WebTestBase
{
    /**
     * Required configuration keys.
     */
    private static final String TENANT_NAME_PNAME = "org.jitsi.misc.single.tenant_name";
    private static final String MODERATOR_TOKEN_PNAME = "org.jitsi.misc.single.moderator.token";
    private static final String MODERATOR_ROOM_NAME_PNAME = "org.jitsi.misc.single.moderator.room_name";
    private static final String NON_MODERATOR1_TOKEN_PNAME = "org.jitsi.misc.single.non.moderator1.token";
    private static final String NON_MODERATOR2_TOKEN_PNAME = "org.jitsi.misc.single.non.moderator2.token";

    /**
     * The config values.
     */
    private String tenantName;
    private String roomName;
    private String moderatorToken;
    private String nonModerator1Token;
    private String nonModerator2Token;

    @Override
    public void setupClass()
    {
        super.setupClass();

        tenantName = System.getProperty(TENANT_NAME_PNAME);
        roomName = System.getProperty(MODERATOR_ROOM_NAME_PNAME);
        moderatorToken = System.getProperty(MODERATOR_TOKEN_PNAME);
        nonModerator1Token = System.getProperty(NON_MODERATOR1_TOKEN_PNAME);
        nonModerator2Token = System.getProperty(NON_MODERATOR2_TOKEN_PNAME);

        if (Utils.checkForMissingConfig(tenantName, roomName, moderatorToken, nonModerator1Token, nonModerator2Token))
        {
            cleanupClass();
            throw new SkipException("missing configuration");
        }
    }

    @Test
    public void testPersonalRoomAndModerators()
    {
        JitsiMeetUrl url = getJitsiMeetUrl();
        url.setRoomName(tenantName + '/' + roomName);

        // moderator + guest
        ensureTwoParticipants(url.copy().setRoomParameters("jwt=" + moderatorToken), url);

        assertTrue(getParticipant1().isModerator(), "Participant 1 must be moderator");
        assertFalse(getParticipant2().isModerator(), "Participant 2 must not be moderator");

        ModeratedRoomsTest.checkModeratorMenuItems(getParticipant1(), getParticipant2());
        ModeratedRoomsTest.checkModeratorMenuItems(getParticipant2(), getParticipant1());

        getParticipant2().hangUp();

        // moderator + another authenticated user from same tenant
        ensureTwoParticipants(
            url.copy().setRoomParameters("jwt=" + moderatorToken),
            url.copy().setRoomParameters("jwt=" + nonModerator1Token));

        assertFalse(getParticipant2().isModerator(), "Participant 2 must not be moderator");
        ModeratedRoomsTest.checkModeratorMenuItems(getParticipant2(), getParticipant1());

        getParticipant2().hangUp();

        // moderator + another authenticated user from different tenant
        ensureTwoParticipants(
            url.copy().setRoomParameters("jwt=" + moderatorToken),
            url.copy().setRoomParameters("jwt=" + nonModerator2Token));

        assertFalse(getParticipant2().isModerator(), "Participant 2 must not be moderator");
        ModeratedRoomsTest.checkModeratorMenuItems(getParticipant2(), getParticipant1());
    }
}
