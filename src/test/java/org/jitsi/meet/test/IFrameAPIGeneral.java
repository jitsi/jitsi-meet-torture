package org.jitsi.meet.test;

import com.google.gson.*;
import org.jitsi.meet.test.base.*;
import org.jitsi.meet.test.pageobjects.web.*;
import org.jitsi.meet.test.util.*;
import org.jitsi.meet.test.web.*;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;
import org.testng.annotations.*;

import java.util.*;

import static org.testng.Assert.*;

public class IFrameAPIGeneral
    extends IFrameAPIBase
{
    @Override
    public void setupClass()
    {
        super.setupClass();

        checkModerationSupported();
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
     * Tests buttons setting through config.js toolbarButtons setting.
     * https://developer.8x8.com/jaas/docs/customize-ui-buttons
     */
    @Test(dependsOnMethods = { "testIFrameAPI" })
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

        mainButtons.put("raisehand", Toolbar.RAISE_HAND);
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

            assertTrue(getParticipant1().getToolbar().hasButton(value), "Missing button:" + value);
        });
    }
}
