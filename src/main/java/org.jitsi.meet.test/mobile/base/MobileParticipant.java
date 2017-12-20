package org.jitsi.meet.test.mobile.base;

import io.appium.java_client.*;
import io.appium.java_client.android.*;
import org.jitsi.meet.test.mobile.*;
import org.jitsi.meet.test.mobile.permissions.*;
import org.openqa.selenium.*;
import org.openqa.selenium.NoSuchElementException;

import java.util.*;
import java.util.logging.*;

/**
 *
 */
public class MobileParticipant
{
    /**
     * FIXME global logger
     */
    private final static Logger logger = Logger.getGlobal();

    private final AppiumDriver<WebElement> driver;
    private final String appBundleId;
    private final String appPath;
    private final String os;
    private final String name;

    public boolean isAndroid()
    {
        return "android".equalsIgnoreCase(os);
    }

    public MobileParticipant(AppiumDriver<WebElement> driver,
                             String name,
                             String os,
                             String appBundleId,
                             String appPath)
    {
        this.name = name;
        this.os = Objects.requireNonNull(os, "os");
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
        return isAndroid() ? (AndroidDriver<WebElement>) driver : null;
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

    public void joinConference(String roomName)
    {
        logger.log(Level.INFO, "Joining a conference: " + roomName);

        if (isAndroid())
        {
            maybeAcceptOverlayPermissions();
        }

        // ALLOW to use camera
        acceptPermissionAlert();

        WelcomePageView welcomePageView = new WelcomePageView(this);

        MobileElement roomNameInput = welcomePageView.getRoomNameInput();

        roomNameInput.sendKeys(roomName);

        if (isAndroid())
        {
            driver.hideKeyboard();
        }
        else
        {
            roomNameInput.sendKeys(Keys.ENTER);
        }

        takeScreenshot("roomNameTextEntered");

        welcomePageView.getJoinRoomButton().click();

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
            MobileTestUtil.takeScreenshot(driver, this + "-" + fileName);
        }
        catch(WebDriverException exc)
        {
            logger.log(
                    Level.SEVERE,
                    "Failed to take a screenshot for: " + fileName,
                    exc);
        }
    }

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
        if (!isAndroid() || driver.isAppInstalled(appBundleId))
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
}
