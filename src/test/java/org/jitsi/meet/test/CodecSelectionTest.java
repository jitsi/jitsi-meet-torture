/*
 * Copyright @ 8x8, Inc.
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
import org.openqa.selenium.support.ui.*;
import org.testng.*;
import org.testng.annotations.*;

import java.time.*;
import java.util.*;

import static org.testng.Assert.*;
import static org.jitsi.meet.test.util.TestUtils.*;

/**
 * Tests for codec selection mechanism.
 *
 * @author Jaya Allamsetty
 */
public class CodecSelectionTest
    extends WebTestBase
{
    @Override
    public void setupClass()
    {
        super.setupClass();
    }

    /**
     * Test asymmetric codecs.
     */
    @Test
    public void testAsymmetricCodecs()
    {
        ensureOneParticipant(getJitsiMeetUrl()
            .appendConfig("config.videoQuality.codecPreferenceOrder=[ 'VP9', 'VP8', 'AV1' ]"));
        WebParticipant participant1 = getParticipant1();

        WebParticipant participant2 = joinSecondParticipant(getJitsiMeetUrl()
            .appendConfig("config.videoQuality.codecPreferenceOrder=[ 'VP8', 'VP9', 'AV1' ]"));
        participant2.waitToJoinMUC();
        participant2.waitForIceConnected();
        participant2.waitForSendReceiveData(true, true);

        // Check if media is playing on both endpoints.
        TestUtils.waitForBoolean(participant1.getDriver(),
            "return JitsiMeetJS.app.testing.isLargeVideoReceived();",
            10);
        TestUtils.waitForBoolean(participant2.getDriver(),
            "return JitsiMeetJS.app.testing.isLargeVideoReceived();",
            10);

        // Check if p1 is sending VP9 and p2 is sending VP8 as per their codec preferences.
        // Except on Firefox because it doesn't support VP9 encode.
        if (participant1.getType().isFirefox())
        {
            TestUtils.waitForBoolean(participant1.getDriver(),
            "return JitsiMeetJS.app.testing.isLocalCameraEncodingVp8();",
            10);
        } 
        else
        {
            TestUtils.waitForBoolean(participant1.getDriver(),
            "return JitsiMeetJS.app.testing.isLocalCameraEncodingVp9();",
            10);
        }

        TestUtils.waitForBoolean(participant2.getDriver(),
            "return JitsiMeetJS.app.testing.isLocalCameraEncodingVp8();",
            10);
    }

    /**
     * Test asymmetric codecs with Av1.
     */
    @Test (dependsOnMethods = { "testAsymmetricCodecs" })
    public void testAsymmetricCodecsWithAv1()
    {
        WebParticipant participant1 = getParticipant1();
        WebParticipant participant2 = getParticipant2();

        JitsiMeetUrl url = getJitsiMeetUrl()
            .appendConfig("config.videoQuality.codecPreferenceOrder=[ 'AV1', 'VP9', 'VP8' ]");

        WebParticipant participant3 = joinThirdParticipant(url, null);
        participant3.waitToJoinMUC();
        participant3.waitForIceConnected();
        participant3.waitForSendReceiveData(true, true);


        // Check if media is playing on p3.
        TestUtils.waitForBoolean(participant3.getDriver(),
            "return JitsiMeetJS.app.testing.isLargeVideoReceived();",
            10);

        // Check if p1 is encoding in VP9, p2 in VP8 and p3 in AV1 as per their codec preferences.
        // Except on Firefox because it doesn't support AV1/VP9 encode and AV1 decode.
        if (participant1.getType().isFirefox())
        {
            TestUtils.waitForBoolean(participant1.getDriver(),
            "return JitsiMeetJS.app.testing.isLocalCameraEncodingVp8();",
            10);
        } 
        else
        {
            TestUtils.waitForBoolean(participant1.getDriver(),
            "return JitsiMeetJS.app.testing.isLocalCameraEncodingVp9();",
            10);
        }

        TestUtils.waitForBoolean(participant2.getDriver(),
            "return JitsiMeetJS.app.testing.isLocalCameraEncodingVp8();",
            10);

        // If there is a Firefox ep in the call, all other eps will switch to VP9.
        if (participant1.getType().isFirefox())
        {
            TestUtils.waitForBoolean(participant3.getDriver(),
            "return JitsiMeetJS.app.testing.isLocalCameraEncodingVp9();",
            10);
        }
        else
        {
            TestUtils.waitForBoolean(participant3.getDriver(),
            "return JitsiMeetJS.app.testing.isLocalCameraEncodingAv1();",
            10);
        }
    }

    /**
     * Test codec switchover and back when a participant that supports only a subset of codecs joins and leaves the
     * call.
     */
    @Test (dependsOnMethods = { "testAsymmetricCodecsWithAv1" })
    public void testCodecSwitchOver()
    {
        hangUpAllParticipants();

        JitsiMeetUrl url = getJitsiMeetUrl()
            .appendConfig("config.videoQuality.codecPreferenceOrder=[ 'VP9', 'VP8', 'AV1' ]");

        ensureTwoParticipants(url, url);
        WebParticipant participant1 = getParticipant1();
        WebParticipant participant2 = getParticipant2();

        // Disable this test on Firefox because it doesn't support VP9 encode.
        if (participant1.getType().isFirefox())
        {
            return;
        }

        // Check if p1 and p2 are encoding in VP9 which is the default codec.
        TestUtils.waitForBoolean(participant1.getDriver(),
            "return JitsiMeetJS.app.testing.isLocalCameraEncodingVp9();",
            10);
        TestUtils.waitForBoolean(participant2.getDriver(),
            "return JitsiMeetJS.app.testing.isLocalCameraEncodingVp9();",
            10);

        JitsiMeetUrl meetUrl = getJitsiMeetUrl()
            .appendConfig("config.videoQuality.codecPreferenceOrder=[ 'VP8' ]");

        WebParticipant participant3 = joinThirdParticipant(meetUrl, null);
        participant3.waitToJoinMUC();
        participant3.waitForIceConnected();
        participant3.waitForSendReceiveData(true, true);

        // Check if all three participants are encoding in VP8 now.
        TestUtils.waitForBoolean(participant1.getDriver(),
            "return JitsiMeetJS.app.testing.isLocalCameraEncodingVp8();",
            10);
        TestUtils.waitForBoolean(participant2.getDriver(),
            "return JitsiMeetJS.app.testing.isLocalCameraEncodingVp8();",
            10);
        TestUtils.waitForBoolean(participant3.getDriver(),
            "return JitsiMeetJS.app.testing.isLocalCameraEncodingVp8();",
            10);

        participant3.hangUp();

        // Check of p1 and p2 have switched to VP9.
        TestUtils.waitForBoolean(participant1.getDriver(),
            "return JitsiMeetJS.app.testing.isLocalCameraEncodingVp9();",
            10);
        TestUtils.waitForBoolean(participant2.getDriver(),
            "return JitsiMeetJS.app.testing.isLocalCameraEncodingVp9();",
            10);
    }
}
