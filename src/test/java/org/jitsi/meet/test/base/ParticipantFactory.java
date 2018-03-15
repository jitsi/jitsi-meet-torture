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

import org.apache.commons.lang3.*;
import org.jitsi.meet.test.util.*;
import org.openqa.selenium.*;

import java.util.*;

/**
 * Used to create participants. Based on the type of the participant we choose
 * to use WebParticipantFactory or MobileParticipantFactory.
 * @param <T> the type of the options (web or mobile).
 */
public abstract class ParticipantFactory<T extends ParticipantOptions>
{
    /**
     * A prefix for global options (not per participant).
     */
    private static final String GLOBAL_PROP_PREFIX = "jitsi-meet";

    /**
     * The url of the deployment to connect to.
     */
    private static final String JITSI_MEET_URL_PROP
        = GLOBAL_PROP_PREFIX + ".instance.url";

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

        moveSystemGlobalProperties();
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

        // Load the global properties
        targetOptions.load(config, GLOBAL_PROP_PREFIX);

        // Load the config options onto of the globals
        targetOptions.load(config, configPrefix);

        // Put explicit options on top of whatever has been loaded from
        // the config and the globals
        if (options != null)
        {
            targetOptions.putAll(options);
        }

        // If at this point there's no participant type we'll go with Chrome,
        // because the web tests were first and we don't want to require changes
        // to the old web run configs.
        ParticipantType participantType = targetOptions.getParticipantType();
        if (participantType == null)
        {
            TestUtils.print(
                "No participant type specified for prefix: "
                    + configPrefix + ", will use Chrome...");
            targetOptions.setParticipantType(
                participantType = ParticipantType.chrome);
        }

        // Provide some default name if wasn't specified neither in
        // the arguments nor in the config.
        if (StringUtils.isBlank(targetOptions.getName()))
        {
            targetOptions.setName(configPrefix);
        }

        Participant<? extends WebDriver> participant
            = doCreateParticipant(targetOptions);

        participant.initialize();

        TestUtils.print(
            "Started " + participantType
                + " driver for prefix: " + configPrefix);

        return participant;
    }

    /**
     * Gathers all the participant properties which can be specified from
     * the system global scope (are not proceeded with any prefix due to various
     * reasons). Those properties are then moved over to the
     * {@link #GLOBAL_PROP_PREFIX}, so that they can be loaded with global
     * prefixed options (since they share the same global level).
     *
     * @return the list of key which will be moved over to
     * the {@link #GLOBAL_PROP_PREFIX}.
     */
    protected List<String> getGlobalConfigKeys()
    {
        return new LinkedList<>();
    }

    /**
     * Creates a new {@link Participant} for given options.
     *
     * @param options the {@link ParticipantOptions} for which new participant
     * is to be created. This set already contains any properties loaded from
     * the config merged with the explicitly passed options to
     * {@link #createParticipant(String, ParticipantOptions)}.
     *
     * @return new {@link Participant} for given set of config options.
     */
    protected abstract Participant<? extends WebDriver> doCreateParticipant(
            ParticipantOptions options);

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

    /**
     * This will move all global properties (not proceeded with any prefix)
     * under the {@link #GLOBAL_PROP_PREFIX}. This will allow to simplify
     * the code which has to deal with global properties.
     * Ideally we would not use global properties, but some of them are out of
     * our control.
     */
    private void moveSystemGlobalProperties()
    {
        List<String> systemGlobalKeys = getGlobalConfigKeys();

        for (String systemGlobalKey : systemGlobalKeys)
        {
            String value = config.getProperty(systemGlobalKey);

            if (StringUtils.isNotBlank(value))
            {
                config.setProperty(
                    GLOBAL_PROP_PREFIX + "." + systemGlobalKey, value);

                // Clear the global one
                config.remove(systemGlobalKey);
            }
        }
    }
}
