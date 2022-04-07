package org.jitsi.meet.test.pageobjects.web;

import org.jitsi.meet.test.util.*;
import org.jitsi.meet.test.web.*;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;

import java.util.*;
import java.util.logging.*;

import static org.testng.Assert.fail;

/**
 * Represents the participants pane in a particular {@link WebParticipant}.
 *
 * @author Gabriel Imre
 */
public class ParticipantsPane
{
    /**
     * Accessibility labels to be used as selectors for finding WebElements
     * within the {@link ParticipantsPane}.
     */
    private final static String INVITE = "Invite Someone";

    /**
     * The ID of the context menu button.
     */
    private final static String CONTEXT_MENU = "participants-pane-context-menu";

    /**
     * Classname of the closed/hidden participants pane
     */
    private final static String PARTICIPANTS_PANE = "participants_pane";

    /**
     * Accessibility label to be used as selectors for finding participants more
     * button within the {@link ParticipantsPane}.
     */
    private final static String PARTICIPANT_MORE_LABEL = "More participant options";

    /**
     * Accessibility label to be used as selectors for finding Add breakout room
     * button within the {@link ParticipantsPane}.
     */
    private final static String ADD_BREAKOUT_ROOM = "Add breakout room";

    /**
     * Accessibility label to be used as selectors for finding Leave breakout room
     * button within the {@link ParticipantsPane}.
     */
    private final static String LEAVE_ROOM_LABEL = "Leave breakout room";

    /**
     * Accessibility label to be used as selectors for finding Auto assign
     * button within the {@link ParticipantsPane}.
     */
    private final static String AUTO_ASSIGN_LABEL = "Auto assign to breakout rooms";

    /**
     * The participant.
     */
    private final WebParticipant participant;

    /**
     * Initializes a new {@link ParticipantsPane} instance.
     *
     * @param participant the participant for this {@link ParticipantsPane}.
     */
    public ParticipantsPane(WebParticipant participant)
    {
        this.participant = Objects.requireNonNull(participant, "participant");
    }

    /**
     * Try to click on the invite button and fail if it cannot be clicked.
     */
    public void clickInvite()
    {
        MeetUIUtils.clickOnElement(
            participant.getDriver(),
            MeetUIUtils.getAccessibilityCSSSelector(INVITE),
            true
        );
    }

    /**
     * Checks if a participant has video muted.
     *
     * @param participantToCheck - the participant to be checked
     * @param muted - whether the participant should have video muted or not
     */
    public void assertIsParticipantVideoMuted(WebParticipant participantToCheck, boolean muted)
    {
        boolean isOpen = isOpen();
        String remoteParticipantEndpointId = participantToCheck.getEndpointId();
        String iconXpath = "//div[@id='participant-item-" + remoteParticipantEndpointId
                + "']//div[contains(@class, 'indicators')]//*[name()='svg' and @id='videoMuted']";
        if (!isOpen)
        {
            Logger.getGlobal().log(Level.INFO, "Participants pane is not open, will open it.");
            open();
            Logger.getGlobal().log(Level.INFO, "Participants pane is now open:" + isOpen());
        }
        try
        {
            if (muted)
            {
                TestUtils.waitForElementByXPath(participant.getDriver(), iconXpath, 3);
            }
            else
            {
                TestUtils.waitForElementNotPresentOrNotDisplayedByXPath(participant.getDriver(), iconXpath, 3);
            }
        }
        catch (TimeoutException exc)
        {
            fail(
                    participantToCheck.getName() + (muted ? " should" : " shouldn't")
                            + " be muted at this point, xpath: " + iconXpath);
        }
        finally
        {
            if (!isOpen)
            {
                close();
            }
        }
    }

    /**
     * Trys to click ask to unmute button after moderator reloads.
     *
     * @param participantToUnmute the participant for this {@link ParticipantsPane} to unmute.
     */
    public void askToUnmute(WebParticipant participantToUnmute)
    {
        String remoteParticipantEndpointId = participantToUnmute.getEndpointId();

        WebElement meetingParticipantListItem = TestUtils.waitForElementBy(
            participant.getDriver(), By.id("participant-item-" + remoteParticipantEndpointId), 5);

        Actions hoverOnMeetingParticipantListItem = new Actions(participant.getDriver());
        hoverOnMeetingParticipantListItem.moveToElement(meetingParticipantListItem);
        hoverOnMeetingParticipantListItem.perform();

        clickAskToUnmuteButtonById(participantToUnmute);
    }

