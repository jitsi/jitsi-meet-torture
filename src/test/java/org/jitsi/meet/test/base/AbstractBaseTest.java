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
package org.jitsi.meet.test.base;

import com.bipmeet.test.util.BMUtils;
import org.testng.*;
import org.testng.annotations.*;

import java.lang.reflect.*;
import java.util.*;
import java.util.stream.*;

import static org.jitsi.meet.test.util.TestUtils.*;

/**
 * The base of all tests, checks whether the tests is supposed to be executed
 * or skipped by checking the properties which tests to run, exclude or include.
 *
 * @param <P></P> the participant's type which can be used in the test's class.
 */
@Listeners(FailureListener.class)
public abstract class AbstractBaseTest<P extends Participant>
{
    /**
     * The name of the property which controls the list of tests to be run.
     */
    private static final String TESTS_TO_RUN_PNAME
        = "jitsi-meet.tests.toRun";

    /**
     * List of tests to run, if empty we assume we run all except those that
     * we explicitly ignore.
     */
    private static List<String> testsToRun = null;

    /**
     * The name of the property which controls the set of tests to be excluded
     * (i.e. not run).
     */
    private static final String TESTS_TO_EXCLUDE_PNAME
        = "jitsi-meet.tests.toExclude";

    /**
     * List of tests to skip in any case.
     */
    private static List<String> testsToExclude = null;

    /**
     * The name of the property which controls the set of tests to be include
     * to the default set of tests.
     */
    private static final String TESTS_TO_INCLUDE_PNAME
        = "jitsi-meet.tests.toInclude";

    /**
     * List of tests to explicitly include to the default set, ignoring
     * their {@link AbstractBaseTest#skipTestByDefault()}.
     */
    private static List<String> testsToInclude = null;

    /**
     * The {@link Properties} instance which holds all configuration properties
     * used by this test and it's sub-components.
     */
    private Properties config;

    /**
     * The current room name used.
     */
    protected String currentRoomName;

    /**
     * The participants pool created/used by this test instance.
     */
    protected ParticipantHelper<P> participants;

    /**
     * Set to <tt>true</tt> or <tt>false</tt>, after
     * {@link #checkForSkip(Properties)} gets executed.
     */
    private Boolean skipped;

    /**
     * Default.
     */
    protected AbstractBaseTest()
    {
        currentRoomName
            = "torture" + String.valueOf((int)(Math.random()*1000000));
        participants = null;
    }

    /**
     * Constructs new AbstractBaseTest with predefined baseTest, to
     * get its participants and room name.
     *
     * @param baseTest the parent test.
     *
     * @deprecated Some test classes are instantiated in order to have their
     * test methods reused which seems like a weird practice. It's weird,
     * because a test has specific lifecycle and cloning a test can lead to
     * unexpected side effects plus makes maintenance more complex, because
     * cloning capability of the abstract test needs to be preserved in order to
     * not break those tests.
     * In most of the tests which use copy constructor a goal is to not
     * duplicate procedures and assertions which could be achieved by providing
     * dedicated page objects or methods in {@link Participant} / test / utility
     * classes.
     */
    protected AbstractBaseTest(AbstractBaseTest<P> baseTest)
    {
        currentRoomName = baseTest.currentRoomName;
        participants = baseTest.participants;
    }

    /**
     * Create {@link ParticipantHelper} which will be used by the test to
     * create and store participants.
     *
     * @param config the config which will be the source of all properties
     * required to create {@link Participant}s.
     *
     * @return a new helper initialized with the given config.
     */
    protected abstract ParticipantHelper<P> createParticipantHelper(
            Properties config);

    /**
     * Convenience method for getting a string property from the configuration.
     *
     * @param propertyKey - The name of the config property to be read.
     *
     * @return a {@code String} value or <tt>null</tt> if property does not
     * exist.
     */
    protected String getStringConfigValue(String propertyKey)
    {
        return config.getProperty(propertyKey);
    }

    /**
     * NOTE: We don't want this method to be overridden in subclasses, because
     * it contains TestNG specific ITestContext. Use {@link #setupClass()} to do
     * any per class setup.
     * @param context - The TestNG test context.
     */
    @BeforeClass
    final public void setupClassPrivate(ITestContext context)
    {
        this.config = TestSettings.initSettings();

        config = mergeTestNGSuiteProperties(config, context);

        // See if this test should be skipped.
        this.skipped = checkForSkip(config);

        String className = this.getClass().getSimpleName();

        if (skipped)
        {
            throw new SkipException("skips-" + className);
        }

        print(
            "---=== Testing " + className + " ===---");

        participants = createParticipantHelper(config);

        currentRoomName = BMUtils.getRoomName(currentRoomName, config);

        participants.initialize();

        // if this one fails, the failure will be registered in the
        // FailureListener to gather information and it will call
        // the cleanupClass() to clean up any leftovers
        // FIXME if setupClass throws a SkipException a browser instance
        //  is left behind if created in setup and @AfterClass is never called
        setupClass();
    }

