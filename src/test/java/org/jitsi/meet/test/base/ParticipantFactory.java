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

import java.util.*;

/**
 * Used to create participants. Based on the type of the participant we choose
 * to use WebParticipantFactory or MobileParticipantFactory.
 *
 * @param <P> the participant's type produced by the factory implementation.
 */
public abstract class ParticipantFactory<P extends Participant>
{
    /**
     * Creates participant the type is extracted from the configuration
     * using the supplied configPrefix.
     * @param options custom options to be used when creating the participant.
     */
    public P createParticipant(ParticipantOptions options)
    {
        P participant = doCreateParticipant(options);

        participant.initialize();

        return participant;
    }

    /**
     * Gathers all the participant properties which can be specified from
     * the system global scope (are not proceeded with any prefix due to various
     * reasons). Those properties are then moved over to the
     * {@link ParticipantOptions#GLOBAL_PROP_PREFIX}, so that they can be loaded
     * with global prefixed options (since they share the same global level).
     *
     * @return the list of key which will be moved over to
     * the {@link ParticipantOptions#GLOBAL_PROP_PREFIX}.
     */
    protected List<String> getGlobalConfigKeys()
    {
        return new LinkedList<>();
    }

    /**
     * Creates a new {@link Participant} for given options.
     *
     * @param options the {@link ParticipantOptions} for which new participant
     * is to be created.
     *
     * @return new {@link Participant} for given set of config options.
     */
    protected abstract P doCreateParticipant(ParticipantOptions options);
}
