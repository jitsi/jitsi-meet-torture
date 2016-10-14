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

import java.io.*;
import java.lang.reflect.*;
import java.util.*;

/**
 * The main test suite which will order tests.
 * @author Damian Minkov
 */
public class TestsRunner
{
    /**
     * The name of the property which controls the list of tests to be run.
     */
    private static final String TESTS_TO_RUN_PNAME = "jitsi-meet.tests.toRun";

    /**
     * The name of the property which controls the set of tests to be excluded
     * (i.e. not run).
     */
    private static final String TESTS_TO_EXCLUDE_PNAME
            = "jitsi-meet.tests.toExclude";

    /**
     * The default list of tests to run. This does not include SetupConference
     * and DisposeConference; they will be added separately.
     *
     * The list contains class names relative to this class' package.
     */
    private static final List<String> DEFAULT_TESTS_TO_RUN
            = new LinkedList<String>();

    static
    {
        DEFAULT_TESTS_TO_RUN.add(AvatarTest.class.getSimpleName());
        DEFAULT_TESTS_TO_RUN.add(MuteTest.class.getSimpleName());
        DEFAULT_TESTS_TO_RUN.add(StopVideoTest.class.getSimpleName());
        DEFAULT_TESTS_TO_RUN.add(SwitchVideoTest.class.getSimpleName());
        DEFAULT_TESTS_TO_RUN.add(EtherpadTest.class.getSimpleName());

        DEFAULT_TESTS_TO_RUN.add(ActiveSpeakerTest.class.getSimpleName());
        DEFAULT_TESTS_TO_RUN.add(StartMutedTest.class.getSimpleName());

        //DEFAULT_TESTS_TO_RUN.add(LastNTest.class.getSimpleName());

        DEFAULT_TESTS_TO_RUN.add(DisplayNameTest.class.getSimpleName());
        DEFAULT_TESTS_TO_RUN.add(DataChannelTest.class.getSimpleName());
        DEFAULT_TESTS_TO_RUN.add(ContactListTest.class.getSimpleName());
        DEFAULT_TESTS_TO_RUN.add(VideoLayoutTest.class.getSimpleName());
        DEFAULT_TESTS_TO_RUN.add(FollowMeTest.class.getSimpleName());
        //DEFAULT_TESTS_TO_RUN.add(
        //    DesktopSharingImitationTest.class.getSimpleName());
        DEFAULT_TESTS_TO_RUN.add(DesktopSharingTest.class.getSimpleName());
        if(Boolean.getBoolean(RestTests.ENABLE_REST_API_TESTS))
            DEFAULT_TESTS_TO_RUN.add(RestTests.class.getSimpleName());

        DEFAULT_TESTS_TO_RUN.add(UDPTest.class.getSimpleName());
        DEFAULT_TESTS_TO_RUN.add(SinglePortTest.class.getSimpleName());
        DEFAULT_TESTS_TO_RUN.add(TCPTest.class.getSimpleName());

        DEFAULT_TESTS_TO_RUN.add(SharedVideoTest.class.getSimpleName());

        DEFAULT_TESTS_TO_RUN.add(LockRoomTest.class.getSimpleName());

        // Disabling this test by default, it will run only on linux currently
        // it needs sudo for accessing iptables and if user run it, it will
        // clear all iptable rules at the end of the run
        //DEFAULT_TESTS_TO_RUN.add(
        //    PeerConnectionStatusTest.class.getSimpleName());

//        DEFAULT_TESTS_TO_RUN.add(MaxUsersTest.class.getSimpleName());
        DEFAULT_TESTS_TO_RUN.add(ConnectionTimeTest.class.getSimpleName());

        File inputFrameDir = new File(PSNRTest.INPUT_FRAME_DIR);
        String fakeStreamVideoFileName
            = System.getProperty(ConferenceFixture.FAKE_VIDEO_FNAME_PROP);
        File fakeStreamVideoFile = new File(fakeStreamVideoFileName);

        if (inputFrameDir.exists() && fakeStreamVideoFile.exists())
        {
            DEFAULT_TESTS_TO_RUN.add(PSNRTest.class.getSimpleName());
        }

        // make sure LipSyncTest tests are the last to run as they will stop
        // and start the browsers one more time and will change the video file
        DEFAULT_TESTS_TO_RUN.add(LipSyncTest.class.getSimpleName());
        
        DEFAULT_TESTS_TO_RUN.add(ReloadTest.class.getSimpleName());

        DEFAULT_TESTS_TO_RUN.add(EndConferenceTest.class.getSimpleName());
    }

