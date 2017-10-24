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

import org.jitsi.meet.test.capture.*;
import org.jitsi.meet.test.tasks.*;
import org.jitsi.meet.test.util.*;

import org.openqa.selenium.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

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

    /**
     * A test where we read some configurations or fallback to default values
     * and we expect a conference to be already established (by SetupConference)
     * and we keep checking whether this is still the case, and if something
     * is not working we fail.
     */
    public void testPSNR()
        throws Exception
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

        VideoOperator ownerVideoOperator = new VideoOperator(owner);

        // read and inject helper script
        ownerVideoOperator.init();

        List<String> ids = MeetUIUtils.getRemoteVideoIDs(owner);

        ownerVideoOperator.recordAll(ids);

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

        HeartbeatTask heartbeatTask = new HeartbeatTask(timeToRunInMillis, false);

        heartbeatTask.start(/* delay */ 1000, /* period */ 1000);

        heartbeatTask.await(timeToRunInMillis, TimeUnit.MILLISECONDS);

        ownerVideoOperator.stopRecording();

        System.err.println("REAL FPS: " + ownerVideoOperator.getRealFPS());
        System.err.println(
                "RAW DATA SIZE: " + ownerVideoOperator.getRawDataSize() + "MB");

        // now close second participant to maximize performance
        ConferenceFixture.closeSecondParticipant();

        for (String id : ids)
        {
            Long framesCount = ownerVideoOperator.getFramesCount(id);
            System.err.printf("frames count for %s: %s\n", id, framesCount);

            float totalPsnr = 0f;
            for (int i = 0; i < framesCount; i += 1)
            {
                byte[] data = ownerVideoOperator.getFrame(id, i);

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
                        System.err.println(s);
                        float psnr = Float.parseFloat(s.split(" ")[1]);
                        assertTrue("Frame is bellow the PSNR threshold",
                                psnr > MIN_PSNR);
                        totalPsnr += psnr;
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

                assertTrue("The psnr-test.sh failed.", proc.waitFor() == 0);

                // If the test has passed for a specific frame, delete
                // it to optimize disk space usage.
                File outputFrameFile = new File(outputFrame);
                outputFrameFile.delete();
            }
            float averagePsnr = totalPsnr / framesCount;
            System.out.println("Average psnr: " + averagePsnr);
            String psnrOutputDir =
                System.getProperty(ConferenceFixture.PSNR_OUTPUT_DIR_PROP);
            String psnrOutputFilename =
                System.getProperty(ConferenceFixture.PSNR_OUTPUT_FILENAME_PROP);
            if (psnrOutputDir != null && !psnrOutputDir.isEmpty() &&
                psnrOutputFilename != null && !psnrOutputFilename.isEmpty())
            {
                PrintWriter writer = new PrintWriter(
                    Paths.get(psnrOutputDir, psnrOutputFilename).toString());
                writer.print(Float.toString(averagePsnr));
                writer.close();
            }
        }

        ownerVideoOperator.dispose();
    }
}
