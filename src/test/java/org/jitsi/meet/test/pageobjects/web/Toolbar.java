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
import org.openqa.selenium.interactions.*;

import java.sql.Driver;
import java.util.*;

/**
 * Represents the toolbar in a particular {@link WebParticipant}.
 *
 * @author Leonard Kim
 */
public class Toolbar
{
    /**
     * Accessibility labels to be used as selectors for finding WebElements
     * within the {@link Toolbar}.
     */
    public final static String AUDIO_MUTE = "Mute / Unmute";
    public final static String CHAT = "Toggle chat window";
    public final static String DESKTOP = "Start / Stop sharing your screen";
    public final static String EMBED_MEETING = "Embed meeting";
    public final static String ETHERPAD = "Toggle shared document";
    public final static String FULLSCREEN = "Toggle full screen";
    public final static String HANGUP = "Leave the meeting";
    public final static String HELP = "Help";
    public final static String INVITE = "Invite people";
    public final static String INVITE_CSS = "Invite Someone";
    public final static String LIVE_STREAM = "Live Stream";
    public final static String MUTE_EVERYONE_AUDIO = "Mute everyone";
    public final static String MUTE_EVERYONE_VIDEO = "Stop everyone's video";
    public final static String OVERFLOW = "Toggle more actions menu";
    public final static String OVERFLOW_MENU = "Toggle more actions menu" ;
    public final static String PARTICIPANTS = "Participants" ;
    public final static String PROFILE = "Edit your profile";
    public final static String RAISE_HAND = "Raise / Lower your hand";
    public final static String RECORD = "Toggle recording";
    public final static String SECURITY = "Security options";
    public final static String SELECT_BACKGROUND = "Select Background";
    public final static String SETTINGS = "Toggle settings";
    public final static String SHARE_AUDIO = "Share audio";
    public final static String SHARE_VIDEO = "Toggle video sharing";
    public final static String SHORTCUTS = "Toggle shortcuts";
    public final static String STATS = "Toggle speaker statistics";
    public final static String TILE_VIEW_BUTTON = "Toggle tile view";
    public final static String VIDEO_MUTE = "Start / Stop camera";
    public final static String VIDEO_QUALITY = "Manage video quality";


    /**
     * The ID of the toolbar. To be used as a selector when trying to locate
     * the toolbar on the page.
     */
    private final static String TOOLBOX_ID = "new-toolbox";

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
     * Clicks on the raise/lower hand button that enables participants will to speak.
     */
    public String clickRaiseHandButton()
    {
        return clickButton(RAISE_HAND);
    }

    /**
     * Clicks on the "desktop sharing" toolbar button. Fails if the button
     * doesn't exist or if the toggled state is not changed after the click.
     */
    public void clickDesktopSharingButton()
    {
        // Firefox require user gesture for getDisplayMedia()
        // https://bugzilla.mozilla.org/show_bug.cgi?id=1580944
        // we us js to click buttons as we have seen some stale elements exceptions due to react updates
        if (this.participant.getType().isFirefox())
        {
            WebElement button = participant.getDriver().findElement(By.cssSelector(
                MeetUIUtils.getAccessibilityCSSSelector(DESKTOP)));
            new Actions(this.participant.getDriver()).moveToElement(button).click().perform();
        }
        else
        {
            clickButton(DESKTOP);
        }
    }

    /**
     * Clicks on the "chat" toolbar button which opens or closes the chat panel.
     */
    public void clickChatButton()
    {
        clickButton(CHAT);
    }

    public void bipmeetLoseFocus(){

        WebDriver driver = participant.getDriver();
        WebElement inputBox = driver.findElement(By.id("usermsg"));
        ((JavascriptExecutor)driver).executeScript(
                "arguments[0].dispatchEvent(new Event('blur'))",
                inputBox);
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
    public void clickParticipantsButton()
    {
        clickButton(PARTICIPANTS);
    }

    /**
     * Returns the invite button.
     * @return the invite button.
     */
    public WebElement getInviteButton()
    {
        return participant.getDriver().findElement(By.cssSelector(MeetUIUtils.getAccessibilityCSSSelector(INVITE_CSS)));
    }

    /**
     * Clicks on the overflow toolbar button which opens or closes the overflow
     * menu.
     */
    public void clickOverflowButton()
    {
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
     * Clicks on the live-streaming toolbar button which toggles live-streaming.
     */
    public void clickLiveStreamingButton()
    {
        clickButtonInOverflowMenu(LIVE_STREAM);
    }

    /**
     * Clicks on the security toolbar button which opens or closes the security dialog.
     */
    public void clickSecurityButton()
    {
        clickButtonInOverflowMenu(SECURITY);
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
            MeetUIUtils.getAccessibilityCSSSelector(PROFILE) + " img"));
    }

