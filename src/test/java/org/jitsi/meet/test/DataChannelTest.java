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

import junit.framework.*;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;
import org.jitsi.meet.test.util.TestUtils;

/**
 * Tests the WebRTC data channels in a Meet conference (i.e.
 * {@code SctpConnection} in Videobridge speak).
 *
 * @author Lyubomir Marinov
 */
public class DataChannelTest
    extends TestCase
{
    /**
     * Collects the tests implemented by the class {@code DataChannelTest} into
     * a (new) {@code TestSuite}.
     *
     * @return a (new} {@code TestSuite} which collects the tests implemented by
     * the class {@code DataChannelTest}
     */
    public static Test suite()
    {
        TestSuite suite = new TestSuite();

        suite.addTest(new DataChannelTest("testDataChannelOfOwner"));
        suite.addTest(
                new DataChannelTest("testDataChannelOfSecondParticipant"));

        return suite;
    }

    /**
     * Initializes a new {@code DataChannelTest} instance with a specific name.
     *
     * @param name the name of the new instance. It may be the name of the test
     * method to run.
     */
    public DataChannelTest(String name)
    {
        super(name);
    }

    /**
     * Determines whether a WebRTC data channel between Videobridge and a
     * specific participant in the Meet conference is open.
     *
     * @param webDriver the {@code WebDriver} which represents the participant
     * in the Meet conference whose WebRTC data channel to/from Videobridge is
     * to be tested
     * @return {@link Boolean#TRUE} if a WebRTC data channel between Videobridge
     * and {@code webDriver} is open; otherwise, {@link Boolean#FALSE}
     */
    private Boolean isDataChannelOpen(WebDriver webDriver)
    {
        String script = "return APP.conference._room.rtc._channel.isOpen()";

        return TestUtils.executeScriptAndReturnBoolean(webDriver, script);
    }

    /**
     * Determines whether a {@code ServerHello} has been received by a specific
     * Meet conference participant from Videobridge over the respective WebRTC
     * data channel.
     *
     * @param webDriver the {@code WebDriver} which represents the Meet
     * conference participant to check for the receipt of a {@code ServerHello}
     * from Videobridge over the respective WebRTC data channel
     * @return {@link Boolean#TRUE} if the specified Meet conference participant
     * has received a {@code ServerHello} from Videobridge over the respective
     * WebRTC data channel; otherwise, {@link Boolean#FALSE}
     */
    private Boolean isServerHelloReceived(WebDriver webDriver)
    {
        String script = "return APP.conference._room.rtc._channel.receivedServerHello;";

        return TestUtils.executeScriptAndReturnBoolean(webDriver, script);
    }

    /**
     * Sends a {@code ClientHello} message over a (open) WebRTC data channel
     * between Videobridge and a specific participant in the Meet conference.
     *
     * @param webDriver the {@code WebDriver} which represents the participant
     * in the Meet conference who is to send the {@code ClientHello} to
     * Videobridge
     * @return {@link Boolean#TRUE} if {@code webDriver} sent a
     * {@code ClientHello} to Videobridge; otherwise, {@link Boolean#FALSE}
     */
    private Boolean sendClientHello(WebDriver webDriver)
    {
        String script
            = "APP.conference._room.rtc.addListener("
                + "        'rtc.datachannel.ServerHello',"
                + "        function (o) {"
                + "            APP.conference._room.rtc._channel.receivedServerHello = true;"
                + "        });"
                + "if (!APP.conference._room.rtc._channel.isOpen()) {"
                + "    return false;"
                + "}"
                + "APP.conference._room.rtc._channel.receivedServerHello = false;"
                + "APP.conference._room.rtc._channel._channel.send(JSON.stringify({"
                + "    'colibriClass': 'ClientHello'"
                + "}));"
                + "return true;";

        return TestUtils.executeScriptAndReturnBoolean(webDriver, script);
    }

    /**
     * Test the WebRTC data channel between Videobridge and a specific
     * participant in the Meet conference.
     *
     * @param webDriver the {@code WebDriver} which represents the participant
     * in the Meet conference whose WebRTC data channel to/from Videobridge is
     * to be tested
     */
    private void testDataChannel(WebDriver webDriver)
    {
        waitForDataChannelToOpen(webDriver);
        assertTrue(
                "Failed to send a ClientHello to Videobridge over a WebRTC data"
                    + " channel!",
                sendClientHello(webDriver));
        waitToReceiveServerHello(webDriver);
    }

    /**
     * Tests the WebRTC data channel between Videobridge and the Meet conference
     * owner.
     */
    public void testDataChannelOfOwner()
    {
        testDataChannel(ConferenceFixture.getOwner());
    }

    /**
     * Tests the WebRTC data channel between Videobridge and the second
     * participant (i.e. not the owner) in the Meet conference.
     */
    public void testDataChannelOfSecondParticipant()
    {
        testDataChannel(ConferenceFixture.getSecondParticipant());
    }

    /**
     * Waits for the opening of a WebRTC data channel between Videobridge and a
     * specific Meet conference participant.
     *
     * @param webDriver the {@code WebDriver} which represents the participant
     * in the Meet conference whose WebRTC data channel to/from Videobridge is
     * to be waited for
     */
    private void waitForDataChannelToOpen(WebDriver webDriver)
    {
        new WebDriverWait(webDriver, 15).until(
                new ExpectedCondition<Boolean>()
                {
                    @Override
                    public Boolean apply(WebDriver webDriver)
                    {
                        return isDataChannelOpen(webDriver);
                    }
                });
    }

    /**
     * Waits for the receipt of a {@code ServerHello} from Videobridge to a
     * specific Meet conference participant over the respective WebRTC data
     * channel.
     *
     * @param webDriver the {@code WebDriver} which represents the Meet
     * conference participant who is to receive a {@code ServerHello} from
     * Videobridge over the respective WebRTC data channel
     */
    private void waitToReceiveServerHello(WebDriver webDriver)
    {
        new WebDriverWait(webDriver, 15).until(
                new ExpectedCondition<Boolean>()
                {
                    @Override
                    public Boolean apply(WebDriver webDriver)
                    {
                        return isServerHelloReceived(webDriver);
                    }
                });
    }
}
