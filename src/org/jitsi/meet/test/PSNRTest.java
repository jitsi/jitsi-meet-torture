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
import org.jitsi.meet.test.tasks.*;
import org.openqa.selenium.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.nio.file.*;

/**
 * A test that will run 1 minute (configurable) and will perform a PSNR test
 * on the received video stream.
 *
 * @author George Politis
 * @author Ivan Symchych
 */
public class PSNRTest
    extends TestCase
{
    /**
     * The PSNR script that produces PSNR results for every frame that we've
     * captured.
     */
    private static final String PSNR_SCRIPT = "scripts/psnr-test.sh";

    /**
     * JS utility which allows to capture frames from the video.
     */
    private static final String PSNR_JS_SCRIPT
        = "resources/PSNRVideoOperator.js";

    /**
     * The directory where we save the captured frames.
     */
    private static final String OUTPUT_FRAME_DIR
        = "test-reports/psnr/captured-frames/";

    /**
     * The directory where we get the input frames.
     */
    public static final String INPUT_FRAME_DIR= "resources/psnr/stamped/";

    /**
     * The directory to use for frame resizing.
     */
    private static final String RESIZED_FRAME_DIR
        = "test-reports/psnr/resized-frames/";

    /**
     * The minimum PSNR value that we will accept before failing. PSNR above 20
     * is pretty indicative of good similarity. For example: Downscaling a 720p
     * image to 360p gives a PSNR of 27.2299. Downscaling a 720p image to 180p
     * gives a PSNR of 21.8882. Downscaling a 720p image to 90p gives a PSNR of
     * 20.1337
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
        File inputFrameDir = new File(INPUT_FRAME_DIR);
        if (!inputFrameDir.exists())
        {
            // Skip the PSNR tests because we don't have any PSNR
            // resources.
            return;
        }
        // Create the output directory for captured frames.
        File outputFrameDir = new File(OUTPUT_FRAME_DIR);
        if (!outputFrameDir.exists())
        {
            outputFrameDir.mkdirs();
        }

        // stop everything to maximize performance
        new MuteTest("muteOwnerAndCheck").muteOwnerAndCheck();
        new MuteTest("muteParticipantAndCheck").muteParticipantAndCheck();
        new StopVideoTest("stopVideoOnOwnerAndCheck").stopVideoOnOwnerAndCheck();

        WebDriver owner = ConferenceFixture.getOwner();
        JavascriptExecutor js = ((JavascriptExecutor) owner);

        // read and inject helper script
        try
        {
            Path jsHelperPath = Paths.get(
                new File(PSNR_JS_SCRIPT).getAbsolutePath());
            String videoOperatorScript = new String(
                Files.readAllBytes(jsHelperPath));
            js.executeScript(videoOperatorScript);
        }
        catch (Exception e)
        {
            e.printStackTrace();

            assertTrue("Failed to inject JS helper.", false);
        }

        List<WebElement> remoteThumbs = owner
            .findElements(By.xpath("//video[starts-with(@id, 'remoteVideo_')]"));

        List<String> ids = new ArrayList<>();
        for (WebElement thumb : remoteThumbs)
        {
            ids.add(thumb.getAttribute("id"));
        }
        js.executeScript(
            "window._operator = new window.VideoOperator();" +
                "window._operator.recordAll(arguments[0]);",
            ids
        );

        String timeToRunInMin = System.getProperty("psnr.duration");

        // default is 1 minute
        if (timeToRunInMin == null || timeToRunInMin.length() == 0)
            timeToRunInMin = "1";

        final int minutesToRun = Integer.valueOf(timeToRunInMin);

        final CountDownLatch waitSignal = new CountDownLatch(1);

        // execute every 1 sec. This heartbeat task isn't necessary for the
        // PSNR testing but it can provide hints as to why the PSNR has failed.
        final Timer timer = new Timer();
        int millsToRun = minutesToRun * 60 * 1000;
        timer.schedule(new HeartbeatTask(timer, waitSignal, millsToRun, false),
            /* delay */ 1000, /* period */ 1000);

        try
        {
            waitSignal.await(minutesToRun, TimeUnit.MINUTES);
        }
        catch (InterruptedException e)
        {
        }

        if (waitSignal.getCount() == 0)
            assertTrue("A problem with the conf occurred", false);
        else
        {
            js.executeScript("window._operator.stop()");

            System.err.println(
                "REAL FPS: " +
                    js.executeScript("return window._operator.getRealFPS()")
            );
            System.err.println(
                "RAW DATA SIZE: " + js.executeScript(
                    "return window._operator.getRawDataSize() / 1024 / 1024") +
                    "MB"
            );

            // now close second participant to maximize performance
            ConferenceFixture.closeSecondParticipant();

            for (String id : ids)
            {
                Long framesCount = (Long) js.executeScript(
                    "return window._operator.getFramesCount(arguments[0])",
                    id
                );
                System.err.printf(
                    "frames count for %s: %s\n", id, framesCount);

                for (int i = 0; i < framesCount; i += 1)
                {
                    String frame = (String) js.executeScript(
                        "return window._operator.getFrame(arguments[0], arguments[1])",
                        id, i
                    );
                    // Convert it to binary
                    // Java 8 has a Base64 class.
                    byte[] data = org.apache.commons.codec.binary.
                        Base64.decodeBase64(frame);

                    String outputFrame
                        = OUTPUT_FRAME_DIR + id + "-" + i + ".png";

                    try
                    {
                        try (OutputStream stream = new FileOutputStream(
                            outputFrame))
                        {
                            stream.write(data);
                        }
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                        assertTrue("An error occurred", false);
                    }

                    Runtime rt = Runtime.getRuntime();
                    String[] commands = {
                        PSNR_SCRIPT, outputFrame,
                        INPUT_FRAME_DIR, RESIZED_FRAME_DIR
                    };

                    Process proc = null;
                    try
                    {
                        proc = rt.exec(commands);
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                        assertTrue("An error occurred", false);
                    }

                    BufferedReader stdInput = new BufferedReader(new
                        InputStreamReader(proc.getInputStream()));

                    BufferedReader stdError = new BufferedReader(new
                        InputStreamReader(proc.getErrorStream()));

                    // read the output from the command
                    String s = null;
                    try
                    {
                        while ((s = stdInput.readLine()) != null)
                        {
                            System.err.println(s);
                            assertTrue("Frame is bellow the PSNR threshold",
                                s == null
                                    || Float.parseFloat(s.split(" ")[1]) > MIN_PSNR);
                        }

                        // read any errors from the attempted command
                        while ((s = stdError.readLine()) != null)
                        {
                            System.err.println(s);
                        }
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }

                    try
                    {
                        assertTrue("The psnr-test.sh failed.",
                            proc.waitFor() == 0);
                    }
                    catch (InterruptedException e)
                    {

                    }

                    // If the test has passed for a specific frame, delete
                    // it to optimize disk space usage.
                    File outputFrameFile = new File(outputFrame);
                    outputFrameFile.delete();
                }
            }

            js.executeScript(
                "window._operator.cleanup();" +
                    "window._operator = null;"
            );
        }
    }
}
