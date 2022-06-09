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
package org.jitsi.meet.test.web;

import io.github.bonigarcia.wdm.*;
import org.jitsi.meet.test.base.*;
import org.jitsi.meet.test.util.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.*;
import org.openqa.selenium.firefox.*;
import org.openqa.selenium.ie.*;
import org.openqa.selenium.logging.*;
import org.openqa.selenium.remote.*;
import org.openqa.selenium.safari.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.TimeoutException;
import java.util.logging.*;

/**
 * Creates web specific participant.
 */
public class WebParticipantFactory
    extends ParticipantFactory<WebParticipant>
{
    /**
     * The format string for the URL used to download an extension.
     */
    private static String EXT_DOWNLOAD_URL_FORMAT
        = "https://clients2.google.com/service/update2/crx"
            + "?response=redirect&prodversion=38.0&x=id%%3D%s"
            + "%%26installsource%%3Dondemand%%26uc";

    /**
     * Gets a {@link File} pointing to the pathname passed as an argument.
     */
    private static File getFile(
            WebParticipantOptions options, String pathname)
    {
        if (pathname == null || pathname.trim().length() == 0)
        {
            return null;
        }

        File file = new File(pathname);
        if (file.isAbsolute() || !options.isRemote())
        {
            return file;
        }

        String remoteResourcePath = options.getRemoteResourcePath();

        return remoteResourcePath == null
            ? file : new File(remoteResourcePath, pathname);
    }

    /**
     * Include web specific globals.
     *
     * {@inheritDoc}
     */
    @Override
    public List<String> getGlobalConfigKeys()
    {
        List<String> globalKeys =  super.getGlobalConfigKeys();

        globalKeys.addAll(WebParticipantOptions.getSystemGlobalPropNames());

        return globalKeys;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected WebParticipant doCreateParticipant(ParticipantOptions options)
    {
        WebParticipantOptions webOptions = new WebParticipantOptions();

        webOptions.putAll(options);

        WebParticipant webParticipant
            = new WebParticipant(
                    webOptions.getName(),
                    startWebDriver(webOptions),
                    webOptions.getParticipantType(),
                    webOptions.getLoadTest());

        return webParticipant;
    }

    /**
     * Starts a <tt>WebDriver</tt> instance using default settings.
     * @param options the options to use when creating the driver.
     * @return the <tt>WebDriver</tt> instance.
     */
    private WebDriver startWebDriver(
        WebParticipantOptions options)
    {
        ParticipantType participantType = options.getParticipantType();
        String version = options.getVersion();
        File browserBinaryAPath = getFile(options, options.getBinary());

        boolean isRemote = options.isRemote();

        // by default we load chrome, but we can load safari or firefox
        if (participantType.isFirefox())
        {
            WebDriverManager.firefoxdriver().setup();

            if (browserBinaryAPath != null
                    && (browserBinaryAPath.exists() || isRemote))
            {
                System.setProperty(
                        "webdriver.firefox.bin",
                        browserBinaryAPath.getAbsolutePath());
            }

            FirefoxProfile profile = new FirefoxProfile();
            // Force firefox to use English instead of system language.
            // Not test because of having not firefox installed.
            profile.setPreference("intl.accept_languages", "en");
            profile.setPreference("media.navigator.permission.disabled", true);
            // Enables tcp in firefox, disabled by default in 44
            profile.setPreference("media.peerconnection.ice.tcp", true);
            profile.setPreference("media.navigator.streams.fake", true);
            profile.setPreference("media.autoplay.default", 0);
            if (options.allowsInsecureCerts())
            {
                profile.setAcceptUntrustedCertificates(true);
            }

            FirefoxOptions ffOptions = new FirefoxOptions();
            if (options.isHeadless())
            {
                ffOptions.addArguments("--headless");
            }

            profile.setPreference(
                "webdriver.log.file", FailureListener.createLogsFolder() +
                "/firefox-js-console-"
                + options.getName() + ".log");

            System.setProperty("webdriver.firefox.logfile",
                FailureListener.createLogsFolder() +
                    "/firefox-console-"
                    + options.getName() + ".log");

            ffOptions.setProfile(profile);

            if (isRemote)
            {
                if (version != null && version.length() > 0)
                {
                    ffOptions.setCapability(CapabilityType.VERSION, version);
                }

                return new RemoteWebDriver(options.getRemoteDriverAddress(), ffOptions);
            }

            return new FirefoxDriver(ffOptions);
        }
        else if (participantType == ParticipantType.safari)
        {
            // You must enable the 'Allow Remote Automation' option in
            // Safari's Develop menu to control Safari via WebDriver.
            // In Safari->Preferences->Websites, select Camera,
            // and select Allow for "When visiting other websites"
            if (isRemote)
            {
                return new RemoteWebDriver(
                        options.getRemoteDriverAddress(), new SafariOptions());
            }
            return new SafariDriver();
        }
        else if (participantType == ParticipantType.edge)
        {
            WebDriverManager.edgedriver().setup();

            InternetExplorerOptions ieOptions = new InternetExplorerOptions();
            ieOptions.ignoreZoomSettings();

            System.setProperty("webdriver.ie.driver.silent", "true");

            return new InternetExplorerDriver(ieOptions);
        }
        else
        {
            WebDriverManager.chromedriver().setup();

            System.setProperty("webdriver.chrome.verboseLogging", "true");
            System.setProperty(
                    "webdriver.chrome.logfile",
                    FailureListener.createLogsFolder()
                        + "/chrome-console-" + options.getName() + ".log");

            LoggingPreferences logPrefs = new LoggingPreferences();
            logPrefs.enable(LogType.BROWSER, Level.ALL);

            final ChromeOptions ops = new ChromeOptions();
            ops.setCapability(CapabilityType.APPLICATION_NAME, options.getApplicationName());

            // Force chrome to use English instead of system language.
            Map<String, Object> prefs = new HashMap<String, Object>();
            prefs.put("intl.accept_languages", "en-US");
            ops.setExperimentalOption("prefs", prefs);

            ops.addArguments("allow-insecure-localhost");
            ops.addArguments("use-fake-ui-for-media-stream");
            ops.addArguments("use-fake-device-for-media-stream");
            ops.addArguments("disable-plugins");
            ops.addArguments("mute-audio");
            ops.addArguments("disable-infobars");
            // Since chrome v66 there are new autoplay policies, which broke
            // shared video tests, disable no-user-gesture to make it work
            ops.addArguments("autoplay-policy=no-user-gesture-required");

            String resolverRules = options.hostResolverRules();
            if (resolverRules != null)
            {
                ops.addArguments("host-resolver-rules=" + resolverRules);
            }

            ops.addArguments("auto-select-desktop-capture-source=Your Entire screen");

            ops.setCapability(CapabilityType.LOGGING_PREFS, logPrefs);
            ops.setCapability(CapabilityType.ACCEPT_INSECURE_CERTS, options.allowsInsecureCerts());

            if (options.isChromeSandboxDisabled())
            {
                ops.addArguments("no-sandbox");
                ops.addArguments("disable-dev-shm-usage");
                ops.addArguments("disable-setuid-sandbox");
            }

            if (options.isHeadless())
            {
                ops.addArguments("headless");
                ops.addArguments("window-size=1200x600");
            }

            // starting version 46 we see crashes of chrome GPU process when
            // running in headless mode
            // which leaves the browser opened and selenium hang forever.
            // There are reports that in older version crashes like that will
            // fallback to software graphics, we try to disable gpu for now
            ops.addArguments("disable-gpu");

            if (browserBinaryAPath != null
                    && (browserBinaryAPath.exists() || isRemote))
            {
                ops.setBinary(browserBinaryAPath.getAbsolutePath());
            }

            File uplinkFile = getFile(options, options.getUplink());
            if (uplinkFile != null)
            {
                ops.addArguments(
                        "uplink=" + uplinkFile.getAbsolutePath());
            }

            File downlinkFile = getFile(options, options.getDownlink());
            if (downlinkFile != null)
            {
                ops.addArguments(
                        "downlink=" + downlinkFile.getAbsolutePath());
            }

            String profileDirectory = options.getProfileDirectory();
            if (profileDirectory != null && profileDirectory != "")
            {
                ops.addArguments("user-data-dir=" + profileDirectory);
            }

            File fakeStreamAudioFile
                = getFile(options, options.getFakeStreamAudioFile());
            if (fakeStreamAudioFile != null)
            {
                ops.addArguments("use-file-for-fake-audio-capture="
                        + fakeStreamAudioFile.getAbsolutePath());
            }

            File fakeStreamVideoFile
                = getFile(options, options.getFakeStreamVideoFile());
            if (fakeStreamVideoFile != null)
            {
                ops.addArguments("use-file-for-fake-video-capture="
                        + fakeStreamVideoFile.getAbsolutePath());
            }

            //ops.addArguments("vmodule=\"*media/*=3,*turn*=3\"");
            //ops.addArguments("enable-logging");
            //ops.addArguments("vmodule=*=3");

            if (isRemote)
            {
                if (version != null && version.length() > 0)
                {
                    ops.setCapability(CapabilityType.VERSION, version);
                }

                return new RemoteWebDriver(
                        options.getRemoteDriverAddress(), ops);
            }

            try
            {
                final ExecutorService pool = Executors.newFixedThreadPool(1);
                // we will retry four times for 1 minute to obtain
                // the chrome driver, on headless environments chrome hangs
                // and we wait forever
                for (int i = 0; i < 2; i++)
                {
                    Future<ChromeDriver> future = null;
                    try
                    {
                        future = pool.submit(
                            () -> {
                                long start = System.currentTimeMillis();
                                ChromeDriver resDr = new ChromeDriver(ops);
                                TestUtils.print(
                                    "ChromeDriver created for:"
                                        + (System.currentTimeMillis() - start)
                                        + " ms.");
                                return resDr;
                            });

                        ChromeDriver res = future.get(2, TimeUnit.MINUTES);
                        if (res != null)
                            return res;
                    }
                    catch (TimeoutException te)
                    {
                        // cancel current task
                        future.cancel(true);

                        TestUtils.print("Timeout waiting for "
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
            TestUtils.print("Just create ChromeDriver, may hang!");
            return new ChromeDriver(ops);
        }
    }
}
