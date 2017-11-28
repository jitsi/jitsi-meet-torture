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

import org.openqa.selenium.*;

import org.jitsi.meet.test.util.*;

/**
 * Utility functions for mobile testing.
 */
public class MobileTestUtil
{
    /**
     * Takes screenshot from the given mobile driver to the predefined output
     * screenshots directory defined by "appium.screenshots.dir" system
     * property. If that property has no value then it will default to
     * a value stored under "java.io.tmpdir". If that is also undefined will
     * default to "" (current directory ?).
     *
     * @param driver a mobile driver which implements {@link TakesScreenshot}.
     * @param name the name of the screenshot file (.png extension will be
     * appended at the end).
     */
    public static void takeScreenshot(TakesScreenshot driver, final String name)
    {
        String screenshotDirectory
            = System.getProperty(
                    "appium.screenshots.dir",
                    System.getProperty("java.io.tmpdir", ""));

        SeleniumUtil.takeScreenshot(driver, screenshotDirectory, name + ".png");
    }
}
