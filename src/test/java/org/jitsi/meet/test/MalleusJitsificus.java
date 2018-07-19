/*
 * Copyright @ 2018 Atlassian Pty Ltd
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
import org.testng.*;
import org.testng.annotations.*;

import java.util.*;

/**
 * @author Damian Minkov
 * @author Boris Grozev
 */
public class MalleusJitsificus
    extends WebTestBase
{
    /**
     * The video file to use as input for the first participant (the sender).
     */
    private static final String INPUT_VIDEO_FILE
        = "resources/FourPeople_1280x720_60.y4m";

    public static final String CONFERENCES_PNAME
        = "org.jitsi.malleus.conferences";
    public static final String PARTICIPANTS_PNAME
        = "org.jitsi.malleus.participants";
    public static final String SENDERS_PNAME
        = "org.jitsi.malleus.senders";
    public static final String ENABLE_P2P_PNAME
        = "org.jitsi.malleus.enable_p2p";
    public static final String DURATION_PNAME
        = "org.jitsi.malleus.duration";
    public static final String ROOM_NAME_PREFIX_PNAME
        = "org.jitsi.malleus.room_name_prefix";


    @DataProvider(name = "dp", parallel = true)
    public Object[][] createData(ITestContext context)
    {
        int numConferences = Integer.valueOf(System.getProperty(CONFERENCES_PNAME));
        int numParticipants = Integer.valueOf(System.getProperty(PARTICIPANTS_PNAME));
        String numSendersStr = System.getProperty(SENDERS_PNAME);
        int numSenders = numSendersStr == null
            ? numParticipants
            : Integer.valueOf(numSendersStr);

        int timeoutMs = 1000 * Integer.valueOf(System.getProperty(DURATION_PNAME));

        String roomNamePrefix = System.getProperty(ROOM_NAME_PREFIX_PNAME);
        if (roomNamePrefix == null)
        {
            roomNamePrefix = "anvil-";
        }

        String enableP2pStr = System.getProperty(ENABLE_P2P_PNAME);
        boolean enableP2p
            = enableP2pStr == null || Boolean.parseBoolean(enableP2pStr);

        // Use one thread per conference.
        context.getCurrentXmlTest().getSuite()
            .setDataProviderThreadCount(numConferences);

        print("will run with:");
        print("conferences="+ numConferences);
        print("participants=" + numParticipants);
        print("senders=" + numSenders);
        print("duration=" + timeoutMs + "ms");
        print("room_name_prefix=" + roomNamePrefix);
        print("enable_p2p=" + enableP2p);

        Object[][] ret = new Object[numConferences][4];
        for (int i = 0; i < numConferences; i++)
        {
            String roomName = roomNamePrefix + i;
            JitsiMeetUrl url
                = participants.getJitsiMeetUrl()
                .setRoomName(roomName)
                // XXX I don't remember if/why these are needed.
                .appendConfig("config.p2p.useStunTurn=true")
                .appendConfig("config.disable1On1Mode=false")

                .appendConfig("config.p2p.enabled=" + (enableP2p ? "true" : "false"));
            ret[i] = new Object[] { url, numParticipants, timeoutMs, numSenders};
        }

        return ret;
    }

    @Test(dataProvider = "dp")
    public void testMain(
        JitsiMeetUrl url, int numberOfParticipants, long waitTime, int numSenders)
        throws InterruptedException
    {
        WebParticipant[] participants = new WebParticipant[numberOfParticipants];
        Thread[] runThreads = new Thread[numberOfParticipants];

        WebParticipantOptions ops
            = new WebParticipantOptions().setFakeStreamVideoFile(
                INPUT_VIDEO_FILE);

        for(int i = 0; i < participants.length; i++)
        {
            participants[i] =
                this.participants
                    .createParticipant("web.participant" + (i + 1), ops);
        }

        for(int i = 0; i < participants.length; i++)
        {
            runThreads[i]
                = runAsync(
                    participants[i],
                    url,
                    waitTime,
                    i >= numSenders /* no video */);
        }

        for (Thread t : runThreads)
        {
            if (t != null)
            {
                t.join();
            }
        }
    }

    private Thread runAsync(final WebParticipant p,
                            JitsiMeetUrl url,
                            long waitTime,
                            boolean muteVideo)
    {
        JitsiMeetUrl _url = url.copy();

        Thread joinThread = new Thread(() -> {

            if (muteVideo)
            {
                _url.appendConfig("startWithVideoMuted=true");
            }

            p.joinConference(_url);

            try
            {
                Thread.sleep(waitTime);
            }
            catch (InterruptedException e)
            {
                throw new RuntimeException(e);
            }
            finally
            {
                p.hangUp();
            }
        });

        joinThread.start();

        return joinThread;
    }

    private void print(String s)
    {
        System.err.println(s);
    }
}
