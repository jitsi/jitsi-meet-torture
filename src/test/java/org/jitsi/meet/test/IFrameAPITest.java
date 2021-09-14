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
import org.jitsi.meet.test.base.*;
import org.jitsi.meet.test.pageobjects.web.*;
import org.jitsi.meet.test.util.*;
import org.jitsi.meet.test.web.*;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;
import org.testng.*;
import org.testng.annotations.*;

import java.net.*;
import java.util.*;
import java.util.function.*;
import java.util.logging.*;

import static org.testng.Assert.*;

/**
 * Test that loads a page using the iframe API to load a meeting.
 * Loads the meeting and we switch to that iframe and then run several
 * tests over it, to make sure iframe API is working fine.
 *
 * TODO:
 * Functions:
 * Need to compare two images for:
 *  https://jitsi.github.io/handbook/docs/dev-guide/dev-guide-iframe#capturelargevideoscreenshot
 * Events:
 * Need to be fixed: https://jitsi.github.io/handbook/docs/dev-guide/dev-guide-iframe#largevideochanged
 * TODO: https://jitsi.github.io/handbook/docs/dev-guide/dev-guide-iframe#participantrolechanged
 *
 * @author Damian Minkov
 */
public class IFrameAPITest
    extends WebTestBase
{
    /**
     * The url of the page implementing the iframe, mut be hosted on https location.
     * {@link src/test/resources/files/iframeAPITest.html}
     */
    @SuppressWarnings("JavadocReference")
    public static final String IFRAME_PAGE_PATH_PNAME = "org.jitsi.iframe.page_path";

    private static final String IFRAME_ROOM_NAME = "iframeAPITest.html";

    /**
     * An url for a page that loads the iframe API.
     */
    private static final String IFRAME_ROOM_PARAMS
        = "domain=%s&room=%s"
        // here goes the default settings, used in Participant join function
        + "&config=%s&interfaceConfig=%s"
        + "&userInfo=%s"
        + "&password=%s";

    /**
     * The url to be reused between tests.
     */
    private JitsiMeetUrl iFrameUrl;

    /**
     * JS to execute getting the number of participants.
     */
    private static final String APP_JS_PARTICIPANTS_COUNT = "return APP.conference._room.getParticipantCount();";

    private boolean isModeratorSupported = false;

    @Override
    public void setupClass()
    {
        super.setupClass();

        ensureOneParticipant();

        this.isModeratorSupported = getParticipant1().isModerator();
    }

    /**
     * Constructs an JitsiMeetUrl to be used with iframeAPI.
     * @return url that will load a meeting in an iframe.
     */
    private JitsiMeetUrl getIFrameUrl(JsonObject userInfo, String password)
    {
        String pagePath = System.getProperty(IFRAME_PAGE_PATH_PNAME);

        if (pagePath == null || pagePath.trim().length() == 0)
        {
            throw new SkipException("missing configuration");
        }

        // uses a custom join, so we can load the page with iframe api
        JitsiMeetUrl iFrameUrl = getJitsiMeetUrl().copy();
        String domain;
        try
        {
            domain = iFrameUrl.getHost();
        }
        catch (MalformedURLException e)
        {
            throw new RuntimeException(e);
        }

        JsonObject defaultParams = new JitsiMeetUrl().appendConfig(WebParticipant.DEFAULT_CONFIG, false)
            .getFragmentParamsAsJson();

        String roomParams = String.format(IFRAME_ROOM_PARAMS,
            domain,
            currentRoomName,
            defaultParams.get("config").toString(),
            defaultParams.get("interfaceConfig").toString(),
            userInfo != null ? userInfo : "",
            password != null ? password : "");

        // Override the server and the path part(which is s room name)
        iFrameUrl.setServerUrl(pagePath);
        iFrameUrl.setRoomName(IFRAME_ROOM_NAME);
        iFrameUrl.setRoomParameters(roomParams);
        iFrameUrl.setIframeToNavigateTo("jitsiConferenceFrame0");

        return iFrameUrl;
    }

    /**
     * Switches selenium so to be able to execute iframeAPI commands.
     * @param driver the driver to use.
     */
    private static void switchToIframeAPI(WebDriver driver)
    {
        driver.switchTo().defaultContent();
    }

    /**
     * Switches to the meeting inside the iframe, so we can execute UI tests.
     * @param iFrameUrl the iframe page URL.
     * @param driver the driver to use.
     */
    private static void switchToMeetContent(JitsiMeetUrl iFrameUrl, WebDriver driver)
    {
        if (iFrameUrl.getIframeToNavigateTo() != null)
        {
            // let's wait for switch to that iframe, so we can continue with regular tests
            WebDriverWait wait = new WebDriverWait(driver, 10);
            wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(
                By.id(iFrameUrl.getIframeToNavigateTo())));
        }
    }

    /**
     * Adds a listener to the iframe API.
     * @param driver the driver to use.
     * @param eventName the event name to add a listener for.
     */
    private static void addIframeAPIListener(WebDriver driver, String eventName)
    {
        TestUtils.executeScript(driver,
            "window.jitsiAPI.addListener('" + eventName + "', (evt) => {"
                + "    window.jitsiAPI.test." + eventName + " = evt;"
                + "});");
    }

    /**
     * Returns the result of an iframe API event.
     * @param driver the driver to use.
     * @param eventName the event name.
     * @return The value of the result as json object.
     */
    private static JsonObject getEventResult(WebDriver driver, String eventName)
    {
        String result = TestUtils.executeScriptAndReturnString(driver,
            "return JSON.stringify(window.jitsiAPI.test." + eventName + ");");

        return result == null ? null : new JsonParser().parse(result).getAsJsonObject();
    }

    /**
     * Opens the iframe and executes SwitchVideoTest and MuteTest.
     *
     * Includes test for audioAvailabilityChanged event.
     * https://jitsi.github.io/handbook/docs/dev-guide/dev-guide-iframe#audioavailabilitychanged
     * https://jitsi.github.io/handbook/docs/dev-guide/dev-guide-iframe#videoavailabilitychanged
     *
     * Event
     * https://jitsi.github.io/handbook/docs/dev-guide/dev-guide-iframe#participantjoined
     * https://jitsi.github.io/handbook/docs/dev-guide/dev-guide-iframe#participantleft
     * https://jitsi.github.io/handbook/docs/dev-guide/dev-guide-iframe#participantkickedout
     */
    @Test
    public void testIFrameAPI()
    {
        JitsiMeetUrl iFrameUrl = getIFrameUrl(null, null);

        ensureOneParticipant(iFrameUrl);
        WebDriver driver1 = getParticipant1().getDriver();

        // Tests event audioAvailabilityChanged
        switchToIframeAPI(driver1);
        assertTrue(getEventResult(driver1, "audioAvailabilityChanged").get("available").getAsBoolean() );
        assertTrue(getEventResult(driver1, "videoAvailabilityChanged").get("available").getAsBoolean() );
        addIframeAPIListener(driver1, "participantJoined");
        addIframeAPIListener(driver1, "participantLeft");
        addIframeAPIListener(driver1, "participantKickedOut");
        switchToMeetContent(iFrameUrl, driver1);

        ensureThreeParticipants(iFrameUrl, null, null);

        String endpointId2 = getParticipant2().getEndpointId();
        String endpointId3 = getParticipant3().getEndpointId();

        // testing event participantJoined
        switchToIframeAPI(driver1);
        JsonObject joinedEvent = getEventResult(driver1, "participantJoined");
        // we get the last participant joined stored
        assertEquals(joinedEvent.get("id").getAsString(), endpointId3);

        switchToMeetContent(iFrameUrl, driver1);

        SwitchVideoTest switchVideoTest = new SwitchVideoTest(this);
        switchVideoTest.participant1ClickOnLocalVideoAndTest();
        switchVideoTest.participant1ClickOnRemoteVideoAndTest();
        switchVideoTest.participant1UnpinRemoteVideoAndTest();
        switchVideoTest.participantClickOnLocalVideoAndTest();
        switchVideoTest.participantClickOnRemoteVideoAndTest();
        switchVideoTest.participant2UnpinRemoteVideo();

        MuteTest muteTest = new MuteTest(this);
        muteTest.muteParticipant1AndCheck();
        muteTest.muteParticipant2AndCheck();
        muteTest.muteParticipant3AndCheck(iFrameUrl, null, null);

        // test event participantLeft
        getParticipant2().hangUp();
        switchToIframeAPI(driver1);
        TestUtils.waitForCondition(driver1, 2, (ExpectedCondition<Boolean>) d ->
            getEventResult(driver1, "participantLeft").get("id").getAsString().equals(endpointId2));
        switchToMeetContent(iFrameUrl, driver1);

        // testing event participantKickedOut
        // FIXME seems participantKickedOut is not working
//        getParticipant1().getRemoteParticipantById(getParticipant3().getEndpointId()).kick();
//        switchToIframeAPI(driver1);
//        getEventResult(driver1, "participantKickedOut")
//        switchToMeetContent(iFrameUrl, driver1);

        hangUpAllParticipants();
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
    @Test(dependsOnMethods = { "testIFrameAPI" })
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
        JsonObject participantsInfo
            = new JsonParser().parse(s).getAsJsonArray().get(0).getAsJsonObject();

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

        // make sure we are not selecting local video so we mess up the pin tests later
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

        // unpin second so we clear the state for next in line for testing
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
     * Test setLargeVideoParticipant the function or command.
     * @param function whether to use the function, uses the command if false.
     */
    private void testSetLargeVideoParticipant(boolean function)
    {
        hangUpAllParticipants();

        this.iFrameUrl = getIFrameUrl(null, null);
        ensureThreeParticipants(this.iFrameUrl, null, null);

        WebParticipant participant1 = getParticipant1();
        WebParticipant participant2 = getParticipant2();
        WebParticipant participant3 = getParticipant3();
        WebDriver driver1 = participant1.getDriver();
        String endpoint2Id = participant2.getEndpointId();
        String endpoint3Id = participant3.getEndpointId();

        // selects third
        switchToIframeAPI(driver1);

        addIframeAPIListener(driver1, "dominantSpeakerChanged");

        String setLargeCommand;
        String setLocalLargeCommand;
        if (function)
        {
            setLargeCommand = "window.jitsiAPI.setLargeVideoParticipant('%s');";
            setLocalLargeCommand= "window.jitsiAPI.setLargeVideoParticipant();";
        }
        else
        {
            setLargeCommand = "window.jitsiAPI.executeCommand('setLargeVideoParticipant', '%s');";
            setLocalLargeCommand = "window.jitsiAPI.executeCommand('setLargeVideoParticipant');";
        }

        TestUtils.executeScript(driver1, String.format(setLargeCommand, endpoint3Id));

        // will check that third is on large now
        switchToMeetContent(this.iFrameUrl, driver1);
        MeetUIUtils.waitForLargeVideoSwitchToEndpoint(driver1, endpoint3Id);

        // we must not be in tile view
        // FIXME: Currently there is a bug in jitsi-meet ans using setLargeVideoParticipant
        // does not switch automatically yo stage view, when in grid view
        getParticipant1().getToolbar().clickTileViewButton();
        MeetUIUtils.waitForTileViewDisplay(participant1, false);

        // selects second
        switchToIframeAPI(driver1);
        TestUtils.executeScript(driver1, String.format(setLargeCommand, endpoint2Id));

        // will check that second is on large now
        switchToMeetContent(this.iFrameUrl, driver1);
        MeetUIUtils.waitForLargeVideoSwitchToEndpoint(driver1, endpoint2Id);

        // mute second and first
        participant2.getToolbar().clickAudioMuteButton();
        participant1.getToolbar().clickAudioMuteButton();
        participant1.getFilmstrip().assertAudioMuteIcon(participant2, true);
        participant1.getFilmstrip().assertAudioMuteIcon(participant1, true);
        participant2.getFilmstrip().assertAudioMuteIcon(participant1, true);
        participant2.getFilmstrip().assertAudioMuteIcon(participant2, true);
        participant3.getFilmstrip().assertAudioMuteIcon(participant1, true);
        participant3.getFilmstrip().assertAudioMuteIcon(participant2, true);

        // only the third is unmuted
        MeetUIUtils.waitForDominantspeaker(driver1, endpoint3Id);

        switchToIframeAPI(driver1);
        TestUtils.executeScript(driver1, setLocalLargeCommand);

        // will check that third - the dominant speaker is on large now
        switchToMeetContent(this.iFrameUrl, driver1);
        MeetUIUtils.waitForLargeVideoSwitchToEndpoint(driver1, endpoint3Id);

        switchToIframeAPI(driver1);

        TestUtils.waitForCondition(driver1, 5, (ExpectedCondition<Boolean>) d -> {
                JsonObject eventData = getEventResult(d, "dominantSpeakerChanged");
                return eventData != null && eventData.get("id").getAsString().equals(endpoint3Id);
        });

        switchToMeetContent(this.iFrameUrl, driver1);

        // let's unmute everyone as it was initially
        participant2.getToolbar().clickAudioMuteButton();
        participant1.getToolbar().clickAudioMuteButton();
        participant2.getFilmstrip().assertAudioMuteIcon(participant1, false);
        participant1.getFilmstrip().assertAudioMuteIcon(participant2, false);
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
        participant2.getFilmstrip().assertVideoMuteIcon(participant1, true);

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
        participant2.getFilmstrip().assertVideoMuteIcon(participant1, false);
    }

    /**
     * Commands testing:
     * https://jitsi.github.io/handbook/docs/dev-guide/dev-guide-iframe#displayname
     * https://jitsi.github.io/handbook/docs/dev-guide/dev-guide-iframe#email
     * Event:
     * https://jitsi.github.io/handbook/docs/dev-guide/dev-guide-iframe#displaynamechange
     * https://jitsi.github.io/handbook/docs/dev-guide/dev-guide-iframe#emailchange
     *
     * Test command displayName and email.
     */
    @Test(dependsOnMethods = { "testFunctionIsAudioOrVideoMuted" })
    public void testCommandDisplayName()
    {
        this.iFrameUrl = getIFrameUrl(null, null);
        ensureTwoParticipants(this.iFrameUrl, null);

        String newName = "P1 New Name-" + (int) (Math.random() * 1_000_000);
        String newEmail = "example@example.com";

        WebParticipant participant1 = getParticipant1();
        WebParticipant participant2 = getParticipant2();
        WebDriver driver1 = participant1.getDriver();
        String endpointId2 = participant2.getEndpointId();

        switchToIframeAPI(driver1);
        TestUtils.executeScript(driver1,
            "return window.jitsiAPI.executeCommand('displayName', '" + newName + "');");
        TestUtils.executeScript(driver1,
            "return window.jitsiAPI.executeCommand('email', '" + newEmail + "');");

        addIframeAPIListener(driver1, "displayNameChange");
        addIframeAPIListener(driver1, "emailChange");

        switchToMeetContent(this.iFrameUrl, driver1);
        participant1.getToolbar().clickSettingsButton();
        SettingsDialog settingsDialog = participant1.getSettingsDialog();
        settingsDialog.waitForDisplay();
        assertEquals(settingsDialog.getDisplayName(), newName);
        assertEquals(settingsDialog.getEmail(), newEmail);

        String newName2 = "P2 New Name-" + (int) (Math.random() * 1_000_000);
        participant2.setDisplayName(newName2);

        switchToIframeAPI(driver1);

        TestUtils.waitForCondition(driver1, 5, (ExpectedCondition<Boolean>) d -> {
                JsonObject displayNameEvent = getEventResult(d, "displayNameChange");
                return displayNameEvent != null && displayNameEvent.get("displayname").getAsString().equals(newName2)
                    && displayNameEvent.get("id").getAsString().equals(endpointId2);
            });

        // FIXME: this seems to be broken in jitsi-meet
//        String newEmail2 = "example2@example.com";
//        participant2.getToolbar().clickSettingsButton();
//        SettingsDialog settingsDialog2 = participant2.getSettingsDialog();
//        settingsDialog2.waitForDisplay();
//        settingsDialog2.setEmail(newEmail2);
//        settingsDialog2.submit();;
//
//        TestUtils.waitForCondition(driver1, 5, (ExpectedCondition<Boolean>) d -> {
//            JsonObject emailEvent = getEventResult(d, "emailChange");
//            return emailEvent.get("id").getAsString().equals(endpointId2)
//                && emailEvent.get("email").getAsString().equals(newEmail2);
//        });

        switchToMeetContent(this.iFrameUrl, driver1);
    }

    /**
     * Commands testing:
     * https://jitsi.github.io/handbook/docs/dev-guide/dev-guide-iframe#password
     * Event:
     * https://jitsi.github.io/handbook/docs/dev-guide/dev-guide-iframe#passwordrequired (this is in the html impl)
     *
     * Test command password.
     */
    @Test(dependsOnMethods = { "testCommandDisplayName" })
    public void testCommandPassword()
    {
        if (!this.isModeratorSupported)
        {
            throw new SkipException("Moderation is required for this test.");
        }

        hangUpAllParticipants();

        this.iFrameUrl = getIFrameUrl(null, null);

        ensureOneParticipant(this.iFrameUrl);

        WebParticipant participant1 = getParticipant1();
        WebDriver driver1 = participant1.getDriver();

        switchToIframeAPI(driver1);

        TestUtils.waitForCondition(driver1, 5, (ExpectedCondition<Boolean>) d ->
            TestUtils.executeScriptAndReturnBoolean(driver1,
                "return window.jitsiAPI.test.isModerator;"));

        String randomPassword = "TestPassword" + (int) (Math.random() * 1_000_000);

        TestUtils.executeScript(driver1,
            "window.jitsiAPI.executeCommand('password', '" + randomPassword + "');");

        switchToMeetContent(this.iFrameUrl, driver1);

        ensureTwoParticipants(this.iFrameUrl, getIFrameUrl(null, randomPassword));
    }

    /**
     * Commands testing:
     * https://jitsi.github.io/handbook/docs/dev-guide/dev-guide-iframe#togglelobby
     *
     * Test command toggleLobby.
     */
    @Test(dependsOnMethods = { "testCommandPassword" })
    public void testCommandToggleLobby()
    {
        if (!this.isModeratorSupported)
        {
            throw new SkipException("Moderation is required for this test.");
        }

        hangUpAllParticipants();

        this.iFrameUrl = getIFrameUrl(null, null);

        ensureOneParticipant(this.iFrameUrl);

        WebParticipant participant1 = getParticipant1();
        WebDriver driver1 = participant1.getDriver();

        switchToIframeAPI(driver1);

        TestUtils.waitForCondition(driver1, 5, (ExpectedCondition<Boolean>) d ->
            TestUtils.executeScriptAndReturnBoolean(driver1,
                "return window.jitsiAPI.test.isModerator;"));

        TestUtils.executeScript(driver1,
            "window.jitsiAPI.executeCommand('toggleLobby', true);");

        switchToMeetContent(this.iFrameUrl, driver1);

        joinSecondParticipant();

        WebParticipant participant2 = getParticipant2();
        LobbyScreen lobbyScreen = participant2.getLobbyScreen();

        // participant 2 should be now on pre-join screen
        lobbyScreen.waitForLoading();
    }

    /**
     * Commands testing:
     * https://jitsi.github.io/handbook/docs/dev-guide/dev-guide-iframe#startsharevideo
     * https://jitsi.github.io/handbook/docs/dev-guide/dev-guide-iframe#stopsharevideo
     *
     * Test command startShareVideo & stopShareVideo.
     */
    @Test(dependsOnMethods = { "testCommandToggleLobby" })
    public void testCommandStartStopShareVideo()
    {
        hangUpAllParticipants();

        this.iFrameUrl = getIFrameUrl(null, null);
        ensureTwoParticipants(this.iFrameUrl, null);

        WebParticipant participant1 = getParticipant1();
        WebParticipant participant2 = getParticipant2();
        WebDriver driver1 = participant1.getDriver();
        WebDriver driver2 = participant2.getDriver();

        switchToIframeAPI(driver1);

        TestUtils.executeScript(driver1,
            "window.jitsiAPI.executeCommand('startShareVideo', '" + SharedVideoTest.V2_LINK + "');");

        switchToMeetContent(this.iFrameUrl, driver1);

        TestUtils.waitForDisplayedElementByID(
            driver1,
            "sharedVideoPlayer",
            10);

        // Now let's check the second participant state
        // make sure we are in meet, not in the frame
        driver2.switchTo().defaultContent();
        TestUtils.waitForDisplayedElementByID(
            driver2,
            "sharedVideoPlayer",
            10);

        switchToIframeAPI(driver1);

        TestUtils.executeScript(driver1,
            "window.jitsiAPI.executeCommand('stopShareVideo');");

        switchToMeetContent(this.iFrameUrl, driver1);

        try
        {
            TestUtils.waitForNotDisplayedElementByID(
                driver1, "sharedVideoPlayer", 5);
        }
        catch (StaleElementReferenceException ex)
        {
            // if the element is detached in a process of checking its display
            // status, means it is not visible anymore
        }
        try
        {
            TestUtils.waitForNotDisplayedElementByID(
                driver2, "sharedVideoPlayer", 5);
        }
        catch (StaleElementReferenceException ex)
        {
            // if the element is detached in a process of checking its display
            // status, means it is not visible anymore
        }
    }

    /**
     * Commands testing:
     * https://jitsi.github.io/handbook/docs/dev-guide/dev-guide-iframe#subject
     * Event:
     * https://jitsi.github.io/handbook/docs/dev-guide/dev-guide-iframe#subjectchange
     *
     * Test command subject.
     */
    @Test(dependsOnMethods = { "testCommandStartStopShareVideo" })
    public void testCommandSubject()
    {
        if (!this.isModeratorSupported)
        {
            throw new SkipException("Moderation is required for this test.");
        }

        this.iFrameUrl = getIFrameUrl(null, null);
        ensureTwoParticipants(this.iFrameUrl, null);

        WebParticipant participant1 = getParticipant1();
        WebParticipant participant2 = getParticipant2();
        WebDriver driver1 = participant1.getDriver();
        WebDriver driver2 = participant2.getDriver();

        switchToIframeAPI(driver1);

        TestUtils.waitForCondition(driver1, 5, (ExpectedCondition<Boolean>) d ->
            TestUtils.executeScriptAndReturnBoolean(driver1,
                "return window.jitsiAPI.test.isModerator;"));

        addIframeAPIListener(driver1, "subjectChange");

        String randomSubject = "My Random Subject " + (int) (Math.random() * 1_000_000);
        TestUtils.executeScript(driver1,
            "window.jitsiAPI.executeCommand('subject', '" + randomSubject + "');");

        TestUtils.waitForCondition(driver1, 1, (ExpectedCondition<Boolean>) d ->{
            JsonObject result = getEventResult(driver1, "subjectChange");

            return result != null && result.get("subject").getAsString().equals(randomSubject);
        });

        switchToMeetContent(this.iFrameUrl, driver1);

        String subjectXpath = "//div[@id='subject-details-container']//span[contains(@class,'subject-text')]";
        TestUtils.waitForDisplayedElementByXPath(
            driver1,
            subjectXpath,
            10);

        assertEquals(driver1.findElement(By.xpath(subjectXpath)).getText(), randomSubject);

        TestUtils.waitForDisplayedElementByXPath(
            driver2,
            subjectXpath,
            10);

        assertEquals(driver2.findElement(By.xpath(subjectXpath)).getText(), randomSubject);
    }

    /**
     * Commands testing:
     * https://jitsi.github.io/handbook/docs/dev-guide/dev-guide-iframe#togglefilmstrip
     * Event:
     * https://jitsi.github.io/handbook/docs/dev-guide/dev-guide-iframe#filmstripdisplaychanged
     *
     * Test command toggleFilmStrip.
     */
    @Test(dependsOnMethods = { "testCommandSubject" })
    public void testCommandToggleFilmStrip()
    {
        hangUpAllParticipants();

        this.iFrameUrl = getIFrameUrl(null, null);
        ensureOneParticipant(this.iFrameUrl, null);

        WebParticipant participant1 = getParticipant1();
        WebDriver driver1 = participant1.getDriver();

        switchToIframeAPI(driver1);

        addIframeAPIListener(driver1, "filmstripDisplayChanged");

        TestUtils.executeScript(driver1,
            "window.jitsiAPI.executeCommand('toggleFilmStrip');");

        switchToMeetContent(this.iFrameUrl, driver1);

        // The xpath for the local video element.
        String LOCAL_VIDEO_XPATH = "//span[@id='localVideoWrapper']";

        TestUtils.waitForNotDisplayedElementByXPath(
            driver1,
            LOCAL_VIDEO_XPATH,
            5);

        switchToIframeAPI(driver1);

        assertFalse(getEventResult(driver1, "filmstripDisplayChanged").get("visible").getAsBoolean());

        TestUtils.executeScript(driver1,
            "window.jitsiAPI.executeCommand('toggleFilmStrip');");

        switchToMeetContent(this.iFrameUrl, driver1);

        TestUtils.waitForDisplayedElementByXPath(
            driver1,
            LOCAL_VIDEO_XPATH,
            5);

        switchToIframeAPI(driver1);
        assertTrue(getEventResult(driver1, "filmstripDisplayChanged").get("visible").getAsBoolean());

        switchToMeetContent(this.iFrameUrl, driver1);
    }

    /**
     * Commands testing:
     * https://jitsi.github.io/handbook/docs/dev-guide/dev-guide-iframe#togglechat
     * Event:
     * https://jitsi.github.io/handbook/docs/dev-guide/dev-guide-iframe#chatupdated
     *
     * Test command toggleChat.
     */
    @Test(dependsOnMethods = { "testCommandToggleFilmStrip" })
    public void testCommandToggleChat()
    {
        this.iFrameUrl = getIFrameUrl(null, null);
        ensureOneParticipant(this.iFrameUrl, null);

        WebParticipant participant1 = getParticipant1();
        WebDriver driver1 = participant1.getDriver();

        switchToIframeAPI(driver1);

        addIframeAPIListener(driver1, "chatUpdated");

        TestUtils.executeScript(driver1,
            "window.jitsiAPI.executeCommand('toggleChat');");

        TestUtils.waitForCondition(driver1, 1, (ExpectedCondition<Boolean>) d ->
            getEventResult(d, "chatUpdated") != null);

        assertTrue(getEventResult(driver1, "chatUpdated").get("isOpen").getAsBoolean());

        switchToMeetContent(this.iFrameUrl, driver1);

        participant1.getChatPanel().assertOpen();

        switchToIframeAPI(driver1);

        TestUtils.executeScript(driver1,
            "window.jitsiAPI.executeCommand('toggleChat');");

        switchToMeetContent(this.iFrameUrl, driver1);

        participant1.getChatPanel().assertClosed();

        switchToIframeAPI(driver1);
        assertFalse(getEventResult(driver1, "chatUpdated").get("isOpen").getAsBoolean());
        switchToMeetContent(this.iFrameUrl, driver1);
    }

    /**
     * Commands testing:
     * https://jitsi.github.io/handbook/docs/dev-guide/dev-guide-iframe#toggleraisehand
     * Event:
     * https://jitsi.github.io/handbook/docs/dev-guide/dev-guide-iframe#raisehandupdated
     *
     * Test command toggleRaiseHand.
     */
    @Test(dependsOnMethods = { "testCommandToggleChat" })
    public void testCommandToggleRaiseHand()
    {
        this.iFrameUrl = getIFrameUrl(null, null);
        ensureTwoParticipants(this.iFrameUrl, null);

        WebParticipant participant1 = getParticipant1();
        WebParticipant participant2 = getParticipant2();
        WebDriver driver1 = participant1.getDriver();
        WebDriver driver2 = participant2.getDriver();
        String endpointId1 = participant1.getEndpointId();
        String endpointId2 = participant2.getEndpointId();

        Logger logger = Logger.getGlobal();
        logger.log(Level.INFO, "EndpointId 1:" + endpointId1);
        logger.log(Level.INFO, "EndpointId 2:" + endpointId2);

        switchToIframeAPI(driver1);

        addIframeAPIListener(driver1, "raiseHandUpdated");

        TestUtils.executeScript(driver1,
            "window.jitsiAPI.executeCommand('toggleRaiseHand');");

        switchToMeetContent(this.iFrameUrl, driver1);

        TestUtils.waitForCondition(driver2, 5, (ExpectedCondition<Boolean>) d ->
            participant2.getNotifications().hasRaisedHandNotification());

        switchToIframeAPI(driver1);

        JsonObject event = getEventResult(driver1, "raiseHandUpdated");
        assertEquals(event.get("id").getAsString(), endpointId1);
        assertTrue(event.get("handRaised").getAsBoolean());

        TestUtils.executeScript(driver1,
            "window.jitsiAPI.executeCommand('toggleRaiseHand');");

        switchToMeetContent(this.iFrameUrl, driver1);

        TestUtils.waitForCondition(driver2, 5, (ExpectedCondition<Boolean>) d ->
            !participant2.getNotifications().hasRaisedHandNotification());

        switchToIframeAPI(driver1);

        event = getEventResult(driver1, "raiseHandUpdated");
        assertEquals(event.get("id").getAsString(), endpointId1);
        assertFalse(event.get("handRaised").getAsBoolean());

        switchToMeetContent(this.iFrameUrl, driver1);

        participant2.getToolbar().clickRaiseHandButton();
        TestUtils.waitForCondition(driver1, 5, (ExpectedCondition<Boolean>) d ->
            participant1.getNotifications().hasRaisedHandNotification());

        switchToIframeAPI(driver1);

        event = getEventResult(driver1, "raiseHandUpdated");
        assertEquals(event.get("id").getAsString(), endpointId2);
        assertTrue(event.get("handRaised").getAsBoolean());

        switchToMeetContent(this.iFrameUrl, driver1);

        participant2.getToolbar().clickRaiseHandButton();
        TestUtils.waitForCondition(driver1, 5, (ExpectedCondition<Boolean>) d ->
            !participant1.getNotifications().hasRaisedHandNotification());

        switchToIframeAPI(driver1);

        TestUtils.waitForCondition(driver1, 5, (ExpectedCondition<Boolean>) d -> {
            JsonObject e = getEventResult(d, "raiseHandUpdated");
            boolean result = e.get("id").getAsString().equals(endpointId2)
                && !e.get("handRaised").getAsBoolean();

            if (!result)
            {
                Logger.getGlobal().log(Level.WARNING, "No raise hand event:" + e);
            }

            return result;
        });

        switchToMeetContent(this.iFrameUrl, driver1);
    }

    /**
     * Commands testing:
     * https://jitsi.github.io/handbook/docs/dev-guide/dev-guide-iframe#togglesharescreen
     * Function
     * https://jitsi.github.io/handbook/docs/dev-guide/dev-guide-iframe#getcontentsharingparticipants
     * Event:
     * https://jitsi.github.io/handbook/docs/dev-guide/dev-guide-iframe#screensharingstatuschanged
     * https://jitsi.github.io/handbook/docs/dev-guide/dev-guide-iframe#contentsharingparticipantschanged
     *
     * Test command toggleShareScreen and getContentSharingParticipants.
     */
    @Test(dependsOnMethods = { "testCommandToggleRaiseHand" })
    public void testCommandToggleShareScreen()
    {
        this.iFrameUrl = getIFrameUrl(null, null);
        ensureTwoParticipants(this.iFrameUrl, null);

        WebParticipant participant1 = getParticipant1();
        String endpointId1 = participant1.getEndpointId();
        WebParticipant participant2 = getParticipant2();
        String endpointId2 = participant2.getEndpointId();
        WebDriver driver1 = participant1.getDriver();

        switchToIframeAPI(driver1);

        addIframeAPIListener(driver1, "contentSharingParticipantsChanged");
        addIframeAPIListener(driver1, "screenSharingStatusChanged");

        TestUtils.executeScript(driver1,
            "window.jitsiAPI.executeCommand('toggleShareScreen');");

        switchToMeetContent(this.iFrameUrl, driver1);

        DesktopSharingTest.testDesktopSharingInPresence(participant2, participant1, "desktop");

        switchToIframeAPI(driver1);

        TestUtils.executeScript(driver1,
            "window.jitsiAPI.getContentSharingParticipants()" +
                ".then(res => window.jitsiAPI.test.getContentSharing1 = JSON.stringify(res.sharingParticipantIds));");

        TestUtils.waitForCondition(driver1, 3, (ExpectedCondition<Boolean>) d ->
            TestUtils.executeScriptAndReturnBoolean(driver1,
                "return window.jitsiAPI.test.getContentSharing1 !== undefined;"));

        String res = TestUtils.executeScriptAndReturnString(driver1,
            "return window.jitsiAPI.test.getContentSharing1;");
        assertNotNull(res);
        assertEquals(new JsonParser().parse(res).getAsJsonArray().get(0).getAsString(), endpointId1);

        JsonObject sharingData = getEventResult(driver1, "screenSharingStatusChanged");
        assertTrue(sharingData.get("on").getAsBoolean(), "Screen sharing mst be on");

        TestUtils.executeScript(driver1,
            "window.jitsiAPI.executeCommand('toggleShareScreen');");

        switchToMeetContent(this.iFrameUrl, driver1);

        DesktopSharingTest.testDesktopSharingInPresence(participant2, participant1, "camera");

        ensureThreeParticipants(this.iFrameUrl, null, null);

        WebParticipant participant3 = getParticipant3();
        String endpointId3 = participant3.getEndpointId();

        participant2.getToolbar().clickDesktopSharingButton();
        participant3.getToolbar().clickDesktopSharingButton();
        DesktopSharingTest.testDesktopSharingInPresence(participant1, participant2, "desktop");
        DesktopSharingTest.testDesktopSharingInPresence(participant1, participant3, "desktop");

        switchToIframeAPI(driver1);

        sharingData = getEventResult(driver1, "screenSharingStatusChanged");
        assertFalse(sharingData.get("on").getAsBoolean(), "Screen sharing mst be on");

        TestUtils.executeScript(driver1,
            "window.jitsiAPI.getContentSharingParticipants()"
                + ".then(res => window.jitsiAPI.test.getContentSharing2 = res.sharingParticipantIds);");

        TestUtils.waitForCondition(driver1, 3, (ExpectedCondition<Boolean>) d ->
            TestUtils.executeScriptAndReturnString(driver1,
                "return JSON.stringify(window.jitsiAPI.test.getContentSharing2);") != null);

        res = TestUtils.executeScriptAndReturnString(driver1,
            "return JSON.stringify(window.jitsiAPI.test.getContentSharing2);");
        assertNotNull(res);

        List<String> resultIds = new ArrayList<>();
        new JsonParser().parse(res).getAsJsonArray().forEach(el -> resultIds.add(el.getAsString()));

        assertTrue(resultIds.contains(endpointId2));
        assertTrue(resultIds.contains(endpointId3));

        resultIds.clear();
        getEventResult(driver1, "contentSharingParticipantsChanged").get("data").getAsJsonArray()
            .forEach(el -> resultIds.add(el.getAsString()));
        assertTrue(resultIds.contains(endpointId2));
        assertTrue(resultIds.contains(endpointId3));

        switchToMeetContent(this.iFrameUrl, driver1);
    }

    /**
     * Commands testing:
     * https://jitsi.github.io/handbook/docs/dev-guide/dev-guide-iframe#toggletileview
     *
     * Event:
     * https://jitsi.github.io/handbook/docs/dev-guide/dev-guide-iframe#tileviewchanged
     *
     * Test command toggleTileView.
     */
    @Test(dependsOnMethods = { "testCommandToggleShareScreen" })
    public void testCommandToggleTileView()
    {
        this.iFrameUrl = getIFrameUrl(null, null);
        ensureOneParticipant(this.iFrameUrl);

        WebParticipant participant1 = getParticipant1();
        WebDriver driver1 = participant1.getDriver();

        // one or two participants not in tile view
        MeetUIUtils.waitForTileViewDisplay(participant1, false);

        switchToIframeAPI(driver1);

        addIframeAPIListener(driver1, "tileViewChanged");

        TestUtils.executeScript(driver1,
            "window.jitsiAPI.executeCommand('toggleTileView');");

        switchToMeetContent(this.iFrameUrl, driver1);

        MeetUIUtils.waitForTileViewDisplay(participant1, true);

        switchToIframeAPI(driver1);

        assertTrue(getEventResult(driver1, "tileViewChanged").get("enabled").getAsBoolean());

        TestUtils.executeScript(driver1,
            "window.jitsiAPI.executeCommand('toggleTileView');");

        switchToMeetContent(this.iFrameUrl, driver1);

        MeetUIUtils.waitForTileViewDisplay(participant1, false);

        switchToIframeAPI(driver1);

        assertFalse(getEventResult(driver1, "tileViewChanged").get("enabled").getAsBoolean());

        switchToMeetContent(this.iFrameUrl, driver1);
    }

    /**
     * Commands testing:
     * https://jitsi.github.io/handbook/docs/dev-guide/dev-guide-iframe#hangup
     * Event:
     * https://jitsi.github.io/handbook/docs/dev-guide/dev-guide-iframe#videoconferenceleft
     *
     * Test command hangup.
     */
    @Test(dependsOnMethods = { "testCommandToggleTileView" })
    public void testCommandHangup()
    {
        this.iFrameUrl = getIFrameUrl(null, null);
        ensureTwoParticipants(this.iFrameUrl, null);

        WebParticipant participant1 = getParticipant1();
        WebDriver driver1 = participant1.getDriver();
        WebDriver driver2 = getParticipant2().getDriver();

        double numOfParticipants1 = TestUtils.executeScriptAndReturnDouble(driver1, APP_JS_PARTICIPANTS_COUNT);
        assertEquals(numOfParticipants1, 2d);
        double numOfParticipants2 = TestUtils.executeScriptAndReturnDouble(driver2, APP_JS_PARTICIPANTS_COUNT);
        assertEquals(numOfParticipants2, 2d);

        switchToIframeAPI(driver1);

        addIframeAPIListener(driver1, "videoConferenceLeft");

        TestUtils.executeScript(driver1,
            "window.jitsiAPI.executeCommand('hangup');");

        switchToMeetContent(this.iFrameUrl, driver1);

        TestUtils.waitForCondition(driver2, 3, (ExpectedCondition<Boolean>) d ->
            TestUtils.executeScriptAndReturnDouble(driver2, APP_JS_PARTICIPANTS_COUNT) == 1d);

        switchToIframeAPI(driver1);
        assertEquals(
            getEventResult(driver1, "videoConferenceLeft").get("roomName").getAsString(),
            getJitsiMeetUrl().getRoomName());
        switchToMeetContent(this.iFrameUrl, driver1);

        hangUpAllParticipants();
    }

    /**
     * Commands testing:
     * https://jitsi.github.io/handbook/docs/dev-guide/dev-guide-iframe#avatarurl
     * Event:
     * https://jitsi.github.io/handbook/docs/dev-guide/dev-guide-iframe#avatarchanged
     *
     * Test command avatarUrl.
     */
    @Test(dependsOnMethods = { "testCommandHangup" })
    public void testCommandAvatarUrl()
    {
        this.iFrameUrl = getIFrameUrl(null, null);
        ensureTwoParticipants(this.iFrameUrl, null);

        WebParticipant participant1 = getParticipant1();
        WebDriver driver1 = participant1.getDriver();
        String endpointId1 = participant1.getEndpointId();
        WebDriver driver2 = getParticipant2().getDriver();

        switchToIframeAPI(driver1);

        String avatarUrl = "https://avatars0.githubusercontent.com/u/3671647";

        addIframeAPIListener(driver1, "avatarChanged");

        TestUtils.executeScript(driver1,
            "window.jitsiAPI.executeCommand('avatarUrl', '" + avatarUrl + "');");

        switchToMeetContent(this.iFrameUrl, driver1);

        TestUtils.waitForCondition(driver2, 2, (ExpectedCondition<Boolean>) d ->
            AvatarTest.getLocalThumbnailSrc(driver1).equals(avatarUrl));

        assertEquals(AvatarTest.getThumbnailSrc(driver2, endpointId1), avatarUrl);

        switchToIframeAPI(driver1);

        // waits for the event
        TestUtils.waitForCondition(driver2, 5, (ExpectedCondition<Boolean>) d ->
            getEventResult(driver1, "avatarChanged").get("avatarURL").getAsString().equals(avatarUrl));

        JsonObject resObj = getEventResult(driver1, "avatarChanged");

        assertEquals(resObj.get("id").getAsString(), endpointId1);
        assertEquals(resObj.get("avatarURL").getAsString(), avatarUrl);

        switchToMeetContent(this.iFrameUrl, driver1);
    }

    /**
     * Commands testing:
     * https://jitsi.github.io/handbook/docs/dev-guide/dev-guide-iframe#sendendpointtextmessage
     * Event:
     * https://jitsi.github.io/handbook/docs/dev-guide/dev-guide-iframe#endpointtextmessagereceived
     *
     * Test command sendEndpointTextMessage.
     */
    @Test(dependsOnMethods = { "testCommandAvatarUrl" })
    public void testCommandSendEndpointTextMessage()
    {
        this.iFrameUrl = getIFrameUrl(null, null);
        ensureTwoParticipants(this.iFrameUrl, null);

        WebParticipant participant1 = getParticipant1();
        WebDriver driver1 = participant1.getDriver();
        WebParticipant participant2 = getParticipant2();
        WebDriver driver2 = participant2.getDriver();
        String endpointId1 = participant1.getEndpointId();
        String endpointId2 = participant2.getEndpointId();
        String participant2Jid
            = TestUtils.executeScriptAndReturnString(driver2, "return APP.conference._room.room.myroomjid;");

        // adds the listener at the receiver
        TestUtils.executeScript(driver2,
            "APP.conference._room.on('conference.endpoint_message_received', "
                + "(p, m) => window.testEndpointCommandTxt = m.text);");

        switchToIframeAPI(driver1);

        String testMessage = "this is a test message";

        TestUtils.executeScript(driver1,
            "window.jitsiAPI.executeCommand('sendEndpointTextMessage', '" + endpointId2 + "', '" + testMessage + "');");

        switchToMeetContent(this.iFrameUrl, driver1);

        TestUtils.waitForCondition(driver2, 5, (ExpectedCondition<Boolean>) d -> {
                String resultMsg = TestUtils.executeScriptAndReturnString(driver2,
                    "return window.testEndpointCommandTxt;");

                return resultMsg != null && resultMsg.equals(testMessage);
            });

        switchToIframeAPI(driver1);

        addIframeAPIListener(driver1, "endpointTextMessageReceived");

        String testMessage2 = testMessage + (int) (Math.random() * 1_000_000);
        TestUtils.executeScript(driver2,
            "APP.conference.sendEndpointMessage("
                + "'" + endpointId1 + "', { name: 'endpoint-text-message', text: '" + testMessage2 + "' });");

        TestUtils.waitForCondition(driver1, 5,
            (ExpectedCondition<Boolean>) d -> getEventResult(d, "endpointTextMessageReceived") != null);

        JsonObject data = getEventResult(driver1, "endpointTextMessageReceived").get("data").getAsJsonObject();
        JsonObject senderInfo = data.get("senderInfo").getAsJsonObject();

        assertEquals(senderInfo.get("id").getAsString(), endpointId2);
        assertEquals(senderInfo.get("jid").getAsString(), participant2Jid);

        JsonObject eventData = data.get("eventData").getAsJsonObject();
        assertEquals(eventData.get("text").getAsString(), testMessage2);
        assertEquals(eventData.get("name").getAsString(), "endpoint-text-message");

        switchToMeetContent(this.iFrameUrl, driver1);
    }

    /**
     * Commands testing:
     * https://jitsi.github.io/handbook/docs/dev-guide/dev-guide-iframe#setlargevideoparticipant-1
     * Event:
     * https://jitsi.github.io/handbook/docs/dev-guide/dev-guide-iframe#dominantspeakerchanged
     *
     * Test command setLargeVideoParticipant.
     */
    @Test(dependsOnMethods = { "testCommandSendEndpointTextMessage" })
    public void testCommandSetLargeVideoParticipant()
    {
        testSetLargeVideoParticipant(false);
    }

    /**
     * Commands testing:
     * https://jitsi.github.io/handbook/docs/dev-guide/dev-guide-iframe#muteeveryone-1
     *
     * Test command muteEveryone.
     */
    @Test(dependsOnMethods = { "testCommandSetLargeVideoParticipant" })
    public void testCommandMuteEveryone()
    {
        if (!this.isModeratorSupported)
        {
            throw new SkipException("Moderation is required for this test.");
        }

        this.iFrameUrl = getIFrameUrl(null, null);
        ensureThreeParticipants(this.iFrameUrl, null, null);

        WebParticipant participant1 = getParticipant1();
        WebParticipant participant2 = getParticipant2();
        WebParticipant participant3 = getParticipant3();
        WebDriver driver1 = participant1.getDriver();

        // selects third
        switchToIframeAPI(driver1);

        TestUtils.waitForCondition(driver1, 5, (ExpectedCondition<Boolean>) d ->
            TestUtils.executeScriptAndReturnBoolean(driver1,
                "return window.jitsiAPI.test.isModerator;"));

        TestUtils.executeScript(driver1, "window.jitsiAPI.executeCommand('muteEveryone');");

        switchToMeetContent(this.iFrameUrl, driver1);

        participant2.getFilmstrip().assertAudioMuteIcon(participant3, true);
        participant3.getFilmstrip().assertAudioMuteIcon(participant2, true);
    }

    /**
     * Commands testing:
     * https://jitsi.github.io/handbook/docs/dev-guide/dev-guide-iframe#initiateprivatechat
     * https://jitsi.github.io/handbook/docs/dev-guide/dev-guide-iframe#cancelprivatechat
     *
     * Test command initiatePrivateChat & cancelPrivateChat.
     */
    @Test(dependsOnMethods = { "testCommandMuteEveryone" })
    public void testCommandInitiateCancelPrivateChat()
    {
        this.iFrameUrl = getIFrameUrl(null, null);
        ensureTwoParticipants(this.iFrameUrl, null);

        WebParticipant participant1 = getParticipant1();
        WebDriver driver1 = participant1.getDriver();
        String endpointId2 = getParticipant2().getEndpointId();

        participant1.setDisplayName("John Doe");

        switchToIframeAPI(driver1);

        TestUtils.executeScript(driver1,
            "window.jitsiAPI.executeCommand('initiatePrivateChat', '" + endpointId2 + "');");

        switchToMeetContent(this.iFrameUrl, driver1);

        TestUtils.waitForDisplayedElementByID(driver1, "chat-recipient", 3);
        assertTrue(driver1.findElement(By.xpath("//div[@id='chat-recipient']/span")).getText()
            .contains("Private message to"));

        switchToIframeAPI(driver1);

        TestUtils.executeScript(driver1,
            "window.jitsiAPI.executeCommand('cancelPrivateChat');");

        switchToMeetContent(this.iFrameUrl, driver1);

        TestUtils.waitForNotDisplayedElementByID(driver1, "chat-recipient", 3);
    }

    /**
     * Commands testing:
     * https://jitsi.github.io/handbook/docs/dev-guide/dev-guide-iframe#kickparticipant
     * Event:
     * https://jitsi.github.io/handbook/docs/dev-guide/dev-guide-iframe#participantkickedout
     *
     * Test command kickParticipant.
     */
    @Test(dependsOnMethods = { "testCommandInitiateCancelPrivateChat" })
    public void testCommandKickParticipant()
    {
        if (!this.isModeratorSupported)
        {
            throw new SkipException("Moderation is required for this test.");
        }

        this.iFrameUrl = getIFrameUrl(null, null);
        ensureTwoParticipants(this.iFrameUrl, null);

        WebParticipant participant1 = getParticipant1();
        WebDriver driver1 = participant1.getDriver();
        String endpointId2 = getParticipant2().getEndpointId();

        switchToIframeAPI(driver1);

        TestUtils.waitForCondition(driver1, 5, (ExpectedCondition<Boolean>) d ->
            TestUtils.executeScriptAndReturnBoolean(driver1,
                "return window.jitsiAPI.test.isModerator;"));

        addIframeAPIListener(driver1, "participantKickedOut");

        TestUtils.executeScript(driver1,
            "window.jitsiAPI.executeCommand('kickParticipant', '" + endpointId2 + "');");

        switchToMeetContent(this.iFrameUrl, driver1);

        participant1.waitForParticipants(0);

        // check that the kicked participant sees the notification
        assertTrue(
            getParticipant2().getNotifications().hasKickedNotification(),
            "The second participant should see a warning that was kicked.");

//        switchToIframeAPI(driver1);
//         FIXME getEventResult(driver1, "participantKickedOut")
//        switchToMeetContent(this.iFrameUrl, driver1);
    }

    /**
     * Commands testing:
     * https://jitsi.github.io/handbook/docs/dev-guide/dev-guide-iframe#overwriteconfig
     *
     * Test command overwriteConfig.
     */
    @Test(dependsOnMethods = { "testCommandKickParticipant" })
    public void testCommandOverwriteConfig()
    {
        this.iFrameUrl = getIFrameUrl(null, null);
        ensureOneParticipant(this.iFrameUrl);

        WebParticipant participant1 = getParticipant1();
        WebDriver driver1 = participant1.getDriver();

        switchToIframeAPI(driver1);

        TestUtils.executeScript(driver1,
            "window.jitsiAPI.executeCommand('overwriteConfig', { toolbarButtons: ['chat'] });");

        switchToMeetContent(this.iFrameUrl, driver1);

        assertTrue(getParticipant1().getToolbar().hasButton(Toolbar.CHAT));
        assertFalse(getParticipant1().getToolbar().hasButton(Toolbar.AUDIO_MUTE));

        hangUpAllParticipants();
    }

    /**
     * Commands testing:
     * https://jitsi.github.io/handbook/docs/dev-guide/dev-guide-iframe#sendchatmessage
     * Event:
     * https://jitsi.github.io/handbook/docs/dev-guide/dev-guide-iframe#chatupdated
     * https://jitsi.github.io/handbook/docs/dev-guide/dev-guide-iframe#incomingmessage
     * https://jitsi.github.io/handbook/docs/dev-guide/dev-guide-iframe#outgoingmessage
     *
     * Test command sendChatMessage.
     */
    @Test(dependsOnMethods = { "testCommandOverwriteConfig" })
    public void testCommandSendChatMessage()
    {
        this.iFrameUrl = getIFrameUrl(null, null);
        ensureThreeParticipants(this.iFrameUrl, null, null);

        WebParticipant participant1 = getParticipant1();
        WebParticipant participant2 = getParticipant2();
        WebParticipant participant3 = getParticipant3();
        WebDriver driver1 = participant1.getDriver();
        WebDriver driver2 = participant2.getDriver();
        WebDriver driver3 = participant3.getDriver();
        String endpointId2 = participant2.getEndpointId();
        String endpointName2 = participant2.getName().replaceAll("web\\.", "");

        String testMessageTxt = "Test message text:";

        switchToIframeAPI(driver1);

        addIframeAPIListener(driver1, "chatUpdated");
        addIframeAPIListener(driver1, "incomingMessage");
        addIframeAPIListener(driver1, "outgoingMessage");

        String msg1 = testMessageTxt + "1";
        TestUtils.executeScript(driver1,
            "window.jitsiAPI.executeCommand('sendChatMessage', '" + msg1 + "');");

        switchToMeetContent(this.iFrameUrl, driver1);

        participant2.getToolbar().clickChatButton();
        participant3.getToolbar().clickChatButton();

        assertTrue(driver2.findElement(By.xpath("//div[contains(@class,'chatmessage')]"
                +"//div[contains(@class,'messagecontent')]/div[contains(@class,'usermessage')]"))
                    .getText().contains(msg1));
        assertTrue(driver3.findElement(By.xpath(
            "//div[contains(@class,'messagecontent')]/div[contains(@class,'usermessage')]"))
                    .getText().contains(msg1));

        switchToIframeAPI(driver1);

        JsonObject sentMsgEvent = getEventResult(driver1, "outgoingMessage");
        assertEquals(sentMsgEvent.get("message").getAsString(), msg1);
        assertFalse(sentMsgEvent.get("privateMessage").getAsBoolean());

        String msg2 = testMessageTxt + "2";
        TestUtils.executeScript(driver1,
            "window.jitsiAPI.executeCommand('sendChatMessage', '" + msg2 + "', '" + endpointId2 + "');");

        switchToMeetContent(this.iFrameUrl, driver1);

        WebElement privateMessage = TestUtils.waitForElementBy(
            driver2,
            By.xpath("//div[contains(@class,'privatemessage')]"
                + "//div[contains(@class,'messagecontent')]/div[contains(@class,'usermessage')]"), 2);
        assertNotNull(privateMessage);
        assertTrue(privateMessage.getText().contains(msg2));

        TestUtils.executeScript(driver2, "APP.conference._room.sendMessage('" + msg2 + "')");
        String msg3 = testMessageTxt + "2";
        TestUtils.executeScript(driver2, "APP.conference._room.sendMessage('" + msg3 + "')");

        switchToIframeAPI(driver1);

        sentMsgEvent = getEventResult(driver1, "outgoingMessage");
        assertEquals(sentMsgEvent.get("message").getAsString(), msg2);
        assertTrue(sentMsgEvent.get("privateMessage").getAsBoolean());

        TestUtils.waitForCondition(driver1, 5, (ExpectedCondition<Boolean>) d ->
            getEventResult(d, "chatUpdated").get("unreadCount").getAsInt() == 2);

        JsonObject lastReceivedMsgEvent = getEventResult(driver1, "incomingMessage");
        assertEquals(lastReceivedMsgEvent.get("from").getAsString(), endpointId2);
        assertEquals(lastReceivedMsgEvent.get("message").getAsString(), msg3);
        assertEquals(lastReceivedMsgEvent.get("nick").getAsString(), endpointName2);
        assertFalse(lastReceivedMsgEvent.get("privateMessage").getAsBoolean());

        switchToMeetContent(this.iFrameUrl, driver1);
    }

    /**
     * Commands testing:
     * https://jitsi.github.io/handbook/docs/dev-guide/dev-guide-iframe#setfollowme
     * https://jitsi.github.io/handbook/docs/dev-guide/dev-guide-iframe#settileview
     *
     * Test command setFollowMe and setTileView.
     */
    @Test(dependsOnMethods = { "testCommandSendChatMessage" })
    public void testCommandSetFollowMe()
    {
        if (!this.isModeratorSupported)
        {
            throw new SkipException("Moderation is required for this test.");
        }

        this.iFrameUrl = getIFrameUrl(null, null);
        ensureThreeParticipants(this.iFrameUrl, null, null);

        WebParticipant participant1 = getParticipant1();
        WebParticipant participant2 = getParticipant2();
        WebDriver driver1 = participant1.getDriver();

        switchToIframeAPI(driver1);

        TestUtils.waitForCondition(driver1, 5, (ExpectedCondition<Boolean>) d ->
            TestUtils.executeScriptAndReturnBoolean(driver1,
                "return window.jitsiAPI.test.isModerator;"));

        TestUtils.executeScript(driver1,
            "window.jitsiAPI.executeCommand('setFollowMe', true);");

        TestUtils.executeScript(driver1,
            "window.jitsiAPI.executeCommand('setTileView', false);");

        switchToMeetContent(this.iFrameUrl, driver1);

        getAllParticipants().forEach(participant ->
            MeetUIUtils.waitForTileViewDisplay(participant, false));

        switchToIframeAPI(driver1);

        TestUtils.executeScript(driver1,
            "window.jitsiAPI.executeCommand('setTileView', true);");

        switchToMeetContent(this.iFrameUrl, driver1);

        getAllParticipants().forEach(participant ->
            MeetUIUtils.waitForTileViewDisplay(participant, true));

        switchToIframeAPI(driver1);

        TestUtils.executeScript(driver1,
            "window.jitsiAPI.executeCommand('setFollowMe', false);");

        TestUtils.executeScript(driver1,
            "window.jitsiAPI.executeCommand('setTileView', false);");

        switchToMeetContent(this.iFrameUrl, driver1);

        MeetUIUtils.waitForTileViewDisplay(participant2, true);
    }

    /**
     * Commands testing:
     * https://jitsi.github.io/handbook/docs/dev-guide/dev-guide-iframe#togglecameramirror
     *
     * Test command toggleCameraMirror.
     */
    @Test(dependsOnMethods = { "testCommandSetFollowMe" })
    public void testCommandToggleCameraMirror()
    {
        this.iFrameUrl = getIFrameUrl(null, null);
        ensureOneParticipant(this.iFrameUrl);

        WebParticipant participant1 = getParticipant1();
        WebDriver driver1 = participant1.getDriver();

        String localVideoXpath = "//span[@id='localVideoWrapper']/video[contains(@class,'flipVideoX')]";
        assertTrue(driver1.findElements(By.xpath(localVideoXpath)).size() > 0,
            "Local video should be flipped by default");

        switchToIframeAPI(driver1);

        TestUtils.executeScript(driver1,
            "window.jitsiAPI.executeCommand('toggleCameraMirror');");

        switchToMeetContent(this.iFrameUrl, driver1);

        assertEquals(driver1.findElements(By.xpath(localVideoXpath)).size(), 0,
            "Local video should not be flipped");

        switchToIframeAPI(driver1);

        TestUtils.executeScript(driver1,
            "window.jitsiAPI.executeCommand('toggleCameraMirror');");

        switchToMeetContent(this.iFrameUrl, driver1);

        assertTrue(driver1.findElements(By.xpath(localVideoXpath)).size() > 0,
            "Local video should be flipped now");
    }

    /**
     * Commands testing:
     * https://jitsi.github.io/handbook/docs/dev-guide/dev-guide-iframe#togglevirtualbackgrounddialog
     *
     * Test command toggleVirtualBackgroundDialog.
     */
    @Test(dependsOnMethods = { "testCommandToggleCameraMirror" })
    public void testCommandToggleVirtualBackgroundDialog()
    {
        this.iFrameUrl = getIFrameUrl(null, null);
        ensureOneParticipant(this.iFrameUrl);

        WebParticipant participant1 = getParticipant1();
        WebDriver driver1 = participant1.getDriver();

        switchToIframeAPI(driver1);

        TestUtils.executeScript(driver1,
            "window.jitsiAPI.executeCommand('toggleVirtualBackgroundDialog');");

        switchToMeetContent(this.iFrameUrl, driver1);

        String virtualBackgroundDialogXpath = "//span[@id='dialog-heading-1']";
        String dialogTitle = "Virtual backgrounds";

        TestUtils.waitForElementBy(driver1, By.xpath(virtualBackgroundDialogXpath), 2);

        TestUtils.waitForCondition(driver1, 2, (ExpectedCondition<Boolean>) d -> {
                try
                {
                    return driver1.findElement(By.xpath(virtualBackgroundDialogXpath)).getText().equals(dialogTitle);
                }
                catch(StaleElementReferenceException e)
                {
                    return false;
                }
            });

        switchToIframeAPI(driver1);

        TestUtils.executeScript(driver1,
            "window.jitsiAPI.executeCommand('toggleVirtualBackgroundDialog');");

        assertEquals(driver1.findElements(By.xpath(virtualBackgroundDialogXpath)).size(), 0,
            "Virtual background dialog should not be visible");

        switchToMeetContent(this.iFrameUrl, driver1);
    }

    /**
     * Tests buttons setting through config.js toolbarButtons setting.
     * https://developer.8x8.com/jaas/docs/customize-ui-buttons
     */
    @Test(dependsOnMethods = { "testCommandToggleVirtualBackgroundDialog" })
    public void testCustomButtons()
    {
        this.iFrameUrl = getIFrameUrl(null, null);
        ensureOneParticipant(this.iFrameUrl);

        WebParticipant participant1 = getParticipant1();
        WebDriver driver1 = participant1.getDriver();

        Map<String, String> mainButtons = new HashMap<>();
        mainButtons.put("camera", Toolbar.VIDEO_MUTE);
        mainButtons.put("chat", Toolbar.CHAT);
        mainButtons.put("desktop", Toolbar.DESKTOP);
        mainButtons.put("microphone", Toolbar.AUDIO_MUTE);

        mainButtons.put("raisehand", participant1.getToolbar().areReactionsEnabled()
            ? Toolbar.REACTIONS_MENU : Toolbar.RAISE_HAND);
        mainButtons.put("participants-pane", Toolbar.PARTICIPANTS);
        mainButtons.put("tileview", Toolbar.TILE_VIEW_BUTTON);
        mainButtons.put("hangup", Toolbar.HANGUP);

        mainButtons.put("embedmeeting", Toolbar.EMBED_MEETING);
        mainButtons.put("fullscreen", Toolbar.FULLSCREEN);

        if (MeetUtils.isHelpEnabled(driver1))
        {
            mainButtons.put("help", Toolbar.HELP);
        }

        mainButtons.put("invite", Toolbar.INVITE);
        mainButtons.put("mute-everyone", Toolbar.MUTE_EVERYONE_AUDIO);
        mainButtons.put("mute-video-everyone", Toolbar.MUTE_EVERYONE_VIDEO);
        mainButtons.put("profile", Toolbar.PROFILE);
        mainButtons.put("security", Toolbar.SECURITY);
        mainButtons.put("select-background", Toolbar.SELECT_BACKGROUND);
        mainButtons.put("settings", Toolbar.SETTINGS);
        mainButtons.put("shareaudio", Toolbar.SHARE_AUDIO);
        mainButtons.put("sharedvideo", Toolbar.SHARE_VIDEO);
        mainButtons.put("shortcuts", Toolbar.SHORTCUTS);
        mainButtons.put("stats", Toolbar.STATS);
        mainButtons.put("videoquality", Toolbar.VIDEO_QUALITY);

        mainButtons.forEach((key, value) -> {
            switchToIframeAPI(driver1);

            TestUtils.executeScript(driver1,
                "window.jitsiAPI.executeCommand('overwriteConfig', { toolbarButtons: ['" + key + "'] });");

            switchToMeetContent(this.iFrameUrl, driver1);

            assertEquals(participant1.getToolbar().mainToolbarButtonsCount(), 1,
                "Expected only " + key + " button in the toolbar");

            assertEquals(participant1.getToolbar().overFlowMenuButtonsCount(), 0,
                "Expected no buttons in the overflow menu");

            assertTrue(getParticipant1().getToolbar().hasButton(value));
        });
    }
}
