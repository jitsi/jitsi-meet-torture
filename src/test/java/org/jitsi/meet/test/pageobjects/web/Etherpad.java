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

import org.jitsi.meet.test.util.*;
import org.jitsi.meet.test.web.*;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;

import java.util.*;

/**
 * Represents the Etherpad feature in a particular {@link WebParticipant}.
 */
public class Etherpad
{
    /**
     * Selectors to be used for finding WebElements within the {@link Etherpad}
     * feature.
     */
    private final static String EDITOR_CONTENT_CONTAINER_NAME = "ace_outer";
    private final static String EDITOR_CONTENT_NAME = "ace_inner";
    private final static String EDITOR_TEXT_AREA_ID = "innerdocbody";
    private final static String ETHERPAD_CONTAINER_ID = "etherpadIFrame";
    private final static String ENTERED_TEXT_XPATH
        = "//span[contains(@class, 'author')]";

    /**
     * The participant used to interact with the {@link Etherpad}.
     */
    private final WebParticipant participant;

    /**
     * Initializes a new {@link Etherpad} instance.
     *
     * @param participant the participant for this {@link Etherpad}.
     */
    public Etherpad(WebParticipant participant)
    {
        this.participant = Objects.requireNonNull(participant, "participant");
    }

    /**
     * Hides display of {@link Etherpad} by interacting with the toolbar. Will
     * be a no-op if {@link Etherpad} is not currently displayed.
     */
    public void close()
    {
        if (!isOpen())
        {
            return;
        }

        participant.getToolbar().clickEtherpadButton();
    }

    /**
     * Types in additional text to the editor content of {@link Etherpad}.
     *
     * @param text The text to type into the editor.
     */
    public void enterText(String text)
    {
        TestUtils.waitForDisplayedElementByID(
            participant.getDriver(),
            "innerdocbody",
            5);

        participant.getDriver()
            .findElement(By.id(EDITOR_TEXT_AREA_ID))
            .sendKeys(text);
    }

    /**
     * Finds and returns {@link Etherpad}.
     *
     * @return The {@code WebElement} for the Etherpad feature or null if it
     * cannot be found.
     */
    public WebElement get()
    {
        List<WebElement> elements = participant.getDriver().findElements(
                By.id(ETHERPAD_CONTAINER_ID));

        return elements.isEmpty() ? null : elements.get(0);
    }

    /**
     * Checks if {@link Etherpad} is currently displayed.
     *
     * @return {@code true} if Etherpad is displayed, {@code false} otherwise.
     */
    public boolean isOpen()
    {
        WebElement dialog = get();

        return  dialog != null && dialog.isDisplayed();
    }

    /**
     * Returns the text that has been typed into the editor by the
     * {@link WebParticipant}.
     *
     * @return {@code String} The text the {@link WebParticipant} has entered so
     * far into the editor.
     */

    public String getParticipantEnteredText()
    {
        return participant.getDriver()
            .findElement(By.xpath(ENTERED_TEXT_XPATH))
            .getText();
    }

    /**
     * Interacts with the Toolbar to display {@link Etherpad} if not already
     * displayed.
     */
    public void open()
    {
        if (isOpen())
        {
            System.out.println("opening!");
            return;
        }

        participant.getToolbar().clickEtherpadButton();
    }

    /**
     * Polls for certain elements of {@link Etherpad} to be available for
     * interaction.
     */
    public void waitForLoadComplete()
    {
        WebDriver driver = participant.getDriver();

        WebDriverWait wait = new WebDriverWait(driver, 30);
        wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(
                By.id(ETHERPAD_CONTAINER_ID)));

        wait = new WebDriverWait(driver, 30);
        wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(
                By.name(EDITOR_CONTENT_CONTAINER_NAME)));

        wait = new WebDriverWait(driver, 30);
        wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(
                By.name(EDITOR_CONTENT_NAME)));
    }
}
