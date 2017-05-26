/*
 * Copyright @ 2017 Atlassian Pty Ltd
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

import static org.junit.Assert.*;
import org.jitsi.meet.test.util.*;
import org.junit.*;
import org.junit.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.*;
import org.openqa.selenium.remote.*;

import java.net.*;

/**
 * It invites X participants that act as senders and 1 participant that acts as
 * a receiver. Initially the senders are muted. Then a sender is unmuted and
 * it becomes the active speaker. The receiver checks that the active speaker
 * is in HD resolution, otherwise the test fails.
 *
 * This needs to be run on a grid with reasonably powered nodes (#vCPU > 4) as
 * it encodes a high resolution video sequence.
 *
 * @author George Politis
 */
public class OnstageQualityTest
{
    /**
     * How many times to change the active speaker.
     */
    public static final String NUM_OF_ROUNDS_PNAME
        = "org.jitsi.meet.test.OnstageQualityTest.NUM_OF_ROUNDS";

    /**
     * How many senders to have in the call.
     */
    public static final String NUM_OF_SENDERS_PNAME
        = "org.jitsi.meet.test.OnstageQualityTest.NUM_OF_SENDERS";

    /**
     * How many times to change the active speaker.
     */
    private static final int NUM_OF_ROUNDS_DEFAULT = 5;

    /**
     * How many senders to have in the call.
     */
    private static final int NUM_OF_SENDERS_DEFAULT = 3;

    /**
     * The senders in the call; we want a bunch of them.
     */
    private WebDriver[] senders;

    /**
     * The receiver in the call; checks the on-stage participant for HD quality.
     */
    private WebDriver receiver;

    /**
     * Allocates the grid nodes, invites the participants and prepares them for
     * the actual test.
     */
    @Before
    public void setUp()
    {
        int sendersLen;
        try
        {
            sendersLen
                = Integer.parseInt(System.getProperty(NUM_OF_SENDERS_PNAME));
        }
        catch (Exception ex)
        {
            sendersLen = NUM_OF_SENDERS_DEFAULT;
        }

        URL remoteAddress = ConferenceFixture.getRemoteDriverAddress();

        ChromeOptions chromeOptions = ConferenceFixture.makeChromeOptions();

        chromeOptions.addArguments("use-file-for-fake-video-capture" +
            "=resources/FourPeople_1280x720_60.y4m");

        chromeOptions.addArguments("use-file-for-fake-audio-capture" +
                "=resources/fakeAudioStream.wav");

        Capabilities desiredCapabilities =
            ConferenceFixture.makeChromeCaps(chromeOptions);

        String senderUrl
            = System.getProperty(ConferenceFixture.JITSI_MEET_URL_PROP)
            + "/onstageQualityTest#config.channelLastN=0";

        // Start the senders.
        senders = new WebDriver[sendersLen];
        for (int i = 0; i < sendersLen; i++)
        {
            senders[i] = new RemoteWebDriver(remoteAddress, desiredCapabilities);
            senders[i].get(senderUrl);
            TestUtils.waitForBoolean(senders[i], MeetUtils.HAS_AUDIO_SCRIPT, 10);
            TestUtils.executeScript(senders[i], MeetUtils.MUTE_AUDIO_SCRIPT);
        }

        // Start the receiver.
        String receiverUrl
            = System.getProperty(ConferenceFixture.JITSI_MEET_URL_PROP)
            + "/onstageQualityTest#config.channelLastN=" + sendersLen;

        receiver = new RemoteWebDriver(remoteAddress, desiredCapabilities);
        receiver.get(receiverUrl);
        TestUtils.waitForBoolean(receiver, MeetUtils.HAS_AUDIO_SCRIPT, 10);
        TestUtils.executeScript(receiver, MeetUtils.MUTE_AUDIO_SCRIPT);
    }

    /**
     * It invites X participants that act as senders and 1 participant that acts as
     * a receiver. Initially the senders are muted. Then a sender is unmuted and
     * it becomes the active speaker. The receiver checks that the active speaker
     * is in HD resolution, otherwise the test fails.
     */
    @Test
    public void testHD()
    {
        int roundsLen;
        try
        {
            roundsLen
                = Integer.parseInt(System.getProperty(NUM_OF_ROUNDS_PNAME));
        }
        catch (Exception ex)
        {
            roundsLen = NUM_OF_ROUNDS_DEFAULT;
        }

        for (int i = 0; i < roundsLen; i++)
        {
            WebDriver dominant = senders[i % senders.length];
            TestUtils.executeScript(dominant, MeetUtils.UNMUTE_AUDIO_SCRIPT);

            try
            {
                Thread.sleep(3000);
            }
            catch (InterruptedException ignored)
            {

            }

            Long height = TestUtils
                .executeScriptAndReturnLong(receiver, MeetUtils.ON_STAGE_HEIGHT);

            assertTrue(
                "on stage participant resolution is: " + height, height > 360);

            TestUtils.executeScript(dominant, MeetUtils.MUTE_AUDIO_SCRIPT);
        }
    }

    /**
     * Disposes the grid nodes.
     */
    @After
    public void tearDown()
    {
        if (senders != null && senders.length != 0)
        {
            for (int i = 0; i < senders.length; i++)
            {
                ConferenceFixture.quit(senders[i], false);
            }
        }

        if (receiver != null)
        {
            ConferenceFixture.quit(receiver, false);
        }
    }
}
