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

import org.jitsi.meet.test.base.JitsiMeetUrl;
import org.jitsi.meet.test.pageobjects.web.*;
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
    private static String AUDIO_ONLY_ENABLED_LABEL_XPATH
        = "//div[@id='videoResolutionLabel'][contains(@class, 'audio-only')]";
    private static String LARGE_AVATAR_XPATH ="//div[@id='dominantSpeaker']";
    private static String VIDEO_QUALITY_SLIDER_CLASS
        = "custom-slider";

    @Override
    public void setupClass()
    {
        super.setupClass();

        JitsiMeetUrl url = getJitsiMeetUrl().appendConfig("config.filmstrip.disableStageFilmstrip=true");
        ensureTwoParticipants(url, url, new WebParticipantOptions().setSkipDisplayNameSet(true), null);
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
            getParticipant2(),
            true);
    }

    /**
     * Verifies that participant1 sees avatars for itself and other participants.
     */
    @Test(dependsOnMethods = { "enableAudioOnlyAndCheck" })
    public void avatarsDisplayForParticipants()
    {
        WebDriver driver1 = getParticipant1().getDriver();

        TestUtils.waitForDisplayedElementByXPath(
            driver1, LARGE_AVATAR_XPATH, 2);

        MeetUIUtils.assertLocalThumbnailShowsAvatar(driver1);
    }

    /**
     * Disables audio only mode and verifies that both participant1 and participant2 see participant1 as not video
     * muted.
     */
    @Test(dependsOnMethods = { "avatarsDisplayForParticipants" })
    public void disableAudioOnlyAndCheck()
    {
        setAudioOnlyAndCheck(
            getParticipant1(),
            getParticipant2(),
            false);
    }

    /**
     * Mutes video on participant1, toggles audio-only twice and then verifies if both participants see participant1
     * as video muted.
     */
    @Test(dependsOnMethods = { "disableAudioOnlyAndCheck"})
    public void testAudioOnlyAfterMuted()
    {
        // Mute video on participant1.
        getParticipant1().getToolbar().clickVideoMuteButton();

        verifyVideoMute(getParticipant1(), getParticipant2(), true);

        // Enable audio-only mode.
        setAudioOnlyAndCheck(getParticipant1(), getParticipant2(), true);

        // Disable audio-only mode.
        setAudioOnly(getParticipant1(), false);

        // Participant1 should stay muted since it was muted before audio-only was enabled.
        verifyVideoMute(getParticipant1(), getParticipant2(), true);
    }

    /**
     * Unmutes video on participant1 and verifies both participants see participant1 as not video muted.
     */
    @Test(dependsOnMethods = { "testAudioOnlyAfterMuted" })
    public void unmuteAfterAudioOnlyDisabled()
    {
        // Unmute video on participant1.
        getParticipant1().getToolbar().clickVideoMuteButton();

        verifyVideoMute(getParticipant1(), getParticipant2(), false);
    }

    /**
     * Toggles the audio only state of a specific Meet conference participant
     * and verifies participant sees the audio only label and that a specific
     * other Meet conference participant sees a specific video mute state for
     * the former.
     *
     * @param testee the {@code WebParticipant} which represents the Meet
     * conference participant whose audio only state is to be toggled
     * @param observer the {@code WebParticipant} which represents the Meet
     * conference participant to verify the mute state of {@code testee}
     * @param audioOnly the audio only state of {@code testee} expected to be
     * observed by {@code observer}
     */
    private void setAudioOnlyAndCheck(
        WebParticipant testee,
        WebParticipant observer,
        boolean audioOnly)
    {
        setAudioOnly(testee, audioOnly);

        verifyVideoMute(testee, observer, audioOnly);

        TestUtils.waitForDisplayedOrNotByXPath(
            testee.getDriver(),
            AUDIO_ONLY_ENABLED_LABEL_XPATH,
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
    public static void setAudioOnly(WebParticipant participant, boolean audioOnly)
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
    private static void setVideoQualityDialogVisible(WebParticipant participant, boolean visible)
    {
        if (visible)
        {
            participant.getToolbar().clickVideoQualityButton();
        }
        else
        {
            // click close button
            ModalDialogHelper.clickCloseButton(participant.getDriver());
        }
    }

    /**
     * Verifies that a specific other Meet conference participant sees a
     * specific mute state for the former.
     *
     * @param testee the {@code WebParticipant} which represents the Meet
     * conference participant whose mute state is to be toggled
     * @param observer the {@code WebParticipant} which represents the Meet
     * conference participant to verify the mute state of {@code testee}
     * @param muted the mute state of {@code testee} expected to be observed by
     * {@code observer}
     */
    private void verifyVideoMute(
        WebParticipant testee,
        WebParticipant observer,
        boolean muted)
    {
        // Verify the observer sees the testee in the desired muted state.
        observer.getParticipantsPane().assertIsParticipantVideoMuted(testee, muted);

        // Verify the testee sees itself in the desired muted state.
        testee.getParticipantsPane().assertIsParticipantVideoMuted(testee, muted);
    }
}
