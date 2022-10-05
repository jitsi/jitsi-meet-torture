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

import org.jitsi.meet.test.base.*;
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
    private final Participant participant1;
    private final Participant participant2;
    private long lastRun = System.currentTimeMillis();
    private int millsToRun;

    private CountDownLatch downloadSignal1 = new CountDownLatch(3);
    private CountDownLatch downloadSignal2 = new CountDownLatch(3);

    public HeartbeatTask(
        Participant participant1,
        Participant participant2,
        int millsToRun, boolean enableBitrateCheck)
    {
        this.participant1 = participant1;
        this.participant2 = participant2;
        // XXX bitrate checks fail on beta. not enough bandwidth to check
        // why.
        this.enableBitrateCheck = enableBitrateCheck;
        this.millsToRun = millsToRun;
    }


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

            if (!MeetUtils.isIceConnected(participant1.getDriver()))
            {
                assertAndQuit("Participant1 ice is not connected.");
                return;
            }

            if (!participant1.isInMuc())
            {
                assertAndQuit("Participant1 is not in the muc.");
                return;
            }

            if (!MeetUtils.isIceConnected(participant2.getDriver()))
            {
                assertAndQuit(
                    "Participant2 ice is not connected.");
                return;
            }

            if (!participant2.isInMuc())
            {
                assertAndQuit(
                    "Participant2 is not in the muc.");
                return;
            }

            long download1
                = MeetUtils.getDownloadBitrate(participant1.getDriver());
            long download2 =
                MeetUtils.getDownloadBitrate(participant2.getDriver());

            if (download1 <= 0)
            {
                TestUtils.print("Participant1 no download bitrate");
                downloadSignal1.countDown();
            }
            else
            {
                downloadSignal1 = new CountDownLatch(3);
            }

            if (downloadSignal1.getCount() <= 0)
            {
                assertAndQuit("Participant1 download bitrate less than 0");
                return;
            }

            if (enableBitrateCheck && download2 <= 0)
            {
                TestUtils.print(
                   "Participant2 no download bitrate");
               downloadSignal2.countDown();
            }
            else
            {
                downloadSignal2 = new CountDownLatch(3);
            }

            if (enableBitrateCheck && downloadSignal2.getCount() <= 0)
            {
                assertAndQuit(
                    "Participant2 download rate less than 0");
                return;
            }

            if (!participant1.isXmppConnected())
            {
                assertAndQuit("Participant1 xmpp connection is not connected");
                return;
            }

            if (!participant2.isXmppConnected())
            {
                assertAndQuit("Participant2's xmpp connection is not connected");
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
            // Chrome crashed
            if (e instanceof WebDriverException && e.getMessage() != null
                && e.getMessage().startsWith("chrome not reachable"))
            {
                // skip this check
                return;
            }

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

            timer.cancel();

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
