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

import java.util.*;
import java.util.concurrent.*;
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
     * The property to change tested browser for the second participant.
     */
    public static final String BROWSER_SECONDP_NAME_PROP
        = "browser.second.participant";

    /**
     * The property to change tested browser for the second participant.
     */
        public static final String BROWSER_THIRDP_NAME_PROP
        = "browser.third.participant";

    public static final String ICE_CONNECTED_CHECK_SCRIPT =
        "for (sid in APP.xmpp.getSessions()) {" +
            "if (APP.xmpp.getSessions()[sid]."
            + "peerconnection.iceConnectionState " +
            "!== 'connected')" +
            "return false;" +
            "}" +
            "return true;";

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
            startParticipant();
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
     * Starts the conference owner, the first participant that generates
     * the random room name.
     * @param fragment adds the given string to the fragment part of the URL
     */
    public static void startOwner(String fragment)
    {
        System.err.println("Starting owner participant.");

        String browser = System.getProperty(BROWSER_OWNER_NAME_PROP);
        owner = startDriver(browser);

        currentRoomName = "torture"
            + String.valueOf((int)(Math.random()*1000000));

        openRoom(owner, fragment, browser);

        ((JavascriptExecutor) owner)
            .executeScript("document.title='Owner'");
    }

    /**
     * Opens the room for the given participant.
     *
     * @param participant to open the current test room.
     * @param fragment adds the given string to the fragment part of the URL
     * @param browser
     */
    public static void openRoom(
            WebDriver participant,
            String fragment,
            String browser)
    {
        String URL = System.getProperty(JITSI_MEET_URL_PROP) + "/"
            + currentRoomName;
        URL += "#config.requireDisplayName=false";
        URL += "&config.callStatsID=false";
        if(fragment != null)
            URL += "&" + fragment;

        if(browser != null
            && browser.equalsIgnoreCase(BrowserType.firefox.toString()))
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
    }

    /**
     * Starts chrome instance using some default settings.
     * @return the webdriver instance.
     */
    private static WebDriver startDriver(String browser)
    {
        WebDriver wd = startDriverInstance(browser);

        wd.manage().timeouts().pageLoadTimeout(60, TimeUnit.SECONDS);

        // just wait the instance to start before doing some stuff
        // can kick a renderer bug hanging
        TestUtils.waits(1000);

        return wd;
    }

    /**
     * Starts chrome instance using some default settings.
     * @return the webdriver instance.
     */
    private static WebDriver startDriverInstance(String browser)
    {
        // by default we load chrome, but we can load safari or firefox
        if(browser != null
            && browser.equalsIgnoreCase(BrowserType.firefox.toString()))
        {
            FirefoxProfile profile = new FirefoxProfile();
            profile.setPreference("media.navigator.permission.disabled", true);
            profile.setAcceptUntrustedCertificates(true);

            profile.setPreference("browser.download.folderList", 1);
            //String downloadDir
            //    = System.getProperty("user.home") + File.separator
            //          + "Downloads";
            //profile.setPreference("browser.download.dir", downloadDir);
            profile.setPreference("browser.helperApps.neverAsk.saveToDisk",
                "text/plain;text/csv");
            profile.setPreference("browser.helperApps.alwaysAsk.force", false);
            profile.setPreference("browser.download.manager.showWhenStarting", false );

            return new FirefoxDriver(profile);
        }
        else if(browser != null
            && browser.equalsIgnoreCase(BrowserType.safari.toString()))
        {
            return new SafariDriver();
        }
        else if(browser != null
            && browser.equalsIgnoreCase(BrowserType.ie.toString()))
        {
            DesiredCapabilities caps = DesiredCapabilities.internetExplorer();
            caps.setCapability("ignoreZoomSetting", true);
            System.setProperty("webdriver.ie.driver.silent", "true");

            return new InternetExplorerDriver(caps);
        }
        else
        {
            DesiredCapabilities caps = DesiredCapabilities.chrome();
            LoggingPreferences logPrefs = new LoggingPreferences();
            logPrefs.enable(LogType.BROWSER, Level.ALL);
            caps.setCapability(CapabilityType.LOGGING_PREFS, logPrefs);

            ChromeOptions ops = new ChromeOptions();
            ops.addArguments("use-fake-ui-for-media-stream");
            ops.addArguments("use-fake-device-for-media-stream");


            if (fakeStreamAudioFName != null)
            {
                ops.addArguments(
                    "use-file-for-fake-audio-capture=" + fakeStreamAudioFName);
            }

            ops.addArguments("vmodule=\"*media/*=3,*turn*=3\"");

            caps.setCapability(ChromeOptions.CAPABILITY, ops);

            return new ChromeDriver(ops);
        }
    }

    /**
     * Start <tt>secondParticipant</tt>.
     */
    public static void startParticipant()
    {
        startParticipant(null);
    }

    /**
     * Start <tt>secondParticipant</tt>.
     * @param fragment A string to be added to the URL as a parameter (i.e.
     * prefixed with a '&').
     */
    public static void startParticipant(String fragment)
    {
        System.err.println("Starting second participant.");

        String browser = System.getProperty(BROWSER_SECONDP_NAME_PROP);
        secondParticipant = startDriver(browser);

        openRoom(secondParticipant, fragment, browser);

        ((JavascriptExecutor) secondParticipant)
            .executeScript("document.title='SecondParticipant'");
    }

    /**
     * Start the third participant reusing the already generated room name.
     */
    public static void startThirdParticipant()
    {
        System.err.println("Starting third participant.");

        String browser = System.getProperty(BROWSER_THIRDP_NAME_PROP);
        thirdParticipant = startDriver(browser);

        openRoom(thirdParticipant, null, browser);

        ((JavascriptExecutor) thirdParticipant)
            .executeScript("document.title='ThirdParticipant'");
    }

    /**
     * Checks whether participant has joined the room
     * @param participant where we check
     * @param timeout the time to wait for the event.
     */
    public static void checkParticipantToJoinRoom(
        WebDriver participant, long timeout)
    {
        TestUtils.waitsForBoolean(
            participant,
            "return (APP.xmpp.getConnection() != null) && APP.xmpp.isMUCJoined();",
            timeout);
    }

    /**
     * Waits the participant to get event for iceConnectionState that changes
     * to connected.
     * @param participant driver instance used by the participant for whom we
     *                    want to wait to join the conference.
     */
    public static void waitsParticipantToJoinConference(WebDriver participant)
    {
        TestUtils.waitsForBoolean(participant, ICE_CONNECTED_CHECK_SCRIPT, 30);
    }

    /**
     * Checks the participant for iceConnectionState is it connected.
     *
     * @param participant driver instance used by the participant for whom we
     *                    want to check.
     * @return {@code true} if the {@code iceConnectionState} of the specified
     * {@code participant} is {@code connected}; otherwise, {@code false}
     */
    public static boolean checkParticipantIsConnected(WebDriver participant)
    {
        Object res = ((JavascriptExecutor) participant)
            .executeScript(ICE_CONNECTED_CHECK_SCRIPT);
        return res != null && res.equals(Boolean.TRUE);
    }

    /**
     * Checks whether participant is joined the room.
     *
     * @param participant where we check
     * @return {@code true} if the specified {@code participant} has joined the
     * room; otherwise, {@code false}
     */
    public static boolean checkParticipantIsInRoom(WebDriver participant)
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
     * Quits the participant.
     * @param participant to quit.
     */
    public static void quit(WebDriver participant)
    {
        if (participant == null)
        {
            System.err.println("quit(): participant is null");
            return;
        }

        try
        {
            MeetUIUtils.clickOnToolbarButtonByClass(participant, "icon-hangup");

            TestUtils.waits(500);
        }
        catch(Throwable t)
        {
            t.printStackTrace();
        }

        try
        {
            participant.close();

            TestUtils.waits(500);

            participant.quit();

            TestUtils.waits(500);
        }
        catch(Throwable t)
        {
            t.printStackTrace();
        }

        String instanceName = getParticipantName(participant);
        System.err.println("Quited " + instanceName + ".");

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
     * The currently used browser that runs tests for driver.
     * @param driver the driver to test.
     * @return browser type.
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
     * Checks statistics for received and sent bitrate.
     * @param participant the participant to check.
     */
    public static void waitForSendReceiveData(final WebDriver participant)
    {
        new WebDriverWait(participant, 15)
            .until(new ExpectedCondition<Boolean>()
        {
            public Boolean apply(WebDriver d)
            {
                Map stats = (Map) ((JavascriptExecutor) participant)
                    .executeScript("return APP.connectionquality.getStats();");

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
     * Methods makes sure that 'conference owner participant' is in the
     * conference room(might not be sending/receiving any data - if no other
     * participants in the room).
     */
    public static void ensureOwnerRunning()
    {
        if (owner == null)
            startOwner(null);

        ConferenceFixture.checkParticipantToJoinRoom(owner, 15);
    }

    /**
     * Makes sure that conference owner is alone in the conference room.
     */
    public static void ensureOwnerOnly()
    {
        ensureOwnerRunning();
        ensureSecondParticipantNotRunning();
        ensureThirdParticipantNotRunning();
    }

    /**
     * Method makes sure that 'second participant' is not in the conference
     * and it's driver is disposed.
     */
    public static void ensureSecondParticipantNotRunning()
    {
        if (secondParticipant != null)
        {
            quit(secondParticipant);
        }
    }

    /**
     * Methods makes sure that 'second participant' is in the conference and is
     * sending/receiving data.
     */
    public static void ensureSecondParticipantRunning()
    {
        WebDriver secondPeer = getSecondParticipant();
        assertNotNull(secondPeer);
        ConferenceFixture.checkParticipantToJoinRoom(secondPeer, 10);
        ConferenceFixture.waitsParticipantToJoinConference(secondPeer);
        ConferenceFixture.waitForSendReceiveData(secondPeer);
    }

    /**
     * Method makes sure that 'third participant' is not in the conference and
     * it's driver is disposed.
     */
    public static void ensureThirdParticipantNotRunning()
    {
        if (thirdParticipant != null)
        {
            quit(thirdParticipant);
        }
    }

    /**
     * Methods makes sure that 'third participant' is in the conference and is
     * sending/receiving data.
     */
    public static void ensureThirdParticipantRunning()
    {
        WebDriver thirdPeer = getThirdParticipant();
        assertNotNull(thirdPeer);
        ConferenceFixture.checkParticipantToJoinRoom(thirdPeer, 10);
        ConferenceFixture.waitsParticipantToJoinConference(thirdPeer);
        ConferenceFixture.waitForSendReceiveData(thirdPeer);
    }

    /**
     * Makes sure that there is currently running conference with exactly two
     * participants(owner and 'second participant').
     */
    public static void ensureTwoParticipants()
    {
        ensureOwnerRunning();
        ensureSecondParticipantRunning();
        ensureThirdParticipantNotRunning();
    }

    /**
     * Makes sure that there is currently running conference with three
     * participants(owner, 'second participant' and 'third participant').
     */
    public static void ensureThreeParticipants()
    {
        ensureOwnerRunning();
        ensureSecondParticipantRunning();
        ensureThirdParticipantRunning();
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
}
