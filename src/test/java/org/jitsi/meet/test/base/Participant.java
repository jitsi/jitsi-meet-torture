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

import com.bipmeet.test.util.BMUtils;
import org.apache.commons.io.*;
import org.jitsi.meet.test.base.stats.*;
import org.jitsi.meet.test.pageobjects.*;
import org.jitsi.meet.test.pageobjects.base.*;
import org.jitsi.meet.test.util.*;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;

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
{
    /**
     * The seconds between calls that will make sure we keep alive the
     * webdriver session.
     */
    private static final int KEEP_ALIVE_SESSION_INTERVAL = 20;

    /**
     * The default config which will be set on the {@link JitsiMeetUrl}, before
     * conference is joined.
     */
    private final String defaultConfig;

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
     * The url used to join.
     */
    private JitsiMeetUrl meetUrl = null;

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
     * @param defaultConfig the config which will be set on
     *        the {@link JitsiMeetUrl}, before conference is joined.
     */
    public Participant(
            String name,
            T driver,
            ParticipantType type,
            String defaultConfig)
    {
        this.name = Objects.requireNonNull(name, "name");
        this.driver = Objects.requireNonNull(driver, "driver");
        this.type = Objects.requireNonNull(type, "type");
        this.defaultConfig = defaultConfig;
    }

    /**
     * Does anything that needs to be done just, after the new
     * {@link Participant} has been created.
     */
    public void initialize()
    {
        startKeepAliveExecution();
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
    public void joinConference(JitsiMeetUrl meetURL)
    {
        meetURL = Objects.requireNonNull(meetURL, "meetURL");

        meetURL.appendConfig(defaultConfig, false /* do not override */);

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
        this.meetUrl = meetURL;
        this.hungUp = false;

         BMUtils.clickContinueOnBrowserButton(this);
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

        hangUp();

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
            Logger.getGlobal().log(Level.SEVERE, "Failed to cleanly close participant:" + getName(), t);
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

    /**
     * @return {@link RtpStatistics} for this participant.
     */
    protected abstract RtpStatistics getRtpStatistics();

    /**
     * Returns {@code true} if the ICE connection is currently in the connected
     * state.
     *
     * @return {@code true} for connected or {@code false} for any other state.
     */
    protected abstract boolean isIceConnected();

    @Override
    public String toString()
    {
        return String.format(
            "%s[%s]@%s",
            this.getClass().getSimpleName(),
            name,
            String.valueOf(hashCode()));
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

    /**
     * Waits 5 seconds for this participant to become a moderator.
     */
    public void waitToBecomeModerator()
    {
        waitToBecomeModerator(5);
    }

    /**
     * Waits until this participant becomes a moderator.
     * @param timeout the maximum time to wait in seconds.
     */
    public void waitToBecomeModerator(int timeout)
    {
        waitForCondition(
                this::isModerator,
                timeout,
                toString() + "#waitToBecomeModerator");
    }

    /**
     * Waits 10 seconds until this participant joins the MUC.
     */
    public void waitToJoinMUC()
    {
        waitToJoinMUC(10);
    }

    /**
     * Waits until this participant joins the MUC.
     * @param timeout the maximum time to wait in seconds.
     */
    public void waitToJoinMUC(int timeout)
    {
        waitForCondition(
                this::isInMuc,
                timeout,
                toString() + "#waitToJoinMUC");
    }

    /**
     * Checks whether this participant is in the MUC.
     *
     * @return {@code true} if the specified {@code participant} has joined the
     * room; otherwise, {@code false}
     */
    public abstract boolean isInMuc();

    /**
     * Checks whether this participant is a moderator.
     *
     * @return {@code true} if the this participant is a moderator; otherwise,
     * {@code false}
     */
    public abstract boolean isModerator();

    /**
     * Checks whether this participant has the grant moderator functionality
     * available.
     *
     * @return {@code true} if the this participant has grant moderator
     * functionality otherwise, {@code false}
     */
    public abstract boolean hasGrantModerator();

    /**
     * Waits 15 sec for the given participant to enter the ICE 'connected'
     * state.
     */
    public void waitForIceConnected()
    {
        waitForIceConnected(15);
    }

    /**
     * Waits for a condition.
     *
     * @param condition a {@link BooleanSupplier} which return {@code true} when
     * the condition has been met.
     * @param timeoutSeconds a timeout in seconds.
     * @param label a label which will appear in a timeout exception when
     * the condition is not met withing the time limit.
     */
    public void waitForCondition(
            final BooleanSupplier    condition,
            int                      timeoutSeconds,
            String                   label)
    {
        TestUtils.waitForCondition(
            driver,
            timeoutSeconds,
            new ExpectedCondition<Boolean>()
            {
                @Override
                public Boolean apply(WebDriver webDriver)
                {
                    return condition.getAsBoolean();
                }

                @Override
                public String toString()
                {
                    return label;
                }
            });
    }

    /**
     * Waits for the given participant to enter the ICE 'connected' state.
     * @param timeoutSeconds timeout in seconds.
     */
    public void waitForIceConnected(int timeoutSeconds)
    {
        waitForCondition(
            this::isIceConnected,
            timeoutSeconds,
            toString() + "#isIceConnected");
    }

    /**
     * Waits until data has been sent and received over the ICE connection
     * in this participant.
     */
    public void waitForSendReceiveData()
    {
        this.waitForSendReceiveData(true, true);
    }

    /**
     * Waits until data has been sent and received over the ICE connection
     * in this participant.
     * @param checkSend should we expect and wait for send data.
     * @param checkReceive should we expect and wait for receive data.
     */
    public void waitForSendReceiveData(boolean checkSend, boolean checkReceive)
    {
        waitForCondition(
            () -> {
                RtpStatistics rtpStats = getRtpStatistics();

                return (!checkSend || rtpStats.getUploadBitrate() > 0)
                    && (!checkReceive || rtpStats.getDownloadBitrate() > 0);
            },
            20,
            toString() + "#waitForSendReceiveData");
    }

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
        try
        {
            if (getMeetUrl().getIframeToNavigateTo() != null)
            {
                // let's wait for switch to that iframe, so we can save the correct page
                WebDriverWait wait = new WebDriverWait(driver, 60);
                wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(
                    By.id(getMeetUrl().getIframeToNavigateTo())));
            }

            FileUtils.openOutputStream(new File(outputDir, fileName))
                .write(driver.getPageSource().replace(">", ">\n").getBytes());
        }
        catch(Exception e)
        {
            Logger.getGlobal().log(Level.SEVERE, "Failed to saveHtmlSource to:" + fileName, e);

            try
            {
                // will try the main page
                FileUtils.openOutputStream(new File(outputDir, fileName))
                    .write(driver.getPageSource().replace(">", ">\n").getBytes());
            }
            catch(Exception ex)
            {
                e.printStackTrace();
            }
        }
    }

    /**
     * Obtains JitsiMeet debug logs.
     *
     * XXX On web the value is obtained from "APP.connection.getLogs()", but
     * it's done by executing a script, so no idea at this point when that will
     * be available on mobile.
     *
     * @return a string with logs.
     */
    public abstract String getMeetDebugLog();

    /**
     * Obtains JitsiMeet rtp stats.
     *
     * XXX On web the value is obtained from
     * "APP.conference._room.jvbJingleSession.peerconnection.stats", but
     * it's done by executing a script, so no idea at this point when that will
     * be available on mobile.
     *
     * @return a string with logs.
     */
    public abstract String getRTPStats();

    /**
     * A list of log entries, which toString() output can be written to
     * a log file.
     *
     * @return a list of log entries.
     */
    public abstract List<Object> getBrowserLogs();

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
    public abstract String getEndpointId();

    /**
     * @return {@code true} if the ICE connection for P2P is in state
     * 'connected' and {@code false} otherwise.
     */
    public abstract boolean isP2pConnected();

    /**
     * @return the transport protocol used by the jitsi-videobridge connection
     * for this {@link Participant}, or {@code null}.
     */
    public abstract String getProtocol();

    /**
     * Checks whether the strophe connection is connected.
     * @return {@code true} iff the XMPP connection is connected.
     */
    public abstract boolean isXmppConnected();

    /**
     * @return a representation of the filmstrip of this participant.
     */
    public abstract Filmstrip<? extends Participant> getFilmstrip();

    /**
     * @return a list of remote participants thumbnails.
     */
    public abstract List<RemoteParticipant<T>> getRemoteParticipants();

    /**
     * Returns a remote participant by id.
     * @param id the id to use when searching for remote participant.
     * @return <tt>RemoteParticipant</tt> instance if found,
     * <tt>null</tt> otherwise.
     */
    public RemoteParticipant<T> getRemoteParticipantById(String id)
    {
        return getRemoteParticipants().stream()
            .filter(p -> p.getEndpointId().equals(id))
            .findFirst()
            .orElse(null);
    }

    /**
     * The url with all options used to join the conference.
     * @return
     */
    public JitsiMeetUrl getMeetUrl()
    {
        return meetUrl;
    }
}
