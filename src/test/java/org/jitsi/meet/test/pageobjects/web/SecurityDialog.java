/*
 * Copyright @ 2020-present 8x8, Inc.
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
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.interactions.*;
import org.openqa.selenium.support.ui.*;

/**
 * Represents the security dialog in a particular {@link WebParticipant}.
 */
public class SecurityDialog
{
    /**
     * Class names to be used as selectors for finding WebElements within the
     * {@link SecurityDialog}.
     */
    private final static String ADD_PASSWORD_LINK = "add-password";
    private final static String ADD_PASSWORD_FIELD = "info-password-input";
    private final static String DIALOG_CONTAINER = "security-dialog";
    private final static String LOCAL_LOCK = "info-password-local";
    private final static String REMOTE_LOCK = "info-password-remote";
    private final static String REMOVE_PASSWORD = "remove-password";
    private final static String LOBBY_SECTION_ID = "lobby-section";

    /**
     * The participant used to interact with the security dialog.
     */
    private final WebParticipant participant;

    /**
     * Initializes a new {@link SecurityDialog} instance.
     *
     * @param participant the participant for this {@link SecurityDialog}.
     */
    public SecurityDialog(WebParticipant participant)
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
        WebDriver driver = participant.getDriver();

        open();

        // we see failures where clicking the password link does not propagate
        // and does not show the password input field. Adding a wait trying to fix this
        TestUtils.waitMillis(1000);

        WebElement addPasswordLink = driver.findElement(By.className(ADD_PASSWORD_LINK));

        new WebDriverWait(driver, 5).until(ExpectedConditions.elementToBeClickable(addPasswordLink));

        new Actions(driver).click(addPasswordLink).perform();

        TestUtils.waitForElementBy(
            driver,
            By.className(ADD_PASSWORD_FIELD),
            5);
        WebElement passwordEntry
            = driver.findElement(By.className(ADD_PASSWORD_FIELD));

        passwordEntry.sendKeys(password);
        passwordEntry.sendKeys(Keys.RETURN);

        String validationMessage = null;

        // There are two cases here, validation is enabled and the field passwordEntry maybe there
        // with validation failed, or maybe successfully hidden after setting the password
        // So let's give it some time to act on any of the above
        TestUtils.waitMillis(1000);

        if (driver.findElements(By.className(ADD_PASSWORD_FIELD)).size() > 0)
        {
            // validation had failed on password field as it is still on the page
            validationMessage = passwordEntry.getAttribute("validationMessage");
        }

