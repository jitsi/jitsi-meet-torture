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

import com.google.gson.*;
import org.jitsi.meet.test.pageobjects.web.*;
import org.jitsi.meet.test.util.*;
import org.jitsi.meet.test.web.*;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;
import org.testng.*;
import org.testng.annotations.*;

import java.util.function.*;

import static org.testng.Assert.*;

/**
 * Tests the functions of iframeAPI.
 *
 * @author Damian Minkov
 */
public class IFrameAPIFunctionsTest
    extends IFrameAPIBase
{
    @Override
    public void setupClass()
    {
        super.setupClass();

        checkModerationSupported();
    }

    /**
     * Functions testing:
     * https://jitsi.github.io/handbook/docs/dev-guide/dev-guide-iframe#getparticipantsinfo
     * https://jitsi.github.io/handbook/docs/dev-guide/dev-guide-iframe#getdisplayname
     * Event:
     * https://jitsi.github.io/handbook/docs/dev-guide/dev-guide-iframe#displaynamechange
     * https://jitsi.github.io/handbook/docs/dev-guide/dev-guide-iframe#emailchange
     * https://jitsi.github.io/handbook/docs/dev-guide/dev-guide-iframe#videoconferencejoined
     *
     * Tests and passing userInfo to JitsiMeetExternalAPI options and getDisplayName
     */
    @Test
    public void testFunctionGetParticipantsInfo()
    {
        String displayName = "John Doe";
        String email = "email@jitsiexamplemail.com";

        JsonObject userInfo = new JsonObject();
        userInfo.addProperty("email", email);
        userInfo.addProperty("displayName", displayName);

        this.iFrameUrl = getIFrameUrl(userInfo, null);

        ensureOneParticipant(this.iFrameUrl);

        WebParticipant participant1 = getParticipant1();
        String endpointId1 = participant1.getEndpointId();
        WebDriver driver = participant1.getDriver();

        switchToIframeAPI(driver);

        JsonObject joinedEvent = getEventResult(driver, "videoConferenceJoined");
        assertEquals(joinedEvent.get("roomName").getAsString(), getJitsiMeetUrl().getRoomName());
        assertEquals(joinedEvent.get("id").getAsString(), endpointId1);
        assertEquals(joinedEvent.get("displayName").getAsString(), displayName);

        addIframeAPIListener(driver, "displayNameChange");

        String s = TestUtils.executeScriptAndReturnString(driver,
            "return JSON.stringify(window.jitsiAPI.getParticipantsInfo());");
        assertNotNull(s);
        String apiDisplayName = TestUtils.executeScriptAndReturnString(driver,
            "return window.jitsiAPI.getDisplayName('" + endpointId1 + "');");

        // returns an array of participants, in this case just us
        JsonObject participantsInfo = JsonParser.parseString(s).getAsJsonArray().get(0).getAsJsonObject();

        // check displayName from getParticipantsInfo, returned as "John Doe (me)"
        assertTrue(
            participantsInfo.get("formattedDisplayName").getAsString().contains(displayName),
            "Wrong display name from iframeAPI.getParticipantsInfo");
        assertEquals(apiDisplayName, displayName);

        switchToMeetContent(this.iFrameUrl, driver);

        DisplayNameTest.checkDisplayNameOnLocalVideo(participant1, displayName);

        // check displayName and email in settings through UI
        participant1.getToolbar().clickSettingsButton();
        SettingsDialog settingsDialog = participant1.getSettingsDialog();
        settingsDialog.waitForDisplay();
        assertEquals(settingsDialog.getDisplayName(), displayName);
        assertEquals(settingsDialog.getEmail(), email);

        settingsDialog.close();

        String newName = displayName + (int) (Math.random() * 1_000_000);
        participant1.setDisplayName(newName);

        participant1.getToolbar().clickSettingsButton();
        settingsDialog = participant1.getSettingsDialog();
        settingsDialog.waitForDisplay();
        assertEquals(settingsDialog.getDisplayName(), newName);

        // make sure we are not selecting local video, so we mess up the pin tests later
        MeetUIUtils.clickOnLocalVideo(driver);

        switchToIframeAPI(driver);

        JsonObject displayNameEvent = getEventResult(driver, "displayNameChange");
        assertEquals(displayNameEvent.get("displayname").getAsString(), newName);
        assertEquals(displayNameEvent.get("id").getAsString(), endpointId1);

        apiDisplayName = TestUtils.executeScriptAndReturnString(driver,
            "return window.jitsiAPI.getDisplayName('" + endpointId1 + "');");
        assertEquals(apiDisplayName, newName);

        switchToMeetContent(this.iFrameUrl, driver);
        settingsDialog.close();
    }

    /**
     * Functions testing:
     * https://jitsi.github.io/handbook/docs/dev-guide/dev-guide-iframe#getvideoquality
     * Command:
     * https://jitsi.github.io/handbook/docs/dev-guide/dev-guide-iframe#setvideoquality
     * Event:
     * https://jitsi.github.io/handbook/docs/dev-guide/dev-guide-iframe#videoqualitychanged
     *
     * Test retrieving video quality.
     */
    @Test(dependsOnMethods = { "testFunctionGetParticipantsInfo" })
    public void testFunctionGetVideoQuality()
    {
        this.iFrameUrl = getIFrameUrl(null, null);
        ensureOneParticipant(this.iFrameUrl);

        WebParticipant participant1 = getParticipant1();
        WebDriver driver = participant1.getDriver();

        switchToIframeAPI(driver);
        addIframeAPIListener(driver, "videoQualityChanged");

        String s = TestUtils.executeScriptAndReturnString(driver,
            "return JSON.stringify(window.jitsiAPI.getVideoQuality());");

        // by default, we start with high quality
        assertEquals(s, "2160");

        switchToMeetContent(this.iFrameUrl, driver);

        AudioOnlyTest.setAudioOnly(participant1, true);

        switchToIframeAPI(driver);

        // audio only switches to 180
        TestUtils.waitForCondition(driver, 2, (ExpectedCondition<Boolean>) d ->
            TestUtils.executeScriptAndReturnBoolean(driver, "return window.jitsiAPI.getVideoQuality() === 180;"));

        assertEquals(getEventResult(driver, "videoQualityChanged").get("videoQuality").getAsString(), "180");

        switchToMeetContent(this.iFrameUrl, driver);

        TestUtils.waitMillis(500);
        AudioOnlyTest.setAudioOnly(participant1, false);

        // now let's test the command
        switchToIframeAPI(driver);

        TestUtils.executeScript(driver,
            "window.jitsiAPI.executeCommand('setVideoQuality', 360);");

        TestUtils.waitForCondition(driver, 2, (ExpectedCondition<Boolean>) d ->
            TestUtils.executeScriptAndReturnBoolean(driver, "return window.jitsiAPI.getVideoQuality() === 360;"));

        assertEquals(getEventResult(driver, "videoQualityChanged").get("videoQuality").getAsString(), "360");

        switchToMeetContent(this.iFrameUrl, driver);
    }

    /**
     * Functions testing:
     * https://jitsi.github.io/handbook/docs/dev-guide/dev-guide-iframe#pinparticipant
     *
     * Test pinning participant.
     */
    @Test(dependsOnMethods = { "testFunctionGetVideoQuality" })
    public void testFunctionPinParticipant()
    {
        this.iFrameUrl = getIFrameUrl(null, null);
        // the previous test left it in meeting content
        ensureThreeParticipants(this.iFrameUrl, null, null);

        WebParticipant participant1 = getParticipant1();
        WebDriver driver1 = participant1.getDriver();

        // selects local
        MeetUIUtils.selectLocalVideo(driver1);

        String endpoint2Id = getParticipant2().getEndpointId();

        switchToIframeAPI(driver1);
        TestUtils.executeScriptAndReturnString(driver1,
            "JSON.stringify(window.jitsiAPI.pinParticipant('" + endpoint2Id + "'));");

        switchToMeetContent(this.iFrameUrl, driver1);

        // we must not be in tile view
        MeetUIUtils.waitForTileViewDisplay(participant1, false);

        MeetUIUtils.waitForLargeVideoSwitchToEndpoint(driver1, endpoint2Id);

        // unpin second, so we clear the state for next in line for testing
        SwitchVideoTest.unpinRemoteVideoAndTest(participant1, endpoint2Id);
    }

    /**
     * Functions testing:
     * https://jitsi.github.io/handbook/docs/dev-guide/dev-guide-iframe#setlargevideoparticipant
     * Event:
     * https://jitsi.github.io/handbook/docs/dev-guide/dev-guide-iframe#dominantspeakerchanged
     *
     * Test selecting participant on large.
     */
    @Test(dependsOnMethods = { "testFunctionGetVideoQuality" })
    public void testFunctionSetLargeVideoParticipant()
    {
        testSetLargeVideoParticipant(true);
    }

    /**
     * Functions testing:
     * https://jitsi.github.io/handbook/docs/dev-guide/dev-guide-iframe#getnumberofparticipants
     *
     * Test retrieving number of participants.
     */
    @Test(dependsOnMethods = { "testFunctionSetLargeVideoParticipant" })
    public void testFunctionGetNumberOfParticipants()
    {
        ensureThreeParticipants(this.iFrameUrl, null, null);

        WebDriver driver1 = getParticipant1().getDriver();
        switchToIframeAPI(driver1);

        double numOfParticipants = TestUtils.executeScriptAndReturnDouble(driver1,
            "return window.jitsiAPI.getNumberOfParticipants();");

        assertEquals(numOfParticipants, 3d);

        switchToMeetContent(this.iFrameUrl, driver1);
        getParticipant3().hangUp();

        switchToIframeAPI(driver1);
        numOfParticipants = TestUtils.executeScriptAndReturnDouble(driver1,
            "return window.jitsiAPI.getNumberOfParticipants();");

        assertEquals(numOfParticipants, 2d);

        switchToMeetContent(this.iFrameUrl, driver1);
        ensureThreeParticipants(this.iFrameUrl, null, null);

        switchToIframeAPI(driver1);

        numOfParticipants = TestUtils.executeScriptAndReturnDouble(driver1,
            "return window.jitsiAPI.getNumberOfParticipants();");

        assertEquals(numOfParticipants, 3d);

        switchToMeetContent(this.iFrameUrl, driver1);
    }

    /**
     * Functions testing:
     * https://jitsi.github.io/handbook/docs/dev-guide/dev-guide-iframe#isaudiomuted
     * https://jitsi.github.io/handbook/docs/dev-guide/dev-guide-iframe#isvideomuted
     *
     * Commands testing:
     * https://jitsi.github.io/handbook/docs/dev-guide/dev-guide-iframe#toggleaudio
     * https://jitsi.github.io/handbook/docs/dev-guide/dev-guide-iframe#toggleVideo
     *
     * Events testing:
     * https://jitsi.github.io/handbook/docs/dev-guide/dev-guide-iframe#audiomutestatuschanged
     * https://jitsi.github.io/handbook/docs/dev-guide/dev-guide-iframe#videomutestatuschanged
     *
     * Test retrieving local audio/video muted state.
     */
    @Test(dependsOnMethods = { "testFunctionGetNumberOfParticipants" })
    public void testFunctionIsAudioOrVideoMuted()
    {
        this.iFrameUrl = getIFrameUrl(null, null);
        ensureTwoParticipants(this.iFrameUrl, null);

        WebParticipant participant1 = getParticipant1();
        WebParticipant participant2 = getParticipant2();
        WebDriver driver1 = participant1.getDriver();

        Function<WebDriver, Boolean[]> getAPIAudioAndVideoState = d -> {
            TestUtils.executeScript(d,
                "return window.jitsiAPI.isAudioMuted().then(r => window.jitsiAPI.testisAudioOrVideoMutedR1 = r);");
            TestUtils.executeScript(d,
                "return window.jitsiAPI.isVideoMuted().then(r => window.jitsiAPI.testisAudioOrVideoMutedR2 = r);");

            boolean result1 = TestUtils.executeScriptAndReturnBoolean(d,
                "return window.jitsiAPI.testisAudioOrVideoMutedR1;");
            boolean result2 = TestUtils.executeScriptAndReturnBoolean(d,
                "return window.jitsiAPI.testisAudioOrVideoMutedR2;");

            return new Boolean[] { result1, result2 };
        };

        switchToIframeAPI(driver1);
        Boolean[] result = getAPIAudioAndVideoState.apply(driver1);

        assertFalse(result[0], "Audio must be initially unmuted");
        assertFalse(result[1], "Video must be initially unmuted");

        addIframeAPIListener(driver1, "audioMuteStatusChanged");
        addIframeAPIListener(driver1, "videoMuteStatusChanged");

        TestUtils.executeScript(driver1,
            "return window.jitsiAPI.executeCommand('toggleAudio');");
        TestUtils.executeScript(driver1,
            "return window.jitsiAPI.executeCommand('toggleVideo');");

        switchToMeetContent(this.iFrameUrl, driver1);
        participant2.getFilmstrip().assertAudioMuteIcon(participant1, true);
        participant2.getParticipantsPane().assertIsParticipantVideoMuted(participant1, true);

        switchToIframeAPI(driver1);
        result = getAPIAudioAndVideoState.apply(driver1);

        assertTrue(result[0], "Audio must be muted");
        assertTrue(result[1], "Video must be muted");

        TestUtils.waitForCondition(driver1, 2, (ExpectedCondition<Boolean>) d ->
            getEventResult(d, "audioMuteStatusChanged").get("muted").getAsBoolean());
        TestUtils.waitForCondition(driver1, 2, (ExpectedCondition<Boolean>) d ->
            getEventResult(d, "videoMuteStatusChanged").get("muted").getAsBoolean());

        // let's revert to the initial state
        TestUtils.executeScript(driver1,
            "return window.jitsiAPI.executeCommand('toggleAudio');");
        TestUtils.executeScript(driver1,
            "return window.jitsiAPI.executeCommand('toggleVideo');");

        TestUtils.waitForCondition(driver1, 2, (ExpectedCondition<Boolean>) d ->
            !getEventResult(d, "audioMuteStatusChanged").get("muted").getAsBoolean());
        TestUtils.waitForCondition(driver1, 2, (ExpectedCondition<Boolean>) d ->
            !getEventResult(d, "videoMuteStatusChanged").get("muted").getAsBoolean());

        switchToMeetContent(this.iFrameUrl, driver1);
        participant2.getFilmstrip().assertAudioMuteIcon(participant1, false);
        participant2.getParticipantsPane().assertIsParticipantVideoMuted(participant1, false);
    }

    /**
     * Functions testing:
     * https://jitsi.github.io/handbook/docs/dev-guide/dev-guide-iframe#ismoderationon
     *
     * Commands testing:
     * https://jitsi.github.io/handbook/docs/dev-guide/dev-guide-iframe#togglemoderation
     *
     * Events testing:
     * https://jitsi.github.io/handbook/docs/dev-guide/dev-guide-iframe#moderationstatuschanged
     *
     * Test retrieving local audio/video muted state.
     */
    @Test(dependsOnMethods = { "testFunctionIsAudioOrVideoMuted" })
    public void testFunctionIsModerationOn()
    {
        if (!isModeratorSupported)
        {
            throw new SkipException("Moderation is required for this test.");
        }

        this.iFrameUrl = getIFrameUrl(null, null);
        ensureOneParticipant(this.iFrameUrl);

        WebParticipant participant1 = getParticipant1();
        WebDriver driver1 = participant1.getDriver();

        Function<WebDriver, Boolean[]> getAPIModerationState = d -> {
            TestUtils.executeScript(d,
                "return window.jitsiAPI.isModerationOn().then(r => window.jitsiAPI.testisAudioModerationOn = r);");
            TestUtils.executeScript(d,
                "return window.jitsiAPI.isModerationOn('video')" +
                    ".then(r => window.jitsiAPI.testisVideoModerationOn = r);");

            boolean result1 = TestUtils.executeScriptAndReturnBoolean(d,
                "return window.jitsiAPI.testisAudioModerationOn;");
            boolean result2 = TestUtils.executeScriptAndReturnBoolean(d,
                "return window.jitsiAPI.testisVideoModerationOn;");

            return new Boolean[] { result1, result2 };
        };

        switchToIframeAPI(driver1);
        Boolean[] result = getAPIModerationState.apply(driver1);

        assertFalse(result[0], "Audio moderation must be initially off");
        assertFalse(result[1], "Video moderation must be initially off");

        // enable moderation and check
        TestUtils.executeScript(driver1,
            "return window.jitsiAPI.executeCommand('toggleModeration', true);");
        TestUtils.executeScript(driver1,
            "return window.jitsiAPI.executeCommand('toggleModeration', true, 'video');");

        TestUtils.waitForCondition(driver1, 5, (ExpectedCondition<Boolean>) d -> {
            Boolean[] res = getAPIModerationState.apply(d);

            // Audio and Video moderation must be on
            return res[0] && res[1];
        });

        // let's revert to the initial state

        // disable moderation
        TestUtils.executeScript(driver1,
            "return window.jitsiAPI.executeCommand('toggleModeration', false);");
        TestUtils.executeScript(driver1,
            "return window.jitsiAPI.executeCommand('toggleModeration', false, 'video');");

        // wait for the change to take effect
        TestUtils.waitForCondition(driver1, 5, (ExpectedCondition<Boolean>) d -> {
            Boolean[] res = getAPIModerationState.apply(d);

            // Audio and Video moderation must be on
            return !res[0] && !res[1];
        });
    }

    /**
     * Functions testing:
     * https://jitsi.github.io/handbook/docs/dev-guide/dev-guide-iframe#isparticipantforcemuted
     *
     * Commands testing:
     * https://jitsi.github.io/handbook/docs/dev-guide/dev-guide-iframe#askToUnmute
     * https://jitsi.github.io/handbook/docs/dev-guide/dev-guide-iframe#approveVideo
     * https://jitsi.github.io/handbook/docs/dev-guide/dev-guide-iframe#rejectParticipant
     *
     * Events testing:
     * https://jitsi.github.io/handbook/docs/dev-guide/dev-guide-iframe#moderationparticipantapproved
     * https://jitsi.github.io/handbook/docs/dev-guide/dev-guide-iframe#moderationparticipantrejected
     *
     * Test retrieving local audio/video muted state.
     */
    @Test(dependsOnMethods = { "testFunctionIsModerationOn" })
    public void testFunctionIsParticipantForceMuted()
    {
        hangUpAllParticipants();
        this.iFrameUrl = getIFrameUrl(null, null);
        ensureTwoParticipants(this.iFrameUrl, null);

        WebParticipant participant1 = getParticipant1();
        WebParticipant participant2 = getParticipant2();
        String endpointId2 = participant2.getEndpointId();
        WebDriver driver1 = participant1.getDriver();

        try
        {
            TestUtils.waitForCondition(participant1.getDriver(), 2,
                (ExpectedCondition<Boolean>) d -> !(participant1.isModerator() && participant2.isModerator()));
        }
        catch(TimeoutException e)
        {
            cleanupClass();
            throw new SkipException("Skipping as all participants are moderators.");
        }

        Function<WebDriver, Boolean[]> getAPIParticipantForceMutedState = d -> {
            TestUtils.executeScript(d, String.format(
                "return window.jitsiAPI.isParticipantForceMuted('%s')" +
                    ".then(r => window.jitsiAPI.testisAudioForceMuted = r);",
                endpointId2));
            TestUtils.executeScript(d, String.format(
                "return window.jitsiAPI.isParticipantForceMuted('%s', 'video')" +
                    ".then(r => window.jitsiAPI.testisVideoForceMuted = r);",
                endpointId2));

            boolean result1 = TestUtils.executeScriptAndReturnBoolean(d,
                "return window.jitsiAPI.testisAudioForceMuted;");
            boolean result2 = TestUtils.executeScriptAndReturnBoolean(d,
                "return window.jitsiAPI.testisVideoForceMuted;");

            return new Boolean[] { result1, result2 };
        };

        switchToIframeAPI(driver1);
        Boolean[] result = getAPIParticipantForceMutedState.apply(driver1);

        assertFalse(result[0], "Participant should not be audio force muted initially");
        assertFalse(result[1], "Participant should not be video force muted initially");

        // enable audio and video moderation
        TestUtils.executeScript(driver1,
            "return window.jitsiAPI.executeCommand('toggleModeration', true);");
        TestUtils.executeScript(driver1,
            "return window.jitsiAPI.executeCommand('toggleModeration', true, 'video');");

        TestUtils.waitForCondition(driver1, 5, (ExpectedCondition<Boolean>) d -> {
            Boolean[] res = getAPIParticipantForceMutedState.apply(d);

            // Participant should be audio and video force muted
            return res[0] && res[1];
        });

        // ask to unmute (approve audio) & approve video
        TestUtils.executeScript(driver1, String.format(
            "return window.jitsiAPI.executeCommand('askToUnmute', '%s');", endpointId2));
        TestUtils.executeScript(driver1, String.format(
            "return window.jitsiAPI.executeCommand('approveVideo', '%s');", endpointId2));

        TestUtils.waitForCondition(driver1, 5, (ExpectedCondition<Boolean>) d -> {
            Boolean[] res = getAPIParticipantForceMutedState.apply(d);

            // Participant should not be audio and video force muted
            return !res[0] && !res[1];
        });

        // reject audio & video
        TestUtils.executeScript(driver1, String.format(
            "return window.jitsiAPI.executeCommand('rejectParticipant', '%s');", endpointId2));
        TestUtils.executeScript(driver1, String.format(
            "return window.jitsiAPI.executeCommand('rejectParticipant', '%s', 'video');", endpointId2));

        TestUtils.waitForCondition(driver1, 5, (ExpectedCondition<Boolean>) d -> {
            Boolean[] res = getAPIParticipantForceMutedState.apply(d);

            // Participant should be audio and video force muted
            return res[0] && res[1];
        });
        // let's revert to the initial state

        // disable audio and video moderation
        TestUtils.executeScript(driver1,
            "return window.jitsiAPI.executeCommand('toggleModeration', false);");
        TestUtils.executeScript(driver1,
            "return window.jitsiAPI.executeCommand('toggleModeration', false, 'video');");

        // end wait for the change
        TestUtils.waitForCondition(driver1, 5, (ExpectedCondition<Boolean>) d -> {
            Boolean[] res = getAPIParticipantForceMutedState.apply(d);

            // Participant should not be audio and video force muted
            return !res[0] && !res[1];
        });
    }
}
