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
import java.net.*;
import java.nio.channels.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.TimeoutException;
import java.util.logging.*;

/**
 * Creates web specific participant.
 */
public class WebParticipantFactory
    extends ParticipantFactory<WebParticipantOptions>
{
    /**
     * The property to change remote selenium grid URL, defaults to
     * http://localhost:4444/wd/hub if requiring remote browser and property
     * is not set.
     */
    private static final String REMOTE_ADDRESS_NAME_PROP = "remote.address";

    /**
     * The property to disable no-sandbox parameter for chrome.
     */
    private static final String DISABLE_NOSANBOX_PARAM
        = "chrome.disable.nosanbox";

    /**
     * The property to enable headless parameter for chrome.
     */
    private static final String ENABLE_HEADLESS_PARAM
        = "chrome.enable.headless";

    /**
     * The property to evaluate for parent path of the resources used when
     * loading chrome remotely like audio/video files.
     */
    private static final String REMOTE_RESOURCE_PARENT_PATH_NAME_PROP
        = "remote.resource.path";

    /**
     * The format string for the URL used to download an extension.
     */
    private static String EXT_DOWNLOAD_URL_FORMAT
        = "https://clients2.google.com/service/update2/crx"
            + "?response=redirect&prodversion=38.0&x=id%%3D%s"
            + "%%26installsource%%3Dondemand%%26uc";

    /**
     * The private constructor of the factory.
     *
     * @param config - A <tt>Properties</tt> instance holding configuration
     *               properties required to setup new participants.
     */
    public WebParticipantFactory(Properties config)
    {
        super(config);
    }

    @Override
    public Participant<? extends WebDriver> createParticipant(
        String configPrefix,
        ParticipantOptions options)
    {
        WebParticipantOptions webOptions = new WebParticipantOptions();
        webOptions.load(config, configPrefix, options);

        return new WebParticipant(
            webOptions.getName(),
                startWebDriver(webOptions),
                webOptions.getParticipantType());
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
        String browserBinary = options.getBinary();
        boolean isRemote = options.isRemote();

        // by default we load chrome, but we can load safari or firefox
        if (participantType.isFirefox())
        {
            FirefoxDriverManager.getInstance().setup();

            if (browserBinary != null && browserBinary.trim().length() > 0)
            {
                File binaryFile = new File(browserBinary);
                if (binaryFile.exists())
                    System.setProperty("webdriver.firefox.bin", browserBinary);
            }

            FirefoxProfile profile = new FirefoxProfile();
            profile.setPreference("media.navigator.permission.disabled", true);
            // Enables tcp in firefox, disabled by default in 44
            profile.setPreference("media.peerconnection.ice.tcp", true);
            profile.setPreference("media.navigator.streams.fake", true);
            profile.setAcceptUntrustedCertificates(true);

            profile.setPreference(
                "webdriver.log.file", FailureListener.createLogsFolder() +
                "/firefox-js-console-"
                + options.getName() + ".log");

            System.setProperty("webdriver.firefox.logfile",
                FailureListener.createLogsFolder() +
                    "/firefox-console-"
                    + options.getName() + ".log");

            if (isRemote)
            {
                FirefoxOptions ffOptions = new FirefoxOptions();
                ffOptions.setProfile(profile);

                if (version != null && version.length() > 0)
                {
                    ffOptions.setCapability(CapabilityType.VERSION, version);
                }

                return new RemoteWebDriver(getRemoteDriverAddress(), ffOptions);
            }

            return new FirefoxDriver(new FirefoxOptions().setProfile(profile));
        }
        else if (participantType == ParticipantType.safari)
        {
            return new SafariDriver();
        }
        else if (participantType == ParticipantType.edge)
        {
            InternetExplorerDriverManager.getInstance().setup();

            InternetExplorerOptions ieOptions = new InternetExplorerOptions();
            ieOptions.ignoreZoomSettings();

            System.setProperty("webdriver.ie.driver.silent", "true");

            return new InternetExplorerDriver(ieOptions);
        }
        else
        {
            ChromeDriverManager.getInstance().setup();

            System.setProperty("webdriver.chrome.verboseLogging", "true");
            System.setProperty(
                    "webdriver.chrome.logfile",
                    FailureListener.createLogsFolder()
                        + "/chrome-console-" + options.getName() + ".log");

            LoggingPreferences logPrefs = new LoggingPreferences();
            logPrefs.enable(LogType.BROWSER, Level.ALL);

            final ChromeOptions ops = new ChromeOptions();
            ops.addArguments("use-fake-ui-for-media-stream");
            ops.addArguments("use-fake-device-for-media-stream");
            ops.addArguments("disable-plugins");
            ops.addArguments("mute-audio");
            ops.addArguments("disable-infobars");

            if(options.getChromeExtensionId() != null)
            {
                try
                {
                    ops.addExtensions(
                        downloadExtension(options.getChromeExtensionId()));
                }
                catch (IOException e)
                {
                   throw new RuntimeException(e);
                }
            }

            ops.addArguments("auto-select-desktop-capture-source=Entire screen");

            ops.setCapability(CapabilityType.LOGGING_PREFS, logPrefs);

            if (!Boolean.getBoolean(DISABLE_NOSANBOX_PARAM))
            {
                ops.addArguments("no-sandbox");
                ops.addArguments("disable-setuid-sandbox");
            }

            if (Boolean.getBoolean(ENABLE_HEADLESS_PARAM))
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

            if (browserBinary != null && browserBinary.trim().length() > 0)
            {
                File binaryFile = new File(browserBinary);
                if (binaryFile.exists())
                    ops.setBinary(binaryFile);
            }

            String remoteResourcePath = config.getProperty(
                REMOTE_RESOURCE_PARENT_PATH_NAME_PROP);

            String fakeStreamAudioFName = options.getFakeStreamAudioFile();
            if (fakeStreamAudioFName != null)
            {
                String fileAbsolutePath = new File(
                    isRemote && remoteResourcePath != null
                        ? new File(remoteResourcePath) : null,
                    fakeStreamAudioFName).getAbsolutePath();

                ops.addArguments(
                    "use-file-for-fake-audio-capture=" + fileAbsolutePath);
            }

            String fakeStreamVideoFName = options.getFakeStreamVideoFile();
            if (fakeStreamVideoFName != null)
            {
                String fileAbsolutePath = new File(
                    isRemote && remoteResourcePath != null
                        ? new File(remoteResourcePath) : null,
                    fakeStreamVideoFName).getAbsolutePath();

                ops.addArguments(
                    "use-file-for-fake-video-capture=" + fileAbsolutePath);
            }

            //ops.addArguments("vmodule=\"*media/*=3,*turn*=3\"");
            ops.addArguments("enable-logging");
            ops.addArguments("vmodule=*=3");

            if (isRemote)
            {
                if (version != null && version.length() > 0)
                {
                    ops.setCapability(CapabilityType.VERSION, version);
                }

                return new RemoteWebDriver(getRemoteDriverAddress(), ops);
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

    /**
     * Returns the remote driver address or the default one.
     * @return the remote driver address or the default one.
     */
    private URL getRemoteDriverAddress()
    {
        try
        {
            String remoteAddress = config.getProperty(
                REMOTE_ADDRESS_NAME_PROP,
                "http://localhost:4444/wd/hub");

            return new URL(remoteAddress);
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Downloads extension from chrome webstore.
     * @param id - the id of the extension.
     * @returns <tt>File</tt> associated with the downloaded extension.
     */
    private static File downloadExtension(String id) throws IOException
    {
        URL website = new URL(String.format(EXT_DOWNLOAD_URL_FORMAT, id));
        File extension = File.createTempFile("jidesha", ".crx");
        extension.deleteOnExit();

        try(
            ReadableByteChannel rbc = Channels.newChannel(website.openStream());
            FileOutputStream fos = new FileOutputStream(extension);
            FileChannel fc = fos.getChannel();
        )
        {
            fc.transferFrom(rbc, 0, Long.MAX_VALUE);
        }

        return extension;
    }
}
