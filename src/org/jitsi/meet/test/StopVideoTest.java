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
        suite.addTest(
            new StopVideoTest("stopAndStartVideoOnOwnerAndCheckStream"));
        suite.addTest(
            new StopVideoTest("stopAndStartVideoOnOwnerAndCheckEvents"));
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
        System.err.println("Start stopVideoOnOwnerAndCheck.");

        MeetUIUtils.muteVideoAndCheck(
            ConferenceFixture.getOwner(),
            ConferenceFixture.getSecondParticipant());
    }

    /**
     * Starts the video on owner.
     */
    public void startVideoOnOwnerAndCheck()
    {
        System.err.println("Start startVideoOnOwnerAndCheck.");

        MeetUIUtils.clickOnToolbarButton(ConferenceFixture.getOwner(),
            "toolbar_button_camera");

        // make sure we check at the remote videos on the second participant
        // side, otherwise if local is muted will fail
        TestUtils.waitForElementNotPresentOrNotDisplayedByXPath(
            ConferenceFixture.getSecondParticipant(),
            "//span[starts-with(@id, 'participant_')]"
                + TestUtils.getXPathStringForClassName("//span", "videoMuted")
                + "/i[@class='icon-camera-disabled']", 10);

        TestUtils.waitForElementNotPresentOrNotDisplayedByXPath(
            ConferenceFixture.getOwner(),
            "//span[@id='localVideoContainer']"
                + TestUtils.getXPathStringForClassName("//span", "videoMuted")
                + "/i[@class='icon-camera-disabled']", 10);
    }

    /**
     * Checks if muting/unmuting of the local video stream affects
     * large video of other participant.
     */
    public void stopAndStartVideoOnOwnerAndCheckStream()
    {
        System.err.println("Start stopAndStartVideoOnOwnerAndCheckStream.");

        WebDriver owner = ConferenceFixture.getOwner();

        // mute owner
        stopVideoOnOwnerAndCheck();

        // now second participant should be on large video
        String secondParticipantVideoId = MeetUIUtils.getLargeVideoID(owner);

        // unmute owner
        startVideoOnOwnerAndCheck();

        // check if video stream from second participant is still on large video
        assertEquals("Large video stream id",
            secondParticipantVideoId,
            MeetUIUtils.getLargeVideoID(owner));
    }

    /**
     * Checks if muting/unmuting remote video triggers TRACK_ADDED or
     * TRACK_REMOVED events for the local participant.
     */
    public void stopAndStartVideoOnOwnerAndCheckEvents()
    {
        System.err.println("Start stopAndStartVideoOnOwnerAndCheckEvents.");

        WebDriver secondParticipant = ConferenceFixture.getSecondParticipant();
        JavascriptExecutor executor = (JavascriptExecutor) secondParticipant;

        String listenForTrackRemoved = "APP.conference._room.addEventListener("
            + "JitsiMeetJS.events.conference.TRACK_REMOVED,"
            + "function () { APP._remoteRemoved = true; }"
            + ");";
        executor.executeScript(listenForTrackRemoved);

        String listenForTrackAdded = "APP.conference._room.addEventListener("
            + "JitsiMeetJS.events.conference.TRACK_ADDED,"
            + "function () { APP._remoteAdded = true; }"
            + ");";
        executor.executeScript(listenForTrackAdded);

        stopVideoOnOwnerAndCheck();
        startVideoOnOwnerAndCheck();

        TestUtils.waitMillis(1000);

        assertFalse("Remote stream was removed",
                    TestUtils.executeScriptAndReturnBoolean(
                                secondParticipant,
                                "return APP._remoteRemoved;"));
        assertFalse("Remote stream was added",
                    TestUtils.executeScriptAndReturnBoolean(
                                secondParticipant,
                                "return APP._remoteAdded;"));
    }

    /**
     * Stops the video on participant.
     */
    public void stopVideoOnParticipantAndCheck()
    {
        System.err.println("Start stopVideoOnParticipantAndCheck.");

        MeetUIUtils.muteVideoAndCheck(
            ConferenceFixture.getSecondParticipant(),
            ConferenceFixture.getOwner());
    }

    /**
     * Starts the video on participant.
     */
    public void startVideoOnParticipantAndCheck()
    {
        System.err.println("Start startVideoOnParticipantAndCheck.");

        MeetUIUtils.clickOnToolbarButton(
            ConferenceFixture.getSecondParticipant(), "toolbar_button_camera");

        TestUtils.waitForElementNotPresentOrNotDisplayedByXPath(
            ConferenceFixture.getOwner(),
            TestUtils.getXPathStringForClassName("//span", "videoMuted") +
            "/i[@class='icon-camera-disabled']", 5);
        
        TestUtils.waitForElementNotPresentOrNotDisplayedByXPath(
            ConferenceFixture.getSecondParticipant(),
            TestUtils.getXPathStringForClassName("//span", "videoMuted") +
            "/i[@class='icon-camera-disabled']", 5);
    }

    /**
     * Closes the participant and leaves the owner alone in the room.
     * Stops video of the owner and then joins new participant and
     * checks the status of the stopped video icon.
     * At the end starts the video to clear the state.
     */
    public void stopOwnerVideoBeforeSecondParticipantJoins()
    {
        System.err.println("Start stopOwnerVideoBeforeSecondParticipantJoins.");

        ConferenceFixture.close(ConferenceFixture.getSecondParticipant());

        // just in case wait
        TestUtils.waitMillis(1000);

        MeetUIUtils.clickOnToolbarButton(ConferenceFixture.getOwner(),
            "toolbar_button_camera");

        TestUtils.waitMillis(500);

        WebDriver secondParticipant
            = ConferenceFixture.startSecondParticipant();

        MeetUtils.waitForParticipantToJoinMUC(secondParticipant, 10);
        MeetUtils.waitForIceConnected(secondParticipant);

        TestUtils.waitForElementByXPath(
            secondParticipant,
            TestUtils.getXPathStringForClassName("//span", "videoMuted")
            + "/i[@class='icon-camera-disabled']",
            5);

        // just debug messages
        /*{
            String ownerJid = (String) ((JavascriptExecutor)
                ConferenceFixture.getOwner())
                .executeScript("return APP.xmpp.myJid();");

            String streamByJid = "APP.RTC.remoteStreams['" + ownerJid + "']";
            System.err.println("Owner jid: " + ownerJid);

            Object streamExist = ((JavascriptExecutor)secondParticipant)
                .executeScript("return " + streamByJid + " != undefined;");
            System.err.println("Stream : " + streamExist);

            if (streamExist != null && streamExist.equals(Boolean.TRUE))
            {
                Object videoStreamExist
                    = ((JavascriptExecutor)secondParticipant).executeScript(
                        "return " + streamByJid + "['Video'] != undefined;");
                System.err.println("Stream exist : " + videoStreamExist);

                if (videoStreamExist != null && videoStreamExist
                    .equals(Boolean.TRUE))
                {
                    Object videoStreamMuted
                        = ((JavascriptExecutor) secondParticipant)
                            .executeScript(
                                "return " + streamByJid + "['Video'].muted;");
                    System.err.println("Stream muted : " + videoStreamMuted);
                }
            }
        }*/

        // now lets start video for owner
        startVideoOnOwnerAndCheck();

        // just in case wait
        TestUtils.waitMillis(1500);
    }

}
