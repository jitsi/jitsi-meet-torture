/*
 * Copyright @ 2020 8x8 Inc.
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

import org.jitsi.meet.test.web.*;

import org.testng.*;
import org.testng.annotations.*;

import static org.jitsi.meet.test.util.TestUtils.*;

/**
 * Tests the muting and unmuting of the participants in Meet conferences.
 *
 * @author Gabriel Imre
 */
public class ModeratorTest
    extends WebTestBase
{
    /**
     * Default constructor.
     */
    public ModeratorTest()
    {
    }

    @Override
    public void setupClass()
    {
        super.setupClass();

        ensureOneParticipant();
        if (!getParticipant1().hasGrantModerator())
        {
            cleanupClass();
            throw new SkipException(
                "No grantModerator functionality detected. Disabling test.");
        }

        ensureTwoParticipants();
    }

    /**
     * Grants moderator to participant2 and checks from participant2's
     * perspective if this is visible.
     */
    @Test
    public void grantModeratorToParticipant2AndValidate()
    {
        WebParticipant participant1 = getParticipant1();
        WebParticipant participant2 = getParticipant2();

        if (!participant1.isModerator())
        {
            print("Participant 1 is not a moderator, skipping check!");
            return;
        }

        if (participant2.isModerator())
        {
            print("Participant 2 is already a moderator, skipping check!");
            return;
        }

        participant1
            .getRemoteParticipantById(participant2.getEndpointId())
            .grantModerator();

        participant2.waitToBecomeModerator();
    }
}
