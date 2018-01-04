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
package org.jitsi.meet.test.tasks;

import org.jitsi.meet.test.*;
import org.jitsi.meet.test.util.*;
import org.openqa.selenium.*;
import org.testng.*;

import java.util.*;
import java.util.concurrent.*;

/**
 * A HeartbeatTask class can be used to make sure that the conference is running
 * for specific period of time. Calling {@link #await(long, TimeUnit)} will
 * block the thread until either the specified time elapses or the conference
 * breaks. The latter will result in the current test failure.
 *
 * @author George Politis
 * @author Pawel Domas
 */
public class HeartbeatTask
    extends TimerTask
{
    private final Timer timer = new Timer();
    private final CountDownLatch waitSignal = new CountDownLatch(1);
    private final boolean enableBitrateCheck;
    private final WebDriver participant1;
    private final WebDriver participant2;

    public HeartbeatTask(
        WebDriver participant1,
        WebDriver participant2,
        int millsToRun, boolean enableBitrateCheck)
    {
        this.participant1 = participant1;
        this.participant2 = participant2;
        // XXX bitrate checks fail on beta. not enough bandwidth to check
        // why.
        this.enableBitrateCheck = enableBitrateCheck;
        this.millsToRun = millsToRun;
    }

    long lastRun = System.currentTimeMillis();
    int millsToRun;

    CountDownLatch ownerDownloadSignal = new CountDownLatch(3);
    CountDownLatch secondPDownloadSignal = new CountDownLatch(3);

    /**
     * Starts the <tt>HeartbeatTask</tt>. From now on it will be checking
     * conference state for failures.
     * @param startDelayMs delay between call to this method and first
     * conference state check(in milliseconds).
     * @param intervalMs how often this <tt>HeartbeatTask</tt> will be doing
     * conference state checks(in milliseconds).
     */
    public void start(long startDelayMs, long intervalMs)
    {
        timer.schedule(this, startDelayMs, intervalMs);
    }

    @Override
    public void run()
    {
        try
        {
            TestUtils.print("Checking at " + new Date()
                + " / to finish: " + millsToRun + " ms.");

            if (!MeetUtils.isIceConnected(participant1))
            {
                assertAndQuit("Owner ice is not connected.");
                return;
            }

            if (!MeetUtils.isInMuc(participant1))
            {
                assertAndQuit("Owner is not in the muc.");
                return;
            }

            if (!MeetUtils.isIceConnected(participant2))
            {
                assertAndQuit(
                    "Second participant ice is not connected.");
                return;
            }

            if (!MeetUtils.isInMuc(participant2))
            {
                assertAndQuit(
                    "The second participant is not in the muc.");
                return;
            }

            long downloadOwner = MeetUtils.getDownloadBitrate(participant1);
            long downloadParticipant =
                MeetUtils.getDownloadBitrate(participant2);

            if (downloadOwner <= 0)
            {
                TestUtils.print("Owner no download bitrate");
                ownerDownloadSignal.countDown();
            }
            else
                ownerDownloadSignal = new CountDownLatch(3);

            if (ownerDownloadSignal.getCount() <= 0)
            {
                assertAndQuit("Owner download bitrate less than 0");
                return;
            }

            if (enableBitrateCheck && downloadParticipant <= 0)
            {
                TestUtils.print(
                   "Second participant no download bitrate");
               secondPDownloadSignal.countDown();
            }
            else
                secondPDownloadSignal = new CountDownLatch(3);

            if (enableBitrateCheck && secondPDownloadSignal.getCount() <= 0)
            {
                assertAndQuit(
                    "Second participant download rate less than 0");
                return;
            }

            if (!MeetUtils.isXmppConnected(participant1))
            {
                assertAndQuit("Owner xmpp connection is not connected");
                return;
            }

            if (!MeetUtils.isXmppConnected(participant2))
            {
                assertAndQuit("The second participant xmpp "
                    + "connection is not connected");
                return;
            }

            long currentTime = System.currentTimeMillis();
            millsToRun -= (currentTime - lastRun);
            lastRun = currentTime;

            if (millsToRun <= 0)
            {
                timer.cancel();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();

            assertAndQuit("Unexpected error occurred.");
        }
    }

    /**
     * Clears what is needed and lowers the assert countdown.
     *
     * @param msg
     */
    private void assertAndQuit(String msg)
    {
        TestUtils.print(msg);
        waitSignal.countDown();
        timer.cancel();
    }

    /**
     * Calling this will make the current thread wait till the conference runs
     * for specified amount of time. If conference breaks in the meantime the
     * current test will fail(Assert.fail() method is called}.
     * @param timeout the amount of time for the conference to run without any
     * problems.
     * @param timeUnit the time unit of the <tt>timeout</tt>.
     */
    public void await(long timeout, TimeUnit timeUnit)
    {
        try
        {
            waitSignal.await(timeout, timeUnit);

            if (waitSignal.getCount() == 0)
                Assert.fail("A problem with the conf occurred");
        }
        catch (Exception e)
        {
            e.printStackTrace();

            Assert.fail("An error occurred: " + e.getMessage());
        }
    }
}
