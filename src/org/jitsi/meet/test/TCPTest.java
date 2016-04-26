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

        suite.addTest(new TCPTest("tcpTest"));

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
                     MeetUtils
                         .getProtocol(ConferenceFixture.getSecondParticipant()));

        ConferenceFixture.close(ConferenceFixture.getSecondParticipant());
        WebDriver secondParticipant
            = ConferenceFixture.startSecondParticipant(DISABLE_UDP_URL_FRAGMENT);
        ConferenceFixture.waitForSecondParticipantToConnect();

        assertEquals("We must be connected through TCP",
                     "tcp",
                     MeetUtils.getProtocol(secondParticipant));
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

}
