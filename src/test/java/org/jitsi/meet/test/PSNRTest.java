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

import org.jitsi.meet.test.base.*;
import org.jitsi.meet.test.capture.*;
import org.jitsi.meet.test.tasks.*;
import org.jitsi.meet.test.util.*;
import org.jitsi.meet.test.web.*;

import org.json.simple.*;
import org.openqa.selenium.*;
import org.testng.annotations.*;
import static org.testng.Assert.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

import static org.jitsi.meet.test.util.TestUtils.*;

/**
 * A test that will run 1 minute (configurable) and will perform a PSNR test
 * on the received video stream.
 *
 * @author George Politis
 * @author Ivan Symchych
 */
public class PSNRTest
    extends WebTestBase
{
    /**
     * Defines the directory in which the psnr output file should be written
     */
    public static final String PSNR_OUTPUT_DIR_PROP = "psnr.output.dir";

    /**
     * Defines the filename to use when writing the psnr output (which will
     * be written to inside the {@link #PSNR_OUTPUT_DIR_PROP}
     * directory).
     */
    public static final String PSNR_OUTPUT_FILENAME_PROP =
        "psnr.output.filename";

    /**
     * The PSNR script that produces PSNR results for every frame that we've
     * captured.
     */
    private static final String PSNR_SCRIPT = "scripts/psnr-test.sh";

    /**
     * The directory where we save the captured frames.
     */
    private static final String OUTPUT_FRAME_DIR
        = "target/psnr/captured-frames/";

    /**
     * The directory where we get the input frames.
     */
    public static final String INPUT_FRAME_DIR= "resources/psnr/stamped/";

    /**
     * The video file to use as input.
     */
    public static final String INPUT_VIDEO_FILE
        = "resources/psnr/psnr-input.y4m";

    /**
     * The directory to use for frame resizing.
     */
    private static final String RESIZED_FRAME_DIR
        = "target/psnr/resized-frames/";

    /**
     * How long we should sample frames for psnr calculations
     */
    private static final String PSNR_DURATION_MILLIS_PROP = "psnr.duration_millis";

    /**
     * The minimum PSNR value that we will accept before failing. PSNR above 20
     * is pretty indicative of good similarity. For example: Downscaling a 720p
     * image to 360p gives a PSNR of 27.2299. Downscaling a 720p image to 180p
     * gives a PSNR of 21.8882. Downscaling a 720p image to 90p gives a PSNR of
     * 20.1337
     */
    private static final float MIN_PSNR = 22f;

    @Override
    public void setupClass()
    {
        super.setupClass();

        File inputFrameDir = new File(INPUT_FRAME_DIR);
        boolean inputFrameDirExists = inputFrameDir.exists();
        boolean inputVideoFileExists = new File(INPUT_VIDEO_FILE).exists();

        if (!inputFrameDirExists || !inputVideoFileExists)
        {
            // Fail the PSNR tests because we don't have any PSNR
            // resources, but out intention is to have them executed (since it's
            // being skipped by default).
            throw new RuntimeException(
                "Unable to run PSNR tests:"
                    + (inputFrameDirExists
                        ? "" : " INPUT_FRAME_DIR does not exist")
                    + (inputVideoFileExists
                        ? "" : " INPUT_VIDEO_FILE does not exist"));
        }

        WebParticipantOptions options
            = new WebParticipantOptions()
                .setFakeStreamVideoFile(INPUT_VIDEO_FILE);

        ensureTwoParticipants(
            null, null, options, options);
    }

    @Override
    public boolean skipTestByDefault()
    {
        return true;
    }

    /**
     * A test where we read some configurations or fallback to default values
     * and we expect a conference to be already established (by SetupConference)
     * and we keep checking whether this is still the case, and if something
     * is not working we fail.
     */
    @Test
    public void testPSNR()
        throws Exception
    {
        // Create the output directory for captured frames.
        File outputFrameDir = new File(OUTPUT_FRAME_DIR);
        if (!outputFrameDir.exists())
        {
            outputFrameDir.mkdirs();
        }

        // stop everything to maximize performance
        MuteTest muteTest = new MuteTest(this);
        muteTest.muteParticipant1AndCheck();
        muteTest.muteParticipant2AndCheck();
        MeetUIUtils.muteVideoAndCheck(getParticipant1(), getParticipant2());

        // Sleep 30 seconds to allow the clients to ramp up
        int rampUpDurationSeconds = 30;
        print("Waiting " + rampUpDurationSeconds +
            " seconds for ramp up");
        try
        {
            Thread.sleep(rampUpDurationSeconds * 1000);
        }
        catch (InterruptedException e)
        {}

        print("Wait for ramp up done");

        WebDriver driver1 = getParticipant1().getDriver();

        VideoOperator operator1 = new VideoOperator(getParticipant1());

        // read and inject helper script
        operator1.init();

        List<String> ids = MeetUIUtils.getRemoteVideoIDs(driver1);

        operator1.recordAll(ids);

        String timeToRunInMillisVal = System.getProperty(PSNR_DURATION_MILLIS_PROP);

        // default is 10 seconds (originally this was 1 minute, but the
        // longer duration seemed to affect stability, perhaps due to
        // memory issues in the browser)
        if (timeToRunInMillisVal == null || timeToRunInMillisVal.length() == 0)
        {
            timeToRunInMillisVal = "10000";
        }
        int timeToRunInMillis = Integer.valueOf(timeToRunInMillisVal);

        // execute every 1 sec. This heartbeat task isn't necessary for the
        // PSNR testing but it can provide hints as to why the PSNR has failed.

        HeartbeatTask heartbeatTask
            = new HeartbeatTask(
                getParticipant1(),
                getParticipant2(),
                timeToRunInMillis,
                false);

        heartbeatTask.start(/* delay */ 1000, /* period */ 1000);

        heartbeatTask.await(timeToRunInMillis, TimeUnit.MILLISECONDS);

        operator1.stopRecording();

        print("REAL FPS: " + operator1.getRealFPS());
        print("RAW DATA SIZE: " + operator1.getRawDataSize() + "MB");

        // now close second participant to maximize performance
        getParticipant2().hangUp();

        for (String id : ids)
        {
            Long framesCount = operator1.getFramesCount(id);
            print(String.format("frames count for %s: %s\n", id, framesCount));

            float totalPsnr = 0f;
            int numFrozenFrames = 0;
            int numSkippedFrames = 0;
            int prevFrameNumber = -1;
            for (int i = 0; i < framesCount; i += 1)
            {
                byte[] data = operator1.getFrame(id, i);

                String outputFrame = OUTPUT_FRAME_DIR + id + "-" + i + ".png";

                try (OutputStream stream = new FileOutputStream(outputFrame))
                {
                    stream.write(data);
                }

                Runtime rt = Runtime.getRuntime();
                String[] commands = {
                    PSNR_SCRIPT, outputFrame,
                    INPUT_FRAME_DIR, RESIZED_FRAME_DIR
                };

                Process proc = rt.exec(commands);

                BufferedReader stdInput
                    = new BufferedReader(
                            new InputStreamReader(proc.getInputStream()));

                BufferedReader stdError
                    = new BufferedReader(
                            new InputStreamReader(proc.getErrorStream()));

                // read the output from the command
                String s;
                try
                {
                    while ((s = stdInput.readLine()) != null)
                    {
                        print(s);
                        int frameNum = Integer.parseInt(s.split(" ")[0]);
                        float psnr = Float.parseFloat(s.split(" ")[1]);

                        assertTrue(
                            psnr > MIN_PSNR,
                            "Frame is bellow the PSNR threshold");

                        if (prevFrameNumber != -1 &&
                            frameNum == prevFrameNumber)
                        {
                            System.out.println("Current frame same as the last, incrementing frozen");
                            numFrozenFrames++;
                        }
                        else if (prevFrameNumber != -1 &&
                            frameNum != prevFrameNumber + 1)
                        {
                            if (frameNum < prevFrameNumber)
                            {
                                // roll-over case, frameNum should be '1', so
                                // calculate anything more than that as skipped
                                // frames
                                System.out.println(
                                    "roll over: previous frame was " +
                                    prevFrameNumber + "current frame is "
                                    + frameNum + ", skipped " +
                                    (frameNum - 1) + " frames");
                                numSkippedFrames += (frameNum - 1);
                            }
                            else
                            {
                                System.out.println(
                                    "previous frame was " + prevFrameNumber +
                                    "current frame is " + frameNum + ", skipped " +
                                    (frameNum - (prevFrameNumber + 1)) + " frames");
                                numSkippedFrames +=
                                    (frameNum - (prevFrameNumber + 1));
                            }
                        }
                        prevFrameNumber = frameNum;

                        System.out.println("frame number " + frameNum + " had psnr " + psnr);
                        totalPsnr += psnr;
                    }

                    // read any errors from the attempted command
                    while ((s = stdError.readLine()) != null)
                    {
                        print(s);
                    }
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }

                assertEquals(
                    proc.waitFor(),
                    0,
                    "The psnr-test.sh failed.");

                // If the test has passed for a specific frame, delete
                // it to optimize disk space usage.
                File outputFrameFile = new File(outputFrame);
                outputFrameFile.delete();
            }
            float averagePsnr = totalPsnr / framesCount;
            System.out.println("Average psnr: " + averagePsnr);
            System.out.println("Num frozen frames: " + numFrozenFrames);
            System.out.println("Frozen pct: " + numFrozenFrames / (float)framesCount);
            System.out.println("Num skipped frames: " + numSkippedFrames);
            System.out.println("Skipped pct: " + numSkippedFrames / (float)framesCount);

            JSONObject json = new JSONObject();
            json.put("totalFrames", framesCount);
            json.put("psnr", averagePsnr);
            json.put("numFrozenFrames", numFrozenFrames);
            json.put("numSkippedFrames", numSkippedFrames);

            String psnrOutputDir = System.getProperty(PSNR_OUTPUT_DIR_PROP);
            String psnrOutputFilename
                = System.getProperty(PSNR_OUTPUT_FILENAME_PROP);
            if (psnrOutputDir != null && !psnrOutputDir.isEmpty() &&
                psnrOutputFilename != null && !psnrOutputFilename.isEmpty())
            {
                PrintWriter writer = new PrintWriter(
                    Paths.get(psnrOutputDir, psnrOutputFilename).toString());
                writer.print(json.toString());
                writer.close();
            }
        }

        operator1.dispose();
    }
}
