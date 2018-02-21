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
package org.jitsi.meet.test.base;

import org.jitsi.meet.test.mobile.base.*;
import org.jitsi.meet.test.util.*;
import org.jitsi.meet.test.web.*;
import org.openqa.selenium.*;

import java.util.*;

/**
 * Used to create participants. Based on the type of the participant we choose
 * to use WebParticipantFactory or MobileParticipantFactory.
 * @param <T> the type of the options (web or mobile).
 */
public class ParticipantFactory<T extends ParticipantOptions>
{
    /**
     * The url of the deployment to connect to.
     */
    public static final String JITSI_MEET_URL_PROP = "jitsi-meet.instance.url";

    /**
     * The test config.
     */
    protected final Properties config;

    /**
     * The private constructor of the factory.
     *
     * @param config - A <tt>Properties</tt> instance holding configuration
     * properties required to setup new participants.
     */
    protected ParticipantFactory(Properties config)
    {
        this.config = Objects.requireNonNull(config, "config");
    }

    /**
     * Creates participant the type is extracted from the configuration
     * using the supplied configPrefix.
     * @param configPrefix the configuration prefix to use for initializing
     * the participant.
     * @param options custom options to be used when creating the participant.
     */
    public Participant<? extends WebDriver> createParticipant(
        String configPrefix,
        ParticipantOptions options)
    {
        ParticipantOptions targetOptions = new ParticipantOptions();
        targetOptions.load(config, configPrefix);

        // FIXME At some point it was decided that a name will be the substring
        // of the config prefix after the first dot. Maybe make just use
        // the config prefix instead ?
        // This way there will be no requirement for the prefix to contain
        // the dot (this requirement is not described anywhere).
        String name = configPrefix.substring(configPrefix.indexOf('.') + 1);
        targetOptions.setName(name);

        // Put explicit options on top of whatever has been loaded from
        // the config
        targetOptions.merge(options);

        // It will be Chrome by default...
        ParticipantType participantType = targetOptions.getParticipantType();
        if (participantType == null)
        {
            TestUtils.print(
                "No participant type specified for prefix: "
                    + configPrefix + ", will use Chrome...");
            targetOptions.setParticipantType(
                participantType = ParticipantType.chrome);
        }

        if (participantType.isWeb())
        {
            return new WebParticipantFactory(config)
                .createParticipant(
                    configPrefix,
                    targetOptions);
        }
        else if (participantType.isMobile())
        {
            return new MobileParticipantFactory(config)
                .createParticipant(
                    configPrefix,
                    targetOptions);
        }
        else
        {
            throw new IllegalArgumentException("Unknown participant type");
        }
    }

    /**
     * Return new {@link JitsiMeetUrl} instance which has only
     * {@link JitsiMeetUrl#serverUrl} field initialized with the value from
     * {@link #JITSI_MEET_URL_PROP} system property.
     *
     * @return a new instance of {@link JitsiMeetUrl}.
     */
    public JitsiMeetUrl getJitsiMeetUrl()
    {
        JitsiMeetUrl url = new JitsiMeetUrl();

        url.setServerUrl(config.getProperty(JITSI_MEET_URL_PROP));

        return url;
    }
}
