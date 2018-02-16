/*
 * Copyright @ Atlassian Pty Ltd
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
package org.jitsi.meet.test.mobile;

import io.appium.java_client.*;
import io.appium.java_client.android.*;
import io.appium.java_client.ios.*;
import org.jitsi.meet.test.base.*;
import org.jitsi.meet.test.util.*;
import org.openqa.selenium.*;
import org.openqa.selenium.remote.*;

import java.net.*;
import java.util.*;

/**
 * The code creates mobile participants.
 *
 * @author Pawel Domas
 */
public abstract class MobileParticipantBuilder
{
    /**
     * The prefix is added to every config property which value ends up in
     * Appium's capabilities.
     */
    protected static final String _CAPS_PROP_PREFIX = "caps.";

    /**
     * The property key for Appium's "app" capability. The value under this key
     * can be either the bundle ID in case the app is installed or the full path
     * to binary if the app is to be installed before the tests.
     */
    protected static final String CAPS_APP = _CAPS_PROP_PREFIX + "app";

    /**
     * The property key for Appium's "deviceName" capability.
     */
    private static final String CAPS_DEVICE_NAME
        = _CAPS_PROP_PREFIX + "deviceName";

    /**
     * The property key for Appium's "platformName" capability.
     */
    protected static final String CAPS_PLATFORM_NAME
        = _CAPS_PROP_PREFIX + "platformName";

    /**
     * Default Appium server address (local).
     */
    private static final String DEFAULT_APPIUM_ADDRESS = "127.0.0.1";

    /**
     * Default Appium server port.
     */
    private static final String DEFAULT_APPIUM_PORT = "4723";

    /**
     * The property name for bundle ID. Required to identify reinstalled app.
     * @see #PROP_REINSTALL_APP
     */
    protected static final String PROP_BUNDLE_ID = "bundleId";

    /**
     * The property key which when set to "true" will reinstall the app, before
     * the test will start.
     */
    private static final String PROP_REINSTALL_APP = "reinstallApp";

    /**
     * The app as in Appium's driver understanding, which can be either bundle
     * id or full path to the binary file.
     */
    private final String app;

    /**
     * The URL pointing to the Appium server.
     */
    private final URL appiumUrl;

    /**
     * Appium driver's capabilities for the new {@link MobileParticipant}.
     */
    private final DesiredCapabilities capabilities;

    /**
     * The config from which participant's properties will be read.
     */
    private final Properties configProperties;

    /**
     * The default properties which will be used in case a value is missing from
     * {@link #configProperties}.
     */
    private final Properties defaultConfigProperties;

    /**
     * The prefix added to each property key in order to retrieve the values for
     * the new participant.
     */
    private final String prefix;

    /**
     * The participant's type.
     */
    private final ParticipantType type;

    /**
     * Creates new {@link MobileParticipantBuilder} for given type and
     * properties.
     *
     * @param config - The config from which participant's properties will be
     * read.
     * @param configPrefix - The prefix which will be added to every property
     * key in order to locate properties specific to new
     * {@link MobileParticipant} instance being constructed.
     * @param participantType - The type fo the {@link MobileParticipant} to be
     * created by the new {@link MobileParticipantBuilder} instance.
     *
     * @return new {@link MobileParticipantBuilder} for the given arguments.
     */
    static public MobileParticipantBuilder createBuilder(
            Properties config,
            String configPrefix,
            ParticipantType participantType)
    {
        switch (participantType)
        {
            case android:
                return new AndroidParticipantBuilder(config, configPrefix);
            case ios:
                return new iOsParticipantBuilder(config, configPrefix);
            default:
                throw new IllegalArgumentException(
                    "Unsupported mobile participant type: " + participantType);
        }
    }

    /**
     * Constructs Appium server URL.
     *
     * @param appiumAddress - The address part of the URL.
     * @param appiumPort - The server port to be included in the URL.
     *
     * @return {@link URL} for given arguments.
     */
    private static URL createAppiumUrl(String appiumAddress, String appiumPort)
    {
        if (appiumAddress == null)
        {
            appiumAddress = DEFAULT_APPIUM_ADDRESS;
        }

        if (appiumPort == null)
        {
            appiumPort = DEFAULT_APPIUM_PORT;
        }

        try
        {
            return new URL(
                "http://" + appiumAddress +":" + appiumPort + "/wd/hub");
        }
        catch (MalformedURLException exc)
        {
            throw new IllegalArgumentException(
                "Invalid Appium server URL", exc);
        }
    }

