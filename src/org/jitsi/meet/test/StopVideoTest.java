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

/**
 * To stop the video on owner and participant side.
 * @author Damian Minkov
 */
public class StopVideoTest
    extends TestCase
{
    /**
     * Constructs test.
     * @param name the method name for the test.
     */
    public StopVideoTest(String name)
    {
        super(name);
    }

    /**
     * Orders the tests.
     * @return the suite with order tests.
     */
    public static junit.framework.Test suite()
    {
        TestSuite suite = new TestSuite();

        suite.addTest(new StopVideoTest("stopVideoOnOwnerAndCheck"));
        suite.addTest(new StopVideoTest("startVideoOnOwnerAndCheck"));
        suite.addTest(new StopVideoTest("stopVideoOnParticipantAndCheck"));
        suite.addTest(new StopVideoTest("startVideoOnParticipantAndCheck"));
        suite.addTest(new StopVideoTest(
            "stopOwnerVideoBeforeSecondParticipantJoins"));

        return suite;
    }

    /**
     * Stops the video on the conference owner.
     */
    public void stopVideoOnOwnerAndCheck()
    {
        MeetUIUtils.clickOnToolbarButton(ConferenceFixture.getOwner(), "video");

        TestUtils.waitsForElementByXPath(
            ConferenceFixture.getSecondParticipant(),
            "//span[@class='videoMuted']/i[@class='icon-camera-disabled']", 5);
    }

    /**
     * Starts the video on owner.
     */
    public void startVideoOnOwnerAndCheck()
    {
        MeetUIUtils.clickOnToolbarButton(ConferenceFixture.getOwner(), "video");

        TestUtils.waitsForElementNotPresentByXPath(
            ConferenceFixture.getSecondParticipant(),
            "//span[@class='videoMuted']/i[@class='icon-camera-disabled']", 5);
    }

    /**
     * Stops the video on participant.
     */
    public void stopVideoOnParticipantAndCheck()
    {
        MeetUIUtils.clickOnToolbarButton(
            ConferenceFixture.getSecondParticipant(), "video");

        TestUtils.waitsForElementByXPath(
            ConferenceFixture.getOwner(),
            "//span[@class='videoMuted']/i[@class='icon-camera-disabled']", 5);
    }

    /**
     * Starts the video on participant.
     */
    public void startVideoOnParticipantAndCheck()
    {
        MeetUIUtils.clickOnToolbarButton(
            ConferenceFixture.getSecondParticipant(), "video");

        TestUtils.waitsForElementNotPresentByXPath(
            ConferenceFixture.getOwner(),
            "//span[@class='videoMuted']/i[@class='icon-camera-disabled']", 5);
    }

    /**
     * Closes the participant and leaves the owner alone in the room.
     * Stops video of the owner and then joins new participant and
     * checks the status of the stopped video icon.
     * At the end starts the video to clear the state.
     */
    public void stopOwnerVideoBeforeSecondParticipantJoins()
    {
        ConferenceFixture.quit(ConferenceFixture.getSecondParticipant());

        // just in case wait
        TestUtils.waits(1000);

        MeetUIUtils.clickOnToolbarButton(ConferenceFixture.getOwner(), "video");

        TestUtils.waits(500);

        ConferenceFixture.startParticipant();

        ConferenceFixture.checkParticipantToJoinRoom(
            ConferenceFixture.getSecondParticipant(), 10);

        ConferenceFixture.waitsParticipantToJoinConference(
            ConferenceFixture.getSecondParticipant());

        TestUtils.waitsForElementByXPath(
            ConferenceFixture.getSecondParticipant(),
            "//span[@class='videoMuted']/i[@class='icon-camera-disabled']", 5);

        // now lets start video for owner
        startVideoOnOwnerAndCheck();

        // just in case wait
        TestUtils.waits(1500);
    }

}
