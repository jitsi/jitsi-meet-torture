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
package org.jitsi.meet.test.mobile.base;

import org.openqa.selenium.remote.*;

import java.util.*;

/**
 * The set of options which configures an Android
 * {@link org.jitsi.meet.test.mobile.MobileParticipant}.
 *
 * @author Pawel Domas
 */
public class AndroidParticipantOptions
    extends MobileParticipantOptions
{
    /**
     * The Android's default value for
     * {@link MobileParticipantOptions#CAPS_PLATFORM_NAME}.
     */
    private static final String ANDROID_PLATFORM_NAME = "android";

    /**
     * If the {@link #CAPS_APP} is not an .apk file then Android driver will
     * require both {@code #CAPS_ACTIVITY} and {@link #CAPS_PACKAGE} to be
     * specified in order to run the app without installation (which will work
     * if the app is on the device already).
     */
    private static final String CAPS_ACTIVITY
        = _CAPS_PROP_PREFIX + "appActivity";

    /**
     * Specifies the Android's app package name. Required to be specified if
     * the {@link #CAPS_APP} is missing.
     */
    private static final String CAPS_PACKAGE = _CAPS_PROP_PREFIX + "appPackage";

    /**
     * The name of Appium capability which tells the Appium driver which
     * activity should it expect when the app starts.
     */
    private static final String CAPS_WAIT_ACTIVITY
        = _CAPS_PROP_PREFIX + "appWaitActivity";

    /**
     * The package name of the wait activity. See {@link #CAPS_WAIT_ACTIVITY}.
     */
    private static final String CAPS_WAIT_PACKAGE
        = _CAPS_PROP_PREFIX + "appWaitPackage";

    /**
     * The default value for {@link #CAPS_ACTIVITY}.
     */
    private static final String DEFAULT_ACTIVITY = ".MainActivity";

    /**
     * Default Android bundle ID of the app being tested.
     */
    private static final String DEFAULT_BUNDLE_ID = "org.jitsi.meet";

    /**
     * Default wait activity. It needs to be overridden in order to be able to
     * start the driver at the settings screen which Jitsi Meet will open after
     * fresh install asking for extra permissions.
     */
    private static final String DEFAULT_CAPS_WAIT_ACTIVITY
        = "com.android.settings.Settings$AppDrawOverlaySettingsActivity,"
            + ".MainActivity";

    /**
     * Required for {@link #DEFAULT_CAPS_WAIT_ACTIVITY} to work correctly.
     */
    private static final String DEFAULT_CAPS_WAIT_PACKAGE
        = "com.android.settings,org.jitsi.meet";

    /**
     * {@inheritDoc}
     */
    @Override
    public DesiredCapabilities createCapabilities()
    {
        DesiredCapabilities capabilities = super.createCapabilities();

        readAndSetCapability(capabilities, CAPS_ACTIVITY);
        readAndSetCapability(capabilities, CAPS_PACKAGE);
        readAndSetCapability(capabilities, CAPS_WAIT_ACTIVITY);
        readAndSetCapability(capabilities, CAPS_WAIT_PACKAGE);

        return capabilities;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Properties initDefaults()
    {
        Properties defaults = super.initDefaults();

        defaults.setProperty(
                CAPS_ACTIVITY, DEFAULT_ACTIVITY);
        defaults.setProperty(
                CAPS_PACKAGE, DEFAULT_BUNDLE_ID);
        defaults.setProperty(
                CAPS_PLATFORM_NAME, ANDROID_PLATFORM_NAME);
        defaults.setProperty(
                CAPS_WAIT_ACTIVITY, DEFAULT_CAPS_WAIT_ACTIVITY);
        defaults.setProperty(
                CAPS_WAIT_PACKAGE, DEFAULT_CAPS_WAIT_PACKAGE);

        defaults.setProperty(
                PROP_BUNDLE_ID, DEFAULT_BUNDLE_ID);

        return defaults;
    }
}