    /**
     * Gets a list of (the simple class names of) the {@code TestCase}s to run.
     * The returned value includes the {@code TestCase}s specified by the
     * property {@link #TESTS_TO_RUN_PNAME} (defaults to
     * {@link #DEFAULT_TESTS_TO_RUN} and excludes the {@code TestCase}s
     * specified by the property {@link #TESTS_TO_EXCLUDE_PNAME} (if a value is
     * assigned to the respective property).
     *
     * @return a list of (the simple class names of) the {@code TestCase}s to
     * run
     */
    private static List<String> getTestsToRun()
    {
        List<String> testsToRun = new LinkedList<String>();

        // Add the tests which are to run.
        String testsToRunString = System.getProperty(TESTS_TO_RUN_PNAME);

        if (testsToRunString == null
                || testsToRunString.equalsIgnoreCase("all"))
        {
            testsToRun.addAll(DEFAULT_TESTS_TO_RUN);
        }
        else
        {
            StringTokenizer tokens = new StringTokenizer(testsToRunString, ",");

            while (tokens.hasMoreTokens())
            {
                testsToRun.add(tokens.nextToken());
            }
        }

        // Remove the tests which are to be excluded i.e. to not run.
        String testsToExclude = System.getProperty(TESTS_TO_EXCLUDE_PNAME);

        if (testsToExclude != null)
        {
            StringTokenizer tokens = new StringTokenizer(testsToExclude, ",");

            while (tokens.hasMoreTokens())
            {
                String test = tokens.nextToken();
                while (testsToRun.remove(test));
            }
        }

        // SetupConference and DisposeConference must always be run exactly
        // once in the beginning and end of the tests. They could potentially be
        // moved to setUp() and tearDown().
        while (testsToRun.remove(SetupConference.class.getSimpleName()));
        while (testsToRun.remove(DisposeConference.class.getSimpleName()));
        testsToRun.add(0, SetupConference.class.getSimpleName());
        testsToRun.add(DisposeConference.class.getSimpleName());

        return testsToRun;
    }

    /**
     * Sets the audio file to be streamed through a fake audio device by the
     * conference participants.
     */
    private static void setFakeAudioStreamFile()
    {
        String fakeStreamAudioFile
            = System.getProperty(ConferenceFixture.FAKE_AUDIO_FNAME_PROP);

        if (fakeStreamAudioFile == null)
        {
            fakeStreamAudioFile
                = new File("resources/fakeAudioStream.wav").getAbsolutePath();
        }

        ConferenceFixture.setFakeStreamAudioFile(fakeStreamAudioFile);
    }

    /**
     * Sets the video file to be streamed through a fake video device by the
     * conference participants.
     */
    private static void setFakeVideoStreamFile()
    {
        String fakeStreamVideoFile
            = System.getProperty(ConferenceFixture.FAKE_VIDEO_FNAME_PROP);

        if (fakeStreamVideoFile != null
            && fakeStreamVideoFile.trim().length() > 0)
        {
            ConferenceFixture.setFakeStreamVideoFile(
                fakeStreamVideoFile.trim());
        }
    }

    /**
     * Gets (a suite of) the tests to run. If the property
     * {@code jitsi-meet.tests.toRun} exists, we use its value to add only the
     * tests it mentions.
     *
     * @return (a suite of) the tests to run
     */
    public static TestSuite suite()
    {
        List<String> testsToRun = getTestsToRun();
        System.err.println("Will run the following tests: " + testsToRun);
        TestSuite suite = suite(testsToRun);

        setFakeAudioStreamFile();
        setFakeVideoStreamFile();

        return suite;
    }

    /**
     * Initializes a new {@code TestSuite} instance from a specific sequence of
     * test cases.
     *
     * @param testsToRun the simple class names of the {@code TestCase}s which
     * comprise the new {@code TestSuite} instance
     * @return a new {@code TestSuite} instance which consists of the specified
     * {@code testsToRun}
     */
    private static TestSuite suite(List<String> testsToRun)
    {
        String packageName = TestsRunner.class.getPackage().getName();
        TestSuite suite = new TestSuite();

        for (String testName : testsToRun)
        {
            // class names are relative to this package
            String className = packageName + "." + testName;

            try
            {
                Class<? extends TestCase> cl
                    = Class.forName(className).asSubclass(TestCase.class);

                Method suiteMethod = null;
                for(Method m : cl.getDeclaredMethods())
                {
                    if(m.getName().equals("suite"))
                    {
                        suiteMethod = m;
                        break;
                    }
                }

                // if suite method exists use it
                if(suiteMethod != null)
                {
                    Object test = suiteMethod.invoke(null);
                    suite.addTest((Test)test);
                }
                else
                {
                    // otherwise just use the class
                    suite.addTestSuite(cl);
                }
            }
            catch(Throwable t)
            {
                System.err.println("Not running " + testName);
                t.printStackTrace();
            }
        }

        return suite;
    }
}
