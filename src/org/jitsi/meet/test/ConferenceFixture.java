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

import org.jitsi.meet.test.util.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.*;
import org.openqa.selenium.firefox.*;
import org.openqa.selenium.ie.*;
import org.openqa.selenium.logging.*;
import org.openqa.selenium.remote.*;
import org.openqa.selenium.safari.*;
import org.openqa.selenium.support.ui.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.TimeoutException;
import java.util.logging.*;

import static org.junit.Assert.*;

/**
 * The static fixture which holds the drivers to access the conference
 * participant pages.
 *
 * @author Damian Minkov
 * @author Pawel Domas
 */
public class ConferenceFixture
{
    public static final String JITSI_MEET_URL_PROP = "jitsi-meet.instance.url";

    public static final String FAKE_AUDIO_FNAME_PROP
        = "jitsi-meet.fakeStreamAudioFile";

    /**
     * The property to change tested browser for the owner.
     */
    public static final String BROWSER_OWNER_NAME_PROP = "browser.owner";

    /**
     * The property to change tested chrome browser binary. To specify
     * different versions.
     */
    public static final String BROWSER_CHROME_BINARY_OWNER_NAME_PROP
        = "browser.chrome.owner.binary";

    /**
     * The property to change tested chrome browser binary. To specify
     * different versions.
     */
    public static final String BROWSER_CHROME_BINARY_SECOND_NAME_PROP
        = "browser.chrome.second.binary";

    /**
     * The property to change tested ff browser binary. To specify
     * different versions.
     */
    public static final String BROWSER_FF_BINARY_NAME_PROP
        = "browser.firefox.binary";

    /**
     * The property to change tested browser for the second participant.
     */
    public static final String BROWSER_SECONDP_NAME_PROP
        = "browser.second.participant";

    /**
     * The property to change tested browser for the second participant.
     */
    public static final String BROWSER_THIRDP_NAME_PROP
    = "browser.third.participant";

    /**
     * The property to disable no-sandbox parameter for chrome.
     */
    public static final String DISABLE_NOSANBOX_PARAM
        = "chrome.disable.nosanbox";

    /**
     * The javascript code which returns {@code true} iff the ICE connection
     * is in state 'connected'.
     */
    public static final String ICE_CONNECTED_CHECK_SCRIPT =
        "try {" +
            "var jingle = APP.xmpp.getConnection().jingle.activecall;" +
            "if (jingle.peerconnection.iceConnectionState === 'connected')" +
                "return true;" +
        "} catch (err) { return false; }";

    /**
     * The javascript code which returns {@code true} iff the ICE connection
     * is in state 'disconnected'.
     */
    public static final String ICE_DISCONNECTED_CHECK_SCRIPT =
        "try {" +
            "var jingle = APP.xmpp.getConnection().jingle.activecall;" +
            "if (jingle.peerconnection.iceConnectionState === 'disconnected')" +
                "return true;" +
        "} catch (err) { return false; }";

    /**
     * The available browser type value.
     */
    public enum BrowserType
    {
        chrome, // default one
        firefox,
        safari,
        ie;

        /**
         * Default is chrome.
         * @param browser the browser type string
         * @return the browser type enum item.
         */
        public static BrowserType valueOfString(String browser)
        {
            if(browser == null)
                return chrome;
            else
                return BrowserType.valueOf(browser);
        }
    }

    /**
     * The current room name used.
     */
    public static String currentRoomName;

    /**
     * Full name of wav file which will be streamed through participant's fake
     * audio device.
     */
    private static String fakeStreamAudioFName;

    /**
     * The conference owner in the tests.
     */
    private static WebDriver owner;

    /**
     * The second participant.
     */
    private static WebDriver secondParticipant;

    /**
     * The third participant.
     */
    private static WebDriver thirdParticipant;

    /**
     * Participant drivers enum.
     */
    private enum Participant
    {
        ownerDriver,
        secondParticipantDriver,
        thirdParticipantDriver
    }

