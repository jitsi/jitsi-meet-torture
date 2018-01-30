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
import java.util.stream.*;

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
        Participant<? extends WebDriver>... participants)
    {
        this.currentRoomName = roomName;

        // add only non null values
        this.participants.addAll(
            Arrays.stream(participants)
                .filter(p -> p != null)
                .collect(Collectors.toList()));
    }

    /**
     * Starts the owner, if it is not started.
     */
    public void ensureOneParticipant()
    {
        ensureOneParticipant((String)null);
    }

    /**
     * Starts the owner, if it is not started.
     * @param fragment adds the given string to the fragment part of the URL
     */
    public void ensureOneParticipant(String fragment)
    {
        this.ensureOneParticipant(null, fragment);
    }

    /**
     * Starts the owner, if it is not started.
     * @param roomParameter an extra parameter to the url. The fragment adds
     * parameters as # where roomParameter actually changes query parameters
     * adding ?something=value.
     * @param fragment adds the given string to the fragment part of the URL
     */
    public void ensureOneParticipant(String roomParameter, String fragment)
    {
        Participant participant
            = joinParticipant(0, roomParameter, fragment, null);

        participant.waitToJoinMUC(10);
    }

    /**
     * Starts owner, if not started. Uses the custom method to join the
     * participant, by skipping the Participant implementation.
     *
     * @param joinRef
     */
    public void ensureOneParticipant(BiConsumer<String, String> joinRef)
    {
        Participant participant
            = joinParticipant(0, null, null, joinRef);

        participant.waitToJoinMUC(10);
    }

    /**
     * Joins the first participant.
     * @return the participant which was created.
     */
    public Participant joinFirstParticipant()
    {
        return joinParticipant(0, null, null, null);
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
    private Participant joinParticipant(
        int index, String roomParameter, String fragment,
        BiConsumer<String, String> joinRef)
    {
        Participant participant;
        if (participants.size() <= index)
        {
            // we need to create this participant.
            participant
                = ParticipantFactory.getInstance()
                    .createParticipant("web.participant" + (++index));
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
     * Starts the owner and the seconds participant, if they are not started,
     * and stops the third participant, if it is not stopped.
     * participants(owner and 'second participant').
     */
    public void ensureTwoParticipants()
    {
        ensureTwoParticipants(null);
    }

    /**
     * Starts the owner and the seconds participant, if they are not started,
     * and stops the third participant, if it is not stopped.
     * participants(owner and 'second participant').
     * @param fragment adds the given string to the fragment part of the URL
     */
    public void ensureTwoParticipants(String fragment)
    {
        ensureTwoParticipantsInternal(fragment);

        hangUpParticipant(2);
    }

    /**
     * Starts the owner and the seconds participant, if they are not started.
     * participants(owner and 'second participant').
     * @param fragment adds the given string to the fragment part of the URL
     */
    private void ensureTwoParticipantsInternal(String fragment)
    {
        ensureOneParticipant();

        Participant participant = joinParticipant(1, null, fragment, null);

        participant.waitToJoinMUC(10);

        participant.waitForIceConnected();
        participant.waitForSendReceiveData();

        TestUtils.waitMillis(500);
    }

    /**
     * Starts the owner, second participant and third participant if they aren't
     * started.
     * @param fragment adds the given string to the fragment part of the URL
     */
    public void ensureThreeParticipants(String fragment)
    {
        ensureTwoParticipantsInternal(null);

        Participant participant = joinParticipant(2, null, fragment, null);

        participant.waitToJoinMUC(15);

        participant.waitForIceConnected();
        participant.waitForSendReceiveData();
        participant.waitForRemoteStreams(2);
    }

    /**
     * Starts the owner, second participant and third participant if they aren't
     * started.
     */
    public void ensureThreeParticipants()
    {
        ensureThreeParticipants(null);
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
    private void hangUpParticipant(int index)
    {
        Participant participant = getParticipant(index);
        if (participant != null)
            participant.hangUp();
    }

    /**
     * Starts the owner, if it isn't started and hangups all other participants.
     */
    public void hangUpAllParticipantsExceptTheOwner()
    {
        ensureOneParticipant();

        hangUpParticipant(1);
        hangUpParticipant(2);
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
    public List<Participant> getAllParticipants()
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
