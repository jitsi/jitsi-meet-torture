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

import org.jitsi.meet.test.util.*;
import org.openqa.selenium.*;

/**
 * Start muted tests
 * @author Hristo Terezov
 */
public class StartMutedTest
    extends TestCase
{
    /**
     * Constructs test
     * @param name the method name for the test.
     */
    public StartMutedTest(String name)
    {
        super(name);
    }

    /**
     * Orders the tests.
     * @return the suite with order tests.
     */
    public static Test suite()
    {
        TestSuite suite = new TestSuite();

        suite.addTest(new StartMutedTest("checkboxesTest"));
        suite.addTest(new StartMutedTest("configOptionsTest"));
        return suite;
    }

    /**
     * Restarts the second participant tab and checks start muted checkboxes.
     * Test if the second participant is muted and the owner is unmuted.
     */
    public void checkboxesTest()
    {
        System.err.println("Start checkboxesTest.");

        ConferenceFixture.quit(ConferenceFixture.getSecondParticipant());
        TestUtils.waits(1000);
        WebDriver owner = ConferenceFixture.getOwner();

        // Make sure settings panel is displayed
        MeetUIUtils.makeSureSettingsAreDisplayed(owner);
        // Wait for 'start muted' checkboxes
        TestUtils.waitsForDisplayedElementByXPath(
            owner, "//input[@id='startAudioMuted']", 5);
        TestUtils.waitsForDisplayedElementByXPath(
            owner, "//input[@id='startVideoMuted']", 5);

        owner.findElement(By.id("startAudioMuted")).click();
        owner.findElement(By.id("startVideoMuted")).click();
        owner.findElement(By.id("updateSettings")).click();

        ConferenceFixture.startParticipant();
        ConferenceFixture.checkParticipantToJoinRoom(
            ConferenceFixture.getSecondParticipant(), 10);

        ConferenceFixture.waitsParticipantToJoinConference(
            ConferenceFixture.getSecondParticipant());

        checkSecondParticipantForMute();

    }

    /**
     * Opens new room and sets start muted config parameters trough the URL.
     * Test if the second participant is muted and the owner is unmuted.
     */
    public void configOptionsTest()
    {
        System.err.println("Start configOptionsTest.");

        ConferenceFixture.quit(ConferenceFixture.getSecondParticipant());
        ConferenceFixture.quit(ConferenceFixture.getOwner());
        TestUtils.waits(1000);
        ConferenceFixture.startOwner("config.startAudioMuted=1&"
            + "config.startVideoMuted=1");
        ConferenceFixture.checkParticipantToJoinRoom(
            ConferenceFixture.getOwner(), 10);

        ConferenceFixture.waitsParticipantToJoinConference(
            ConferenceFixture.getOwner());

        ConferenceFixture.startParticipant();

        ConferenceFixture.checkParticipantToJoinRoom(
            ConferenceFixture.getSecondParticipant(), 10);

        ConferenceFixture.waitsParticipantToJoinConference(
            ConferenceFixture.getSecondParticipant());

        checkSecondParticipantForMute();
    }

    /**
     * Tests if the second participant is muted and the first participant is
     * unmuted.
     */
    private void checkSecondParticipantForMute()
    {
        System.err.println("Start checkSecondParticipantForMute.");

        WebDriver secondParticipant = ConferenceFixture.getSecondParticipant();
        WebDriver owner = ConferenceFixture.getOwner();

        final String ownerResourceJid = MeetUtils.getResourceJid(owner);

        TestUtils.waitsForElementByXPath(
            secondParticipant,
            "//span[@id='localVideoContainer']/span[@class='audioMuted']/"
            + "i[@class='icon-mic-disabled']", 25);

        TestUtils.waitsForElementByXPath(
            secondParticipant,
            "//span[@id='localVideoContainer']/span[@class='videoMuted']/"
            + "i[@class='icon-camera-disabled']", 25);

        TestUtils.waitsForElementNotPresentByXPath(
            secondParticipant,
            "//span[@id='participant_" + ownerResourceJid + "']/"
                + "span[@class='audioMuted']/i[@class='icon-mic-disabled']", 25);

        TestUtils.waitsForElementNotPresentByXPath(
            secondParticipant,
            "//span[@id='participant_" + ownerResourceJid + "']/"
                + "span[@class='videoMuted']/i[@class='icon-camera-disabled']",
                25);
    }
}
