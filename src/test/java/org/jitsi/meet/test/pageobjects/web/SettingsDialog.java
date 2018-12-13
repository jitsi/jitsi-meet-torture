package org.jitsi.meet.test.pageobjects.web;

import org.jitsi.meet.test.util.*;
import org.jitsi.meet.test.web.*;
import org.openqa.selenium.*;

import java.util.*;

public class SettingsDialog
{
    /**
     * Selectors to be used for finding WebElements within the
     * {@link SettingsDialog}. While most are CSS selectors, some are XPath
     * selectors, and prefixed as such, due to the extra functionality XPath
     * provides.
     */
    private final static String CANCEL_BUTTON = "#modal-dialog-cancel-button";
    private final static String DISPLAY_NAME_FIELD
        = "#setDisplayName";
    private final static String EMAIL_FIELD = "#setEmail";
    private final static String FOLLOW_ME_CHECKBOX = "[name='follow-me'] ~ div";
    private final static String OK_BUTTON = "#modal-dialog-ok-button";
    private final static String SETTINGS_DIALOG = ".settings-dialog";
    private final static String START_AUDIO_MUTED_CHECKBOX
        = "[name='start-audio-muted'] ~ div";
    private final static String START_VIDEO_MUTED_CHECKBOX
        = "[name='start-video-muted'] ~ div";
    private final static String X_PATH_MORE_TAB
        =  "//div[contains(@class, 'settings-dialog')]//*[text()='More']";
    private final static String X_PATH_PROFILE_TAB
        =  "//div[contains(@class, 'settings-dialog')]//*[text()='Profile']";

    /**
     * The participant.
     */
    private final WebParticipant participant;

    /**
     * Initializes a new {@link SettingsDialog} instance.
     * @param participant the participant for this {@link SettingsDialog}.
     */
    public SettingsDialog(WebParticipant participant)
    {
        this.participant = Objects.requireNonNull(participant, "participant");
    }

    /**
     * Clicks the cancel button on the settings dialog to close the dialog
     * without saving any changes.
     */
    public void close()
    {
        participant.getDriver()
            .findElement(By.cssSelector(CANCEL_BUTTON))
            .click();
    }

    /**
     * Returns the participant's display name displayed in the settings dialog.
     *
     * @return {@code string} The display name displayed in the settings dialog.
     */
    public String getDisplayName()
    {
        openProfileTab();

        return participant.getDriver()
            .findElement(By.cssSelector(DISPLAY_NAME_FIELD))
            .getAttribute("value");
    }

    /**
     * Returns the participant's email displayed in the settings dialog.
     *
     * @return {@code string} The email displayed in the settings dialog.
     */
    public String getEmail()
    {
        openProfileTab();

        return participant.getDriver()
            .findElement(By.cssSelector(EMAIL_FIELD))
            .getAttribute("value");
    }

    /**
     * Returns whether or not the Follow Me checkbox is visible to the user in
     * the settings dialog.
     *
     * @return {@code boolean} True if the Follow Me checkbox is visible to the
     * user.
     */
    public boolean isFollowMeDisplayed()
    {
        openMoreTab();

        WebDriver driver = participant.getDriver();
        List<WebElement> followMeCheckboxes
            = driver.findElements(By.cssSelector(FOLLOW_ME_CHECKBOX));

        return followMeCheckboxes.size() > 0;
    }

    /**
     * Selects the More tab to be displayed.
     */
    public void openMoreTab()
    {
        openTab(X_PATH_MORE_TAB);
    }

    /**
     * Selects the Profile tab to be displayed.
     */
    public void openProfileTab()
    {
       openTab(X_PATH_PROFILE_TAB);
    }

    /**
     * Enters the passed in email into the email field.
     */
    public void setEmail(String email)
    {
        openProfileTab();
        WebDriver driver = participant.getDriver();
        driver.findElement(By.cssSelector(EMAIL_FIELD)).sendKeys(email);
    }

    /**
     * Sets the option for having other participants automatically perform the
     * actions performed by the local participant.
     */
    public void setFollowMe(boolean enable)
    {
        openMoreTab();
        setCheckbox(FOLLOW_ME_CHECKBOX, enable);
    }

    /**
     * Sets the option for having participants join as audio muted.
     */
    public void setStartAudioMuted(boolean enable)
    {
        openMoreTab();
        setCheckbox(START_AUDIO_MUTED_CHECKBOX, enable);
    }

    /**
     * Sets the option for having participants join as video muted.
     */
    public void setStartVideoMuted(boolean enable)
    {
        openMoreTab();
        setCheckbox(START_VIDEO_MUTED_CHECKBOX, enable);
    }

    /**
     * Clicks the OK button on the settings dialog to close the dialog
     * and save any changes made.
     */
    public void submit()
    {
        participant.getDriver()
            .findElement(By.cssSelector(OK_BUTTON))
            .click();
    }

    /**
     * Waits for the settings dialog to be visible.
     */
    public void waitForDisplay()
    {
        TestUtils.waitForElementBy(
            participant.getDriver(),
            By.cssSelector(SETTINGS_DIALOG),
            5);
    }

    /**
     * Displays a specific tab in the settings dialog.
     */
    private void openTab(String xPathSelectorForTab)
    {
        WebDriver driver = participant.getDriver();
        By tabElement = By.xpath(xPathSelectorForTab);

        TestUtils.waitForElementBy(driver, tabElement, 5);
        driver.findElement(tabElement).click();
    }

    /**
     * Sets the state checked/selected of a checkbox in the settings dialog.
     */
    private void setCheckbox(String cssSelector, boolean check)
    {
        WebDriver driver = participant.getDriver();
        By checkboxElement = By.cssSelector(cssSelector);
        TestUtils.waitForElementBy(driver, checkboxElement, 5);
        WebElement checkbox = driver.findElement(checkboxElement);
        boolean isChecked = checkbox.isSelected();

        if (check != isChecked) {
            checkbox.click();
        }
    }
}
