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

/**
 * Checks that 'owner' and 'secondParticipant' are connected via UDP.
 * @author Boris Grozev
 */
public class UDPTest
    extends TestCase
{
    /**
     * Constructs this <tt>Test</tt>.
     * @param name the method name for the test.
     */
    public UDPTest(String name)
    {
        super(name);
    }

    /**
     * Orders the tests.
     * @return the suite with ordered tests.
     */
    public static Test suite()
    {
        TestSuite suite = new TestSuite();
        suite.addTest(new UDPTest("udpTest"));
        return suite;
    }

    /**
     * Asserts that 'owner' and 'secondParticipant' are connected via UDP.
     */
    public void udpTest()
    {
        assertEquals(
                 "The owner must be connected through UDP",
                 "udp",
                 TCPTest.getProtocol(ConferenceFixture.getOwner()));
        assertEquals(
                "The second participant must be connected through UDP",
                "udp",
                TCPTest.getProtocol(ConferenceFixture.getSecondParticipant()));
    }
}
