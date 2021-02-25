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
import java.util.concurrent.*;
import java.util.stream.*;

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
        = "resources/FourPeople_1280x720_30.y4m";
    private static final int DISABLE_FAILURE_DETECTION = -1;

    public static final String CONFERENCES_PNAME
        = "org.jitsi.malleus.conferences";
    public static final String PARTICIPANTS_PNAME
        = "org.jitsi.malleus.participants";
    public static final String SENDERS_PNAME
        = "org.jitsi.malleus.senders";
    public static final String AUDIO_SENDERS_PNAME
        = "org.jitsi.malleus.audio_senders";
    public static final String ENABLE_P2P_PNAME
        = "org.jitsi.malleus.enable_p2p";
    public static final String DURATION_PNAME
        = "org.jitsi.malleus.duration";
    public static final String ROOM_NAME_PREFIX_PNAME
        = "org.jitsi.malleus.room_name_prefix";
    public static final String REGIONS_PNAME
        = "org.jitsi.malleus.regions";
    public static final String USE_NODE_TYPES_PNAME
        = "org.jitsi.malleus.use_node_types";
    public static final String MAX_DISRUPTED_BRIDGES_PCT_PNAME
        = "org.jitsi.malleus.max_disrupted_bridges_pct";
    public static final String USE_LOAD_TEST_PNAME
        = "org.jitsi.malleus.use_load_test";
    public static final String JOIN_DELAY_PNAME
        = "org.jitsi.malleus.join_delay";

    private final Phaser allHungUp = new Phaser();

    private CountDownLatch bridgeSelectionCountDownLatch;

    @DataProvider(name = "dp", parallel = true)
    public Object[][] createData(ITestContext context)
    {
        // If the tests is not in the list of tests to be executed,
        // skip executing the DataProvider.
        if (isSkipped())
        {
            return new Object[0][0];
        }

        int numConferences = Integer.parseInt(System.getProperty(CONFERENCES_PNAME));
        int numParticipants = Integer.parseInt(System.getProperty(PARTICIPANTS_PNAME));
        String numSendersStr = System.getProperty(SENDERS_PNAME);
        int numSenders = numSendersStr == null
            ? numParticipants
            : Integer.parseInt(numSendersStr);

        String numAudioSendersStr = System.getProperty(AUDIO_SENDERS_PNAME);
        int numAudioSenders = numAudioSendersStr == null
                ? numParticipants
                : Integer.parseInt(numAudioSendersStr);

        int durationMs = 1000 * Integer.parseInt(System.getProperty(DURATION_PNAME));

        int joinDelayMs = Integer.parseInt(System.getProperty(JOIN_DELAY_PNAME));

        String[] regions = null;
        String regionsStr = System.getProperty(REGIONS_PNAME);
        if (regionsStr != null && !"".equals(regionsStr))
        {
            regions = regionsStr.split(",");
        }

        String roomNamePrefix = System.getProperty(ROOM_NAME_PREFIX_PNAME);
        if (roomNamePrefix == null)
        {
            roomNamePrefix = "anvil-";
        }

        String enableP2pStr = System.getProperty(ENABLE_P2P_PNAME);
        boolean enableP2p
            = enableP2pStr == null || Boolean.parseBoolean(enableP2pStr);

        float maxDisruptedBridges = 0;
        String maxDisruptedBridgesStr = System.getProperty(MAX_DISRUPTED_BRIDGES_PCT_PNAME);
        if (!"".equals(maxDisruptedBridgesStr))
        {
            maxDisruptedBridges = Float.parseFloat(maxDisruptedBridgesStr);
        }

        boolean useLoadTest = Boolean.parseBoolean(System.getProperty(USE_LOAD_TEST_PNAME));

        // Use one thread per conference.
        context.getCurrentXmlTest().getSuite()
            .setDataProviderThreadCount(numConferences);

        print("will run with:");
        print("conferences="+ numConferences);
        print("participants=" + numParticipants);
        print("senders=" + numSenders);
        print("audio senders=" + numAudioSenders);
        print("duration=" + durationMs + "ms");
        print("join delay=" + joinDelayMs + "ms");
        print("room_name_prefix=" + roomNamePrefix);
        print("enable_p2p=" + enableP2p);
        print("max_disrupted_bridges_pct=" + maxDisruptedBridges);
        print("regions=" + (regions == null ? "null" : Arrays.toString(regions)));

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
                .appendConfig("config.testing.noAutoPlayVideo=true")
                .appendConfig("config.pcStatsInterval=10000")
                .appendConfig("config.p2p.enabled=" + (enableP2p ? "true" : "false"));

            if (useLoadTest)
            {
                url.setServerUrl(url.getServerUrl() + "/_load-test");
            }

            ret[i] = new Object[] {
                url, numParticipants, durationMs, joinDelayMs, numSenders, numAudioSenders, regions, maxDisruptedBridges
            };
        }

        return ret;
    }

    @Test(dataProvider = "dp")
    public void testMain(
        JitsiMeetUrl url, int numberOfParticipants,
        long durationMs, long joinDelayMs, int numSenders, int numAudioSenders,
        String[] regions, float blipMaxDisruptedPct)
        throws Exception
    {
        ParticipantThread[] runThreads = new ParticipantThread[numberOfParticipants];

        bridgeSelectionCountDownLatch = new CountDownLatch(numberOfParticipants);

        boolean disruptBridges = blipMaxDisruptedPct > 0;
        for (int i = 0; i < numberOfParticipants; i++)
        {
         runThreads[i]
                = new ParticipantThread(
                i,
                url.copy(),
                durationMs,
                i * joinDelayMs,
                i >= numSenders /* no video */,
                i >= numAudioSenders /* no audio */,
                regions == null ? null : regions[i % regions.length],
                disruptBridges ? 0 : DISABLE_FAILURE_DETECTION
             );

            runThreads[i].start();
        }

        if (disruptBridges)
        {
            try
            {
                disruptBridges(blipMaxDisruptedPct, durationMs / 1000, runThreads);
            }
            catch (Exception e)
            {
                throw new Exception("Failed to disrupt the bridges.");
            }
            finally
            {
                for (ParticipantThread t : runThreads)
                {
                    if (t != null)
                    {
                        t.join();
                    }
                }
            }
        }

        int minFailureTolerance = Integer.MAX_VALUE;
        for (ParticipantThread t : runThreads)
        {
            if (t != null)
            {
                t.join();

                minFailureTolerance = Math.min(minFailureTolerance, t.failureTolerance);
            }
        }

        if (disruptBridges && minFailureTolerance < 0)
        {
            throw new Exception("Minimum failure tolerance is less than 0");
        }
    }

    private class ParticipantThread
        extends Thread
    {
        protected final int i;
        protected final JitsiMeetUrl _url;
        protected final long durationMs;
        protected final long joinDelayMs;
        protected final boolean muteVideo;
        protected final boolean muteAudio;
        protected final String region;

        WebParticipant participant;
        public int failureTolerance;
        private String bridge;

        public ParticipantThread(
            int i, JitsiMeetUrl url, long durationMs, long joinDelayMs,
            boolean muteVideo, boolean muteAudio, String region,
            int initialFailureTolerance)
        {
            this.i = i;
            this._url = url;
            this.durationMs = durationMs;
            this.joinDelayMs = joinDelayMs;
            this.muteVideo = muteVideo;
            this.muteAudio = muteAudio;
            this.region = region;
            this.failureTolerance = initialFailureTolerance;
        }

        @Override
        public void run()
        {
            boolean useLoadTest = Boolean.parseBoolean(System.getProperty(USE_LOAD_TEST_PNAME));

            WebParticipantOptions ops
                = new WebParticipantOptions()
                        .setFakeStreamVideoFile(INPUT_VIDEO_FILE)
                        .setLoadTest(useLoadTest);

            if (muteVideo)
            {
                _url.appendConfig("config.startWithVideoMuted=true");
            }
            if (muteAudio)
            {
                _url.appendConfig("config.startWithAudioMuted=true");
            }

            boolean useNodeTypes = Boolean.parseBoolean(System.getProperty(USE_NODE_TYPES_PNAME));

            if (useNodeTypes)
            {
                if (muteVideo)
                {
                    /* TODO: is it okay to have an audio sender use a malleus receiver ep? */
                    ops.setApplicationName("malleusReceiver");
                }
                else
                {
                    ops.setApplicationName("malleusSender");
                }
            }

            if (region != null)
            {
                _url.appendConfig("config.deploymentInfo.userRegion=\"" + region + "\"");
            }

            try
            {
                Thread.sleep(joinDelayMs);
            }
            catch (InterruptedException e)
            {
                throw new RuntimeException(e);
            }

            participant = participants.createParticipant("web.participant" + (i + 1), ops);
            allHungUp.register();
            try
            {
                participant.joinConference(_url);
            }
            catch (Exception e)
            {
                /* If join failed, don't block other threads from hanging up. */
                allHungUp.arriveAndDeregister();
                /* If join failed, don't block bridge disruption. */
                bridgeSelectionCountDownLatch.countDown();
                throw e;
            }

            try
            {
                if (failureTolerance > DISABLE_FAILURE_DETECTION)
                {
                    bridge = participant.getBridgeIp();
                }
            }
            catch (Exception e)
            {
                /* If we fail to fetch the bridge ip, don't block other threads from hanging up. */
                allHungUp.arriveAndDeregister();
                throw e;
            }
            finally
            {
                bridgeSelectionCountDownLatch.countDown();
            }

            try
            {
                if (failureTolerance > DISABLE_FAILURE_DETECTION)
                {
                    check();
                }
                else
                {
                    Thread.sleep(durationMs);
                }
            }
            catch (InterruptedException e)
            {
                allHungUp.arriveAndDeregister();
                throw new RuntimeException(e);
            }
            finally
            {
                try
                {
                    participant.hangUp();
                }
                catch (Exception e)
                {
                    TestUtils.print("Exception hanging up " + participant.getName());
                    e.printStackTrace();
                }
                try
                {
                    /* There seems to be a Selenium or chrome webdriver bug where closing one parallel
                     * Chrome session can cause another one to close too.  So wait for all sessions
                     * to hang up before we close any of them.
                     */
                    allHungUp.arriveAndAwaitAdvance();
                    MalleusJitsificus.this.closeParticipant(participant);
                }
                catch (Exception e)
                {
                    TestUtils.print("Exception closing " + participant.getName());
                    e.printStackTrace();
                }
            }
        }

        private void check()
            throws InterruptedException
        {
            long healthCheckIntervalMs = 5000;
            long remainingMs = durationMs;
            while (remainingMs > 0)
            {
                long sleepTime = Math.min(healthCheckIntervalMs, remainingMs);

                long currentMillis = System.currentTimeMillis();

                Thread.sleep(sleepTime);

                try
                {
                    participant.waitForIceConnected(0 /* no timeout */);
                }
                catch (Exception ex)
                {
                    TestUtils.print("Participant " + i + " is NOT connected (tolerance=" + failureTolerance + ").");
                    failureTolerance--;
                    if (failureTolerance < 0)
                    {
                        throw ex;
                    }
                    else
                    {
                        // wait for reconnect
                        participant.waitForIceConnected(20);
                        TestUtils.print("Participant " + i + " reconnected (tolerance=" + failureTolerance + ").");
                    }
                }

                // we use the elapsedMillis because the healthcheck operation may
                // also take time that needs to be accounted for.
                long elapsedMillis = System.currentTimeMillis() - currentMillis;

                remainingMs -= elapsedMillis;
            }
        }
    }

    private void disruptBridges(float blipMaxDisruptedPct, long durationInSeconds, ParticipantThread[] runThreads)
        throws Exception
    {
        bridgeSelectionCountDownLatch.await();

        Set<String> bridges = Arrays.stream(runThreads)
            .map(t -> t.bridge).collect(Collectors.toSet());

        Set<String> bridgesToFail = bridges.stream()
            .limit((long) (Math.ceil(bridges.size() * blipMaxDisruptedPct / 100))).collect(Collectors.toSet());

        for (ParticipantThread runThread : runThreads)
        {
            if (bridgesToFail.contains(runThread.bridge))
            {
                runThread.failureTolerance = 1;
            }
        }

        Blip.failFor(durationInSeconds).theseBridges(bridgesToFail).call();
    }

    private void print(String s)
    {
        System.err.println(s);
    }

    /**
     * {@inheritDoc}
     */
    public boolean skipTestByDefault()
    {
        // Skip by default.
        return true;
    }
}
