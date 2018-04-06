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
import org.jitsi.meet.test.pageobjects.*;
import org.jitsi.meet.test.pageobjects.mobile.*;
import org.jitsi.meet.test.pageobjects.mobile.permissions.*;
import org.openqa.selenium.*;
import org.openqa.selenium.NoSuchElementException;

import java.util.*;
import java.util.logging.*;

/**
 * A mobile {@link Participant}.
 *
 * @author Pawel Domas
 */
public class MobileParticipant extends Participant<AppiumDriver<WebElement>>
{
    /**
     * Logger used by this instance.
     */
    private final static Logger logger = Logger.getGlobal();

    /**
     * The app bundle identifier used to reinstall/start the app if it was
     * previously installed on the device.
     */
    private final String appBundleId;

    /**
     * The full path to the binary file which can be used to install the app on
     * the device.
     */
    private final String appBinaryFile;

    /**
     * The conference view instance.
     *
     * FIXME refactor things to have a page objects factory which would be
     * stored in a field here instead of keeping all the objects in participant
     * directly.
     */
    private ConferenceView conferenceView;

    /**
     * The Appium driver instance.
     */
    private final AppiumDriver<WebElement> driver;

    /**
     * Initializes {@link MobileParticipant}.
     *
     * @param driver - The Appium driver instance connected to the mobile
     * device.
     * @param name - The name for this participant.
     * @param type - The participant's type.
     * @param appBundleId - The app bundle identifies which can be used to
     * start/uninstall the app.
     * @param appBinaryFile - A full path to the app binary file which can be
     * used to install the app on the device.
     */
    public MobileParticipant(AppiumDriver<WebElement> driver,
                             String name,
                             ParticipantType type,
                             String appBundleId,
                             String appBinaryFile)
    {
        super(name, driver, type);
        this.driver = Objects.requireNonNull(driver, "driver");
        this.appBundleId = Objects.requireNonNull(appBundleId, "appBundleId");
        this.appBinaryFile = appBinaryFile;
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

    /**
     * @return {@link ConferenceView} for this participant.
     */
    public ConferenceView getConferenceView()
    {
        if (conferenceView == null)
        {
            conferenceView = new ConferenceView(this);
        }

        return conferenceView;
    }

    /**
     * Not implemented yet.
     *
     * @return <tt>null</tt>
     */
    public Object getConfigValue(String key) { return null; }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getEndpointId()
    {
        throw new RuntimeException("Not implemented");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getMeetDebugLog()
    {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getRTPStats()
    {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getProtocol()
    {
        throw new RuntimeException("Not implemented");
    }

    /**
     * @return {@link ToolbarView} for this participant.
     */
    public ToolbarView getToolbarView()
    {
        return getConferenceView().getToolbarView();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isP2pConnected()
    {
        throw new RuntimeException("Not implemented");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isXmppConnected()
    {
        throw new RuntimeException("Not implemented");
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

    /**
     * {@inheritDoc}
     */
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

        roomNameInput.sendKeys(conferenceUrl.toString());

        if (type.isAndroid())
        {
            roomNameInput.click();

            getAndroidDriver().pressKeyCode(AndroidKeyCode.ENTER);
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
    public void close()
    {
        try
        {
            driver.quit();
        }
        catch (Exception e)
        {
            logger.log(Level.SEVERE, "Failed to close driver", e);
        }
    }

    /**
     * {@inheritDoc}
     */
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
            driver.installApp(appBinaryFile);
            Logger.getGlobal().log(Level.INFO, "Launching app...");
            driver.launchApp();
        }
    }

    @Override
    public String toString()
    {
        return "MobileParticipant[" + name + "]@" + hashCode();
    }

    /**
     * {@inheritDoc}
     */
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
    public void waitForSendReceiveData(boolean send, boolean receive)
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

    @Override
    public Filmstrip<MobileParticipant> getFilmstrip()
    {
        throw new RuntimeException("Not implemented");
    }
}
