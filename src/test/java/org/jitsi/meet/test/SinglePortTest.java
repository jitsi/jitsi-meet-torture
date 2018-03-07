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
 * Tests whether the Jitsi-Videobridge single-port mode works. Note that this
 * mode may need to be enabled on the bridge (depending on the version).
 *
 * @author Boris Grozev
 */
public class SinglePortTest
    extends WebTestBase
{
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
    public void singlePortTest()
    {
        WebParticipant participant1 = getParticipant1();
        WebParticipant participant2 = getParticipant2();

        // Just make sure everyone is ready
        participant1.waitForSendReceiveData();

        String port1Str = getRemotePort(participant1);
        String port2Str = getRemotePort(participant2);
        int port1 = -1;
        int port2 = -1;

        try
        {
            port1 = Integer.parseInt(port1Str);
            port2 = Integer.parseInt(port2Str);
        }
        catch (NumberFormatException nfe)
        {}

        if (port1 == -1)
        {
            fail("The remote port for participant1 has to be an int: "
                     + port1Str);
        }
        if (port2 == -1)
        {
            fail("The remote port for participant2 has to be an int: "
                     + port2Str);
        }

        assertEquals(
            port1,
            port2,
            "The two participants must be connected to the same port.");
    }

    /**
     * Returns the remote port used by the media connection in the
     * Jitsi-Meet conference running in <tt>participant</tt> as a {@code String}, or
     * a {@code String} (which does not parse as an integer) if the port could
     * not be determined.
     * @return the remote port used by the media connection in the
     * Jitsi-Meet conference running in <tt>participant</tt> as a {@code String}, or
     * a {@code String} (which does not parse as an integer) if the port could
     * not be determined.
     * @param participant the <tt>WebDriver</tt> running Jitsi-Meet.
     */
    private static String getRemotePort(WebParticipant participant)
    {
        // FIXME this should be in Participant and this specific implementation
        // in the WebParticipant (because it executes scripts).
        if (participant == null)
        {
            return null;
        }
        Object result
            = participant.executeScript(
                "try {" +
                        "return APP.conference.getStats().transport[0].ip;" +
                    "} catch (err) { return 'error: '+err; }");

        if (result != null && result instanceof String)
        {
            String str = (String) result;
            if (str.startsWith("error: "))
            {
                return str;
            }
            String[] ipAndPort = str.split(":");
            if (ipAndPort.length > 0)
            {
                return ipAndPort[ipAndPort.length - 1];
            }

            return result.toString();
        }

        return null;
    }
}

