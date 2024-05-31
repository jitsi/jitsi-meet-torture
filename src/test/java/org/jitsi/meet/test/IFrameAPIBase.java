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
import org.apache.commons.lang3.*;
import org.jitsi.meet.test.base.*;
import org.jitsi.meet.test.util.*;
import org.jitsi.meet.test.web.*;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;
import org.testng.*;

import java.net.*;
import java.time.*;
import java.util.logging.*;

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
public class IFrameAPIBase
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
     * The url to be reused between tests.
     */
    protected JitsiMeetUrl iFrameUrl;

    protected static boolean isModeratorSupported = false;

    /**
     * Loads one/firsst participant and checks for moderator rights.
     */
    protected void checkModerationSupported()
    {
        // if it is true there is no point of checking.
        if (isModeratorSupported)
        {
            return;
        }

        ensureOneParticipant();

        // make sure we wait a bit if we got updated later to moderator
        try
        {
            TestUtils.waitForCondition(getParticipant1().getDriver(), 2,
                (ExpectedCondition<Boolean>) d -> getParticipant1().isModerator());

            isModeratorSupported = true;
        }
        catch(TimeoutException e)
        {
            isModeratorSupported = false;
        }
    }

    /**
     * Checks whether iframe API is disabled.
     */
    protected void checkIframeDisabled()
    {
        ensureOneParticipant();

        if (MeetUtils.iFrameAPIDisabled(getParticipant1().getDriver()))
        {
            cleanupClass();
            throw new SkipException(
                "IFrameAPI is disabled. Disabling test.");
        }
    }

    /**
     * Constructs an JitsiMeetUrl to be used with iframeAPI.
     * @return url that will load a meeting in an iframe.
     */
    protected JitsiMeetUrl getIFrameUrl(JsonObject userInfo, String password)
    {
        return getIFrameUrl(userInfo, password, "");
    }

    /**
     * Constructs an JitsiMeetUrl to be used with iframeAPI.
     * @return url that will load a meeting in an iframe.
     */
    protected JitsiMeetUrl getIFrameUrl(JsonObject userInfo, String password, String config)
    {
        String pagePath = System.getProperty(IFRAME_PAGE_PATH_PNAME);

        if (pagePath == null || pagePath.trim().length() == 0)
        {
            Logger.getGlobal().warning("Skipping because " + IFRAME_PAGE_PATH_PNAME + " is not set.");
            throw new SkipException(IFRAME_PAGE_PATH_PNAME + " is not set.");
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

        JsonObject defaultParams = getJitsiMeetUrl()
            .appendConfig(WebParticipant.DEFAULT_CONFIG, false)
            .appendConfig(config)
            .getFragmentParamsAsJson();

        iFrameUrl.addFragmentParam("domain", domain);
        iFrameUrl.addFragmentParam("room", currentRoomName);

        // do not include empty params (externalAPI complains)
        if (userInfo != null && StringUtils.isNotBlank(userInfo.toString()))
        {
            iFrameUrl.addFragmentParam("userInfo", userInfo.toString());
        }
        if (password != null && StringUtils.isNotBlank(password))
        {
            iFrameUrl.addFragmentParam("password", password);
        }

        // Override the server and the path part(which is s room name)
        iFrameUrl.setServerUrl(pagePath);
        iFrameUrl.setRoomName(IFRAME_ROOM_NAME);
        iFrameUrl.setIframeToNavigateTo("jitsiConferenceFrame0");

        return iFrameUrl;
    }

    /**
     * Switches selenium so to be able to execute iframeAPI commands.
     * @param driver the driver to use.
     */
    protected static void switchToIframeAPI(WebDriver driver)
    {
        driver.switchTo().defaultContent();
    }

    /**
     * Switches to the meeting inside the iframe, so we can execute UI tests.
     * @param iFrameUrl the iframe page URL.
     * @param driver the driver to use.
     */
    protected static void switchToMeetContent(JitsiMeetUrl iFrameUrl, WebDriver driver)
    {
        if (iFrameUrl.getIframeToNavigateTo() != null)
        {
            // let's wait for switch to that iframe, so we can continue with regular tests
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(60));
            wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(
                By.id(iFrameUrl.getIframeToNavigateTo())));
        }
    }

    /**
     * Adds a listener to the iframe API.
     * @param driver the driver to use.
     * @param eventName the event name to add a listener for.
     */
    protected static void addIframeAPIListener(WebDriver driver, String eventName)
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
    protected static JsonObject getEventResult(WebDriver driver, String eventName)
    {
        String result = TestUtils.executeScriptAndReturnString(driver,
            "return JSON.stringify(window.jitsiAPI.test." + eventName + ");");

        return result == null ? null : JsonParser.parseString(result).getAsJsonObject();
    }

    /**
     * Test setLargeVideoParticipant the function or command.
     * @param function whether to use the function, uses the command if false.
     */
    protected void testSetLargeVideoParticipant(boolean function)
    {
        hangUpAllParticipants();

        this.iFrameUrl = getIFrameUrl(null, null);

        // let's mute participants in the beginning so dominant speaker will not overwrite
        // setLargeVideoParticipant execution, or they will not become dominant speaker too early and later
        // will skip becoming dominant speaker
        JitsiMeetUrl meetUrlAudioMuted = getJitsiMeetUrl().appendConfig("config.startWithAudioMuted=true");

        ensureThreeParticipants(this.iFrameUrl, meetUrlAudioMuted, meetUrlAudioMuted);

        WebParticipant participant1 = getParticipant1();
        WebParticipant participant2 = getParticipant2();
        WebParticipant participant3 = getParticipant3();
        WebDriver driver1 = participant1.getDriver();
        String endpoint2Id = participant2.getEndpointId();
        String endpoint3Id = participant3.getEndpointId();

        TestUtils.print("EndpointId 1:" + participant1.getEndpointId());
        TestUtils.print("EndpointId 2:" + endpoint2Id);
        TestUtils.print("EndpointId 3:" + endpoint3Id);

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

        // selects second
        switchToIframeAPI(driver1);
        TestUtils.executeScript(driver1, String.format(setLargeCommand, endpoint2Id));

        // will check that second is on large now
        switchToMeetContent(this.iFrameUrl, driver1);
        MeetUIUtils.waitForLargeVideoSwitchToEndpoint(driver1, endpoint2Id);

        participant1.getNotifications().dismissAnyJoinNotification();
        participant2.getNotifications().dismissAnyJoinNotification();
        participant3.getNotifications().dismissAnyJoinNotification();

        // Leave muted second and first. Third is unmuted
        // (the first one is unmuted already)
        participant1.getToolbar().clickAudioMuteButton();
        participant3.getToolbar().clickAudioUnmuteButton();

        participant1.getFilmstrip().assertAudioMuteIcon(participant1, true);
        participant1.getFilmstrip().assertAudioMuteIcon(participant2, true);
        participant1.getFilmstrip().assertAudioMuteIcon(participant3, false);

        participant2.getFilmstrip().assertAudioMuteIcon(participant1, true);
        participant2.getFilmstrip().assertAudioMuteIcon(participant2, true);
        participant2.getFilmstrip().assertAudioMuteIcon(participant3, false);

        participant3.getFilmstrip().assertAudioMuteIcon(participant1, true);
        participant3.getFilmstrip().assertAudioMuteIcon(participant2, true);
        participant3.getFilmstrip().assertAudioMuteIcon(participant3, false);

        // only the third is unmuted
        MeetUIUtils.waitForDominantSpeaker(driver1, endpoint3Id);

        switchToIframeAPI(driver1);
        TestUtils.executeScript(driver1, setLocalLargeCommand);

        // will check that third - the dominant speaker is on large now
        switchToMeetContent(this.iFrameUrl, driver1);
        MeetUIUtils.waitForLargeVideoSwitchToEndpoint(driver1, endpoint3Id);

        switchToIframeAPI(driver1);

        TestUtils.waitForCondition(driver1, 5, (ExpectedCondition<Boolean>) d -> {
            JsonObject eventData = getEventResult(d, "dominantSpeakerChanged");
            if (eventData != null)
            {
                TestUtils.print("dominantSpeakerChanged:" + eventData.get("id").getAsString());
            }
            else
            {

                TestUtils.print("No dominantSpeakerChanged");
            }

            return eventData != null && eventData.get("id").getAsString().equals(endpoint3Id);
        });

        switchToMeetContent(this.iFrameUrl, driver1);

        // let's unmute everyone as it was initially
        participant2.getToolbar().clickAudioUnmuteButton();
        participant1.getToolbar().clickAudioUnmuteButton();

        participant2.getFilmstrip().assertAudioMuteIcon(participant1, false);
        participant1.getFilmstrip().assertAudioMuteIcon(participant2, false);
    }
}
