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

import java.util.*;

/**
 * Base class for web tests.
 */
public class WebTestBase
    extends AbstractBaseTest
{
    /**
     * Default config for Web participants.
     */
    public static final String DEFAULT_CONFIG
        = "config.requireDisplayName=false"
        + "&config.debug=true"
        + "&config.disableAEC=true"
        + "&config.disableNS=true"
        + "&config.callStatsID=false"
        + "&config.alwaysVisibleToolbar=true"
        + "&config.p2p.enabled=false"
        + "&config.disable1On1Mode=true";

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

    @Override
    public ParticipantFactory getFactory(Properties config)
    {
        return new WebParticipantFactory(config);
    }

    /**
     * Starts the owner, if it is not started.
     */
    public void ensureOneParticipant()
    {
        ensureOneParticipant(null, null);
    }

    /**
     * Starts the owner, if it is not started.
     */
    public void ensureOneParticipant(JitsiMeetUrl meetURL)
    {
        ensureOneParticipant(meetURL, null);
    }

    /**
     * Starts the owner, if it is not started.
     * @param options
     */
    public void ensureOneParticipant(
        JitsiMeetUrl meetURL, ParticipantOptions options)
    {
        Participant participant = joinParticipant(0, meetURL, options);

        participant.waitToJoinMUC(10);
    }

    /**
     * Starts the owner and the seconds participant, if they are not started,
     * and stops the third participant, if it is not stopped.
     * participants(owner and 'second participant').
     */
    public void ensureTwoParticipants()
    {
        ensureTwoParticipants(null, null, null, null);
    }

    /**
     * Starts the owner and the seconds participant, if they are not started,
     * and stops the third participant, if it is not stopped.
     * participants(owner and 'second participant').
     */
    public void ensureTwoParticipants(
        JitsiMeetUrl participantOneMeetURL,
        JitsiMeetUrl participantTwoMeetURL)
    {
        ensureTwoParticipants(
            participantOneMeetURL, participantTwoMeetURL, null, null);
    }

    /**
     * Starts the owner and the seconds participant, if they are not started,
     * and stops the third participant, if it is not stopped.
     * participants(owner and 'second participant').
     * @param participantOneOptions
     * @param participantTwoOptions
     */
    public void ensureTwoParticipants(
        JitsiMeetUrl participantOneMeetURL,
        JitsiMeetUrl participantTwoMeetURL,
        ParticipantOptions participantOneOptions,
        ParticipantOptions participantTwoOptions)
    {
        ensureTwoParticipantsInternal(
            participantOneMeetURL,
            participantTwoMeetURL,
            participantOneOptions, participantTwoOptions);

        participants.hangUpByIndex(2);
    }

    /**
     * Starts the owner and the seconds participant, if they are not started.
     * participants(owner and 'second participant').
     * @param participantOneOptions
     * @param participantTwoOptions
     */
    private void ensureTwoParticipantsInternal(
        JitsiMeetUrl participantOneMeetURL,
        JitsiMeetUrl participantTwoMeetURL,
        ParticipantOptions participantOneOptions,
        ParticipantOptions participantTwoOptions)
    {
        ensureOneParticipant(participantOneMeetURL, participantOneOptions);

        Participant participant
            = joinParticipant(1, participantTwoMeetURL, participantTwoOptions);

        participant.waitToJoinMUC(10);

        participant.waitForIceConnected();
        participant.waitForSendReceiveData();

        TestUtils.waitMillis(500);
    }

    /**
     * Starts the owner, second participant and third participant if they aren't
     * started.
     */
    public void ensureThreeParticipants(
        JitsiMeetUrl participantOneMeetURL,
        JitsiMeetUrl participantTwoMeetURL,
        JitsiMeetUrl participantThreeMeetURL)
    {
        ensureTwoParticipantsInternal(
            participantOneMeetURL,
            participantTwoMeetURL,
            null, null);

        Participant participant
            = joinParticipant(
                2, participantThreeMeetURL, null);

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
        ensureThreeParticipants(null, null, null);
    }

    /**
     * Returns the first participant.
     * @return the first participant.
     */
    public Participant getParticipant1()
    {
        return participants.get(0);
    }

    /**
     * Returns the second participant.
     * @return the second participant.
     */
    public Participant getParticipant2()
    {
        return participants.get(1);
    }

    /**
     * Returns the third participant.
     * @return the third participant.
     */
    public Participant getParticipant3()
    {
        return participants.get(2);
    }

    /**
     * Hangups all participants.
     */
    public void hangUpAllParticipants()
    {
        participants.hangUpAll();
    }

    /**
     * Starts the owner, if it isn't started and hangups all other participants.
     */
    public void hangUpAllParticipantsExceptTheOwner()
    {
        ensureOneParticipant();

        participants.hangUpByIndex(1);
        participants.hangUpByIndex(2);
    }

    /**
     * Joins a participant, created if does not exists.
     *
     * @param index the participant index.
     * @param options
     * @return the participant which was created
     */
    private Participant joinParticipant(
        int                     index,
        JitsiMeetUrl            meetURL,
        ParticipantOptions      options)
    {
        Participant p = participants.get(index);

        if (p == null)
        {
            // There's an assumption that the participants are created
            // starting from 0, 1, 2, so throw an Exception if they happen to be
            // created in different order.
            int size = participants.getAll().size();
            if (index != size)
            {
                throw new IllegalArgumentException(
                    String.format(
                        "New participant would have been inserted at different "
                            + "index than expected. Index: %d, size %d.",
                        index,
                        size));
            }

            String configPrefix = "web.participant" + (index + 1);

            p = participants.createParticipant(configPrefix, options);

            // Adds a print in the console/selenium-node logs
            // useful when checking crashes or failures in node logs
            p.executeScript(
                    "console.log('--- Will start test:"
                        + getClass().getSimpleName() + "')");
        }

        if (meetURL == null)
        {
            meetURL = getJitsiMeetUrl();
            meetURL.setRoomName(currentRoomName);
        }

        p.joinConference(meetURL);

        return p;
    }

    /**
     * Joins the first participant.
     * @return the participant which was created.
     */
    public Participant joinFirstParticipant()
    {
        return joinParticipant(0, null, null);
    }

    /**
     * Return new {@link JitsiMeetUrl} instance which has only
     * {@link JitsiMeetUrl#serverUrl} field initialized with the value from
     * {@link ParticipantFactory#JITSI_MEET_URL_PROP} system property.
     *
     * @return a new instance of {@link JitsiMeetUrl}.
     */
    @Override
    public JitsiMeetUrl getJitsiMeetUrl()
    {
        JitsiMeetUrl url = super.getJitsiMeetUrl();

        url.appendConfig(DEFAULT_CONFIG);

        return url;
    }
}
