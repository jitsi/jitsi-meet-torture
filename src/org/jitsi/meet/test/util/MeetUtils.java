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

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.*;
import org.openqa.selenium.support.ui.*;

import java.util.*;

import static org.junit.Assert.assertFalse;

/**
 * Class contains utility methods related with jitsi-meet application logic.
 *
 * @author Pawel Domas
 */
public class MeetUtils
{
    /**
     * The javascript code which returns {@code true} if we are joined in
     * the muc.
     */
    public static final String IS_MUC_JOINED =
        "return APP.conference.isJoined();";

    /**
     * The javascript code which returns {@code true} if the ICE connection
     * is in state 'connected'.
     */
    public static final String ICE_CONNECTED_CHECK_SCRIPT =
        "return APP.conference.getConnectionState() === 'connected';";

    /**
     * The javascript code which returns {@code true} if the ICE connection
     * is in state 'disconnected'.
     */
    public static final String ICE_DISCONNECTED_CHECK_SCRIPT =
        "return APP.conference.getConnectionState() === 'disconnected';";

    /**
     * The javascript code which returns {@code true} if etherpad is enabled.
     */
    public static final String ETHERPAD_ENABLED_CHECK_SCRIPT =
        "return config.etherpad_base !== undefined;";


    /**
     * Obtains the RTP bundle port used by the given <tt>participant</tt>.
     *
     * @param participant the <tt>WebDriver</tt> instance of the participant for
     * whose bundle port is to be obtained.
     *
     * @return a <tt>String</tt> with the bundle port number.
     */
    public static String getBundlePort(WebDriver participant)
    {
        return TestUtils.executeScriptAndReturnString(
            participant,
            "return APP.conference._room.room.session.peerconnection."
                + "localDescription.sdp.split('\\r\\n')."
                + "filter(function(line){ "
                + "return line.indexOf('a=candidate:') !== -1 "
                + "&& line.indexOf(' udp ') !== -1; })[0].split(' ')[5];");
    }

