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
 * The set of options which configures an iOS
 * {@link org.jitsi.meet.test.mobile.MobileParticipant}.
 *
 * @author Pawel Domas
 */
public class iOSParticipantOptions
    extends MobileParticipantOptions
{
    /**
     * The property for Appium's automation's name capability.
     */
    private static final String CAPS_AUTOMATION
        = _CAPS_PROP_PREFIX + "automationName";

    /**
     * The property for Appium capability which prints extra info coming from
     * XCode. Useful during first time real device setup.
     */
    private static final String CAPS_SHOW_XCODE_LOG
        = _CAPS_PROP_PREFIX + "showXcodeLog";

    /**
     * The iOS UDID number required to install the app on real iOS device.
     */
    private static final String CAPS_UDID
        = _CAPS_PROP_PREFIX + "udid";

    /**
     * Default iOS value for {@link MobileParticipantOptions#PROP_BUNDLE_ID}.
     */
    private static final String DEFAULT_BUNDLE_ID = "org.jitsi.JitsiMeet.ios";

    /**
     * Appium capability which identifies the app. To be used if it has been
     * installed on the device already. Otherwise a full path to .ipk file
     * should specified under {@link #CAPS_APP} capability.
     */
    private static final String DEFAULT_CAPS_APP = DEFAULT_BUNDLE_ID;

    /**
     * Default value for {@link #CAPS_AUTOMATION}.
     */
    private static final String DEFAULT_CAPS_AUTOMATION = "XCUITest";

    /**
     * The iOS default {@link #CAPS_PLATFORM_NAME}.
     */
    private static final String IOS_PLATFORM_NAME = "ios";

    /**
     * {@inheritDoc}
     */
    @Override
    public DesiredCapabilities createCapabilities()
    {
        DesiredCapabilities capabilities = super.createCapabilities();

        readAndSetCapability(capabilities, CAPS_AUTOMATION);
        // FIXME must be of type boolean...
        readAndSetCapability(capabilities, CAPS_SHOW_XCODE_LOG);
        readAndSetCapability(capabilities, CAPS_UDID);

        return capabilities;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Properties initDefaults()
    {
        Properties defaults = super.initDefaults();

        defaults.setProperty(CAPS_APP, DEFAULT_CAPS_APP);
        defaults.setProperty(CAPS_AUTOMATION, DEFAULT_CAPS_AUTOMATION);
        defaults.setProperty(CAPS_PLATFORM_NAME, IOS_PLATFORM_NAME);

        defaults.setProperty(PROP_BUNDLE_ID, DEFAULT_BUNDLE_ID);

        return defaults;
    }
}