    /**
     * Returns the currently allocated conference owner.
     * @return the currently allocated conference owner.
     */
    public static WebDriver getOwner()
    {
        return owner;
    }

    /**
     * Returns the currently allocated second participant. If missing
     * will create it.
     * @return the currently allocated second participant.
     */
    public static WebDriver getSecondParticipant()
    {
        if(secondParticipant == null)
        {
            // this will only happen if some test that quits the
            // second participant fails, and couldn't open it
            startSecondParticipant();
        }

        return secondParticipant;
    }

    /**
     * Returns the currently allocated second participant.
     * @return the currently allocated second participant.
     */
    public static WebDriver getSecondParticipantInstance()
    {
        return secondParticipant;
    }

    /**
     * Returns the currently allocated third participant. If missing
     * will create it.
     * @return the currently allocated third participant.
     */
    public static WebDriver getThirdParticipant()
    {
        if(thirdParticipant == null)
        {
            // this will only happen if some test that quits the
            // third participant fails, and couldn't open it
            startThirdParticipant();
        }

        return thirdParticipant;
    }

    /**
     * Returns the currently allocated third participant.
     * @return the currently allocated third participant.
     */
    public static WebDriver getThirdParticipantInstance()
    {
        return thirdParticipant;
    }

    /**
     * Initializes <tt>currentRoomName</tt> and starts the "owner".
     * @param fragment fragment to be added the URL opened by the "owner".
     * @return the {@code WebDriver} which was started.
     */
    public static WebDriver startOwner(String fragment)
    {
        System.err.println("Starting owner participant.");

        BrowserType browser
            = BrowserType.valueOfString(System.getProperty(
                    BROWSER_OWNER_NAME_PROP));
        if (owner != null)
        {
            System.err.println(
                "Starting 'owner' while an old instance exists!");
        }

        owner = startDriver(browser, Participant.ownerDriver);

        currentRoomName = "torture"
            + String.valueOf((int)(Math.random()*1000000));

        openRoom(owner, fragment, browser);

        ((JavascriptExecutor) owner)
            .executeScript("document.title='Owner'");

        return owner;
    }

    /**
     * Opens the room for the given participant.
     *
     * @param participant to open the current test room.
     * @param fragment adds the given string to the fragment part of the URL
     * @param browser the browser type.
     */
    public static void openRoom(
            WebDriver participant,
            String fragment,
            BrowserType browser)
    {
        String URL = System.getProperty(JITSI_MEET_URL_PROP) + "/"
            + currentRoomName;
        URL += "#config.requireDisplayName=false";
        URL += "&config.callStatsID=false";
        if(fragment != null)
            URL += "&" + fragment;

        if (browser == BrowserType.firefox)
            URL += "&config.firefox_fake_device=true";

        String participantName = getParticipantName(participant);
        System.err.println(participantName + " is opening URL: " + URL);

        participant.get(URL);

        // disables animations
        ((JavascriptExecutor) participant)
            .executeScript("try { jQuery.fx.off = true; } catch(e) {}");
        // Disables toolbar hiding
        ((JavascriptExecutor) participant).executeScript(
            "config.alwaysVisibleToolbar = true");

        ((JavascriptExecutor) participant)
            .executeScript("APP.UI.dockToolbar(true);");

        // Hack-in disabling of callstats (old versions of jitsi-meet don't
        // handle URL parameters)
        ((JavascriptExecutor) participant)
                .executeScript("config.callStatsID=false;");
    }

    /**
     * Starts a {@code WebDriver} instance using default settings.
     * @param browser the browser type.
     * @param participant the participant we are creating a driver for.
     * @return the {@code WebDriver} instance.
     */
    private static WebDriver startDriver(BrowserType browser,
        Participant participant)
    {
        WebDriver wd = startDriverInstance(browser, participant);

        //wd.manage().timeouts().pageLoadTimeout(60, TimeUnit.SECONDS);

        // just wait the instance to start before doing some stuff
        // can kick a renderer bug hanging
        TestUtils.waitMillis(1000);

        return wd;
    }

