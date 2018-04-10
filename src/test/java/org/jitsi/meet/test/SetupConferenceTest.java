/*
 * Copyright @ Atlassian Pty Ltd
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
import org.jitsi.meet.test.hybrid.*;

import org.testng.annotations.*;

import java.util.*;

/**
 * Joins conference with 2 participants and checks if the media connection is
 * successfully established. Then hangups the call.
 */
public class SetupConferenceTest
    extends HybridTestBase
{
    @Override
    public void setupClass()
    {
        super.setupClass();

        // XXX each test subclass could be providing config prefixes for
        // participant # and then the 'joinParticipant' method could be moved
        // from WebTestBase to AbstractTestBase.
        participants.createParticipant(
                getStringConfigValue("hybrid.participant1"));
        participants.createParticipant(
                getStringConfigValue("hybrid.participant2"));
    }

    /**
     * Joins the conference, waits for the media to start flowing and ends
     * the call.
     *
     * XXX Once we have any other "hybrid" tests this one can be removed, as
     * the "ensureXParticipants" will be testing that, before any test that uses
     * 2 participants starts.
     */
    @Test
    public void testJoinAndLeaveConference()
    {
        // FIXME this will eventually end up as the ensureTwoParticipants call,
        // but one step at a time.
        List<Participant> all = participants.getAll();

        all.forEach(p -> p.joinConference(getJitsiMeetUrl()));

        all.forEach(Participant::waitToJoinMUC);
        all.forEach(Participant::waitForIceConnected);
        all.forEach(Participant::waitForSendReceiveData);

        participants.getAll().forEach(Participant::hangUp);
    }
}