    /**
     * Trys to click add breakout room button.
     */
    public void addBreakoutRoom()
    {
        String cssSelector = MeetUIUtils.getAccessibilityCSSSelector(ADD_BREAKOUT_ROOM);
        TestUtils.waitForElementBy(participant.getDriver(),
                By.cssSelector(cssSelector), 5);
        clickAddBreakoutRoomButton();
    }

    /**
     * Trys to click auto assign to breakout rooms button.
     */
    public void autoAssignToBreakoutRooms()
    {
        String cssSelector = MeetUIUtils.getAccessibilityCSSSelector(AUTO_ASSIGN_LABEL);
        TestUtils.waitForElementBy(participant.getDriver(),
                By.cssSelector(cssSelector), 5);
        clickAutoAssignButton();
    }

    /**
     * Trys to click leave breakout room button.
     */
    public void leaveBreakoutRoom()
    {
        String cssSelector = MeetUIUtils.getAccessibilityCSSSelector(LEAVE_ROOM_LABEL);
        TestUtils.waitForElementBy(participant.getDriver(),
                By.cssSelector(cssSelector), 5);
        clickLeaveBreakoutRoomButton();
    }

    /**
     * Try to click on the ask to unmute button and fails if it cannot be clicked.
     * @param participantToUnmute the participant for this {@link ParticipantsPane} to unmute.
     */
    public void clickAskToUnmuteButtonById(WebParticipant participantToUnmute)
    {
        String remoteParticipantEndpointId = participantToUnmute.getEndpointId();
        String cssSelector = MeetUIUtils.getTestIdCSSSelector("unmute-" + remoteParticipantEndpointId);
        TestUtils.waitForElementBy(participant.getDriver(), By.cssSelector(cssSelector), 2);

        MeetUIUtils.clickOnElement(participant.getDriver(), cssSelector, true);
    }

    /**
     * Trys to click mute button after moderator reloads.
     *
     * @param participantToMute the participant for this {@link ParticipantsPane} to mute.
     */
    public void muteParticipant(WebParticipant participantToMute)
    {
        String remoteParticipantEndpointId = participantToMute.getEndpointId();

        WebElement meetingParticipantListItem = TestUtils.waitForElementBy(
                participant.getDriver(), By.id("participant-item-" + remoteParticipantEndpointId), 5);

        Actions hoverOnMeetingParticipantListItem = new Actions(participant.getDriver());
        hoverOnMeetingParticipantListItem.moveToElement(meetingParticipantListItem);
        hoverOnMeetingParticipantListItem.perform();

        clickMuteButtonById(participantToMute);
    }

    /**
     * Try to click on the mute button and fails if it cannot be clicked.
     * @param participantToMute the participant for this {@link ParticipantsPane} to unmute.
     */
    public void clickMuteButtonById(WebParticipant participantToMute)
    {
        String remoteParticipantEndpointId = participantToMute.getEndpointId();
        String cssSelector = MeetUIUtils.getTestIdCSSSelector("mute-" + remoteParticipantEndpointId);
        TestUtils.waitForElementBy(participant.getDriver(), By.cssSelector(cssSelector), 2);

        MeetUIUtils.clickOnElement(participant.getDriver(), cssSelector, true);
    }

    /**
     * Tries to click on the reject button and fails if it cannot be clicked.
     * @param participantIdToAdmit - the id of the participant for this {@link ParticipantsPane} to admit.
     */
    public void admitLobbyParticipant(String participantIdToAdmit)
    {
        String cssSelector = MeetUIUtils.getTestIdCSSSelector("admit-" + participantIdToAdmit);
        TestUtils.waitForElementBy(participant.getDriver(), By.cssSelector(cssSelector), 2);

        MeetUIUtils.clickOnElement(participant.getDriver(), cssSelector, true);
    }

    /**
     * Tries to click on the reject button and fails if it cannot be clicked.
     * @param participantIdToReject - the id of the participant for this {@link ParticipantsPane} to reject.
     */
    public void rejectLobbyParticipant(String participantIdToReject)
    {
        String cssSelector = MeetUIUtils.getTestIdCSSSelector("reject-" + participantIdToReject);
        TestUtils.waitForElementBy(participant.getDriver(), By.cssSelector(cssSelector), 2);

        MeetUIUtils.clickOnElement(participant.getDriver(), cssSelector, true);
    }

