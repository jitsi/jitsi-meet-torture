/*
 * Copyright @ 2018 Atlassian Pty Ltd
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
import org.jitsi.meet.test.web.*;
import org.openqa.selenium.*;

import java.util.*;

/**
 * Represents the toolbar in a particular {@link WebParticipant}.
 *
 * @author Leonard Kim
 */
public class Toolbar {
    /**
     * Accessibility labels to be used as selectors for finding WebElements
     * within the {@link Toolbar}.
     */
    private final static String AUDIO_MUTE = "Toggle mute audio";
    private final static String CHAT = "Toggle chat window";
    private final static String DESKTOP = "Toggle screenshare";
    private final static String ETHERPAD = "Toggle shared document";
    private final static String HANGUP = "Leave the call";
    private final static String INVITE = "Invite people";
    private final static String OVERFLOW = "Toggle more actions menu";
    private final static String OVERFLOW_MENU = "More actions menu" ;
    private final static String PROFILE = "Edit your profile";
    private final static String RECORD = "Toggle recording";
    private final static String SECURITY = "Security options";
    private final static String SETTINGS = "Toggle settings";
    private final static String SHARE_VIDEO = "Toggle Youtube video sharing";
    private final static String TILE_VIEW_BUTTON = "Toggle tile view";
    private final static String VIDEO_MUTE = "Toggle mute video";
    private final static String VIDEO_QUALITY = "Manage video quality";

    /**
     * The ID of the toolbar. To be used as a selector when trying to locate
     * the toolbar on the page.
     */
    private final static String TOOLBOX_ID = "new-toolbox";

    /**
     * The xpath for the desktop button icon. Its toggle class can determine
     * if desktop sharing is active or not active.
     */
    private final static String DESKTOP_ICON_XPATH
        = "//*[local-name() = 'svg' and @id = 'share-desktop']";

    /**
     * The participant.
     */
    private final WebParticipant participant;

    /**
     * Initializes a new {@link Toolbar} instance.
     *
     * @param participant the participant for this {@link Toolbar}.
     */
    public Toolbar(WebParticipant participant)
    {
        this.participant = Objects.requireNonNull(participant, "participant");
    }

    /**
     * Clicks on the microphone mute toolbar button, which toggles audio mute.
     */
    public void clickAudioMuteButton()
    {
        clickButton(AUDIO_MUTE);
    }

    /**
     * Clicks on the "desktop sharing" toolbar button. Fails if the button
     * doesn't exist or if the toggled state is not changed after the click.
     */
    public void clickDesktopSharingButton()
    {
        WebDriver driver = participant.getDriver();

        waitForButtonDisplay(DESKTOP);

        WebElement desktopIcon = driver.findElement(
            By.xpath(DESKTOP_ICON_XPATH));
        String classNames = desktopIcon.getAttribute("class");
        boolean isToggled = classNames.contains("toggled");

        clickButton(DESKTOP);

        if (isToggled)
        {
            TestUtils.waitForElementNotContainsClassByXPath(
                driver,
                DESKTOP_ICON_XPATH,
                "toggled",
                2);
        }
        else
        {
            TestUtils.waitForElementContainsClassByXPath(
                driver,
                DESKTOP_ICON_XPATH,
                "toggled",
                2);
        }
    }

    /**
     * Clicks on the "chat" toolbar button which opens or closes the chat panel.
     */
    public void clickChatButton()
    {
        clickButton(CHAT);
    }

    /**
     * Clicks on the etherpad toolbar button which shows or hides etherpad.
     */
    public void clickEtherpadButton()
    {
        clickButtonInOverflowMenu(ETHERPAD);
    }

    /**
     * Clicks on the settings toolbar button which opens the device selection
     * popup.
     */
    public void clickFilmstripOnlySettingsButton()
    {
        clickButton(SETTINGS);
    }

    /**
     * Clicks on the hangup toolbar button which leaves the current conference.
     */
    public void clickHangUpButton()
    {
        attemptToClickButton(HANGUP);
    }

    /**
     * Clicks on the info toolbar button which opens or closes the info dialog.
     */
    public void clickInviteButton()
    {
        clickButton(INVITE);
    }

    /**
     * Clicks on the overflow toolbar button which opens or closes the overflow
     * menu.
     */
    public void clickOverflowButton() {
        clickButton(OVERFLOW);
    }

    /**
     * Clicks on the profile toolbar button which opens or closes the profile
     * panel.
     */
    public void clickProfileButton()
    {
        clickButtonInOverflowMenu(PROFILE);
    }

    /**
     * Clicks on the recording toolbar button which toggles recording or live
     * streaming.
     */
    public void clickRecordButton()
    {
        clickButtonInOverflowMenu(RECORD);
    }

    /**
     * Clicks on the security toolbar button which opens or closes the security dialog.
     */
    public void clickSecurityButton()
    {
        clickButton(SECURITY);
    }

    /**
     * Clicks on the settings toolbar button which opens or closes the settings
     * panel.
     */
    public void clickSettingsButton()
    {
        clickButtonInOverflowMenu(SETTINGS);
    }

    /**
     * Clicks on the shared video toolbar button which toggles sharing of a
     * YouTube video.
     */
    public void clickSharedVideoButton()
    {
        clickButtonInOverflowMenu(SHARE_VIDEO);
    }

    /**
     * Clicks on the tile view button which enables or disables tile layout.
     */
    public void clickTileViewButton()
    {
        clickButton(TILE_VIEW_BUTTON);
    }

    /**
     * Clicks on the video mute toolbar button which toggles video mute.
     */
    public void clickVideoMuteButton()
    {
        clickButton(VIDEO_MUTE);
    }

