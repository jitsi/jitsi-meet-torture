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
import org.openqa.selenium.*;
import org.testng.*;
import org.testng.annotations.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;
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
    public static final String USE_LITE_MODE_PNAME = "org.jitsi.malleus.use_lite_mode";
    public static final String JOIN_DELAY_PNAME
        = "org.jitsi.malleus.join_delay";
    public static final String SWITCH_SPEAKERS
        = "org.jitsi.malleus.switch_speakers";
    public static final String USE_STAGE_VIEW
        = "org.jitsi.malleus.use_stage_view";
    public static final String USE_HEADLESS
        = "org.jitsi.malleus.enable.headless";
    public static final String SET_SAVE_LOGS
        = "org.jitsi.malleus.set.saveLogs";
    public static final String SENDERS_PER_TAB
        = "org.jitsi.malleus.senders_per_tab";
    public static final String RECEIVERS_PER_TAB
        = "org.jitsi.malleus.receivers_per_tab";

    public static final String SENDER_TABS_PER_BROWSER
        = "org.jitsi.malleus.sender_tabs_per_browser";

    public static final String RECEIVER_TABS_PER_BROWSER
        = "org.jitsi.malleus.receiver_tabs_per_browser";

    public static final String EXTRA_SENDER_PARAMS
        = "org.jitsi.malleus.extra_sender_params";
    public static final String EXTRA_RECEIVER_PARAMS
        = "org.jitsi.malleus.extra_receiver_params";

    // The maximum number of audio senders per browser.  This is a hard-coded Chrome limit, so hard-code it here too.
    public static final int MAX_AUDIO_SENDERS_PER_BROWSER = 16;

    private final Phaser allHungUp = new Phaser();

    private CountDownLatch bridgeSelectionCountDownLatch;

    // The participant threads put the IP of their bridge in this set.
    private final Set<String> bridgeSelection = ConcurrentHashMap.newKeySet();

    // The test thread creates this set.
    // The participant threads check if the IP of their bridge is in this set.
    private Set<String> bridgesToFail;

    // The shared base drivers for senders and receivers respectively
    private SharedBaseDriver senderBaseDriver;
    private SharedBaseDriver receiverBaseDriver;

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

        String senderTabsPerBrowserStr = System.getProperty(SENDER_TABS_PER_BROWSER);
        int senderTabsPerBrowser = senderTabsPerBrowserStr == null
            ? 1
            : Integer.parseInt(senderTabsPerBrowserStr);

        String receiverTabsPerBrowserStr = System.getProperty(RECEIVER_TABS_PER_BROWSER);
        int receiverTabsPerBrowser = receiverTabsPerBrowserStr == null
            ? 1
            : Integer.parseInt(receiverTabsPerBrowserStr);

        String sendersPerTabStr = System.getProperty(SENDERS_PER_TAB);
        int sendersPerTab = sendersPerTabStr == null
            ? 1
            : Integer.parseInt(sendersPerTabStr);

        String receiversPerTabStr = System.getProperty(RECEIVERS_PER_TAB);
        int receiversPerTab = receiversPerTabStr == null
            ? 1
            : Integer.parseInt(receiversPerTabStr);

        int durationMs = 1000 * Integer.parseInt(System.getProperty(DURATION_PNAME));

        //int joinDelayMs = Integer.parseInt(System.getProperty(JOIN_DELAY_PNAME));
        int joinDelayMs = 0;

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

        boolean useLiteMode = Boolean.parseBoolean(System.getProperty(USE_LITE_MODE_PNAME));

        boolean switchSpeakers = Boolean.parseBoolean(System.getProperty(SWITCH_SPEAKERS));

        boolean useStageView = Boolean.parseBoolean(System.getProperty(USE_STAGE_VIEW));

        String extraSenderParams = System.getProperty(EXTRA_SENDER_PARAMS);
        String extraReceiverParams = System.getProperty(EXTRA_RECEIVER_PARAMS);

        // Use one thread per conference.
        context.getCurrentXmlTest().getSuite()
            .setDataProviderThreadCount(numConferences);

        if (!useLoadTest && (sendersPerTab > 1 || receiversPerTab > 1))
        {
            print("WARNING: multiple clients per tab only supported in load-test mode");
            sendersPerTab = 1;
            receiversPerTab = 1;
        }

        print("will run with:");
        print("conferences="+ numConferences);
        print("participants=" + numParticipants);
        print("senders=" + numSenders);
        print("audio senders=" + numAudioSenders + (switchSpeakers ? " (switched)" : ""));
        print("duration=" + durationMs + "ms");
        print("join delay=" + joinDelayMs + "ms");
        print("room_name_prefix=" + roomNamePrefix);
        print("enable_p2p=" + enableP2p);
        print("max_disrupted_bridges_pct=" + maxDisruptedBridges);
        print("regions=" + (regions == null ? "null" : Arrays.toString(regions)));
        print("stage view=" + useStageView);
        print("tabs per browser=" + senderTabsPerBrowser + " send / " + receiverTabsPerBrowser + " recv");
        print("participants per tab=" + sendersPerTab + " send / " + receiversPerTab + " recv");
        print("extra sender params=" + extraSenderParams);
        print("extra receiver params=" + extraReceiverParams);

        senderBaseDriver = new SharedBaseDriver(senderTabsPerBrowser);
        receiverBaseDriver = new SharedBaseDriver(receiverTabsPerBrowser);

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

            if (useStageView)
                url.appendConfig("config.disableTileView=true");

            if (useLoadTest)
            {
                url.setServerUrl(url.getServerUrl() + "/_load-test");
            }

            ret[i] = new Object[] {
                url, numParticipants, durationMs, joinDelayMs, numSenders, numAudioSenders,
                regions, maxDisruptedBridges,
                switchSpeakers,
                senderTabsPerBrowser, receiverTabsPerBrowser,
                sendersPerTab, receiversPerTab,
                extraSenderParams, extraReceiverParams,
                useLiteMode
            };
        }

        return ret;
    }

    @Test(dataProvider = "dp")
    public void testMain(
        JitsiMeetUrl url, int numberOfParticipants,
        long durationMs, long joinDelayMs, int numSenders, int numAudioSenders,
        String[] regions, float blipMaxDisruptedPct, boolean switchSpeakers,
        int senderTabsPerBrowser, int receiverTabsPerBrowser,
        int sendersPerTab, int receiversPerTab,
        String extraSenderParams, String extraReceiverParams,
        boolean useLiteMode)
        throws Exception
    {
        List<MalleusTask> malleusTasks = new ArrayList<>(numberOfParticipants);
        List<SpeakerTask> speakerTasks = new ArrayList<>( switchSpeakers ? numAudioSenders : 0);

        bridgeSelectionCountDownLatch = new CountDownLatch(numberOfParticipants);

        ScheduledExecutorService pool = Executors.newScheduledThreadPool(numberOfParticipants + 2);

        boolean disruptBridges = blipMaxDisruptedPct > 0;

        if ((sendersPerTab == 0 && receiversPerTab == 0) || (senderTabsPerBrowser == 0 && receiverTabsPerBrowser == 0))
        {
            return;
        }

        if (numSenders > 0 && (sendersPerTab == 0 || senderTabsPerBrowser == 0))
        {
            print("Cannot have " + numSenders + " senders with " + sendersPerTab + " senders per tab or " +
                senderTabsPerBrowser + " sender tabs per browser");
            return;
        }

        int numReceivers = numberOfParticipants - numSenders;
        if (numReceivers > 0 && (receiversPerTab == 0 || receiverTabsPerBrowser == 0))
        {
            print("Cannot have " + numReceivers + " receivers with " + receiversPerTab + " receivers per tab or " +
                receiverTabsPerBrowser + " receivers tabs per browser");
            return;
        }

        SharedBaseDriver sharedBaseDriver;
        int audioSenders = 0;

        for (int i = 0; i < numberOfParticipants; )
        {
            boolean sender = i < numSenders;
            boolean audioSender = audioSenders < numAudioSenders;

            JitsiMeetUrl urlCopy = url.copy();

            int numClients;
            boolean multitab;

            if (sender)
            {
                // N.B. this does the right thing for null or empty values
                urlCopy.appendConfig(extraSenderParams);
                numClients = sendersPerTab;
                if (i + numClients > numSenders)
                {
                    numClients = numSenders - i;
                }
                multitab = (senderTabsPerBrowser > 1);
                sharedBaseDriver = senderBaseDriver;
            }
            else
            {
                urlCopy.appendConfig(extraReceiverParams);
                numClients = receiversPerTab;

                if (useLiteMode)
                {
                    urlCopy.appendConfig("config.flags.runInLiteMode=true");
                }
                multitab = (receiverTabsPerBrowser > 1);
                sharedBaseDriver = receiverBaseDriver;
            }

            if (audioSender && audioSenders + numClients > numAudioSenders)
            {
                numClients = numAudioSenders - audioSenders;
            }

            if (i + numClients > numberOfParticipants)
            {
                numClients = numberOfParticipants - i;
            }

            MalleusTask task = new MalleusTask(
                i,
                urlCopy,
                durationMs,
                joinDelayMs,
                i * joinDelayMs,
                audioSender, /* Don't do GUM before unmuting */
                !sender /* no video */,
                switchSpeakers || !audioSender /* no audio */,
                regions == null ? null : regions[i % regions.length],
                numClients,
                disruptBridges,
                multitab ? sharedBaseDriver : null
            );
            malleusTasks.add(task);
            task.start(pool);
            i += numClients;
            if (audioSender)
            {
                audioSenders += numClients;
                if (switchSpeakers)
                {
                    if (numClients == 1)
                    {
                        speakerTasks.add(new SpeakerTask(task, null));
                    }
                    else
                    {
                        for (int j = 0; j < numClients; j++)
                        {
                            speakerTasks.add(new SpeakerTask(task, j));
                        }
                    }
                }
            }
        }

        List<Future<?>> otherTasks = new ArrayList<>();

        if (disruptBridges)
        {
            otherTasks.add(pool.submit(() -> {
                    try
                    {
                        disruptBridges(blipMaxDisruptedPct, durationMs / 1000);
                    }
                    catch (Exception e)
                    {
                        // Let it be returned by Future#get()
                        throw new RuntimeException(e);
                    }
                }
            ));
        }

        if (switchSpeakers)
        {
            otherTasks.add(pool.submit(() -> {
                    try
                    {
                        switchSpeakers(speakerTasks, durationMs + joinDelayMs * numberOfParticipants);
                    }
                    catch (Exception e)
                    {
                        // Let it be returned by Future#get()
                        throw new RuntimeException(e);
                    }
                }
            ));
        }

        List<Throwable> errors = new ArrayList<>();

        for (MalleusTask t: malleusTasks)
        {
            try
            {
                t.waitUntilComplete();
            }
            catch (ExecutionException e)
            {
                errors.add(e.getCause());
            }
        }

        for (Future<?> t: otherTasks)
        {
            try
            {
                t.get();
            }
            catch (ExecutionException e)
            {
                errors.add(e.getCause());
            }
        }

        if (!errors.isEmpty())
        {
            throw new Exception("Failed with multiple errors. Throws the primary.", errors.get(0));
        }
    }

    /** Object that holds the shared base driver that can be used by tabbed drivers. */
    private static class SharedBaseDriver
    {
        private final Object lock = new Object();
        private WebDriver baseDriver = null;

        private final int maxTabs;

        private int count = 0;

        public SharedBaseDriver(int maxTabs)
        {
            this.maxTabs = maxTabs;
        }

        public int createOrGetDriver(Supplier<WebDriver> create, Consumer<WebDriver> use)
        {
            int oldCount;
            synchronized (lock)
            {
                WebDriver driver = baseDriver;
                if (driver == null)
                {
                    baseDriver = create.get();
                }
                else
                {
                    use.accept(driver);
                }
                oldCount = count;
                count++;
                if (count >= maxTabs)
                {
                    baseDriver = null;
                    count = 0;
                }
            }
            return oldCount;
        }
    }

    private class MalleusTask
    {
        private final int i;
        private final JitsiMeetUrl _url;
        private final long durationMs;
        private final long joinDelayMs;
        private boolean audioSender;
        private final boolean muteVideo;
        private boolean muteAudio;
        private final boolean enableFailureDetection;

        public boolean running;

        private Future<?> started;
        private Future<?> complete;
        private ScheduledFuture<?> checking;

        WebParticipant participant;
        private String bridge;

        private ScheduledExecutorService pool;

        private final SharedBaseDriver sharedBaseDriver;

        public MalleusTask(
            int i, JitsiMeetUrl url, long durationMs, long joinDelayMs, long totalJoinDelayMs,
            boolean audioSender, boolean muteVideo, boolean muteAudio, String region, int numClients,
            boolean enableFailureDetection, SharedBaseDriver sharedBaseDriver)
        {
            this.i = i;
            this._url = url;
            this.durationMs = durationMs;
            this.joinDelayMs = totalJoinDelayMs;
            this.audioSender = audioSender;
            this.muteVideo = muteVideo;
            this.muteAudio = muteAudio;
            this.enableFailureDetection = enableFailureDetection;
            this.sharedBaseDriver = sharedBaseDriver;

            if (!audioSender)
            {
                _url.appendConfig("config.disableInitialGUM=true");
            }
            if (muteVideo)
            {
                _url.appendConfig("config.startWithVideoMuted=true");
            }
            if (muteAudio)
            {
                _url.appendConfig("config.startWithAudioMuted=true");
            }
            if (numClients != 1)
            {
                _url.appendConfig("numClients=" + numClients);
                _url.appendConfig("clientInterval=" + joinDelayMs);
            }

            if (region != null)
            {
                _url.appendConfig("config.deploymentInfo.userRegion=\"" + region + "\"");
            }
        }

        public void start(ScheduledExecutorService pool)
        {
            this.pool = pool;

            started = pool.schedule(this::join, joinDelayMs, TimeUnit.MILLISECONDS);
            complete = pool.schedule(this::finish, joinDelayMs + durationMs, TimeUnit.MILLISECONDS);

            if (enableFailureDetection)
            {
                long healthCheckIntervalMs = 5000; // Configure this?

                checking = pool.scheduleWithFixedDelay(this::check,
                    joinDelayMs + healthCheckIntervalMs, healthCheckIntervalMs, TimeUnit.MILLISECONDS);
            }
        }

        private void join()
        {
            boolean useLoadTest = Boolean.parseBoolean(System.getProperty(USE_LOAD_TEST_PNAME));
            boolean useNodeTypes = Boolean.parseBoolean(System.getProperty(USE_NODE_TYPES_PNAME));
            boolean useHeadless = Boolean.parseBoolean(System.getProperty(USE_HEADLESS));
            boolean setSaveLogs = Boolean.parseBoolean(System.getProperty(SET_SAVE_LOGS));

            WebParticipantOptions ops
                = new WebParticipantOptions()
                .setFakeStreamVideoFile(INPUT_VIDEO_FILE)
                .setHeadless(useHeadless)
                .setLoadTest(useLoadTest)
                .setSaveLogs(setSaveLogs);

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

            String configPrefix = "web.participant" + (i + 1);

            if (sharedBaseDriver != null)
            {
                ops.setMultitab(true);
                int numTabs = sharedBaseDriver.createOrGetDriver(
                    () -> {
                        participant = participants.createParticipant(configPrefix, ops);
                        return ((TabbedWebDriver) participant.getDriver()).getBaseDriver();
                    },
                    (baseDriver) ->
                    {
                        ops.setBaseDriver(baseDriver);
                        participant = participants.createParticipant(configPrefix, ops);
                    }
                );
                if (numTabs >= MAX_AUDIO_SENDERS_PER_BROWSER)
                {
                    /* Chrome can't support more than MAX_AUDIO_SENDER_PER_BROWSER audio sender tabs per browser,
                     * so retroactively apply !audioSender and muteAudio to this participant.
                     */
                     if (audioSender)
                     {
                         audioSender = false;
                         _url.appendConfig("config.disableInitialGUM=true");
                     }
                    if (!muteAudio)
                    {
                        muteAudio = true;
                        _url.appendConfig("config.startWithAudioMuted=true");
                    }
                }
            }
            else
            {
                participant = participants.createParticipant(configPrefix, ops);
            }

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
                complete.cancel(true);
                throw e;
            }

            try
            {
                if (enableFailureDetection)
                {
                    bridge = participant.getBridgeIp();
                    bridgeSelection.add(bridge);
                }
            }
            catch (Exception e)
            {
                /* If we fail to fetch the bridge ip, don't block other threads from hanging up. */
                allHungUp.arriveAndDeregister();
                complete.cancel(true);
                if (checking != null)
                {
                    checking.cancel(true);
                }
                throw e;
            }
            finally
            {
                bridgeSelectionCountDownLatch.countDown();
            }

            running = true;
        }

        private void finish()
        {
            running = false;
            if (checking != null)
            {
                checking.cancel(true);
            }

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

        public void waitUntilComplete() throws ExecutionException, InterruptedException
        {
            started.get();
            complete.get();
        }

        private void check()
        {
            try
            {
                participant.waitForIceConnected(0 /* no timeout */);
            }
            catch (Exception ex)
            {
                TestUtils.print("Participant " + i + " is NOT connected.");
                if (!bridgesToFail.contains(bridge))
                {
                    throw ex;
                }
                else
                {
                    // wait for reconnect
                    participant.waitForIceConnected(20);
                    TestUtils.print("Participant " + i + " reconnected.");
                }
            }
        }
    }

        private static class SpeakerTask
        {
            public MalleusTask mTask;
            public final Integer num;
            private boolean muteAudio;
            private boolean spoken = false;

            SpeakerTask(MalleusTask m, Integer n)
            {
                mTask = m;
                num = n;
                muteAudio = m.muteAudio;
            }

            public void muteAudio(boolean mute)
            {
                mTask.pool.execute(() -> doMuteAudio(mute));
            }

            private void doMuteAudio(boolean mute)
            {
                if (mute != muteAudio)
                {
                    muteAudio = mute;

                    if (num != null)
                    {
                        mTask.participant.muteOneAudio(mute, num);
                        if (mute)
                        {
                            TestUtils.print("Muted participant " +
                                (mTask.i + num) + " (" + mTask.i + "+" + num + ")");
                        }
                        else
                        {
                            TestUtils.print("Unmuted participant " +
                                (mTask.i + num) + " (" + mTask.i + "+" + num + ")");
                            spoken = true;
                        }

                    }
                    else
                    {
                        mTask.participant.muteAudio(mute);
                        if (mute)
                        {
                            TestUtils.print("Muted participant " + mTask.i);
                        }
                        else
                        {
                            TestUtils.print("Unmuted participant " + mTask.i);
                            spoken = true;
                        }
                    }
                }
            }
        }


    private void disruptBridges(float blipMaxDisruptedPct, long durationInSeconds)
        throws Exception
    {
        bridgeSelectionCountDownLatch.await();

        bridgesToFail = Collections.synchronizedSet(bridgeSelection.stream()
            .limit((long) (Math.ceil(bridgeSelection.size() * blipMaxDisruptedPct / 100)))
            .collect(Collectors.toSet()));

        Blip.failFor(durationInSeconds).theseBridges(bridgesToFail).call();
    }

    private static final double SINGLE_TALK_MS = 854;
    private static final double DOUBLE_TALK_MS = 226;
    private static final double SILENCE_MS = 456;

    private static final double P_SILENCE = 0.4;

    private static long getDuration(double t)
    {
        return (long)(-t * (Math.log(1 - ThreadLocalRandom.current().nextDouble())));
    }

    /** Randomly switch speakers.
     *  This is modeled on ITU-T P.59, but choosing among N speakers rather than just 2.
     *  (At most 2 at a time.)
     */
    private void switchSpeakers(List<SpeakerTask> speakerTasks, long durationInMs)
        throws InterruptedException
    {
        List<SpeakerTask> currentSpeakers = new ArrayList<>();
        long remainingTime = durationInMs;

        while (remainingTime > 0)
        {
            switch (currentSpeakers.size())
            {
            case 0:
            {
                /* No speakers - add a speaker. */
                SpeakerTask newSpeaker = chooseSpeaker(speakerTasks, currentSpeakers);
                if (newSpeaker != null)
                {
                    newSpeaker.muteAudio(false);
                    currentSpeakers.add(newSpeaker);
                }
                break;
            }

            case 1:
            {
                /* One speaker - either add or remove a speaker. */
                if (ThreadLocalRandom.current().nextDouble() < P_SILENCE)
                {
                    SpeakerTask removedSpeaker = currentSpeakers.get(0);
                    removedSpeaker.muteAudio(true);
                    currentSpeakers.remove(removedSpeaker);
                }
                else
                {
                    SpeakerTask newSpeaker = chooseSpeaker(speakerTasks, currentSpeakers);
                    if (newSpeaker != null)
                    {
                        newSpeaker.muteAudio(false);
                        currentSpeakers.add(newSpeaker);
                    }
                }
                break;
            }

            default:
            {
                /* More than one speaker - remove a speaker. */
                int idx = ThreadLocalRandom.current().nextInt(currentSpeakers.size());
                SpeakerTask removedSpeaker = currentSpeakers.get(idx);
                removedSpeaker.muteAudio(true);
                currentSpeakers.remove(removedSpeaker);
                break;
            }
            }

            long duration;
            switch (currentSpeakers.size())
            {
            case 0:
            {
                /* Silence */
                duration = 0;
                while (duration < 200)
                {
                    duration += getDuration(SILENCE_MS);
                }
                break;
            }
            case 1:
            {
                /* Single-talk */
                duration = getDuration(SINGLE_TALK_MS);
                break;
            }
            default:
            {
                /* Double-talk */
                duration = getDuration(DOUBLE_TALK_MS);
                break;
            }
            }

            long sleepTime = Math.min(duration, remainingTime);
            remainingTime -= sleepTime;
            Thread.sleep(sleepTime);
        }
    }

    /** Randomly choose an active MalleusTask to be the next speaker.
     * If there are N past speakers we choose a past speaker with probability (N / (N + 1))
     * and some other conference member with probability (1 / (N + 1)), unless everyone
     * is a past speaker.
     */
    private SpeakerTask chooseSpeaker(List<SpeakerTask> tasks, List<SpeakerTask> currentSpeakers)
    {
        List<SpeakerTask> pastSpeakers = tasks.stream().
            filter((t) -> t.mTask.running).
            filter((t) -> t.spoken).
            filter((t) -> !currentSpeakers.contains(t)).
            collect(Collectors.toList());

        List<SpeakerTask> nonSpeakers = tasks.stream().
            filter((t) -> t.mTask.running).
            filter((t) -> !t.spoken).
            filter((t) -> !currentSpeakers.contains(t)).
            filter((t) -> t.mTask.audioSender).
            collect(Collectors.toList());

        if (pastSpeakers.isEmpty() && nonSpeakers.isEmpty())
        {
            return null;
        }

        int randMax = pastSpeakers.size() + (nonSpeakers.isEmpty() ? 0 : 1);
        int idx = ThreadLocalRandom.current().nextInt(randMax);
        if (idx < pastSpeakers.size())
        {
            return pastSpeakers.get(idx);
        }
        int idx2 = ThreadLocalRandom.current().nextInt(nonSpeakers.size());
        return nonSpeakers.get(idx2);
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
