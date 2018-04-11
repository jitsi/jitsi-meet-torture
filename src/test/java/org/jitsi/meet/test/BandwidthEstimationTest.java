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
 * This class defines tests that evaluate the performance of our bandwidth
 * estimations. Tests in this class are comprised of a chrome sender, a chrome
 * receiver, a JVB, a TURN server and a bottleneck scenario. The test will
 * connect the sender and the receiver through the middlebox and play the
 * bottleneck scenario. When the bottleneck scenario is over, it will extract
 * and store the webrtc-internals of the receiver. This sequence of steps is
 * run once for the JVB and once for the TURN server, so we end up with two
 * webrtc-internals, one for the JVB and one for the TURN server. Finally, it
 * will feed the two webrtc-internals to an analysis script (bwe-benchmark.py)
 * that exits with success if the JVB bitrate time series is healthy, or with
 * failure otherwise.
 *
 * There are several requirements that must be met in order for this test to
 * function correctly:
 * - The user that is executing the test needs to be able to run the tc command.
 * - The JVB and the TURN server need to run on the same host that is running
 *   the test.
 * - A recent version of the python pandas library needs to be installed.
 * - The grid nodes need to be powerful enough to achieve high target bitrates.
 *
 * @author George Politis
 */
public class BandwidthEstimationTest
    extends AbstractTest
{
    /**
     * Name of the system property which point to the tc script used to
     * rate-limit the egress bitrate to the second participant (the receiver)
     * ports.
     * See {@link #tcScript}.
     */
    private static final String TC_SCRIPT_PROP_NAME = "tc.script";

    /**
     * Name of the system which holds the Chrome wrapper script with mahimahi
     * support.
     */
    private static final String CHROME_WRAPPER_PROP_NAME = "chrome.wrapper";

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
    private static final String INPUT_VIDEO_FILE
        = "resources/FourPeople_1280x720_60.y4m";

    /**
     * The Verizon EVDO (driving) uplink packet delivery trace file.
     *
     * see /usr/share/mahimahi/traces/README for more information.
     */
    private static final String VERIZON_EVDO_DRIVING_UP
        = "/usr/share/mahimahi/traces/Verizon-EVDO-driving.up";

    /**
     * The Verizon EVDO (driving) downlink packet delivery trace file.
     */
    private static final String VERIZON_EVDO_DRIVING_DOWN
        = "/usr/share/mahimahi/traces/Verizon-LTE-driving.down";

    /**
     * The Verizon LTE (short) uplink packet delivery trace file.
     *
     * see /usr/share/mahimahi/traces/README for more information.
     */
    private static final String VERIZON_LTE_SHORT_UP
        = "/usr/share/mahimahi/traces/Verizon-LTE-short.up";

    /**
     * The Verizon LTE (short) downlink packet delivery trace file.
     */
    private static final String VERIZON_LTE_SHORT_DOWN
        = "/usr/share/mahimahi/traces/Verizon-LTE-short.down";

    /**
     * The Verizon LTE (driving) uplink packet delivery trace file.
     *
     * see /usr/share/mahimahi/traces/README for more information.
     */
    private static final String VERIZON_LTE_DRIVING_UP
        = "/usr/share/mahimahi/traces/Verizon-LTE-driving.up";

    /**
     * The Verizon LTE (driving) downlink packet delivery trace file.
     */
    private static final String VERIZON_LTE_DRIVING_DOWN
        = "/usr/share/mahimahi/traces/Verizon-LTE-driving.down";

    /**
     * The ATT LTE (driving) uplink packet delivery trace file.
     */
    private static final String ATT_LTE_DRIVING_UP
        = "/usr/share/mahimahi/traces/ATT-LTE-driving.up";

    /**
     * The ATT LTE (driving) downlink packet delivery trace file.
     */
    private static final String ATT_LTE_DRIVING_DOWN
        = "/usr/share/mahimahi/traces/ATT-LTE-driving.down";

    /**
     * The ATT LTE (driving in 2016) uplink packet delivery trace file.
     */
    private static final String ATT_LTE_DRIVING_2016_UP
        = "/usr/share/mahimahi/traces/ATT-LTE-driving-2016.up";

    /**
     * The ATT LTE (driving in 2016) downlink packet delivery trace file.
     */
    private static final String ATT_LTE_DRIVING_2016_DOWN
        = "/usr/share/mahimahi/traces/ATT-LTE-driving-2016.down";

    /**
     * The TMobile LTE (driving) uplink packet delivery trace file.
     */
    private static final String TMOBILE_LTE_DRIVING_UP
        = "/usr/share/mahimahi/traces/TMobile-LTE-driving.up";

    /**
     * The TMobile LTE (driving) downlink packet delivery trace file.
     */
    private static final String TMOBILE_LTE_DRIVING_DOWN
        = "/usr/share/mahimahi/traces/TMobile-LTE-driving.down";

    /**
     * The TMobile LTE (short) uplink packet delivery trace file.
     */
    private static final String TMOBILE_LTE_SHORT_UP
        = "/usr/share/mahimahi/traces/TMobile-LTE-short.up";

    /**
     * The TMobile LTE (short) downlink packet delivery trace file.
     */
    private static final String TMOBILE_LTE_SHORT_DOWN
        = "/usr/share/mahimahi/traces/TMobile-LTE-short.down";

    /**
     * The ATT LTE (driving) network descriptor.
     */
    private static final Network attLTEDriving = new Network(
            "attLTEDriving", ATT_LTE_DRIVING_UP, ATT_LTE_DRIVING_DOWN);

    /**
     * The ATT LTE (driving in 2016) network descriptor.
     */
    private static final Network attLTEDriving2016 = new Network(
            "attLTEDriving2016",
            ATT_LTE_DRIVING_2016_UP, ATT_LTE_DRIVING_2016_DOWN);

    /**
     * The TMobile LTE (driving) network descriptor.
     */
    private static final Network tmobileLTEDriving = new Network(
            "tmobileLTEDriving",
            TMOBILE_LTE_DRIVING_UP, TMOBILE_LTE_DRIVING_DOWN);

    /**
     * The TMobile LTE (short) network descriptor.
     */
    private static final Network tmobileLTEShort = new Network(
            "tmobileLTEShort",
            TMOBILE_LTE_SHORT_UP, TMOBILE_LTE_SHORT_DOWN);

    /**
     * The Verizon LTE (short) network descriptor.
     */
    private static final Network verizonLTEShort = new Network(
            "verizonLTEShort", VERIZON_LTE_SHORT_UP, VERIZON_LTE_SHORT_DOWN);

    /**
     * The Verizon LTE (driving) network descriptor.
     */
    private static final Network verizonLTEDriving = new Network(
            "verizonLTEDriving",
            VERIZON_LTE_DRIVING_UP, VERIZON_LTE_DRIVING_DOWN);

    /**
     * The Verizon EVDO (driving) network descriptor.
     */
    private static final Network verizonEVDODriving = new Network(
            "verizonEVDODriving",
            VERIZON_EVDO_DRIVING_UP, VERIZON_EVDO_DRIVING_DOWN);

    /**
     * The "default" network descriptor (no mahimahi emulation).
     */
    private static final Network def = new Network("def", null, null);

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
     * Default chrome wrapper script with mahimahi(1) support.
     */
    private static final String DEFAULT_CHROME_WRAPPER
        = "scripts/google-chrome-stable-mahimahi.sh";

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
     * Stores the path to the Chrome wrapper with mahimahi(1) support.
     */
    private static String chromeWrapper;

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
     * @param jvb the {@link File} to read the JVB webrtc-stats from.
     * @param p2p the {@link File} to read the webrtc-stats file from.
     * @param analysisFile the {@link File} to store the analysis results in.
     */
    private static int benchmark(File jvbFile, File p2pFile, File analysisFile)
        throws Exception
    {
        CmdExecutor cmdExecutor = new CmdExecutor()
        {
            @Override
            public void configureProcessBuilder(ProcessBuilder processBuilder)
            {
                processBuilder.redirectOutput(analysisFile);
                processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);
            }
        };

        List<String> cmdArgs = new LinkedList<>();

        cmdArgs.add(benchmarkScript);
        cmdArgs.add("compare");
        cmdArgs.add("--jvb");
        cmdArgs.add(jvbFile.toString());
        cmdArgs.add("--p2p");
        cmdArgs.add(p2pFile.toString());

        return cmdExecutor.executeCmd(cmdArgs, 3, TimeUnit.SECONDS);
    }

    /**
     * Returns a human readable name for the bottleneck schedule that is passed
     * in as a parameter. The name won't contain any special characters so it
     * can be used in URLs and file names.
     *
     * @param schedule the bottleneck schedule to humanize
     * @return the human readable name for the bottleneck schedule that is
     * passed in as a parameter.
     */
    private static String humanizeSchedule(String[] schedule)
    {
	StringBuilder sb = new StringBuilder();
        for (String s : schedule)
        {
            sb.append(s.replaceAll(",", ""));
            sb.append("s"); // append an to indicate the duration unit.
        }
	return sb.toString();
    }

    /**
     * Gets a {@link File} with the specified name located in the logs folder.
     *
     * @param fileName the file name.
     * @return a {@link File} with the specified name located in the logs
     * folder.
     */
    private static File getLogFile(String fileName)
    {
        return new File(FailureListener.createLogsFolder(), fileName);
    }

    /**
     * Writes the contents to the output file.
     */
    private static void writeFile(File outputFile, String contents)
        throws IOException
    {
        try (FileWriter fileWriter = new FileWriter(outputFile))
        {
            fileWriter.write(contents);
        }
    }

    @Override
    public boolean skipTestByDefault()
    {
        return true;
    }

    @Override
    public boolean saveDebugInformation()
    {
        // When a test method fails, the failure listener will try to get RTP
        // stats, save screenshots, etc., but, in this particular test class,
        // we quit the drivers/sessions after each bwe scenario run (because
        // different bwe scenarios require different Chrome command line
        // options), which would lead to an exception being raised.

        return false;
    }

    @Override
    public void setupClass(Properties config)
    {
        super.setupClass(config);

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

        chromeWrapper = System.getProperty(CHROME_WRAPPER_PROP_NAME);
        if (chromeWrapper == null)
        {
            if ("linux".equalsIgnoreCase(System.getProperty("os.name")))
            {
                chromeWrapper = DEFAULT_CHROME_WRAPPER;
            }
            else
            {
                print(
                    "WARN no chrome wrapper has been specified and "
                        + "the BandwidthEstimationTest will not be "
                        + "executed!");
                throw new SkipException("no tc script has been specified");
            }
        }
    }

    @DataProvider(name = "dp", parallel = true)
    public Object[][] createData()
    {
        // These are bitrate,duration pairs. The units are important and are
        // defined in TC(8). The test duration should be in seconds.
        String[] schedule1 = { "100mbit,90" };
        String[] schedule2 = { "10mbit,60", "1mbit,60", "10mbit,60" };

        return new Object[][]
        {
            new Object[] { def, schedule1 },
            new Object[] { def, schedule2 },
            new Object[] { verizonLTEShort, schedule1 },
            new Object[] { verizonLTEShort, schedule2 },
            new Object[] { verizonLTEDriving, schedule1 },
            new Object[] { verizonLTEDriving, schedule2 },
            new Object[] { verizonEVDODriving, schedule1 },
            new Object[] { verizonEVDODriving, schedule2 },
            new Object[] { tmobileLTEDriving, schedule1 },
            new Object[] { tmobileLTEDriving, schedule2 },
            new Object[] { tmobileLTEShort, schedule1 },
            new Object[] { tmobileLTEShort, schedule2 },
            new Object[] { attLTEDriving, schedule1 },
            new Object[] { attLTEDriving, schedule2 },
            new Object[] { attLTEDriving2016, schedule1 },
            new Object[] { attLTEDriving2016, schedule2 },
        };
    }

    @Test(dataProvider = "dp")
    public void test(Network network, String[] schedule)
        throws Exception
    {
        Properties config = TestSettings.initSettings();
        ParticipantFactory factory = new WebParticipantFactory(config);
        ParticipantHelper participants = new ParticipantHelper(factory);

        try
        {
        // XXX notice that the webrtc stats gathering default interval is 300
        // seconds.
        String jvbStats = runScenario(participants,
                true, false, true, 200, TimeUnit.SECONDS, network, schedule);
        File jvbFile = getLogFile(
                network.name + "JVB" + humanizeSchedule(schedule) + ".json");
        writeFile(jvbFile, jvbStats);

        String p2pLinearRegressionStats = runScenario(participants,
                false, false, true, 200, TimeUnit.SECONDS, network, schedule);
        File p2pLinearRegressionFile = getLogFile(
                network.name + "Regression"
                + humanizeSchedule(schedule) + ".json");
        writeFile(p2pLinearRegressionFile, p2pLinearRegressionStats);

        String p2pKalmanFilterStats = runScenario(participants,
                false, true, false, 200, TimeUnit.SECONDS, network, schedule);
        File p2pKalmanFilterFile = getLogFile(
                network.name + "Kalman"
                + humanizeSchedule(schedule) + ".json");
        writeFile(p2pKalmanFilterFile, p2pKalmanFilterStats);
        }
        finally
        {
            participants.cleanup();
        }
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
    private static String runScenario(ParticipantHelper participants,
            boolean useJVB, boolean enableRemb, boolean enableTcc,
            long timeout, TimeUnit unit,
            Network network, String[] schedule)
        throws Exception
    {
        JitsiMeetUrl senderUrl, receiverUrl;
        if (!useJVB)
        {
            senderUrl = participants.getJitsiMeetUrl();
            senderUrl.removeFragmentParam("config.callStatsID");

            String ccName = enableRemb ? "Kalman" : "Regression";
            String roomName = network.name + ccName + humanizeSchedule(schedule);
            senderUrl.setRoomName(roomName);
            senderUrl.appendConfig("config.p2p.enabled=true");
            senderUrl.appendConfig("config.p2p.iceTransportPolicy=\"relay\"");
            senderUrl.appendConfig("config.p2p.useStunTurn=true");
            senderUrl.appendConfig("config.enableRemb="
                    + Boolean.toString(enableRemb).toLowerCase());
            senderUrl.appendConfig("config.enableTcc="
                    + Boolean.toString(enableTcc).toLowerCase());


            receiverUrl = participants.getJitsiMeetUrl();
            receiverUrl.removeFragmentParam("config.callStatsID");
            receiverUrl.setRoomName(roomName);
            receiverUrl.appendConfig("config.p2p.enabled=true");
            receiverUrl.appendConfig("config.enableRemb="
                    + Boolean.toString(enableRemb).toLowerCase());
            receiverUrl.appendConfig("config.enableTcc="
                    + Boolean.toString(enableTcc).toLowerCase());
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
            String roomName = network.name + "JVB" + humanizeSchedule(schedule);
            senderUrl = receiverUrl = participants.getJitsiMeetUrl();
            senderUrl.removeFragmentParam("config.callStatsID");
            senderUrl.appendConfig("config.enableRemb="
                    + Boolean.toString(enableRemb).toLowerCase());
            senderUrl.appendConfig("config.enableTcc="
                    + Boolean.toString(enableTcc).toLowerCase());
            senderUrl.setRoomName(roomName);
        }

        WebParticipantOptions receiverOptions = new WebParticipantOptions();

        if ((network.uplink != null && network.uplink != "")
            && (network.downlink != null && network.downlink != ""))
        {
            receiverOptions.setUplink(network.uplink);
            receiverOptions.setDownlink(network.downlink);

            receiverOptions.setBinary(chromeWrapper);
            // XXX The default behavior of the chromedriver is to SIGKILL the
            // launched chrome instance. However, we need the chromedriver to
            // either SIGINT or SIGTERM the chrome wrapper to give it a chance
            // to free up any resources it has allocated. Setting the profile
            // directory does that..
            receiverOptions.setProfileDirectory("/tmp/bwe-receiver-data-dir");
        }

        final WebParticipantOptions senderOptions
            = new WebParticipantOptions().setFakeStreamVideoFile(
                INPUT_VIDEO_FILE);

        Participant p1 = participants
                .createParticipant("web.participant1", senderOptions);
        p1.joinConference(senderUrl);
        WebDriver d1 = p1.getDriver();

        Participant p2 = participants
                .createParticipant("web.participant2", receiverOptions);
        p2.joinConference(receiverUrl);
        WebDriver d2 = p2.getDriver();


        for (int i = 10; i > 0; i--)
        {
            // Wait for up to 10 seconds (10*1000) for a "prflx" candidate.
            String localCandidateType
                = MeetUtils.getLocalCandidateType(d2, useJVB);

            if ("prflx".equalsIgnoreCase(localCandidateType))
            {
                break;
            }

            print(String.format("Waiting %d seconds for a prflx local "
                        + "candidate type. Got: %s.", i, localCandidateType));

            Thread.sleep(1000);
        }

        int receiverPort = MeetUtils.getBundlePort(d2, useJVB);
        if (receiverPort == -1)
        {
            throw new RuntimeException("Failed to obtain the bundle port.");
        }

        print("Receiver port: " + receiverPort);

        // Rate limit the media flow towards the receiver and analyze the
        // webrtc internals.
        // This will take a while (blocking), depending on the schedule.
        schedulePort(receiverPort, timeout, unit, schedule);

        String rtcStats = MeetUtils.getRtpStats(d2, useJVB);

        // XXX prevent ghosts
        participants.hangUpAll();
        // XXX we want to actually quit the drivers because we may wish to
        // launch chrome with different parameters.
        participants.cleanup();

        return rtcStats;
    }

    static class Network
    {
        Network(String name, String uplink, String downlink)
        {
            this.name = name;
            this.uplink = uplink;
            this.downlink = downlink;
        }

        final String name;
        final String uplink;
        final String downlink;
    }
}
