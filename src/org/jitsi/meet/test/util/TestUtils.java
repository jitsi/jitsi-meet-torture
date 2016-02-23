/*
 * Copyright @ 2015 Atlassian Pty Ltd
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
package org.jitsi.meet.test.util;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;

import java.util.*;

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
     * Waits until a javascript expression evaluates to {@code true}.
     * @param participant where we check (poll)
     * @param scriptToExecute the javascript to execute and expect a boolean
     * value from.
     * @param timeout time to wait in seconds
     */
    public static void waitForBoolean(
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
     * Waits until a javascript expression evaluates to a String equal to
     * <tt>expectedResult</tt>.
     * @param participant the {@code WebDriver} instance.
     * @param scriptToExecute the javascript code to execute.
     * @param expectedResult the expected value.
     * @param timeout timeout in seconds.
     */
    public static void waitForStrings(
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
     * Waits until an element becomes available.
     * @param participant the {@code WebDriver}.
     * @param xpath the xpath to search for the element
     * @param timeout the time to wait for the element in seconds.
     */
    public static void waitForElementByXPath(
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
     * Waits until an element becomes unavailable.
     * @param participant the {@code WebDriver}.
     * @param xpath the xpath to search for the element
     * @param timeout the time to wait for the element to disappear in seconds.
     */
    public static void waitForElementNotPresentByXPath(
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
     * Waits until an element becomes unavailable or not displayed.
     * @param participant the {@code WebDriver}.
     * @param xpath the xpath to search for the element
     * @param timeout the time to wait for the element to disappear in seconds.
     */
    public static void waitForElementNotPresentOrNotDisplayedByXPath(
        WebDriver participant,
        final String xpath,
        long timeout)
    {
        new WebDriverWait(participant, timeout)
            .until(new ExpectedCondition<Boolean>()
            {
                public Boolean apply(WebDriver d)
                {
                    List<WebElement> elems = d.findElements(By.xpath(xpath));

                    // element missing
                    if (elems.isEmpty())
                        return true;

                    // let's check whether all elements are not displayed
                    for (WebElement e : elems)
                    {
                        if(e.isDisplayed())
                            return false;
                    }
                    return true;
                }
            });
    }

    /**
     * Waits until an element becomes available and displayed.
     * @param participant the {@code WebDriver}.
     * @param xpath the xpath to search for the element
     * @param timeout the time to wait for the element in seconds.
     */
    public static void waitForDisplayedElementByXPath(
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
     * Waits until an element is not displayed.
     * @param participant the {@code WebDriver}.
     * @param xpath the xpath to search for the element
     * @param timeout the time to wait for the element in seconds.
     */
    public static void waitForNotDisplayedElementByXPath(
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
                    return el == null || !el.isDisplayed();
                }
            });
    }

    /**
     * Waits until an element becomes available and displayed.
     * @param participant the {@code WebDriver}.
     * @param id the id to search for the element
     * @param timeout the time to wait for the element in seconds.
     */
    public static void waitForNotDisplayedElementByID(
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
     * Waits until an element becomes available and displayed.
     * @param participant the {@code WebDriver}.
     * @param id the id to search for the element
     * @param timeout the time to wait for the element in seconds.
     */
    public static void waitForDisplayedElementByID(
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
     * Waits until the given condition is fulfilled and fails the currently
     * running test if this doesn't happen within {@code timeoutSeconds} seconds.
     * @param participant the {@code WebDriver}.
     * @param timeoutSeconds the time to wait for the element in seconds.
     * @param condition the condition to be met.
     */
    public static void waitForCondition(WebDriver participant,
                                        int timeoutSeconds,
                                        ExpectedCondition<?> condition)
    {
        (new WebDriverWait(participant, timeoutSeconds)).until(condition);
    }

    /**
     * Waits for the specified amount of <tt>time</tt> in milliseconds.
     * @param time to wait in milliseconds.
     * XXX Any reason we're not using Thread.sleep() instead of?
     */
    public static void waitMillis(long time)
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

    /**
     * Executes a specific (piece of) JavaScript script in the browser
     * controlled by a specific {@code WebDriver} and returns the result of its
     * execution as a {@code Boolean} value.
     *
     * @param webDriver the {@code WebDriver} which controls the browser in
     * which the specified {@code script} is to be executed
     * @param script the script to execute in the browser controlled by
     * {@code webDriver}
     * @return the result of the execution of {@code script} in the browser
     * controlled by {@code webDriver} as a {@code Boolean} value
     */
    public static Boolean executeScriptAndReturnBoolean(
        WebDriver webDriver,
        String script)
    {
        Object o = ((JavascriptExecutor) webDriver).executeScript(script);

        return (o instanceof Boolean) ? (Boolean) o : Boolean.FALSE;
    }
}
