package org.jitsi.meet.test.mobile;

import io.appium.java_client.*;
import io.appium.java_client.android.*;
import io.appium.java_client.ios.*;
import org.jitsi.meet.test.base.*;
import org.openqa.selenium.*;
import org.openqa.selenium.remote.*;

import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.*;

/**
 *
 */
public class MobileParticipantBuilder
{
    private static final String DEFAULT_APPIUM_ADDRESS = "127.0.0.1";

    private static final String DEFAULT_APPIUM_PORT = "4723";

    private final Properties configProperties;
    private final String prefix;
    private final DesiredCapabilities capabilities;
    private final URL appiumUrl;
    private final ParticipantType type;
    private final String appPath;

    public MobileParticipantBuilder(Properties         config,
                                    String             prefix,
                                    ParticipantType    type)
    {
        String appiumAddress = config.getProperty("appium.server.address");
        if (appiumAddress == null)
        {
            appiumAddress = DEFAULT_APPIUM_ADDRESS;
        }

        String appiumPort = config.getProperty("appium.server.port");
        if (appiumPort == null)
        {
            appiumPort = DEFAULT_APPIUM_PORT;
        }

        try
        {
            this.appiumUrl
                = new URL(
                    "http://" + appiumAddress +":" + appiumPort + "/wd/hub");
        }
        catch (MalformedURLException exc)
        {
            throw new IllegalArgumentException(
                    "Invalid Appium server URL", exc);
        }

        this.configProperties
            = Objects.requireNonNull(
                    config, "configProperties");
        this.prefix = Objects.requireNonNull(prefix, "prefix");
        this.type = Objects.requireNonNull(type, "type");

        this.capabilities = new DesiredCapabilities();

        this.appPath = readAndSetCapability("app", "caps.app");

        Objects.requireNonNull(
            readAndSetCapability("platformName", "caps.platformName"),
            "No 'platformName' specified for prefix: " + prefix);

        readAndSetCapability("deviceName", "caps.deviceName");

        if (type.isAndroid())
        {
            readAndSetCapability("appWaitActivity", "caps.appWaitActivity");
            readAndSetCapability("appWaitPackage", "caps.appWaitPackage");
        }
        else
        {
            readAndSetCapability("automationName", "caps.automationName");
            readAndSetCapability("udid", "caps.udid");
            // FIXME must be of type boolean...
            readAndSetCapability("showXcodeLog", "caps.showXcodeLog");
        }

        // This is required to not timeout drivers waiting for other devices
        // to start
        capabilities.setCapability("newCommandTimeout", 120);
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

    // FIXME isn't meetURL part of the config anyway ?
    public MobileParticipant startNewDriver(String meetServerURL)
    {
        AppiumDriver<WebElement> driver;

        if (type.isAndroid())
        {
            driver = new AndroidDriver<>(appiumUrl, capabilities);
        }
        else
        {
            driver = new IOSDriver<>(appiumUrl, capabilities);
        }

        MobileParticipant participant
            = new MobileParticipant(
                    driver,
                    prefix,
                    type,
                    getCfgProperty("bundleId"),
                    appPath,
                    meetServerURL);

        Logger.getGlobal().log(Level.INFO, "Started " + type + " driver");

        String bundleId = getCfgProperty("bundleId");

        if ("true".equalsIgnoreCase(getCfgProperty("reinstallApp"))
            && bundleId != null)
        {
            participant.reinstallAppIfInstalled();
        }

        return participant;
    }
}
