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
 * @author Hristo Terezov
 */
public class Toolbar {
    /**
     * Button IDs to be used as selectors for finding WebElements within the
     * {@link Toolbar}.
     */
    private final static String AUDIO_MUTE_BUTTON_ID = "toolbar_button_mute";
    private final static String CONTACT_LIST_BUTTON_ID
            = "toolbar_contact_list";
    private final static String DS_BUTTON_ID = "toolbar_button_desktopsharing";
    private final static String ETHERPAD_BUTTON_ID = "toolbar_button_etherpad";
    private final static String FILMSTRIP_ONLY_SETTINGS_BUTTON_ID
        = "toolbar_button_fodeviceselection";
    private final static String HANGUP_BUTTON_ID = "toolbar_button_hangup";
    private final static String INFO_BUTTON_ID = "toolbar_button_info";
    private final static String PROFILE_BUTTON_ID = "toolbar_button_profile";
    private final static String RECORD_BUTTON_ID = "toolbar_button_record";
    private final static String SETTINGS_BUTTON_ID = "toolbar_button_settings";
    private final static String SHARED_VIDEO_BUTTON_ID
        = "toolbar_button_sharedvideo";
    private final static String VIDEO_MUTE_BUTTON_ID  = "toolbar_button_camera";
    private final static String VIDEO_QUALITY_BUTTON_ID
        = "toolbar_button_videoquality";

    /**
     * Xpaths to be used as selectors for finding WebElements within the
     * {@link Toolbar}.
     */
    private final static String DS_BUTTON_XPATH
        = "//a[@id='" + DS_BUTTON_ID + "']";

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
        MeetUIUtils.clickOnButton(
            participant.getDriver(),
            AUDIO_MUTE_BUTTON_ID,
            true);
    }

    /**
     * Clicks on the "desktop sharing" toolbar button. Fails if the button
     * doesn't exist or if the toggled state is not changed after the click.
     */
    public void clickDesktopSharingButton()
    {
        WebDriver driver = participant.getDriver();

        TestUtils.waitForElementByXPath(
            driver,
            DS_BUTTON_XPATH,
            5);

        WebElement el = driver.findElement(By.xpath(DS_BUTTON_XPATH));
        String classNames = el.getAttribute("class");
        boolean isToggled = classNames.contains("toggled");

        MeetUIUtils.clickOnToolbarButton(
            driver,
            DS_BUTTON_ID);

        if (isToggled)
        {
            TestUtils.waitForElementNotContainsClassByXPath(
                driver,
                DS_BUTTON_XPATH,
                "toggled",
                2);
        }
        else
        {
            TestUtils.waitForElementContainsClassByXPath(
                driver,
                DS_BUTTON_XPATH,
                "toggled",
                2);
        }
    }

    /**
     * Clicks on the "chat" toolbar button which opens or closes the chat panel.
     */
    public void clickChatButton()
    {
        MeetUIUtils.clickOnButton(
            participant.getDriver(),
            AUDIO_MUTE_BUTTON_ID,
            true);
    }

    /**
     * Clicks on the contact list toolbar button which opens or closes the
     * contact list.
     */
    public void clickContactListButton()
    {
        MeetUIUtils.clickOnButton(
            participant.getDriver(),
            CONTACT_LIST_BUTTON_ID,
            true);
    }

    /**
     * Clicks on the etherpad toolbar button which shows or hides etherpad.
     */
    public void clickEtherpadButton()
    {
        // waits for etherpad button to be displayed in the toolbar
        TestUtils.waitForDisplayedElementByID(
            participant.getDriver(), ETHERPAD_BUTTON_ID, 15);

        MeetUIUtils.clickOnToolbarButton(
            participant.getDriver(), ETHERPAD_BUTTON_ID);
    }

    /**
     * Clicks on the settings toolbar button which opens the device selection
     * popup.
     */
    public void clickFilmstripOnlySettingsButton()
    {
        MeetUIUtils.clickOnToolbarButton(
            participant.getDriver(),
            FILMSTRIP_ONLY_SETTINGS_BUTTON_ID);
    }

    /**
     * Clicks on the hangup toolbar button which leaves the current conference.
     */
    public void clickHangUpButton()
    {
        MeetUIUtils.clickOnButton(
            participant.getDriver(),
            HANGUP_BUTTON_ID,
            false);
    }

    /**
     * Clicks on the info toolbar button which opens or closes the info dialog.
     */
    public void clickInfoButton()
    {
        MeetUIUtils.clickOnButton(
            participant.getDriver(),
            INFO_BUTTON_ID,
            true);
    }

    /**
     * Clicks on the profile toolbar button which opens or closes the profile
     * panel.
     */
    public void clickProfileButton()
    {
        MeetUIUtils.clickOnToolbarButton(
            participant.getDriver(),
            PROFILE_BUTTON_ID);
    }

    /**
     * Clicks on the recording toolbar button which toggles recording or live
     * streaming.
     */
    public void clickRecordButton()
    {
        MeetUIUtils.clickOnToolbarButton(
            participant.getDriver(),
            RECORD_BUTTON_ID);
    }

    /**
     * Clicks on the settings toolbar button which opens or closes the settings
     * panel.
     */
    public void clickSettingsButton()
    {
        MeetUIUtils.clickOnToolbarButton(
            participant.getDriver(),
            SETTINGS_BUTTON_ID);
    }

    /**
     * Clicks on the shared video toolbar button which toggles sharing of a
     * YouTube video.
     */
    public void clickSharedVideoButton()
    {
        MeetUIUtils.clickOnToolbarButton(
            participant.getDriver(),
            SHARED_VIDEO_BUTTON_ID);
    }

    /**
     * Clicks on the video mute toolbar button which toggles video mute.
     */
    public void clickVideoMuteButton()
    {
        MeetUIUtils.clickOnToolbarButton(
            participant.getDriver(),
            VIDEO_MUTE_BUTTON_ID);
    }

    /**
     * Clicks on the video quality toolbar button which opens or closes the
     * dialog for adjusting max-received video quality.
     */
    public void clickVideoQualityButton()
    {
        MeetUIUtils.clickOnToolbarButton(
            participant.getDriver(),
            VIDEO_QUALITY_BUTTON_ID);
    }

    /**
     * Returns whether or not the recording button is present in the toolbar.
     */
    public boolean hasRecordButton()
    {
        List<WebElement> elements = participant.getDriver().findElements(
            By.id(RECORD_BUTTON_ID));

        return !elements.isEmpty();
    }

    /**
     * Waits up to 10 seconds for the shared video button in the toolbar to be
     * visible.
     */
    public void waitForSharedVideoButtonDisplay()
    {
        TestUtils.waitForDisplayedElementByID(
            participant.getDriver(),
            SHARED_VIDEO_BUTTON_ID,
            10);
    }

    /**
     * Waits up to 10 seconds for the video mute button in the toolbar to be
     * visible.
     */
    public void waitForVideoMuteButtonDisplay()
    {
        TestUtils.waitForDisplayedElementByID(
            participant.getDriver(),
            VIDEO_MUTE_BUTTON_ID,
            10);
    }
}