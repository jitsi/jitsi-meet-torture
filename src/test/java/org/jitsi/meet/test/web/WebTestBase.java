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
package org.jitsi.meet.test.web;

import org.jitsi.meet.test.base.*;
import org.jitsi.meet.test.util.*;

import java.util.function.*;

/**
 * Base class for web tests.
 */
public class WebTestBase extends AbstractBaseTest
{
    /**
     * Default
     */
    public WebTestBase()
    { }

    /**
     * Constructs new AbstractBaseTest with predefined baseTest, to
     * get its participants and room name.
     * @param baseTest the parent test.
     */
    public WebTestBase(AbstractBaseTest baseTest)
    {
        super(baseTest);
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
     * Starts the owner, if it isn't started and hangups all other participants.
     */
    public void hangUpAllParticipantsExceptTheOwner()
    {
        ensureOneParticipant();

        hangUpParticipant(1);
        hangUpParticipant(2);
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
        int                        index,
        String                     roomParameter,
        String                     fragment,
        BiConsumer<String, String> joinRef)
    {
        String configPrefix = "web.participant" + (index + 1);

        return joinParticipant(
            index, configPrefix, roomParameter, fragment, joinRef);
    }

    /**
     * Joins the first participant.
     * @return the participant which was created.
     */
    public Participant joinFirstParticipant()
    {
        return joinParticipant(0, null, null, null);
    }
}
