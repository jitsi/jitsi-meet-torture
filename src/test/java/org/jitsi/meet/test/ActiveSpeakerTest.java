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

import org.jitsi.meet.test.web.*;

import org.testng.*;
import org.testng.annotations.*;

import static org.jitsi.meet.test.util.TestUtils.*;

/**
 * The tests for active speaker detection feature.
 *
 * @author Pawel Domas
 */
public class ActiveSpeakerTest
    extends WebTestBase
{
    @Override
    public void setupClass()
    {
        super.setupClass();

        // This test requires a conference of three
        ensureThreeParticipants();
    }

    /**
     * Active speaker test scenario.
     */
    @Test
    public void testActiveSpeaker()
    {
        WebParticipant participant1 = getParticipant1();
        WebParticipant participant2 = getParticipant2();
        WebParticipant participant3 = getParticipant3();

        // skip if we are not chrome
        if (!participant1.getType().isChrome())
        {
            throw new SkipException("skip as it is not chrome");
        }

        // Mute all
        muteAllParticipants();

        // participant1 becomes active speaker - check from participant2's
        // perspective
        testActiveSpeaker(participant1, participant2, participant3);
        // participant3 becomes active speaker - check from participant2's
        // perspective
        testActiveSpeaker(participant3, participant2, participant1);
        // participant2 becomes active speaker - check from participant1's
        // perspective
        testActiveSpeaker(participant2, participant1, participant3);

        // check the displayed speakers, there should be only one speaker
        participant1.getFilmstrip().assertOneDominantSpeaker();
        participant2.getFilmstrip().assertOneDominantSpeaker();
        participant3.getFilmstrip().assertOneDominantSpeaker();
    }

    private void muteAllParticipants()
    {
        MuteTest muteTest = new MuteTest(this);
        muteTest.muteParticipant1AndCheck();
        muteTest.muteParticipant2AndCheck();
        muteTest.muteParticipant3AndCheck();
    }

    /**
     * Tries to make given participant an active speaker by un-muting it.
     * Verifies from {@code participant2}'s perspective that the active speaker
     * has been displayed on the large video area. Mutes him back.
     *
     * @param activeSpeaker <tt>Participant</tt> instance of the
     * participant who will be testes as an active speaker.
     * @param participant2 <tt>Participant</tt> of the participant who will
     * be observing and verifying active speaker change.
     * @param participant3 used only to print some debugging info
     */
    private void testActiveSpeaker(
        WebParticipant activeSpeaker,
        WebParticipant participant2,
        WebParticipant participant3)
    {
        // we cannot use firefox as active speaker as it uses constant beep
        // audio which is not detected as speech
        if (!activeSpeaker.getType().isChrome())
        {
            throw new SkipException("skip as it is not chrome");
        }

        print("Start testActiveSpeaker for participant: "
            + activeSpeaker.getName());

        final String speakerEndpoint = activeSpeaker.getEndpointId();

        // just a debug print to go in logs
        activeSpeaker.executeScript(
                "console.log('Unmuting in testActiveSpeaker');");
        // Unmute
        activeSpeaker.getToolbar().clickAudioMuteButton();

        // just a debug print to go in logs
        participant2.executeScript(
                "console.log('Participant unmuted in testActiveSpeaker "
                    + speakerEndpoint + "');");
        // just a debug print to go in logs
        participant3.executeScript(
                "console.log('Participant unmuted in testActiveSpeaker "
                    + speakerEndpoint + "');");
        getParticipant2().getFilmstrip()
            .assertAudioMuteIcon(activeSpeaker, false);

        // Verify that the user is now an active speaker from participant2's
        // perspective
        participant2.getLargeVideo()
            .waitForParticipantToDisplay(speakerEndpoint);

        // just a debug print to go in logs
        activeSpeaker.executeScript(
                "console.log('Muting in testActiveSpeaker');");
        // Mute back again
        activeSpeaker.getToolbar().clickAudioMuteButton();
        // just a debug print to go in logs
        participant2.executeScript(
                "console.log('Participant muted in testActiveSpeaker "
                    + speakerEndpoint + "');");
        // just a debug print to go in logs
        participant3.executeScript(
                "console.log('Participant muted in testActiveSpeaker "
                    + speakerEndpoint + "');");
        participant2.getFilmstrip().assertAudioMuteIcon(activeSpeaker, true);
    }
}
