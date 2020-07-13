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

import java.util.concurrent.*;

import org.jitsi.meet.test.base.*;
import org.jitsi.meet.test.util.*;
import org.jitsi.meet.test.web.*;

import org.testng.annotations.*;
import static org.testng.Assert.*;

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

        ensureTwoParticipants();
    }

    /**
     * Grants moderator to participant2 and checks from participant2's
     * perspective if this is viible.
     */
    @Test
    public void grantModeratorToParticipant2AndValidate()
    {
        WebParticipant participant1 = getParticipant1();
        WebParticipant participant2 = getParticipant2();

        participant1
            .getRemoteParticipantById(participant2.getEndpointId())
            .grantModerator();

        participant2.waitToBecomeModerator();
    }
}
