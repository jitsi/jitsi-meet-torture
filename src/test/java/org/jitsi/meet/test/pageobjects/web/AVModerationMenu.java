package org.jitsi.meet.test.pageobjects.web;

import org.jitsi.meet.test.web.WebParticipant;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.Objects;

/**
 * Represents the av moderation context menu in a particular {@link WebParticipant}.
 *
 * @author Calin Chitu
 */
public class AVModerationMenu
{
    /**
     * ID of the moderation context menu items
     */

    private final static String START_MODERATION = "participants-pane-context-menu-start-moderation";

    private final static String STOP_MODERATION = "participants-pane-context-menu-stop-moderation";

    /**
     * The participant.
     */
    private final WebParticipant participant;

    /**
     * Initializes a new {@link AVModerationMenu} instance.
     *
     * @param participant the participant for this {@link AVModerationMenu}.
     */
    public AVModerationMenu(WebParticipant participant)
    {
        this.participant = Objects.requireNonNull(participant, "participant");
    }

    /**
     * Trys to click on the start moderation menu item and fails if it cannot be clicked.
     */
    public void clickStartModeration()
    {
        WebDriver driver = participant.getDriver();
        WebElement startModerationMenuItem
                = driver.findElement(By.id(START_MODERATION));

        startModerationMenuItem.click();
    }

    /**
     * Trys to click on the stop moderation menu item and fails if it cannot be clicked.
     */
    public void clickStopModeration()
    {
        WebDriver driver = participant.getDriver();
        WebElement startModerationMenuItem
                = driver.findElement(By.id(STOP_MODERATION));

        startModerationMenuItem.click();
    }
}
