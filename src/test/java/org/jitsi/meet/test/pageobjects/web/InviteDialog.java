/*
 * Copyright @ 2015-present 8x8, Inc.
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
 * Represents the invite dialog in a particular {@link WebParticipant}.
 *
 * @author Leonard Kim
 */
public class InviteDialog
{
    /**
     * Class names to be used as selectors for finding WebElements within the
     * {@link InviteDialog}.
     */
    private final static String CONFERENCE_URL = "invite-more-dialog-conference-url";
    private final static String DIALOG_CONTAINER = "invite-more-dialog";
    private final static String MORE_NUMBERS = "more-numbers";
    private final static String PHONE_NUMBER = "phone-number";
    private final static String CONFERENCE_ID = "conference-id";

    /**
     * The participant used to interact with the invite dialog.
     */
    private final WebParticipant participant;

    /**
     * Initializes a new {@link InviteDialog} instance.
     *
     * @param participant the participant for this {@link InviteDialog}.
     */
    public InviteDialog(WebParticipant participant)
    {
        this.participant = Objects.requireNonNull(participant, "participant");
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
     * Clicks the "info" toolbar button which opens or closes the invite dialog.
     */
    private void clickToolbarButton()
    {
        participant.getToolbar().clickInviteButton();
    }

    /**
     * Clicks the "info" toolbar button to close the invite dialog, if the info
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
     * Finds and returns the invite dialog.
     *
     * @return The {@code WebElement} for the invite dialog or null if it cannot
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
     * Gets the string that contains the url to the current conference.
     *
     * @return {@code String} for the current conference's url.
     */
    public String getMeetingURL()
    {
        WebDriver driver = participant.getDriver();
        TestUtils.waitForElementBy(
            driver,
            By.className(CONFERENCE_URL),
            5);

        String fullText
            = driver.findElement(By.className(CONFERENCE_URL)).getText();

        return fullText.trim();
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
        // we need to remove the last character from the pin which is '#'
        return this.getValueAfterColon(CONFERENCE_ID).replaceAll("#", "");
    }

    /**
     * Checks if the invite dialog is currently displayed.
     *
     * @return {@code true} if the invite dialog is displayed, {@code false}
     * otherwise.
     */
    public boolean isOpen()
    {
        WebElement dialog = get();

        return dialog != null;
    }

    /**
     * Clicks the "info" toolbar button to open the invite dialog, if the info
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
     * Private helper to get values after colons. The invite dialog lists
     * conference specific information after a label, followed by a colon.
     *
     * @param className - The class name to search for that signifies the
     *                  current lock state of the conference.
     * @return {@code true} if the invite dialog has the passed in class,
     * {@code false} otherwise.
     */
    private String getValueAfterColon(String className)
    {
        open();

        WebDriver driver = participant.getDriver();

        // waits for the element to be available before getting the text
        // PHONE_NUMBER for example can take time before shown
        TestUtils.waitForElementBy(
            driver,
            By.className(className),
            5);

        String fullText
            = driver.findElement(By.className(className)).getText();

        return fullText.split(":")[1].trim();
    }
}
