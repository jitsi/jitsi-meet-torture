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
package org.jitsi.meet.test.mobile;

import io.appium.java_client.*;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;

import org.testng.annotations.*;

import org.jitsi.meet.test.mobile.base.*;

import java.util.logging.*;

/**
 * Simple test case where we wait for room name field, fill it and clicks join.
 *
 * @author Damian Minkov
 */
public class WelcomePage
    extends AbstractBaseTest
{
    /**
     * The logger.
     */
    private static Logger logger
        = Logger.getLogger(WelcomePage.class.getName());

    @Test
    public void joinConference()
    {
        logger.log(Level.INFO, "Joining a conference.");

        AppiumDriver<WebElement> driver = getDriver();

        // wait 3 seconds for room name field
        final String roomNameInputFieldStr = "Input room name.";
        new WebDriverWait(driver, 3000)
            .until(new ExpectedCondition<Boolean>()
            {
                public Boolean apply(WebDriver d)
                {
                    AppiumDriver<WebElement> dr = (AppiumDriver<WebElement>)d;
                    WebElement el
                        = dr.findElementByAccessibilityId(roomNameInputFieldStr);
                    return el != null && el.isDisplayed();
                }
            });

        WebElement roomName
            = driver.findElementByAccessibilityId(roomNameInputFieldStr);
        roomName.sendKeys(getRoomName());

        takeScreenshot("RoomNameTextEntered");

        WebElement joinButton
            = driver.findElementByAccessibilityId("Tap to Join.");
        joinButton.click();

        Object obj = new Object();
        synchronized(obj)
        {
            try
            {
                obj.wait(10000);
            }
            catch(Throwable t){}
        }
        takeScreenshot("JustBeforeLeaving");
    }
}
