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
package org.jitsi.meet.test.util;

import org.jitsi.meet.test.base.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.*;
import org.openqa.selenium.support.ui.*;

import java.util.*;

import static org.testng.Assert.fail;

/**
 * Class contains utility methods related with jitsi-meet application logic.
 *
 * @author Pawel Domas
 */
public class MeetUtils
{
    /**
     * The javascript code which returns {@code true} if the ICE connection
     * is in state 'connected'.
     */
    public static final String ICE_CONNECTED_CHECK_SCRIPT =
        "return APP.conference.getConnectionState() === 'connected';";

    public static final String START_P2P_SCRIPT =
        "APP.conference._startP2P();";

    public static final String STOP_P2P_SCRIPT =
        "APP.conference._stopP2P();";

    /**
     * The javascript code which check whether the P2P ICE session is in state
     * 'connected'.
     */
    public static final String P2P_ICE_CONNECTED_CHECK_SCRIPT =
        "return APP.conference.getP2PConnectionState() === 'connected';";

    public static final String P2P_ICE_DISCONNECTED_CHECK_SCRIPT =
        "return APP.conference.getP2PConnectionState() !== 'connected';";

    /**
     * The javascript code which returns {@code true} if the ICE connection
     * is in state 'disconnected'.
     */
    public static final String ICE_DISCONNECTED_CHECK_SCRIPT =
        "return APP.conference.getConnectionState() === 'disconnected';";

    public static final String DIAL_IN_ENABLED_CHECK_SCRIPT =
        "return Boolean(config.dialInConfCodeUrl && config.dialInNumbersUrl " +
            "&& config.hosts && config.hosts.muc)";

    /**
     * The javascript code which returns {@code true} if etherpad is enabled.
     */
    public static final String ETHERPAD_ENABLED_CHECK_SCRIPT =
        "return config.etherpad_base !== undefined;";

    /**
     * The javascript code which returns {@code true} if reactions are enabled.
     */
    public static final String REACTIONS_ENABLED_CHECK_SCRIPT =
        "return config.enableReactions === true;";

    /**
     * The javascript code which returns {@code true} if help button enabled.
     */
    public static final String HELP_ENABLED_CHECK_SCRIPT =
        "return config.deploymentUrls && config.deploymentUrls.userDocumentationURL !== undefined;";

    /**
     * The javascript code which returns {@code true} if multi-stream is enabled.
     */
    public static final String MULTI_STREAM_ENABLED_CHECK_SCRIPT =
        "return (config.flags?.sourceNameSignaling && config.flags?.sendMultipleVideoStreams) ?? true";

    /**
     * Returns the webrtc stats (in JSON format) of the specific participant
     * of the specified peer connection.
     *
     * @param driver
     * @param useJVB true if we want the stats when the used connection was
     * through jvb, or false if p2p is used.
     */
    public static String getRtpStats(WebDriver driver, boolean useJVB)
    {
        String script = String.format(
                "let pc;"
                + "return APP.conference._room && (pc = APP.conference._room.%s) && pc.peerconnection ? JSON.stringify("
                + "pc.peerconnection.stats) : null",
                useJVB ? "jvbJingleSession" : "p2pJingleSession");

        return TestUtils.executeScriptAndReturnString(driver, script);
    }

    /**
     * Obtains the last value of the desired stat.
     *
     * @param statName the stat name to sample
     * @param useJVB if true, use the JVB peer connection, or the P2P peer
     * connection otherwise.
     */
    private static String getLastStatValueJS(String statName, boolean useJVB)
    {
        return "var stats = APP.conference._room."
            + (useJVB
                ? "jvbJingleSession.peerconnection.stats;"
                : "p2pJingleSession.peerconnection.stats;")
            + "var values = stats['" + statName + "'].values;"
            + "return values[values.length-1];";
    }

    /**
     * Obtains the local candidate type used by the given <tt>participant</tt>.
     *
     * @param driver the <tt>WebDriver</tt> instance of the participant for
     * whose local candidate type is to be obtained.
     * @param useJVB if true, use the JVB peer connection, or the P2P peer
     * connection otherwise.
     *
     * @return an <tt>String</tt> with the local candidate type.
     *
     * @throws RuntimeException if the local candidate type is either invalid
     * or the script has failed to obtain it(the intention of throwing runtime
     * exception is to fail the test).
     */
    public static String getLocalCandidateType(WebDriver driver, boolean useJVB)
    {
        String lastLocalCandidateTypeJS = getLastStatValueJS(
                "Conn-audio-1-0-googLocalCandidateType", useJVB);

        String lastLocalCandidateType = TestUtils
            .executeScriptAndReturnString(driver, lastLocalCandidateTypeJS);

        if (lastLocalCandidateType == null)
        {
            throw new RuntimeException(
                "Failed to obtain the local candidate type through"
                    + " the JS script(might be broken)");
        }

        return lastLocalCandidateType;
    }

