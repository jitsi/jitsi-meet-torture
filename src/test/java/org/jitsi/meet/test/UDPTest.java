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

import org.jitsi.meet.test.util.*;
import org.jitsi.meet.test.web.*;

import org.testng.annotations.*;

import static org.testng.Assert.*;

/**
 * Checks that participant1 and participant2 are connected via UDP.
 *
 * @author Boris Grozev
 */
public class UDPTest
    extends WebTestBase
{
    @Override
    public void setupClass()
    {
        super.setupClass();

        ensureTwoParticipants();
    }

    /**
     * Asserts that participant1 and participant2 are connected via UDP.
     */
    @Test
    public void udpTest()
    {
        // just in case wait 1500, this is the interval we use for `config.pcStatsInterval`
        TestUtils.waitMillis(1500);

        assertEquals(
            getParticipant1().getProtocol(),
             "udp",
            "participant1 must be connected through UDP");
        assertEquals(
            getParticipant2().getProtocol(),
            "udp",
            "participant2 must be connected through UDP");
    }
}
