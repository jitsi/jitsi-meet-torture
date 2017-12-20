/*
 * Copyright @ Atlassian Pty Ltd
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
package org.jitsi.meet.test.mobile.base;

import io.appium.java_client.*;

import org.openqa.selenium.*;

import org.testng.*;

import java.util.*;
import java.util.logging.*;

/**
 * Mobile test failure listener.
 */
public class MobileTestFailureListener extends TestListenerAdapter
{
    /**
     * Does extra stuff around mobile test failure.
     *
     * @param result <tt>ITestResult</tt> instance.
     */
    public void onTestFailure(ITestResult result)
    {
        Object testInstance = result.getInstance();

        if (testInstance instanceof AbstractBaseTest)
        {
            List<MobileParticipant> mobiles
                = ((AbstractBaseTest) testInstance).getMobileParticipants();

            for (MobileParticipant mobile : mobiles)
            {
                AppiumDriver<WebElement> driver = mobile.getDriver();

                if (driver != null)
                {
                    MobileTestUtil.takeScreenshot(
                            driver, "onTestFailure(" + mobile + ")");

                    // Print what's currently visible on the screen.
                    Logger.getGlobal().severe(
                        "onTestFailure (" + mobile + "): \n"
                            + driver.getPageSource().replace(">", ">\n"));
                }
            }
        }
    }
}