    /**
     * Returns resource JID which corresponds to XMPP MUC nickname of the given
     * <tt>participant</tt>.
     *
     * @param participant the <tt>WebDriver</tt> instance which runs conference
     *                    participant.
     * @return resource JID which corresponds to XMPP MUC nickname of the given
     * <tt>participant</tt>.
     */
    public static String getResourceJid(WebDriver participant)
    {
        return (String)((JavascriptExecutor) participant)
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
    public static Double getPeerAudioLevel(WebDriver observer,
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
     * Waits for number of remote streams.
     * @param participant the driver to use for the check.
     * @param n number of remote streams to wait for.
     */
    public static void waitForRemoteStreams(
            final WebDriver participant,
            final int n)
    {
        waitForRemoteStreams(participant, n, 15);
    }

    /**
     * Waits for number of remote streams.
     * @param participant the driver to use for the check.
     * @param n number of remote streams to wait for.
     * @param timeout the maximum amount of time in seconds to wait.
     */
    public static void waitForRemoteStreams(
        final WebDriver participant,
        final int n,
        int timeout)
    {
        new WebDriverWait(participant, timeout)
            .until(new ExpectedCondition<Boolean>()
            {
                public Boolean apply(WebDriver d)
                {
                    return (Boolean)((JavascriptExecutor) participant)
                        .executeScript(
                            "return APP.conference"
                                + ".getNumberOfParticipantsWithTracks() >= "
                                + n + ";");
                }
            });
    }

    /**
     * Waits for the page to be loaded before continuing with the operations.
     * @param driver the webdriver that just loaded a page
     */
    public static void waitForPageToLoad(WebDriver driver)
    {
        ExpectedCondition<Boolean> expectation = new
            ExpectedCondition<Boolean>()
            {
                public Boolean apply(WebDriver driver)
                {
                    return ((JavascriptExecutor)driver)
                        .executeScript("return document.readyState")
                        .equals("complete");
                }
            };
        Wait<WebDriver> wait = new WebDriverWait(driver, 10);
        try
        {
            wait.until(expectation);
        }
        catch(Throwable error)
        {
            assertFalse("Timeout waiting for Page Load Request to complete.",
                true);
        }
    }

    /**
     * Waits until data has been sent and received over the ICE connection
     * in {@code participant}.
     * @param participant the participant.
     */
    public static void waitForSendReceiveData(final WebDriver participant)
    {
        new WebDriverWait(participant, 15)
            .until(new ExpectedCondition<Boolean>()
            {
                public Boolean apply(WebDriver d)
                {
                    Map stats = (Map) ((JavascriptExecutor) participant)
                            .executeScript("return APP.conference.getStats();");

                    Map<String, Long> bitrate =
                            (Map<String, Long>) stats.get("bitrate");

                    if (bitrate != null)
                    {
                        long download = bitrate.get("download");
                        long upload = bitrate.get("upload");

                        if (download > 0 && upload > 0)
                            return true;
                    }

                    return false;
                }
            });
    }

    /**
     * Returns download bitrate.
     * @param participant
     * @return
     */
    public static long getDownloadBitrate(WebDriver participant)
    {
        Map stats = (Map)((JavascriptExecutor) participant)
            .executeScript("return APP.conference.getStats();");

        Map<String,Long> bitrate =
            (Map<String,Long>)stats.get("bitrate");

        if(bitrate != null)
        {
            long download =  bitrate.get("download");
            return download;
        }

        return 0;
    }

    /**
     * Checks whether the strophe connection is connected.
     * @param participant
     * @return
     */
    public static boolean isXmppConnected(WebDriver participant)
    {
        Object res = ((JavascriptExecutor) participant)
            .executeScript(
                "return APP.conference._room.xmpp.connection.connected;");
        return res != null && res.equals(Boolean.TRUE);
    }

    /**
     * Waits until {@code participant} joins the MUC.
     * @param participant the participant.
     * @param timeout the maximum time to wait in seconds.
     */
    public static void waitForParticipantToJoinMUC(
        WebDriver participant, long timeout)
    {
        TestUtils.waitForBoolean(
            participant,
            IS_MUC_JOINED,
            timeout);
    }

    /**
     * Checks whether a participant is in the MUC.
     *
     * @param participant the participant.
     * @return {@code true} if the specified {@code participant} has joined the
     * room; otherwise, {@code false}
     */
    public static boolean isInMuc(WebDriver participant)
    {
        Object res = ((JavascriptExecutor) participant)
            .executeScript(IS_MUC_JOINED);
        return res != null && res.equals(Boolean.TRUE);
    }

    /**
     * Waits 30 sec for the given participant to enter the ICE 'connected'
     * state.
     *
     * @param participant the participant.
     */
    public static void waitForIceConnected(WebDriver participant)
    {
        waitForIceConnected(participant, 30);
    }

    /**
     * Waits for the given participant to enter the ICE 'connected' state.
     *
     * @param participant the participant.
     * @param timeout timeout in seconds.
     */
    public static void waitForIceConnected(WebDriver participant, long timeout)
    {
        TestUtils.waitForBoolean(
            participant, ICE_CONNECTED_CHECK_SCRIPT, timeout);
    }

    /**
     * Checks whether the iceConnectionState of <tt>participant</tt> is in state
     * {@code connected}.
     *
     * @param participant driver instance used by the participant for whom we
     *                    want to check.
     * @return {@code true} if the {@code iceConnectionState} of the specified
     * {@code participant} is {@code connected}; otherwise, {@code false}
     */
    public static boolean isIceConnected(WebDriver participant)
    {
        Object res = ((JavascriptExecutor) participant)
            .executeScript(ICE_CONNECTED_CHECK_SCRIPT);
        return res != null && res.equals(Boolean.TRUE);
    }

    /**
     * Waits 30 sec for the given participant to enter the ICE 'disconnected'
     * state.
     *
     * @param participant the participant.
     */
    public static void waitForIceDisconnected(WebDriver participant)
    {
        waitForIceDisconnected(participant, 30);
    }

    /**
     * Waits for the given participant to enter the ICE 'disconnected' state.
     *
     * @param participant the participant.
     * @param timeout timeout in seconds.
     */
    public static void waitForIceDisconnected(
            WebDriver participant,
            long timeout)
    {
        TestUtils.waitForBoolean(
            participant, ICE_DISCONNECTED_CHECK_SCRIPT, timeout);
    }

    /**
     * Waits until {@code participant} joins the MUC.
     * @param participant the participant.
     */
    public static void waitForParticipantToJoinMUC(WebDriver participant)
    {
        waitForParticipantToJoinMUC(participant, 5);
    }

    /**
     * Returns the transport protocol used by the media connection in the
     * Jitsi-Meet conference running in <tt>driver</tt>, or an error string
     * (beginning with "error:") or null on failure.
     * @return the transport protocol used by the media connection in the
     * Jitsi-Meet conference running in <tt>driver</tt>.
     * @param driver the <tt>WebDriver</tt> running Jitsi-Meet.
     */
    public static String getProtocol(WebDriver driver)
    {
        if (driver == null)
            return "error: driver is null";
        Object protocol = ((JavascriptExecutor) driver).executeScript(
            "try {" +
                "return APP.conference.getStats().transport[0].type;" +
            "} catch (err) { return 'error: '+err; }");

        return (protocol == null) ? null : protocol.toString().toLowerCase();
    }

    /**
     * Returns a JS script which checks if the remote participant's media
     * connection is OK or not.
     *
     * @param peerId the ID(Colibri endpoint ID/MUC nickname) of the remote
     * participant whose connection is to be checked with the script produced.
     * @param isActive tells whether the script should check for connected or
     * disconnected status.
     *
     * @return a <tt>String</tt> representing JS script.
     */
    public static String isConnectionActiveScript(String     peerId,
                                                  boolean    isActive)
    {
        return
            "return APP.conference.isParticipantConnectionActive('"
                + peerId + "') " + ( isActive ? "!==" : "===" ) + "false;";
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
