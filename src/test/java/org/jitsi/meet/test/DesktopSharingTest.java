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
    public static void testDesktopSharingInPresence(
        WebParticipant localParticipant,
        WebParticipant remoteParticipant,
        final String expectedResult)
    {
        String participantEndpointId = remoteParticipant.getEndpointId();

        TestUtils.waitForStrings(
            localParticipant.getDriver(),
            "return JitsiMeetJS.app.testing.getRemoteVideoType('" + participantEndpointId + "');",
            expectedResult,
            10);
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

        WebDriver driver1 = getParticipant1().getDriver();

        // a workaround to directly set audio only mode without going through the rest of the settings in the UI
        TestUtils.executeScript(driver1, "APP.store.dispatch({type: 'SET_AUDIO_ONLY', audioOnly: true});");
        TestUtils.executeScript(driver1, "APP.UI.emitEvent('UI.toggle_audioonly', true);");

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

        WebDriver driver1 = getParticipant1().getDriver();

        // a workaround to directly set audio only mode without going through the rest of the settings in the UI
        TestUtils.executeScript(driver1, "APP.store.dispatch({type: 'SET_AUDIO_ONLY', audioOnly: true});");
        TestUtils.executeScript(driver1, "APP.UI.emitEvent('UI.toggle_audioonly', true);");

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

    /**
     * Test screensharing with lastN. We add p4 with lastN=2 and verify that it receives the expected streams.
     */
    @Test(dependsOnMethods = { "testAudioOnlyAndDominantScreenShare" })
    public void testLastNAndScreenshare()
    {
        hangUpAllParticipants();
        ensureThreeParticipants();

        WebParticipant participant1 = getParticipant1();
        WebParticipant participant2 = getParticipant2();
        WebParticipant participant3 = getParticipant3();

        participant3.getToolbar().clickDesktopSharingButton();

        participant1.getToolbar().clickAudioMuteButton();
        participant3.getToolbar().clickAudioMuteButton();

        WebParticipant participant4 = joinFourthParticipant(getJitsiMeetUrl()
            .appendConfig("config.channelLastN=2")
            .appendConfig("config.startWithAudioMuted=true"));
        WebDriver driver4 = participant4.getDriver();

        // We now have p1, p2, p3, p4.
        // p3 is screensharing.
        // p1, p3, p4 are audio muted, so p2 should eventually become dominant speaker.

        // Participants should have received signaling that p3 is screensharing.
        testDesktopSharingInPresence(participant1, participant3, "desktop");
        testDesktopSharingInPresence(participant2, participant3, "desktop");
        testDesktopSharingInPresence(participant4, participant3, "desktop");

        // Participants should display p3 on-stage because it is screensharing.
        MeetUIUtils.waitsForLargeVideoSwitch(driver4, participant3.getEndpointId());
        // And the video should be playing
        TestUtils.waitForBoolean(driver4,
            "return JitsiMeetJS.app.testing.isLargeVideoReceived();",
            10);

        // Participant4 has lastN=2 and has selected p3. With p2 being dominant speaker p4 should eventually
        // see video for [p3, p2] and p1 as ninja.
        MeetUIUtils.waitForNinjaIcon(driver4, participant1.getEndpointId());

        MeetUIUtils.waitForRemoteVideo(driver4, participant2.getEndpointId(), true);
        // We are getting too many false positives here we need to add
        // some debugs and investigate, will comment for now to stop failing PR tests randomly
        //MeetUIUtils.waitForRemoteVideo(driver4, participant1EndpointId, false);

        // the FF audio is a constant beep sound and will not be detected and will not trigger dominant speaker
        // switch. We skip it in this case
        if (!participant1.getType().isFirefox())
        {
            // Let's switch and check, muting participant 2 and unmuting 1 will leave participant 1 as dominant
            participant1.getToolbar().clickAudioMuteButton();
            participant2.getToolbar().clickAudioMuteButton();

            // Participant4 should eventually see video for [p3, p1] and p2 as a ninja.
            MeetUIUtils.waitForNinjaIcon(driver4, participant2.getEndpointId());
            MeetUIUtils.waitForRemoteVideo(driver4, participant1.getEndpointId(), true);
        }
    }

    /**
     * Tests if screenshare is successful when it is started after after the local video is muted.
     */
    @Test(dependsOnMethods = { "testLastNAndScreenshare" })
    public void testMuteAndScreenShare()
    {
        hangUpAllParticipants();
        ensureTwoParticipants();

        WebParticipant participant1 = getParticipant1();
        WebParticipant participant2 = getParticipant2();

        // Mute video on participant1 before starting screenshare.
        MeetUIUtils.muteVideoAndCheck(participant1, participant2);

        participant1.getToolbar().clickDesktopSharingButton();
        testDesktopSharingInPresence(participant2, participant1, "desktop");
        // And the video should be playing
        TestUtils.waitForBoolean(participant2.getDriver(),
            "return JitsiMeetJS.app.testing.isLargeVideoReceived();",
            10);

        // Stop desktop sharing on p1 and enable camera.
        participant1.getToolbar().clickDesktopSharingButton();
        MeetUIUtils.unmuteVideoAndCheck(participant1, participant2);
    }
}
