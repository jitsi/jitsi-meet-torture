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
 * @author Pawel Domas
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
        //suite.addTest(new StartMutedTest("configOptionsTest"));
        //suite.addTest(new StartMutedTest("restartParticipants"));
        return suite;
    }

    /**
     * Restarts the second participant tab and checks start muted checkboxes.
     * Test if the second participant is muted and the owner is unmuted.
     */
    public void checkboxesTest()
    {
        for (int i=0; i<30;i++)
        {
            System.err.println("Start checkboxesTest." + i);


            WebDriver secondPeer = ConferenceFixture.getSecondParticipantInstance();
            if (secondPeer != null)
                ConferenceFixture.close(secondPeer);

            WebDriver owner = ConferenceFixture.getOwner();
            TestUtils.waitMillis(1000);

            // Make sure settings panel is displayed
            MeetUIUtils.displaySettingsPanel(owner);
            // Wait for 'start muted' checkboxes
            TestUtils.waitForDisplayedElementByXPath(
                owner, "//input[@id='startAudioMuted']", 5);
            TestUtils.waitForDisplayedElementByXPath(
                owner, "//input[@id='startVideoMuted']", 5);

            owner.findElement(By.id("startAudioMuted")).click();
            owner.findElement(By.id("startVideoMuted")).click();

            WebDriver secondParticipant
                = ConferenceFixture.startSecondParticipant();
            MeetUtils.waitForParticipantToJoinMUC(secondParticipant, 10);
            MeetUtils.waitForIceConnected(secondParticipant);



            checkSecondParticipantForMute();

            ConferenceFixture.closeAllParticipants();
        }
    }

    /**
     * Opens new room and sets start muted config parameters trough the URL.
     * Test if the second participant is muted and the owner is unmuted.
     */
    public void configOptionsTest()
    {
        System.err.println("Start configOptionsTest.");

        ConferenceFixture.closeAllParticipants();

        WebDriver owner
            = ConferenceFixture.startOwner("config.startAudioMuted=1&" +
                                           "config.startVideoMuted=1");
        MeetUtils.waitForParticipantToJoinMUC(owner, 10);

        final WebDriver secondParticipant
            = ConferenceFixture.startSecondParticipant();
        MeetUtils.waitForParticipantToJoinMUC(secondParticipant, 10);

        MeetUtils.waitForIceConnected(owner);
        MeetUtils.waitForIceConnected(secondParticipant);

        checkSecondParticipantForMute();

        // Unmute and see if the audio works
        MeetUIUtils.clickOnToolbarButton(
            secondParticipant, "toolbar_button_mute");
        MeetUIUtils.waitForAudioMuted(
            owner, secondParticipant, "second peer", false /* unmuted */);
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

        TestUtils.waitForElementByXPath(
            secondParticipant,
            "//span[@id='localVideoContainer']/span[@class='audioMuted']/"
                + "i[@class='icon-mic-disabled']", 25);

        TestUtils.waitForElementByXPath(
            secondParticipant,
            "//span[@id='localVideoContainer']/span[@class='videoMuted']/"
                + "i[@class='icon-camera-disabled']", 25);

        MeetUIUtils.waitForAudioMuted(
            owner,
            secondParticipant,
            "secondParticipant",
            true);

        TestUtils.waitForElementNotPresentByXPath(
            secondParticipant,
            "//span[@id='participant_" + ownerResourceJid + "']/"
                + "span[@class='audioMuted']/i[@class='icon-mic-disabled']", 25);

        TestUtils.waitForElementNotPresentByXPath(
            secondParticipant,
            "//span[@id='participant_" + ownerResourceJid + "']/"
                + "span[@class='videoMuted']/i[@class='icon-camera-disabled']",
            25);
    }

    /**
     * Restarts the two participants so we clear states of this test.
     */
    public void restartParticipants()
    {
        System.err.println("Start restartParticipants.");

        ConferenceFixture.close(ConferenceFixture.getSecondParticipant());
        ConferenceFixture.close(ConferenceFixture.getOwner());
        TestUtils.waitMillis(1000);

        WebDriver owner = ConferenceFixture.startOwner(null);

        MeetUtils.waitForParticipantToJoinMUC(owner, 10);
        ConferenceFixture.waitForSecondParticipantToConnect();
        MeetUtils.waitForIceConnected(owner);
    }
}
