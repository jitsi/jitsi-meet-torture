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

import org.jitsi.meet.test.capture.*;
import org.jitsi.meet.test.tasks.*;
import org.jitsi.meet.test.util.*;
import org.jitsi.meet.test.web.*;

import org.openqa.selenium.*;
import org.testng.annotations.*;

import java.io.*;
import java.text.*;
import java.util.*;
import java.util.concurrent.*;

import static org.testng.Assert.*;
import static org.jitsi.meet.test.util.TestUtils.*;

/**
 * A test for WebRTC audio and video synchronisation feature. Works with Chrome
 * only.
 *
 * A video is being streamed from fake devices which is composed of red
 * background and small green dot appearing at the center when a beep sound is
 * played. Then the times for the green dot the beep sound are recorded from
 * both sender's and receiver's perspective.
 *
 * The recording happens at 20 fps which corresponds to a frame stored every
 * 50 ms. With current recording method there is around 1-2 frames delay between
 * audio sent and audio received times. This is probably, because the beep
 * detection is done only based on the audio level and we're doing that
 * from WebRTC remote stream stats which may appear with slight delay. The video
 * is captured with Canvas and seems to be pretty accurate(0 frame delay).
 * The green dot is detected by verifying if the pixel at the center of
 * the video has green value of at least 200{@link #rgbaToColorStr(List)}.
 * The beep sound is detected when the audio level is greater than 0.2
 * {@link #isBeep(Double)}.
 *
 * Once the data has been collected the differences between the times when
 * audio/video was sent to when it was received are being calculated. In normal
 * case when there is no special delay, there should be around 1-2 frames delay
 * for audio and 0 for video(for the reason explained above). For the tests
 * purpose there is a packet delay added for the audio which will increase it to
 * around 8-9 frames. After the lip-sync is enabled and given that it works fine,
 * the video delay should be slowly increasing over time to reach around 7 after
 * 15 seconds. The test results are based on the ration of average audio frame
 * delay and the average of the last 5 video delays. For 7/9 it is more than 0.8
 * and the tests will fail when the value is lower than 0.5.
 *
 * @author Pawel Domas
 */