    /**
     * Checks whether the recording button is present in the toolbar.
     *
     * @return True if the recording button is present, false if it is not.
     */
    public boolean hasRecordButton()
    {
        openOverflowMenu();

        List<WebElement> elements
            = participant.getDriver().findElements(By.cssSelector(
                MeetUIUtils.getAccessibilityCSSSelector(RECORD)));

        return !elements.isEmpty();
    }

    /**
     * Checks whether the live-streaming button is present in the toolbar.
     *
     * @return True if the live-streaming button is present, false if it is not.
     */
    public boolean hasLiveStreamButton()
    {
        openOverflowMenu();

        List<WebElement> elements
            = participant.getDriver().findElements(By.cssSelector(
            MeetUIUtils.getAccessibilityCSSSelector(RECORD)));

        return !elements.isEmpty();
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
     * Waits up to 10 seconds for the audio mute button in the toolbar to be
     * visible.
     */
    public void waitForAudioMuteButtonDisplay()
    {
        waitForButtonDisplay(AUDIO_MUTE);
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
            MeetUIUtils.getAccessibilityCSSSelector(accessibilityLabel),
            false
        );
    }

    /**
     * Try to click on a button and fail if it cannot be clicked.
     *
     * @param accessibilityLabel The accessibility label of the button to be
     * clicked.
     * @return the css-selector String.
     */
    private String clickButton(String accessibilityLabel)
    {
        String selector = MeetUIUtils.getAccessibilityCSSSelector(accessibilityLabel);

        MeetUIUtils.clickOnElement(participant.getDriver(), selector, true);

        return selector;
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
     * Helper for retrieving the selector for the overflow menu.
     *
     * @return <tt>By</tt> to be used by WebDriver to locate the overflow menu.
     */
    private By getOverflowMenuSelector()
    {
        return By.cssSelector(MeetUIUtils.getAccessibilityCSSSelector(OVERFLOW_MENU));
    }

    /**
     * Waits up to 10 seconds for a button to be visible on the toolbar.
     *
     * @param accessibilityLabel The accessibility label of the button to be
     * clicked.
     */
    private void waitForButtonDisplay(String accessibilityLabel)
    {
        WebElement button = TestUtils.waitForElementBy(
            participant.getDriver(),
            By.cssSelector(MeetUIUtils.getAccessibilityCSSSelector(accessibilityLabel)),
            10);

        participant.consolePrint(getClass().getSimpleName(),
            "Button '" + accessibilityLabel + "' is pressed:" + button.getAttribute("aria-pressed"));
    }

    /**
     * Waits up to 10 seconds for the overflow menu to be visible or not
     * visible.
     *
     * @param visible Whether to wait for the overflow menu to be visible or
     * not visible.
     */
    private void waitForOverFlowMenu(boolean visible)
    {
        By selector = getOverflowMenuSelector();
        WebDriver driver = participant.getDriver();
        int waitTime = 10;

        if (visible)
        {
            TestUtils.waitForElementBy(
                driver,
                selector,
                waitTime);
        }
        else
        {
            TestUtils.waitForElementNotPresentBy(
                driver,
                selector,
                waitTime);
        }
    }

    /**
     * Checks whether a button is available.
     * @param id the id of the button (accessibility label).
     * @return true if the button is available.
     */
    public boolean hasButton(String id)
    {
        List<WebElement> elements
            = participant.getDriver().findElements(By.cssSelector(MeetUIUtils.getAccessibilityCSSSelector(id)));

        return !elements.isEmpty();
    }

    /**
     * Returns the number of buttons in the main toolbar.
     * @return the number of buttons in the main toolbar.
     */
    public int mainToolbarButtonsCount()
    {
        return participant.getDriver().findElements(By.xpath(
            "//div[contains(@class,'toolbox-content')]//div[contains(@class,'toolbox-content-items')]/div")).size();
    }

    /**
     * Returns the number of buttons in the overflow menu.
     * @return the number of buttons in the overflow menu.
     */
    public int overFlowMenuButtonsCount()
    {
        // if there is no overflow menu there are no items in it
        if (!hasButton(OVERFLOW))
        {
            return 0;
        }

        this.openOverflowMenu();

        int count = participant.getDriver().findElements(By.xpath("//ul[@id='overflow-menu']//li")).size();

        this.closeOverflowMenu();

        return count;
    }
}
