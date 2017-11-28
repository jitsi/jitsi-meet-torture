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
import io.appium.java_client.android.*;

import org.jitsi.meet.test.mobile.base.*;

import org.jitsi.meet.test.mobile.permissions.*;
import org.openqa.selenium.*;

import org.testng.annotations.*;

import java.util.logging.*;

/**
 * Simple test case where we wait for room name field, fill it and clicks join.
 *
 * @author Damian Minkov
 * @author Pawel Domas
 */
public class WelcomePageTest
    extends AbstractBaseTest
{
    /**
     * The logger.
     */
    private static Logger logger
        = Logger.getLogger(WelcomePageTest.class.getName());

    /**
     * Accepts camera/microphone permissions dialog.
     *
     * @param driver a mobile driver instance.
     */
    private void acceptPermissionAlert(AppiumDriver<WebElement> driver)
    {
        PermissionsAlert alert = new PermissionsAlert(driver);

        try
        {
            // XXX Maybe check if alert is displayed ?
            alert.getAllowButton().click();
        }
        catch (NoSuchElementException exc)
        {
            logger.log(
                Level.SEVERE,
                "Allow button not found ! Page source: "
                    + driver.getPageSource());
        }
    }

    /**
     * Will try to grant drawing overlay permissions on Android.
     *
     * @param driver mobile <tt>AppiumDriver</tt> instance.
     */
    private void maybeAcceptOverlayPermissions(AndroidDriver<WebElement> driver)
    {
        String currentActivity = driver.currentActivity();
        if (!currentActivity.contains("Settings"))
        {
            logger.info("Settings not opened - will continue");
            return;
        }

        logger.info("Will try to grant draw overlay permissions...");

        OverlayDrawingPermissions permissionDialog
            = new OverlayDrawingPermissions(driver);

        // This will throw if not found
        permissionDialog.getPermitDescription().getText();

        WebElement toggleSwitch = permissionDialog.getAllowSwitch();

        toggleSwitch.click();

        driver.pressKeyCode(AndroidKeyCode.BACK);

        logger.info("Overlay permissions granted !");
    }

    @Test
    public void joinConference()
    {
        logger.log(Level.INFO, "Joining a conference.");

        AppiumDriver<WebElement> driver = getDriver();

        if (isAndroid())
        {
            maybeAcceptOverlayPermissions((AndroidDriver<WebElement>) driver);
        }

        // ALLOW to use camera
        acceptPermissionAlert(driver);

        WelcomePageView welcomePageView = new WelcomePageView(driver);

        String conferenceRoomName = getRoomName();
        MobileElement roomNameInput = welcomePageView.getRoomNameInput();

        roomNameInput.sendKeys(conferenceRoomName);

        if (isAndroid())
        {
            driver.hideKeyboard();
        }
        else
        {
            roomNameInput.sendKeys(Keys.ENTER);
        }

        takeScreenshot("RoomNameTextEntered");

        welcomePageView.getJoinRoomButton().click();


        // ALLOW to use mic
        acceptPermissionAlert(driver);

        ConferenceView conference = new ConferenceView(driver);

        conference.getRootView().click();

        takeScreenshot("joinedConferenceToolbarToggled");

        ToolbarView toolbar = new ToolbarView(driver);

        toolbar.getHangupButton().click();

        takeScreenshot("JustBeforeLeaving");
    }
}
