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

        ensureTwoParticipants();

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

        ensureTwoParticipants(
            getJitsiMeetUrl().appendConfig(
                "config.startAudioMuted=1&" +
                "config.debugAudioLevels=true&" +
                "config.startVideoMuted=1"),
            null);

        WebParticipant participant1 = getParticipant1();

        final WebDriver driver2 = getParticipant2().getDriver();
        participant1.executeScript(
            "console.log('Start configOptionsTest, second participant: "
                + getParticipant2().getEndpointId() + "');");

        participant1.waitForIceConnected();

        // On the PR testing machine it seems that some audio is leaking before
        // we mute. The audio is muted when 'session-initiate' is received, but
        // seems like a bit of sound goes through in random cases. Let's wait
        // here a bit, before checking the audio levels.
        TestUtils.waitMillis(500);

        checkParticipant2ForMute();

        // Unmute and see if the audio works
        getParticipant2().getToolbar().clickAudioMute();
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
        WebDriver driver1 = getParticipant1().getDriver();
        WebDriver driver2 = getParticipant2().getDriver();

        TestUtils.waitForElementByXPath(
            driver2,
            "//span[@id='localVideoContainer']"
                + TestUtils.getXPathStringForClassName("//span", "audioMuted")
                + "/i[@class='icon-mic-disabled']", 25);

        TestUtils.waitForElementByXPath(
            driver2,
            "//span[@id='localVideoContainer']"
                + TestUtils.getXPathStringForClassName("//span", "videoMuted")
                + "/i[@class='icon-camera-disabled']", 25);

        MeetUIUtils.waitForAudioMuted(
            driver1,
            driver2,
            "participant2",
            true);

        String participant1EndpointId = getParticipant1().getEndpointId();
        TestUtils.waitForElementNotPresentByXPath(
            driver2,
            "//span[@id='participant_" + participant1EndpointId + "']"
                + TestUtils.getXPathStringForClassName("//span", "audioMuted")
                + "/i[@class='icon-mic-disabled']",
            25);

        TestUtils.waitForElementNotPresentByXPath(
            driver2,
            "//span[@id='participant_" + participant1EndpointId + "']"
                + TestUtils.getXPathStringForClassName("//span", "videoMuted")
                + "/i[@class='icon-camera-disabled']",
            25);
    }
}
