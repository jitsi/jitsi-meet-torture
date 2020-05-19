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
import org.openqa.selenium.*;

import java.util.*;
import java.util.logging.*;

/**
 * Base class for web tests.
 */
public class WebTestBase
    extends TypedBaseTest<WebParticipant, WebParticipantFactory>
{
    /**
     * Default
     */
    public WebTestBase()
    {
        super(WebParticipantFactory.class);
    }

    /**
     * Constructs new AbstractBaseTest with predefined baseTest, to
     * get its participants and room name.
     * @param baseTest the parent test.
     *
     * @deprecated see
     * {@link AbstractBaseTest#AbstractBaseTest(AbstractBaseTest)}
     */
    public WebTestBase(AbstractBaseTest<WebParticipant> baseTest)
    {
        super(baseTest, WebParticipantFactory.class);
    }

    /**
     * Starts participant1, if it is not started.
     */
    public void ensureOneParticipant()
    {
        ensureOneParticipant(null, null);
    }

    /**
     * Starts participant1, if it is not started.
     * @param meetURL a {@link JitsiMeetUrl} which represents the full
     * conference URL which includes server, conference parameters and
     * the config part. For example:
     * "https://server.com/conference1?login=true#config.debug=true"
     */
    public void ensureOneParticipant(JitsiMeetUrl meetURL)
    {
        ensureOneParticipant(meetURL, null);
    }

    /**
     * Starts participant1, if it is not started.
     * @param meetURL a {@link JitsiMeetUrl} which represents the full
     * conference URL which includes server, conference parameters and
     * the config part. For example:
     * "https://server.com/conference1?login=true#config.debug=true"
     * @param options custom options to be used for the new participant.
     */
    public void ensureOneParticipant(
        JitsiMeetUrl meetURL, ParticipantOptions options)
    {
        joinParticipantAndWait(0, meetURL, options);
    }

    /**
     * Starts participant1 and participant2, if they are not started, and stops
     * participant3, if it is not stopped.
     */
    public void ensureTwoParticipants()
    {
        ensureTwoParticipants(null, null, null, null);
    }

    /**
     * Starts participant1 and participant2, if they are not started,
     * and stops participant3, if it is not stopped.
     * @param participantOneMeetURL the url for the first participant.
     * @param participantTwoMeetURL the url for the second participant.
     */
    public void ensureTwoParticipants(
        JitsiMeetUrl participantOneMeetURL,
        JitsiMeetUrl participantTwoMeetURL)
    {
        ensureTwoParticipants(
            participantOneMeetURL, participantTwoMeetURL, null, null);
    }

    /**
     * Starts participant1 and participant2, if they are not started,
     * and stops participant3, if it is not stopped.
     * @param participantOneMeetURL the url for the first participant,
     * if it does'n exist already.
     * @param participantTwoMeetURL the url for the second participant,
     * if it does'n exist already.
     * @param participantOneOptions custom options to be used
     * for the first participant, if it does'n exist already.
     * @param participantTwoOptions custom options to be used
     * for the second participant, if it does'n exist already.
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
     * Starts participant1 and participant2, if they are not started.
     * @param participantOneMeetURL the url for the first participant,
     * if it does'n exist already.
     * @param participantTwoMeetURL the url for the second participant,
     * if it does'n exist already.
     * @param participantOneOptions custom options to be used
     * for the first participant, if it does'n exist already.
     * @param participantTwoOptions custom options to be used
     * for the second participant, if it does'n exist already.
     */
    private void ensureTwoParticipantsInternal(
        JitsiMeetUrl participantOneMeetURL,
        JitsiMeetUrl participantTwoMeetURL,
        ParticipantOptions participantOneOptions,
        ParticipantOptions participantTwoOptions)
    {
        ensureOneParticipant(participantOneMeetURL, participantOneOptions);

        Participant participant
            = joinParticipantAndWait(
                1, participantTwoMeetURL, participantTwoOptions);

        participant.waitForIceConnected();
        participant.waitForSendReceiveData();

        // FIXME missing a comment on why is it needed here (in case someone
        // would want to come up with a proper fix).
        TestUtils.waitMillis(500);
    }

    /**
     * Starts participant1, participant2 and participant3 if they aren't
     * started.
     * @param participantOneMeetURL the url for the first participant,
     * if it does'n exist already.
     * @param participantTwoMeetURL the url for the second participant,
     * if it does'n exist already.
     * @param participantThreeMeetURL the url for the third participant,
     * if it does'n exist already.
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

        WebParticipant participant
            = joinParticipantAndWait(2, participantThreeMeetURL, null);

        participant.waitForIceConnected();
        participant.waitForSendReceiveData();
        participant.waitForRemoteStreams(2);
    }

    /**
     * Starts participant1, participant2 and participant3 if they aren't
     * started.
     */
    public void ensureThreeParticipants()
    {
        ensureThreeParticipants(null, null, null);
    }

    /**
     * @return the {@code id}-th participant as a {@link WebParticipant}.
     * @param id 1-based index of the participant.
     */
    public WebParticipant getParticipant(int id)
    {
        return participants.get(id - 1);
    }

    /**
     * Returns the first participant.
     * @return the first participant.
     */
    public WebParticipant getParticipant1()
    {
        return getParticipant(1);
    }

    /**
     * Returns the second participant.
     * @return the second participant.
     */
    public WebParticipant getParticipant2()
    {
        return getParticipant(2);
    }

    /**
     * Returns the third participant.
     * @return the third participant.
     */
    public WebParticipant getParticipant3()
    {
        return getParticipant(3);
    }

    /**
     * Hangups all participants.
     */
    public void hangUpAllParticipants()
    {
        participants.hangUpAll();
    }

    /**
     * Starts participant1, if it isn't started and closes all other
     * participants.
     */
    protected void hangUpAllExceptParticipant1()
    {
        ensureOneParticipant();

        participants.hangUpByIndex(1);
        participants.hangUpByIndex(2);
    }

    /**
     * Joins a participant, created if does not exists.
     *
     * @param index the participant index.
     * @param meetURL a {@link JitsiMeetUrl} which represents the full
     * conference URL which includes server, conference parameters and
     * the config part. For example:
     * "https://server.com/conference1?login=true#config.debug=true"
     * @param options the options to be used when creating the participant.
     * @return the participant which was created
     */
    private WebParticipant joinParticipant(
        int                     index,
        JitsiMeetUrl            meetURL,
        ParticipantOptions      options)
    {
        WebParticipant p = participants.get(index);

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
        }

        p.joinConference(meetURL);

        return p;
    }

    /**
     * Joins the first participant.
     * @return the participant which was created.
     */
    public WebParticipant joinFirstParticipant()
    {
        return joinParticipant(0, null, null);
    }

    /**
     * Joins the second participant to a specified {@link JitsiMeetUrl}.
     *
     * @param meetUrl a {@link JitsiMeetUrl} which represents the full
     * conference URL which includes server, conference parameters and
     * the config part.
     * @return the participant which was created.
     */
    public WebParticipant joinSecondParticipant(JitsiMeetUrl meetUrl)
    {
        WebParticipant participant = joinParticipant(1, meetUrl, null);

        participant.waitToJoinMUC(10);
        participant.waitForIceConnected();

        return participant;
    }

    /**
     * Joins the second participant.
     * @return the participant which was created.
     */
    public WebParticipant joinSecondParticipant()
    {
        return joinSecondParticipant(null);
    }

    /**
     * Joins the third participant.
     * @return the participant which was created.
     */
    public WebParticipant joinThirdParticipant()
    {
        return joinParticipantAndWait(2, null, null);
    }

    /**
     * Joins a participant, and waits for it to join the room.
     *
     * @param index the participant index.
     * @param meetURL a {@link JitsiMeetUrl} which represents the full
     * conference URL which includes server, conference parameters and
     * the config part. For example:
     * "https://server.com/conference1?login=true#config.debug=true"
     * @param options the options to be used when creating the participant.
     * @return the participant which was created
     */
    private WebParticipant joinParticipantAndWait(
        int                     index,
        JitsiMeetUrl            meetURL,
        ParticipantOptions      options)
    {
        WebParticipant participant = joinParticipant(index, meetURL, options);

        // FIXME: remove this when chrome changes and we stop seeing the warning
        // in the tests logs
        try
        {
            participant.waitToJoinMUC(10);
        }
        catch (TimeoutException ex)
        {
            // workaround is only for chrome
            if (!participant.getType().isChrome())
            {
                throw ex;
            }

            Logger.getGlobal().log(
                Level.WARNING,
                "Participant did not join, retrying: " + participant.getName());

            // Participant did not join, let's give it another try.
            // This workarounds a problem where we see chrome waiting on media
            // permissions screen
            // we close the driver in, case of browser stuck on grid, to move
            // to new node
            closeParticipant(participant);

            participant = joinParticipant(index, meetURL, options);
            participant.waitToJoinMUC();
        }

        return participant;
    }
}
