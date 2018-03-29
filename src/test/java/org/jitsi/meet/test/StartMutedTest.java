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

import org.jitsi.meet.test.util.*;
import org.jitsi.meet.test.web.*;

import org.openqa.selenium.*;
import org.testng.annotations.*;

/**
 * Start muted tests
 * @author Hristo Terezov
 * @author Pawel Domas
 */
public class StartMutedTest
    extends WebTestBase
{
    @Override
    public void setupClass()
    {
        super.setupClass();

        ensureOneParticipant();
    }

    /**
     * Restarts the second participant tab and checks start muted checkboxes.
     * Test if the second participant is muted and participant1 is unmuted.
     */
    @Test
    public void checkboxesTest()
    {
        WebDriver driver1 = getParticipant1().getDriver();

        // Make sure settings panel is displayed
        MeetUIUtils.displaySettingsPanel(getParticipant1());
        // Wait for 'start muted' checkboxes
        TestUtils.waitForDisplayedElementByXPath(
            driver1, "//input[@id='startAudioMuted']", 5);
        TestUtils.waitForDisplayedElementByXPath(
                driver1, "//input[@id='startVideoMuted']", 5);

        driver1.findElement(By.id("startAudioMuted")).click();
        driver1.findElement(By.id("startVideoMuted")).click();

        WebParticipant participant2 = joinSecondParticipant();

        // if the participant2 is audio only and audio muted there will be no
        // data sent
        participant2.waitForSendReceiveData(
            !participant2.isAudioOnlyParticipant(),
            true);

        // On the PR testing machine it seems that some audio is leaking before
        // we mute. The audio is muted when 'session-initiate' is received, but
        // seems like a bit of sound goes through in random cases. Let's wait
        // here a bit, before checking the audio levels.
        TestUtils.waitMillis(500);
        checkParticipant2ForMute();
    }

    /**
     * Opens new room and sets start muted config parameters trough the URL.
     * Test if the second participant is muted and participant1 is unmuted.
     */
    @Test(dependsOnMethods = { "checkboxesTest" })
    public void configOptionsTest()
    {
        hangUpAllParticipants();

        ensureOneParticipant(getJitsiMeetUrl().appendConfig(
            "config.startAudioMuted=1&" +
                "config.debugAudioLevels=true&" +
                "config.startVideoMuted=1"));

        WebParticipant participant2 = joinSecondParticipant();

        // if the participant is audio only, if audio is muted we will not
        // receive any data, so skip upload check
        participant2.waitForSendReceiveData(
            !participant2.isAudioOnlyParticipant(), true);

        WebParticipant participant1 = getParticipant1();

        final WebDriver driver2 = participant2.getDriver();
        participant1.executeScript(
            "console.log('Start configOptionsTest, second participant: "
                + participant2.getEndpointId() + "');");

        participant1.waitForIceConnected();

        // On the PR testing machine it seems that some audio is leaking before
        // we mute. The audio is muted when 'session-initiate' is received, but
        // seems like a bit of sound goes through in random cases. Let's wait
        // here a bit, before checking the audio levels.
        TestUtils.waitMillis(500);

        checkParticipant2ForMute();

        // Unmute and see if the audio works
        participant2.getToolbar().clickAudioMuteButton();
        participant1.executeScript(
            "console.log('configOptionsTest, unmuted second participant');");
        MeetUIUtils.waitForAudioMuted(
            participant1.getDriver(),
            driver2,
            "participant2",
            false /* unmuted */);
    }

    /**
     * Tests if the second participant is muted and the first participant is
     * unmuted.
     */
    private void checkParticipant2ForMute()
    {
        WebParticipant participant1 = getParticipant1();
        WebParticipant participant2 = getParticipant2();

        getParticipant2().getFilmstrip()
            .assertAudioMuteIcon(getParticipant2(), true);
        getParticipant2().getFilmstrip()
            .assertVideoMuteIcon(getParticipant2(), true);

        MeetUIUtils.waitForAudioMuted(
            participant1.getDriver(),
            participant2.getDriver(),
            "participant2",
            true);

        participant2.getFilmstrip().assertAudioMuteIcon(participant1, false);
        participant2.getFilmstrip().assertVideoMuteIcon(participant1, false);
    }
}
