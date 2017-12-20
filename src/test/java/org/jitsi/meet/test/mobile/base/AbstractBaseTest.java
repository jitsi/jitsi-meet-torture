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

    private Properties config;
    private URL appiumServerUrl;

    protected List<MobileParticipant> mobiles = new ArrayList<>();

    /**
     * @throws MalformedURLException occurs when the URL is wrong
     */
    @BeforeSuite
    public void setUpAppium()
        throws MalformedURLException
    {
        logger.log(Level.INFO, "Starting driver");

        // loads test settings
        this.config = TestSettings.initSettings();

        String appiumAddress = config.getProperty("appium.server.address");
        if (appiumAddress == null)
        {
            appiumAddress = "127.0.0.1";
        }

        String appiumPort = config.getProperty("appium.server.port");
        if (appiumPort == null)
        {
            appiumPort = "4723";
        }

        this.appiumServerUrl
            = new URL("http://" + appiumAddress +":" + appiumPort + "/wd/hub");
    }

    public MobileParticipant createMobile(String configPrefix)
    {
        MobileParticipantFactory factory
            = new MobileParticipantFactory(
                    config, configPrefix, appiumServerUrl);

        MobileParticipant newParticipant
            = factory.loadConfig().startNewDriver();

        if (newParticipant != null)
        {
            mobiles.add(newParticipant);
        }

        return newParticipant;
    }

    public void destroyMobile(MobileParticipant mobile)
    {
        if (mobiles.remove(mobile))
        {
            mobile.quit();
        }
    }

    @AfterClass
    public void disposeMobiles()
    {
        for (MobileParticipant m : getMobileParticipants())
        {
            destroyMobile(m);
        }
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
    public List<MobileParticipant> getMobileParticipants()
    {
        return new ArrayList<>(mobiles);
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