    /**
     * Starts a <tt>WebDriver</tt> instance using default settings.
     * @param browser the browser type.
     * @param participant the participant we are creating a driver for.
     * @return the <tt>WebDriver</tt> instance.
     */
    private static WebDriver startDriverInstance(BrowserType browser,
        Participant participant)
    {
        // by default we load chrome, but we can load safari or firefox
        if (browser == BrowserType.firefox)
        {
            String browserBinary
                = System.getProperty(BROWSER_FF_BINARY_NAME_PROP);
            if(browserBinary != null && browserBinary.trim().length() > 0)
            {
                File binaryFile = new File(browserBinary);
                if(binaryFile.exists())
                    System.setProperty("webdriver.firefox.bin", browserBinary);
            }

            FirefoxProfile profile = new FirefoxProfile();
            profile.setPreference("media.navigator.permission.disabled", true);
            profile.setAcceptUntrustedCertificates(true);

            return new FirefoxDriver(profile);
        }
        else if (browser == BrowserType.safari)
        {
            return new SafariDriver();
        }
        else if (browser == BrowserType.ie)
        {
            DesiredCapabilities caps = DesiredCapabilities.internetExplorer();
            caps.setCapability("ignoreZoomSetting", true);
            System.setProperty("webdriver.ie.driver.silent", "true");

            return new InternetExplorerDriver(caps);
        }
        else
        {
            System.setProperty("webdriver.chrome.verboseLogging", "true");
            System.setProperty("webdriver.chrome.logfile",
                FailureListener.createLogsFolder() +
                "/chrome-console-" + getParticipantName(participant) + ".log");

            DesiredCapabilities caps = DesiredCapabilities.chrome();
            LoggingPreferences logPrefs = new LoggingPreferences();
            logPrefs.enable(LogType.BROWSER, Level.ALL);
            caps.setCapability(CapabilityType.LOGGING_PREFS, logPrefs);

            final ChromeOptions ops = new ChromeOptions();
            ops.addArguments("use-fake-ui-for-media-stream");
            ops.addArguments("use-fake-device-for-media-stream");
            ops.addArguments("disable-extensions");
            ops.addArguments("disable-plugins");

            String disProp = System.getProperty(DISABLE_NOSANBOX_PARAM);
            if(disProp == null && !Boolean.parseBoolean(disProp))
            {
                ops.addArguments("no-sandbox");
                ops.addArguments("disable-setuid-sandbox");
            }

            // starting version 46 we see crashes of chrome GPU process when
            // running in headless mode
            // which leaves the browser opened and selenium hang forever.
            // There are reports that in older version crashes like that will
            // fallback to software graphics, we try to disable gpu for now
            ops.addArguments("disable-gpu");

            String browserProp;
            if (participant == Participant.secondParticipantDriver)
                browserProp = BROWSER_CHROME_BINARY_SECOND_NAME_PROP;
            else
                browserProp = BROWSER_CHROME_BINARY_OWNER_NAME_PROP;

            String browserBinary = System.getProperty(browserProp);
            if(browserBinary != null && browserBinary.trim().length() > 0)
            {
                File binaryFile = new File(browserBinary);
                if(binaryFile.exists())
                    ops.setBinary(binaryFile);
            }

            if (fakeStreamAudioFName != null)
            {
                ops.addArguments(
                    "use-file-for-fake-audio-capture=" + fakeStreamAudioFName);
            }

            //ops.addArguments("vmodule=\"*media/*=3,*turn*=3\"");
            ops.addArguments("enable-logging");
            ops.addArguments("vmodule=*=3");

            caps.setCapability(ChromeOptions.CAPABILITY, ops);

            try
            {
                final ExecutorService pool = Executors.newFixedThreadPool(1);
                // we will retry four times for 1 minute to obtain
                // the chrome driver, on headless environments chrome hangs
                // and we wait forever
                for (int i = 0; i < 4; i++)
                {
                    try
                    {
                        ChromeDriver res =
                            pool.submit(
                                new Callable<ChromeDriver>()
                            {
                                @Override
                                public ChromeDriver call() throws Exception
                                {
                                    return new ChromeDriver(ops);
                                }
                            }).get(1, TimeUnit.MINUTES);
                        if(res != null)
                            return res;
                    }
                    catch (TimeoutException te)
                    {
                        System.err.println("Timeout waiting for "
                            + "chrome instance! We will retry now, this was our"
                            + "attempt " + i);
                    }

                }
            }
            catch (InterruptedException | ExecutionException e)
            {
                e.printStackTrace();
            }

            // keep the old code
            System.err.println("Just create ChromeDriver, may hang!");
            return new ChromeDriver(ops);
        }
    }

