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

import java.util.*;
import java.util.concurrent.*;

/**
 *
 * @author George Politis
 */
public class HeartbeatTask
    extends TimerTask
{
    private final Timer timer;
    private final CountDownLatch waitSignal;
    private final boolean enableBitrateCheck;

    public HeartbeatTask(Timer timer, CountDownLatch waitSignal, int millsToRun,
                         boolean enableBitrateCheck)
    {
        // XXX bitrate checks fail on beta. not enough bandwidth to check
        // why.
        this.enableBitrateCheck = enableBitrateCheck;
        this.timer = timer;
        this.waitSignal = waitSignal;
        this.millsToRun = millsToRun;
    }

    long lastRun = System.currentTimeMillis();
    int millsToRun;

    CountDownLatch ownerDownloadSignal = new CountDownLatch(3);
    CountDownLatch secondPDownloadSignal = new CountDownLatch(3);

    @Override
    public void run()
    {
        try
        {
            System.err.println("Checking at " + new Date()
                + " / to finish: " + millsToRun + " ms.");

            if (!ConferenceFixture.isIceConnected(
                ConferenceFixture.getOwner()))
            {
                assertAndQuit("Owner ice is not connected.");
                return;
            }

            if (!ConferenceFixture.isInMuc(
                ConferenceFixture.getOwner()))
            {
                assertAndQuit("Owner is not in the muc.");
                return;
            }

            if (!ConferenceFixture.isIceConnected(
                ConferenceFixture.getSecondParticipant()))
            {
                assertAndQuit(
                    "Second participant ice is not connected.");
                return;
            }

            if (!ConferenceFixture.isInMuc(
                ConferenceFixture.getSecondParticipant()))
            {
                assertAndQuit(
                    "The second participant is not in the muc.");
                return;
            }

            long downloadOwner = ConferenceFixture.getDownloadBitrate(
                ConferenceFixture.getOwner());
            long downloadParticipant =
                ConferenceFixture.getDownloadBitrate(
                    ConferenceFixture.getSecondParticipant());

            if (downloadOwner <= 0)
            {
                System.err.println("Owner no download bitrate");
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
                System.err.println(
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

            if (!ConferenceFixture.isXmppConnected(
                ConferenceFixture.getOwner()))
            {
                assertAndQuit("Owner xmpp connection is not connected");
                return;
            }

            if (!ConferenceFixture.isXmppConnected(
                ConferenceFixture.getSecondParticipant()))
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
        System.err.println(msg);
        waitSignal.countDown();
        timer.cancel();
    }
}
