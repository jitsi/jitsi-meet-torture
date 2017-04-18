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

import junit.framework.*;
import org.jitsi.meet.test.util.*;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.*;

/**
 * Tests conference state after entering audio only mode and after existing
 * audio only mode.
 *
 * @author Leonard Kim
 */
public class AudioOnlyTest
        extends TestCase
{
    /**
     * Constructs test
     * @param name the method name for the test.
     */
    public AudioOnlyTest(String name)
    {
        super(name);
    }

    /**
     * Orders the tests.
     * @return the suite with order tests.
     */
    public static junit.framework.Test suite()
    {
        TestSuite suite = new TestSuite();

        suite.addTest(
            new AudioOnlyTest("enableAudioOnlyAndCheck"));
        suite.addTest(
            new AudioOnlyTest("videoUnmuteDisabledInAudioOnly"));
        suite.addTest(
            new AudioOnlyTest("avatarsDisplaysForParticipants"));
        suite.addTest(
            new AudioOnlyTest("disableAudioOnlyAndCheck"));

        return suite;
    }

    /**
     * Enables audio only mode for the owner and verifies the other participant
     * sees the owner as video muted.
     */
    public void enableAudioOnlyAndCheck()
    {
        toggleAudioOnlyAndCheck(
            ConferenceFixture.getOwner(),
            "owner",
            ConferenceFixture.getSecondParticipant(),
            true);

        TestUtils.waitMillis(2000);
    }

    /**
     * Verifies the owner cannot video unmute while in audio only mode.
     */
    public void videoUnmuteDisabledInAudioOnly() {
        MeetUIUtils.clickOnToolbarButton(ConferenceFixture.getOwner(),
            "toolbar_button_camera");

        verifyVideoMute(
            ConferenceFixture.getOwner(),
            "owner",
            ConferenceFixture.getSecondParticipant(),
            true);
    }

    /**
     * Verifies the owner sees itself and other participants as avatars.
     */
    public void avatarsDisplaysForParticipants() {
        WebDriver owner = ConferenceFixture.getOwner();
        String largeAvatarXPath = "//div[@id='dominantSpeaker']";

        TestUtils.waitForDisplayedElementByXPath(owner, largeAvatarXPath, 2);

        MeetUIUtils.assertLocalThumbnailShowsAvatar(owner);
    }

    /**
     * Disables audio only mode and verifies both the owner and the other
     * participant see the owner as not video muted.
     */
    public void disableAudioOnlyAndCheck() {
        toggleAudioOnlyAndCheck(
            ConferenceFixture.getOwner(),
            "owner",
            ConferenceFixture.getSecondParticipant(),
            false);
    }

    /**
     * Toggles the audio only state of a specific Meet conference participant
     * and verifies participant sees the audio only label and that a specific
     * other Meet conference participants sees a specific video mute state for
     * the former.
     *
     * @param testee the {@code WebDriver} which represents the Meet conference
     * participant whose audio only state is to be toggled
     * @param testeeName the name of {@code testee} to be displayed
     * should the test fail
     * @param observer the {@code WebDriver} which represents the Meet
     * conference participant to verify the mute state of {@code testee}
     * @param muted the mute state of {@code testee} expected to be observed by
     * {@code observer}
     */
    private void toggleAudioOnlyAndCheck(
        WebDriver testee,
        String testeeName,
        WebDriver observer,
        boolean muted)
    {
        MeetUIUtils.clickOnToolbarButton(
            testee, "toolbar_button_audioonly");

        verifyVideoMute(
            testee, testeeName, observer, muted);

        TestUtils.waitForDisplayedOrNotByXPath(
            testee,
            "//div[contains(@class, 'audio-only-label')]",
            5,
            muted);
    }

    /**
     * Toggles the mute state of a specific Meet conference participant and
     * verifies that a specific other Meet conference participants sees a
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