    /**
     * Clicks on the video quality toolbar button which opens or closes the
     * dialog for adjusting max-received video quality.
     */
    public void clickVideoQualityButton()
    {
        clickButtonInOverflowMenu(VIDEO_QUALITY);
    }

    /**
     * Ensures the overflow menu is not displayed.
     */
    public void closeOverflowMenu()
    {
        if (!isOverflowMenuOpen())
        {
            return;
        }

        clickOverflowButton();

        waitForOverFlowMenu(false);
    }

    /**
     * Gets the participant's avatar image element located in the toolbar.
     *
     * @return <tt>WebElement</tt> for the toolbar profile image.
     */
    public WebElement getProfileImage()
    {
        openOverflowMenu();

        return participant.getDriver().findElement(By.cssSelector(
            getAccessibilityCSSSelector(PROFILE) + " img"));
    }

    /**
     * Checks whether or not the recording button is present in the toolbar.
     *
     * @return True if the recording button is present, false if it is not.
     */
    public boolean hasRecordButton()
    {
        openOverflowMenu();

        List<WebElement> elements
            = participant.getDriver().findElements(By.cssSelector(
                getAccessibilityCSSSelector(RECORD)));

        return !elements.isEmpty();
    }

    /**
     * Returns whether or not this the participant is using the new toolbar.
     * This method is used to help transition tests over to the new toolbar.
     *
     * @return true to indicate this is the new toolbar.
     */
    public boolean isNewToolbar()
    {
        return true;
    }

    /**
     * Checks if the overflow menu is open and visible.
     *
     * @return True if the overflow menu is visible, false if it is not.
     */
    public boolean isOverflowMenuOpen()
    {
        List<WebElement> elements = participant.getDriver().findElements(
            getOverflowMenuSelector());

        return !elements.isEmpty();
    }

    /**
     * Ensure the overflow menu is displayed.
     */
    public void openOverflowMenu()
    {
        if (isOverflowMenuOpen())
        {
            return;
        }

        clickOverflowButton();

        waitForOverFlowMenu(true);
    }

    /**
     * Waits up to 10 seconds for the shared video button in the toolbar to be
     * visible.
     */
    public void waitForSharedVideoButtonDisplay()
    {
        clickButtonInOverflowMenu(SHARE_VIDEO);
    }

    /**
     * Waits up to 10 seconds for the video mute button in the toolbar to be
     * visible.
     */
    public void waitForVideoMuteButtonDisplay()
    {
        waitForButtonDisplay(VIDEO_MUTE);
    }

    /**
     * Waits up to 3 seconds for the toolbar to be visible.
     */
    public void waitForVisible()
    {
        TestUtils.waitForDisplayedElementByID(
            participant.getDriver(),
            TOOLBOX_ID,
            3);
    }

    /**
     * Try to click on a button but do not fail if it is not clickable.
     *
     * @param accessibilityLabel The accessibility label of the button to be
     * clicked.
     */
    private void attemptToClickButton(String accessibilityLabel)
    {
        MeetUIUtils.clickOnElement(
            participant.getDriver(),
            getAccessibilityCSSSelector(accessibilityLabel),
            false
        );
    }

    /**
     * Try to click on a button and fail if it cannot be clicked.
     *
     * @param accessibilityLabel The accessibility label of the button to be
     * clicked.
     */
    private void clickButton(String accessibilityLabel)
    {
        MeetUIUtils.clickOnElement(
            participant.getDriver(),
            getAccessibilityCSSSelector(accessibilityLabel),
            true
        );
    }

    /**
     * Ensure the overflow menu is open and clicks on a specified button.
     *
     * @param accessibilityLabel The accessibility label of the button to be
     * clicked.
     */
    private void clickButtonInOverflowMenu(String accessibilityLabel)
    {
        openOverflowMenu();

        clickButton(accessibilityLabel);

        closeOverflowMenu();
    }

    /**
     * Helper for formatting the string to be used as a CSS selector for
     * an accessibility label.
     *
     * @param accessibilityLabel The accessibility label to be used to search
     * for a WebElement on the page.
     * @return String intended to be used with By#cssSelector.
     */
    private String getAccessibilityCSSSelector(String accessibilityLabel)
    {
        return String.format("[aria-label=\"%s\"]", accessibilityLabel);
    }

    /**
     * Helper for retrieving the selector for the overflow menu.
     *
     * @return <tt>By</tt> to be used by WebDriver to locate the overflow menu.
     */
    private By getOverflowMenuSelector()
    {
        return By.cssSelector(getAccessibilityCSSSelector(OVERFLOW_MENU));
    }

    /**
     * Waits up to 10 seconds for a button to be visible on the toolbar.
     *
     * @param accessibilityLabel The accessibility label of the button to be
     * clicked.
     */
    private void waitForButtonDisplay(String accessibilityLabel)
    {
        TestUtils.waitForElementBy(
            participant.getDriver(),
            By.cssSelector(getAccessibilityCSSSelector(accessibilityLabel)),
            10);
    }

    /**
     * Waits up to 10 seconds for the overflow menu to be visible or not
     * visible.
     *
     * @param visible Whether to wait for the overflow menu to be visible or
     * not visible.
     */
    private void waitForOverFlowMenu(boolean visible) {
        By selector = getOverflowMenuSelector();
        WebDriver driver = participant.getDriver();
        int waitTime = 10;

        if (visible) {
            TestUtils.waitForElementBy(
                driver,
                selector,
                waitTime);
        } else {
            TestUtils.waitForElementNotPresentBy(
                driver,
                selector,
                waitTime);
        }
    }
}
