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

import org.jitsi.meet.test.web.*;

import org.testng.annotations.*;

import static org.testng.Assert.*;

/**
 * Tests whether the Jitsi-Meet client can connect to jitsi-videobridge using
 * TCP.
 *
 * @author Damian Minkov
 * @author Boris Grozev
 */
public class TCPTest
    extends WebTestBase
{
    /**
     * The URL fragment which when added to a Jitsi-Meet URL will effectively
     * disable the use of UDP for media.
     */
    public static final String DISABLE_UDP_URL_FRAGMENT
            = "config.webrtcIceUdpDisable=true";

    /**
     * The URL fragment which when added to a Jitsi-Meet URL will effectively
     * disable the use of TCP for media.
     */
    public static final String DISABLE_TCP_URL_FRAGMENT
        = "config.webrtcIceTcpDisable=true";

    /**
     * The test is disabled by default, because TCP is disabled by default in
     * jitsi-meet.
     */
    @Override
    public boolean skipTestByDefault()
    {
        return true;
    }

    @Override
    public void setupClass()
    {
        super.setupClass();

        ensureTwoParticipants();
    }

    /**
     * Closes the second participant.
     * Starts it again with UDP disabled and checks that it is connected over
     * TCP.
     */
    @Test
    public void tcpTest()
    {
        // Initially we should be connected over UDP
        assertEquals(
            getParticipant2().getProtocol(),
            "udp",
            "We must be connected through UDP");

        getParticipant2().hangUp();

        ensureTwoParticipants(
            null,
            getJitsiMeetUrl().appendConfig(DISABLE_UDP_URL_FRAGMENT));

        assertEquals(
            getParticipant2().getProtocol(),
            "tcp",
            "We must be connected through TCP");
    }
}
