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
package org.jitsi.meet.test.hybrid;

import org.jitsi.meet.test.base.*;
import org.jitsi.meet.test.mobile.*;
import org.jitsi.meet.test.mobile.base.*;
import org.jitsi.meet.test.web.*;
import org.openqa.selenium.*;

import java.util.*;

/**
 * A factory which will create either {@link WebParticipant} or
 * {@link MobileParticipant} depending on the final {@link ParticipantType}
 * resolved after merging all config layers of the {@link ParticipantOptions}.
 */
public class HybridParticipantFactory
    extends ParticipantFactory<ParticipantOptions>
{
    /**
     * The mobile participant factory.
     */
    private final MobileParticipantFactory mobileFactory;

    /**
     * The web participant factory.
     */
    private final WebParticipantFactory webFactory;

    /**
     * The constructor of the factory.
     *
     * @param config - A <tt>Properties</tt> instance holding configuration
     * properties required to setup new participants.
     */
    HybridParticipantFactory(Properties config)
    {
        super(config);

        this.webFactory = new WebParticipantFactory(config);
        this.mobileFactory = new MobileParticipantFactory(config);
    }

    /**
     * Includes global keys from both mobile and web worlds.
     *
     * {@inheritDoc}
     */
    @Override
    protected List<String> getGlobalConfigKeys()
    {
        List<String> globalKeys = super.getGlobalConfigKeys();

        globalKeys.addAll(webFactory.getGlobalConfigKeys());
        globalKeys.addAll(mobileFactory.getGlobalConfigKeys());

        return globalKeys;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Participant<? extends WebDriver> doCreateParticipant(
            ParticipantOptions options)
    {
        ParticipantType type = options.getParticipantType();

        if (type == null || type.isWeb())
        {
            return webFactory.doCreateParticipant(options);
        }
        else
        {
            return mobileFactory.doCreateParticipant(options);
        }
    }
}
