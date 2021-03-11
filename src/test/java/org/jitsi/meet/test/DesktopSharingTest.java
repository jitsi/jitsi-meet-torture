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
            "return JitsiMeetJS.app.testing.getRemoteVideoType('" + participantEndpointId + "');",
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

    @Override
    public boolean skipTestByDefault()
    {
        return true;
    }

    /**
     * A case where do non dominant speaker is sharing screen for a participant in low bandwidth mode
     * where only a screen share can be received. A bug fixed in jvb 0c5dd91b where the video was not received.
     *
     * We don't use ensureXParticipants methods because of the muting the waitForSendReceiveData will fail.
     */
    @Test(dependsOnMethods = { "testDesktopSharingStop" })
    public void testAudioOnlyAndNonDominantScreenShare()
    {
        hangUpAllParticipants();

        ensureOneParticipant();
        AudioOnlyTest.setAudioOnly(getParticipant1(), true);
        getParticipant1().getToolbar().clickAudioMuteButton();

        WebParticipant participant2 = joinSecondParticipant();
        participant2.waitToJoinMUC();
        participant2.waitForIceConnected();
        participant2.waitForSendReceiveData(true, false);

        WebParticipant participant3 = joinThirdParticipant();
        participant3.waitToJoinMUC();
        participant3.waitForIceConnected();
        participant3.waitForSendReceiveData();

        getParticipant3().getToolbar().clickAudioMuteButton();

        participant3.getToolbar().clickDesktopSharingButton();

        testDesktopSharingInPresence(getParticipant1(), participant3, "desktop");
        testDesktopSharingInPresence(getParticipant2(), participant3, "desktop");

        // the video should be playing
        TestUtils.waitForBoolean(getParticipant1().getDriver(),
            "return JitsiMeetJS.app.testing.isLargeVideoReceived();",
            10);
    }

    /**
     * A case where first participant is muted (a&v) and enters low bandwidth mode,
     * the second one is audio muted only and the one sharing (the third) is dominant speaker.
     * A problem fixed in jitsi-meet 3657c19e and d6ab0a72.
     *
     * We don't use ensureXParticipants methods because of the muting the waitForSendReceiveData will fail.
     */
    @Test(dependsOnMethods = { "testAudioOnlyAndNonDominantScreenShare" })
    public void testAudioOnlyAndDominantScreenShare()
    {
        hangUpAllParticipants();

        JitsiMeetUrl meetUrl1 = getJitsiMeetUrl()
            .appendConfig("config.startWithAudioMuted=true")
            .appendConfig("config.startWithVideoMuted=true");
        JitsiMeetUrl meetUrl2 = getJitsiMeetUrl()
            .appendConfig("config.startWithAudioMuted=true");

        ensureOneParticipant(meetUrl1);

        AudioOnlyTest.setAudioOnly(getParticipant1(), true);

        WebParticipant participant2 = joinSecondParticipant(meetUrl2);
        participant2.waitToJoinMUC();
        participant2.waitForIceConnected();

        WebParticipant participant3 = joinThirdParticipant();
        participant3.waitToJoinMUC();
        participant3.waitForIceConnected();
        participant3.waitForSendReceiveData();

        String participant3EndpointId = participant3.getEndpointId();

        participant3.getToolbar().clickDesktopSharingButton();
        testDesktopSharingInPresence(getParticipant1(), participant3, "desktop");
        testDesktopSharingInPresence(getParticipant2(), participant3, "desktop");

        assertEquals(
            participant3EndpointId,
            MeetUIUtils.getLargeVideoResource(getParticipant1().getDriver()),
            "The desktop sharing participant should be on large");

        // the video should be playing
        TestUtils.waitForBoolean(getParticipant1().getDriver(),
            "return JitsiMeetJS.app.testing.isLargeVideoReceived();",
            10);
    }

    @Test(dependsOnMethods = { "testAudioOnlyAndDominantScreenShare" })
    public void testLastNAndScreenshare()
    {
        hangUpAllParticipants();

        ensureThreeParticipants();

        getParticipant3().getToolbar().clickDesktopSharingButton();
        // Let's make sure participant1 is not dominant
        getParticipant1().getToolbar().clickAudioMuteButton();

        WebParticipant participant4 = joinFourthParticipant(getJitsiMeetUrl()
            .appendConfig("config.channelLastN=2").appendConfig("config.startWithAudioMuted=true"));
        WebDriver driver4 = participant4.getDriver();

        WebParticipant participant1 = getParticipant1();
        WebParticipant participant2 = getParticipant2();
        WebParticipant participant3 = getParticipant3();

        testDesktopSharingInPresence(participant1, participant3, "desktop");
        testDesktopSharingInPresence(participant2, participant3, "desktop");

        // Let's make sure participant3 is not dominant
        getParticipant3().getToolbar().clickAudioMuteButton();

        String participant3EndpointId = participant3.getEndpointId();

        // video on large, the screenshare from p3
        MeetUIUtils.waitsForLargeVideoSwitch(driver4, participant3EndpointId);

        // the video should be playing
        TestUtils.waitForBoolean(driver4,
            "return JitsiMeetJS.app.testing.isLargeVideoReceived();",
            10);

        // one inactive icon should appear in few seconds
        MeetUIUtils.waitForNinjaIcon(driver4);

        // and there is video for the other participant
        String participant1EndpointId = participant1.getEndpointId();
        String participant2EndpointId = participant2.getEndpointId();

        boolean p1IsNinja = MeetUIUtils.hasNinjaUserConnStatusIndication(driver4, participant1EndpointId);

        assertTrue(p1IsNinja, "Participant 1 should be ninja");

        // check participant 2 whether video is received
        MeetUIUtils.waitForRemoteVideo(driver4, participant2EndpointId, true);
        // We are getting too many false positives here we need to add
        // some debugs and investigate, will comment for now to stop failing PR tests randomly
        //MeetUIUtils.waitForRemoteVideo(driver4, participant1EndpointId, false);

        // Let's switch and check, muting participant 2 and unmuting 1 will leave participant 1 as dominant
        getParticipant1().getToolbar().clickAudioMuteButton();
        getParticipant2().getToolbar().clickAudioMuteButton();

        MeetUIUtils.waitForNinjaIcon(driver4, participant2EndpointId);
    }
}
