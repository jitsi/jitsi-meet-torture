/*
 * Copyright @ 2020 8x8, Inc.
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

/**
 * A test that works exactly like the {@link MalleusJitsificus} but with blips
 * enabled by default and some health checks that are periodically run on the
 * endpoints.
 */
public class BlipTest extends MalleusJitsificus
{
    @Override
    void healthCheck(int i, WebParticipant participant)
    {
        participant.waitForIceConnected(0 /* no timeout */);

        TestUtils.print("Participant " + i + " is connected.");
    }
}
