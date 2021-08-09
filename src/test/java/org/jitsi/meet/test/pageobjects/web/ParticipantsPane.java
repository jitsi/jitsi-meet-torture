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

    private final static String ASK_TO_UNMUTE = "Ask to unmute";


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
     * Trys to click ask to unmute button.
     *
     * @param moderator the participant for this {@link ParticipantsPane} that has moderator rights.
     */
    public void askToUnmute(WebParticipant moderator)
    {
        WebDriver driver = moderator.getDriver();
        WebElement meetingParticipantListItem = driver.findElement(By.id("raised-hand-participant"));

        Actions hoverOnMeetingParticipantListItem = new Actions(driver);
        hoverOnMeetingParticipantListItem.moveToElement(meetingParticipantListItem);
        hoverOnMeetingParticipantListItem.perform();

        clickAskToUnmuteButton();
    }

    /**
     * Try to click on the ask to unmute button and fails if it cannot be clicked.
     */
    public void clickAskToUnmuteButton()
    {
        MeetUIUtils.clickOnElement(
                participant.getDriver(),
                MeetUIUtils.getAccessibilityCSSSelector(ASK_TO_UNMUTE),
                true
        );
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