    /**
     * Trys to click on the context menu button and fails if it cannot be clicked.
     */
    public void clickContextMenuButton()
    {
        WebDriver driver = participant.getDriver();
        TestUtils.waitForElementBy(participant.getDriver(), By.id(CONTEXT_MENU), 3);
        WebElement contextMenuButton
                = driver.findElement(By.id(CONTEXT_MENU));

        contextMenuButton.click();
    }

    /**
     * Tries to click on add breakout room button and fails if it cannot be clicked.
     */
    private void clickAddBreakoutRoomButton()
    {
        MeetUIUtils.clickOnElement(
                participant.getDriver(),
                MeetUIUtils.getAccessibilityCSSSelector(ADD_BREAKOUT_ROOM),
                true
        );
    }

    /**
     * Tries to click on leave breakout room button and fails if it cannot be clicked.
     */
    private void clickLeaveBreakoutRoomButton()
    {
        MeetUIUtils.clickOnElement(
                participant.getDriver(),
                MeetUIUtils.getAccessibilityCSSSelector(LEAVE_ROOM_LABEL),
                true
        );
    }

    /**
     * Tries to click on auto assign button and fails if it cannot be clicked.
     */
    private void clickAutoAssignButton()
    {
        MeetUIUtils.clickOnElement(
                participant.getDriver(),
                MeetUIUtils.getAccessibilityCSSSelector(AUTO_ASSIGN_LABEL),
                true
        );
    }

    /**
     * Open context menu for given participant.
     *
     * @param remoteId id the participant for which to open the menu
     */
    private void openParticipantContextMenu(String remoteId)
    {
        WebElement listItem = TestUtils.waitForElementBy(participant.getDriver(),
                By.xpath("//div[@id='participant-item-" + remoteId + "']"), 5);

        Actions hoverOnParticipantListItem = new Actions(participant.getDriver());
        hoverOnParticipantListItem.moveToElement(listItem);
        hoverOnParticipantListItem.perform();

        String cssSelector = MeetUIUtils.getAccessibilityCSSSelector(PARTICIPANT_MORE_LABEL);
        listItem.findElement(By.cssSelector(cssSelector)).click();
    }

    /**
     * Tries to send a participant to a breakout room.
     *
     * @param participantToSend the participant to send to brekaout room
     * @param roomName the name of the breakout room where to send the participant
     */
    public void sendParticipantToBreakoutRoom(WebParticipant participantToSend, String roomName)
    {
        String remoteParticipantId = participantToSend.getEndpointId();

        openParticipantContextMenu(remoteParticipantId);

        String cssSelector = MeetUIUtils.getAccessibilityCSSSelector(roomName);
        WebElement sendButton = TestUtils.waitForElementBy(participant.getDriver(),
                By.cssSelector(cssSelector), 2);
        sendButton.click();
    }

    /**
     * Clicks the "participants" toolbar button to open the participants pane.
     */
    public void open()
    {
        participant.getToolbar().clickParticipantsButton();
        waitForVisible();
    }

    /**
     * Clicks the "participants" toolbar button to close the participants pane.
     */
    public void close()
    {
        participant.getToolbar().clickParticipantsButton();
        waitForHidden();
    }

    /**
     * Checks if the pane is open.
     * @return {@code true} if it's open, {@code false} otherwise
     */
    public boolean isOpen()
    {
        try
        {
            TestUtils.waitForElementBy(participant.getDriver(), By.className(PARTICIPANTS_PANE), 3);
            return true;
        }
        catch (TimeoutException ex)
        {
            return false;
        }
    }

    /**
     * Waits up to 3 seconds for the participants pane to be visible.
     */
    public void waitForVisible()
    {
        TestUtils.waitForElementDisplayToBe(
            participant.getDriver(),
            By.className(PARTICIPANTS_PANE),
            3,
            true);
    }

    /**
     * Waits up to 3 seconds for the participants pane to be hidden.
     */
    public void waitForHidden()
    {
        TestUtils.waitForElementDisplayToBe(
                participant.getDriver(),
                By.className(PARTICIPANTS_PANE),
                3,
                false);
    }
}
