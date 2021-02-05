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

import org.jitsi.meet.test.base.*;
import org.jitsi.meet.test.util.*;
import org.jitsi.meet.test.web.*;
import org.openqa.selenium.*;
import org.testng.annotations.*;

/**
 * The tests for LastN feature.
 *
 */
public class LastNTest
    extends WebTestBase
{
    /**
     * Last N test scenario.
     */
    @Test
    public void testLastN()
    {
        hangUpAllParticipants();

        JitsiMeetUrl meetUrl1 = getJitsiMeetUrl()
            .appendConfig("config.startWithAudioMuted=true")
            .appendConfig("config.startWithVideoMuted=true")
            .appendConfig("config.channelLastN=1");
        JitsiMeetUrl meetUrl2 = meetUrl1.copy();
        JitsiMeetUrl meetUrl3 = getJitsiMeetUrl()
            .appendConfig("config.channelLastN=1");
        JitsiMeetUrl meetUrl4 = meetUrl3.copy();

        ensureOneParticipant(meetUrl1);
        WebParticipant participant1 = getParticipant1();
        WebDriver driver1 = participant1.getDriver();

        WebParticipant participant2 = joinSecondParticipant(meetUrl2);
        WebDriver driver2 = participant2.getDriver();
        participant2.waitToJoinMUC();
        participant2.waitForIceConnected();

        WebParticipant participant3 = joinThirdParticipant(meetUrl3, null);
        participant3.waitToJoinMUC();
        participant3.waitForIceConnected();
        participant3.waitForSendReceiveData(true, false);

        String participant3EndpointId = participant3.getEndpointId();
        MeetUIUtils.waitForRemoteVideo(driver1, participant3EndpointId, true);

        // Mute audio on participant3.
        participant3.getToolbar().clickAudioMuteButton();

        WebParticipant participant4 = joinFourthParticipant(meetUrl4);
        participant4.waitToJoinMUC();
        participant4.waitForIceConnected();
        participant4.waitForSendReceiveData(true, true);
        String participant4EndpointId = participant4.getEndpointId();

        // Mute audio on p4 and unmute p3.
        participant4.getToolbar().clickAudioMuteButton();
        participant3.getToolbar().clickAudioMuteButton();

        // Check if p1 starts receiving video from p3 and p4 shows up as ninja.
        MeetUIUtils.waitForNinjaIcon(driver1, participant4EndpointId);
        MeetUIUtils.waitForRemoteVideo(driver1, participant3EndpointId, true);

        // At this point, mute video of p3 and others should be receiving p4's video.
        MeetUIUtils.muteVideoAndCheck(participant3, participant1);
        MeetUIUtils.waitForRemoteVideo(driver1, participant4EndpointId, true);

        // Unmute p3's video and others should switch to receiving p3's video.
        participant3.getToolbar().clickVideoMuteButton();
        MeetUIUtils.waitForRemoteVideo(driver1, participant3EndpointId, true);
        MeetUIUtils.waitForNinjaIcon(driver1, participant4EndpointId);

        // Mute p3's audio and unmute p2's audio. Other endpoints should continue to receive video from p3
        // even though p2 is the dominant speaker.
        participant3.getToolbar().clickAudioMuteButton();
        participant2.getToolbar().clickAudioMuteButton();
        MeetUIUtils.waitForRemoteVideo(driver1, participant3EndpointId, true);
    }
}
