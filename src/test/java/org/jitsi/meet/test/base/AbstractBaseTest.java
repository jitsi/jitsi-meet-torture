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

import org.testng.*;
import org.testng.annotations.*;

import java.util.*;
import java.util.stream.*;

/**
 * The base of all tests, checks whether the tests is supposed to be executed
 * or skipped by checking the properties which tests to run, exclude or include.
 */
public abstract class AbstractBaseTest
    extends AbstractParticipantHelper
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

    @BeforeClass
    public void check()
    {
        if (testsToRun == null)
        {
            testsToRun
                = getStringTokens(System.getProperty(TESTS_TO_RUN_PNAME));
            testsToExclude
                = getStringTokens(System.getProperty(TESTS_TO_EXCLUDE_PNAME));
            testsToInclude
                = getStringTokens(System.getProperty(TESTS_TO_INCLUDE_PNAME));
        }

        String thisClassName = this.getClass().getSimpleName();

        // if test is explicitly in exclude, skip
        if (testsToExclude.contains(thisClassName))
        {
            throw new SkipException("skips-" + thisClassName);
        }

        // if test is explicitly in include, run it
        if (testsToInclude.contains(thisClassName))
        {
            return;
        }

        // if testsToRun is empty, then check if test implementation
        // prefers to not be run by default
        // or if testsToRun is not empty check whether it contains this test
        if ((testsToRun.isEmpty() && skipTestByDefault())
            || (!testsToRun.isEmpty() && !testsToRun.contains(thisClassName)))
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
     * Wraps assertTrue.
     *
     * @param message the message.
     * @param value the value to check.
     */
    public void assertTrue(String message, boolean value)
    {
        Assert.assertTrue(value, message);
    }

    /**
     * Wraps assertEquals.
     *
     * @param obj1 the first object.
     * @param obj2 the second object.
     */
    public void assertEquals(Object obj1, Object obj2)
    {
        this.assertEquals(null, obj1, obj2);
    }

    /**
     * Wraps assertEquals.
     *
     * @param message the message to show.
     * @param obj1 the first object.
     * @param obj2 the second object.
     */
    public void assertEquals(String message, Object obj1, Object obj2)
    {
        Assert.assertEquals(obj1, obj2, message);
    }

    /**
     * Wraps assertFalse.
     * @param message the message to show.
     * @param value the value to check.
     */
    public void assertFalse(String message, boolean value)
    {
        Assert.assertFalse(value, message);
    }

    @BeforeClass
    public void setup()
    {
        super.setup();
    }

    @AfterClass
    public void cleanup()
    {
        super.cleanup();
    }
}
