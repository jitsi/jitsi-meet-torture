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

import org.jitsi.meet.test.util.*;
import org.openqa.selenium.*;

/**
 * A helper class to encapsulate common operations with modal dialogs in web UI.
 *
 * @author Damian Minkov
 */
public class ModalDialogHelper
{
    /**
     * Button IDs to be able to search for the elements.
     */
    private final static String CANCEL_BUTTON = "modal-dialog-cancel-button";
    private final static String OK_BUTTON = "modal-dialog-ok-button";
    private final static String CLOSE_BUTTON = "modal-header-close-button";

    /**
     * Clicks on the ok button.
     * @param driver the page driver.
     */
    public static void clickOKButton(WebDriver driver)
    {
        TestUtils.click(driver, By.id(OK_BUTTON));
    }

    /**
     * Clicks on the cancel button.
     * @param driver the page driver.
     */
    public static void clickCancelButton(WebDriver driver)
    {
        TestUtils.click(driver, By.id(CANCEL_BUTTON));
    }

    /**
     * Clicks on the X (close) button.
     * @param driver the page driver.
     */
    public static void clickCloseButton(WebDriver driver)
    {
        TestUtils.click(driver, By.id(CLOSE_BUTTON));
    }

}
