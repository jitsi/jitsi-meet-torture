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
import org.jitsi.meet.test.util.*;
import org.openqa.selenium.*;

import java.io.*;
import java.util.*;

/**
 * Tests whether the Jitsi-Meet client can connect to jitsi-videobridge using
 * TCP.
 *
 * @author Damian Minkov
 * @author Boris Grozev
 */
public class TCPTest
    extends TestCase
{
    /**
     * The property that will indicate that the test will not check whether
     * meet reports connected to tcp.
     * When using TURN statistics do not show whether we are connected with tcp
     * to the turn server.
     */
    public static final String JITSI_MEET_DISABLE_TCP_PROTOCOL_CHECK_PROP
        = "jitsi-meet.tcp.protocol.check.disabled";

    /**
     * The URL fragment which when added to a Jitsi-Meet URL will effectively
     * disable the use of UDP for media.
     */
    private static final String DISABLE_UDP_URL_FRAGMENT
            = "config.webrtcIceUdpDisable=true";

    /**
     * Constructs test.
     * @param name the method name for the test.
     */
    public TCPTest(String name)
    {
        super(name);
    }

    /**
     * Orders the tests.
     * @return the suite with order tests.
     */
    public static junit.framework.Test suite()
    {
        TestSuite suite = new TestSuite();

        if (isEnabled())
        {
            suite.addTest(new TCPTest("tcpTest"));
        }

        return suite;
    }

    /**
     * Closes the second participant.
     * Starts it again with UDP disabled and checks that it is connected over
     * TCP.
     * Stops it, and start it. Checks that it is connected over UDP.
     */
    public void tcpTest()
    {
        ConferenceFixture.quit(ConferenceFixture.getSecondParticipant());
        ConferenceFixture.startParticipant(DISABLE_UDP_URL_FRAGMENT);
        ConferenceFixture.checkParticipantToJoinRoom(
                ConferenceFixture.getSecondParticipant(), 10);
        ConferenceFixture.waitsParticipantToJoinConference(
                ConferenceFixture.getSecondParticipant());
        ConferenceFixture.waitForSendReceiveData(
                ConferenceFixture.getSecondParticipant());

        String mustBeTcpProtocol
                = getProtocol(ConferenceFixture.getSecondParticipant());

        // Bring the conference to the default state before we return.
        ConferenceFixture.quit(ConferenceFixture.getSecondParticipant());
        ConferenceFixture.startParticipant();
        ConferenceFixture.checkParticipantToJoinRoom(
                ConferenceFixture.getSecondParticipant(), 10);
        ConferenceFixture.waitsParticipantToJoinConference(
                ConferenceFixture.getSecondParticipant());
        ConferenceFixture.waitForSendReceiveData(
                ConferenceFixture.getSecondParticipant());

        assertEquals("We must be connected through TCP",
                     "tcp", mustBeTcpProtocol);

        // Since we're connected anyway, we may as well check UDP, too.
        assertEquals("We must be connected through UDP",
                     "udp",
                     getProtocol(ConferenceFixture.getSecondParticipant()));
    }

    /**
     * Returns the transport protocol used by the media connection in the
     * Jitsi-Meet conference running in <tt>driver</tt>, or an error string
     * (beginning with "error:") or null on failure.
     * @return the transport protocol used by the media connection in the
     * Jitsi-Meet conference running in <tt>driver</tt>.
     * @param the <tt>WebDriver</tt> running Jitsi-Meet.
     */
    private String getProtocol(WebDriver driver)
    {
        Object protocol = ((JavascriptExecutor) driver).executeScript(
            "try {" +
                "return APP.connectionquality.getStats().transport[0].type;" +
            "} catch (err) { return 'error: '+err; }");

        return (protocol == null) ? null : protocol.toString().toLowerCase();
    }

    /**
     * Checks whether this test should be run or not.
     * @return <tt>true</tt> if this test should be run, <tt>false</tt>
     * otherwise.
     */
    private static boolean isEnabled()
    {
        if (Boolean.valueOf(
                System.getProperty(
                        JITSI_MEET_DISABLE_TCP_PROTOCOL_CHECK_PROP, "false")))
        {
            System.err.println("Not running TCPTest, because it is disabled.");
            return false;
        }

        // TCP doesn't currently work on firefox.
        String browser
            = System.getProperty(ConferenceFixture.BROWSER_SECONDP_NAME_PROP);
        if (ConferenceFixture.BrowserType.firefox.toString()
                .equalsIgnoreCase(browser))
        {
            System.err.println("Not running TCPTest on firefox.");
            return false;
        }

        return true;
    }
}
