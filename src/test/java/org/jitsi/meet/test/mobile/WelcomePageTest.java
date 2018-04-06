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
package org.jitsi.meet.test.mobile;

import org.jitsi.meet.test.mobile.base.*;
import org.testng.annotations.*;

/**
 * Simple test case where we wait for room name field, fill it and clicks join.
 *
 * FIXME we no longer need this test ?
 *
 * @author Damian Minkov
 * @author Pawel Domas
 */
public class WelcomePageTest
    extends MobileTestBase
{
    /**
     * Executes {@link #testJoinConference(MobileParticipant)}} for the 1st
     * mobile participant.
     */
    @Test
    public void joinConference()
    {
        MobileParticipant mobile = createParticipant1();

        testJoinConference(mobile);

        mobile.close();
    }

    /**
     * Will enter conference room name in the welcome page and join
     * the conference. After join will press the hangup button.
     *
     * @param participant - A mobile participant on which the test will execute.
     */
    private void testJoinConference(MobileParticipant participant)
    {
        participant.joinConference(currentRoomName);

        participant.takeScreenshot("joinedConference");

        participant.getToolbarView().open();

        participant.takeScreenshot("joinedConferenceToolbarToggled");

        participant.hangUp();

        participant.takeScreenshot("afterHangUp");
    }
}
