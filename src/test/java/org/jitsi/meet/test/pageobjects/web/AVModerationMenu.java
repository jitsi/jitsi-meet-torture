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

    private final static String START_AUDIO_MODERATION = "participants-pane-context-menu-start-audio-moderation";

    private final static String START_VIDEO_MODERATION = "participants-pane-context-menu-start-video-moderation";

    private final static String STOP_AUDIO_MODERATION = "participants-pane-context-menu-stop-audio-moderation";

    private final static String STOP_VIDEO_MODERATION = "participants-pane-context-menu-stop-video-moderation";

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
     * Tries to click on the start audio moderation menu item and fails if it cannot be clicked.
     */
    public void clickStartAudioModeration()
    {
        WebDriver driver = participant.getDriver();
        WebElement startModerationMenuItem
                = driver.findElement(By.id(START_AUDIO_MODERATION));

        startModerationMenuItem.click();
    }

    /**
     * Tries to click on the start video moderation menu item and fails if it cannot be clicked.
     */
    public void clickStartVideoModeration()
    {
        WebDriver driver = participant.getDriver();
        WebElement startModerationMenuItem
                = driver.findElement(By.id(START_VIDEO_MODERATION));

        startModerationMenuItem.click();
    }

    /**
     * Tries to click on the stop audio moderation menu item and fails if it cannot be clicked.
     */
    public void clickStopAudioModeration()
    {
        WebDriver driver = participant.getDriver();
        WebElement startModerationMenuItem
                = driver.findElement(By.id(STOP_AUDIO_MODERATION));

        startModerationMenuItem.click();
    }

    /**
     * Tries to click on the stop video moderation menu item and fails if it cannot be clicked.
     */
    public void clickStopVideoModeration()
    {
        WebDriver driver = participant.getDriver();
        WebElement startModerationMenuItem
                = driver.findElement(By.id(STOP_VIDEO_MODERATION));

        startModerationMenuItem.click();
    }
}
