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
import org.jitsi.meet.test.web.*;
import org.jitsi.meet.test.util.*;

import org.openqa.selenium.*;
import org.testng.*;
import org.testng.annotations.*;

import java.io.*;
import java.text.*;
import java.util.*;
import java.util.concurrent.*;

import static org.testng.Assert.*;
import static org.jitsi.meet.test.util.TestUtils.*;

/**
 * The tests for bandwidth estimations.
 *
 * @author George Politis
 */
public class BandwidthEstimationTest
    extends WebTestBase
{
    /**
     * Name of the system property which point to the tc script used to
     * rate-limit the egress bitrate to the second participant (the receiver)
     * ports.
     * See {@link #tcScript}.
     */
    private static final String TC_SCRIPT_PROP_NAME = "tc.script";

    /**
     * Name of the system property which point to the benchmark script used to
     * determine whether or not the jvb bitrate time series is healthy.
     * See {@link #benchmarkScript}.
     */
    private static final String BENCHMARK_SCRIPT_PROP_NAME
        = "bwe-benchmark.script";

    /**
     * The video file to use as input for the first participant (the sender).
     */
    public static final String INPUT_VIDEO_FILE
        = "resources/FourPeople_1280x720_60.y4m";

    /**
     * Default tc script which will work only on linux.
     */
    private static final String DEFAULT_TC_SCRIPT
        = "scripts/tc-port-schedule.sh";

    /**
     * Default benchmark script which will work only on linux.
     */
    private static final String DEFAULT_BENCHMARK_SCRIPT
        = "scripts/bwe-benchmark.py";

    /**
     * Stores the path to the tc script. It is expected that the script
     * supports two arguments:
     * <p>
     * 1. "{port number}" will adjust the outgoing rate of the given
     * port number, based on the schedule provided with the
     * "{schedule}" parameter.
     * <p>
     */
    private static String tcScript;

    /**
     * Stores the path to the benchmark script.
     */
    private static String benchmarkScript;

    /**
     * The options to launch the sender participant with.
     */
    private ParticipantOptions senderOptions;

    /**
     * Utility method that calls {@link #tcScript} to rate-limit given port.
     *
     * @param portNumber the port number to be rate-limited.
     * @param timeout
     * @param unit
     * @throws Exception if anything goes wrong.
     */
    private static void schedulePort(
            int portNumber, long timeout, TimeUnit unit, String ... schedule)
        throws Exception
    {
        if (portNumber == -1)
        {
            throw new IllegalArgumentException("Trying to rate-limit port -1");
        }

        CmdExecutor cmdExecutor = new CmdExecutor();

        List<String> cmdArgs = new LinkedList<>();

        cmdArgs.add(tcScript);
        cmdArgs.add(String.valueOf(portNumber));
        Collections.addAll(cmdArgs, schedule);

        // This will take a while (blocking), depending on the schedule.
        cmdExecutor.executeCmd(cmdArgs, timeout, unit);
    }

    /**
     * Runs the benchmark script with the jvb and the p2p webrtc-stats
     * as a parameter.
     *
     * @param jvb the JVB webrtc-stats file.
     * @param p2p the P2P webrtc-stats file.
     */
    private static int benchmark(File jvb, File p2p)
        throws Exception
    {
        CmdExecutor cmdExecutor = new CmdExecutor();

        List<String> cmdArgs = new LinkedList<>();

        cmdArgs.add(benchmarkScript);
        cmdArgs.add("compare");
        cmdArgs.add("--jvb");
        cmdArgs.add(jvb.toString());
        cmdArgs.add("--p2p");
        cmdArgs.add(p2p.toString());

        return cmdExecutor.executeCmd(cmdArgs, 3, TimeUnit.SECONDS);
    }

    @Override
    public boolean skipTestByDefault()
    {
        return true;
    }

    @Override
    public void setupClass()
    {
        super.setupClass();

        tcScript = System.getProperty(TC_SCRIPT_PROP_NAME);
        if (tcScript == null)
        {
            if ("linux".equalsIgnoreCase(System.getProperty("os.name")))
            {
                tcScript = DEFAULT_TC_SCRIPT;
            }
            else
            {
                print(
                    "WARN no tc script has been specified and "
                        + "the BandwidthEstimationTest will not be "
                        + "executed!");
                throw new SkipException("no tc script has been specified");
            }
        }

        benchmarkScript = System.getProperty(BENCHMARK_SCRIPT_PROP_NAME);
        if (benchmarkScript == null)
        {
            if ("linux".equalsIgnoreCase(System.getProperty("os.name")))
            {
                benchmarkScript = DEFAULT_BENCHMARK_SCRIPT;
            }
            else
            {
                print(
                    "WARN no benchmark script has been specified and "
                        + "the BandwidthEstimationTest will not be "
                        + "executed!");
                throw new SkipException("no tc script has been specified");
            }
        }

        senderOptions
            = new WebParticipantOptions().setFakeStreamVideoFile(
                INPUT_VIDEO_FILE);
    }

    @Test
    public void test10mbit60s1mbit60s10mbit60s()
        throws Exception
    {
        // These are bitrate,duration pairs. The units are important and are
        // defined in TC(8). The test duration should be in seconds.
        String[] schedule = { "10mbit,60", "1mbit,60", "10mbit,60" };
        File jvb = this.test(true, 200, TimeUnit.SECONDS, schedule);
        File p2p = this.test(false, 200, TimeUnit.SECONDS, schedule);

        int result = benchmark(jvb, p2p);
        assertEquals(result, 0);
    }

    /**
     * This test evaluates a congestion control scenario.
     *
     * @param useJVB
     * @param timeout
     * @param unit
     * @param schedule
     * @return the file where the results where written.
     *
     * @throws Exception if something goes wrong.
     */
    private File test(
            boolean useJVB, long timeout, TimeUnit unit, String[] schedule)
        throws Exception
    {
        JitsiMeetUrl senderUrl, receiverUrl;
        if (!useJVB)
        {
            senderUrl = getJitsiMeetUrl();
            senderUrl.appendConfig("config.p2p.enabled=true");
            senderUrl.appendConfig("config.p2p.iceTransportPolicy=\"relay\"");
            senderUrl.appendConfig("config.p2p.useStunTurn=true");

            receiverUrl = getJitsiMeetUrl();
            receiverUrl.appendConfig("config.p2p.enabled=true");
            // XXX we disable TURN server discovery in an attempt to fix a
            // situation where the receiver connects with the relay candidate
            // (so we get the port the TURN server has reserved for the
            // receiver, which is not what we want), and after a few seconds
            // the server reflexive candidate is connected (which is what we
            // want).

            receiverUrl.appendConfig("config.p2p.useStunTurn=false");
        }
        else
        {
            senderUrl = receiverUrl = getJitsiMeetUrl();
        }

        ensureTwoParticipants(senderUrl, receiverUrl, senderOptions, null);

        WebDriver sender = getParticipant1().getDriver();
        assertNotNull(sender);
        WebDriver receiver = getParticipant2().getDriver();
        assertNotNull(receiver);

        // Rate limit the media flow on the receiver and analyze the webrtc
        // internals.
        int receiverPort = MeetUtils.getBundlePort(receiver, useJVB);

        print("Local bundle port for 2: " + receiverPort);

        schedulePort(receiverPort, timeout, unit, schedule);

        // Save the stats results and process them.
        StringBuilder fName = new StringBuilder();
        fName.append(new SimpleDateFormat("yyyyMMddHHmm").format(new Date()));
        fName.append(useJVB ? "-jvb-" : "-webrtc-");
        fName.append(String.join(",", schedule));
        fName.append(".json");

        String stats = MeetUtils.getRtcStats(receiver, useJVB);

        File outputFile
            = new File(FailureListener.createLogsFolder(), fName.toString());

        try (FileWriter fileWriter = new FileWriter(outputFile))
        {
            fileWriter.write(stats);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        getParticipant1().hangUp();
        getParticipant2().hangUp();

        return outputFile;
    }
}
