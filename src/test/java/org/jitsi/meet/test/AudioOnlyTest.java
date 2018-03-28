/*
 * Copyright @ 2017 Atlassian Pty Ltd
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

import org.jitsi.meet.test.util.*;
import org.jitsi.meet.test.web.*;

import org.openqa.selenium.*;
import org.testng.annotations.*;

/**
 * Tests conference state after entering and exiting audio only mode.
 *
 * @author Leonard Kim
 */
public class AudioOnlyTest
    extends WebTestBase
{
    private static String AUDIO_ONLY_ENABLED_ICON_XPATH
        = "//div[@id='videoResolutionLabel']" +
            "//i[contains(@class, 'icon-visibility-off')]";
    private static String LARGE_AVATAR_XPATH ="//div[@id='dominantSpeaker']";
    private static String MUTE_BUTTON_ID = "toolbar_button_camera";
    private static String VIDEO_QUALITY_SLIDER_CLASS
        = "video-quality-dialog-slider";

    @Override
    public void setupClass()
    {
        super.setupClass();

        ensureTwoParticipants();
    }

    /**
     * Enables audio only mode for participant1 and verifies that the other
     * participant sees participant1 as video muted.
     */
    @Test
    public void enableAudioOnlyAndCheck()
    {
        setAudioOnlyAndCheck(
            getParticipant1(),
            "participant1",
            getParticipant2(),
            true);
    }

    /**
     * Verifies that participant1 cannot video unmute while in audio only mode.
     */
    @Test(dependsOnMethods = { "enableAudioOnlyAndCheck" })
    public void videoUnmuteDisabledInAudioOnly()
    {
        getParticipant1().getToolbar().clickAudioMuteButton();

        verifyVideoMute(
            getParticipant1().getDriver(),
            "participant1",
            getParticipant2().getDriver(),
            true);
    }

    /**
     * Verifies that participant1 sees avatars for itself and other participants.
     */
    @Test(dependsOnMethods = { "videoUnmuteDisabledInAudioOnly" })
    public void avatarsDisplayForParticipants()
    {
        WebDriver driver1 = getParticipant1().getDriver();

        TestUtils.waitForDisplayedElementByXPath(
            driver1, LARGE_AVATAR_XPATH, 2);

        MeetUIUtils.assertLocalThumbnailShowsAvatar(driver1);
    }

    /**
     * Disables audio only mode and verifies that both participant1 and
     * participant2 participant see participant1 as not video muted.
     */
    @Test(dependsOnMethods = { "avatarsDisplayForParticipants" })
    public void disableAudioOnlyAndCheck()
    {
        setAudioOnlyAndCheck(
            getParticipant1(),
            "participant1",
            getParticipant2(),
            false);
    }

    /**
     * Toggles the audio only state of a specific Meet conference participant
     * and verifies participant sees the audio only label and that a specific
     * other Meet conference participant sees a specific video mute state for
     * the former.
     *
     * @param testee the {@code WebParticipant} which represents the Meet
     * conference participant whose audio only state is to be toggled
     * @param testeeName the name of {@code testee} to be displayed
     * should the test fail
     * @param observer the {@code WebParticipant} which represents the Meet
     * conference participant to verify the mute state of {@code testee}
     * @param audioOnly the audio only state of {@code testee} expected to be
     * observed by {@code observer}
     */
    private void setAudioOnlyAndCheck(
        WebParticipant testee,
        String testeeName,
        WebParticipant observer,
        boolean audioOnly)
    {
        setAudioOnly(testee, audioOnly);

        verifyVideoMute(
            testee.getDriver(), testeeName, observer.getDriver(), audioOnly);

        TestUtils.waitForDisplayedOrNotByXPath(
            testee.getDriver(),
            AUDIO_ONLY_ENABLED_ICON_XPATH,
            5,
            audioOnly);
    }

    /**
     * Shows the video quality menu in the from the toolbar and sets audio only
     * mode to either on or off.
     *
     * @param participant the {@code WebParticipant}.
     * @param audioOnly whether or not audio only mode should be enabled.
     */
    private void setAudioOnly(WebParticipant participant, boolean audioOnly)
    {
        WebDriver participantDriver = participant.getDriver();

        // Open the video quality dialog.
        setVideoQualityDialogVisible(participant, true);

        // Calculate how far to move the quality slider and in which direction.
        WebElement videoQualitySlider = participantDriver.findElement(
            By.className(VIDEO_QUALITY_SLIDER_CLASS));

        int audioOnlySliderValue
            = Integer.parseInt(videoQualitySlider.getAttribute("min"));
        int maxDefinitionSliderValue
            = Integer.parseInt(videoQualitySlider.getAttribute("max"));

        int activeValue
            = Integer.parseInt(videoQualitySlider.getAttribute("value"));
        int targetValue
            = audioOnly ? audioOnlySliderValue : maxDefinitionSliderValue;

        int distanceToTargetValue = targetValue - activeValue;
        Keys keyDirection = distanceToTargetValue > 0 ? Keys.RIGHT : Keys.LEFT;

        // Move the slider to the target value.
        for (int i = 0; i < Math.abs(distanceToTargetValue); i++)
        {
            videoQualitySlider.sendKeys(keyDirection);
        }

        // Close the video quality dialog.
        setVideoQualityDialogVisible(participant, false);
    }

    /**
     * Sets whether or not the video quality dialog is displayed.
     *
     * @param participant the {@code WebParticipant}.
     * @param visible whether or not the dialog should be displayed.
     */
    private void setVideoQualityDialogVisible(
        WebParticipant participant, boolean visible)
    {
        boolean isDialogSliderVisible
            = !participant.getDriver().findElements(
                By.className(VIDEO_QUALITY_SLIDER_CLASS)).isEmpty();

        if ((isDialogSliderVisible && !visible)
            || (!isDialogSliderVisible && visible))
        {
            participant.getToolbar().clickVideoQualityButton();
        }
    }

    /**
     * Verifies that a specific other Meet conference participant sees a
     * specific mute state for the former.
     *
     * @param testee the {@code WebDriver} which represents the Meet conference
     * participant whose mute state is to be toggled
     * @param testeeName the name of {@code testee} to be displayed should the
     * test fail
     * @param observer the {@code WebDriver} which represents the Meet
     * conference participant to verify the mute state of {@code testee}
     * @param muted the mute state of {@code testee} expected to be observed by
     * {@code observer}
     */
    private void verifyVideoMute(
        WebDriver testee,
        String testeeName,
        WebDriver observer,
        boolean muted
    ) {
        // Verify the observer sees the testee in the desired muted state.
        MeetUIUtils.assertMuteIconIsDisplayed(
                observer,
                testee,
                muted,
                true, // checking video mute
                testeeName);

        // Verify the testee sees itself in the desired muted state.
        MeetUIUtils.assertMuteIconIsDisplayed(
                testee,
                testee,
                muted,
                true, // checking video mute
                testeeName);
    }
}
