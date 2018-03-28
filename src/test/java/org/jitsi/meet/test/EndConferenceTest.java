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

import org.jitsi.meet.test.util.*;
import org.jitsi.meet.test.web.*;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;
import org.testng.annotations.*;

/**
 * Test if we can hangup the call.
 */
public class EndConferenceTest
    extends WebTestBase
{
    @Override
    public void setupClass()
    {
        super.setupClass();

        ensureTwoParticipants();
    }

    /**
     * Hang up the call and check if we're redirected to the main page.
     */
    @Test
    public void hangupCallAndCheck()
    {
        final WebDriver driver1 = getParticipant1().getDriver();
        final String url = driver1.getCurrentUrl();

        // hangup and wait for redirect
        getParticipant1().getToolbar().clickHangUpButton();
        TestUtils.waitForCondition(
            driver1,
            5,
            (ExpectedCondition<Boolean>) d
                -> !url.equals(driver1.getCurrentUrl()));
    }
}
