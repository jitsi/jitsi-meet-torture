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
import org.jitsi.meet.test.mobile.*;
import org.openqa.selenium.remote.*;

import java.net.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * Creates mobile participant.
 */
public class MobileParticipantFactory
    extends ParticipantFactory<MobileParticipant>
{
    /**
     * Include global properties specific to mobile.
     * {@inheritDoc}
     */
    @Override
    public List<String> getGlobalConfigKeys()
    {
        List<String> systemGlobalKeys = new LinkedList<>();

        // If at any point Android or iOS will require to handle global
        // properties they will have to be included here as well. The reason why
        // it's done this way is that the ParticipantOptions type is not known
        // until config properties are loaded and merged with the explicit
        // options.
        systemGlobalKeys.addAll(
            MobileParticipantOptions.getSystemGlobalPropNames());

        return systemGlobalKeys;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected MobileParticipant doCreateParticipant(ParticipantOptions options)
    {
        ParticipantType type = options.getParticipantType();

        if (!type.isMobile())
        {
            throw new IllegalArgumentException(
                type + " is not supported by the mobile participant factory");
        }

        // The type here is important to have the right default values.
        MobileParticipantOptions targetOptions
            = type.isAndroid()
                ? new AndroidParticipantOptions()
                : new iOSParticipantOptions();

        targetOptions.putAll(options);

        URL appiumUrl = targetOptions.getAppiumServerUrl();
        DesiredCapabilities capabilities = targetOptions.createCapabilities();

        AppiumDriver<MobileElement> driver
            = type.isAndroid()
                ? new AndroidDriver<>(appiumUrl, capabilities)
                : new IOSDriver<>(appiumUrl, capabilities);

        driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);

        MobileParticipant participant
            = new MobileParticipant(
                    driver,
                    targetOptions.getName(),
                    targetOptions.getParticipantType(),
                    targetOptions.getBundleId(),
                    targetOptions.getCapabilityApp());

        if (targetOptions.shouldReinstallApp()
                && targetOptions.getBundleId() != null)
        {
            participant.reinstallAppIfInstalled();
        }

        return participant;
    }
}