    /**
     * Starts <tt>secondParticipant</tt>.
     * @return the {@code WebDriver} which was started.
     */
    public static WebDriver startSecondParticipant()
    {
        return startSecondParticipant(null);
    }

    /**
     * Starts <tt>secondParticipant</tt>.
     * @param fragment A string to be added to the URL as a parameter (i.e.
     * prefixed with a '&').
     * @return the {@code WebDriver} which was started.
     */
    public static WebDriver startSecondParticipant(String fragment)
    {
        System.err.println("Starting second participant.");

        BrowserType browser
            = BrowserType.valueOfString(
                System.getProperty(BROWSER_SECONDP_NAME_PROP));

        if (secondParticipant != null)
        {
            System.err.println(
                "Starting 'secondParticipant' while an old instance exists!");
        }
        secondParticipant
            = startDriver(browser, Participant.secondParticipantDriver);

        openRoom(secondParticipant, fragment, browser);

        ((JavascriptExecutor) secondParticipant)
            .executeScript("document.title='SecondParticipant'");

        return secondParticipant;
    }

    /**
     * Starts the third participant reusing the already generated room name.
     * @return the {@code WebDriver} which was started.
     */
    public static WebDriver startThirdParticipant()
    {
        System.err.println("Starting third participant.");

        BrowserType browser
            = BrowserType.valueOfString(
                System.getProperty(BROWSER_THIRDP_NAME_PROP));

        if (thirdParticipant != null)
        {
            System.err.println(
                "Starting 'thirdParticipant' while an old instance exists!");
        }
        thirdParticipant
            = startDriver(browser, Participant.thirdParticipantDriver);

        openRoom(thirdParticipant, null, browser);

        ((JavascriptExecutor) thirdParticipant)
            .executeScript("document.title='ThirdParticipant'");

        return thirdParticipant;
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
     * Waits until {@code participant} joins the MUC.
     * @param participant the participant.
     * @param timeout the maximum time to wait in seconds.
     */
    public static void waitForParticipantToJoinMUC(
        WebDriver participant, long timeout)
    {
        TestUtils.waitForBoolean(
            participant,
            "return (APP.xmpp.getConnection() != null) &&" +
                    "APP.xmpp.isMUCJoined();",
            timeout);
    }

    /**
     * Waits 30 sec for the given participant to enter the ICE 'connected'
     * state.
     *
     * @param participant the participant.
     */
    public static void waitForIceCompleted(WebDriver participant)
    {
        waitForIceCompleted(participant, 30);
    }

    /**
     * Waits 30 sec for the given participant to enter the ICE 'connected'
     * state.
     *
     * @param participant the participant.
     * @param timeout timeout in seconds.
     */
    public static void waitForIceCompleted(WebDriver participant, long timeout)
    {
        TestUtils.waitForBoolean(
            participant, ICE_CONNECTED_CHECK_SCRIPT, timeout);
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
    public static void waitForIceDisconnected(WebDriver    participant,
                                              long         timeout)
    {
        TestUtils.waitForBoolean(
            participant, ICE_DISCONNECTED_CHECK_SCRIPT, timeout);
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
     * Checks whether a participant is in the MUC.
     *
     * @param participant the participant.
     * @return {@code true} if the specified {@code participant} has joined the
     * room; otherwise, {@code false}
     */
    public static boolean isInMuc(WebDriver participant)
    {
        Object res = ((JavascriptExecutor) participant)
            .executeScript("return APP.xmpp.isMUCJoined();");
        return res != null && res.equals(Boolean.TRUE);
    }

    /**
     * Returns download bitrate.
     * @param participant
     * @return
     */
    public static long getDownloadBitrate(WebDriver participant)
    {
        Map stats = (Map)((JavascriptExecutor) participant)
            .executeScript("return APP.connectionquality.getStats();");

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
            .executeScript("return APP.xmpp.getConnection().connected;");
        return res != null && res.equals(Boolean.TRUE);
    }

    /**
     * Hangs up the Jitsi-Meet call running in {@code participant} and closes
     * the driver.
     * @param participant the participant.
     */
    public static void close(WebDriver participant)
    {
        if (participant == null)
        {
            System.err.println("close(): participant is null");
            return;
        }

        try
        {
            MeetUIUtils.clickOnToolbarButtonByClass(participant, "icon-hangup");

            TestUtils.waitMillis(500);
        }
        catch(Throwable t)
        {
            t.printStackTrace();
        }

        try
        {
            participant.close();

            TestUtils.waitMillis(500);

            participant.quit();

            TestUtils.waitMillis(500);
        }
        catch(Throwable t)
        {
            t.printStackTrace();
        }

        String instanceName = getParticipantName(participant);
        System.err.println("Closed " + instanceName + ".");

        if (participant == owner)
        {
            owner = null;
        }
        else if (participant == secondParticipant)
        {
            secondParticipant = null;
        }
        else if (participant == thirdParticipant)
        {
            thirdParticipant = null;
        }
    }

    /**
     * Sets the name of wav audio file which will be streamed through fake audio
     * device by participants. The file is not looped, so must be long enough
     * for all tests to finish.
     *
     * @param fakeStreamAudioFile full name of wav file for the fake audio
     *                            device.
     */
    public static void setFakeStreamAudioFile(String fakeStreamAudioFile)
    {
        fakeStreamAudioFName = fakeStreamAudioFile;
    }

    /**
     * Gets the {@code BrowserType} of {@code driver}.
     * @param driver the driver.
     * @return the browser type.
     */
    public static BrowserType getBrowserType(WebDriver driver)
    {
        if(driver == owner)
        {
            return BrowserType.valueOfString(
                System.getProperty(BROWSER_OWNER_NAME_PROP));
        }
        else if(driver == secondParticipant)
        {
            return BrowserType.valueOfString(
                System.getProperty(BROWSER_SECONDP_NAME_PROP));
        }
        else if(driver == thirdParticipant)
        {
            return BrowserType.valueOfString(
                System.getProperty(BROWSER_THIRDP_NAME_PROP));
        }

        return null;
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
                            .executeScript("return APP.connectionquality" +
                                                   ".getStats();");

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
     * Waits until the owner joins the room, creating and starting the owner
     * if it hasn't been started.
     */
    public static void waitForOwnerToJoinMUC()
    {
        if (owner == null)
            startOwner(null);

        waitForParticipantToJoinMUC(owner, 15);
    }

    /**
     * Starts the owner, if it isn't started and closes all other participants.
     */
    public static void closeAllParticipantsExceptTheOwner()
    {
        waitForOwnerToJoinMUC();
        closeSecondParticipant();
        closeThirdParticipant();
    }

    /**
     * Closes {@code secondParticipant} if it is open.
     */
    public static void closeSecondParticipant()
    {
        if (secondParticipant != null)
        {
            close(secondParticipant);
        }
    }

    /**
     * Waits until {@code secondParticipant} has joined the conference (its ICE
     * connection has completed and has it has sent and received data).
     */
    public static void waitForSecondParticipantToConnect()
    {
        WebDriver secondParticipant = getSecondParticipant();
        assertNotNull(secondParticipant);
        waitForParticipantToJoinMUC(secondParticipant, 10);
        waitForIceCompleted(secondParticipant);
        waitForSendReceiveData(secondParticipant);
    }

    /**
     * Closes {@code thirdParticipant}, if it exists.
     */
    public static void closeThirdParticipant()
    {
        if (thirdParticipant != null)
        {
            close(thirdParticipant);
        }
    }

    /**
     * Waits until {@code thirdParticipant} has joined the conference (its ICE
     * connection has completed and has it has sent and received data).
     */
    public static void waitForThirdParticipantToConnect()
    {
        WebDriver thirdParticipant = getThirdParticipant();
        assertNotNull(thirdParticipant);
        waitForParticipantToJoinMUC(thirdParticipant, 10);
        waitForIceCompleted(thirdParticipant);
        waitForSendReceiveData(thirdParticipant);
        waitForRemoteStreams(thirdParticipant, 2);
    }

    /**
     * Waits for number of remote streams.
     * @param participant the driver to use for the check.
     * @param n number of remote streams to wait for
     */
    public static void waitForRemoteStreams(
            final WebDriver participant,
            final int n)
    {
        new WebDriverWait(participant, 15)
            .until(new ExpectedCondition<Boolean>()
            {
                public Boolean apply(WebDriver d)
                {
                    long streams = (Long)((JavascriptExecutor) participant)
                        .executeScript("return "
                            + "Object.keys(APP.RTC.remoteStreams).length;");

                    if(streams >= n)
                        return true;
                    else
                        return false;
                }
            });
    }

    /**
     * Starts the owner and the seconds participant, if they are not started,
     * and stops the third participant, if it is not stopped.
     * participants(owner and 'second participant').
     */
    public static void ensureTwoParticipants()
    {
        waitForOwnerToJoinMUC();
        waitForSecondParticipantToConnect();
        closeThirdParticipant();
    }

    /**
     * Starts the owner, second participant and third participant if they aren't
     * started.
     */
    public static void ensureThreeParticipants()
    {
        waitForOwnerToJoinMUC();
        waitForSecondParticipantToConnect();
        waitForThirdParticipantToConnect();
    }

    /**
     * Returns human readable name of <tt>WebDriver</tt> instance.
     * @param driver the instance of <tt>WebDriver</tt> for which we want to get
     *               human readable name. Should be one of instances managed by
     *               <tt>ConferenceFixture</tt> class.
     */
    public static String getParticipantName(WebDriver driver)
    {
        if (driver == null)
            return "nullDriverInstance";

        if (owner == driver)
        {
            return "owner";
        }
        else if (secondParticipant == driver)
        {
            return "secondParticipant";
        }
        else if (thirdParticipant == driver)
        {
            return "thirdParticipant";
        }
        else
        {
            return "unknownDriverInstance";
        }
    }

    /**
     * Returns human readable name of <tt>Participant</tt> instance.
     * @param p the instance of <tt>Participant</tt> for which we want to get
     *               human readable name.
     */
    public static String getParticipantName(Participant p)
    {
        if (p == null)
            return "nullDriverInstance";

        if (Participant.ownerDriver == p)
        {
            return "owner";
        }
        else if (Participant.secondParticipantDriver == p)
        {
            return "secondParticipant";
        }
        else if (Participant.thirdParticipantDriver == p)
        {
            return "thirdParticipant";
        }
        else
        {
            return "unknownDriverInstance";
        }
    }
}
