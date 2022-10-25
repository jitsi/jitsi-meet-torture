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

import java.util.*;

import static org.testng.Assert.*;

/**
 * Tests the commands of iframeAPI.
 *
 * @author Damian Minkov
 */
public class IFrameAPICommandsTest
    extends IFrameAPIBase
{
    /**
     * JS to execute getting the number of participants.
     */
    private static final String APP_JS_PARTICIPANTS_COUNT = "return APP.conference._room.getParticipantCount();";

    @Override
    public void setupClass()
    {
        super.setupClass();

        checkModerationSupported();
    }

    /**
     * Commands testing:
     * https://jitsi.github.io/handbook/docs/dev-guide/dev-guide-iframe#displayname
     * https://jitsi.github.io/handbook/docs/dev-guide/dev-guide-iframe#email
     * Event:
     * https://jitsi.github.io/handbook/docs/dev-guide/dev-guide-iframe#displaynamechange
     * https://jitsi.github.io/handbook/docs/dev-guide/dev-guide-iframe#emailchange
     * <p>
     * Test command displayName and email.
     */
    @Test
    public void testCommandDisplayName()
    {
        hangUpAllParticipants();
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
     * <p>
     * Test command password.
     */
    @Test(dependsOnMethods = {"testCommandDisplayName"})
    public void testCommandPassword()
    {
        if (!isModeratorSupported)
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
     * <p>
     * Test command toggleLobby.
     */
    @Test(dependsOnMethods = {"testCommandPassword"})
    public void testCommandToggleLobby()
    {
        if (!isModeratorSupported)
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
     * <p>
     * Test command startShareVideo & stopShareVideo.
     */
    @Test(dependsOnMethods = {"testCommandToggleLobby"})
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
        catch(StaleElementReferenceException ex)
        {
            // if the element is detached in a process of checking its display
            // status, means it is not visible anymore
        }
        try
        {
            TestUtils.waitForNotDisplayedElementByID(
                driver2, "sharedVideoPlayer", 5);
        }
        catch(StaleElementReferenceException ex)
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
     * <p>
     * Test command subject.
     */
    @Test(dependsOnMethods = {"testCommandStartStopShareVideo"})
    public void testCommandSubject()
    {
        if (!isModeratorSupported)
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

        TestUtils.waitForCondition(driver1, 1, (ExpectedCondition<Boolean>) d -> {
            JsonObject result = getEventResult(driver1, "subjectChange");

            return result != null && result.get("subject").getAsString().equals(randomSubject);
        });

        switchToMeetContent(this.iFrameUrl, driver1);

        String subjectXpath = "//div[contains(@class,'subject-info-container')]"
            + "//div[contains(@class,'subject-text--content')]";

        // waits for element to be available, displayed and with the correct text
        ExpectedCondition<Boolean> expectedSubject = d -> {
            List<WebElement> res = driver1.findElements(By.xpath(subjectXpath));
            return res.size() > 0 && res.get(0).isDisplayed() && res.get(0).getText().equals(randomSubject);
        };
        TestUtils.waitForCondition(driver1, 10, expectedSubject);
        TestUtils.waitForCondition(driver2, 10, expectedSubject);
    }

    /**
     * Commands testing:
     * https://jitsi.github.io/handbook/docs/dev-guide/dev-guide-iframe#togglefilmstrip
     * Event:
     * https://jitsi.github.io/handbook/docs/dev-guide/dev-guide-iframe#filmstripdisplaychanged
     * <p>
     * Test command toggleFilmStrip.
     */
    @Test(dependsOnMethods = {"testCommandSubject"})
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
     * <p>
     * Test command toggleChat.
     */
    @Test(dependsOnMethods = {"testCommandToggleFilmStrip"})
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
     * <p>
     * Test command toggleRaiseHand.
     */
    @Test(dependsOnMethods = {"testCommandToggleChat"})
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

        TestUtils.print("EndpointId 1:" + endpointId1);
        TestUtils.print("EndpointId 2:" + endpointId2);

        // Make sure we mute both participants so dominant speaker changes will not clear the raise hand we are
        // about to test
        participant1.getToolbar().clickAudioMuteButton();
        participant2.getToolbar().clickAudioMuteButton();
        participant1.getFilmstrip().assertAudioMuteIcon(participant2, true);
        participant1.getFilmstrip().assertAudioMuteIcon(participant1, true);
        participant2.getFilmstrip().assertAudioMuteIcon(participant2, true);
        participant2.getFilmstrip().assertAudioMuteIcon(participant1, true);

        switchToIframeAPI(driver1);

        addIframeAPIListener(driver1, "raiseHandUpdated");

        TestUtils.executeScript(driver1,
            "window.jitsiAPI.executeCommand('toggleRaiseHand');");

        switchToMeetContent(this.iFrameUrl, driver1);

        TestUtils.waitForCondition(driver2, 5, (ExpectedCondition<Boolean>) d ->
            participant2.getNotifications().hasRaisedHandNotification());

        switchToIframeAPI(driver1);

        TestUtils.waitForCondition(driver1, 5, (ExpectedCondition<Boolean>) d -> {
            JsonObject e = getEventResult(d, "raiseHandUpdated");

            if (e == null )
            {
                return false;
            }

            long handRaisedTimestamp = e.get("handRaised").getAsLong();
            return e.get("id").getAsString().equals(endpointId1) && handRaisedTimestamp > 0;
        });

        TestUtils.executeScript(driver1,
            "window.jitsiAPI.executeCommand('toggleRaiseHand');");

        switchToMeetContent(this.iFrameUrl, driver1);

        TestUtils.waitForCondition(driver2, 5, (ExpectedCondition<Boolean>) d ->
            !participant2.getNotifications().hasRaisedHandNotification());

        switchToIframeAPI(driver1);

        TestUtils.waitForCondition(driver1, 5, (ExpectedCondition<Boolean>) d -> {
            JsonObject e = getEventResult(d, "raiseHandUpdated");

            if (e == null )
            {
                return false;
            }

            long handRaisedTimestamp = e.get("handRaised").getAsLong();
            return e.get("id").getAsString().equals(endpointId1) && handRaisedTimestamp == 0;
        });

        switchToMeetContent(this.iFrameUrl, driver1);

        String raiseHandSelector = participant2.getToolbar().clickRaiseHandButton();

        TestUtils.waitForCondition(driver2, 5, (ExpectedCondition<Boolean>) d ->
            d.findElement(By.cssSelector(raiseHandSelector)).getAttribute("aria-pressed").equals("true"));

        TestUtils.waitForCondition(driver1, 5, (ExpectedCondition<Boolean>) d ->
            participant1.getNotifications().hasRaisedHandNotification());

        switchToIframeAPI(driver1);

        TestUtils.waitForCondition(driver1, 5, (ExpectedCondition<Boolean>) d -> {
            JsonObject e = getEventResult(d, "raiseHandUpdated");

            if (e == null )
            {
                return false;
            }

            long handRaisedTimestamp = e.get("handRaised").getAsLong();
            return e.get("id").getAsString().equals(endpointId2) && handRaisedTimestamp > 0;
        });

        switchToMeetContent(this.iFrameUrl, driver1);

        participant2.getToolbar().clickRaiseHandButton();

        try
        {
            TestUtils.waitForCondition(driver2, 5, (ExpectedCondition<Boolean>) d ->
                d.findElement(By.cssSelector(raiseHandSelector)).getAttribute("aria-pressed").equals("false"));
        }
        catch(TimeoutException te)
        {
            TestUtils.print("Retrying clicking raise hand for:" + endpointId2);

            // We sometimes see that the second click (to lower the hand) is not propagated for some reason
            // So let's do a retry waiting for the UI to set that the button is not toggled anymore
            participant2.getToolbar().clickRaiseHandButton();

            TestUtils.waitForCondition(driver2, 5, (ExpectedCondition<Boolean>) d ->
                d.findElement(By.cssSelector(raiseHandSelector)).getAttribute("aria-pressed").equals("false"));
        }

        TestUtils.waitForCondition(driver1, 5, (ExpectedCondition<Boolean>) d ->
            !participant1.getNotifications().hasRaisedHandNotification());

        switchToIframeAPI(driver1);

        TestUtils.waitForCondition(driver1, 5, (ExpectedCondition<Boolean>) d -> {
            JsonObject e = getEventResult(d, "raiseHandUpdated");
            long handRaisedTimestamp = e.get("handRaised").getAsLong();
            boolean result = e.get("id").getAsString().equals(endpointId2)
                && handRaisedTimestamp == 0;

            if (!result)
            {
                TestUtils.print("raise hand event:" + e);
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
     * <p>
     * Test command toggleShareScreen and getContentSharingParticipants.
     */
    @Test(dependsOnMethods = {"testCommandToggleRaiseHand"})
    public void testCommandToggleShareScreen()
    {
        hangUpAllParticipants();
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

        DesktopSharingTest.checkForScreensharingTile(participant1, participant2, true, 5);

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
        assertEquals(JsonParser.parseString(res).getAsJsonArray().get(0).getAsString(), endpointId1);

        JsonObject sharingData = getEventResult(driver1, "screenSharingStatusChanged");
        assertTrue(sharingData.get("on").getAsBoolean(), "Screen sharing must be on");

        TestUtils.executeScript(driver1,
            "window.jitsiAPI.executeCommand('toggleShareScreen');");

        switchToMeetContent(this.iFrameUrl, driver1);

        DesktopSharingTest.checkForScreensharingTile(participant1, participant2, false, 5);

        ensureThreeParticipants(this.iFrameUrl, null, null);

        WebParticipant participant3 = getParticipant3();
        String endpointId3 = participant3.getEndpointId();

        participant2.getToolbar().clickDesktopSharingButton();
        participant3.getToolbar().clickDesktopSharingButton();
        DesktopSharingTest.checkForScreensharingTile(participant2, participant1, true, 5);
        DesktopSharingTest.checkForScreensharingTile(participant3, participant1, true, 5);

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
        JsonParser.parseString(res).getAsJsonArray().forEach(el -> resultIds.add(el.getAsString()));

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
     * <p>
     * Event:
     * https://jitsi.github.io/handbook/docs/dev-guide/dev-guide-iframe#tileviewchanged
     * <p>
     * Test command toggleTileView.
     */
    @Test(dependsOnMethods = {"testCommandToggleShareScreen"})
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
     * <p>
     * Test command hangup.
     */
    @Test(dependsOnMethods = {"testCommandToggleTileView"})
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

        TestUtils.waitForCondition(driver1, 3, (ExpectedCondition<Boolean>) d -> {
            JsonObject event = getEventResult(driver1, "videoConferenceLeft");

            return event != null && event.get("roomName").getAsString().equals(getJitsiMeetUrl().getRoomName());
        });
        switchToMeetContent(this.iFrameUrl, driver1);

        hangUpAllParticipants();
    }

    /**
     * Commands testing:
     * https://jitsi.github.io/handbook/docs/dev-guide/dev-guide-iframe#avatarurl
     * Event:
     * https://jitsi.github.io/handbook/docs/dev-guide/dev-guide-iframe#avatarchanged
     * <p>
     * Test command avatarUrl.
     */
    @Test(dependsOnMethods = {"testCommandHangup"})
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

        TestUtils.waitForCondition(driver1, 2, (ExpectedCondition<Boolean>) d ->
            AvatarTest.getLocalThumbnailSrc(d).equals(avatarUrl));
        TestUtils.waitForCondition(driver2, 2, (ExpectedCondition<Boolean>) d ->
            AvatarTest.getThumbnailSrc(d, endpointId1).equals(avatarUrl));

        switchToIframeAPI(driver1);

        // waits for the event
        TestUtils.waitForCondition(driver1, 5, (ExpectedCondition<Boolean>) d ->
            getEventResult(d, "avatarChanged").get("avatarURL").getAsString().equals(avatarUrl));

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
     * <p>
     * Test command sendEndpointTextMessage.
     */
    @Test(dependsOnMethods = {"testCommandAvatarUrl"})
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
     * <p>
     * Test command setLargeVideoParticipant.
     */
    @Test(dependsOnMethods = {"testCommandSendEndpointTextMessage"})
    public void testCommandSetLargeVideoParticipant()
    {
        testSetLargeVideoParticipant(false);
    }

    /**
     * Commands testing:
     * https://jitsi.github.io/handbook/docs/dev-guide/dev-guide-iframe#muteeveryone-1
     * <p>
     * Test command muteEveryone.
     */
    @Test(dependsOnMethods = {"testCommandSetLargeVideoParticipant"})
    public void testCommandMuteEveryone()
    {
        if (!isModeratorSupported)
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
     * <p>
     * Test command initiatePrivateChat & cancelPrivateChat.
     */
    @Test(dependsOnMethods = {"testCommandMuteEveryone"})
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
     * <p>
     * Test command kickParticipant.
     */
    @Test(dependsOnMethods = {"testCommandInitiateCancelPrivateChat"})
    public void testCommandKickParticipant()
    {
        if (!isModeratorSupported)
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
     * <p>
     * Test command overwriteConfig.
     */
    @Test(dependsOnMethods = {"testCommandKickParticipant"})
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
     * <p>
     * Test command sendChatMessage.
     */
    @Test(dependsOnMethods = {"testCommandOverwriteConfig"})
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
                + "//div[contains(@class,'messagecontent')]/div[contains(@class,'usermessage')]"))
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
     * <p>
     * Test command setFollowMe and setTileView.
     */
    @Test(dependsOnMethods = {"testCommandSendChatMessage"})
    public void testCommandSetFollowMe()
    {
        if (!isModeratorSupported)
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
     * <p>
     * Test command toggleCameraMirror.
     */
    @Test(dependsOnMethods = {"testCommandSetFollowMe"})
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
     * <p>
     * Test command toggleVirtualBackgroundDialog.
     */
    @Test(dependsOnMethods = {"testCommandToggleCameraMirror"})
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

        String virtualBackgroundDialogXpath = "//p[@id='dialog-title']";
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
     * Command testing:
     * https://jitsi.github.io/handbook/docs/dev-guide/dev-guide-iframe#localsubject
     *
     * Test command local subject.
     */
    @Test(dependsOnMethods = { "testCommandToggleVirtualBackgroundDialog" })
    public void testCommandLocalSubject()
    {
        this.iFrameUrl = getIFrameUrl(null, null);
        ensureOneParticipant(this.iFrameUrl);

        WebParticipant participant1 = getParticipant1();
        WebDriver driver = participant1.getDriver();

        switchToIframeAPI(driver);

        String randomLocalSubject = "My Random Local Subject " + (int) (Math.random() * 1_000_000);
        TestUtils.executeScript(driver,
            "window.jitsiAPI.executeCommand('localSubject', '" + randomLocalSubject + "');");

        String conferenceNameXpath = "//div[contains(@class,'subject-info-container')]"
            + "//div[contains(@class,'subject-text--content')]";

        switchToMeetContent(this.iFrameUrl, driver);

        // waits for element to be available, displayed and with the correct text
        ExpectedCondition<Boolean> expectedLocalSubject = d ->{
            List<WebElement> res = driver.findElements(By.xpath(conferenceNameXpath));
            return res.size() > 0 && res.get(0).isDisplayed() && res.get(0).getText().equals(randomLocalSubject);
        };

        TestUtils.waitForCondition(driver, 10, expectedLocalSubject);
    }

    /**
     * Command testing:
     * https://jitsi.github.io/handbook/docs/dev-guide/dev-guide-iframe#toggleparticipantspane
     * <p>
     * Function
     * https://jitsi.github.io/handbook/docs/dev-guide/dev-guide-iframe#isparticipantspaneopen
     * <p>
     * Test command toggleTileView and isParticipantsPaneOpen.
     */
    @Test(dependsOnMethods = {"testCommandLocalSubject"})
    public void testCommandToggleParticipantsPane()
    {
        this.iFrameUrl = getIFrameUrl(null, null);
        ensureOneParticipant(this.iFrameUrl);

        WebParticipant participant1 = getParticipant1();
        WebDriver driver1 = participant1.getDriver();
        ParticipantsPane participantsPane = participant1.getParticipantsPane();

        // first checks that the participants pane is closed
        switchToIframeAPI(driver1);
        TestUtils.executeScript(driver1,
                "window.jitsiAPI.isParticipantsPaneOpen()" +
                        ".then(res => window.jitsiAPI.test.isParticipantsPaneOpen1 = res);");
        boolean isParticipantsPaneOpen = TestUtils.executeScriptAndReturnBoolean(driver1,
                "return window.jitsiAPI.test.isParticipantsPaneOpen1;");

        // checks both the function and the UI
        assertFalse(isParticipantsPaneOpen);

        switchToMeetContent(this.iFrameUrl, driver1);
        TestUtils.waitForCondition(driver1, 5, (ExpectedCondition<Boolean>) d ->
                participantsPane.isOpen() == false);

        // then executes the command to open it
        switchToIframeAPI(driver1);
        TestUtils.executeScript(driver1,
                "window.jitsiAPI.executeCommand('toggleParticipantsPane', true);");

        TestUtils.executeScript(driver1,
                "window.jitsiAPI.isParticipantsPaneOpen()" +
                        ".then(res => window.jitsiAPI.test.isParticipantsPaneOpen2 = res);");
        isParticipantsPaneOpen = TestUtils.executeScriptAndReturnBoolean(driver1,
                "return window.jitsiAPI.test.isParticipantsPaneOpen2;");

        assertTrue(isParticipantsPaneOpen);

        switchToMeetContent(this.iFrameUrl, driver1);
        TestUtils.waitForCondition(driver1, 5, (ExpectedCondition<Boolean>) d ->
                participantsPane.isOpen() == true);

        // executes the command to close it
        switchToIframeAPI(driver1);
        TestUtils.executeScript(driver1,
                "window.jitsiAPI.executeCommand('toggleParticipantsPane', false);");
        TestUtils.executeScript(driver1,
                "window.jitsiAPI.isParticipantsPaneOpen()" +
                        ".then(res => window.jitsiAPI.test.isParticipantsPaneOpen2 = res);");
        isParticipantsPaneOpen = TestUtils.executeScriptAndReturnBoolean(driver1,
                "return window.jitsiAPI.test.isParticipantsPaneOpen2;");

        assertFalse(isParticipantsPaneOpen);

        switchToMeetContent(this.iFrameUrl, driver1);
        TestUtils.waitForCondition(driver1, 5, (ExpectedCondition<Boolean>) d ->
                participantsPane.isOpen() == false);
    }

    /**
     * Command testing:
     * https://jitsi.github.io/handbook/docs/dev-guide/dev-guide-iframe#addbreakoutroom
     * Function
     * https://jitsi.github.io/handbook/docs/dev-guide/dev-guide-iframe#listbreakoutrooms
     * <p>
     * Test command addBreakoutRoom and listBreakoutRooms.
     */
    @Test(dependsOnMethods = {"testCommandToggleParticipantsPane"})
    public void testCommandAddBreakoutRoom()
    {
        if (!isModeratorSupported)
        {
            throw new SkipException("Moderation is required for this test.");
        }

        this.iFrameUrl = getIFrameUrl(null, null);
        ensureOneParticipant(this.iFrameUrl, null);

        WebParticipant participant1 = getParticipant1();
        WebDriver driver1 = participant1.getDriver();
        BreakoutRoomsList roomsList = participant1.getBreakoutRoomsList();
        String roomName = "testRoomName";

        // checks that initially no breakout rooms are present
        TestUtils.waitForCondition(driver1, 5, (ExpectedCondition<Boolean>) d ->
                roomsList.getRoomsCount() == 0);

        switchToIframeAPI(driver1);
        TestUtils.waitForCondition(driver1, 5, (ExpectedCondition<Boolean>) d ->
                TestUtils.executeScriptAndReturnBoolean(driver1,
                        "return window.jitsiAPI.test.isModerator;"));

        // attempts to add a breakout room
        TestUtils.executeScript(driver1,
                "window.jitsiAPI.executeCommand('addBreakoutRoom');");

        // checks the result
        switchToMeetContent(this.iFrameUrl, driver1);
        participant1.getParticipantsPane().open();
        TestUtils.waitForCondition(driver1, 5, (ExpectedCondition<Boolean>) d ->
                roomsList.getRoomsCount() == 1);

        switchToIframeAPI(driver1);
        TestUtils.executeScript(driver1,
                "window.jitsiAPI.executeCommand('addBreakoutRoom', '" + roomName + "');");

        // attempts to add another breakout room, this time with custom name
        switchToMeetContent(this.iFrameUrl, driver1);
        TestUtils.waitForCondition(driver1, 5, (ExpectedCondition<Boolean>) d ->
                roomsList.getRoomsCount() == 2);

        String roomId1 = roomsList.getRooms().get(0).getId();
        String roomId2 = roomsList.getRooms().get(1).getId();

        // uses the function to list breakout rooms and retrieve room objects
        switchToIframeAPI(driver1);
        TestUtils.executeScript(driver1,
                "window.jitsiAPI.listBreakoutRooms()" +
                        ".then(res => window.jitsiAPI.test.breakoutRooms1 = JSON.stringify(res));");

        String res = TestUtils.executeScriptAndReturnString(driver1,
                "return window.jitsiAPI.test.breakoutRooms1;");
        assertNotNull(res);
        assertTrue(JsonParser.parseString(res).getAsJsonObject().has(roomId1));
        assertEquals(JsonParser.parseString(res).getAsJsonObject().get(roomId2).getAsJsonObject()
                .get("name").getAsString(), roomName);

        switchToMeetContent(this.iFrameUrl, driver1);
        hangUpAllParticipants();
        roomsList.removeRooms();
    }

    /**
     * Command testing:
     * https://jitsi.github.io/handbook/docs/dev-guide/dev-guide-iframe#joinbreakoutroom
     * <p>
     * Test command joinBreakoutRoom.
     */
    @Test(dependsOnMethods = {"testCommandAddBreakoutRoom"})
    public void testCommandJoinBreakoutRoom()
    {
        if (!isModeratorSupported)
        {
            throw new SkipException("Moderation is required for this test.");
        }

        this.iFrameUrl = getIFrameUrl(null, null);
        ensureOneParticipant(this.iFrameUrl, null);

        WebParticipant participant1 = getParticipant1();
        WebDriver driver1 = participant1.getDriver();
        BreakoutRoomsList roomsList = participant1.getBreakoutRoomsList();

        switchToIframeAPI(driver1);
        TestUtils.waitForCondition(driver1, 5, (ExpectedCondition<Boolean>) d ->
                TestUtils.executeScriptAndReturnBoolean(driver1,
                        "return window.jitsiAPI.test.isModerator;"));

        // adds a breakout room to join later
        TestUtils.executeScript(driver1,
                "window.jitsiAPI.executeCommand('addBreakoutRoom');");

        switchToMeetContent(this.iFrameUrl, driver1);
        participant1.getParticipantsPane().open();

        TestUtils.waitForCondition(driver1, 5, (ExpectedCondition<Boolean>) d ->
                participant1.getBreakoutRoomsList().getRoomsCount() == 1);

        // saves room id to use in command
        String roomId = roomsList.getRooms().get(0).getId();

        // attempts to join the newly created breakout room
        switchToIframeAPI(driver1);
        TestUtils.executeScript(driver1,
                "window.jitsiAPI.executeCommand('joinBreakoutRoom', '" + roomId + "');");

        // listens to conference joined event to check room type flag
        TestUtils.waitForCondition(driver1, 3, (ExpectedCondition<Boolean>) d -> {
            JsonObject event = getEventResult(driver1, "videoConferenceJoined");

            return event != null && event.get("breakoutRoom").getAsBoolean();
        });

        switchToMeetContent(this.iFrameUrl, driver1);

        hangUpAllParticipants();
        roomsList.removeRooms();
    }

    /**
     * Command testing:
     * https://jitsi.github.io/handbook/docs/dev-guide/dev-guide-iframe#removebreakoutroom
     * <p>
     * Test command removeBreakoutRoom.
     */
    @Test(dependsOnMethods = {"testCommandJoinBreakoutRoom"})
    public void testCommandRemoveBreakoutRoom()
    {
        if (!isModeratorSupported)
        {
            throw new SkipException("Moderation is required for this test.");
        }

        this.iFrameUrl = getIFrameUrl(null, null);
        ensureOneParticipant(this.iFrameUrl, null);

        WebParticipant participant1 = getParticipant1();
        WebDriver driver1 = participant1.getDriver();
        BreakoutRoomsList roomsList = participant1.getBreakoutRoomsList();

        switchToIframeAPI(driver1);
        TestUtils.waitForCondition(driver1, 5, (ExpectedCondition<Boolean>) d ->
                TestUtils.executeScriptAndReturnBoolean(driver1,
                        "return window.jitsiAPI.test.isModerator;"));

        // adds a breakout room to remove later
        TestUtils.executeScript(driver1,
                "window.jitsiAPI.executeCommand('addBreakoutRoom');");

        switchToMeetContent(this.iFrameUrl, driver1);
        participant1.getParticipantsPane().open();

        TestUtils.waitForCondition(driver1, 5, (ExpectedCondition<Boolean>) d ->
                roomsList.getRoomsCount() == 1);

        // saves the room id to use in command
        String roomId = roomsList.getRooms().get(0).getId();

        // uses the function to list breakout rooms to retrieve room jid
        switchToIframeAPI(driver1);
        TestUtils.executeScript(driver1,
                "window.jitsiAPI.listBreakoutRooms()" +
                        ".then(res => window.jitsiAPI.test.breakoutRooms1 = JSON.stringify(res));");

        String res = TestUtils.executeScriptAndReturnString(driver1,
                "return window.jitsiAPI.test.breakoutRooms1;");
        assertNotNull(res);
        String roomJid = JsonParser.parseString(res).getAsJsonObject().get(roomId).getAsJsonObject()
                .get("jid").getAsString();

        // uses room jid in command call
        switchToIframeAPI(driver1);
        TestUtils.executeScript(driver1,
                "window.jitsiAPI.executeCommand('removeBreakoutRoom', '" + roomJid + "');");

        switchToMeetContent(this.iFrameUrl, driver1);
        TestUtils.waitForCondition(driver1, 3, (ExpectedCondition<Boolean>) d ->
                roomsList.getRoomsCount() == 0);

        hangUpAllParticipants();
        roomsList.removeRooms();
    }

    /**
     * Command testing:
     * https://jitsi.github.io/handbook/docs/dev-guide/dev-guide-iframe#sendparticipanttoroom
     * <p>
     * Test command sendParticipantToRoom.
     */
    @Test(dependsOnMethods = {"testCommandRemoveBreakoutRoom"})
    public void testCommandSendParticipantToRoom()
    {
        if (!isModeratorSupported)
        {
            throw new SkipException("Moderation is required for this test.");
        }

        this.iFrameUrl = getIFrameUrl(null, null);
        ensureTwoParticipants(this.iFrameUrl, null);

        WebParticipant participant1 = getParticipant1();
        WebParticipant participant2 = getParticipant2();
        WebDriver driver1 = participant1.getDriver();
        BreakoutRoomsList roomsList = participant1.getBreakoutRoomsList();

        switchToIframeAPI(driver1);
        TestUtils.waitForCondition(driver1, 5, (ExpectedCondition<Boolean>) d ->
                TestUtils.executeScriptAndReturnBoolean(driver1,
                        "return window.jitsiAPI.test.isModerator;"));

        // adds a breakout room to send a participant to it later
        TestUtils.executeScript(driver1,
                "window.jitsiAPI.executeCommand('addBreakoutRoom');");

        switchToMeetContent(this.iFrameUrl, driver1);
        participant1.getParticipantsPane().open();

        TestUtils.waitForCondition(driver1, 5, (ExpectedCondition<Boolean>) d ->
                roomsList.getRoomsCount() == 1);

        // saves the room id to use in command
        String roomId = roomsList.getRooms().get(0).getId();

        // attempts to send the other participant to the newly created breakout room
        switchToIframeAPI(driver1);
        TestUtils.executeScript(driver1,
                "window.jitsiAPI.executeCommand('sendParticipantToRoom', '"
                        + participant2.getEndpointId() + "', '" + roomId + "');");

        switchToMeetContent(this.iFrameUrl, driver1);
        TestUtils.waitForCondition(driver1, 5, (ExpectedCondition<Boolean>) d ->
        {
            List<BreakoutRoomsList.BreakoutRoom> rooms = roomsList.getRooms();
            return rooms.size() == 1 && rooms.get(0).getParticipantsCount() == 1;
        });

        hangUpAllParticipants();
        roomsList.removeRooms();
    }

    /**
     * Command testing:
     * https://jitsi.github.io/handbook/docs/dev-guide/dev-guide-iframe#autoassigntobreakoutrooms
     * <p>
     * Test command autoAssignToBreakoutRooms.
     */
    @Test(dependsOnMethods = {"testCommandSendParticipantToRoom"})
    public void testCommandAutoAssignToBreakoutRooms()
    {
        if (!isModeratorSupported)
        {
            throw new SkipException("Moderation is required for this test.");
        }

        hangUpAllParticipants();

        this.iFrameUrl = getIFrameUrl(null, null);
        ensureThreeParticipants(this.iFrameUrl, null, null);

        WebParticipant participant1 = getParticipant1();
        WebDriver driver1 = participant1.getDriver();
        BreakoutRoomsList roomsList = participant1.getBreakoutRoomsList();

        switchToIframeAPI(driver1);
        TestUtils.waitForCondition(driver1, 5, (ExpectedCondition<Boolean>) d ->
                TestUtils.executeScriptAndReturnBoolean(driver1,
                        "return window.jitsiAPI.test.isModerator;"));

        // adds two breakout rooms to assign participants to them later
        TestUtils.executeScript(driver1,
                "window.jitsiAPI.executeCommand('addBreakoutRoom');");
        TestUtils.executeScript(driver1,
                "window.jitsiAPI.executeCommand('addBreakoutRoom');");

        switchToMeetContent(this.iFrameUrl, driver1);
        participant1.getParticipantsPane().open();

        TestUtils.waitForCondition(driver1, 2, (ExpectedCondition<Boolean>) d ->
                roomsList.getRoomsCount() == 2);

        // attempts to auto assign the other participants to the breakout rooms
        switchToIframeAPI(driver1);
        TestUtils.executeScript(driver1,
                "window.jitsiAPI.executeCommand('autoAssignToBreakoutRooms');");

        switchToMeetContent(this.iFrameUrl, driver1);
        TestUtils.waitForCondition(driver1, 5, (ExpectedCondition<Boolean>) d ->
        {
            List<BreakoutRoomsList.BreakoutRoom> rooms = roomsList.getRooms();
            return rooms.size() == 2
                    && rooms.get(0).getParticipantsCount() == 1
                    && rooms.get(1).getParticipantsCount() == 1;
        });

        hangUpAllParticipants();
        roomsList.removeRooms();
    }

    /**
     * Command testing:
     * https://jitsi.github.io/handbook/docs/dev-guide/dev-guide-iframe#closebreakoutroom
     * <p>
     * Test command closeBreakoutRoom.
     */
    @Test(dependsOnMethods = {"testCommandAutoAssignToBreakoutRooms"})
    public void testCommandCloseBreakoutRoom()
    {
        if (!isModeratorSupported)
        {
            throw new SkipException("Moderation is required for this test.");
        }

        this.iFrameUrl = getIFrameUrl(null, null);
        ensureTwoParticipants(this.iFrameUrl, null);

        WebParticipant participant1 = getParticipant1();
        WebDriver driver1 = participant1.getDriver();
        BreakoutRoomsList roomsList = participant1.getBreakoutRoomsList();

        switchToIframeAPI(driver1);
        TestUtils.waitForCondition(driver1, 5, (ExpectedCondition<Boolean>) d ->
                TestUtils.executeScriptAndReturnBoolean(driver1,
                        "return window.jitsiAPI.test.isModerator;"));

        // adds a breakout room to close it later
        TestUtils.executeScript(driver1,
                "window.jitsiAPI.executeCommand('addBreakoutRoom');");

        switchToMeetContent(this.iFrameUrl, driver1);
        participant1.getParticipantsPane().open();

        TestUtils.waitForCondition(driver1, 2, (ExpectedCondition<Boolean>) d ->
                roomsList.getRoomsCount() == 1);

        // auto assigns participants to have them return to the main room later
        switchToIframeAPI(driver1);
        TestUtils.executeScript(driver1,
                "window.jitsiAPI.executeCommand('autoAssignToBreakoutRooms');");

        switchToMeetContent(this.iFrameUrl, driver1);
        TestUtils.waitForCondition(driver1, 5, (ExpectedCondition<Boolean>) d ->
        {
            List<BreakoutRoomsList.BreakoutRoom> rooms = roomsList.getRooms();
            return rooms.size() == 1 && rooms.get(0).getParticipantsCount() == 1;
        });

        // saves the room id to use in command
        String roomId = roomsList.getRooms().get(0).getId();

        // attempts to close the breakout room
        switchToIframeAPI(driver1);
        TestUtils.executeScript(driver1,
                "window.jitsiAPI.executeCommand('closeBreakoutRoom', '" + roomId + "');");

        switchToMeetContent(this.iFrameUrl, driver1);
        TestUtils.waitForCondition(driver1, 5, (ExpectedCondition<Boolean>) d ->
                roomsList.getRooms().get(0).getParticipantsCount() == 0);

        hangUpAllParticipants();
        roomsList.removeRooms();
    }
}
