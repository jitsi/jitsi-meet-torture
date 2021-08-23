/*
 * Copyright @ 2018 8x8 Pty Ltd
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
package org.jitsi.meet.test.pageobjects.web;

import org.jitsi.meet.test.util.TestUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * A helper class to encapsulate operations with unmute modal dialog.
 *
 * @author Damian Minkov
 */
public class UnmuteModalDialogHelper
{
    /**
     * Button IDs to be able to search for the elements.
     */
    private final static String DISMISS_BUTTON = "dismiss-button";
    private final static String UNMUTE_BUTTON = "unmute-button";

    /**
     * Clicks on the unmute button.
     * @param driver the page driver.
     */
    public static void clickUnmuteButton(WebDriver driver)
    {
        TestUtils.click(driver, By.id(UNMUTE_BUTTON));
    }

    /**
     * Clicks on the dismiss button.
     * @param driver the page driver.
     */
    public static void clickDismissButton(WebDriver driver)
    {
        TestUtils.click(driver, By.id(DISMISS_BUTTON));
    }

    /**
     * Checks whether there is unmute button, waits 2 seconds for the button to appear if missing.
     * @param driver the driver to check.
     * @return <tt>true</tt>
     */
    public static boolean hasUnmuteButton(WebDriver driver)
    {
        return TestUtils.waitForElementBy(driver, By.id(UNMUTE_BUTTON), 2) != null;
    }
}
