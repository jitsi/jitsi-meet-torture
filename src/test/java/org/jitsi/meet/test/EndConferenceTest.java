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
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;
import org.jitsi.meet.test.util.*;

/**
 * Test if we can hangup the call.
 */
public class EndConferenceTest
    extends TestCase
{
    /**
     * Constructs test.
     * @param name the method name for the test.
     */
    public EndConferenceTest(String name)
    {
        super(name);
    }

    /**
     * Orders the tests.
     * @return the suite with order tests.
     */
    public static Test suite()
    {
        TestSuite suite = new TestSuite();

        suite.addTest(new EndConferenceTest("hangupCallAndCheck"));

        return suite;
    }

    /**
     * Hang up the call and check if we're redirected to the main page.
     */
    public void hangupCallAndCheck()
    {
        System.err.println("Start hangupCallAndCheck.");

        final WebDriver owner = ConferenceFixture.getOwner();
        final String url = owner.getCurrentUrl();

        // hangup and wait for redirect
        MeetUIUtils.clickOnToolbarButton(owner, "toolbar_button_hangup");
        TestUtils.waitForCondition(owner, 5,
            new ExpectedCondition<Boolean>()
            {
                public Boolean apply(WebDriver d)
                {
                    return !url.equals(owner.getCurrentUrl());
                }
            });

        // Close the owner, as the welcome page is not a state recognized
        // by the ConferenceFixture and it will fail to figure out that
        // the owner must be restarted in case any tests are to follow
        // the EndConferenceTest.
        // It simpler to close it than implement extra state which would not
        // be of much use.
        ConferenceFixture.close(owner);
    }
}
