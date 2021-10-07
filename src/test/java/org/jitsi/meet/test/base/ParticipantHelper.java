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

import java.util.*;
import java.util.concurrent.*;

import static org.jitsi.meet.test.base.ParticipantOptions.GLOBAL_PROP_PREFIX;

/**
 * Helper class for managing {@link Participant}s.
 *
 * @param <P> the type of participants supported by the helper.
 */
public abstract class ParticipantHelper<P extends Participant>
{
    /**
     * The global config which will be used as a source for participant's
     * config properties.
     */
    private final Properties config;

    /**
     * The factory which creates participants compatible with this helper's
     * instance.
     */
    private ParticipantFactory<P> participantFactory;

    /**
     * The current test participant list, the order is important, as the
     * first to join is the owner of the conference, in some cases has more
     * options than the rest of the participants.
     */
    private final List<P> participants;

    /**
     * Default.
     *
     * @param config - The config which will be used as a properties source for
     * participant's configs.
     */
    protected ParticipantHelper(Properties config)
    {
        this.config = Objects.requireNonNull(config, "config");
        this.participants = new CopyOnWriteArrayList<>();
        this.participantFactory = null;
    }

    /**
     * Makes sure that this instance has been initialized or throws an
     * exception. Should be called in al places where
     * {@link #participantFactory} field is expected to be initialized.
     *
     * @throws IllegalStateException if this instance has not been initialized
     * yet.
     */
    private void assertInitialized()
    {
        if (participantFactory == null)
        {
            throw new IllegalStateException(
                "This instance has not been initialized yet.");
        }
    }

    /**
     * Creates a factory which produces participants compatible with this
     * helper.
     *
     * @return new {@link ParticipantFactory} instance which will be used by
     * this helper instance to create new participants.
     */
    protected abstract ParticipantFactory<P> createFactory();

    /**
     * Joins a participant, created if does not exists.
     *
     * @param configPrefix  the config prefix which is used to identify
     * the config properties which describe the new participant.
     * @return the participant which was created
     */
    public P createParticipant(String configPrefix)
    {
        return this.createParticipant(-1, configPrefix, null);
    }

    /**
     * Joins a participant, created if it does not exist.
     *
     * @param configPrefix the config prefix which is used to identify
     * the config properties which describe the new participant.
     * @param options custom options to be used for the participant.
     * @return the participant which was created
     */
    public P createParticipant(String configPrefix, ParticipantOptions options)
    {
        return this.createParticipant(-1, configPrefix, options);
    }

    /**
     * Joins a participant, created if it does not exist.
     *
     * @param ix The index of the participant or -1 for the next available.
     * @param configPrefix the config prefix which is used to identify
     * the config properties which describe the new participant.
     * @param options custom options to be used for the participant.
     * @return the participant which was created
     */
    public P createParticipant(int ix, String configPrefix, ParticipantOptions options)
    {
        assertInitialized();

        ParticipantOptions targetOptions = new ParticipantOptions();

        // Load the global properties
        targetOptions.load(config, ParticipantOptions.GLOBAL_PROP_PREFIX);

        // Load the config options onto of the globals
        targetOptions.load(config, configPrefix);

        // Put explicit options on top of whatever has been loaded from
        // the config and the globals
        if (options != null)
        {
            targetOptions.putAll(options);
        }

        // If at this point there's no participant type we'll go with Chrome,
        // because the web tests were first, and we don't want to require changes
        // to the old web run configs.
        if (targetOptions.getParticipantType() == null)
        {
            TestUtils.print(
                "No participant type specified for prefix: "
                    + configPrefix + ", will use Chrome...");
            targetOptions.setParticipantType(ParticipantType.chrome);
        }

        // Provide some default name if wasn't specified, neither in
        // the arguments nor in the config.
        if (StringUtils.isBlank(targetOptions.getName()))
        {
            targetOptions.setName(configPrefix);
        }

        P participant = participantFactory.createParticipant(targetOptions);

        if (ix > -1 && ix < participants.size())
        {
            participants.set(ix, participant);
        }
        else
        {
            participants.add(participant);
        }

        TestUtils.print("Started " + participant.getType() + " driver for prefix: " + configPrefix);

        return participant;
    }

    /**
     * Returns the participant if it exists or null.
     * @param index the index of the participant.
     * @return the participant if it exists or null.
     */
    public P get(int index)
    {
        return index < participants.size() ? participants.get(index) : null;
    }

    /**
     * Cleans up by closing all participants.
     */
    public void cleanup()
    {
        if (participants != null)
        {
            participants.stream()
                .filter(participant -> participant != null)
                .forEach(Participant::closeSafely);
            participants.clear();
        }
    }

    /**
     * Hangups a participant.
     * @param index the participant index to be hungup.
     */
    public void hangUpByIndex(int index)
    {
        Participant participant = get(index);
        if (participant != null)
            participant.hangUp();
    }

    /**
     * Hangups all participants.
     */
    public void hangUpAll()
    {
        participants.forEach(Participant::hangUp);
    }

    /**
     * Closes a participant's session/driver. No exception is thrown if close
     * do not work for some reason.
     *
     * @param participant the participant to close.
     */
    public void closeParticipant(P participant)
    {
        participant.closeSafely();

        // remove the index, so we do not mess up the number of participants
        // and their order/indexes in the list
        int ix = participants.indexOf(participant);
        participants.set(ix, null);
    }

    /**
     * Gets the list of all participants.
     * @return a copy of the list which holds all participants.
     */
    public List<P> getAll()
    {
        return new LinkedList<>(participants);
    }

    /**
     * Return new {@link JitsiMeetUrl} instance which has only
     * {@link JitsiMeetUrl#serverUrl} field initialized with the value from
     * {@link ParticipantOptions#JITSI_MEET_URL_PROP} system property.
     *
     * @return a new instance of {@link JitsiMeetUrl}.
     */
    public JitsiMeetUrl getJitsiMeetUrl()
    {
        JitsiMeetUrl url = new JitsiMeetUrl();

        String serverUrl = config.getProperty(ParticipantOptions.JITSI_MEET_URL_PROP);
        if (serverUrl == null)
        {
            throw new RuntimeException(
                "No server URL configured. Set one with " +
                        "-Djitsi-meet.instance.url=https://example.com");
        }

        url.setServerUrl(serverUrl);
        return url;
    }

    /**
     * Initializes this instance. Must be called, before first use.
     */
    public void initialize()
    {
        if (participantFactory != null)
        {
            throw new IllegalStateException(
                "This class has been initialized already");
        }

        participantFactory = createFactory();

        moveSystemGlobalProperties();
    }

    /**
     * This will move all global properties (not proceeded with any prefix)
     * under the {@link ParticipantOptions#GLOBAL_PROP_PREFIX}. This will allow
     * to simplify the code which has to deal with global properties.
     * Ideally we would not use global properties, but some of them are out of
     * our control.
     */
    private void moveSystemGlobalProperties()
    {
        List<String> systemGlobalKeys
            = participantFactory.getGlobalConfigKeys();

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
