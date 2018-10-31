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

import org.jitsi.meet.test.util.TestUtils;
import org.jitsi.meet.test.web.*;
import org.openqa.selenium.*;

import java.util.*;

/**
 * Represents the dialog for submitting a password to enter a locked conference.
 */
public class PasswordPromptDialog
{
    /**
     * Selectors used to find WebElements within {@link PasswordPromptDialog}.
     */
    private static final String SUBMIT_BUTTON_ID = "modal-dialog-ok-button";

    /**
     * The participant used to interact with this {@link PasswordPromptDialog}.
     */
    private final WebParticipant participant;

    /**
     * Initializes a new {@link PasswordPromptDialog} instance.
     *
     * @param participant the participant for this {@link PasswordPromptDialog}.
     */
    public PasswordPromptDialog(WebParticipant participant)
    {
        this.participant = Objects.requireNonNull(participant, "participant");
    }

    /**
     * Checks if the {@link PasswordPromptDialog} is currently displayed.
     *
     * @return {@code true} if the dialog is displayed, {@code false} otherwise.
     */
    public boolean isOpen()
    {
        WebElement passwordInput = getPasswordInput();

        return passwordInput != null && passwordInput.isDisplayed();
    }

    /**
     * Enters the passed in password and submits the password for validation.
     */
    public void submitPassword(String password)
    {
        WebElement passwordInput = getPasswordInput();

        passwordInput.sendKeys(password);
        participant.getDriver().findElement(By.id(SUBMIT_BUTTON_ID)).click();
    }

    /**
     * Finds and returns the input field for typing in a password.
     *
     * @return {@link WebElement} The input field for entering a password.
     */
    private WebElement getPasswordInput()
    {
        TestUtils.waitForElementBy(
            this.participant.getDriver(),
            By.xpath("//input[@name='lockKey']"),
            5);

        return participant.getDriver().findElement(
            By.xpath("//input[@name='lockKey']"));
    }
}
