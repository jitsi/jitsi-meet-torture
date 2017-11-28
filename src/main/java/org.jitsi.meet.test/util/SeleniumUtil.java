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
package org.jitsi.meet.test.util;

import org.openqa.selenium.*;

import java.io.*;

/**
 * Selenium related utility methods.
 */
public class SeleniumUtil
{
    /**
     * Takes a screenshot using given driver.
     *
     * @param driver a driver implementing {@link TakesScreenshot}.
     * @param dirName the output directory name.
     * @param fileName the output filename.
     */
    public static void takeScreenshot(
            TakesScreenshot driver, String dirName, String fileName)
    {
        if(driver == null)
        {
            // FIXME exception or logger instead of System.err.println ?
            System.err.println(
                    "No driver to take screenshot from! FileName:" + fileName);
            return;
        }

        File scrFile = driver.getScreenshotAs(OutputType.FILE);
        if (!scrFile.renameTo(new File(dirName, fileName)))
        {
            throw new RuntimeException(
                    "Failed to rename screenshot file to directory name: "
                        + dirName + ", file name: " + fileName);
        }
    }
}
