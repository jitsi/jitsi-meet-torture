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

import org.jitsi.meet.test.base.*;
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
     * Test if the second participant is muted and the owner is unmuted.
     */
    @Test
    public void checkboxesTest()
    {
        WebDriver owner = getParticipant1().getDriver();

        // Make sure settings panel is displayed
        MeetUIUtils.displaySettingsPanel(owner);
        // Wait for 'start muted' checkboxes
        TestUtils.waitForDisplayedElementByXPath(
            owner, "//input[@id='startAudioMuted']", 5);
        TestUtils.waitForDisplayedElementByXPath(
                owner, "//input[@id='startVideoMuted']", 5);

        owner.findElement(By.id("startAudioMuted")).click();
        owner.findElement(By.id("startVideoMuted")).click();

        ensureTwoParticipants();

        // On the PR testing machine it seems that some audio is leaking before
        // we mute. The audio is muted when 'session-initiate' is received, but
        // seems like a bit of sound goes through in random cases. Let's wait
        // here a bit, before checking the audio levels.
        TestUtils.waitMillis(500);
        checkSecondParticipantForMute();
    }

    /**
     * Opens new room and sets start muted config parameters trough the URL.
     * Test if the second participant is muted and the owner is unmuted.
     */
    @Test(dependsOnMethods = { "checkboxesTest" })
    public void configOptionsTest()
    {
        hangUpAllParticipants();

        ensureOneParticipant("config.startAudioMuted=1&" +
            "config.debugAudioLevels=true&" +
            "config.startVideoMuted=1");
        Participant owner = getParticipant1();

        ensureTwoParticipants();
        final WebDriver secondParticipantDriver = getParticipant2().getDriver();
        owner.executeScript(
            "console.log('Start configOptionsTest, second participant: "
                + MeetUtils.getResourceJid(secondParticipantDriver) + "');");

        owner.waitForIceConnected();

        // On the PR testing machine it seems that some audio is leaking before
        // we mute. The audio is muted when 'session-initiate' is received, but
        // seems like a bit of sound goes through in random cases. Let's wait
        // here a bit, before checking the audio levels.
        TestUtils.waitMillis(500);

        checkSecondParticipantForMute();

        // Unmute and see if the audio works
        MeetUIUtils.clickOnToolbarButton(
            secondParticipantDriver, "toolbar_button_mute");
        owner.executeScript(
            "console.log('configOptionsTest, unmuted second participant');");
        MeetUIUtils.waitForAudioMuted(
            owner.getDriver(),
            secondParticipantDriver,
            "second peer",
            false /* unmuted */);
    }

    /**
     * Tests if the second participant is muted and the first participant is
     * unmuted.
     */
    private void checkSecondParticipantForMute()
    {
        WebDriver secondParticipant = getParticipant2().getDriver();
        WebDriver owner = getParticipant1().getDriver();

        final String ownerResourceJid = MeetUtils.getResourceJid(owner);

        TestUtils.waitForElementByXPath(
            secondParticipant,
            "//span[@id='localVideoContainer']"
                + TestUtils.getXPathStringForClassName("//span", "audioMuted")
                + "/i[@class='icon-mic-disabled']", 25);

        TestUtils.waitForElementByXPath(
            secondParticipant,
            "//span[@id='localVideoContainer']"
                + TestUtils.getXPathStringForClassName("//span", "videoMuted")
                + "/i[@class='icon-camera-disabled']", 25);

        MeetUIUtils.waitForAudioMuted(
            owner,
            secondParticipant,
            "secondParticipant",
            true);

        TestUtils.waitForElementNotPresentByXPath(
            secondParticipant,
            "//span[@id='participant_" + ownerResourceJid + "']"
                + TestUtils.getXPathStringForClassName("//span", "audioMuted")
                + "/i[@class='icon-mic-disabled']", 25);

        TestUtils.waitForElementNotPresentByXPath(
            secondParticipant,
            "//span[@id='participant_" + ownerResourceJid + "']"
                + TestUtils.getXPathStringForClassName("//span", "videoMuted")
                + "/i[@class='icon-camera-disabled']",
            25);
    }
}
