package org.jitsi.meet.test.pageobjects.web;

import org.jitsi.meet.test.util.*;
import org.jitsi.meet.test.web.*;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;

import java.util.*;

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
        WebElement contextMenuButton
                = driver.findElement(By.id(CONTEXT_MENU));

        contextMenuButton.click();
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
