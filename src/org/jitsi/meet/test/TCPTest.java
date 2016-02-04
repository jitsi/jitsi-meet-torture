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
     */
    public void tcpTest()
    {
        // Initially we should be connected over UDP
        assertEquals("We must be connected through UDP",
                     "udp",
                     getProtocol(ConferenceFixture.getSecondParticipant()));

        ConferenceFixture.close(ConferenceFixture.getSecondParticipant());
        WebDriver secondParticipant
            = ConferenceFixture.startSecondParticipant(DISABLE_UDP_URL_FRAGMENT);
        ConferenceFixture.waitForSecondParticipantToConnect();

        assertEquals("We must be connected through TCP",
                     "tcp",
                     getProtocol(secondParticipant));
    }


    /**
     * Brings the conference to the default state before.
     */
    @Override
    protected void tearDown()
    {
        ConferenceFixture.close(ConferenceFixture.getSecondParticipant());
        ConferenceFixture.waitForSecondParticipantToConnect();
    }

    /**
     * Returns the transport protocol used by the media connection in the
     * Jitsi-Meet conference running in <tt>driver</tt>, or an error string
     * (beginning with "error:") or null on failure.
     * @return the transport protocol used by the media connection in the
     * Jitsi-Meet conference running in <tt>driver</tt>.
     * @param driver the <tt>WebDriver</tt> running Jitsi-Meet.
     */
    static String getProtocol(WebDriver driver)
    {
        if (driver == null)
            return "error: driver is null";
        Object protocol = ((JavascriptExecutor) driver).executeScript(
            "try {" +
                "return APP.conference.getStats().transport[0].type;" +
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
