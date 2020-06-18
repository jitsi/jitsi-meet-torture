/*
 * Copyright @ Atlassian Pty Ltd
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
import org.jitsi.meet.test.web.*;
import org.testng.annotations.*;

import java.io.*;
import java.util.*;

/**
 * Joins conference with 2 participants and checks if the media connection is
 * successfully established. Then hangups the call.
 */
public class ClientPerformanceTest
    extends WebTestBase
{
    private void saveBrowserLogs(
            Participant p, String fileNamePrefix)
    {
        try
        {
            List logs = p.getBrowserLogs();

            if (logs != null)
            {
                File outputFile
                        = new File(
                        "target",
                        fileNamePrefix + "-driver.log");

                try (BufferedWriter out
                             = new BufferedWriter(
                        new FileWriter(outputFile)))
                {
                    for (Object e : logs)
                    {
                        out.write(e.toString());
                        out.newLine();
                    }
                    out.flush();
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Joins the conference, sits there for 60 sec and ends the call.
     */
    @Test
    public void testConference()
            throws InterruptedException
    {
        int participantCount = 10;

        JitsiMeetUrl url = getJitsiMeetUrl()
                //.appendConfig("config.startWithAudioMuted=true")
                .appendConfig("config.p2p.enabled=false")
                //.appendConfig("config.disableThirdPartyRequests=true")
                .appendConfig("config.prejoinPageEnabled=false");
                //.appendConfig("config.startWithVideoMuted=true");

        final WebParticipantOptions options = new WebParticipantOptions();

        List<Thread> threads = new ArrayList<>();

        Participant first = createNextParticipant(options);

        first.joinConference(url);
        first.waitToJoinMUC(10);

        url.appendConfig("config.startWithAudioMuted=true");

        for (int i = 1; i < participantCount; i++) {

            threads.add(new Thread(() ->
               {
                   Participant p = createNextParticipant(options);

                   p.joinConference(url);
                   p.waitToJoinMUC(10);

                   try
                   {
                       Thread.sleep(45000);
                   } catch (InterruptedException e)
                   {
                       e.printStackTrace();
                   }

                   p.hangUp();
               }));
            threads.get(threads.size() - 1).start();
        }

        first.hangUp();

        for(Thread t : threads) {
            try
            {
                t.join();
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }
}