    /**
     * Obtains the RTP bundle port used by the given <tt>participant</tt>.
     *
     * @param driver the <tt>WebDriver</tt> instance of the participant for
     * whose bundle port is to be obtained.
     * @param useJVB if true, use the JVB peer connection, or the P2P peer
     * connection otherwise.
     *
     * @return an <tt>int</tt> with the bundle port number.
     *
     * @throws RuntimeException if the port is either invalid or the script has
     * failed to obtain it(the intention of throwing runtime exception is
     * to fail the test).
     */
    public static int getBundlePort(WebDriver driver, boolean useJVB)
    {
        String lastLocalAddressJS
            = getLastStatValueJS("Conn-audio-1-0-googLocalAddress", useJVB);

        String lastLocalAddress = TestUtils
            .executeScriptAndReturnString(driver, lastLocalAddressJS);

        if (lastLocalAddress == null)
        {
            throw new RuntimeException(
                "Failed to obtain the bundle port through"
                    + " the JS script(might be broken)");
        }

        // Try to parse to see if it's a valid integer
        int portNumber = Integer.parseInt(lastLocalAddress.split(":")[1]);
        if (portNumber < 0 || portNumber > 65535)
        {
            throw new RuntimeException(
                "Invalid port number: " + portNumber);
        }

        return portNumber;
    }

    /**
     * Returns resource JID which corresponds to XMPP MUC nickname of the given
     * <tt>participant</tt>.
     *
     * @param driver the <tt>WebDriver</tt> instance which runs conference
     *                    participant.
     * @return resource JID which corresponds to XMPP MUC nickname of the given
     * <tt>participant</tt>.
     * @deprecated use {@link Participant#getEndpointId()} instead.
     */
    @Deprecated
    public static String getResourceJid(WebDriver driver)
    {
        return (String)((JavascriptExecutor) driver)
            .executeScript("return APP.conference.getMyUserId();");
    }

    /**
     * Checks whether given <tt>WebDriver</tt> instance has RTP stats support.
     *
     * @param driver the <tt>WebDriver</tt> for which we're getting the info
     *
     * @return <tt>true</tt> if given <tt>driver</tt> has RTP stats support.
     */
    public static boolean areRtpStatsSupported(WebDriver driver)
    {
        return driver instanceof ChromeDriver;
    }

    /**
     * NOTE: audioLevel == null also when it is 0
     * Returns the audio level for a participant.
     *
     * @param observer
     * @param participant
     * @return
     */
    public static Double getRemoteAudioLevel(WebDriver observer,
                                             WebDriver participant)
    {

        String jid = getResourceJid(participant);

        String script = "" +
            "var level = APP.conference." +
            "getPeerSSRCAudioLevel(\"" + jid + "\");" +
            "return level ? level.toFixed(2) : null;";

        Object levelObj
            = ((JavascriptExecutor) observer).executeScript(script);

        return levelObj != null ?
            Double.valueOf(String.valueOf(levelObj)) : null;
    }

    /**
     * Waits for the page to be loaded before continuing with the operations.
     * @param driver the webdriver that just loaded a page
     */
    public static void waitForPageToLoad(WebDriver driver)
    {
        String checkPageLoadScript = "" +
            "var state = ''; " +
            "try { state = document.readyState; } " +
            "catch (e) { console.warn(e); } " +
            "return state;";

        Wait<WebDriver> wait = new WebDriverWait(driver, 10);
        try
        {
            wait.until(driverInstance -> {
                try
                {
                    return Objects.equals(((JavascriptExecutor) driverInstance)
                        .executeScript(checkPageLoadScript),
                        "complete");
                }
                catch (WebDriverException e)
                {
                    throw e;
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    return false;
                }
            });
        }
        catch(TimeoutException error)
        {
            fail("Timeout waiting for Page Load Request to complete.", error);
        }
    }

    /**
     * Returns download bitrate.
     * @param driver
     * @return
     */
    public static long getDownloadBitrate(WebDriver driver)
    {
        Map stats = (Map)((JavascriptExecutor) driver)
            .executeScript("return APP.conference.getStats();");

        Map<String, Long> bitrate = (Map<String, Long>)stats.get("bitrate");

        if (bitrate != null)
        {
            long download =  bitrate.get("download");
            return download;
        }

        return 0;
    }

    public static void startP2P(WebDriver driver)
    {
        ((JavascriptExecutor) driver).executeScript(START_P2P_SCRIPT);
    }

