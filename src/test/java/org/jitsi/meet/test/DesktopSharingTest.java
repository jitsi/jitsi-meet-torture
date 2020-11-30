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
import org.testng.annotations.*;

/**
 * Launches a hook script that will launch a participant that will join
 * the conference and that participant will be sharing its screen.
 *
 * @author Hristo Terezov
 */
public class DesktopSharingTest
    extends WebTestBase
{
    /**
     * The xpath for the local video element.
     */
    private static final String LOCAL_VIDEO_XPATH
        = "//span[@id='localVideoWrapper']";

    /**
     * Check desktop sharing start.
     */
    @Test
    public void testDesktopSharingStart()
    {
        ensureOneParticipant();

        ensureTwoParticipants();
        toggleDesktopSharing();
        checkExpandingDesktopSharingLargeVideo(true);
        testDesktopSharingInPresence(getParticipant1(), getParticipant2(), "desktop");
    }

    /**
     * Ensure we have only one participant available in the room - participant1.
     * Starts the new participants using the external script and check whether
     * the stream we are receiving from him is screen.
     * Returns at the end the state we found tests - with 2 participants.
     */
    @Test(dependsOnMethods = { "testDesktopSharingStart" })
    public void testDesktopSharingStop()
    {
        toggleDesktopSharing();
        checkExpandingDesktopSharingLargeVideo(false);
        testDesktopSharingInPresence(getParticipant1(), getParticipant2(), "camera");
    }

    /**
     * Checks the status of desktop sharing received from the presence and
     * compares it to the passed expected result.
     * @param localParticipant the participant doing the check
     * @param remoteParticipant the participant doing the sharing
     * @param expectedResult camera/desktop
     */
    private void testDesktopSharingInPresence(
        WebParticipant localParticipant,
        WebParticipant remoteParticipant,
        final String expectedResult)
    {
        String participantEndpointId = remoteParticipant.getEndpointId();

        TestUtils.waitForStrings(
            localParticipant.getDriver(),
            "return APP.UI.getRemoteVideoType('" + participantEndpointId + "');",
            expectedResult,
            5);
    }

    /**
     * Toggles desktop sharing.
     */
    private void toggleDesktopSharing()
    {
        getParticipant2().getToolbar().clickDesktopSharingButton();
    }

    /**
     * Checks the video layout on the other side, after we imitate
     * desktop sharing.
     * @param isScreenSharing <tt>true</tt> if SS is started and <tt>false</tt>
     * otherwise.
     */
    private void checkExpandingDesktopSharingLargeVideo(boolean isScreenSharing)
    {
        // check layout
        new VideoLayoutTest().driverVideoLayoutTest(
            getParticipant1(), isScreenSharing);

        // hide thumbs
        MeetUIUtils.clickOnToolbarButton(
            getParticipant1().getDriver(), "toggleFilmstripButton");

        TestUtils.waitForNotDisplayedElementByXPath(
            getParticipant1().getDriver(),
            LOCAL_VIDEO_XPATH,
            5);

        // check layout
        new VideoLayoutTest().driverVideoLayoutTest(
            getParticipant1(), isScreenSharing);

        // show thumbs
        MeetUIUtils.clickOnToolbarButton(
            getParticipant1().getDriver(), "toggleFilmstripButton");

        TestUtils.waitForDisplayedElementByXPath(
            getParticipant1().getDriver(),
            LOCAL_VIDEO_XPATH,
            5);
    }

    @Test(dependsOnMethods = { "testDesktopSharingStop" })
    public void testAudioOnlyAndScreenShare()
    {
        hangUpAllParticipants();

        ensureTwoParticipants();

        AudioOnlyTest.setAudioOnly(getParticipant1(), true);

        getParticipant1().getToolbar().clickAudioMuteButton();

        getParticipant2().getToolbar().clickAudioMuteButton();

        ensureThreeParticipants();

        WebParticipant participant3 = getParticipant3();
        String participant3EndpointId = participant3.getEndpointId();
        participant3.getToolbar().clickDesktopSharingButton();

        testDesktopSharingInPresence(getParticipant1(), participant3, "desktop");
        testDesktopSharingInPresence(getParticipant2(), participant3, "desktop");

        // the video should be playing (fixed in jvb2 0c5dd91b)
        TestUtils.waitForBoolean(getParticipant1().getDriver(),
            "return APP.UI.isRemoteVideoPlaying('" + participant3EndpointId + "');",
            10);

        // now change dominant speaker
        getParticipant2().getToolbar().clickAudioMuteButton();

        getParticipant3().hangUp();

        ensureThreeParticipants();

        participant3 = getParticipant3();

        getParticipant3().getToolbar().clickAudioMuteButton();
        participant3EndpointId = participant3.getEndpointId();

        participant3.getToolbar().clickDesktopSharingButton();
        testDesktopSharingInPresence(getParticipant1(), participant3, "desktop");
        testDesktopSharingInPresence(getParticipant2(), participant3, "desktop");

        // the video should be playing (fixed in jitsi-meet 3657c19e)
        TestUtils.waitForBoolean(getParticipant1().getDriver(),
            "return APP.UI.isRemoteVideoPlaying('" + participant3EndpointId + "');",
            10);
    }

    public void testLastNAndScreenshare()
    {

    }

    public void testLastNTileViewAndScreenshare()
    {

    }
}
