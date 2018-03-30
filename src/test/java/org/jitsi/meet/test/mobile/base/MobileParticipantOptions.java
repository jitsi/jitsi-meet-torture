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

import org.apache.commons.lang3.*;
import org.jitsi.meet.test.base.*;
import org.openqa.selenium.remote.*;

import java.net.*;
import java.util.*;

/**
 * Holds mobile specific options.
 */
public class MobileParticipantOptions
    extends ParticipantOptions
{
    /**
     * The prefix is added to every config property which value ends up in
     * Appium's capabilities.
     */
    static final String _CAPS_PROP_PREFIX = "caps.";

    /**
     * The property key for Appium's "app" capability. The value under this key
     * can be either the bundle ID in case the app is installed or the full path
     * to binary if the app is to be installed before the tests.
     */
    static final String CAPS_APP = _CAPS_PROP_PREFIX + "app";

    /**
     * The property key for Appium's "automationName" capability.
     */
    private static final String CAPS_AUTOMATION_NAME
        = _CAPS_PROP_PREFIX + "automationName";

    /**
     * The property key for Appium's "deviceName" capability.
     */
    private static final String CAPS_DEVICE_NAME
        = _CAPS_PROP_PREFIX + "deviceName";

    /**
     * The property key for Appium's "platformName" capability.
     */
    static final String CAPS_PLATFORM_NAME
        = _CAPS_PROP_PREFIX + "platformName";

    /**
     * Default Appium server address (local).
     */
    private static final String DEFAULT_APPIUM_SERVER_ADDRESS = "127.0.0.1";

    /**
     * Default Appium server port.
     */
    private static final String DEFAULT_APPIUM_SERVER_PORT = "4723";

    /**
     * The name of the property which specifies the Appium server address.
     */
    private static final String PROP_APPIUM_SERVER_ADDRESS
        = "appium.server.address";

    /**
     * The name of the property which specifies the Appium server's port.
     */
    private static final String PROP_APPIUM_SERVER_PORT = "appium.server.port";

    /**
     * The property name for bundle ID. Required to identify reinstalled app.
     * @see #PROP_REINSTALL_APP
     */
    static final String PROP_BUNDLE_ID = "bundleId";

    /**
     * The property key which when set to "true" will reinstall the app, before
     * the test will start.
     */
    private static final String PROP_REINSTALL_APP = "reinstallApp";

    /**
     * Creates {@link DesiredCapabilities} out of the capabilities properties
     * set held by this instance (all that start with
     * {@link #_CAPS_PROP_PREFIX}).
     *
     * @return new {@link DesiredCapabilities} instance configured based on
     * the capabilities options held by this instance.
     */
    public DesiredCapabilities createCapabilities()
    {
        DesiredCapabilities capabilities = new DesiredCapabilities();

        readAndSetCapability(capabilities, CAPS_APP);
        readAndSetCapability(capabilities, CAPS_AUTOMATION_NAME);
        readAndSetCapability(capabilities, CAPS_PLATFORM_NAME);
        readAndSetCapability(capabilities, CAPS_DEVICE_NAME);

        // This is required to not timeout drivers waiting for other devices
        // to start
        capabilities.setCapability("newCommandTimeout", 120);

        return capabilities;
    }

    /**
     * The getter for {@link #PROP_APPIUM_SERVER_ADDRESS}.
     *
     * @return the server address part of the URL.
     */
    private String getAppiumServerAddress()
    {
        return getProperty(PROP_APPIUM_SERVER_ADDRESS);
    }

    /**
     * The getter for {@link #PROP_APPIUM_SERVER_PORT}.
     *
     * @return the port number as a String.
     */
    private String getAppiumServerPort()
    {
        return getProperty(PROP_APPIUM_SERVER_PORT);
    }

    /**
     * Constructs Appium server URL.
     *
     * @return {@link URL} from the options held by this instance.
     */
    public URL getAppiumServerUrl()
    {
        String appiumAddress
            = Objects.requireNonNull(
                    getAppiumServerAddress(), "appiumServerAddress");
        String appiumPort
            = Objects.requireNonNull(
                    getAppiumServerPort(), "appiumServerPort");

        try
        {
            return new URL(
                "http://" + appiumAddress + ":" + appiumPort + "/wd/hub");
        }
        catch (MalformedURLException exc)
        {
            throw new IllegalArgumentException(
                "Invalid Appium server URL", exc);
        }
    }

    /**
     * The getter for {@link #PROP_BUNDLE_ID}.
     *
     * @return a string.
     */
    public String getBundleId()
    {
        return getProperty(PROP_BUNDLE_ID);
    }

    /**
     * Returns the Appium app capability. See {@link #CAPS_APP}.
     *
     * @return a string with the app capability value.
     */
    public String getCapabilityApp()
    {
        return getProperty(CAPS_APP);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Properties initDefaults()
    {
        Properties defaults = super.initDefaults();

        defaults.setProperty(
            PROP_APPIUM_SERVER_ADDRESS, DEFAULT_APPIUM_SERVER_ADDRESS);
        defaults.setProperty(
            PROP_APPIUM_SERVER_PORT, DEFAULT_APPIUM_SERVER_PORT);

        return defaults;
    }

    /**
     * Enumerates mobile specific global properties.
     */
    public static List<String> getSystemGlobalPropNames()
    {
        List<String> globals = new LinkedList<>();

        // Those properties are specified by the Amazon device farm runner and
        // we can not do anything about that, so lets just have them moved over
        // to the global prefix.
        globals.add(PROP_APPIUM_SERVER_ADDRESS);
        globals.add(PROP_APPIUM_SERVER_PORT);

        return globals;
    }

    /**
     * This will read a capability property and set it on given
     * {@link DesiredCapabilities} instance while stripping
     * the {@link #_CAPS_PROP_PREFIX} from the given <tt>key</tt>.
     *
     * @param caps the capabilities instance to be modified.
     * @param key one of the capability properties which is supposed to start
     * with the {@link #_CAPS_PROP_PREFIX}.
     */
    void readAndSetCapability(DesiredCapabilities caps, String key)
    {
        String propValue = getProperty(key);

        if (StringUtils.isNotBlank(propValue))
        {
            // When the value is set to the capabilities object, the "caps."
            // property prefix needs to be stripped out.
            key = key.substring(_CAPS_PROP_PREFIX.length());

            caps.setCapability(key, propValue);
        }
    }

    /**
     * Tells whether the app should be reinstalled just after
     * {@link org.jitsi.meet.test.mobile.MobileParticipant} gets created.
     *
     * @return <tt>true</tt> of <tt>false</tt> (by default).
     */
    boolean shouldReinstallApp()
    {
        return getBooleanProperty(PROP_REINSTALL_APP);
    }
}
