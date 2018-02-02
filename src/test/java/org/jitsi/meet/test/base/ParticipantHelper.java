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

/**
 * Helper class for managing {@link Participant}s.
 */
public class ParticipantHelper
{
    private final ParticipantFactory participantFactory;

    /**
     * The current test participant list, the order is important, as the
     * first to join is the owner of the conference, in some cases has more
     * options than the rest of the participants.
     */
    private final List<Participant<? extends WebDriver>> participants;

    /**
     * Default.
     */
    protected ParticipantHelper()
    {
        participants = new LinkedList<>();
        participantFactory = new ParticipantFactory();
    }

    /**
     * Constructs with predefined room name and participants.
     * @param participants the participants to add.
     */
    protected ParticipantHelper(ParticipantHelper participants)
    {
        this.participants = participants.getAll();
        this.participantFactory = participants.participantFactory;
    }

    /**
     * Joins a participant, created if does not exists.
     *
     * @param configPrefix  the config prefix which is used to identify
     * the config properties which describe the new participant.
     * @return the participant which was created
     */
    public Participant createParticipant(String configPrefix)
    {
        Participant<? extends WebDriver> participant
            = participantFactory.createParticipant(configPrefix);

        participants.add(participant);

        // Adds a print in the console/selenium-node logs
        // useful when checking crashes or failures in node logs
        participant.executeScript(
                "console.log('--- Will start test:"
                    + getClass().getSimpleName() + "')");

        return participant;
    }

    /**
     * Returns the participant if it exists or null.
     * @param index the index of the participant.
     * @return the participant if it exists or null.
     */
    public Participant get(int index)
    {
        return index < participants.size() ? participants.get(index) : null;
    }

    /**
     * Cleans up by quiting all participants.
     */
    public void cleanup()
    {
        participants.stream().forEach(Participant::quitSafely);
        participants.clear();
    }

    /**
     * @return the {@link ParticipantFactoryConfig} instance used by the
     * underlying {@link ParticipantFactory} to create new participants.
     */
    public ParticipantFactoryConfig getFactoryConfig()
    {
        return participantFactory;
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
     * Gets the list of all participants.
     * @return a copy of the list which holds all participants.
     */
    public List<Participant<? extends WebDriver>> getAll()
    {
        return new LinkedList<>(participants);
    }
}
