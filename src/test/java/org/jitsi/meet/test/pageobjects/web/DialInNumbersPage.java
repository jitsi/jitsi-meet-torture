package org.jitsi.meet.test.pageobjects.web;

import org.jitsi.meet.test.util.TestUtils;
import org.jitsi.meet.test.web.WebParticipant;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.Objects;

/**
 * Represents the dial in numbers page in a particular {@link WebParticipant}.
 *
 * @author Leonard Kim
 */
public class DialInNumbersPage
{

    /**
     * The participant used to interact with the info dialog.
     */
    private final WebParticipant participant;

    /**
     * Classnames to be used as selectors for finding WebElements within the
     * {@link DialInNumbersPage}.
     */
    private final String CONFERNCE_ID_MESSAGE = "dial-in-conference-id";
    private final String NUMBERS = "dial-in-numbers-list";
    private final String NUMBER = "dial-in-number";

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
    public String getConferenceIdMessage() {
        WebDriver driver = participant.getDriver();
        WebElement conferenceIDMessage = driver.findElement(
                By.className(CONFERNCE_ID_MESSAGE));

        return conferenceIDMessage.getText();
    }

    /**
     * Searches the displayed dial in numbers to verify if the passed in number
     * is displayed.
     *
     * @param number - The number to find in the page.
     * @return Whether or not the passed in number is displayed.
     */
    public boolean includesNumber(String number) {
        WebDriver driver = participant.getDriver();

        List<WebElement> elements = driver.findElements(By.className(NUMBER));

        WebElement element = elements.stream()
            .filter(e -> e.getText().equals(number))
            .findFirst()
            .orElse(null);

        return element != null;
    }

    /**
     * Performs a wait for the dial in numbers page to load phone numbers.
     */
    public void waitForLoad() {
        WebDriver driver = participant.getDriver();

        TestUtils.waitForElementBy(
            driver,
            By.className(NUMBERS),
            10);
    }
}