    /**
     * Merge torture test config with the TestNG suite parameters. TestNG
     * parameters can be declared in <parameter> tags in testng.xml file which
     * describes the test Suite.
     *
     * @param config - The torture tests config initialized by
     * {@link TestSettings}.
     * @param context - The TestNG {@link ITestContext} instance for the suite
     * currently being executed.
     *
     * @return the merged properties set.
     */
    private Properties mergeTestNGSuiteProperties(
            Properties config, ITestContext context)
    {
        Map<String, String> params
            = context.getSuite().getXmlSuite().getAllParameters();

        Properties output = new Properties();

        output.putAll(config);

        params.forEach(output::setProperty);

        return output;
    }

    /**
     * Returns all {@link Participant}s held by the underlying
     * {@link ParticipantHelper}.
     */
    public List<P>  getAllParticipants()
    {
        return participants.getAll();
    }

    /**
     * Return new {@link JitsiMeetUrl} instance which has only
     * {@link JitsiMeetUrl#serverUrl} field initialized with the value from
     * {@link ParticipantOptions#JITSI_MEET_URL_PROP} system property.
     *
     * @return a new instance of {@link JitsiMeetUrl}.
     */
    public JitsiMeetUrl getJitsiMeetUrl()
    {
        return participants.getJitsiMeetUrl().setRoomName(currentRoomName);
    }

    /**
     * Method is called "before class". {@link AbstractBaseTest} will figure out
     * if the test should be skipped in which case this method will not be
     * called.
     */
    protected void setupClass()
    {
        // Currently does nothing.
    }

    /**
     * Checks whether the current test needs to be skipped.
     *
     * @param config - The torture tests config.
     */
    private boolean checkForSkip(Properties config)
    {
        // protect static members, initialize them once
        synchronized (TESTS_TO_RUN_PNAME)
        {
            if (testsToRun == null)
            {
                testsToRun = getStringTokens(
                    config.getProperty(TESTS_TO_RUN_PNAME));
                testsToExclude = getStringTokens(
                    config.getProperty(TESTS_TO_EXCLUDE_PNAME));
                testsToInclude = getStringTokens(
                    config.getProperty(TESTS_TO_INCLUDE_PNAME));
                print("testsToRun=" + testsToRun);
                print("testsToExclude=" + testsToExclude);
                print("testsToInclude=" + testsToInclude);
            }
        }

        String thisClassName = this.getClass().getSimpleName();

        // if test is explicitly in exclude, skip
        if (testsToExclude.contains(thisClassName))
        {
            return true;
        }

        // if test is not explicitly in include
        // if testsToRun is empty, then check if test implementation
        // prefers to not be run by default
        // or if testsToRun is not empty check whether it contains this test
        if (!testsToInclude.contains(thisClassName)
            && ((testsToRun.isEmpty() && skipTestByDefault())
            || (!testsToRun.isEmpty()
            && !testsToRun.contains(thisClassName))))
        {
            return true;
        }

        return false;
    }

    /**
     * Returns list of string tokens, using ',' delimiter.
     * If string is null returns empty list.
     *
     * @param str the string with values delimited by ','.
     * @return list of strings.
     */
    private List<String> getStringTokens(String str)
    {
        if (str != null)
        {
            return Stream.of(str.split(","))
                .filter(e -> e.trim().length() > 0)
                .collect(Collectors.toList());
        }
        else
        {
            return Collections.EMPTY_LIST;
        }
    }

    /**
     * Tests can override this to be disabled by default and run only when
     * explicitly included in the tests to run using
     * {@link AbstractBaseTest#TESTS_TO_RUN_PNAME} or
     * {@link AbstractBaseTest#TESTS_TO_INCLUDE_PNAME}.
     *
     * @return <code>true</code> to skip current test class
     * or <code>false</code> otherwise.
     */
    public boolean skipTestByDefault()
    {
        return false;
    }

    /**
     * Checks if this test class is to be skipped.
     *
     * NOTE the method must not be called prior to
     * {@link AbstractBaseTest#setupClassPrivate(ITestContext)} execution
     * ("BeforeClass" TestNG annotation).
     *
     * @return <tt>true</tt> if this test class should be skipped or
     * <tt>false</tt> otherwise.
     * @throws IllegalStateException if "BeforeClass" test annotation has not
     * been executed yet and the skipped value is unknown.
     */
    public boolean isSkipped()
    {
        if (skipped == null)
        {
            throw new IllegalStateException(
                    "Method called, before the test class was configured");
        }
        return skipped;
    }

    /**
     * Closes a participant's session/driver.
     *
     * @param participant the participant to close.
     */
    public void closeParticipant(P participant)
    {
        this.participants.closeParticipant(participant);
    }

    /**
     * Method called "AfterClass". Will clean up any dangling
     * {@link Participant}s held by the {@link ParticipantHelper}.
     */
    @AfterClass
    public void cleanupClass()
    {
        if (this.participants != null)
        {
            this.participants.cleanup();
        }
    }

    @BeforeMethod
    public void beforeMethod(Method m)
    {
        print("Start " + m.getName() + ".");
    }

    @AfterMethod
    public void afterMethod(Method m)
    {
        print("End " + m.getName() + ".");
    }

}
