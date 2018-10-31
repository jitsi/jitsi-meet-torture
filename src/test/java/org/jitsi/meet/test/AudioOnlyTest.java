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

import org.jitsi.meet.test.pageobjects.web.*;
import org.jitsi.meet.test.util.*;
import org.jitsi.meet.test.web.*;

import org.testng.annotations.*;

/**
 * Tests conference state after entering and exiting audio only mode.
 *
 * @author Leonard Kim
 */
public class AudioOnlyTest
    extends WebTestBase
{
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
        getParticipant1().getLargeVideo().waitForAvatarToDisplay();

        MeetUIUtils.assertLocalThumbnailShowsAvatar(
            getParticipant1().getDriver());
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
        setAudioOnly(testee, audioOnly);

        verifyVideoMute(testee, observer, audioOnly);

        testee.getStatusLabels().waitForAudioOnlyDisplayStatus(audioOnly);
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
        VideoQualityDialog videoQualityDialog
            = participant.getVideoQualityDialog();

        videoQualityDialog.open();

        videoQualityDialog.setAudioOnly(audioOnly);

        videoQualityDialog.close();
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
        boolean muted
    ) {
        // Verify the observer sees the testee in the desired muted state.
        observer.getFilmstrip().assertVideoMuteIcon(testee, muted);

        // Verify the testee sees itself in the desired muted state.
        testee.getFilmstrip().assertVideoMuteIcon(testee, muted);
    }
}
