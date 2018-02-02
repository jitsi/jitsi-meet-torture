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

import org.jitsi.meet.test.util.*;

import org.openqa.selenium.*;
import org.testng.*;
import org.testng.annotations.*;

import java.lang.reflect.*;
import java.util.*;
import java.util.stream.*;

/**
 * The base of all tests, checks whether the tests is supposed to be executed
 * or skipped by checking the properties which tests to run, exclude or include.
 */
@Listeners(FailureListener.class)
public abstract class AbstractBaseTest
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
     * The current room name used.
     */
    protected final String currentRoomName;

    /**
     * The participants pool created/used by this test instance.
     */
    final protected ParticipantHelper participants;

    /**
     * Default.
     */
    protected AbstractBaseTest()
    {
        currentRoomName
            = "torture" + String.valueOf((int)(Math.random()*1000000));
        participants = new ParticipantHelper();
    }

    /**
     * Constructs new AbstractBaseTest with predefined baseTest, to
     * get its participants and room name.
     *
     * @param baseTest the parent test.
     */
    protected AbstractBaseTest(AbstractBaseTest baseTest)
    {
        currentRoomName = baseTest.currentRoomName;
        participants = new ParticipantHelper(baseTest.participants);
    }

    @BeforeClass
    public void check()
    {
        checkForSkip();

        print(
            "---=== Testing " + getClass().getSimpleName() + " ===---");

        // make sure if setup fails we will cleanup
        // when configure method @BeforeClass fails @AfterClass is not executed
        try
        {
            setupClass();
        }
        catch (Throwable t)
        {
            cleanupClass();
            throw t;
        }
    }

    /**
     * Returns all {@link Participant}s held by the underlying
     * {@link ParticipantHelper}.
     */
    public List<Participant<? extends WebDriver>>  getAllParticipants()
    {
        return participants.getAllParticipants();
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
     * Checks whether the current test need to be skiped and throw SkipException
     * if this is true.
     *
     * @throws SkipException thrown when test execution needs to be skipped
     */
    protected void checkForSkip()
        throws SkipException
    {
        // protect static members, initialize them once
        synchronized (TESTS_TO_RUN_PNAME)
        {
            if (testsToRun == null)
            {
                testsToRun = getStringTokens(
                    System.getProperty(TESTS_TO_RUN_PNAME));
                testsToExclude = getStringTokens(
                    System.getProperty(TESTS_TO_EXCLUDE_PNAME));
                testsToInclude = getStringTokens(
                    System.getProperty(TESTS_TO_INCLUDE_PNAME));
            }
        }

        String thisClassName = this.getClass().getSimpleName();

        // if test is explicitly in exclude, skip
        if (testsToExclude.contains(thisClassName))
        {
            throw new SkipException("skips-" + thisClassName);
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
            throw new SkipException("skips-" + thisClassName);
        }
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
     * Method called "AfterClass". Will clean up any dangling
     * {@link Participant}s held by the {@link ParticipantHelper}.
     */
    @AfterClass
    public void cleanupClass()
    {
        this.participants.cleanup();
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

    /**
     * Prints text.
     * @param txt the text to print.
     */
    protected static void print(String txt)
    {
        TestUtils.print(txt);
    }
}
