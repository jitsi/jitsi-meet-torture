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

import java.io.*;
import java.nio.file.*;
import java.util.*;

import static org.testng.Assert.fail;

/**
 * Utility class.
 * @author Damian Minkov
 * @author Pawel Domas
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
     * Click an element on the page by first checking for visibility and then
     * checking for clickability.
     *
     * @param driver the {@code WebDriver}.
     * @param by the search query for the element
     */
    public static void click(WebDriver driver, final By by)
    {
        waitForElementBy(driver, by, 10);
        WebDriverWait wait = new WebDriverWait(driver, 10);
        WebElement element = wait.until(
            ExpectedConditions.elementToBeClickable(by));

        try
        {
            element.click();
        }
        catch (StaleElementReferenceException e)
        {
            // when we want to click on notification and a new one comes
            // we can hit StaleElementReferenceException because of re-render
            // a workaround is to try again
            driver.findElement(by).click();
        }
    }

    /**
     * Injects JS script into given <tt>participant</tt> <tt>WebDriver</tt>.
     * @param driver the <tt>WebDriver</tt> where the script will be
     * injected.
     * @param scriptPath the path of the JS script to be injected.
     */
    public static void injectScript(WebDriver driver, String scriptPath)
    {
        JavascriptExecutor js = ((JavascriptExecutor) driver);

        // read and inject helper script
        try
        {
            Path scriptAbsolutePath
                = Paths.get(new File(scriptPath).getAbsolutePath());

            String script = new String(Files.readAllBytes(scriptAbsolutePath));

            js.executeScript(script);
        }
        catch (Exception e)
        {
            e.printStackTrace();

            fail("Failed to inject JS script: " + scriptPath + " into "
                    + driver);
        }
    }

    /**
     * Waits until a javascript expression evaluates to {@code true}.
     * @param driver where we check (poll)
     * @param scriptToExecute the javascript to execute and expect a boolean
     * value from.
     * @param timeout time to wait in seconds
     */
    public static void waitForBoolean(
        final WebDriver driver,
        final String scriptToExecute,
        long timeout)
    {
        (new WebDriverWait(driver, timeout))
            .until((ExpectedCondition<Boolean>) d -> {
                Object res = ((JavascriptExecutor) driver)
                    .executeScript(scriptToExecute);
                return res != null && res.equals(Boolean.TRUE);
            });
    }

    /**
     * Waits until a javascript expression evaluates to a String equal to
     * <tt>expectedResult</tt>.
     * @param driver the {@code WebDriver} instance.
     * @param scriptToExecute the javascript code to execute.
     * @param expectedResult the expected value.
     * @param timeout timeout in seconds.
     */
    public static void waitForStrings(
        final WebDriver driver,
        final String scriptToExecute,
        final String expectedResult,
        long timeout)
    {
        (new WebDriverWait(driver, timeout))
            .until((ExpectedCondition<Boolean>) d -> {
                Object res =
                    ((JavascriptExecutor) driver)
                        .executeScript(scriptToExecute);

                return res != null && res.equals(expectedResult);
            });
    }

    /**
     * Waits until an element becomes available.
     * @param driver the {@code WebDriver}.
     * @param xpath the xpath to search for the element
     * @param timeout the time to wait for the element in seconds.
     */
    public static WebElement waitForElementByXPath(
        WebDriver driver,
        final String xpath,
        long timeout)
    {
        return waitForElementByXPath(driver, xpath, timeout, null);
    }

    /**
     * Waits until an element becomes available.
     * @param driver the {@code WebDriver}.
     * @param xpath the xpath to search for the element
     * @param timeout the time to wait for the element in seconds.
     * @param errorMessage the error message which should be displayed by
     * the exception thrown when the check fails.
     */
    public static WebElement waitForElementByXPath(
        WebDriver driver,
        final String xpath,
        long timeout,
        String errorMessage)
    {
        FluentWait<WebDriver> waitImpl
            = new WebDriverWait(driver, timeout);

        if (errorMessage != null)
        {
            waitImpl = waitImpl.withMessage(errorMessage);
        }

        return  waitImpl.until((ExpectedCondition<WebElement>) d -> {
            List<WebElement> elements
                = d.findElements(By.xpath(xpath));
            return elements.isEmpty() ? null : elements.get(0);
        });
    }

    /**
     * Waits until an element becomes available and return it.
     * @param driver the {@code WebDriver}.
     * @param by the xpath to search for the element
     * @param timeout the time to wait for the element in seconds.
     * @return WebElement the found element
     */
    public static WebElement waitForElementBy(
        WebDriver driver,
        final By by,
        long timeout)
    {
        final WebElement[] foundElement = new WebElement[1];
        new WebDriverWait(driver, timeout)
            .until((ExpectedCondition<Boolean>) d -> {
                List<WebElement> elements = d.findElements(by);

                if (!elements.isEmpty())
                {
                    foundElement[0] = elements.get(0);
                    return true;
                }
                else
                    return false;
            });

        return foundElement[0];
    }

    /**
     * Waits until an element becomes available/displayed and return it.
     * @param driver the {@code WebDriver}.
     * @param by the xpath to search for the element
     * @param timeout the time to wait for the element in seconds.
     * @return WebElement the found element
     */
    public static String waitForElementTextBy(
        WebDriver driver,
        final By by,
        long timeout)
    {
        final String[] foundElement = new String[2];
        try
        {
            new WebDriverWait(driver, timeout)
                .until((ExpectedCondition<Boolean>) d -> {
                    List<WebElement> elements = d.findElements(by);

                    if (!elements.isEmpty())
                    {
                        if (!elements.get(0).isDisplayed())
                        {
                            return false;
                        }
                        foundElement[1] = Boolean.TRUE.toString();
                        foundElement[0] = elements.get(0).getText();
                        return true;
                    }
                    else
                        return false;
                });
        }
        catch (StaleElementReferenceException e)
        {
            // sometimes while getting the text we get:
            // StaleElementReferenceException: stale element reference: element is not attached to the page document
            // Then we will try one more time, hoping for no re-render.
            if (foundElement[1] != null)
            {
                return driver.findElement(by).getText();
            }
        }

        return foundElement[0];
    }

    /**
     * Waits until an element becomes unavailable
     * @param driver the {@code WebDriver}.
     * @param by the xpath to search for the element
     * @param timeout the time to wait for the element in seconds.
     */
    public static void waitForElementNotPresentBy(
        WebDriver driver,
        final By by,
        long timeout)
    {
        new WebDriverWait(driver, timeout)
            .until((ExpectedCondition<Boolean>) d -> d.findElements(by)
                .isEmpty());
    }

    /**
     * Waits until an element becomes unavailable.
     * @param driver the {@code WebDriver}.
     * @param xpath the xpath to search for the element
     * @param timeout the time to wait for the element to disappear in seconds.
     */
    public static void waitForElementNotPresentByXPath(
        WebDriver driver,
        final String xpath,
        long timeout)
    {
        waitForElementNotPresentBy(driver, By.xpath(xpath), timeout);
    }

    /**
     * Waits until an element becomes unavailable or not displayed.
     * @param driver the {@code WebDriver}.
     * @param xpath the xpath to search for the element
     * @param timeout the time to wait for the element to disappear in seconds.
     */
    public static void waitForElementNotPresentOrNotDisplayedByXPath(
        WebDriver driver,
        final String xpath,
        long timeout)
    {
        waitForDisplayedOrNotByXPath(driver, xpath, timeout, false);
    }

    /**
     * Waits until an element becomes available and displayed.
     * @param driver the {@code WebDriver}.
     * @param xpath the xpath to search for the element
     * @param timeout the time to wait for the element in seconds.
     */
    public static void waitForDisplayedElementByXPath(
        WebDriver driver,
        final String xpath,
        long timeout)
    {
        waitForDisplayedOrNotByXPath(driver, xpath, timeout, true);
    }

    /**
     * Waits until an element becomes available and displayed or until the
     * element becomes either unavailable or not displayed depending on
     * the <tt>isDisplayed</tt> flag value.
     *
     * @param driver the {@code WebDriver}.
     * @param xpath the xpath to search for the element
     * @param timeout the time to wait for the element in seconds.
     * @param isDisplayed determines type of the check. <tt>true</tt> means
     * we're waiting for the element to become available and <tt>false</tt>
     * means the element must either be hidden or unavailable.
     */
    public static void waitForDisplayedOrNotByXPath(
        WebDriver        driver,
        final String     xpath,
        long             timeout,
        final boolean    isDisplayed)
    {
        waitForElementDisplayToBe(
            driver,
            By.xpath(xpath),
            timeout,
            isDisplayed
        );
    }

    /**
     * Waits until an element has attribute equal to specified value.
     * @param driver the {@code WebDriver}.
     * @param xpath the xpath to search for the element
     * @param attributeName name of the the element's attribute
     * @param attributeValue expected value of the the element's attribute
     * @param timeout the time to wait for the element in seconds.
     */
    public static void waitForElementAttributeValueByXPath(
            WebDriver driver,
            final String xpath,
            final String attributeName,
            final Object attributeValue,
            long timeout)
    {
        new WebDriverWait(driver, timeout)
                .until((ExpectedCondition<Boolean>) d -> {
                    WebElement el = d.findElement(By.xpath(xpath));

                    return el != null &&
                            el.getAttribute(attributeName)
                                    .equals(attributeValue);
                });
    }

    /**
     * Waits until element will contain class name
     * passed with parameter
     * @param driver the {@code WebDriver}.
     * @param xpath the xpath to search for the element.
     * @param className expected class value of the element.
     * @param timeout the time to wait for the element in seconds.
     */
    public static void waitForElementContainsClassByXPath(
            WebDriver driver,
            final String xpath,
            final String className,
            long timeout
    )
    {
        new WebDriverWait(driver, timeout)
            .until((ExpectedCondition<Boolean>) d -> {
                WebElement el = d.findElement(By.xpath(xpath));
                String classNames = el.getAttribute("class");
                return classNames.contains(className);
            });
    }

    /**
     * Waits until element will not contain class name
     * passed with parameter
     * @param driver the {@code WebDriver}.
     * @param xpath the xpath to search for the element.
     * @param className expected class value of the element.
     * @param timeout the time to wait for the element in seconds.
     */
    public static void waitForElementNotContainsClassByXPath(
            WebDriver driver,
            final String xpath,
            final String className,
            long timeout)
    {
        new WebDriverWait(driver, timeout)
                .until((ExpectedCondition<Boolean>) d -> {
                    WebElement el = d.findElement(By.xpath(xpath));
                    String classNames = el.getAttribute("class");
                    return !classNames.contains(className);
                });
    }

    /**
     * Waits until an element is not displayed.
     * @param driver the {@code WebDriver}.
     * @param xpath the xpath to search for the element
     * @param timeout the time to wait for the element in seconds.
     */
    public static void waitForNotDisplayedElementByXPath(
        WebDriver driver,
        final String xpath,
        long timeout)
    {
        waitForElementDisplayToBe(driver, By.xpath(xpath), timeout, false);
    }

    /**
     * Waits until an element becomes is not displayed.
     * @param driver the {@code WebDriver}.
     * @param id the id to search for the element
     * @param timeout the time to wait for the element in seconds.
     */
    public static void waitForNotDisplayedElementByID(
        WebDriver driver,
        final String id,
        long timeout)
    {
        waitForElementDisplayToBe(driver, By.id(id), timeout, false);
    }

    /**
     * Waits until an element becomes available and displayed.
     * @param driver the {@code WebDriver}.
     * @param id the id to search for the element
     * @param timeout the time to wait for the element in seconds.
     */
    public static void waitForDisplayedElementByID(
        WebDriver driver,
        final String id,
        long timeout)
    {
        waitForElementDisplayToBe(driver, By.id(id), timeout, true);
    }

    /**
     * Waits until an element becomes available and displayed or not available
     * and not displayed.
     *
     * @param driver the {@code WebDriver}.
     * @param by the selector to use for finding the element.
     * @param timeout the time to wait for the element in seconds.
     * @param isDisplayed determines type of the check. <tt>true</tt> means
     * we're waiting for the element to become available and <tt>false</tt>
     * means the element must either be hidden or unavailable.
     */
    public static void waitForElementDisplayToBe(
        WebDriver driver,
        By by,
        long timeout,
        final boolean isDisplayed)
    {
        new WebDriverWait(driver, timeout)
            .withMessage(
                "Is " + (isDisplayed ? "" : "not")
                    + "displayed: " + by.toString())
            .until((ExpectedCondition<Boolean>) d -> {
                List<WebElement> elList = d.findElements(by);
                WebElement el = elList.isEmpty() ? null : elList.get(0);

                boolean expectedConditionMet = false;

                try
                {
                    expectedConditionMet = isDisplayed
                            ? el != null && el.isDisplayed()
                            : el == null || !el.isDisplayed();
                }
                catch (StaleElementReferenceException e)
                {
                    // if the element is detached in a process of checking
                    // its display status, means its not visible anymore
                }

                return expectedConditionMet;
            });
    }

    /**
     * Waits until the given condition is fulfilled and fails the currently
     * running test if this doesn't happen within {@code timeoutSeconds} seconds.
     * @param driver the {@code WebDriver}.
     * @param message the message to print in case of a failure.
     * @param timeoutSeconds the time to wait for the element in seconds.
     * @param condition the condition to be met.
     */
    public static void waitForCondition(WebDriver driver,
                                        String message,
                                        int timeoutSeconds,
                                        ExpectedCondition<?> condition)
    {
        WebDriverWait wait = new WebDriverWait(driver, timeoutSeconds);

        if (message != null)
        {
            wait.withMessage(message);
        }

        wait.until(condition);
    }

    /**
     * Waits until the given condition is fulfilled and fails the currently
     * running test if this doesn't happen within {@code timeoutSeconds} seconds.
     * @param driver the {@code WebDriver}.
     * @param timeoutSeconds the time to wait for the element in seconds.
     * @param condition the condition to be met.
     */
    public static void waitForCondition(WebDriver driver,
                                        int timeoutSeconds,
                                        ExpectedCondition<?> condition)
    {
        waitForCondition(driver, null, timeoutSeconds, condition);
    }

    /**
     * FIXME remove unused method
     *
     * Waits until the given condition is fulfilled and fails the currently
     * running test if this doesn't happen within {@code timeoutSeconds} seconds.
     * @param driver the {@code WebDriver}.
     * @param timeoutSeconds the time to wait for the element in seconds.
     * @param condition the condition to be met.
     * @param pollWaitTime configure waits between checks
     */
    public static void waitForCondition(WebDriver driver,
                                        int timeoutSeconds,
                                        ExpectedCondition<?> condition,
                                        long pollWaitTime)
    {
        (new WebDriverWait(driver, timeoutSeconds, pollWaitTime))
            .until(condition);
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
     * Executes a specific (piece of) JavaScript script in the browser
     * controlled by a specific {@code WebDriver} and returns the result of its
     * execution as a {@code Boolean} value.
     *
     * @param driver the {@code WebDriver} which controls the browser in
     * which the specified {@code script} is to be executed
     * @param script the script to execute in the browser controlled by
     * {@code webDriver}
     * @return the result of the execution of {@code script} in the browser
     * controlled by {@code webDriver} as a {@code Boolean} value
     */
    public static Boolean executeScriptAndReturnBoolean(
        WebDriver driver,
        String script)
    {
        Object o = ((JavascriptExecutor) driver).executeScript(script);

        return (o instanceof Boolean) ? (Boolean) o : Boolean.FALSE;
    }

    /**
     * Executes a specific (piece of) JavaScript script in the browser
     * controlled by a specific {@code WebDriver} and returns the result of its
     * execution as a {@code double} value.
     *
     * @param driver the {@code WebDriver} which controls the browser in
     * which the specified {@code script} is to be executed
     * @param script the script to execute in the browser controlled by
     * {@code webDriver}
     * @return the result of the execution of {@code script} in the browser
     * controlled by {@code webDriver} as a {@code double} value
     */
    public static double executeScriptAndReturnDouble(
        WebDriver driver,
        String script)
    {
        Object o = ((JavascriptExecutor) driver).executeScript(script);

        double d = 0;

        if (o instanceof Double)
        {
            d = (Double) o;
        }
        else if (o instanceof Long)
        {
            d = (Long) o;
        }

        return d;
    }

    /**
     * Converts an arbitrary result of the JavaScript execution into primitive
     * <tt>boolean</tt> (null means <tt>false</tt>).
     *
     * @param jsExecutionResult - The <tt>Object</tt> which is a result of
     * JavaScript execution.
     * @return a primitive <tt>boolean</tt> value for the given <tt>Object</tt>
     * where <tt>null</tt> will be converted to <tt>false</tt>.
     */
    public static boolean getBooleanResult(Object jsExecutionResult)
    {
        // FIXME cleanup to not use 'executeScriptAndReturnBoolean' and have one
        // way for dealing with script execution (the method above works on
        // WebDriver directly).
        return jsExecutionResult != null
            && Boolean.valueOf(jsExecutionResult.toString());
    }

    /**
     * Executes a specific (piece of) JavaScript script in the browser
     * controlled by a specific {@code WebDriver} and returns the result of its
     * execution as a {@code String} value.
     *
     * @param driver the {@code WebDriver} which controls the browser in
     * which the specified {@code script} is to be executed
     * @param script the script to execute in the browser controlled by
     * {@code webDriver}
     * @return the result of the execution of {@code script} in the browser
     * controlled by {@code webDriver} as a {@code String} value
     */
    public static String executeScriptAndReturnString(
        WebDriver driver,
        String script)
    {
        Object o = ((JavascriptExecutor) driver).executeScript(script);

        return (o instanceof String) ? (String) o : null;
    }

    /**
     * Executes a specific (piece of) JavaScript script in the browser
     * controlled by a specific {@code WebDriver}
     *
     * @param driver the {@code WebDriver} which controls the browser in
     * which the specified {@code script} is to be executed
     * @param script the script to execute in the browser controlled by
     * {@code webDriver}
     */
    public static void executeScript(
        WebDriver driver,
        String script)
    {
        ((JavascriptExecutor) driver).executeScript(script);
    }

    /**
     * Prints a text with a prefix of the thread id.
     * @param txt the text to print.
     */
    public static void print(String txt)
    {
        System.err.println("[" + Thread.currentThread().getId() + "] " + txt);
    }
}