public class LipSyncTest
    extends WebTestBase
{
    /**
     * Minimum delay required to be reported for the audio beeps. By verifying
     * that we make sure that delaying audio packets has been enabled on
     * the bridge. The test will fail if the value is lower than this.
     */
    private static final int MIN_BEEP_DELAY = 4;

    /**
     * Minimal video to audio delay ratio we expect to see from the last few
     * samples in order to pass the test.
     */
    private static final double MIN_SYNC_RATIO = 0.5;

    private boolean debug;

    @Override
    public void setupClass()
    {
        super.setupClass();
    }

    /**
     * Runs the lip-sync test. See {@link LipSyncTest} class description for
     * more info on how the test works.
     */
    @Test
    public void testLipSync()
        throws IOException
    {
        debug = System.getProperty("lipsync.debug") != null;

        // We need special config to be passed and audio
        // video streams reset, because the audio does not loop in Chrome and we
        // don't want to end up with silence being streamed

        // this file is required in order to run this test
        WebParticipantOptions options
            = new WebParticipantOptions()
                .setFakeStreamVideoFile("resources/fakeVideoStream.y4m")
                .setFakeStreamAudioFile("resources/fakeAudioStream-lipsync.wav");

        // Start participant1 with lip-sync enabled, audio packet delay and
        // shorter audio levels interval
        ensureTwoParticipants(
            getJitsiMeetUrl().appendConfig(
                "config.enableLipSync=true&config.audioPacketDelay=15" +
                    "&config.audioLevelsInterval=100"),
            getJitsiMeetUrl().appendConfig(
                "config.disableSuspendVideo=true"),
            options, options);

        WebParticipant participant1 = getParticipant1();
        WebParticipant participant2 = getParticipant2();
        // FIXME do not operate on the drivers directly
        WebDriver driver1 = participant1.getDriver();
        WebDriver driver2 = participant2.getDriver();

        // Wait for the conference to start
        participant1.waitForIceConnected();

        // Stops audio and video on participant1 to improve performance
        new MuteTest(this).muteParticipant1AndCheck();
        MeetUIUtils.muteVideoAndCheck(getParticipant1(), getParticipant2());

        // Read and inject helper script
        VideoOperator operator1 = new VideoOperator(participant1);
        operator1.init();

        VideoOperator operator2 = new VideoOperator(participant2);
        operator2.init();

        int fps = 20; // frame every 50ms

        // Record remote video and audio levels from the perspective of
        // participant1
        List<String> participant1Ids = MeetUIUtils.getRemoteVideoIDs(driver1);
        List<String> participant1EndpointIds = new ArrayList<>();
        participant1EndpointIds.add(participant2.getEndpointId());

        operator1.recordAll(
            participant1Ids, fps, participant1EndpointIds);

        // Record local audio and video from participant2's perspective
        List<String> participant2Ids = new ArrayList<>();
        participant2Ids.add(MeetUIUtils.getLocalVideoID(driver2));

        operator2.recordAll(
            participant2Ids, fps, participant1EndpointIds);

        String timeToRunInSeconds = System.getProperty("lipsync.duration");

        // default is 30 seconds
        if (timeToRunInSeconds == null || timeToRunInSeconds.length() == 0)
        {
            timeToRunInSeconds = "30";
        }

        final int seconds = Integer.valueOf(timeToRunInSeconds);
        int millsToRun = seconds * 1000;

        HeartbeatTask heartbeatTask
            = new HeartbeatTask(
                participant1,
                participant2,
                millsToRun,
                false);

        heartbeatTask.start(/* delay */ 1000, /* period */ 1000);

        // await will fail if the conference dies before
        // specified "seconds" elapse
        heartbeatTask.await(seconds, TimeUnit.SECONDS);

        // Stop recorders
        operator1.stopRecording();
        operator2.stopRecording();

        // Retrieve recorder data
        Series participant1Series
            = new Series(operator1, participant1Ids.get(0));
        Series participant2Series
            = new Series(operator2, participant2Ids.get(0));

        // Compare sent to received timestamps
        SeriesComparison comparison
            = new SeriesComparison(participant2Series, participant1Series);
        comparison.process();

        // Debug dump to files
        if (System.getProperty("lipsync.debug") != null)
        {
            print("Printing from the perspective of participant1:");
            dumpLipSyncInfo("participant1.log", participant1Series);

            print("Printing from the perspective of participant2:");
            dumpLipSyncInfo("participant2.log", participant2Series);
        }

        print("BEEP diffs: " + comparison.beepDifferences);
        print("GREEN diffs: " + comparison.greenDifferences);

        double beepAvg = comparison.getBeepDiffAvg();

        assertTrue(
            beepAvg > MIN_BEEP_DELAY,
            "The audio does not seem to be delayed enough, beepAvg: "
                + beepAvg);

        double lastGreenAvg = comparison.getLastFewGreenAvg(5);

        print(
            "Beep delay avg: " + beepAvg + " last green avg: " + lastGreenAvg);

        double syncRatio = lastGreenAvg / beepAvg;
        print("A/V sync ratio: " + syncRatio);

        operator1.dispose();
        operator2.dispose();

        assertTrue(
            syncRatio > MIN_SYNC_RATIO,
            "A/V sync ratio is too low: " + syncRatio);
    }

    private static void dumpLipSyncInfo(String fileName, Series series)
        throws IOException
    {
        try (FileWriter fileWriter = new FileWriter(fileName))
        {
            int framesCount = series.getLength();
            fileWriter.write(
                    String.format(
                            "frames count for %s: %s\n", series.getVideoId(),
                            framesCount));

            fileWriter.write("Beep starts: " + series.getBeepStarts() + "\n");
            fileWriter.write("Green starts: " + series.getGreenStarts() + "\n");

            for (int i = 0; i < framesCount; i += 1)
            {
                Date date = series.getTs(i);
                DateFormat df = new SimpleDateFormat("HH:mm.ss.SSS");
                boolean beep = series.getBeep(i);
                String color = series.getColor(i);
                fileWriter.write(
                        String.format(
                                "%s color: %s beep: %s\n",
                                df.format(date), color, beep));
            }
        }
    }

    /**
     * Returns <tt>true</tt> if given audio level is considered a beep sounds.
     *
     * @param aLvl a <tt>Double</tt> from 0.0 to 1.0 which represents the output
     * audio level.
     */
    static private boolean isBeep(Double aLvl)
    {
        return aLvl > 0.2d;
    }

    /**
     * Returns string representation of given RGBA array.
     * @param rgba an array with 0-255 values of red, green, blue and alpha
     * color channels.
     * @return "RED", "GREEN" or "R={red},G={green},B={blue},A={alpha}"
     */
    static private String rgbaToColorStr(List<Long> rgba)
    {
        if (rgba.get(0) > 200)
        {
            return  "RED";
        }
        else if (rgba.get(1) > 200)
        {
            return  "GREEN";
        }
        else
        {
            return String.format(
                    "R=%s,G=%s,B=%s,A=%s",
                    rgba.get(0), rgba.get(1), rgba.get(2), rgba.get(3));
        }
    }

    /**
     * Structure that stores the info about captured frame.
     */
    private class CapturedFrame
    {
        final Date timestamp;

        final boolean beep;

        final String color;

        CapturedFrame(Date timestamp, boolean beep, List<Long> pixelRgba)
        {
            Objects.requireNonNull(timestamp, "timestamp");
            Objects.requireNonNull(pixelRgba, "pixelRgba");

            this.timestamp = timestamp;
            this.beep = beep;
            this.color = rgbaToColorStr(pixelRgba);
        }

        boolean isGreen()
        {
            return "GREEN".equals(this.color);
        }
    }

    /**
     * A series of captured frames.
     */
    private class Series
    {
        private final String videoId;

        private final CapturedFrame[] frames;

        private ArrayList<Integer> beepStarts = new ArrayList<>();

        private ArrayList<Integer> greenStarts = new ArrayList<>();

        Series(VideoOperator operator, String videoID)
        {
            this.videoId = videoID;

            Long countLong = operator.getFramesCount(videoID);
            int frameCount = Integer.parseInt(String.valueOf(countLong));

            this.frames = new CapturedFrame[frameCount];

            for (int i = 0; i < frameCount; i += 1)
            {
                //Double audioLevel = operator.getAudioLevel(id, i);
                Long timestamp = operator.getTimestamp(videoID, i);
                Date date = new Date(timestamp);
                boolean beep = isBeep(operator.getAudioLevel(videoID, i));
                List<Long> pixelRgba = operator.getRGBAatTheCenter(videoID, i);

                frames[i] = new CapturedFrame(date, beep, pixelRgba);
            }
        }

        int getLength()
        {
            return frames.length;
        }

        Date getTs(int idx)
        {
            return frames[idx].timestamp;
        }

        boolean getBeep(int idx)
        {
            return frames[idx].beep;
        }

        String getColor(int idx)
        {
            return frames[idx].color;
        }

        void process()
        {
            // Find when beep starts for the first time
            int beepIdx = 0;
            do
            {
                beepIdx = findBeep(beepIdx);
                if (beepIdx != -1)
                {
                    beepStarts.add(beepIdx);
                }
            } while (beepIdx > 0);

            int greenIdx = 0;
            do
            {
                greenIdx = findGreen(greenIdx);
                if (greenIdx != -1)
                {
                    greenStarts.add(greenIdx);
                }
            } while (greenIdx > 0);
        }

        /**
         * Goes over given array and returns an index of the element for which
         * given <tt>{@link Comparator}</tt> returns value greater than 0 when
         * compared with the previous element.
         *
         * @param series an array of <tt>T</tt>
         * @param condition a <tt>Comparator</tt> which detects the signal.
         * @param startIdx starting array index.
         * @param <T> the type of the array elements.
         *
         * @return an index of the element for which given
         * <tt>{@link Comparator}</tt> returns value greater than 0 when
         * compared with the previous element or <tt>-1</tt> when it never
         * happens.
         */
        private <T> int findSignalIdx(T[] series,
                                      Comparator<T> condition, int startIdx)
        {
            for (int i = startIdx + 1; i + 1 < series.length; i++)
            {
                if (condition.compare(series[i-1], series[i]) > 0)
                {
                    return i;
                }
            }
            return -1;
        }

        int findBeep(int startIdx)
        {
            return findSignalIdx(
                frames,
                (o1, o2) -> (!o1.beep && o2.beep) ? 1 : 0,
                startIdx);
        }

        int findGreen(int startIdx)
        {
            return findSignalIdx(
                frames,
                (e0, e1) -> (!e0.isGreen() && e1.isGreen()) ? 1 : 0,
                startIdx);
        }

        String getVideoId()
        {
            return videoId;
        }

        List<Integer> getBeepStarts()
        {
            return beepStarts;
        }

        ArrayList<Integer> getGreenStarts()
        {
            return greenStarts;
        }
    }

    /**
     * A comparison of the two <tt>{@link Series}</tt> for the LipSyncTest
     * purpose. Finds the differences between the times when green dot/beep
     * sound have been sent and received.
     */
    private class SeriesComparison
    {
        private final Series senderSeries;

        private final Series receiverSeries;

        private List<Integer> beepDifferences;

        private List<Integer> greenDifferences;

        SeriesComparison(Series senderSeries, Series receiverSeries)
        {
            this.senderSeries = senderSeries;
            this.receiverSeries = receiverSeries;
        }

        void process()
        {
            senderSeries.process();
            receiverSeries.process();

            this.beepDifferences
                = calcDifferences(
                        receiverSeries.beepStarts, senderSeries.beepStarts);

            this.greenDifferences
                = calcDifferences(
                        receiverSeries.greenStarts, senderSeries.greenStarts);
        }

        private List<Integer> calcDifferences(ArrayList<Integer> receiverTimes,
                                              ArrayList<Integer> senderTimes)
        {
            List<Integer> diffs = new ArrayList<>();

            // We may have to remove first entry of the receiver side if
            // we have missed first rising edge on the sender side.
            if (receiverTimes.size() > 0 && senderTimes.size() > 0)
            {
                if (senderTimes.get(0) > receiverTimes.get(0))
                {
                    // Remove first element
                    Integer removed = receiverTimes.remove(0);
                    if (debug)
                    {
                        print("ADJUSTING! removed: " + removed);
                    }
                }
            }

            for (int i =0; i < receiverTimes.size() && i < senderTimes.size();
                    i++)
            {
                diffs.add(receiverTimes.get(i) - senderTimes.get(i));
            }
            return diffs;
        }

        /**
         * Returns an average of the differences between frame times when
         * the beep sound was sent and received.
         */
        double getBeepDiffAvg()
        {
            double avg = 0;
            for (int diff : beepDifferences)
            {
                avg += diff;
            }
            return avg / beepDifferences.size();
        }

        /**
         * Returns an average of the last few differences between frame times
         * when the green dot was sent and received.
         */
        double getLastFewGreenAvg(int lastCount)
        {
            double avg = 0;
            for (int i = greenDifferences.size() - lastCount;
                    i < greenDifferences.size(); i++)
            {
                avg += greenDifferences.get(i);
            }
            return avg / lastCount;
        }
    }
}
