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
import java.util.function.*;

import static org.testng.Assert.*;

/**
 * Test that loads a page using the iframe API to load a meeting.
 * Loads the meeting and we switch to that iframe and then run several
 * tests over it, to make sure iframe API is working fine.
 *
 * functions
 * TODO Need to compare two images: https://jitsi.github.io/handbook/docs/dev-guide/dev-guide-iframe#capturelargevideoscreenshot
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
     * Opens the iframe and executes SwitchVideoTest and MuteTest.
     */
    @Test
    public void testIFrameAPI()
    {
        JitsiMeetUrl iFrameUrl = getIFrameUrl(null, null);

        ensureOneParticipant(iFrameUrl);
        ensureThreeParticipants(iFrameUrl, null, null);

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

        hangUpAllParticipants();
    }

    /**
     * Functions testing:
     * https://jitsi.github.io/handbook/docs/dev-guide/dev-guide-iframe#getparticipantsinfo
     * https://jitsi.github.io/handbook/docs/dev-guide/dev-guide-iframe#getdisplayname
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
        String s = TestUtils.executeScriptAndReturnString(driver,
            "return JSON.stringify(window.jitsiAPI.getParticipantsInfo());");
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
        apiDisplayName = TestUtils.executeScriptAndReturnString(driver,
            "return window.jitsiAPI.getDisplayName('" + endpointId1 + "');");
        assertEquals(apiDisplayName, newName);

        switchToMeetContent(this.iFrameUrl, driver);
    }

    /**
     * Functions testing:
     * https://jitsi.github.io/handbook/docs/dev-guide/dev-guide-iframe#getvideoquality
     *
     * Test retrieving video quality.
     */
    @Test(dependsOnMethods = { "testFunctionGetParticipantsInfo" })
    public void testFunctionGetVideoQuality()
    {
        ensureOneParticipant(this.iFrameUrl);

        WebParticipant participant1 = getParticipant1();
        WebDriver driver = participant1.getDriver();

        switchToIframeAPI(driver);
        String s = TestUtils.executeScriptAndReturnString(driver,
            "return JSON.stringify(window.jitsiAPI.getVideoQuality());");

        // by default, we start with high quality
        assertEquals(s, "2160");

        switchToMeetContent(this.iFrameUrl, driver);

        AudioOnlyTest.setAudioOnly(participant1, true);

        switchToIframeAPI(driver);

        s = TestUtils.executeScriptAndReturnString(driver,
            "return JSON.stringify(window.jitsiAPI.getVideoQuality());");
        // audio only switches to 180
        assertEquals(s, "180");

        switchToMeetContent(this.iFrameUrl, driver);

        AudioOnlyTest.setAudioOnly(participant1, false);
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
     *
     * Test selecting participant on large.
     */
    @Test(dependsOnMethods = { "testFunctionGetVideoQuality" })
    public void testFunctionSetLargeVideoParticipant()
    {
        ensureThreeParticipants(this.iFrameUrl, null, null);

        WebParticipant participant1 = getParticipant1();
        WebParticipant participant2 = getParticipant2();
        WebParticipant participant3 = getParticipant3();
        WebDriver driver1 = participant1.getDriver();
        String endpoint2Id = participant2.getEndpointId();
        String endpoint3Id = participant3.getEndpointId();

        // selects third
        switchToIframeAPI(driver1);
        TestUtils.executeScriptAndReturnString(driver1,
            "JSON.stringify(window.jitsiAPI.setLargeVideoParticipant('" + endpoint3Id + "'));");

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
        TestUtils.executeScriptAndReturnString(driver1,
            "JSON.stringify(window.jitsiAPI.setLargeVideoParticipant('" + endpoint2Id + "'));");

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
        TestUtils.executeScriptAndReturnString(driver1, "JSON.stringify(window.jitsiAPI.setLargeVideoParticipant());");

        // will check that third - the dominant speaker is on large now
        switchToMeetContent(this.iFrameUrl, driver1);
        MeetUIUtils.waitForLargeVideoSwitchToEndpoint(driver1, endpoint3Id);

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
     * Test retrieving local audio/video muted state.
     */
    @Test(dependsOnMethods = { "testFunctionGetNumberOfParticipants" })
    public void testFunctionIsAudioOrVideoMuted()
    {
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

        switchToMeetContent(this.iFrameUrl, driver1);
        participant1.getToolbar().clickAudioMuteButton();
        participant1.getToolbar().clickVideoMuteButton();
        participant2.getFilmstrip().assertAudioMuteIcon(participant1, true);
        participant2.getFilmstrip().assertVideoMuteIcon(participant1, true);

        switchToIframeAPI(driver1);
        result = getAPIAudioAndVideoState.apply(driver1);

        assertTrue(result[0], "Audio must be muted");
        assertTrue(result[1], "Video must be muted");

        // let's revert to the initial state
        switchToMeetContent(this.iFrameUrl, driver1);
        participant1.getToolbar().clickAudioMuteButton();
        participant1.getToolbar().clickVideoMuteButton();
        participant2.getFilmstrip().assertAudioMuteIcon(participant1, true);
        participant2.getFilmstrip().assertVideoMuteIcon(participant1, true);
    }

    /**
     * Commands testing:
     * https://jitsi.github.io/handbook/docs/dev-guide/dev-guide-iframe#displayname
     *
     * Test command displayName
     */
    @Test(dependsOnMethods = { "testFunctionIsAudioOrVideoMuted" })
    public void testCommandDisplayName()
    {
        ensureOneParticipant(this.iFrameUrl);

        String newName = "P1 New Name-" + (int) (Math.random() * 1_000_000);

        WebParticipant participant1 = getParticipant1();
        WebDriver driver1 = participant1.getDriver();

        switchToIframeAPI(driver1);
        TestUtils.executeScript(driver1,
            "return window.jitsiAPI.executeCommand('displayName', '" + newName + "');");

        switchToMeetContent(this.iFrameUrl, driver1);
        participant1.getToolbar().clickSettingsButton();
        SettingsDialog settingsDialog = participant1.getSettingsDialog();
        settingsDialog.waitForDisplay();
        assertEquals(settingsDialog.getDisplayName(), newName);
    }

    /**
     * Commands testing:
     * https://jitsi.github.io/handbook/docs/dev-guide/dev-guide-iframe#password
     *
     * Test command password.
     */
    @Test(dependsOnMethods = { "testCommandDisplayName" })
    public void testCommandPassword()
    {
        hangUpAllParticipants();

        this.iFrameUrl = getIFrameUrl(null, null);

        ensureOneParticipant(this.iFrameUrl);

        WebParticipant participant1 = getParticipant1();
        WebDriver driver1 = participant1.getDriver();

        switchToIframeAPI(driver1);

        TestUtils.waitForCondition(driver1, 5000, (ExpectedCondition<Boolean>) d ->
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
        hangUpAllParticipants();

        this.iFrameUrl = getIFrameUrl(null, null);

        ensureOneParticipant(this.iFrameUrl);

        WebParticipant participant1 = getParticipant1();
        WebDriver driver1 = participant1.getDriver();

        switchToIframeAPI(driver1);

        TestUtils.waitForCondition(driver1, 5000, (ExpectedCondition<Boolean>) d ->
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
     *
     * Test command subject.
     */
    @Test(dependsOnMethods = { "testCommandStartStopShareVideo" })
    public void testCommandSubject()
    {
        this.iFrameUrl = getIFrameUrl(null, null);
        ensureTwoParticipants(this.iFrameUrl, null);

        WebParticipant participant1 = getParticipant1();
        WebParticipant participant2 = getParticipant2();
        WebDriver driver1 = participant1.getDriver();
        WebDriver driver2 = participant2.getDriver();

        switchToIframeAPI(driver1);

        TestUtils.waitForCondition(driver1, 5000, (ExpectedCondition<Boolean>) d ->
            TestUtils.executeScriptAndReturnBoolean(driver1,
                "return window.jitsiAPI.test.isModerator;"));

        String randomSubject = "My Random Subject " + (int) (Math.random() * 1_000_000);
        TestUtils.executeScript(driver1,
            "window.jitsiAPI.executeCommand('subject', '" + randomSubject + "');");

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
}
