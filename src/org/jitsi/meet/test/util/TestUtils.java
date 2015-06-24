/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jitsi.meet.test.util;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;

/**
 * Utility class.
 * @author Damian Minkov
 */
public class TestUtils
{
    /** <tt>true</tt> if OS is Linux. */
    public static final boolean IS_LINUX;

    /** <tt>true</tt> if OS is MacOSX. */
    public static final boolean IS_MAC;

    static
    {
        // OS
        String osName = System.getProperty("os.name");

        if (osName == null)
        {
            IS_LINUX = false;
            IS_MAC = false;
        }
        else if (osName.startsWith("Linux"))
        {
            IS_LINUX = true;
            IS_MAC = false;
        }
        else if (osName.startsWith("Mac"))
        {
            IS_LINUX = false;
            IS_MAC = true;
        }
        else
        {
            IS_LINUX = false;
            IS_MAC = false;
        }
    }

    /**
     * Waits for a boolean value of javascript variable.
     * @param participant where we check (poll)
     * @param scriptToExecute the script that returns the boolean variable.
     * @param timeout time to wait in seconds
     */
    public static void waitsForBoolean(
        final WebDriver participant,
        final String scriptToExecute,
        long timeout)
    {
        (new WebDriverWait(participant, timeout))
            .until(new ExpectedCondition<Boolean>()
            {
                public Boolean apply(WebDriver d)
                {
                    Object res = ((JavascriptExecutor) participant)
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
     * @param timeout time to wait for the change in seconds
     */
    public static void waitsForEqualsStrings(
        final WebDriver participant,
        final String scriptToExecute,
        final String expectedResult,
        long timeout)
    {
        (new WebDriverWait(participant, timeout))
            .until(new ExpectedCondition<Boolean>()
            {
                public Boolean apply(WebDriver d)
                {
                    Object res =
                        ((JavascriptExecutor) participant)
                        .executeScript(scriptToExecute);

                    return res != null && res.equals(expectedResult);
                }
            });
    }

    /**
     * Waits till an element becomes available.
     * @param participant where we check
     * @param xpath the xpath to search for the element
     * @param timeout the time to wait for the element in seconds.
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
     * @param timeout the time to wait for the element to disappear in seconds.
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
     * @param timeout the time to wait for the element in seconds.
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

    /**
     * Waits till an element becomes available and displayed.
     * @param participant where we check
     * @param id the id to search for the element
     * @param timeout the time to wait for the element in seconds.
     */
    public static void waitsForNotDisplayedElementByID(
        WebDriver participant,
        final String id,
        long timeout)
    {
        new WebDriverWait(participant, timeout)
            .until(new ExpectedCondition<Boolean>()
            {
                public Boolean apply(WebDriver d)
                {
                    WebElement el = d.findElement(By.id(id));
                    return el == null || !el.isDisplayed();
                }
            });
    }

    /**
     * Waits till an element becomes available and displayed.
     * @param participant where we check
     * @param id the id to search for the element
     * @param timeout the time to wait for the element in seconds.
     */
    public static void waitsForDisplayedElementByID(
        WebDriver participant,
        final String id,
        long timeout)
    {
        new WebDriverWait(participant, timeout)
            .until(new ExpectedCondition<Boolean>()
            {
                public Boolean apply(WebDriver d)
                {
                    WebElement el = d.findElement(By.id(id));
                    return el != null && el.isDisplayed();
                }
            });
    }

    /**
     * Waits the specified <tt>time</tt> milliseconds.
     * @param time to wait in milliseconds.
     */
    public static void waits(long time)
    {
        Object obj = new Object();
        synchronized(obj)
        {
            try
            {
                obj.wait(time);
            }
            catch(Throwable t){}
        }
    }

    /**
     * Returns the resource for the jid or null if missing.
     * @param jid the full jid.
     * @return the resource for the jid or null if missing.
     */
    public static String getResourceFromJid(String jid)
    {
        if(jid == null)
            return null;

        int ix = jid.lastIndexOf("/");

        if(ix == -1)
            return null;

        return jid.substring(ix + 1, jid.length());
    }
}
