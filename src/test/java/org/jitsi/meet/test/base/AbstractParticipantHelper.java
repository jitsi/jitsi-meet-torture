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

public abstract class AbstractParticipantHelper
{
    /**
     * The current room name used.
     */
    protected String currentRoomName;

    /**
     * The conference first participant(owner) in the tests.
     */
    protected Participant<WebDriver> participant1;

    /**
     * The second participant.
     */
    protected Participant<WebDriver> participant2;

    /**
     * The third participant.
     */
    protected Participant<WebDriver> participant3;

    /**
     * Starts the owner, if it is not started.
     */
    public void ensureOneParticipant()
    {
        ensureOneParticipant(null);
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
        waitForParticipant1ToJoinMUC(roomParameter, fragment);
    }

    /**
     * Starts the owner and the seconds participant, if they are not started,
     * and stops the third participant, if it is not stopped.
     * participants(owner and 'second participant').
     */
    public void ensureTwoParticipants()
    {
        waitForParticipant1ToJoinMUC(null, null);
        waitForSecondParticipantToConnect(null);

        if (participant3 != null && !participant3.isHungUp())
        {
            participant3.hangUp();
        }
    }

    /**
     * Starts the owner, second participant and third participant if they aren't
     * started.
     */
    public void ensureThreeParticipants()
    {
        waitForParticipant1ToJoinMUC(null, null);
        waitForSecondParticipantToConnect(null);
        waitForThirdParticipantToConnect();
    }

    /**
     * Creates the first participant (owner) if not already created and
     * join him in the room.
     * @param roomParameter an extra parameter to the url. The fragment adds
     * parameters as # where roomParameter actually changes query parameters
     * adding ?something=value.
     * @param fragment adds the given string to the fragment part of the URL
     */
    public void startParticipant1(String roomParameter, String fragment)
    {
        if (participant1 == null)
        {
            participant1
                = ParticipantFactory.getInstance()
                    .createParticipant("web.participant1");
        }

        if (participant1.isHungUp())
        {
            String roomName = currentRoomName;

            // we do not persist room params for now, in case of jwt
            // we want them just for one of the participants
            if (roomParameter != null)
                roomName += roomParameter;

            // join room
            participant1.joinConference(roomName, fragment);
        }
    }

    /**
     * Waits until the owner joins the room, creating and starting the owner
     * if it hasn't been started.
     * @param roomParameter an extra parameter to the url. The fragment adds
     * parameters as # where roomParameter actually changes query parameters
     * adding ?something=value.
     * @param fragment adds the given string to the fragment part of the URL
     */
    private void waitForParticipant1ToJoinMUC(
        String roomParameter, String fragment)
    {
        startParticipant1(roomParameter, fragment);

        MeetUtils.waitForParticipantToJoinMUC(participant1.getDriver(), 15);
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
        quitParticipant(participant1);
        quitParticipant(participant2);
        quitParticipant(participant3);
    }

    /**
     * Quits a participant if initialized.
     * @param participant the participant to quit.
     */
    private void quitParticipant(Participant<WebDriver> participant)
    {
        if (participant == null)
            return;

        TestUtils.print("Quiting " + participant.getName());
        participant.quit();
    }

    /**
     * Waits until {@code secondParticipant} has joined the conference (its ICE
     * connection has completed and has it has sent and received data).
     * @param fragment adds the given string to the fragment part of the URL
     */
    public void waitForSecondParticipantToConnect(String fragment)
    {
        waitForSecondParticipantToJoin(fragment);
        MeetUtils.waitForIceConnected(participant2.getDriver());
        MeetUtils.waitForSendReceiveData(participant2.getDriver());

        TestUtils.waitMillis(5000);
    }

    /**
     * Waits until {@code secondParticipant} has joined the conference.
     * @param fragment adds the given string to the fragment part of the URL
     */
    public void waitForSecondParticipantToJoin(String fragment)
    {
        if (participant2 == null)
        {
            participant2
                = ParticipantFactory.getInstance()
                    .createParticipant("web.participant2");
        }

        if (participant2.isHungUp())
        {
            participant2.joinConference(currentRoomName, fragment);
        }

        MeetUtils.waitForParticipantToJoinMUC(participant2.getDriver(), 10);
    }

    /**
     * Starts the owner, if it isn't started and hangups all other participants.
     */
    public void hangUpAllParticipantsExceptTheOwner()
    {
        waitForParticipant1ToJoinMUC(null, null);

        if (participant2 != null && !participant2.isHungUp())
        {
            participant2.hangUp();
        }

        if (participant3 != null && !participant3.isHungUp())
        {
            participant3.hangUp();
        }
    }

    /**
     * Hangups all participants.
     */
    public void hangUpAllParticipants()
    {
        if (participant1 != null && !participant1.isHungUp())
        {
            participant1.hangUp();
        }

        if (participant2 != null && !participant2.isHungUp())
        {
            participant2.hangUp();
        }

        if (participant3 != null && !participant3.isHungUp())
        {
            participant3.hangUp();
        }
    }

    /**
     * Waits until {@code thirdParticipant} has joined the conference (its ICE
     * connection has completed and has it has sent and received data).
     */
    public void waitForThirdParticipantToConnect()
    {
        waitForThirdParticipantToConnect(null);
    }

    /**
     * Waits until {@code thirdParticipant} has joined the conference (its ICE
     * connection has completed and has it has sent and received data).
     * @param fragment adds the given string to the fragment part of the URL
     */
    public void waitForThirdParticipantToConnect(String fragment)
    {
        if (participant3 == null)
        {
            participant3
                = ParticipantFactory.getInstance()
                    .createParticipant("web.participant3");
        }

        if (participant3.isHungUp())
        {
            participant3.joinConference(currentRoomName, fragment);
        }

        MeetUtils.waitForParticipantToJoinMUC(participant3.getDriver(), 10);
        MeetUtils.waitForIceConnected(participant3.getDriver());
        MeetUtils.waitForSendReceiveData(participant3.getDriver());
        MeetUtils.waitForRemoteStreams(participant3.getDriver(), 2);

        // TODO do we need this????
        TestUtils.waitMillis(1000);
    }
}
