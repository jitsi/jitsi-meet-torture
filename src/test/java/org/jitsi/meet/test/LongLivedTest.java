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
import org.jitsi.meet.test.tasks.*;

import java.util.*;
import java.util.concurrent.*;

/**
 * A test that will run for hours (configurable) and will continuously check
 * the connection and the conference and if it detects a problem will fail.
 * @author Damian Minkov
 */
public class LongLivedTest
    extends TestCase
{
    /**
     * A test where we read some configurations or fallback to default values
     * and we expect a conference to be already established (by SetupConference)
     * and we keep checking whether this is still the case, and if something
     * is not working we fail. This one is supposed to run for a long period
     * of time.
     */
    public void testLongLive()
    {
        String timeToRunInMin = System.getProperty("longlived.duration");

        // default is 6 hours
        if(timeToRunInMin == null || timeToRunInMin.length() == 0)
            timeToRunInMin = "360";

        final int minutesToRun = Integer.valueOf(timeToRunInMin);

        // execute every 10 secs.
        // sometimes the check is executed once more
        // at the time while we are in a process of disposing
        // the two participants and ~9 secs before finishing successful
        // it fails.
        int millsToRun = (minutesToRun - 1) * 60 * 1000;
        HeartbeatTask heartbeatTask = new HeartbeatTask(millsToRun, true);

        heartbeatTask.start(10 * 1000, 10 * 1000);

        heartbeatTask.await(minutesToRun, TimeUnit.MINUTES);
    }
}
