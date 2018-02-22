/*
 * Copyright @ 2015-2018 Atlassian Pty Ltd
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
package org.jitsi.meet.test.pageobjects.web;

import java.util.*;

import org.jitsi.meet.test.util.*;
import org.jitsi.meet.test.web.*;
import org.openqa.selenium.*;

/**
 * Represents the info dialog in a particular {@link WebParticipant}.
 *
 * @author Leonard Kim
 */
public class InfoDialog
{
    /**
     * Class names to be used as selectors for finding WebElements within the
     * {@link InfoDialog}.
     */
    private final static String ADD_PASSWORD_LINK = "add-password";
    private final static String ADD_PASSWORD_FIELD = "info-password-input";
    private final static String CONFERENCE_URL = "info-dialog-conference-url";
    private final static String INFO_DIALOG_BUTTON = "toolbar_button_info";
    private final static String INFO_DIALOG_CONTAINER = "info-dialog";
    private final static String LOCAL_LOCK = "info-password-local";
    private final static String MORE_NUMBERS = "more-numbers";
    private final static String PHONE_NUMBER = "phone-number";
    private final static String CONFERENCE_ID = "conference-id";
    private final static String REMOTE_LOCK = "info-password-remote";
    private final static String REMOVE_PASSWORD = "remove-password";

    /**
     * The participant used to interact with the info dialog.
     */
    private final WebParticipant participant;

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
    public void addPassword(String password)
    {
        WebDriver participantDriver = participant.getDriver();

        open();

        participantDriver.findElement(By.className(ADD_PASSWORD_LINK)).click();

        TestUtils.waitForElementBy(
            participantDriver,
            By.className(ADD_PASSWORD_FIELD),
            5);
        WebElement passwordEntry
            = participantDriver.findElement(By.className(ADD_PASSWORD_FIELD));

        passwordEntry.sendKeys(password);
        passwordEntry.sendKeys(Keys.RETURN);
    }

    /**
     * Clicks the link to open a page to show all available dial in numbers.
     */
    public void openDialInNumbersPage()
    {
        open();

        WebDriver driver = participant.getDriver();
        WebElement moreNumbersElement
            = driver.findElement(By.className(MORE_NUMBERS));

        moreNumbersElement.click();
    }

    /**
     * Clicks the "info" toolbar button which opens or closes the info dialog.
     */
    private void clickToolbarButton()
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
    public void close()
    {
        if (!isOpen())
        {
            return;
        }

        clickToolbarButton();
    }

    /**
     * Finds and returns the info dialog.
     *
     * @return The {@code WebElement} for the info dialog or null if it cannot
     * be found.
     */
    public WebElement get()
    {
        List<WebElement> elements
            = participant.getDriver().findElements(
                By.className(INFO_DIALOG_CONTAINER));

        return elements.isEmpty() ? null : elements.get(0);
    }

    /**
     * Gets the string that contains the url to the current conference.
     *
     * @return {@code String} for the current conference's url.
     */
    public String getMeetingURL()
    {
        return this.getValueAfterColon(CONFERENCE_URL);
    }

    /**
     * Gets the string that contains the dial in number for the current
     * conference.
     *
     * @return {@code String} for the current conference's dial in number.
     */
    public String getDialInNumber()
    {
        return this.getValueAfterColon(PHONE_NUMBER);
    }

    /**
     * Gets the string that contains the pin number needed for dialing into
     * the current conference.
     *
     * @return {@code String} for the current conference's pin number.
     */
    public String getPinNumber()
    {
        return this.getValueAfterColon(CONFERENCE_ID);
    }

    /**
     * Checks if the current conference is locked based on the info dialog's
     * display state.
     *
     * @return {@code true} if the conference is displayed as locked in the
     * info dialog, {@code false} otherwise.
     */
    public boolean isLocked()
    {
        return isLockedLocally() || isLockedRemotely();
    }

    /**
     * Checks if the current conference is locked with a locally set password.
     *
     * @return {@code true} if the conference is displayed as locked locally in
     * the info dialog, {@code false} otherwise.
     */
    private boolean isLockedLocally()
    {
        return getLockStateByClass(LOCAL_LOCK);
    }

    /**
     * Checks if the current conference is locked with a locally set password.
     *
     * @return {@code true}  if the conference is displayed as locked remotely
     * in the info dialog, {@code false} otherwise.
     */
    private boolean isLockedRemotely()
    {
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
        WebElement dialog = get();

        return dialog != null;
    }

    /**
     * Clicks the "info" toolbar button to open the info dialog, if the info
     * dialog is closed.
     */
    public void open()
    {
        if (isOpen())
        {
            return;
        }

        this.clickToolbarButton();
    }

    /**
     * Removes the password from the current conference through the info dialog,
     * if a password is set.
     */
    public void removePassword()
    {
        if (!isLocked())
        {
            return;
        }

        this.open();

        WebDriver driver = participant.getDriver();
        WebElement removePasswordElement
            = driver.findElement(By.className(REMOVE_PASSWORD));

        if (removePasswordElement != null)
        {
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
    private boolean getLockStateByClass(String className)
    {
        open();

        WebDriver driver = participant.getDriver();
        return driver.findElements(By.className(className)).size() != 0;
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
    private String getValueAfterColon(String className)
    {
        open();

        WebDriver driver = participant.getDriver();
        String fullText
            = driver.findElement(By.className(className)).getText();

        return fullText.split(":")[1].trim();
    }
}
