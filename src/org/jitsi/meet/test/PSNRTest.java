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

import junit.framework.*;
import org.openqa.selenium.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * A test that will run 1 minute (configurable) and will perform a PSNR test
 * on the received video stream.
 *
 * @author George Politis
 */
public class PSNRTest
    extends TestCase
{
    /**
     * The PSNR script that produces PSNR results for every frame that we've
     * captured.
     */
    private static final String PSNR_SCRIPT = "resources/psnr/psnr-test.sh";

    /**
     * The directory where we save the captured frames.
     */
    private static final String OUTPUT_FRAME_DIR= "test-reports/psnr/captured-frames/";

    /**
     * The directory where we get the input frames.
     */
    private static final String INPUT_FRAME_DIR= "resources/psnr/output/stamped/";

    /**
     * The directory to use for frame resizing.
     */
    private static final String RESIZED_FRAME_DIR = "test-reports/psnr/resized-frames/";

    /**
     * The minimum PSNR value that we will accept before failing.
     */
    private static final float MIN_PSNR = 22f;

    /**
     * A test where we read some configurations or fallback to default values
     * and we expect a conference to be already established (by SetupConference)
     * and we keep checking whether this is still the case, and if something
     * is not working we fail.
     */
    public void testPSNR()
    {
        // Create the output directory for captured frames.
        File outputFrameDir = new File(OUTPUT_FRAME_DIR);
        if (!outputFrameDir.exists())
        {
            outputFrameDir.mkdirs();
        }

        String timeToRunInMin = System.getProperty("psnr.duration");

        // default is 1 minute
        if(timeToRunInMin == null || timeToRunInMin.length() == 0)
            timeToRunInMin = "1";

        final int minutesToRun = Integer.valueOf(timeToRunInMin);

        final CountDownLatch waitSignal = new CountDownLatch(1);

        // execute every 1 sec.
        final Timer timer = new Timer();
        timer.schedule(new TimerTask()
        {
            long lastRun = System.currentTimeMillis();

            int millsToRun = minutesToRun*60*1000;

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
                            .findElements(By.xpath(
                                "//video[starts-with(@id, 'remoteVideo_')]"));

                        for (WebElement webElement : remoteThumb)
                        {
                            //FIXME This needs to be optimized. We run this
                            // every second. It encodes an image in base64 and
                            // it transfers it over the network (that's how
                            // selenium communicates with the debugger). So this
                            // might work with a few images per second.. But this
                            // will fail miserably if we want to capture 30fps.
                            // The proper solution would be to store the images
                            // in the sandboxed HTML filesystem that modern
                            // browsers provide. And transfer them at the end
                            // of the test. We could follow the same approach
                            // if we want to grab the whole webm/vp8 stream using
                            // the Recorder API.
                            String elmId = webElement.getAttribute("id");
                            Object pngUrl = js.executeScript(
                                "var video = document.getElementById(\""+ elmId + "\");" +
                                "var canvasId = 'canvas-capture';" +
                                "var canvas = document.getElementById(canvasId);" +
                                "if (canvas == null) {" +
                                "    canvas = document.createElement('canvas');" +
                                "    canvas.id = canvasId;" +
                                "    document.body.appendChild(canvas);" +
                                "}" +
                                "canvas.width = video.videoWidth;" +
                                "canvas.height = video.videoHeight;" +
                                "var ctx = canvas.getContext('2d');" +
                                "ctx.drawImage(video, 0, 0);" +
                                "return canvas.toDataURL(\"image/png\");");

                            // Parse the URI to get only the base64 part
                            String strBase64 = pngUrl.toString()
                                .substring("data:image/png;base64,".length());

                            // Convert it to binary
                            // Java 8 has a Base64 class.
                            byte[] data = org.apache.commons.codec.binary.
                                Base64.decodeBase64(strBase64);

                            try (OutputStream stream = new FileOutputStream(
                                OUTPUT_FRAME_DIR + elmId + "-" + lastRun + ".png")) {
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

        }, /* delay */ 1000, /* period */ 1000);

        try
        {
            waitSignal.await(minutesToRun, TimeUnit.MINUTES);

            if(waitSignal.getCount() == 0)
                assertTrue("A problem with the conf occurred", false);
            else
            {
                Runtime rt = Runtime.getRuntime();
                String[] commands = {
                    PSNR_SCRIPT, OUTPUT_FRAME_DIR,
                    INPUT_FRAME_DIR, RESIZED_FRAME_DIR
                };
                Process proc = rt.exec(commands);

                BufferedReader stdInput = new BufferedReader(new
                    InputStreamReader(proc.getInputStream()));

                BufferedReader stdError = new BufferedReader(new
                    InputStreamReader(proc.getErrorStream()));

                // read the output from the command
                String s = null;
                while ((s = stdInput.readLine()) != null) {
                    assertTrue(s == null
                        || Float.parseFloat(s.split(" ")[1]) > MIN_PSNR);
                }

                // read any errors from the attempted command
                while ((s = stdError.readLine()) != null) {
                    System.err.println(s);
                }
            }
        }
        catch (Exception e)
        {
            assertTrue("An error occurred", false);
        }
    }
}
