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

import org.openqa.selenium.*;

import java.util.*;

public abstract class ParticipantFactory<T extends ParticipantOptions>
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
     * The configuration prefix to use for initializing the participant.
     *
     */
    public abstract Participant<? extends WebDriver> createParticipant(
        T options);

    public abstract T getDefaultParticipantOptions();

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
