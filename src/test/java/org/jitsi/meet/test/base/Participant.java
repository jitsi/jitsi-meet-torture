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
import java.util.logging.*;

/**
 * The participant instance holding the {@link WebDriver}.
 * @param <T> the driver type.
 */
public abstract class Participant<T extends WebDriver>
    implements JavascriptExecutor
{
    /**
     * The seconds between calls that will make sure we keep alive the
     * webdriver session.
     */
    private static final int KEEP_ALIVE_SESSION_INTERVAL = 20;

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
     * Constructs a Participant.
     * @param name the name.
     * @param driver its driver instance.
     * @param type the type (type of browser).
     */
    public Participant(
        String name,
        T driver,
        ParticipantType type)
    {
        this.name = Objects.requireNonNull(name, "name");
        this.driver = Objects.requireNonNull(driver, "driver");
        this.type = Objects.requireNonNull(type, "type");
    }

    /**
     * Joins a conference.
     * @param roomName the room name to join.
     */
    public void joinConference(String roomName)
    {
        this.joinConference(new JitsiMeetUrl().setRoomName(roomName));
    }

    /**
     * Joins a conference.
     *
     * @param meetURL a {@link JitsiMeetUrl} which represents the full
     * conference URL which includes server, conference parameters and
     * the config part. For example:
     * "https://server.com/conference1?login=true#config.debug=true"
     */
    public void joinConference(
        JitsiMeetUrl meetURL)
    {
        meetURL = Objects.requireNonNull(meetURL, "meetURL");

        TestUtils.print(getName() + " is opening URL: " + meetURL);

        // not hungup, so not joining
        // FIXME this should also check for room parameters, config etc.
        if (!this.hungUp
            && this.joinedRoomName != null
            && this.joinedRoomName.equals(meetURL.getRoomName()))
        {
            TestUtils.print("Not joining " + this.name + " in " + meetURL.getRoomName()
                + ", already joined.");
            return;
        }

        doJoinConference(meetURL);

        this.joinedRoomName = meetURL.getRoomName();
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
    private void startKeepAliveExecution()
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
     * Closes this {@link Participant}'s driver.
     */
    public void close()
    {
        TestUtils.print("Closing " + name);

        cancelKeepAlive();

        driver.quit();

        // FIXME missing comment on why this is necessary ? (if it really is...)
        TestUtils.waitMillis(500);
    }

    /**
     * Will call {@link #close()}, but catching any {@link Throwable}s.
     */
    public void closeSafely()
    {
        try
        {
            this.close();
        }
        catch (Throwable t)
        {
            // FIXME: use Logger ?
            t.printStackTrace();
        }
    }

    /**
     * Hangup participant using the UI.
     */
    public void hangUp()
    {
        if (this.hungUp)
        {
            return;
        }

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
     * @return this participant's name.
     */
    public String getName()
    {
        return name;
    }

    private JavascriptExecutor getJSExecutor()
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
        File dstFile = new File(outputDir, fileName);
        try
        {
            FileUtils.moveFile(scrFile, dstFile);
        }
        catch (IOException ioe)
        {
            throw new RuntimeException(
                "Failed to move the screenshot file, from: "
                    + scrFile.toString() + " to: " + dstFile.toString(), ioe);
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

    /**
     * Returns the value for the given <tt>key</tt> from the config.js loaded
     * for the participant.
     *
     * @param key the <tt>String</tt> key from config.js.
     * @return the value for the given <tt>key</tt> from the config.js loaded
     * for the participant.
     */
    public abstract Object getConfigValue(String key);

    /**
     * Presses a shortcut in this {@link Participant}'s driver.
     * @param shortcut the {@link Character} which represents the shortcut to
     * press.
     */
    public abstract void pressShortcut(Character shortcut);

    /**
     * @return the endpoint ID of this participant (i.e. the resource part of
     * the occupant JID in the MUC).
     */
    public String getEndpointId()
    {
        Object o = executeScript("return APP.conference.getMyUserId();");
        return o == null ? null : o.toString();
    }

    /**
     * @return {@code true} if the ICE connection for P2P is in state
     * 'connected' and {@code false} otherwise.
     */
    public boolean isP2pConnected()
    {
        Object result = executeScript(MeetUtils.ICE_CONNECTED_CHECK_SCRIPT);

        return Boolean.valueOf(result.toString());
    }

    /**
     * @return the transport protocol used by the jitsi-videobridge connection
     * for this {@link Participant}, or {@code null}.
     */
    public String getProtocol()
    {
        WebDriver driver = getDriver();

        if (driver == null)
        {
            return null;
        }
        Object protocol
            = executeScript(
                "try {" +
                    "return APP.conference.getStats().transport[0].type;" +
                "} catch (err) { return 'error: '+err; }");

        return (protocol == null) ? null : protocol.toString().toLowerCase();
    }

    /**
     * Checks whether the strophe connection is connected.
     * @return {@code true} iff the XMPP connection is connected.
     */
    public boolean isXmppConnected()
    {
        Object res
            = executeScript(
                "return APP.conference._room.xmpp.connection.connected;");
        return res != null && res.equals(Boolean.TRUE);
    }
}
