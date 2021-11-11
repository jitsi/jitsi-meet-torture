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

import static org.testng.Assert.*;

/**
 * To stop the video on participant1's and participant2's side.
 *
 * @author Damian Minkov
 */
public class StopVideoTest
    extends WebTestBase
{
    /**
     * Default constructor.
     */
    public StopVideoTest()
    {}

    /**
     * Constructs StopVideoTest with already allocated test.
     * @param baseTest the parent test
     * @deprecated see
     * {@link AbstractBaseTest#AbstractBaseTest(AbstractBaseTest)}
     */
    public StopVideoTest(AbstractBaseTest baseTest)
    {
        super(baseTest);
    }

    @Override
    public void setupClass()
    {
        super.setupClass();

        ensureTwoParticipants();
    }

    /**
     * Stops the video on participant1
     * TODO: do we actually need this?
     */
    @Test
    public void stopVideoOnParticipant1AndCheck()
    {
        MeetUIUtils.muteVideoAndCheck(getParticipant1(), getParticipant2());
    }

    /**
     * Starts the video on participant1.
     */
    @Test(dependsOnMethods = {"stopVideoOnParticipant1AndCheck"})
    public void startVideoOnParticipant1AndCheck()
    {
        getParticipant1().getToolbar().clickVideoMuteButton();

        // make sure we check at the remote videos on the second participant
        // side, otherwise if local is muted will fail
        getParticipant2().getParticipantsPane()
                .assertIsParticipantVideoMuted(getParticipant1(), false);
        getParticipant1().getParticipantsPane()
                .assertIsParticipantVideoMuted(getParticipant1(), false);
    }

    /**
     * Checks if muting/unmuting of the local video stream affects
     * large video of other participant.
     */
    @Test(dependsOnMethods = {"startVideoOnParticipant1AndCheck"})
    public void stopAndStartVideoOnParticipant1AndCheckStream()
    {
        // mute participant1
        MeetUIUtils.muteVideoAndCheck(getParticipant1(), getParticipant2());

        // now participant2 should be on large video
        WebDriver driver1 = getParticipant1().getDriver();
        String participant2VideoId = MeetUIUtils.getLargeVideoID(driver1);

        // unmute participant1
        startVideoOnParticipant1AndCheck();

        // check if video stream from second participant is still on large video
        assertEquals(
            MeetUIUtils.getLargeVideoID(driver1),
            participant2VideoId,
            "Large video stream id");
    }

    /**
     * Checks if muting/unmuting remote video triggers TRACK_ADDED or
     * TRACK_REMOVED events for the local participant.
     */
    @Test(dependsOnMethods = {"stopAndStartVideoOnParticipant1AndCheckStream"})
    public void stopAndStartVideoOnParticipant1AndCheckEvents()
    {
        WebParticipant participant2 = getParticipant2();
        WebDriver driver2 = participant2.getDriver();

        String listenForTrackRemoved = "APP.conference._room.addEventListener("
            + "JitsiMeetJS.events.conference.TRACK_REMOVED,"
            + "function () { APP._remoteRemoved = true; }"
            + ");";
        participant2.executeScript(listenForTrackRemoved);

        String listenForTrackAdded = "APP.conference._room.addEventListener("
            + "JitsiMeetJS.events.conference.TRACK_ADDED,"
            + "function () { APP._remoteAdded = true; }"
            + ");";
        participant2.executeScript(listenForTrackAdded);

        MeetUIUtils.muteVideoAndCheck(getParticipant1(), getParticipant2());
        startVideoOnParticipant1AndCheck();

        TestUtils.waitMillis(1000);

        assertFalse(
            TestUtils.executeScriptAndReturnBoolean(
                driver2,
                "return APP._remoteRemoved;"),
            "Remote stream was removed");
        assertFalse(
            TestUtils.executeScriptAndReturnBoolean(
                driver2,
                "return APP._remoteAdded;"),
            "Remote stream was added");
    }

    /**
     * Stops the video on participant.
     */
    @Test(dependsOnMethods = {"stopAndStartVideoOnParticipant1AndCheckEvents"})
    public void stopVideoOnParticipantAndCheck()
    {
        MeetUIUtils.muteVideoAndCheck(getParticipant2(), getParticipant1());
    }

    /**
     * Starts the video on participant.
     */
    @Test(dependsOnMethods = { "stopVideoOnParticipantAndCheck" })
    public void startVideoOnParticipantAndCheck()
    {
        getParticipant2().getToolbar().clickVideoMuteButton();

        getParticipant1().getParticipantsPane()
                .assertIsParticipantVideoMuted(getParticipant1(), false);
        getParticipant2().getParticipantsPane()
                .assertIsParticipantVideoMuted(getParticipant1(), false);
    }

    /**
     * Closes the participant and leaves participant1 alone in the room.
     * Stops video of participant1 and then joins new participant and
     * checks the status of the stopped video icon.
     * At the end starts the video to clear the state.
     */
    @Test(dependsOnMethods = { "startVideoOnParticipantAndCheck" })
    public void stopParticipant1VideoBeforeSecondParticipantJoins()
    {
        getParticipant2().hangUp();

        // just in case wait
        TestUtils.waitMillis(1000);

        getParticipant1().getToolbar().clickVideoMuteButton();

        TestUtils.waitMillis(500);

        ensureTwoParticipants();

        getParticipant2().getParticipantsPane()
                        .assertIsParticipantVideoMuted(getParticipant1(), true);

        // now lets start video for participant1
        startVideoOnParticipant1AndCheck();

        // just in case wait
        TestUtils.waitMillis(1500);
    }

}
