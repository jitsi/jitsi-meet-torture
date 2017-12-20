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

import org.testng.*;
import org.testng.annotations.*;

/**
 * Simple test case where we wait for room name field, fill it and clicks join.
 *
 * @author Damian Minkov
 * @author Pawel Domas
 */
public class WelcomePageTest
    extends AbstractBaseTest
{
    @Test
    public void joinConferenceAndroid()
    {
        MobileParticipant androidParticipant = createMobile("mobile.android");

        if (androidParticipant != null)
        {
            testJoinConference(androidParticipant);

            destroyMobile(androidParticipant);
        }
        else
        {
            throw new SkipException("No Android driver config.");
        }
    }

    @Test
    public void joinConferenceIOS()
    {
        MobileParticipant iosParticipant = createMobile("mobile.ios");

        if (iosParticipant != null)
        {
            testJoinConference(iosParticipant);

            destroyMobile(iosParticipant);
        }
        else
        {
            throw new SkipException("No iOS driver config.");
        }
    }

    private void testJoinConference(MobileParticipant participant)
    {
        participant.joinConference(getRoomName());

        ConferenceView conference = new ConferenceView(participant);

        conference.getRootView().click();

        participant.takeScreenshot("joinedConferenceToolbarToggled");

        ToolbarView toolbar = new ToolbarView(participant);

        toolbar.getHangupButton().click();

        participant.takeScreenshot("justBeforeLeaving");
    }
}
