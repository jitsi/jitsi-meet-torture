package org.jitsi.meet.test.mobile.base;

import io.appium.java_client.*;
import io.appium.java_client.android.*;
import io.appium.java_client.ios.*;
import org.openqa.selenium.*;
import org.openqa.selenium.remote.*;

import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.*;

/**
 *
 */
public class MobileParticipantFactory
{
    private final Properties configProperties;
    private final String prefix;
    private final DesiredCapabilities capabilities;
    private final URL appiumUrl;
    private String os;
    private String appPath;

    public MobileParticipantFactory(Properties configProperties,
                                    String prefix,
                                    URL appiumUrl)
    {
        this.appiumUrl = Objects.requireNonNull(appiumUrl, "appiumUrl");
        this.configProperties
            = Objects.requireNonNull(
                    configProperties, "configProperties");
        this.prefix = Objects.requireNonNull(prefix, "prefix");
        this.capabilities = new DesiredCapabilities();
    }

    private String getCfgProperty(String name)
    {
        Object value = configProperties.get(prefix + "." + name);

        return value != null ? String.valueOf(value) : null;
    }

    /**
     * Sets a capability if its value is available in system properties.
     *
     * @param capability the name of the capability to set
     * @param property the system property name to check
     */
    private String readAndSetCapability(
        String capability,
        String property)
    {
        String propValue = getCfgProperty(property);

        if (propValue != null)
        {
            capabilities.setCapability(capability, propValue);
        }

        return propValue;
    }

    public MobileParticipantFactory loadConfig()
    {
        this.os = readAndSetCapability("platformName", "caps.platformName");

        readAndSetCapability("deviceName", "caps.deviceName");

        this.appPath = readAndSetCapability("app", "caps.app");

        if (isAndroid())
        {
            readAndSetCapability("appWaitActivity", "caps.appWaitActivity");
            readAndSetCapability("appWaitPackage", "caps.appWaitPackage");
        }
        else
        {
            readAndSetCapability("automationName", "caps.automationName");
            readAndSetCapability("udid", "caps.udid");
            // FIXME must be of type boolean...
            //readAndSetCapability("showXcodeLog", "caps.showXcodeLog");
        }

        // This is required to not timeout drivers waiting for other devices
        // to start
        capabilities.setCapability("newCommandTimeout", 120);

        return this;
    }

    public MobileParticipant startNewDriver()
    {
        if (os == null)
        {
            // There's no valid config for the config prefix.
            return null;
        }

        AppiumDriver<WebElement> driver;

        if (isAndroid())
        {
            driver = new AndroidDriver<>(appiumUrl, capabilities);
        }
        else
        {
            driver = new IOSDriver<>(appiumUrl, capabilities);
        }

        //Use a higher value if your mobile elements take time to show up
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);

        MobileParticipant participant
            = new MobileParticipant(
                    driver, prefix, os, getCfgProperty("bundleId"), appPath);

        Logger.getGlobal().log(Level.INFO, "Started " + os + " driver");

        String bundleId = getCfgProperty("bundleId");

        if ("true".equalsIgnoreCase(getCfgProperty("reinstallApp"))
            && bundleId != null)
        {
            participant.reinstallAppIfInstalled();
        }

        return participant;
    }

    public boolean isAndroid()
    {
        return "android".equalsIgnoreCase(os);
    }
}
