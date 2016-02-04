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

/**
 * Tests whether the Jitsi-Videobridge single-port mode works. Note that this
 * mode may need to be enabled on the bridge (depending on the version).
 *
 * @author Boris Grozev
 */
public class SinglePortTest
        extends TestCase
{
    /**
     * Constructs test.
     * @param name the method name for the test.
     */
    public SinglePortTest(String name)
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

        suite.addTest(new SinglePortTest("singlePortTest"));

        return suite;
    }

    /**
     * Closes the second participant.
     * Starts it again with UDP disabled and checks that it is connected over
     * TCP.
     */
    public void singlePortTest()
    {
        WebDriver owner = ConferenceFixture.getOwner();
        WebDriver secondParticipant = ConferenceFixture.getSecondParticipant();

        // Just make sure everyone is ready
        ConferenceFixture.waitForSendReceiveData(owner);
        ConferenceFixture.waitForSendReceiveData(secondParticipant);

        String ownerPortStr = getRemotePort(owner);
        String secondParticipantPortStr = getRemotePort(secondParticipant);
        int ownerPort = -1;
        int secondParticipantPort = -1;

        try
        {
            ownerPort = Integer.parseInt(ownerPortStr);
            secondParticipantPort = Integer.parseInt(secondParticipantPortStr);
        }
        catch (NumberFormatException nfe)
        {}

        if (ownerPort == -1)
        {
            fail("The remote port for the owner has to be an int: "
                     + ownerPortStr);
        }
        if (secondParticipantPort == -1)
        {
            fail("The remote port for the second participant has to be an int: "
                     + secondParticipantPortStr);
        }

        assertEquals(
                "The two participants must be connected to the same port.",
                ownerPort,
                secondParticipantPort);
    }

    /**
     * Returns the remote port used by the media connection in the
     * Jitsi-Meet conference running in <tt>driver</tt> as a {@code String}, or
     * a {@code String} (which does not parse as an integer) if the port could
     * not be determined.
     * @return the remote port used by the media connection in the
     * Jitsi-Meet conference running in <tt>driver</tt> as a {@code String}, or
     * a {@code String} (which does not parse as an integer) if the port could
     * not be determined.
     * @param driver the <tt>WebDriver</tt> running Jitsi-Meet.
     */
    private static String getRemotePort(WebDriver driver)
    {
        if (driver == null)
            return null;
        Object result = ((JavascriptExecutor) driver).executeScript(
            "try {" +
                    "return APP.conference.getStats().transport[0].ip;" +
                "} catch (err) { return 'error: '+err; }");

        if (result != null && result instanceof String)
        {
            String str = (String) result;
            if (str.startsWith("error: "))
                return str;
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

