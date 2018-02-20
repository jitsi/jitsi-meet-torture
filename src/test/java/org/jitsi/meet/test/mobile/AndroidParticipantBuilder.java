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

import org.jitsi.meet.test.base.*;

import java.util.*;

/**
 * The Android version of {@link MobileParticipantBuilder}. Holds all the stuff
 * specific to Android {@link MobileParticipant}.
 *
 * @author Pawel Domas
 */
public class AndroidParticipantBuilder extends MobileParticipantBuilder
{

    /**
     * Default Android's value for
     * {@link MobileParticipantBuilder#CAPS_PLATFORM_NAME}.
     */
    private static final String ANDROID_PLATFORM_NAME = "android";

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
     * Holds default config properties set for all Android participant builders.
     */
    private static final Properties DEFAULT_ANDROID_PROPERTIES
        = new Properties();

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
     * Initialize default Android properties
     */
    static
    {
        DEFAULT_ANDROID_PROPERTIES.setProperty(
                CAPS_PLATFORM_NAME, ANDROID_PLATFORM_NAME);
        DEFAULT_ANDROID_PROPERTIES.setProperty(
                CAPS_WAIT_ACTIVITY, DEFAULT_CAPS_WAIT_ACTIVITY);
        DEFAULT_ANDROID_PROPERTIES.setProperty(
                CAPS_WAIT_PACKAGE, DEFAULT_CAPS_WAIT_PACKAGE);
        DEFAULT_ANDROID_PROPERTIES.setProperty(
                PROP_BUNDLE_ID, DEFAULT_BUNDLE_ID);
    }

    /**
     * Initializes {@link AndroidParticipantBuilder}.
     *
     * @param config - The config holding participant's properties.
     * @param prefix - The config prefix added to each property name in order to
     * get participant's property key.
     */
    public AndroidParticipantBuilder(Properties config, String prefix)
    {
        super(
            config,
            DEFAULT_ANDROID_PROPERTIES,
            prefix,
            ParticipantType.android);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void configureOsSpecificCapabilities()
    {
        readAndSetCapability(CAPS_WAIT_ACTIVITY);
        readAndSetCapability(CAPS_WAIT_PACKAGE);
    }
}
