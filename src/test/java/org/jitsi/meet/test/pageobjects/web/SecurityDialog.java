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

        driver.findElement(By.className(ADD_PASSWORD_LINK)).click();

        TestUtils.waitForElementBy(
            driver,
            By.className(ADD_PASSWORD_FIELD),
            5);
        WebElement passwordEntry
            = driver.findElement(By.className(ADD_PASSWORD_FIELD));

        passwordEntry.sendKeys(password);
        passwordEntry.sendKeys(Keys.RETURN);
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
}
