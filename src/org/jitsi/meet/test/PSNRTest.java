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
package org.jitsi.meet.test;

import com.gargoylesoftware.htmlunit.javascript.background.*;
import junit.framework.*;
import org.openqa.selenium.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * A test that will run for hours (configurable) and will continuously check
 * the connection and the conference and if it detects a problem will fail.
 * @author Damian Minkov
 */
public class PSNRTest
    extends TestCase
{
    /**
     * A test where we read some configurations or fallback to default values
     * and we expect a conference to be already established (by SetupConference)
     * and we keep checking whether this is still the case, and if something
     * is not working we fail. This one is supposed to run for a long period
     * of time.
     */
    public void testLongLive()
    {
        String timeToRunInMin = System.getProperty("psnr.duration");

        // default is 10 minutes
        if(timeToRunInMin == null || timeToRunInMin.length() == 0)
            timeToRunInMin = "10";

        final int minutesToRun = Integer.valueOf(timeToRunInMin);

        final CountDownLatch waitSignal = new CountDownLatch(1);

        // execute every 10 secs.
        final Timer timer = new Timer();
        timer.schedule(new TimerTask()
        {
            long lastRun = System.currentTimeMillis();
            // sometimes the check is executed once more
            // at the time while we are in a process of disposing
            // the two participants and ~9 secs before finishing successful
            // it fails.
            int millsToRun = (minutesToRun - 1)*60*1000;

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

                    if(!ConferenceFixture.isInMuc(
                            ConferenceFixture.getOwner()))
                    {
                        assertAndQuit("Owner is not in the muc.");
                        return;
                    }

                    if(!ConferenceFixture.isIceConnected(
                            ConferenceFixture.getSecondParticipant()))
                    {
                        assertAndQuit(
                            "Second participant ice is not connected.");
                        return;
                    }

                    if(!ConferenceFixture.isInMuc(
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

                    if(downloadOwner <= 0)
                    {
                        System.err.println("Owner no download bitrate");
                        ownerDownloadSignal.countDown();
                    }
                    else
                        ownerDownloadSignal = new CountDownLatch(3);

                    if(ownerDownloadSignal.getCount() <= 0)
                    {
                        assertAndQuit("Owner download bitrate less than 0");
                        return;
                    }

                    if(downloadParticipant <= 0)
                    {
                        System.err.println(
                            "Second participant no download bitrate");
                        secondPDownloadSignal.countDown();
                    }
                    else
                        secondPDownloadSignal = new CountDownLatch(3);

                    if(secondPDownloadSignal.getCount() <= 0)
                    {
                        assertAndQuit(
                            "Second participant download rate less than 0");
                        return;
                    }

                    if(!ConferenceFixture.isXmppConnected(
                            ConferenceFixture.getOwner()))
                    {
                        assertAndQuit("Owner xmpp connection is not connected");
                        return;
                    }

                    if(!ConferenceFixture.isXmppConnected(
                                ConferenceFixture.getSecondParticipant()))
                    {
                        assertAndQuit("The second participant xmpp "
                            + "connection is not connected");
                        return;
                    }

                    WebDriver driver = ConferenceFixture.getOwner();
                    if (driver instanceof JavascriptExecutor)
                    {
                        JavascriptExecutor js = ((JavascriptExecutor) driver);

                        List<WebElement> remoteThumb = driver
                            .findElements(By.tagName("video"));


                        for (WebElement webElement : remoteThumb)
                        {
                            String elmId = webElement.getAttribute("id");
                            Object pngUrl = js.executeScript(
                                "    var video = document.getElementById(\"" + elmId + "\");\n" +
                                "    var canvasId = 'canvas-capture';\n" +
                                "    var canvas = document.getElementById(canvasId);\n" +
                                "    if (canvas == null) {\n" +
                                "        canvas = document.createElement('canvas');\n" +
                                "        canvas.id = canvasId;\n" +
                                "        document.body.appendChild(canvas);\n" +
                                "    }\n" +
                                "    canvas.width = video.videoWidth;\n" +
                                "    canvas.height = video.videoHeight;\n" +
                                "    var ctx = canvas.getContext('2d');\n" +
                                "    ctx.drawImage(video, 0, 0);\n" +
                                "    return canvas.toDataURL(\"image/png\");");

                            // Parse the URI to get only the base64 part
                            String strBase64
                                = pngUrl.toString().substring("data:image/png;base64,".length());

                            // Convert it to binary
                            byte[] data = Base64.getDecoder().decode(strBase64);
                            try (OutputStream stream = new FileOutputStream("/tmp/" + elmId + "-" + lastRun + ".png")) {
                                stream.write(data);
                            }
                        }
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
             * @param msg
             */
            private void assertAndQuit(String msg)
            {
                System.err.println(msg);
                waitSignal.countDown();
                timer.cancel();
            }

        }, 10 * 1000, 10 * 1000);

        try
        {
            waitSignal.await(minutesToRun, TimeUnit.MINUTES);

            if(waitSignal.getCount() == 0)
                assertTrue("A problem with the conf occurred", false);
        }
        catch (Exception e)
        {
            assertTrue("An error occurred", false);
        }
    }
}
