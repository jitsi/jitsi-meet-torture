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

import org.jitsi.meet.test.base.*;
import org.openqa.selenium.*;
import org.openqa.selenium.remote.*;
import org.testng.annotations.*;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.util.logging.*;

/**
 * Abstract base of mobile test cases. Setups and starts a mobile driver.
 *
 * @author Damian Minkov
 */
public abstract class AbstractBaseTest
{
    /**
     * The logger.
     */
    private static Logger logger
        = Logger.getLogger(AbstractBaseTest.class.getName());

    /**
     * Current mobile driver instance.
     */
    private static AppiumDriver<WebElement> driver;

    /**
     * The current room name used.
     */
    public static String currentRoomName;

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

        final String URL_STRING = "http://127.0.0.1:4723/wd/hub";

        URL url = new URL(URL_STRING);

        DesiredCapabilities capabilities = new DesiredCapabilities();

        if ("android".equals(System.getProperty("mobile.participant")))
        {
            setCapability(capabilities, "deviceName", "mobile.caps.deviceName");
            setCapability(capabilities, "app", "mobile.caps.app");

            driver = new AndroidDriver<>(url, capabilities);
        }
        else
        {
            driver = new IOSDriver<>(url, capabilities);
        }

        //Use a higher value if your mobile elements take time to show up
        driver.manage().timeouts().implicitlyWait(35, TimeUnit.SECONDS);
    }

    /**
     * Sets a capability if its value is available in system properties.
     *
     * @param caps the capability object to set new caps
     * @param name the name of the capability to set
     * @param systemProperty the system property name to check
     */
    private static void setCapability(
        DesiredCapabilities caps, String name, String systemProperty)
    {
        String propValue = System.getProperty(systemProperty);
        if (propValue != null)
        {
            caps.setCapability(name, propValue);
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
     * Restart the app after every test class to go back to the main
     * screen and to reset the behavior
     */
    @AfterClass
    public void restartApp()
    {
        logger.log(Level.INFO, "Reset App");
        driver.resetApp();
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
     * Takes screenshot from mobile driver.
     * @param name the name of the screenshot.
     */
    public static void takeScreenshot(final String name)
    {
        String screenshotDirectory
            = System.getProperty("appium.screenshots.dir",
            System.getProperty("java.io.tmpdir", ""));

        File screenshot = driver.getScreenshotAs(OutputType.FILE);
        screenshot.renameTo(
            new File(screenshotDirectory, String.format("%s.png", name)));
    }

    /**
     * Returns current room name, if one is missing a random one is generated.
     * @return current room name.
     */
    public String getRoomName()
    {
        if (currentRoomName == null)
        {
            currentRoomName = "torture"
                + String.valueOf((int)(Math.random()*1000000));
        }

        return currentRoomName;
    }
}
