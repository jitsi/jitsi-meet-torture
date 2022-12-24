/*
 * Copyright @ 2021 8x8 International
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
package org.jitsi.meet.test;

import org.jitsi.meet.test.pageobjects.web.*;
import org.jitsi.meet.test.util.*;
import org.jitsi.meet.test.web.*;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.*;
import org.openqa.selenium.support.ui.*;
import org.testng.annotations.*;

/**
 * Tests disable self view functionality.
 */
public class DisableSelfViewTest
    extends WebTestBase
{
    /**
     * The local tile container xpath.
     */
    private final static String LOCAL_TILE_XPATH = "//span[@id='localVideoContainer']";

    /**
     * The local video menu trigger button xpath.
     */
    private final static String LOCAL_VIDEO_MENU_BUTTON_XPATH = LOCAL_TILE_XPATH + "//span[@id='localvideomenu']";

    /**
     * The local video menu hide self view button xpath.
     */
    private final static String HIDE_SELF_VIEW_BUTTON_XPATH = "//div[@class='popover']//div[@id='hideselfviewButton']";

    /**
     * The participants.
     */
    private WebParticipant participant1;
    private WebParticipant participant2;

    @Override
    public void setupClass()
    {
        super.setupClass();

        ensureTwoParticipants();
        participant1 = getParticipant1();
        participant2 = getParticipant2();
    }

    @Test
    public void testHideSelfViewFromMenu()
    {
        checkSelfViewHidden(false);

        WebDriver driver1 = participant1.getDriver();

        // open local video menu
        WebElement localTile = driver1.findElement(By.xpath(LOCAL_TILE_XPATH));
        Actions hoverOnLocalTile = new Actions(driver1);
        hoverOnLocalTile.moveToElement(localTile);
        hoverOnLocalTile.perform();

        TestUtils.waitForDisplayedElementByXPath(driver1, LOCAL_VIDEO_MENU_BUTTON_XPATH, 5);
        driver1.findElement(By.xpath(LOCAL_VIDEO_MENU_BUTTON_XPATH)).click();

        // click Hide self view button
        TestUtils.waitForDisplayedElementByXPath(driver1, HIDE_SELF_VIEW_BUTTON_XPATH, 5);
        driver1.findElement(By.xpath(HIDE_SELF_VIEW_BUTTON_XPATH)).click();

        checkSelfViewHidden(true, true);

        // switch to tile view and check
        participant1.getToolbar().clickTileViewButton();
        checkSelfViewHidden(true);
    }

    @Test(dependsOnMethods = { "testHideSelfViewFromMenu" })
    public void testShowSelfViewFromSettings()
    {
        toggleSelfViewFromSettings(false);
        checkSelfViewHidden(false);
    }

    @Test(dependsOnMethods = { "testShowSelfViewFromSettings" })
    public void testHideSelfViewFromSettings()
    {
        toggleSelfViewFromSettings(true);
        checkSelfViewHidden(true, true);
    }

    @Test(dependsOnMethods = { "testHideSelfViewFromSettings" })
    public void testSelfViewIsShownIfAloneInMeeting()
    {
        checkSelfViewHidden(true);
        participant2.hangUp();
        checkSelfViewHidden(false);
    }

    /**
     * Toggles the self view option from the settings dialog.
     * @param hide - Whether to hide the self view or not.
     */
    private void toggleSelfViewFromSettings(Boolean hide)
    {
        participant1.getToolbar().clickSettingsButton();
        SettingsDialog settings = participant1.getSettingsDialog();
        settings.waitForDisplay();
        settings.setHideSelfView(hide);
        settings.submit();
    }

    /**
     * Checks whether the local self view is displayed or not.
     * @param hidden - Whether the self view should be hidden or not.
     * @param notification - Whether the re-enable self view notification should be displayed.
     */
    private void checkSelfViewHidden(Boolean hidden, Boolean notification)
    {
        if (!hidden)
        {
            TestUtils.waitForDisplayedElementByXPath(participant1.getDriver(), LOCAL_TILE_XPATH, 5);
        }
        else
        {
            if (notification)
            {
                // the notification about re-enabling tile view should appear
                TestUtils.waitForCondition(participant1.getDriver(), 5, (ExpectedCondition<Boolean>) d ->
                        participant1.getNotifications().hasReenableSelfViewNotification());
                participant1.getNotifications().closeReeanbleSelfViewNotification();
            }
            TestUtils.waitForElementNotPresentOrNotDisplayedByXPath(participant1.getDriver(),
                    LOCAL_TILE_XPATH, 5);
        }
    }

    private void checkSelfViewHidden(Boolean hidden)
    {
        checkSelfViewHidden(hidden, false);
    }
}
