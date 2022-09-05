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
package com.bipmeet.test;

import com.google.inject.internal.util.$StackTraceElements;
import org.jitsi.meet.test.AudioOnlyTest;
import org.jitsi.meet.test.base.JitsiMeetUrl;
import org.jitsi.meet.test.pageobjects.web.ModalDialogHelper;
import org.jitsi.meet.test.pageobjects.web.Toolbar;
import org.jitsi.meet.test.util.MeetUIUtils;
import org.jitsi.meet.test.util.TestUtils;
import org.jitsi.meet.test.web.WebParticipant;
import org.jitsi.meet.test.web.WebParticipantOptions;
import org.jitsi.meet.test.web.WebTestBase;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.testng.annotations.Test;

/**
 * Tests conference state after entering and exiting audio only mode.
 *
 * @author Leonard Kim
 */
public class BMAudioOnlyTest extends AudioOnlyTest {

    private static String AUDIO_ONLY_ENABLED_LABEL_XPATH
        = "//div[@id='videoResolutionLabel'][contains(@class, 'audio-only')]";
    private static String LARGE_AVATAR_XPATH ="//div[@id='dominantSpeaker']";
   /* private static String VIDEO_QUALITY_SLIDER_CLASS
        = "custom-slider";*/

    @Override
    public void setupClass()
    {
        JitsiMeetUrl url = getJitsiMeetUrl().appendConfig("config.filmstrip.disableStageFilmstrip=true");
        ensureTwoParticipants(url, url, null, null);
        //ensureTwoParticipants(url, url, new WebParticipantOptions().setSkipDisplayNameSet(true), null);
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
     * Verifies that participant1 cannot video unmute while in audio only mode.
     */
    @Test(dependsOnMethods = { "enableAudioOnlyAndCheck" })
    public void videoUnmuteDisabledInAudioOnly()
    {
        getParticipant1().getToolbar().clickAudioMuteButton();

        verifyVideoMute(
            getParticipant1(),
            getParticipant2(),
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
        setAudioOnlyForBipMeet(testee, audioOnly);

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
    public static void setAudioOnlyForBipMeet(WebParticipant participant, boolean audioOnly)
    {
        WebDriver participantDriver = participant.getDriver();
        /*Toolbar toolbar = participant.getToolbar();
        toolbar.clickOverflowButton();*/
        // Open the video quality dialog.

        setVideoQualityDialogVisible(participant, true);
        TestUtils.click(participantDriver,By.xpath("//div[contains(@class,'prefer-btn')]//a[text()='Only Audio']"));
        WebElement okButton = participantDriver.findElement(By.id("modal-dialog-ok-button"));
        okButton.click();
        audioOnly = true;
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
