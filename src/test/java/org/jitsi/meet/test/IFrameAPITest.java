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
        + "&config=%s&interfaceConfig=%s&userInfo=%s";

    /**
     * The url to be reused between tests.
     */
    private JitsiMeetUrl iFrameUrl;

    /**
     * Constructs an JitsiMeetUrl to be used with iframeAPI.
     * @return url that will load a meeting in an iframe.
     */
    private JitsiMeetUrl getIFrameUrl(JsonObject userInfo)
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
            userInfo != null ? userInfo : "");

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
        JitsiMeetUrl iFrameUrl = getIFrameUrl(null);

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
     *
     * Tests and passing userInfo to JitsiMeetExternalAPI options
     */
    @Test(dependsOnMethods = { "testIFrameAPI" })
    public void testFunctionGetParticipantsInfo()
    {
        String displayName = "John Doe";
        String email = "email@jitsiexamplemail.com";

        JsonObject userInfo = new JsonObject();
        userInfo.addProperty("email", email);
        userInfo.addProperty("displayName", displayName);

        this.iFrameUrl = getIFrameUrl(userInfo);

        ensureOneParticipant(this.iFrameUrl);

        WebParticipant participant1 = getParticipant1();
        WebDriver driver = participant1.getDriver();

        switchToIframeAPI(driver);
        String s = TestUtils.executeScriptAndReturnString(driver,
            "return JSON.stringify(window.jitsiAPI.getParticipantsInfo());");

        // returns an array of participants, in this case just us
        JsonObject participantsInfo
            = new JsonParser().parse(s).getAsJsonArray().get(0).getAsJsonObject();

        // check displayName from getParticipantsInfo, returned as "John Doe (me)"
        assertTrue(
            participantsInfo.get("formattedDisplayName").getAsString().contains(displayName),
            "Wrong display name from iframeAPI.getParticipantsInfo");

        switchToMeetContent(this.iFrameUrl, driver);

        DisplayNameTest.checkDisplayNameOnLocalVideo(participant1, displayName);

        // check displayName and email in settings through UI
        participant1.getToolbar().clickSettingsButton();
        SettingsDialog settingsDialog = participant1.getSettingsDialog();
        settingsDialog.waitForDisplay();
        assertEquals(settingsDialog.getDisplayName(), displayName);
        assertEquals(settingsDialog.getEmail(), email);
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
        WebParticipant participant1 = getParticipant1();
        WebDriver driver1 = participant1.getDriver();

        // the previous test left it in meeting content
        ensureTwoParticipants(this.iFrameUrl, null);

        // selects local
        MeetUIUtils.selectLocalVideo(getParticipant1().getDriver());

        String endpoint2Id = getParticipant2().getEndpointId();

        switchToIframeAPI(driver1);
        TestUtils.executeScriptAndReturnString(driver1,
            "JSON.stringify(window.jitsiAPI.pinParticipant('" + endpoint2Id + "'));");

        switchToMeetContent(this.iFrameUrl, driver1);

        MeetUIUtils.waitForLargeVideoSwitchToEndpoint(
            driver1,
            endpoint2Id);
    }
}
