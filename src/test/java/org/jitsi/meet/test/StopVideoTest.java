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

import org.openqa.selenium.*;
import org.testng.annotations.*;

import static org.testng.Assert.*;

/**
 * To stop the video on owner and participant side.
 * @author Damian Minkov
 */
public class StopVideoTest
    extends AbstractBaseTest
{
    /**
     * Default constructor.
     */
    public StopVideoTest()
    {}

    /**
     * Constructs StopVideoTest with already allocated participants.
     * @param participant1 the first participant
     * @param participant2 the second participant
     */
    public StopVideoTest(
        Participant participant1, Participant participant2)
    {
        this.addParticipants(participant1, participant2);
    }

    @Override
    public void setup()
    {
        super.setup();

        ensureTwoParticipants();
    }

    /**
     * Stops the video on the conference owner.
     */
    @Test
    public void stopVideoOnOwnerAndCheck()
    {
        MeetUIUtils.muteVideoAndCheck(
            getParticipant1().getDriver(),
            getParticipant2().getDriver());
    }

    /**
     * Starts the video on owner.
     */
    @Test(dependsOnMethods = { "stopVideoOnOwnerAndCheck" })
    public void startVideoOnOwnerAndCheck()
    {
        MeetUIUtils.clickOnToolbarButton(getParticipant1().getDriver(),
            "toolbar_button_camera");

        // make sure we check at the remote videos on the second participant
        // side, otherwise if local is muted will fail
        TestUtils.waitForElementNotPresentOrNotDisplayedByXPath(
            getParticipant2().getDriver(),
            "//span[starts-with(@id, 'participant_')]"
                + TestUtils.getXPathStringForClassName("//span", "videoMuted")
                + "/i[@class='icon-camera-disabled']", 10);

        TestUtils.waitForElementNotPresentOrNotDisplayedByXPath(
            getParticipant1().getDriver(),
            "//span[@id='localVideoContainer']"
                + TestUtils.getXPathStringForClassName("//span", "videoMuted")
                + "/i[@class='icon-camera-disabled']", 10);
    }

    /**
     * Checks if muting/unmuting of the local video stream affects
     * large video of other participant.
     */
    @Test(dependsOnMethods = { "startVideoOnOwnerAndCheck" })
    public void stopAndStartVideoOnOwnerAndCheckStream()
    {
        WebDriver owner = getParticipant1().getDriver();

        // mute owner
        stopVideoOnOwnerAndCheck();

        // now second participant should be on large video
        String secondParticipantVideoId = MeetUIUtils.getLargeVideoID(owner);

        // unmute owner
        startVideoOnOwnerAndCheck();

        // check if video stream from second participant is still on large video
        assertEquals(
            secondParticipantVideoId,
            MeetUIUtils.getLargeVideoID(owner),
            "Large video stream id");
    }

    /**
     * Checks if muting/unmuting remote video triggers TRACK_ADDED or
     * TRACK_REMOVED events for the local participant.
     */
    @Test(dependsOnMethods = { "stopAndStartVideoOnOwnerAndCheckStream" })
    public void stopAndStartVideoOnOwnerAndCheckEvents()
    {
        WebDriver secondParticipant = getParticipant2().getDriver();
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

        assertFalse(
            TestUtils.executeScriptAndReturnBoolean(
                        secondParticipant,
                        "return APP._remoteRemoved;"),
            "Remote stream was removed");
        assertFalse(
            TestUtils.executeScriptAndReturnBoolean(
                        secondParticipant,
                        "return APP._remoteAdded;"),
            "Remote stream was added");
    }

    /**
     * Stops the video on participant.
     */
    @Test(dependsOnMethods = { "stopAndStartVideoOnOwnerAndCheckEvents" })
    public void stopVideoOnParticipantAndCheck()
    {
        MeetUIUtils.muteVideoAndCheck(
            getParticipant2().getDriver(),
            getParticipant1().getDriver());
    }

    /**
     * Starts the video on participant.
     */
    @Test(dependsOnMethods = { "stopVideoOnParticipantAndCheck" })
    public void startVideoOnParticipantAndCheck()
    {
        MeetUIUtils.clickOnToolbarButton(
            getParticipant2().getDriver(), "toolbar_button_camera");

        TestUtils.waitForElementNotPresentOrNotDisplayedByXPath(
            getParticipant1().getDriver(),
            TestUtils.getXPathStringForClassName("//span", "videoMuted") +
            "/i[@class='icon-camera-disabled']", 5);
        
        TestUtils.waitForElementNotPresentOrNotDisplayedByXPath(
            getParticipant2().getDriver(),
            TestUtils.getXPathStringForClassName("//span", "videoMuted") +
            "/i[@class='icon-camera-disabled']", 5);
    }

    /**
     * Closes the participant and leaves the owner alone in the room.
     * Stops video of the owner and then joins new participant and
     * checks the status of the stopped video icon.
     * At the end starts the video to clear the state.
     */
    @Test(dependsOnMethods = { "startVideoOnParticipantAndCheck" })
    public void stopOwnerVideoBeforeSecondParticipantJoins()
    {
        getParticipant2().hangUp();

        // just in case wait
        TestUtils.waitMillis(1000);

        MeetUIUtils.clickOnToolbarButton(
            getParticipant1().getDriver(),
            "toolbar_button_camera");

        TestUtils.waitMillis(500);

        ensureTwoParticipants();
        WebDriver secondParticipant = getParticipant2().getDriver();

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
            print("Owner jid: " + ownerJid);

            Object streamExist = ((JavascriptExecutor)secondParticipant)
                .executeScript("return " + streamByJid + " != undefined;");
            print("Stream : " + streamExist);

            if (streamExist != null && streamExist.equals(Boolean.TRUE))
            {
                Object videoStreamExist
                    = ((JavascriptExecutor)secondParticipant).executeScript(
                        "return " + streamByJid + "['Video'] != undefined;");
                print("Stream exist : " + videoStreamExist);

                if (videoStreamExist != null && videoStreamExist
                    .equals(Boolean.TRUE))
                {
                    Object videoStreamMuted
                        = ((JavascriptExecutor) secondParticipant)
                            .executeScript(
                                "return " + streamByJid + "['Video'].muted;");
                    print("Stream muted : " + videoStreamMuted);
                }
            }
        }*/

        // now lets start video for owner
        startVideoOnOwnerAndCheck();

        // just in case wait
        TestUtils.waitMillis(1500);
    }

}
