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
     * The tests that are currently started.
     * If the property jitsi-meet.tests.toRun exists we use its values to
     * add only the tests mentioned in it.
     * @return tests that are currently started.
     */
    public static TestSuite suite()
    {
        TestSuite suite = new TestSuite();

        String testsToRun = System.getProperty("jitsi-meet.tests.toRun");

        if(testsToRun == null || testsToRun.equalsIgnoreCase("all"))
        {
            suite.addTest(SetupConference.suite());

            suite.addTest(AvatarTest.suite());
            suite.addTest(MuteTest.suite());
            suite.addTest(StopVideoTest.suite());
            suite.addTest(SwitchVideoTests.suite());
            suite.addTest(EtherpadTests.suite());
            suite.addTest(LockRoomTest.suite());
            // doing the same test two more times to be sure it is
            // not a problem, as there was reported an issue about that
            // https://github.com/jitsi/jitsi-meet/issues/83
            suite.addTest(LockRoomTest.suite());
            suite.addTest(LockRoomTest.suite());

            suite.addTest(TCPTest.suite());

            suite.addTest(ActiveSpeakerTest.suite());
            suite.addTest(StartMutedTest.suite());
            suite.addTestSuite(DisplayNameTest.class);

            suite.addTestSuite(DisposeConference.class);
        }
        else
        {
            // it is a comma separated list of class names
            StringTokenizer tokens = new StringTokenizer(testsToRun, ",");
            while(tokens.hasMoreTokens())
            {
                String testName = tokens.nextToken();
                // class names are relative to this package
                String className = "org.jitsi.meet.test." + testName;

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
                    t.printStackTrace();
                }
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