    public static void stopP2P(WebDriver driver)
    {
        ((JavascriptExecutor) driver).executeScript(STOP_P2P_SCRIPT);
    }

    public static void waitForP2PIceConnected(WebDriver driver)
    {
        // FIXME method with timeout
        TestUtils.waitForBoolean(
            driver, P2P_ICE_CONNECTED_CHECK_SCRIPT, 15);
    }

    public static void waitForP2PIceDisconnected(WebDriver driver)
    {
        // FIXME method with timeout
        TestUtils.waitForBoolean(
            driver, P2P_ICE_DISCONNECTED_CHECK_SCRIPT, 15);
    }

    /**
     * Checks whether the iceConnectionState of <tt>participant</tt> is in state
     * {@code connected}.
     *
     * @param driver driver instance used by the participant for whom we
     *                    want to check.
     * @return {@code true} if the {@code iceConnectionState} of the specified
     * {@code participant} is {@code connected}; otherwise, {@code false}
     */
    public static boolean isIceConnected(WebDriver driver)
    {
        Object res = ((JavascriptExecutor) driver)
            .executeScript(ICE_CONNECTED_CHECK_SCRIPT);
        return res != null && res.equals(Boolean.TRUE);
    }

    /**
     * Waits for the given participant to enter the ICE 'disconnected' state.
     *
     * @param driver the participant.
     * @param timeout timeout in seconds.
     */
    public static void waitForIceDisconnected(
            WebDriver driver,
            long timeout)
    {
        TestUtils.waitForBoolean(
            driver, ICE_DISCONNECTED_CHECK_SCRIPT, timeout);
    }

    /**
     * Returns a JS script which checks if the remote participant's media
     * connection is OK or not.
     *
     * @param endpointId the ID(Colibri endpoint ID/MUC nickname) of the remote
     * participant whose connection is to be checked with the script produced.
     * @param isActive tells whether the script should check for connected or
     * disconnected status.
     *
     * @return a <tt>String</tt> representing JS script.
     */
    public static String isConnectionActiveScript(
        String endpointId,
        boolean isActive)
    {
        return
            "return APP.conference.getParticipantConnectionStatus('"
                + endpointId + "') "
                + ( isActive ? "===" : "!==" )
                + " JitsiMeetJS.constants.participantConnectionStatus.ACTIVE;";
    }

    /**
     * Returns a JS script which checks whether local media connection is OK.
     *
     * @param isActive tells whether the script should check for connected or
     * disconnected status.
     *
     * @return a <tt>String</tt> representing JS script.
     */
    public static String isLocalConnectionActiveScript(boolean isActive)
    {
        return
            "return APP.conference.isConnectionInterrupted() "
                + ( isActive ? "===" : "!==" ) + "false";
    }

    /**
     * Checks whether dial in is enabled.
     * @param driver the <tt>WebDriver</tt> running Jitsi-Meet.
     * @return returns {@code true} if dial in is enabled.
     */
    public static boolean isDialInEnabled(WebDriver driver)
    {
        return TestUtils.executeScriptAndReturnBoolean(
            driver, DIAL_IN_ENABLED_CHECK_SCRIPT);
    }

    /**
     * Checks whether etherpad is enabled.
     * @param driver the <tt>WebDriver</tt> running Jitsi-Meet.
     * @return returns {@code true} if etherpad is enabled.
     */
    public static boolean isEtherpadEnabled(WebDriver driver)
    {
        return TestUtils.executeScriptAndReturnBoolean(
            driver, ETHERPAD_ENABLED_CHECK_SCRIPT);
    }

    /**
     * Checks whether help is enabled.
     * @param driver the <tt>WebDriver</tt> running Jitsi-Meet.
     * @return returns {@code true} if help is enabled.
     */
    public static boolean isHelpEnabled(WebDriver driver)
    {
        return TestUtils.executeScriptAndReturnBoolean(driver, HELP_ENABLED_CHECK_SCRIPT);
    }

    /**
     * Checks whether multi-stream is enabled.
     * @param driver the <tt>WebDriver</tt> running Jitsi-Meet.
     * @return returns {@code true} if multi-stream is enabled.
     */
    public static boolean isMultiStreamEnabled(WebDriver driver)
    {
        return TestUtils.executeScriptAndReturnBoolean(driver, MULTI_STREAM_ENABLED_CHECK_SCRIPT);
    }

    /**
     * Checks whether reactions are enabled.
     * @param driver the <tt>WebDriver</tt> running Jitsi-Meet.
     * @return returns {@code true} if reactions are enabled.
     */
    public static boolean areReactionsEnabled(WebDriver driver)
    {
        return TestUtils.executeScriptAndReturnBoolean(driver, REACTIONS_ENABLED_CHECK_SCRIPT);
    }
}
