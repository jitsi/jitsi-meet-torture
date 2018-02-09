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
import org.jitsi.meet.test.base.*;
import org.jitsi.meet.test.pageobjects.mobile.*;
import org.jitsi.meet.test.pageobjects.mobile.permissions.*;
import org.openqa.selenium.*;
import org.openqa.selenium.NoSuchElementException;

import java.util.*;
import java.util.logging.*;

/**
 * A mobile {@link Participant}.
 */
public class MobileParticipant extends Participant<AppiumDriver<WebElement>>
{
    /**
     * Logger used by this instance.
     */
    private final static Logger logger = Logger.getGlobal();

    private final String appBundleId;

    private final String appPath;

    private ConferenceView conferenceView;

    private final AppiumDriver<WebElement> driver;

    public MobileParticipant(AppiumDriver<WebElement> driver,
                             String name,
                             ParticipantType type,
                             String appBundleId,
                             String appPath,
                             String meetURL)
    {
        super(name, driver, type, meetURL, null);
        this.driver = Objects.requireNonNull(driver, "driver");
        this.appBundleId = Objects.requireNonNull(appBundleId, "appBundleId");
        this.appPath = Objects.requireNonNull(appPath, "appPath");
    }

    /**
     * Accepts camera/microphone permissions dialog.
     */
    private void acceptPermissionAlert()
    {
        PermissionsAlert alert = new PermissionsAlert(this);

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

    private AndroidDriver<WebElement> getAndroidDriver()
    {
        return type.isAndroid() ? (AndroidDriver<WebElement>) driver : null;
    }

    public ConferenceView getConferenceView()
    {
        if (conferenceView == null)
        {
            conferenceView = new ConferenceView(this);
        }

        return conferenceView;
    }

    public ToolbarView getToolbarView()
    {
        return getConferenceView().getToolbarView();
    }

    /**
     * Will try to grant drawing overlay permissions on Android.
     */
    private void maybeAcceptOverlayPermissions()
    {
        String currentActivity = getAndroidDriver().currentActivity();
        if (!currentActivity.contains("Settings"))
        {
            logger.info("Settings not opened - will continue");
            return;
        }

        logger.info("Will try to grant draw overlay permissions...");

        OverlayDrawingPermissions permissionDialog
            = new OverlayDrawingPermissions(this);

        // This will throw if not found
        permissionDialog.getPermitDescription().getText();

        WebElement toggleSwitch = permissionDialog.getAllowSwitch();

        toggleSwitch.click();

        getAndroidDriver().pressKeyCode(AndroidKeyCode.BACK);

        logger.info("Overlay permissions granted !");
    }

    @Override
    public void doJoinConference(JitsiMeetUrl conferenceUrl)
    {
        logger.log(Level.INFO, "Joining a conference: " + conferenceUrl);

        if (type.isAndroid())
        {
            maybeAcceptOverlayPermissions();
        }

        // ALLOW to use camera
        acceptPermissionAlert();

        WelcomePageView welcomePageView = new WelcomePageView(this);

        MobileElement roomNameInput = welcomePageView.getRoomNameInput();

        // FIXME: check how this works...
        roomNameInput.sendKeys(conferenceUrl.toString());

        if (type.isAndroid())
        {
            driver.hideKeyboard();

            welcomePageView.getJoinRoomButton().click();
        }
        else
        {
            // NOTE on iOS the most reliable way for closing the keyboard is to
            // send the ENTER key.
            roomNameInput.sendKeys(Keys.ENTER);
        }

        takeScreenshot("roomNameTextEntered");

        // ALLOW to use mic
        acceptPermissionAlert();

        ConferenceView conference = new ConferenceView(this);

        // Just to make sure we're in the conference
        conference.getRootView().getText();
    }

    /**
     * Wraps take screen shot functionality.
     *
     * @param fileName the name of the screenshot file without the extension.
     */
    public void takeScreenshot(String fileName)
    {
        // This sometimes fails on Amazon device farm, but the tests can
        // continue after that.
        try
        {
            takeScreenshot(
                FailureListener.getScreenshotsOutputFolder(),
                fileName + ".png");
        }
        catch(WebDriverException exc)
        {
            logger.log(
                    Level.SEVERE,
                    "Failed to take a screenshot for: " + fileName,
                    exc);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void quit()
    {
        try
        {
            driver.quit();
        }
        catch (Exception e)
        {
            logger.log(Level.SEVERE, "Failed to quit driver", e);
        }
    }

    public AppiumDriver<WebElement> getDriver()
    {
        return driver;
    }

    /**
     * Will reinstall the app if it's currently installed. Can be helpful when
     * running locally to reproduce the media permissions alerts.
     */
    public void reinstallAppIfInstalled()
    {
        // FIXME driver.isAppInstalled does not work on iOS
        if (!type.isAndroid() || driver.isAppInstalled(appBundleId))
        {
            Logger.getGlobal().log(Level.INFO, "Removing app...");
            driver.removeApp(appBundleId);
            Logger.getGlobal().log(Level.INFO, "Installing app...");
            driver.installApp(appPath);
            Logger.getGlobal().log(Level.INFO, "Launching app...");
            driver.launchApp();
        }
    }

    @Override
    public String toString()
    {
        return "MobileParticipant[" + name + "]@" + hashCode();
    }

    @Override
    protected void doHangUp()
    {
        ToolbarView toolbar = getToolbarView();

        if (!toolbar.isOpen())
        {
            toolbar.open();
        }

        toolbar.getHangupButton().click();
    }

    @Override
    public void waitToJoinMUC(long timeout)
    {

    }

    @Override
    public boolean isInMuc()
    {
        return false;
    }

    @Override
    public void waitForIceConnected()
    {

    }

    @Override
    public void waitForIceConnected(long timeout)
    {

    }

    @Override
    public void waitForSendReceiveData()
    {

    }

    @Override
    public void waitForRemoteStreams(int n)
    {

    }

    @Override
    public void setDisplayName(String name)
    {

    }

    @Override
    public void pressShortcut(Character shortcut)
    {

    }
}
