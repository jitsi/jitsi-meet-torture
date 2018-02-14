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
 * The iOS version of {@link MobileParticipantBuilder} which holds all the iOS
 * specific stuff.
 *
 * @author Pawel Domas
 */
public class iOsParticipantBuilder extends MobileParticipantBuilder
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
     * Default iOS value for {@link MobileParticipantBuilder#PROP_BUNDLE_ID}.
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
     * Default iOS config properties passed over to
     * {@link MobileParticipantBuilder}.
     */
    private static final Properties DEFAULT_IOS_PROPERTIES = new Properties();

    /**
     * The iOS default {@link #CAPS_PLATFORM_NAME}.
     */
    private static final String IOS_PLATFORM_NAME = "ios";

    /**
     * Initializes default iOS config properties.
     */
    static
    {
        DEFAULT_IOS_PROPERTIES.setProperty(CAPS_APP, DEFAULT_CAPS_APP);
        DEFAULT_IOS_PROPERTIES.setProperty(
            CAPS_AUTOMATION, DEFAULT_CAPS_AUTOMATION);
        DEFAULT_IOS_PROPERTIES.setProperty(PROP_BUNDLE_ID, DEFAULT_BUNDLE_ID);
        DEFAULT_IOS_PROPERTIES.setProperty(
            CAPS_PLATFORM_NAME, IOS_PLATFORM_NAME);
    }

    /**
     * Initializes new {@link iOsParticipantBuilder}.
     *
     * @param config - The config from which participant's properties will be
     * retrieved.
     * @param prefix - The prefix which will be added to properties keys in
     * order to figure out participant's property names.
     */
    public iOsParticipantBuilder(Properties config, String prefix)
    {
        super(config, DEFAULT_IOS_PROPERTIES, prefix, ParticipantType.ios);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void configureOsSpecificCapabilities()
    {
        readAndSetCapability(CAPS_AUTOMATION);
        readAndSetCapability(CAPS_UDID);
        // FIXME must be of type boolean...
        readAndSetCapability(CAPS_SHOW_XCODE_LOG);
    }
}
