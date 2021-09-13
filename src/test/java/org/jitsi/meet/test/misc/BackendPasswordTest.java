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
 * This test depends on several external configs, providing valid jwt tokens and a server module that pre-sets the room
 * password.
 * - The owner of the room joins and is asked for password.
 * - Second authenticated participant joins and is asked for password.
 * - Third participant joins as guest and is asked for password.
 *
 * @author Damian Minkov
 */
public class BackendPasswordTest
    extends WebTestBase
{
    /**
     * Required configuration keys.
     */
    private static final String TENANT_NAME_PNAME = "org.jitsi.misc.pass.tenant_name";
    private static final String MODERATOR1_TOKEN_PNAME = "org.jitsi.misc.pass.moderator1.token";
    private static final String MODERATOR2_TOKEN_PNAME = "org.jitsi.misc.pass.moderator2.token";
    private static final String MODERATOR_ROOM_NAME_PNAME = "org.jitsi.misc.pass.moderator.room_name";
    private static final String MODERATOR_ROOM_PASS_PNAME = "org.jitsi.misc.pass.moderator.room_pass";

    /**
     * The config values.
     */
    private String tenantName;
    private String roomName;
    private String moderator1Token;
    private String moderator2Token;
    private String roomPass;


    @Override
    public void setupClass()
    {
        super.setupClass();
        tenantName = System.getProperty(TENANT_NAME_PNAME);
        roomName = System.getProperty(MODERATOR_ROOM_NAME_PNAME);
        moderator1Token = System.getProperty(MODERATOR1_TOKEN_PNAME);
        moderator2Token = System.getProperty(MODERATOR2_TOKEN_PNAME);
        roomPass = System.getProperty(MODERATOR_ROOM_PASS_PNAME);

        if (Utils.checkForMissingConfig(tenantName, roomName, moderator1Token, moderator2Token, roomPass))
        {
            cleanupClass();
            throw new SkipException("missing configuration");
        }
    }

    @Test
    public void testRoomPreSetPassword()
    {
        JitsiMeetUrl url = getJitsiMeetUrl();
        url.setRoomName(tenantName + '/' + roomName);

        joinFirstParticipant(url.copy().setRoomParameters("jwt=" + moderator1Token), null);

        joinWithPassAndCheck(getParticipant1());

        joinSecondParticipant(url.copy().setRoomParameters("jwt=" + moderator2Token));
        joinWithPassAndCheck(getParticipant2());

        joinThirdParticipant(url, null);
        joinWithPassAndCheck(getParticipant3());
    }

    /**
     * Joins participant entering a password.
     * @param participant the participant to join.
     */
    private void joinWithPassAndCheck(WebParticipant participant)
    {
        WebDriver driver = participant.getDriver();

        // wait for password prompt
        LockRoomTest.waitForPasswordDialog(driver);

        LockRoomTest.submitPassword(driver, roomPass);

        participant.waitToJoinMUC(5);

        SecurityDialog securityDialog1 = participant.getSecurityDialog();
        securityDialog1.open();
        assertTrue(securityDialog1.isLocked());
    }
}