    /**
     * Initializes new {@link MobileParticipantBuilder}.
     *
     * @param config - The configuration from which participant's properties
     * will be read.
     * @param defaultConfig - A set of default values which will be used in case
     * a property is not found in the <tt>config</tt>.
     * @param prefix - A prefix which will be added at the beginning of every
     * property key in order to identify the keys corresponding to
     * the participant constructed by the new {@link MobileParticipantBuilder}.
     * @param type - The type of that participant that wil be created by the new
     * {@link MobileParticipantBuilder}.
     */
    public MobileParticipantBuilder(Properties         config,
                                    Properties         defaultConfig,
                                    String             prefix,
                                    ParticipantType    type)
    {
        this.appiumUrl
            = createAppiumUrl(
                    config.getProperty("appium.server.address"),
                    config.getProperty("appium.server.port"));

        this.configProperties
            = Objects.requireNonNull(config, "configProperties");

        this.defaultConfigProperties
            = defaultConfig != null ? defaultConfig : new Properties();

        this.prefix = Objects.requireNonNull(prefix, "prefix");

        this.type = Objects.requireNonNull(type, "type");

        this.capabilities = new DesiredCapabilities();

        this.app = readAndSetCapability(CAPS_APP);

        readAndSetCapability(CAPS_PLATFORM_NAME);
        readAndSetCapability(CAPS_DEVICE_NAME);

        // This is required to not timeout drivers waiting for other devices
        // to start
        capabilities.setCapability("newCommandTimeout", 120);

        configureOsSpecificCapabilities();
    }

    /**
     * Method called in the constructor in order to configure OS specific
     * Appium driver capabilities.
     */
    protected abstract void configureOsSpecificCapabilities();

    /**
     * Gets the property from {@link #configProperties}. A default value from
     * {@link #defaultConfigProperties} will be used if not found.
     *
     * @param name - The name of the config property to be retrieved (without
     * the participant's prefix part).
     * @return a <tt>String</tt>
     */
    private String getCfgProperty(String name)
    {
        Object value = configProperties.get(prefix + "." + name);

        return value != null
            ? String.valueOf(value)
            : defaultConfigProperties.getProperty(name);
    }

    /**
     * Will read a value from {@link #configProperties} with a fallback to
     * {@link #defaultConfigProperties} and set it in the {@link #capabilities}
     * object.
     *
     * @param key the name of the capability to set (with
     * {@link #_CAPS_PROP_PREFIX}.
     */
    protected String readAndSetCapability(String key)
    {
        String propValue = getCfgProperty(key);

        if (propValue != null)
        {
            // When the value is set to the capabilities object, the "caps."
            // property prefix needs to be stripped out.
            key = key.substring(_CAPS_PROP_PREFIX.length());

            capabilities.setCapability(key, propValue);
        }

        return propValue;
    }

    /**
     * Start new Appium driver and return {@link MobileParticipant} wrapped
     * around it for given <tt>meetServerURL</tt>.
     *
     * @param meetServerURL - The server address part of the
     * {@link JitsiMeetUrl} which will be automatically appended to every
     * conference joined by the new {@link MobileParticipant} created.
     *
     * @return new {@link MobileParticipant} for given server URL, constructed
     * based on the config information contained in this
     * {@link MobileParticipantBuilder}.
     */
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

        String bundleId = getCfgProperty(PROP_BUNDLE_ID);

        MobileParticipant participant
            = new MobileParticipant(
                    driver,
                    prefix,
                    type,
                    bundleId,
                    app,
                    meetServerURL);

        TestUtils.print(
                "Started " + type + " driver for config prefix: " + prefix);

        if (Boolean.valueOf(getCfgProperty(PROP_REINSTALL_APP))
                && bundleId != null)
        {
            participant.reinstallAppIfInstalled();
        }

        return participant;
    }
}
