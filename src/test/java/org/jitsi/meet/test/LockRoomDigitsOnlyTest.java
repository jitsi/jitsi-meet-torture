/*
 * Copyright @ 2015 Atlassian Pty Ltd
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

import org.testng.*;
import org.testng.annotations.*;

import org.jitsi.meet.test.pageobjects.web.*;
import org.jitsi.meet.test.util.*;

import org.jitsi.meet.test.web.*;

import static org.testng.Assert.*;

/**
 * Tests that the digits only password feature works.
 *
 * 1. Lock the room with a string (shouldn't work)
 * 2. Lock the room with a valid numeric password (should work)
 */
public class LockRoomDigitsOnlyTest
    extends WebTestBase
{
    /**
     * Stops the participant. And locks the room from participant1.
     */
    @Test
    public void lockRoomWithDigitsOnly()
    {
        ensureOneParticipant(
            getJitsiMeetUrl().appendConfig(
                "config.roomPasswordNumberOfDigits=5"));

        getParticipant1().waitToJoinMUC();
    
        WebParticipant participant1 = getParticipant1();
    
        Object res = participant1.executeScript(
            "return APP.store.getState()['features/base/config'].roomPasswordNumberOfDigits === 5");

        if (Boolean.FALSE.equals(res))
        {
            throw new SkipException("roomPasswordNumberOfDigits cannot be overridden");
        }

        SecurityDialog securityDialog = participant1.getSecurityDialog();
        securityDialog.open();
    
        assertFalse(securityDialog.isLocked());

        // Set a non-numeric password.
        securityDialog.addPassword("AAAAA");
        TestUtils.waitMillis(1000);
        assertFalse(securityDialog.isLocked());
        securityDialog.close();

        // Set a valid numeric password.
        securityDialog.open();
        securityDialog.addPassword("12345");
        securityDialog.close();

        securityDialog.open();

        assertTrue(securityDialog.isLocked());
    }
}
