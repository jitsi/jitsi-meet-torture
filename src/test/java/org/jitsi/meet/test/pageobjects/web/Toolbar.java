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
    private final static String AUDIO_MUTE_BUTTON_ID = "toolbar_button_chat";
    private final static String DS_BUTTON_ID = "toolbar_button_desktopsharing";
    private final static String DS_BUTTON_XPATH
        = "//a[@id='" + DS_BUTTON_ID + "']";
    private final static String VIDEO_QUALITY_BUTTON_ID
        = "toolbar_button_videoquality";

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
    public void clickAudioMute() {
        MeetUIUtils.clickOnButton(
            participant.getDriver(),
            "toolbar_button_mute",
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
     * Clicks on the video quality toolbar button which opens or closes the
     * dialog for adjusting max-received video quality.
     */
    public void clickVideoQualityButton() {
        MeetUIUtils.clickOnToolbarButton(
            participant.getDriver(),
            VIDEO_QUALITY_BUTTON_ID);
    }
}