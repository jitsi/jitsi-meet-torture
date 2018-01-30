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
package org.jitsi.meet.test.base;

import org.jitsi.meet.test.util.*;
import org.openqa.selenium.*;
import org.testng.annotations.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * The participant instance holding the {@link WebDriver}.
 * @param <T> the driver type.
 */
public abstract class Participant<T extends WebDriver>
    implements JavascriptExecutor
{
    /**
     * The driver.
     */
    private final T driver;

    /**
     * The participant type chrome, firefox etc.
     */
    private final ParticipantFactory.ParticipantType type;

    /**
     * The name of the participant.
     */
    private final String name;

    /**
     * The url to join conferences.
     */
    private final String meetURL;

    /**
     * Is hung up.
     */
    boolean hungUp = true;

    /**
     * We store the room name we joined as if someone calls joinConference twice
     * to be able to detect that there is no need to load anything.
     */
    String joinedRoomName = null;

    /**
     * Executor that is responsible for keeping the participant session alive.
     */
    private ScheduledExecutorService executor
        = Executors.newScheduledThreadPool(1);

    /**
     * The execution of the keepalive, if it is null this means
     * it is not started.
     */
    private ScheduledFuture<?> keepAliveExecution = null;

    /**
     * The seconds between calls that will make sure we keep alive the
     * webdriver session.
     */
    private static final int KEEP_ALIVE_SESSION_INTERVAL = 20;

    /**
     * Constructs a Participant.
     * @param name the name.
     * @param driver its driver instance.
     * @param type the type (type of browser).
     * @param meetURL the url to use when joining room.
     */
    public Participant(
        String name,
        T driver,
        ParticipantFactory.ParticipantType type,
        String meetURL)
    {
        this.name = name;
        this.driver = Objects.requireNonNull(driver, "driver");
        this.type = type;
        this.meetURL = meetURL;
    }

    /**
     * Joins a conference.
     * @param roomName the room name to join.
     */
    public void joinConference(String roomName)
    {
        this.joinConference(roomName, null);
    }

    /**
     * Joins a conference.
     * @param roomName the room name to join.
     * @param fragment adds the given string to the fragment part of the URL.
     */
    public void joinConference(String roomName, String fragment)
    {
        // not hungup, so not joining
        if (!this.hungUp
            && this.joinedRoomName != null
            && this.joinedRoomName.equals(roomName))
        {
            TestUtils.print("Not joining " + this.name + " in " + roomName
                + ", already joined.");
            return;
        }

        this.joinedRoomName = roomName;

        String URL = this.meetURL + "/" + roomName;
        URL += "#config.requireDisplayName=false";
        URL += "&config.debug=true";
        URL += "&config.disableAEC=true";
        URL += "&config.disableNS=true";
        URL += "&config.callStatsID=false";
        URL += "&config.alwaysVisibleToolbar=true";
        URL += "&config.p2p.enabled=false";
        URL += "&config.disable1On1Mode=true";

        if (fragment != null)
            URL += "&" + fragment;

        TestUtils.print(name + " is opening URL: " + URL);

        {
            // with chrome v52 we start getting error:
            // "Timed out receiving message from renderer" and
            // "Navigate timeout: cannot determine loading status"
            // seems its a bug or rare problem, maybe concerns async loading
            // of resources ...
            // https://bugs.chromium.org/p/chromedriver/issues/detail?id=402
            // even there is a TimeoutException the page is loaded correctly
            // and driver is operating, we just lower the page load timeout
            // default is 3 minutes and we log and skip this exception
            driver.manage().timeouts()
                .pageLoadTimeout(30, TimeUnit.SECONDS);
            try
            {
                driver.get(URL);
            }
            catch (org.openqa.selenium.TimeoutException ex)
            {
                ex.printStackTrace();
                TestUtils.print("TimeoutException while loading page, "
                    + "will skip it and continue:" + ex.getMessage());
            }
        }
        MeetUtils.waitForPageToLoad(driver);

        // disables animations
        executeScript("try { jQuery.fx.off = true; } catch(e) {}");

        executeScript("APP.UI.dockToolbar(true);");

        // disable keyframe animations (.fadeIn and .fadeOut classes)
        executeScript("$('<style>.notransition * { "
            + "animation-duration: 0s !important; "
            + "-webkit-animation-duration: 0s !important; transition:none; }"
            + " </style>').appendTo(document.head);");
        executeScript("$('body').toggleClass('notransition');");

        // disable the blur effect in firefox as it has some performance issues
        if (this.type == ParticipantFactory.ParticipantType.firefox)
        {
            executeScript(
                "try { var blur "
                    + "= document.querySelector('.video_blurred_container'); "
                    + "if (blur) { "
                    + "document.querySelector('.video_blurred_container')"
                    + ".style.display = 'none' "
                    + "} } catch(e) {}");
        }

        // Hack-in disabling of callstats (old versions of jitsi-meet don't
        // handle URL parameters)
        executeScript("config.callStatsID=false;");

        String version
            = TestUtils.executeScriptAndReturnString(driver,
                "return JitsiMeetJS.version;");
        TestUtils.print(name + " lib-jitsi-meet version: " + version);

        executeScript("document.title='" + name + "'");

        this.hungUp = false;

        startKeepAliveExecution();
    }

    /**
     * Starts the keep-alive execution.
     */
    public void startKeepAliveExecution()
    {
        if (this.keepAliveExecution == null)
        {
            this.keepAliveExecution = this.executor
                .scheduleAtFixedRate(
                    () -> driver.getCurrentUrl(),
                    KEEP_ALIVE_SESSION_INTERVAL,
                    KEEP_ALIVE_SESSION_INTERVAL,
                    TimeUnit.SECONDS);
        }
    }

    /**
     * Cancels keep alive execution.
     */
    private synchronized void cancelKeepAlive()
    {
        if (this.keepAliveExecution != null)
        {
            this.keepAliveExecution.cancel(true);
            this.keepAliveExecution = null;
        }
    }

    /**
     * Quits the driver.
     */
    public void quit()
    {
        try
        {
            cancelKeepAlive();

            driver.quit();

            TestUtils.waitMillis(500);
        }
        catch(Throwable t)
        {
            t.printStackTrace();
        }

    }

    /**
     * Hangup participant using the UI.
     */
    public void hangUp()
    {
        if (this.hungUp)
            return;

        MeetUIUtils.clickOnButton(
            driver, "toolbar_button_hangup", false);

        TestUtils.waitMillis(500);

        this.hungUp = true;
        this.joinedRoomName = null;

        TestUtils.print("Hung up in " + name + ".");

        // open a blank page after hanging up, to make sure
        // we will successfully navigate to the new link containing the
        // parameters, which change during testing
        driver.get("about:blank");
        MeetUtils.waitForPageToLoad(driver);
    }

    /**
     * The driver instance.
     * @return driver instance.
     */
    public T getDriver()
    {
        return driver;
    }

    @Override
    public String toString()
    {
        return super.toString();
    }

    /**
     * Returns the participant type.
     * @return the participant type.
     */
    public ParticipantFactory.ParticipantType getType()
    {
        return type;
    }

    /**
     * Returns participant's name.
     * @return
     */
    public String getName()
    {
        return name;
    }

    /**
     * Executes a script in this {@link Participant}'s {@link WebDriver}.
     * See {@link JavascriptExecutor#executeScript(String, Object...)}.
     */
    @Override
    public Object executeScript(String var1, Object... var2)
    {
        return ((JavascriptExecutor) getDriver()).executeScript(var1, var2);
    }

    /**
     * Executes a script asynchronously in this {@link Participant}'s
     * {@link WebDriver}.
     * See {@link JavascriptExecutor#executeScript(String, Object...)}.
     */
    @Override
    public Object executeAsyncScript(String var1, Object... var2)
    {
        return
            ((JavascriptExecutor) getDriver()).executeAsyncScript(var1, var2);
    }

    /**
     * Waits until this participant joins the MUC.
     * @param timeout the maximum time to wait in seconds.
     */
    public abstract void waitToJoinMUC(long timeout);

    /**
     * Checks whether this participant is in the MUC.
     *
     * @return {@code true} if the specified {@code participant} has joined the
     * room; otherwise, {@code false}
     */
    public abstract boolean isInMuc();

    /**
     * Waits 15 sec for the given participant to enter the ICE 'connected'
     * state.
     */
    public abstract void waitForIceConnected();

    /**
     * Waits for the given participant to enter the ICE 'connected' state.
     * @param timeout timeout in seconds.
     */
    public abstract void waitForIceConnected(long timeout);

    /**
     * Waits until data has been sent and received over the ICE connection
     * in this participant.
     */
    public abstract void waitForSendReceiveData();

    /**
     * Waits for number of remote streams.
     * @param n number of remote streams to wait for.
     */
    public abstract void waitForRemoteStreams(int n);

    /**
     * Sets the display name of the local participant.
     * @param name the name to set.
     */
    public abstract void setDisplayName(String name);

    /**
     * Takes screenshot.
     * @param outputDir the screenshots output directory.
     * @param fileName the destination screenshot file name.
     */
    public void takeScreenshot(File outputDir, String fileName)
    {
        if (!(driver instanceof TakesScreenshot))
        {
            TestUtils.print(
                "Driver does not support taking screenshots ! FileName:"
                    + fileName);
            return;
        }

        TakesScreenshot takesScreenshot = (TakesScreenshot) driver;

        File scrFile = takesScreenshot.getScreenshotAs(OutputType.FILE);
        if (!scrFile.renameTo(new File(outputDir, fileName)))
        {
            throw new RuntimeException(
                "Failed to rename screenshot file to directory name: "
                    + outputDir.toString() + ", file name: " + fileName
                    + ", original file: " + scrFile.toString());
        }
    }

    /**
     * Presses a shortcut in this {@link Participant}'s driver.
     * @param shortcut the {@link Character} which represents the shortcut to
     * press.
     */
    public abstract void pressShortcut(Character shortcut);
}
