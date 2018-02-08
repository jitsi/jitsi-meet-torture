package org.jitsi.meet.test.pageobjects.web;

import org.jitsi.meet.test.util.MeetUIUtils;
import org.jitsi.meet.test.util.TestUtils;
import org.jitsi.meet.test.web.WebParticipant;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.Objects;

/**
 * Represents the info dialog in a particular {@link WebParticipant}.
 *
 * @author Leonard Kim
 */
public class InfoDialog
{
    /**
     * The participant used to interact with the info dialog.
     */
    private final WebParticipant participant;

    /**
     * Classnames to be used as selectors for finding WebElements within the
     * {@link InfoDialog}.
     */
    private final String ADD_PASSWORD_LINK = "add-password";
    private final String ADD_PASSWORD_FIELD = "info-password-input";
    private final String CONFERENCE_URL = "info-dialog-conference-url";
    private final String INFO_DIALOG_BUTTON = "toolbar_button_info";
    private final String INFO_DIALOG_CONTAINER = "info-dialog";
    private final String LOCAL_LOCK = "info-password-local";
    private final String MORE_NUMBERS = "more-numbers";
    private final String PHONE_NUMBER = "phone-number";
    private final String PIN_NUMBER = "pin-number";
    private final String REMOTE_LOCK = "info-password-remote";
    private final String REMOVE_PASSWORD
        = "remove-password";

    /**
     * Initializes a new {@link InfoDialog} instance.
     *
     * @param participant the participant for this {@link InfoDialog}.
     */
    public InfoDialog(WebParticipant participant)
    {
        this.participant = Objects.requireNonNull(participant, "participant");
    }

    /**
     * Sets a password on the current conference to lock it.
     *
     * @param password - The password to use to lock the conference.
     */
    public void addPassword(String password) {
        WebDriver participantDriver = participant.getDriver();

        this.open();

        WebElement dialog = this.get();

        dialog.findElement(By.className(ADD_PASSWORD_LINK)).click();

        TestUtils.waitForElementBy(
            participantDriver,
            By.className(ADD_PASSWORD_FIELD),
            5);
        WebElement passwordEntry
            = dialog.findElement(By.className(ADD_PASSWORD_FIELD));

        passwordEntry.sendKeys(password);
        passwordEntry.sendKeys(Keys.RETURN);
    }

    /**
     * Clicks the link to open a page to show all available dial in numbers.
     */
    public void openDialInNumbersPage() {
        this.open();

        WebElement dialog = this.get();

        WebElement moreNumbersElement
            = dialog.findElement(By.className(MORE_NUMBERS));

        moreNumbersElement.click();
    }

    /**
     * Clicks the "info" toolbar button which opens or closes the info dialog.
     */
    public void clickToolbarButton()
    {
        MeetUIUtils.clickOnButton(
            participant.getDriver(),
            INFO_DIALOG_BUTTON,
            true);
    }

    /**
     * Clicks the "info" toolbar button to close the info dialog, if the info
     * dialog is open.
     */
    public void close() {
        if (this.isOpen() == false) {
            return;
        }

        this.clickToolbarButton();
    }

    /**
     * Finds and returns the info dialog.
     *
     * @return The {@code WebElement} for the info dialog or null if it cannot
     * be found.
     */
    public WebElement get() {
        List<WebElement> elements = participant.getDriver().findElements(
            By.className(INFO_DIALOG_CONTAINER));

        return elements.size() > 0 ? elements.get(0) : null;
    }

    /**
     * Gets the string that contains the url to the current conference.
     *
     * @return {@code String} for the current conference's url.
     */
    public String getMeetingURL() {
        return this.getValueAfterColon(CONFERENCE_URL);
    }

    /**
     * Gets the string that contains the dial in number for the current
     * conference.
     *
     * @return {@code String} for the current conference's dial in number.
     */
    public String getDialInNumber() {
        return this.getValueAfterColon(PHONE_NUMBER);
    }

    /**
     * Gets the string that contains the pin number needed for dialing into
     * the current conference.
     *
     * @return {@code String} for the current conference's pin number.
     */
    public String getPinNumber() {
        return this.getValueAfterColon(PIN_NUMBER);
    }

    /**
     * Checks if the current conference is locked based on the info dialog's
     * display state.
     *
     * @return {@code true} if the conference is displayed as locked in the
     * info dialog, {@code false} otherwise.
     */
    public boolean isLocked() {
        return this.isLockedLocally() || this.isLockedRemotely();
    }

    /**
     * Checks if the current conference is locked with a locally set password.
     *
     * @return {@code true} if the conference is displayed as locked locally in
     * the info dialog, {@code false} otherwise.
     */
    public boolean isLockedLocally() {
        return getLockStateByClass(LOCAL_LOCK);
    }

    /**
     * Checks if the current conference is locked with a locally set password.
     *
     * @return {@code true}  if the conference is displayed as locked remotely
     * in the info dialog, {@code false} otherwise.
     */
    public boolean isLockedRemotely() {
        return getLockStateByClass(REMOTE_LOCK);
    }

    /**
     * Checks if the info dialog is currently displayed.
     *
     * @return {@code true} if the info dialog is displayed, {@code false}
     * otherwise.
     */
    public boolean isOpen()
    {
        WebElement dialog = this.get();

        return dialog != null;
    }

    /**
     * Clicks the "info" toolbar button to open the info dialog, if the info
     * dialog is closed.
     */
    public void open() {
        if (this.isOpen()) {
            return;
        }

        this.clickToolbarButton();
    }

    /**
     * Removes the password from the current conference through the info dialog,
     * if a password is set.
     */
    public void removePassword() {
        if (!this.isLocked()) {
            return;
        }

        this.open();

        WebElement dialog = this.get();
        WebElement removePasswordElement
            = dialog.findElement(By.className(REMOVE_PASSWORD));

        if (removePasswordElement != null) {
            removePasswordElement.click();
        }
    }

    /**
     * Private helper to determine if the passed in className is displayed.
     * Used internally to determine if what kind of conference lock state the
     * info dialog is display.
     *
     * @param className - The class name to search for that signifies the
     *                  current lock state of the conference.
     * @return {@code true} if the info dialog has the passed in class,
     * {@code false} otherwise.
     */
    private boolean getLockStateByClass(String className) {
        this.open();

        WebElement dialog = this.get();

        return dialog.findElements(By.className(className)).size() != 0;
    }

    /**
     * Private helper to get values after colons. The info dialog lists
     * conference specific information after a label, followed by a colon.
     *
     * @param className - The class name to search for that signifies the
     *                  current lock state of the conference.
     * @return {@code true} if the info dialog has the passed in class,
     * {@code false} otherwise.
     */
    private String getValueAfterColon(String className) {
        this.open();

        WebElement dialog = this.get();

        String fullText = dialog.findElement(By.className(className))
            .getText();

        return fullText.split(":")[1].trim();
    }
}
