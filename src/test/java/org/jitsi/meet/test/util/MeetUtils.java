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

import static org.testng.Assert.assertFalse;

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
     * Obtains the RTP bundle port used by the given <tt>participant</tt>.
     *
     * @param driver the <tt>WebDriver</tt> instance of the participant for
     * whose bundle port is to be obtained.
     *
     * @return an <tt>int</tt> with the bundle port number.
     *
     * @throws RuntimeException if the port is either invalid or the script has
     * failed to obtain it(the intention of throwing runtime exception is
     * to fail the test).
     */
    public static int getBundlePort(WebDriver driver)
    {
        String portNumberStr
            = TestUtils.executeScriptAndReturnString(
                driver,
                "return APP.conference._room.jvbJingleSession.peerconnection."
                    + "localDescription.sdp.split('\\r\\n')."
                    + "filter(function(line){ "
                    + "return line.indexOf('a=candidate:') !== -1 "
                    + "&& line.indexOf(' udp ') !== -1; })[0].split(' ')[5];");

        if (portNumberStr == null)
        {
            throw new RuntimeException(
                "Failed to obtain the bundle port through"
                    + " the JS script(might be broken)");
        }

        // Try to parse to see if it's a valid integer
        int portNumber = Integer.parseInt(portNumberStr);
        if (portNumber < 0 || portNumber > 65535)
        {
            throw new RuntimeException(
                "Invalid port number: " + portNumberStr);
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
        ExpectedCondition<Boolean> expectation
            = d-> ((JavascriptExecutor) d)
                .executeScript("return document.readyState")
                .equals("complete");
        Wait<WebDriver> wait = new WebDriverWait(driver, 10);
        try
        {
            wait.until(expectation);
        }
        catch(TimeoutException error)
        {
            assertFalse(
                true,
                "Timeout waiting for Page Load Request to complete.");
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

        Map<String,Long> bitrate = (Map<String,Long>)stats.get("bitrate");

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
}