        if (validationMessage != null && validationMessage.length() > 0)
        {
            passwordEntry.sendKeys(Keys.ESCAPE);
            throw new ValidationError(validationMessage);
        }
    }

    /**
     * Clicks the "info" toolbar button which opens or closes the security dialog.
     */
    private void clickToolbarButton()
    {
        participant.getToolbar().clickSecurityButton();
    }

    /**
     * Clicks the "info" toolbar button to close the security dialog, if the info
     * dialog is open.
     */
    public void close()
    {
        if (!isOpen())
        {
            return;
        }

        clickToolbarButton();

        // waits till it is closed
        new WebDriverWait(participant.getDriver(), 6).until(
            (ExpectedCondition<Boolean>) d -> !isOpen());
    }

    /**
     * Finds and returns the security dialog.
     *
     * @return The {@code WebElement} for the security dialog or null if it cannot
     * be found.
     */
    public WebElement get()
    {
        List<WebElement> elements
            = participant.getDriver().findElements(
                By.className(DIALOG_CONTAINER));

        return elements.isEmpty() ? null : elements.get(0);
    }

    /**
     * Checks if the current conference is locked based on the security dialog's
     * display state.
     *
     * @return {@code true} if the conference is displayed as locked in the
     * security dialog, {@code false} otherwise.
     */
    public boolean isLocked()
    {
        return isLockedLocally() || isLockedRemotely();
    }

    /**
     * Checks if the current conference is locked with a locally set password.
     *
     * @return {@code true} if the conference is displayed as locked locally in
     * the security dialog, {@code false} otherwise.
     */
    private boolean isLockedLocally()
    {
        return getLockStateByClass(LOCAL_LOCK);
    }

    /**
     * Checks if the current conference is locked with a locally set password.
     *
     * @return {@code true}  if the conference is displayed as locked remotely
     * in the security dialog, {@code false} otherwise.
     */
    private boolean isLockedRemotely()
    {
        return getLockStateByClass(REMOTE_LOCK);
    }

    /**
     * Checks if the security dialog is currently displayed.
     *
     * @return {@code true} if the security dialog is displayed, {@code false}
     * otherwise.
     */
    public boolean isOpen()
    {
        WebElement dialog = get();

        return dialog != null;
    }

    /**
     * Clicks the "info" toolbar button to open the security dialog, if the info
     * dialog is closed.
     */
    public void open()
    {
        if (isOpen())
        {
            return;
        }

        this.clickToolbarButton();

        // waits till its open
        new WebDriverWait(participant.getDriver(), 3).until(
            (ExpectedCondition<Boolean>) d -> isOpen());

        // give time the dialog to settle before operating on it, we see failures where click button handlers are
        // not triggered when clicked shortly after opening dialog, not always reproducible
        TestUtils.waitMillis(500);
    }

    /**
     * Removes the password from the current conference through the security dialog,
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

        new WebDriverWait(driver, 5).until(
            ExpectedConditions.elementToBeClickable(By.className(REMOVE_PASSWORD)));

        WebElement removePasswordElement
            = driver.findElement(By.className(REMOVE_PASSWORD));

        if (removePasswordElement != null)
        {
            new Actions(driver).click(removePasswordElement).perform();
        }
    }

    /**
     * Private helper to determine if the passed in className is displayed.
     * Used internally to determine if what kind of conference lock state the
     * security dialog is display.
     *
     * @param className - The class name to search for that signifies the
     *                  current lock state of the conference.
     * @return {@code true} if the security dialog has the passed in class,
     * {@code false} otherwise.
     */
    private boolean getLockStateByClass(String className)
    {
        open();

        WebDriver driver = participant.getDriver();
        return driver.findElements(By.className(className)).size() != 0;
    }

    /**
     * Returns the switch that can be used to detect lobby state or change lobby state.
     * @return the lobby switch UI element.
     */
    private WebElement getLobbySwitch()
    {
        WebDriver driver = participant.getDriver();
        WebElement lobbySection = driver.findElement(By.id(LOBBY_SECTION_ID));

        return lobbySection.findElement(By.tagName("input"));
    }

    /**
     * Checks if the current conference has lobby enabled based on the security dialog's display state.
     *
     * @return {@code true} if the conference has lobby enabled in the security dialog, {@code false} otherwise.
     */
    public boolean isLobbyEnabled()
    {
        open();

        WebElement lobbySwitch = getLobbySwitch();

        return lobbySwitch != null && lobbySwitch.isSelected();
    }

    /**
     * Toggles Lobby.
     */
    public void toggleLobby()
    {
        WebDriver driver = participant.getDriver();
        WebElement lobbySwitch = getLobbySwitch();

        if (lobbySwitch == null)
        {
            throw new NoSuchElementException("Lobby switch not found!");
        }

        new Actions(driver).moveToElement(lobbySwitch).click().perform();
    }

    /**
     * Checks whether lobby section is present in the UI.
     * @return {@code true} if the lobby switch is displayed in the security dialog, {@code false} otherwise.
     */
    public boolean isLobbySectionPresent()
    {
        try
        {
            WebElement lobbySwitch = getLobbySwitch();
            return lobbySwitch != null;
        }
        catch(NoSuchElementException e)
        {
            return false;
        }
    }
}
