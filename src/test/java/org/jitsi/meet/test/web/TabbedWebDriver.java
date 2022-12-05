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
package org.jitsi.meet.test.web;

import org.apache.commons.lang.*;
import org.jitsi.meet.test.util.*;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.*;
import org.openqa.selenium.internal.*;
import org.openqa.selenium.logging.*;
import org.openqa.selenium.remote.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

/** A wrapper around RemoteWebDriver that allows multiple tabs to be running at once in the same browser instance. */
public class TabbedWebDriver implements WebDriver, JavascriptExecutor,
    FindsById, FindsByClassName, FindsByLinkText, FindsByName,
    FindsByCssSelector, FindsByTagName, FindsByXPath,
    HasInputDevices, HasCapabilities, Interactive, TakesScreenshot
{
    private static final Map<WebDriver, AtomicInteger> bases = Collections.synchronizedMap(new IdentityHashMap<>());

    final RemoteWebDriver baseDriver;
    final AtomicInteger counter;

    String tabId;

    public TabbedWebDriver(RemoteWebDriver base)
    {
        baseDriver = base;

        synchronized (baseDriver)
        {
            Set<String> oldTabs = baseDriver.getWindowHandles();

            AtomicBoolean first = new AtomicBoolean(false);
            counter = bases.computeIfAbsent(baseDriver, key -> {
                first.set(true);
                return new AtomicInteger();
            });
            counter.incrementAndGet();

            if (first.get())
            {
                assert (oldTabs.size() == 1);
                tabId = oldTabs.iterator().next();
            }
            else
            {
                /* TODO: is there a non-quadratic way of doing this? */
                baseDriver.executeScript("window.open()");
                Set<String> newTabs = baseDriver.getWindowHandles();
                newTabs.removeAll(oldTabs);
                assert (newTabs.size() == 1);
                tabId = newTabs.iterator().next();
            }
        }

        TestUtils.print("Opened tabbed window with baseDriver " + baseDriver + " and tab ID " + tabId);
    }

    public RemoteWebDriver getBaseDriver()
    {
        return baseDriver;
    }

    @Override
    public Capabilities getCapabilities()
    {
        return baseDriver.getCapabilities();
    }

    @Override
    public Object executeScript(String script, Object... args)
    {
        synchronized (baseDriver)
        {
            baseDriver.switchTo().window(tabId);
            return baseDriver.executeScript(script, args);
        }
    }

    @Override
    public Object executeAsyncScript(String script, Object... args)
    {
        synchronized (baseDriver)
        {
            baseDriver.switchTo().window(tabId);
            return baseDriver.executeAsyncScript(script, args);
        }
    }

    @Override
    public <X> X getScreenshotAs(OutputType<X> target) throws WebDriverException
    {
        synchronized (baseDriver)
        {
            baseDriver.switchTo().window(tabId);
            return baseDriver.getScreenshotAs(target);
        }
    }

    @Override
    public void get(String url)
    {
        synchronized (baseDriver)
        {
            baseDriver.switchTo().window(tabId);
            baseDriver.get(url);
        }
    }

    @Override
    public String getCurrentUrl()
    {
        synchronized (baseDriver)
        {
            baseDriver.switchTo().window(tabId);
            return baseDriver.getCurrentUrl();
        }
    }

    @Override
    public String getTitle()
    {
        synchronized (baseDriver)
        {
            baseDriver.switchTo().window(tabId);
            return baseDriver.getTitle();
        }
    }

    @Override
    public List<WebElement> findElements(By by)
    {
        synchronized (baseDriver)
        {
            baseDriver.switchTo().window(tabId);
            return baseDriver.findElements(by);
        }
    }

    @Override
    public WebElement findElement(By by)
    {
        synchronized (baseDriver)
        {
            baseDriver.switchTo().window(tabId);
            return baseDriver.findElement(by);
        }
    }

    @Override
    public String getPageSource()
    {
        synchronized (baseDriver)
        {
            baseDriver.switchTo().window(tabId);
            return baseDriver.getPageSource();
        }
    }

    @Override
    public void close()
    {
        synchronized (baseDriver)
        {
            baseDriver.switchTo().window(tabId);
            baseDriver.close();
            tabId = null;
        }
    }

    @Override
    public void quit()
    {
        synchronized (baseDriver)
        {
            if (tabId != null)
            {
                close();
            }
            if (counter.decrementAndGet() == 0)
            {
                bases.remove(baseDriver);
                baseDriver.quit();
            }
        }
    }

    @Override
    public Set<String> getWindowHandles()
    {
        return Collections.singleton(tabId);
    }

    @Override
    public String getWindowHandle()
    {
        return tabId;
    }

    @Override
    public TargetLocator switchTo()
    {
        throw new IllegalArgumentException("Can't use switchTo() in a tabbed driver");
    }

    @Override
    public Navigation navigate()
    {
        // TODO: if we need it, need to add locking inside the returned object.
        throw new NotImplementedException("Not implemented");
    }

    @Override
    public Options manage()
    {
        synchronized (baseDriver)
        {
            baseDriver.switchTo().window(tabId);
            return new TabbedWebDriverOptions(baseDriver.manage());
        }
    }

    @Override
    public Keyboard getKeyboard()
    {
        // TODO: if we need it, need to add locking inside the returned object.
        throw new NotImplementedException("Not implemented");
    }

    @Override
    public Mouse getMouse()
    {
        // TODO: if we need it, need to add locking inside the returned object.
        throw new NotImplementedException("Not implemented");
    }

    @Override
    public void perform(Collection<Sequence> actions)
    {
        synchronized (baseDriver)
        {
            baseDriver.switchTo().window(tabId);
            baseDriver.perform(actions);
        }
    }

    @Override
    public void resetInputState()
    {
        synchronized (baseDriver)
        {
            baseDriver.switchTo().window(tabId);
            baseDriver.resetInputState();
        }
    }

    @Override
    public WebElement findElementByClassName(String using)
    {
        synchronized (baseDriver)
        {
            baseDriver.switchTo().window(tabId);
            return baseDriver.findElementByClassName(using);
        }
    }

    @Override
    public List<WebElement> findElementsByClassName(String using)
    {
        synchronized (baseDriver)
        {
            baseDriver.switchTo().window(tabId);
            return baseDriver.findElementsByClassName(using);
        }
    }

    @Override
    public WebElement findElementByCssSelector(String using)
    {
        synchronized (baseDriver)
        {
            baseDriver.switchTo().window(tabId);
            return baseDriver.findElementByCssSelector(using);
        }
    }

    @Override
    public List<WebElement> findElementsByCssSelector(String using)
    {
        synchronized (baseDriver)
        {
            baseDriver.switchTo().window(tabId);
            return baseDriver.findElementsByCssSelector(using);
        }
    }

    @Override
    public WebElement findElementById(String using)
    {
        synchronized (baseDriver)
        {
            baseDriver.switchTo().window(tabId);
            return baseDriver.findElementById(using);
        }
    }

    @Override
    public List<WebElement> findElementsById(String using)
    {
        synchronized (baseDriver)
        {
            baseDriver.switchTo().window(tabId);
            return baseDriver.findElementsById(using);
        }
    }

    @Override
    public WebElement findElementByLinkText(String using)
    {
        synchronized (baseDriver)
        {
            baseDriver.switchTo().window(tabId);
            return baseDriver.findElementByLinkText(using);
        }
    }

    @Override
    public List<WebElement> findElementsByLinkText(String using)
    {
        synchronized (baseDriver)
        {
            baseDriver.switchTo().window(tabId);
            return baseDriver.findElementsByLinkText(using);
        }
    }

    @Override
    public WebElement findElementByPartialLinkText(String using)
    {
        synchronized (baseDriver)
        {
            baseDriver.switchTo().window(tabId);
            return baseDriver.findElementByPartialLinkText(using);
        }
    }

    @Override
    public List<WebElement> findElementsByPartialLinkText(String using)
    {
        synchronized (baseDriver)
        {
            baseDriver.switchTo().window(tabId);
            return baseDriver.findElementsByPartialLinkText(using);
        }
    }

    @Override
    public WebElement findElementByName(String using)
    {
        synchronized (baseDriver)
        {
            baseDriver.switchTo().window(tabId);
            return baseDriver.findElementByName(using);
        }
    }

    @Override
    public List<WebElement> findElementsByName(String using)
    {
        synchronized (baseDriver)
        {
            baseDriver.switchTo().window(tabId);
            return baseDriver.findElementsByName(using);
        }
    }

    @Override
    public WebElement findElementByTagName(String using)
    {
        synchronized (baseDriver)
        {
            baseDriver.switchTo().window(tabId);
            return baseDriver.findElementByTagName(using);
        }
    }

    @Override
    public List<WebElement> findElementsByTagName(String using)
    {
        synchronized (baseDriver)
        {
            baseDriver.switchTo().window(tabId);
            return baseDriver.findElementsByTagName(using);
        }
    }

    @Override
    public WebElement findElementByXPath(String using)
    {
        synchronized (baseDriver)
        {
            baseDriver.switchTo().window(tabId);
            return baseDriver.findElementByXPath(using);
        }
    }

    @Override
    public List<WebElement> findElementsByXPath(String using)
    {
        synchronized (baseDriver)
        {
            baseDriver.switchTo().window(tabId);
            return baseDriver.findElementsByXPath(using);
        }
    }

    protected class TabbedWebDriverOptions
        implements Options
    {
        private final Options baseOptions;

        protected TabbedWebDriverOptions(Options base)
        {
            baseOptions = base;
        }

        @Override
        public void addCookie(Cookie cookie)
        {
            synchronized (baseDriver)
            {
                baseDriver.switchTo().window(tabId);
                baseOptions.addCookie(cookie);
            }
        }

        @Override
        public void deleteCookieNamed(String name)
        {
            synchronized (baseDriver)
            {
                baseDriver.switchTo().window(tabId);
                baseOptions.deleteCookieNamed(name);
            }
        }

        @Override
        public void deleteCookie(Cookie cookie)
        {
            synchronized (baseDriver)
            {
                baseDriver.switchTo().window(tabId);
                baseOptions.deleteCookie(cookie);
            }
        }

        @Override
        public void deleteAllCookies()
        {
            synchronized (baseDriver)
            {
                baseDriver.switchTo().window(tabId);
                baseOptions.deleteAllCookies();
            }
        }

        @Override
        public Set<Cookie> getCookies()
        {
            synchronized (baseDriver)
            {
                baseDriver.switchTo().window(tabId);
                return baseOptions.getCookies();
            }
        }

        @Override
        public Cookie getCookieNamed(String name)
        {
            synchronized (baseDriver)
            {
                baseDriver.switchTo().window(tabId);
                return baseOptions.getCookieNamed(name);
            }
        }

        @Override
        public Timeouts timeouts()
        {
            synchronized (baseDriver)
            {
                baseDriver.switchTo().window(tabId);
                return new TabbedWebTimeouts(baseOptions.timeouts());
            }
        }

        @Override
        public ImeHandler ime()
        {
            // TODO: if we need it, need to add locking inside the returned object.
            throw new NotImplementedException("Not implemented");
        }

        @Override
        public Window window()
        {
            // TODO: if we need it, need to add locking inside the returned object.
            throw new NotImplementedException("Not implemented");
        }

        @Override
        public Logs logs()
        {
            // TODO: if we need it, need to add locking inside the returned object.
            throw new NotImplementedException("Not implemented");
        }
    }

    /** TODO: I think these parameters may affect the base driver globally.  It should be ok for us though. */
    protected class TabbedWebTimeouts implements Timeouts
    {
        private final Timeouts baseTimeouts;

        protected TabbedWebTimeouts(Timeouts base)
        {
            baseTimeouts = base;
        }

        @Override
        public Timeouts implicitlyWait(long time, TimeUnit unit)
        {
            return null;
        }

        @Override
        public Timeouts setScriptTimeout(long time, TimeUnit unit)
        {
            return null;
        }

        @Override
        public Timeouts pageLoadTimeout(long time, TimeUnit unit)
        {
            return null;
        }
    }
}
