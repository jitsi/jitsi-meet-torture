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
package org.jitsi.meet.test.mobile.base;

import io.appium.java_client.*;
import io.appium.java_client.android.*;
import io.appium.java_client.ios.*;

import org.openqa.selenium.*;
import org.openqa.selenium.remote.*;
import org.testng.annotations.*;

import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.*;

/**
 * Abstract base of mobile test cases. Setups and starts a mobile driver.
 *
 * @author Damian Minkov
 * @author Pawel Domas
 */
@Listeners(MobileTestFailureListener.class)
public abstract class AbstractBaseTest
{
    /**
     * The logger.
     */
    private static Logger logger
        = Logger.getLogger(AbstractBaseTest.class.getName());

    /**
     * The current room name used.
     */
    private static String currentRoomName;

    /**
     * Current mobile driver instance.
     */
    private static AppiumDriver<WebElement> driver;

    /**
     * The OS currently being tested ('android' or 'ios').
     */
    private static String os;

    /**
     * Tries to get config property from Java system properties which normally
     * should start with "mobile.". There are 3 types of properties currently
     * supported:
     *
     * 1. A generic one used for both Android and iOS. Should start with
     *    "mobile.".
     * 2. Android specific property starts with 'mobile.android'.
     * 3. iOS specific properties start with 'mobile.ios'.
     *
     * For example if "mobile.caps.app" is given as <tt>systemPropertyName</tt>
     * then the method will automagically look for "mobile.android.caps.app" if
     * generic property does not exist and the {@link #os} field is currently
     * set to "android".
     *
     * @param systemPropertyName the name of the property to be found.
     * @return <tt>String</tt> or <tt>null</tt> if not found
     */
    private static String getMobileProperty(String systemPropertyName)
    {
        String propValue = System.getProperty(systemPropertyName);

        if (propValue == null && systemPropertyName.startsWith("mobile."))
        {
            propValue
                = System.getProperty(
                        systemPropertyName.replace(
                                "mobile.", "mobile." + os + "."));
        }

        return propValue;
    }

    /**
     * Returns <tt>true</tt> if {@link #driver}'s OS is Android or
     * <tt>false</tt> if it's iOS.
     * @return <tt>boolean</tt>
     */
    protected boolean isAndroid()
    {
        return "android".equalsIgnoreCase(os);
    }

    /**
     * @throws MalformedURLException occurs when the URL is wrong
     */
    @BeforeSuite
    public void setUpAppium()
        throws MalformedURLException
    {
        logger.log(Level.INFO, "Starting driver");

        // loads test settings
        TestSettings.initSettings();

        String appiumAddress = getMobileProperty("appium.server.address");
        if (appiumAddress == null)
        {
            appiumAddress = "127.0.0.1";
        }

        String appiumPort = getMobileProperty("appium.server.port");
        if (appiumPort == null)
        {
            appiumPort = "4723";
        }

        final URL driverUrl
            = new URL("http://" + appiumAddress +":" + appiumPort + "/wd/hub");

        DesiredCapabilities capabilities = new DesiredCapabilities();

        os
            = Objects.requireNonNull(
                    System.getProperty("mobile.participant"));

        capabilities.setCapability("platformName", os);
        setCapability(capabilities, "deviceName", "mobile.caps.deviceName");

        String appPath = setCapability(capabilities, "app", "mobile.caps.app");

        if (isAndroid())
        {
            setCapability(
                    capabilities,
                    "appWaitActivity",
                    "mobile.caps.appWaitActivity");
            setCapability(
                    capabilities,
                    "appWaitPackage",
                    "mobile.caps.appWaitPackage");

            driver = new AndroidDriver<>(driverUrl, capabilities);

            logger.log(Level.INFO, "Started Android driver");
        }
        else
        {
            setCapability(
                capabilities, "automationName", "mobile.caps.automationName");
            setCapability(capabilities, "udid", "mobile.caps.udid");
            setCapability(
                capabilities, "showXcodeLog", "mobile.caps.showXcodeLog");

            driver = new IOSDriver<>(driverUrl, capabilities);

            logger.log(Level.INFO, "Started iOS driver");
        }

        String bundleId = getMobileProperty("mobile.bundleId");

        if ("true".equalsIgnoreCase(
                    getMobileProperty("mobile.reinstallApp"))
                && bundleId != null)
        {
            reinstallAppIfInstalled(bundleId, appPath);
        }

        //Use a higher value if your mobile elements take time to show up
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
    }

    /**
     * Sets a capability if its value is available in system properties.
     *
     * @param caps the capability object to set new caps
     * @param name the name of the capability to set
     * @param systemProperty the system property name to check
     */
    private static String setCapability(
            DesiredCapabilities caps, String name, String systemProperty)
    {
        String propValue = getMobileProperty(systemProperty);

        if (propValue != null)
        {
            caps.setCapability(name, propValue);
        }

        return propValue;
    }

    /**
     * Wraps take screen shot functionality.
     *
     * @param fileName the name of the screenshot file without the extension.
     */
    protected void takeScreenshot(String fileName)
    {
        // This sometimes fails on Amazon device farm, but the tests can
        // continue after that.
        try
        {
            MobileTestUtil.takeScreenshot(getDriver(), fileName);
        }
        catch(WebDriverException exc)
        {
            logger.log(
                Level.SEVERE,
                "Failed to take a screenshot for: " + fileName,
                exc);
        }
    }

    /**
     * Always quit
     */
    @AfterSuite
    public void tearDownAppium()
    {
        logger.log(Level.INFO, "Quit App");
        driver.quit();
    }

    /**
     * Can be used to automatically enter a random room name.
     */
    @BeforeClass
    public void navigateTo()
        throws InterruptedException
    {
    }

    /**
     * Will reinstall the app if it's currently installed. Can be helpful when
     * running locally to reproduce the media permissions alerts.
     * @param bundleId e.g. org.jitsi.meet (Android) or
     *        org.jitsi.JitsiMeet.ios (iOS)
     * @param appPath the path to application archive, should point to
     *        .apk (Android) or .ipk/.app(iOS) file
     */
    private void reinstallAppIfInstalled(String bundleId, String appPath)
    {
        // FIXME driver.isAppInstalled does not work on iOS
        if (!isAndroid() || driver.isAppInstalled(bundleId))
        {
            logger.log(Level.INFO, "Removing app...");
            driver.removeApp(bundleId);
            logger.log(Level.INFO, "Installing app...");
            driver.installApp(appPath);
            logger.log(Level.INFO, "Launching app...");
            driver.launchApp();
        }
    }

    /**
     * Restart the app after every test class to go back to the main
     * screen and to reset the behavior
     */
    @AfterClass
    public void restartApp()
    {
        // XXX currently this step is not necessary - it just opens the app
        // on the entry screen at the end of the test. I'm leaving this
        // commented out to know what to use if we need to restart the app here,
        // as the new tests will come in.
        //
        // FIXME Eventually every test should know for itself what needs to
        // be done *before* it starts and not after. Maybe this should be
        // removed completely from the base class.
        //logger.log(Level.INFO, "Reset App");
        //driver.resetApp();
    }

    /**
     * Returns the mobile driver.
     * @return the mobile driver.
     */
    public static AppiumDriver<WebElement> getDriver()
    {
        return driver;
    }

    /**
     * Returns current room name, if one is missing a random one is generated.
     * @return current room name.
     */
    public String getRoomName()
    {
        if (currentRoomName == null)
        {
            currentRoomName
                = "torture" + String.valueOf((int)(Math.random() * 1000000));
        }

        return currentRoomName;
    }
}
