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
package org.jitsi.meet.test;

import junit.framework.*;

/**
 * This test will setup the conference and will end when both
 * participants are connected.
 * We order tests alphabetically and use the stage1,2,3... to order them.
 *
 * @author Damian Minkov
 */
public class SetupConference
    extends TestCase
{
    /**
     * Constructs test.
     * @param name method name.
     */
    public SetupConference(String name)
    {
        super(name);
    }

    /**
     * Orders tests.
     * @return the suite with order tests.
     */
    public static junit.framework.Test suite()
    {
        TestSuite suite = new TestSuite();

        suite.addTest(new SetupConference("startOwner"));
        suite.addTest(new SetupConference("checkOwnerJoinRoom"));
        suite.addTest(new SetupConference("startSecondParticipant"));
        suite.addTest(new SetupConference("checkSecondParticipantJoinRoom"));
        suite.addTest(new SetupConference("waitsOwnerToJoinConference"));
        suite.addTest(new SetupConference("waitsSecondParticipantToJoinConference"));
        suite.addTest(new SetupConference("waitForOwnerSendReceiveData"));
        suite.addTest(new SetupConference("waitForSecondParticipantSendReceiveData"));

        return suite;
    }

    /**
     * First starts the owner.
     */
    public void startOwner()
    {
        ConferenceFixture.startOwner(null);
    }

    /**
     * Checks whether owner joined the room.
     */
    public void checkOwnerJoinRoom()
    {
        // first lets wait 10 secs to join
        ConferenceFixture.checkParticipantToJoinRoom(
            ConferenceFixture.getOwner(), 10);
    }

    /**
     * Starts the second participant.
     */
    public void startSecondParticipant()
    {
        ConferenceFixture.startParticipant();
    }

    /**
     * Checks whether the second participant has joined the room.
     */
    public void checkSecondParticipantJoinRoom()
    {
        ConferenceFixture.checkParticipantToJoinRoom(
            ConferenceFixture.getSecondParticipant(), 10);
    }

    /**
     * Waits the owner to get event for iceConnectionState that changes
     * to connected.
     */
    public void waitsOwnerToJoinConference()
    {
        ConferenceFixture.waitsParticipantToJoinConference(
            ConferenceFixture.getOwner());
    }

    /**
     * Waits the participant to get event for iceConnectionState that changes
     * to connected.
     */
    public void waitsSecondParticipantToJoinConference()
    {
        ConferenceFixture.waitsParticipantToJoinConference(
            ConferenceFixture.getSecondParticipant());
    }

    /**
     * Checks statistics for received and sent bitrate.
     */
    public void waitForOwnerSendReceiveData()
    {
        ConferenceFixture.waitForSendReceiveData(
            ConferenceFixture.getOwner());
    }

    /**
     * Checks statistics for received and sent bitrate.
     */
    public void waitForSecondParticipantSendReceiveData()
    {
        ConferenceFixture.waitForSendReceiveData(
            ConferenceFixture.getSecondParticipant());
    }
}
