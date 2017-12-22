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
    private String currentRoomName;

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
     * Starts the owner and the seconds participant, if they are not started,
     * and stops the third participant, if it is not stopped.
     * participants(owner and 'second participant').
     */
    public void ensureTwoParticipants()
    {
        waitForParticipant1ToJoinMUC();
        waitForSecondParticipantToConnect();

        // TODO do we need this???
        //closeThirdParticipant();
    }

    /**
     * Starts the owner, second participant and third participant if they aren't
     * started.
     */
    public void ensureThreeParticipants()
    {
        waitForParticipant1ToJoinMUC();
        waitForSecondParticipantToConnect();
        waitForThirdParticipantToConnect();
    }


    /**
     * Waits until the owner joins the room, creating and starting the owner
     * if it hasn't been started.
     */
    private void waitForParticipant1ToJoinMUC()
    {
        if (participant1 == null)
        {
            participant1
                = ParticipantFactory.getInstance()
                    .createParticipant("web.participant1");
        }

        if (participant1.isHungUp())
        {
            // join room
            participant1.joinConference(currentRoomName);
        }

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

        participant.quit();
    }

    /**
     * Waits until {@code secondParticipant} has joined the conference (its ICE
     * connection has completed and has it has sent and received data).
     */
    public void waitForSecondParticipantToConnect()
    {
        if (participant2 == null)
        {
            participant2 = ParticipantFactory.getInstance()
                .createParticipant("web.participant2");
        }

        if (participant2.isHungUp())
        {
            participant2.joinConference(currentRoomName);
        }

        MeetUtils.waitForParticipantToJoinMUC(participant2.getDriver(), 10);
        MeetUtils.waitForIceConnected(participant2.getDriver());
        MeetUtils.waitForSendReceiveData(participant2.getDriver());

        TestUtils.waitMillis(5000);
    }

    /**
     * Starts the owner, if it isn't started and hangups all other participants.
     */
    public void closeAllParticipantsExceptTheOwner()
    {
        waitForParticipant1ToJoinMUC();

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
        if (participant3 == null)
        {
            participant3 = ParticipantFactory.getInstance()
                .createParticipant("web.participant3");
        }

        if (participant3.isHungUp())
        {
            participant3.joinConference(currentRoomName);
        }

        MeetUtils.waitForParticipantToJoinMUC(participant3.getDriver(), 10);
        MeetUtils.waitForIceConnected(participant3.getDriver());
        MeetUtils.waitForSendReceiveData(participant3.getDriver());
        MeetUtils.waitForRemoteStreams(participant3.getDriver(), 2);

        // TODO do we need this????
        TestUtils.waitMillis(1000);
    }
}
