package org.jitsi.meet.test.pageobjects.web;

import org.jitsi.meet.test.util.*;
import org.jitsi.meet.test.web.*;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;

import java.util.*;

public class SettingsDialog
{
    /**
     * Selectors to be used for finding WebElements within the
     * {@link SettingsDialog}. While most are CSS selectors, some are XPath
     * selectors, and prefixed as such, due to the extra functionality XPath
     * provides.
     */
    private final static String DISPLAY_NAME_FIELD
        = "#setDisplayName";
    private final static String EMAIL_FIELD = "#setEmail";
    private final static String FOLLOW_ME_CHECKBOX = "[name='follow-me']";
    private final static String HIDE_SELF_VIEW_CHECKBOX = "[name='hide-self-view']";
    private final static String MORE_TAB_CONTENT = ".more-tab";
    private final static String MODERATOR_TAB_CONTENT = ".moderator-tab";
    private final static String SETTINGS_DIALOG = ".settings-dialog";
    private final static String SETTINGS_DIALOG_CONTENT = ".settings-pane";
    private final static String START_AUDIO_MUTED_CHECKBOX
        = "[name='start-audio-muted']";
    private final static String START_VIDEO_MUTED_CHECKBOX
        = "[name='start-video-muted']";
    private final static String X_PATH_MORE_TAB
        =  "//div[contains(@class, 'settings-dialog')]//*[text()='More']";
    private final static String X_PATH_MODERATOR_TAB
        =  "//div[contains(@class, 'settings-dialog')]//*[text()='Moderator']";
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
        ModalDialogHelper.clickCancelButton(participant.getDriver());
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
        try
        {
            openModeratorTab();
        }
        catch (Exception e)
        {
            // if there is no access to the Moderator tab the settings in it won't be displayed
            return false;
        }

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

        TestUtils.waitForElementBy(
            participant.getDriver(),
            By.cssSelector(MORE_TAB_CONTENT),
            5);
    }

    /**
     * Selects the Moderator tab to be displayed.
     */
    public void openModeratorTab()
    {
        openTab(X_PATH_MODERATOR_TAB);

        TestUtils.waitForElementBy(
            participant.getDriver(),
            By.cssSelector(MODERATOR_TAB_CONTENT),
            5);
    }

    /**
     * Selects the Profile tab to be displayed.
     */
    public void openProfileTab()
    {
       openTab(X_PATH_PROFILE_TAB);
    }

    /**
     * Sets the option for hiding the self view.
     */
    public void setHideSelfView(boolean enable)
    {
        openMoreTab();
        setCheckbox(HIDE_SELF_VIEW_CHECKBOX, enable);
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
        openModeratorTab();
        setCheckbox(FOLLOW_ME_CHECKBOX, enable);
    }

    /**
     * Sets the option for having participants join as audio muted.
     */
    public void setStartAudioMuted(boolean enable)
    {
        openModeratorTab();
        setCheckbox(START_AUDIO_MUTED_CHECKBOX, enable);
    }

    /**
     * Sets the option for having participants join as video muted.
     */
    public void setStartVideoMuted(boolean enable)
    {
        openModeratorTab();
        setCheckbox(START_VIDEO_MUTED_CHECKBOX, enable);
    }

    /**
     * Clicks the OK button on the settings dialog to close the dialog
     * and save any changes made.
     */
    public void submit()
    {
        ModalDialogHelper.clickOKButton(participant.getDriver());
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

        TestUtils.waitForElementBy(
            participant.getDriver(),
            By.cssSelector(SETTINGS_DIALOG_CONTENT),
            5);
    }

    /**
     * Displays a specific tab in the settings dialog.
     */
    private void openTab(String xPathSelectorForTab)
    {
        WebDriver driver = participant.getDriver();

        TestUtils.waitForElementBy(driver, By.xpath(xPathSelectorForTab), 5);
        WebDriverWait wait = new WebDriverWait(driver, 10);
        WebElement element = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath(xPathSelectorForTab)));

        element.click();
    }

    /**
     * Sets the state checked/selected of a checkbox in the settings dialog.
     */
    private void setCheckbox(String cssSelector, boolean check)
    {
        WebDriver driver = participant.getDriver();
        TestUtils.waitForElementBy(driver, By.cssSelector(cssSelector), 5);
        WebElement checkbox = driver.findElement(By.cssSelector(cssSelector));

        boolean isChecked = checkbox.isSelected();

        if (check != isChecked)
        {
            ((JavascriptExecutor)driver).executeScript("arguments[0].click();", checkbox);
        }
    }
}
