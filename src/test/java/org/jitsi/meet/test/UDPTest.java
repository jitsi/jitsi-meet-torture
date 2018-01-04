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

import org.jitsi.meet.test.base.*;
import org.jitsi.meet.test.util.*;
import org.testng.annotations.*;

/**
 * Checks that 'owner' and 'secondParticipant' are connected via UDP.
 * @author Boris Grozev
 */
public class UDPTest
    extends AbstractBaseTest
{
    @Override
    public void setup()
    {
        super.setup();

        ensureTwoParticipants();
    }

    /**
     * Asserts that 'owner' and 'secondParticipant' are connected via UDP.
     */
    @Test
    public void udpTest()
    {
        assertEquals(
                 "The owner must be connected through UDP",
                 "udp",
                 MeetUtils.getProtocol(participant1.getDriver()));
        assertEquals(
                "The second participant must be connected through UDP",
                "udp",
                MeetUtils.getProtocol(participant2.getDriver()));
    }
}
