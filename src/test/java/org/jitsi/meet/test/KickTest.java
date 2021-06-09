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

import org.jitsi.meet.test.base.*;
import org.jitsi.meet.test.web.*;

import org.testng.*;
import org.testng.annotations.*;

import java.util.logging.*;

import static org.testng.Assert.*;

/**
 * Tests the kick functionality.
 *
 * @author Damian Minkov
 */
public class KickTest
    extends WebTestBase
{
    @Override
    public void setupClass()
    {
        super.setupClass();

        ensureTwoParticipants();
    }

    /**
     * Kick participant2 and checks at participant1 is this is visible.
     * and whether participant2 sees a notification that was kicked.
     */
    @Test
    public void kickParticipant2AndCheck()
    {
        WebParticipant participant1 = getParticipant1();

        if (participant1.isModerator())
        {
            participant1.getRemoteParticipants().get(0).kick();

            participant1.waitForParticipants(0);

            // check that the kicked participant sees the notification
            assertTrue(
                getParticipant2().getNotifications().hasKickedNotification(),
                "The second participant should see a warning that was kicked.");
        }
        else
        {
            Logger.getGlobal().log(Level.WARNING, "Not testing kick as torture is not moderator.");
            throw new SkipException("skip as test's participant cannot be moderator");
        }
    }

    /**
     * Executes the kick test kickParticipant2AndCheck in p2p mode.
     */
    @Test(dependsOnMethods = {"kickParticipant2AndCheck"})
    public void kickP2PParticipant2AndCheck()
    {
        hangUpAllParticipants();

        JitsiMeetUrl url
            = getJitsiMeetUrl().appendConfig("config.p2p.enabled=true");
        ensureTwoParticipants(url, url);

        kickParticipant2AndCheck();
    }
}
