/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jitsi.meet.test;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;

/**
 * Utility class.
 * @author Damian Minkov
 */
public class TestUtils
{
    /**
     * Waits for a boolean value of javascript variable.
     * @param participant where we check (poll)
     * @param scriptToExecute the script that returns the boolean variable.
     * @param timeout
     */
    public static void waitsForBoolean(
        WebDriver participant,
        final String scriptToExecute,
        long timeout)
    {
        (new WebDriverWait(participant, timeout))
            .until(new ExpectedCondition<Boolean>()
            {
                public Boolean apply(WebDriver d)
                {
                    Object res = ((JavascriptExecutor) ConferenceFixture.focus)
                        .executeScript(scriptToExecute);
                    return res != null && res.equals(Boolean.TRUE);
                }
            });
    }

    /**
     * Waits a javascript String variable to become <tt>expectedResult</tt>.
     * @param participant where we check
     * @param scriptToExecute the script that returns the String variable.
     * @param expectedResult the expected value
     * @param timeout time to wait for the change
     */
    public static void waitsForEqualsStrings(
        WebDriver participant,
        final String scriptToExecute,
        final String expectedResult,
        long timeout)
    {
        (new WebDriverWait(participant, timeout))
            .until(new ExpectedCondition<Boolean>()
            {
                public Boolean apply(WebDriver d)
                {
                    Object res = ((JavascriptExecutor) ConferenceFixture.focus)
                        .executeScript(scriptToExecute);

                    return res != null && res.equals(expectedResult);
                }
            });
    }

    /**
     * Waits till an element becomes available.
     * @param participant where we check
     * @param xpath the xpath to search for the element
     * @param timeout the time to wait for the element.
     */
    public static void waitsForElementByXPath(
        WebDriver participant,
        final String xpath,
        long timeout)
    {
        new WebDriverWait(participant, timeout)
            .until(new ExpectedCondition<Boolean>()
        {
            public Boolean apply(WebDriver d)
            {
                return !d.findElements(By.xpath(xpath)).isEmpty();
            }
        });
    }

    /**
     * Waits till an element becomes unavailable.
     * @param participant where we check
     * @param xpath the xpath to search for the element
     * @param timeout the time to wait for the element to disappear.
     */
    public static void waitsForElementNotPresentByXPath(
        WebDriver participant,
        final String xpath,
        long timeout)
    {
        new WebDriverWait(participant, timeout)
            .until(new ExpectedCondition<Boolean>()
            {
                public Boolean apply(WebDriver d)
                {
                    return d.findElements(By.xpath(xpath))
                        .isEmpty();
                }
            });
    }

    /**
     * Waits till an element becomes available and displayed.
     * @param participant where we check
     * @param xpath the xpath to search for the element
     * @param timeout the time to wait for the element.
     */
    public static void waitsForDisplayedElementByXPath(
        WebDriver participant,
        final String xpath,
        long timeout)
    {
        new WebDriverWait(participant, timeout)
            .until(new ExpectedCondition<Boolean>()
            {
                public Boolean apply(WebDriver d)
                {
                    WebElement el = d.findElement(By.xpath(xpath));
                    return el != null && el.isDisplayed();
                }
            });
    }

}
