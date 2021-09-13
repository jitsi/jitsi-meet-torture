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
 * Represents the dial in numbers page in a particular {@link WebParticipant}.
 *
 * @author Leonard Kim
 */
public class DialInNumbersPage
{
    /**
     * Class names to be used as selectors for finding WebElements within the
     * {@link DialInNumbersPage}.
     */
    private final static String CONFERENCE_ID_MESSAGE = "dial-in-conference-id";
    private final static String NUMBERS = "dial-in-numbers-list";
    private final static String NUMBER = "dial-in-number";

    /**
     * The participant used to interact with the info dialog.
     */
    private final WebParticipant participant;

    /**
     * Initializes a new {@link DialInNumbersPage} instance.
     *
     * @param participant the participant for this {@link DialInNumbersPage}.
     */
    public DialInNumbersPage(WebParticipant participant)
    {
        this.participant = Objects.requireNonNull(participant, "participant");
    }

    /**
     * Searches the dial in numbers page to retrieve the help text for dialing
     * into the conference. The help text should include the conference id used
     * as a pin.
     *
     * @return The help text for dialing in, which includes the conference pin.
     */
    public String getConferenceIdMessage()
    {
        WebDriver driver = participant.getDriver();
        WebElement conferenceIDMessage
            = driver.findElement(By.className(CONFERENCE_ID_MESSAGE));

        return conferenceIDMessage.getText();
    }

    /**
     * Searches the displayed dial in numbers to verify if the passed in number
     * is displayed.
     *
     * @param number - The number to find in the page.
     * @return Whether or not the passed in number is displayed.
     */
    public boolean includesNumber(String number)
    {
        WebDriver driver = participant.getDriver();

        List<WebElement> elements = driver.findElements(By.className(NUMBER));

        WebElement element
            = elements.stream()
                .filter(e -> e.getText().equals(number))
                .findFirst()
                .orElse(null);

        return element != null;
    }

    /**
     * Performs a wait for the dial in numbers page to load phone numbers.
     */
    public void waitForLoad()
    {
        WebDriver driver = participant.getDriver();

        TestUtils.waitForElementBy(
            driver,
            By.className(NUMBERS),
            10);
    }
}
