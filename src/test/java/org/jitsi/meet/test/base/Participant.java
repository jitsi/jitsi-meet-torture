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

import org.apache.commons.io.*;
import org.jitsi.meet.test.util.*;
import org.openqa.selenium.*;
import org.openqa.selenium.logging.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;
import java.util.logging.*;

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
    protected final T driver;

    /**
     * The participant type chrome, firefox etc.
     */
    protected final ParticipantType type;

    /**
     * The name of the participant.
     */
    protected final String name;

    /**
     * The url to join conferences.
     */
    private final JitsiMeetUrl meetURL = new JitsiMeetUrl();

    /**
     * Is hung up.
     */
    private boolean hungUp = true;

    /**
     * We store the room name we joined as if someone calls joinConference twice
     * to be able to detect that there is no need to load anything.
     */
    private String joinedRoomName = null;

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
     * @param defaultConfigPart the hash part of the URL which specifies default
     * config overrides. For example:
     * "config.requireDisplayName=false&config.debug=true"
     * This will be added for every conference URL joined by this
     * <tt>Participant</tt> instance. Note that the hash sign ("#") is added
     * automagically, so it should be omitted.
     */
    public Participant(
        String name,
        T driver,
        ParticipantType type,
        String meetURL,
        String defaultConfigPart)
    {
        this.name = Objects.requireNonNull(name, "name");
        this.driver = Objects.requireNonNull(driver, "driver");
        this.type = Objects.requireNonNull(type, "type");
        this.meetURL.setServerUrl(meetURL);
        this.meetURL.appendConfig(defaultConfigPart);
    }

    /**
     * Joins a conference.
     * @param roomName the room name to join.
     */
    public void joinConference(String roomName)
    {
        this.joinConference(roomName, null, null, null);
    }

    /**
     * Joins a conference.
     *
     * @param roomName the room name to join.
     * @param roomParameter the {@link JitsiMeetUrl#roomParameters} part of
     * the Jitsi Meet URL.
     * @param config the {@link JitsiMeetUrl#hashConfigPart} which will be
     * appended at the end of the default config part (if any) specified in the
     * {@link Participant}'s constructor.
     */
    public void joinConference(
        String roomName, String roomParameter, String config,
        Consumer<JitsiMeetUrl> customJoinImpl)
    {
        JitsiMeetUrl conferenceUrl = (JitsiMeetUrl) this.meetURL.clone();

        conferenceUrl.setRoomName(roomName);
        conferenceUrl.setRoomParameters(roomParameter);
        conferenceUrl.appendConfig(config);

        TestUtils.print(getName() + " is opening URL: " + conferenceUrl);

        // not hungup, so not joining
        // FIXME this should also check for room parameters, config etc.
        if (!this.hungUp
            && this.joinedRoomName != null
            && this.joinedRoomName.equals(roomName))
        {
            TestUtils.print("Not joining " + this.name + " in " + roomName
                + ", already joined.");
            return;
        }

        if (customJoinImpl != null)
        {
            customJoinImpl.accept(conferenceUrl);
        }
        else
        {
            doJoinConference(conferenceUrl);
        }

        this.joinedRoomName = roomName;
        this.hungUp = false;

        startKeepAliveExecution();
    }

    /**
     * Implements the logic of joining a conference.
     * @param conferenceUrl a {@link JitsiMeetUrl} which represents the full
     * conference URL which includes server, conference parameters and
     * the config part. For example:
     * "https://server.com/conference1?login=true#config.debug=true"
     */
    protected abstract void doJoinConference(JitsiMeetUrl conferenceUrl);

    /**
     * Starts the keep-alive execution.
     */
    public void startKeepAliveExecution()
    {
        if (this.keepAliveExecution == null)
        {
            this.keepAliveExecution = this.executor
                .scheduleAtFixedRate(
                    driver::getCurrentUrl,
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

        doHangUp();

        TestUtils.print("Hung up in " + name + ".");

        this.hungUp = true;
        this.joinedRoomName = null;
    }

    /**
     * Does hang up the participant.
     */
    protected abstract void doHangUp();

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
    public ParticipantType getType()
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

    public JavascriptExecutor getJSExecutor()
    {
        return driver instanceof JavascriptExecutor
            ? (JavascriptExecutor) driver : null;
    }

    /**
     * Executes a script in this {@link Participant}'s {@link WebDriver}.
     * See {@link JavascriptExecutor#executeScript(String, Object...)}.
     */
    @Override
    public Object executeScript(String var1, Object... var2)
    {
        JavascriptExecutor executor = getJSExecutor();

        return executor != null
            ? executor.executeScript(var1, var2) : null;
    }

    /**
     * Executes a script asynchronously in this {@link Participant}'s
     * {@link WebDriver}.
     * See {@link JavascriptExecutor#executeScript(String, Object...)}.
     */
    @Override
    public Object executeAsyncScript(String var1, Object... var2)
    {
        JavascriptExecutor executor = getJSExecutor();

        return executor != null
            ? executor.executeAsyncScript(var1, var2) : null;
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
     * Saves the html source of the supplied page.
     * @param outputDir the output directory.
     * @param fileName the destination html file name.
     */
    public void saveHtmlSource(File outputDir, String fileName)
    {
        try(FileOutputStream fOut
                = FileUtils.openOutputStream(
                        new File(outputDir, fileName)))
        {
            fOut.write(
                    driver.getPageSource().replace(">",">\n").getBytes());
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public String getMeetDebugLog()
    {
        try
        {
            Object log
                = executeScript(
                    "try{ "
                        + "return JSON.stringify("
                        + "  APP.conference.getLogs(), null, '    ');"
                        + "}catch (e) {}");

            return log instanceof String ? (String) log : null;
        }
        catch (Exception e)
        {
            Logger.getGlobal()
                .log(Level.SEVERE, "Failed to get meet logs from " + name, e);

            return null;
        }
    }

    public LogEntries getBrowserLogs()
    {
        if (type.isFirefox())
        {
            // not currently supported in FF
            // https://github.com/SeleniumHQ/selenium/issues/2910
            return null;
        }

        return driver.manage().logs().get(LogType.BROWSER);
    }

    public File getChromeWebDriverLogFile()
    {
        if (type.isChrome())
        {
            File srcFile
                = new File(
                        ParticipantFactory.getChromeWebDriverLogFile(name));

            // in case of remote driver the file does not exist
            if (srcFile.exists())
                return srcFile;
        }

        return null;
    }

    /**
     * Presses a shortcut in this {@link Participant}'s driver.
     * @param shortcut the {@link Character} which represents the shortcut to
     * press.
     */
    public abstract void pressShortcut(Character shortcut);
}
