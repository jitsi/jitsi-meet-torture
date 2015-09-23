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
import org.junit.runner.*;
import org.junit.runners.*;

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
     * The name of the SetupConference class.
     */
    private static final String SETUP_CONFERENCE_NAME = "SetupConference";

    /**
     * The name of the DisposeConference class.
     */
    private static final String DISPOSE_CONFERENCE_NAME = "DisposeConference";

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
        DEFAULT_TESTS_TO_RUN.add("AvatarTest");
        DEFAULT_TESTS_TO_RUN.add("MuteTest");
        DEFAULT_TESTS_TO_RUN.add("StopVideoTest");
        DEFAULT_TESTS_TO_RUN.add("SwitchVideoTests");
        DEFAULT_TESTS_TO_RUN.add("EtherpadTests");
        DEFAULT_TESTS_TO_RUN.add("LockRoomTest");
        // doing the same test two more times to be sure it is
        // not a problem, as there was reported an issue about that
        // https://github.com/jitsi/jitsi-meet/issues/83
        DEFAULT_TESTS_TO_RUN.add("LockRoomTest");
        DEFAULT_TESTS_TO_RUN.add("LockRoomTest");

        DEFAULT_TESTS_TO_RUN.add("UDPTest");
        DEFAULT_TESTS_TO_RUN.add("TCPTest");

        DEFAULT_TESTS_TO_RUN.add("ActiveSpeakerTest");

        DEFAULT_TESTS_TO_RUN.add("StartMutedTest");

        DEFAULT_TESTS_TO_RUN.add("DisplayNameTest");
    }

    /**
     * The tests that are currently started.
     * If the property jitsi-meet.tests.toRun exists we use its values to
     * add only the tests mentioned in it.
     * @return tests that are currently started.
     */
    public static TestSuite suite()
    {
        TestSuite suite = new TestSuite();

        List<String> testsToRun = new LinkedList<String>();

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

        String testsToExclude = System.getProperty(TESTS_TO_EXCLUDE_PNAME);
        if (testsToExclude != null)
        {
            StringTokenizer tokens = new StringTokenizer(testsToExclude, ",");
            while (tokens.hasMoreTokens())
            {
                String token = tokens.nextToken();
                while (testsToRun.remove(token))
                    ;
            }
        }

        // SetupConference and DisposeConference must always be run exactly
        // once in the beginning and end of the tests.
        // They could potentially be moved to setUp() and tearDown().
        while(testsToRun.remove(SETUP_CONFERENCE_NAME))
            ;
        while(testsToRun.remove(DISPOSE_CONFERENCE_NAME))
            ;
        testsToRun.add(0, SETUP_CONFERENCE_NAME);
        testsToRun.add(DISPOSE_CONFERENCE_NAME);


        for (String testName : testsToRun)
        {
            // class names are relative to this package
            String className = TestsRunner.class.getPackage().getName()
                + "." + testName;

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

        String fakeStreamAudioFile
            = System.getProperty(ConferenceFixture.FAKE_AUDIO_FNAME_PROP);

        if (fakeStreamAudioFile == null)
        {
            File file = new File("resources/fakeAudioStream.wav");
            fakeStreamAudioFile = file.getAbsolutePath();
        }

        ConferenceFixture.setFakeStreamAudioFile(fakeStreamAudioFile);

        return suite;
    }
}
