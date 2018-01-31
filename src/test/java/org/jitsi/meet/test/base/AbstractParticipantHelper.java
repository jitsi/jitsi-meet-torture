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

import org.jitsi.meet.test.util.*;
import org.openqa.selenium.*;

import java.util.*;
import java.util.function.*;

public abstract class AbstractParticipantHelper
{
    /**
     * The current room name used.
     */
    protected String currentRoomName;

    /**
     * The current test participant list, the order is important, as the
     * first to join is the owner of the conference, in some cases has more
     * options than the rest of the participants.
     */
    private List<Participant<? extends WebDriver>> participants
        = new LinkedList<>();

    /**
     * Default.
     */
    protected AbstractParticipantHelper()
    {}

    /**
     * Constructs with predefined room name and participants.
     * @param roomName predefined room name.
     * @param participants the participants to add.
     */
    protected AbstractParticipantHelper(String roomName,
        List<Participant<? extends WebDriver>> participants)
    {
        this.currentRoomName = roomName;
        this.participants
            = Objects.requireNonNull(participants, "participants");
    }

    /**
     * Joins a participant, created if does not exists.
     *
     * @param index the participant index.
     * @param roomParameter a room parameter to add, if any.
     * @param fragment adds the given string to the fragment part of the URL
     * @param joinRef custom join method (optional).
     * @return the participant which was created
     */
    protected Participant joinParticipant(
            int                        index,
            String                     configPrefix,
            String                     roomParameter,
            String                     fragment,
            BiConsumer<String, String> joinRef)
    {
        Participant<? extends WebDriver> participant;

        if (index >= participants.size())
        {
            // we need to create this participant.
            participant
                = ParticipantFactory
                    .getInstance()
                    .createParticipant(configPrefix);

            participants.add(participant);

            // Adds a print in the console/selenium-node logs
            // useful when checking crashes or failures in node logs
            participant.executeScript(
                "console.log('--- Will start test:"
                    + getClass().getSimpleName() + "')");
        }
        else
            participant = participants.get(index);

        String roomName = currentRoomName;

        // we do not persist room params for now, in case of jwt
        // we want them just for one of the participants
        if (roomParameter != null)
            roomName += roomParameter;

        // join room
        participant.joinConference(roomName, fragment, joinRef);

        return participant;
    }

    /**
     * Returns the participant if it exists or null.
     * @param index the index of the participant.
     * @return the participant if it exists or null.
     */
    private Participant getParticipant(int index)
    {
        if (participants.size() <= index)
        {
            return null;
        }
        else
            return participants.get(index);
    }

    /**
     * Setup helper by generating the room name to use.
     */
    public void setup()
    {
        this.currentRoomName
            = "torture" + String.valueOf((int)(Math.random()*1000000));
    }

    /**
     * Cleans up by quiting all participants.
     */
    public void cleanup()
    {
        participants.stream().forEach(this::quitParticipant);
        participants.clear();
    }

    /**
     * Quits a participant if initialized.
     * @param participant the participant to quit.
     */
    private void quitParticipant(Participant<? extends WebDriver> participant)
    {
        if (participant == null)
            return;

        TestUtils.print("Quiting " + participant.getName());
        participant.quit();
    }

    /**
     * Hangups a participant.
     * @param index the participant index to be hungup.
     */
    protected void hangUpParticipant(int index)
    {
        Participant participant = getParticipant(index);
        if (participant != null)
            participant.hangUp();
    }

    /**
     * Hangups all participants.
     */
    public void hangUpAllParticipants()
    {
        participants.forEach(Participant::hangUp);
    }

    /**
     * Gets the list of all participants.
     * @return a copy of the list which holds all participants.
     */
    public List<Participant<? extends WebDriver>> getAllParticipants()
    {
        return new ArrayList<>(participants);
    }

    /**
     * Returns the first participant.
     * @return the first participant.
     */
    public Participant getParticipant1()
    {
        return getParticipant(0);
    }

    /**
     * Returns the second participant.
     * @return the second participant.
     */
    public Participant getParticipant2()
    {
        return getParticipant(1);
    }

    /**
     * Returns the third participant.
     * @return the third participant.
     */
    public Participant getParticipant3()
    {
        return getParticipant(2);
    }
}
