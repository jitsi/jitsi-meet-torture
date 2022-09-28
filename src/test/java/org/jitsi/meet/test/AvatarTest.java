/*
 * Copyright @ 2015 Atlassian Pty Ltd
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
import org.openqa.selenium.support.ui.*;
import org.testng.annotations.*;

import static org.testng.Assert.*;

/**
 * Tests for users' avatars.
 */
public class AvatarTest
    extends WebTestBase
{
    public static String EMAIL = "support@jitsi.org";
    public static String HASH = "38f014e4b7dde0f64f8157d26a8c812e";

    /**
     * We store participant2's avatar src when running
     * test changeAvatarAndCheck, and in avatarWhenVideoMuted we restart
     * participant2 and the value should be the same.
     * Avatars should not change after a reload.
     */
    private static String participant2AvatarSrc = null;

    @Override
    public void setupClass()
    {
        super.setupClass();

        WebParticipantOptions ops = new WebParticipantOptions().setSkipDisplayNameSet(true);
        ensureTwoParticipants(null, null, ops, ops);
    }

    /**
     * Scenario tests few cases for the avatar to be displayed when user mutes
     * his video.
     */
    @Test(dependsOnMethods = { "changeAvatarAndCheck" })
    public void avatarWhenVideoMuted()
    {
        hangUpAllExceptParticipant1();

        // Start participant1
        WebDriver driver1 = getParticipant1().getDriver();
        String participant1EndpointId = getParticipant1().getEndpointId();

        // Mute participant1's video
        MeetUIUtils.muteVideoAndCheck(getParticipant1(), null);

        // Check if avatar on large video is the same as on local thumbnail
        String participant1ThumbSrc = getLocalThumbnailSrc(driver1);
        final String participant1LargeSrc = getLargeVideoSrc(driver1);
        assertTrue(
            participant1LargeSrc.startsWith(participant1ThumbSrc),
            "invalid avatar on the large video: " + participant1LargeSrc +
                    ", should start with: " + participant1ThumbSrc);

        // Join participant2
        ensureTwoParticipants(null, null, null, new WebParticipantOptions().setSkipDisplayNameSet(true));
        WebDriver driver2 = getParticipant2().getDriver();
        String participant2EndpointId = getParticipant2().getEndpointId();

        // Verify that participant1 is muted from the perspective of
        // participant2
        getParticipant2().getParticipantsPane().assertIsParticipantVideoMuted(getParticipant1(), true);

        // Pin participant1's thumbnail, as participant1 is started muted for
        // participant2, there is no video element, so don't use
        // SwitchVideoTests.clickOnRemoteVideoAndTest which will check for
        // remote video switching on large
        MeetUIUtils.clickOnRemoteVideo(driver2, participant1EndpointId);
        // Check if participant1's avatar is on large video now
        TestUtils.waitForCondition(
            driver2,
            5,
            (ExpectedCondition<Boolean>) d -> {
                String currentSrc = getLargeVideoSrc(d);
                return currentSrc.equals(participant1LargeSrc);
            });

        // participant1 pins participant2's video
        MeetUIUtils.clickOnRemoteVideo(driver1, participant2EndpointId);
        // Check if avatar is displayed on participant1's local video thumbnail
        MeetUIUtils.assertLocalThumbnailShowsAvatar(driver1);
        // Unmute - now local avatar should be hidden and local video displayed
        StopVideoTest stopVideoTest = new StopVideoTest(this);
        stopVideoTest.startVideoOnParticipant1AndCheck();

        getParticipant2().getParticipantsPane().assertIsParticipantVideoMuted(getParticipant1(), false);

        MeetUIUtils.assertLocalThumbnailShowsVideo(driver1);

        // Now both participant1 and participant2 have video muted
        MeetUIUtils.muteVideoAndCheck(getParticipant1(), getParticipant2());
        getParticipant2().getParticipantsPane().assertIsParticipantVideoMuted(getParticipant1(), true);

        MeetUIUtils.muteVideoAndCheck(getParticipant2(), getParticipant1());
        getParticipant1().getParticipantsPane().assertIsParticipantVideoMuted(getParticipant2(), true);
        // we check whether avatar of participant2 is same on both sides
        // and we check whether it had changed after reloading the page
        assertEquals(
            getLocalThumbnailSrc(driver2),
            participant2AvatarSrc);
        assertEquals(
            getThumbnailSrc(driver1, participant2EndpointId),
            participant2AvatarSrc);

        // Start the third participant
        ensureThreeParticipants();

        // Temporary wait few seconds so the avatar to be downloaded from libravatar
        // Seems there is some delay when requesting it
        TestUtils.waitMillis(3000);

        WebDriver driver3 = getParticipant3().getDriver();

        String participant2Src = getLocalThumbnailSrc(driver2);

        // Pin local video and verify avatars are displayed
        MeetUIUtils.clickOnLocalVideo(driver3);

        MeetUIUtils.assertAvatarDisplayed(driver3, participant1EndpointId);
        MeetUIUtils.assertAvatarDisplayed(driver3, participant2EndpointId);

        assertEquals(
            getThumbnailSrc(driver3, participant2EndpointId),
            participant2Src);
        assertEquals(
            getThumbnailSrc(driver3, participant1EndpointId),
            participant1ThumbSrc);

        // Click on participant1's video
        MeetUIUtils.clickOnRemoteVideo(driver3, participant1EndpointId);
        // His avatar should be on large video and
        // display name instead of an avatar, local video displayed
        MeetUIUtils.waitsForLargeVideoSwitch(driver3, participant1EndpointId);
        MeetUIUtils.assertDisplayNameVisible(driver3, participant1EndpointId);
        MeetUIUtils.assertAvatarDisplayed(driver3, participant2EndpointId);
        MeetUIUtils.assertLocalThumbnailShowsVideo(driver3);

        // Click on participant2's video
        MeetUIUtils.clickOnRemoteVideo(driver3, participant2EndpointId);
        MeetUIUtils.waitsForLargeVideoSwitch(driver3, participant2EndpointId);
        MeetUIUtils.assertDisplayNameVisible(driver3, participant2EndpointId);
        MeetUIUtils.assertAvatarDisplayed(driver3, participant1EndpointId);
        MeetUIUtils.assertLocalThumbnailShowsVideo(driver3);

        getParticipant3().hangUp();

        TestUtils.waitMillis(1500);

        // Unmute participant1's and participant2's videos
        stopVideoTest.startVideoOnParticipant1AndCheck();
        getParticipant2().getParticipantsPane().assertIsParticipantVideoMuted(getParticipant1(), false);
        stopVideoTest.startVideoOnParticipantAndCheck();
        getParticipant2().getParticipantsPane().assertIsParticipantVideoMuted(getParticipant1(), false);
    }

    /**
     * Check persistence of email.
     * Checks that email was set, reload and check again.
     */
    @Test(dependsOnMethods = { "avatarWhenVideoMuted" })
    public void testEmailPersistence()
    {
        // This test is very often failing on FF (due to a crash on hangup
        // present in FF57)
        // will disable it for now
        if (getParticipant1().getType().isFirefox())
        {
            return;
        }

        getParticipant1().getToolbar().clickProfileButton();

        String currentEmailValue
            = getParticipant1().getSettingsDialog().getEmail();

        assertEquals(
            currentEmailValue,
            EMAIL,
            "Current email has wrong value");

        getParticipant1().hangUp();

        ensureTwoParticipants(null, null, new WebParticipantOptions().setSkipDisplayNameSet(true), null);

        getParticipant1().getToolbar().clickProfileButton();
        currentEmailValue
            = getParticipant1().getSettingsDialog().getEmail();

        assertEquals(
            currentEmailValue,
            EMAIL,
            "Current email has wrong value after reload");
    }

    /**
     *  Changes the avatar for one participant and checks if it has changed
     *  properly everywhere
     */
    @Test
    public void changeAvatarAndCheck()
    {
        WebParticipant participant1 = getParticipant1();
        WebParticipant participant2 = getParticipant2();
        final WebDriver driver1 = participant1.getDriver();
        final WebDriver driver2 = participant2.getDriver();

        final String participant1EndpointId = participant1.getEndpointId();

        String participant1AvatarXPath = MeetUIUtils.getAvatarXpathForParticipant(participant1EndpointId);

        // Wait for the avatar element to be created
        TestUtils.waitForElementByXPath(
            driver2, participant1AvatarXPath, 20);

        final String srcOneSecondParticipant =
            getSrcByXPath(driver2, participant1AvatarXPath);

        // change the email for participant1
        participant1.getToolbar().clickProfileButton();

        SettingsDialog settingsDialog = participant1.getSettingsDialog();
        settingsDialog.waitForDisplay();
        settingsDialog.setEmail(EMAIL);
        settingsDialog.submit();

        //check if the local avatar in the settings menu has changed
        TestUtils.waitForCondition(
            driver1,
            5,
            (ExpectedCondition<Boolean>) d
                -> participant1.getToolbar()
                        .getProfileImage()
                        .getAttribute("src")
                        .contains(HASH));

        //check if the avatar in the local thumbnail has changed
        checkSrcIsCorrect(getLocalThumbnailSrc(driver1));
        //check if the avatar in the contact list has changed
        //checkSrcIsCorrect(
        // getContactSrc(participant1, participant1.getEndpointId()));

        // waits till the src changes so we can continue with the check
        // sometimes the notification for the avatar change can be more
        // than 5 seconds
        TestUtils.waitForCondition(
            driver2,
            15,
            (ExpectedCondition<Boolean>) d -> {
                String currentSrc =
                    getThumbnailSrc(driver2, participant1EndpointId);
                return !currentSrc.equals(srcOneSecondParticipant);
            });

        //check if the avatar in the thumbnail for the other participant has
        // changed
        checkSrcIsCorrect(
            getThumbnailSrc(driver2, participant1EndpointId));
        //check if the avatar in the contact list has changed for the other
        // participant
        //checkSrcIsCorrect(
        //  getContactSrc(participant2, participant1.getEndpointId()));
        //check if the avatar displayed on large video has changed for the other
        // participant
        TestUtils.waitForCondition(
            driver2,
            5,
            (ExpectedCondition<Boolean>) d -> {
                String currentSrc = getLargeVideoSrc(driver2);
                return currentSrc.contains(HASH);
            });

        // we check whether avatar of participant2 is same on both sides
        // and we stored to check it after reload
        participant2AvatarSrc = getLocalThumbnailSrc(driver2);
        String participant2EndpointId = participant2.getEndpointId();
        assertEquals(
            getThumbnailSrc(driver1, participant2EndpointId),
            participant2AvatarSrc);

        // the problem on FF where we can send keys to the input field,
        // and the m from the text can mute the call, check whether we are muted
        getParticipant2().getFilmstrip()
            .assertAudioMuteIcon(getParticipant1(), false);
    }

    /**
     * Checks if the element with the given xpath has the correct src attribute
     */
    private void checkSrcIsCorrect(String src)
    {
        assertTrue(
            src.contains(HASH),
            "The avatar invalid src: " + src +" hash: " + HASH);
    }

    /**
     * Return the participant avatar src that we will check
     * @return the participant avatar src that we will check
     */
    private static String getSrcByXPath(WebDriver driver, String xpath)
    {
        return driver.findElement(By.xpath(xpath)).getAttribute("src");
    }

    /**
     * Gets avatar SRC attribute for the one displayed on small video thumbnail.
     * @param perspective where are we checking this ?
     * @param endpointId resource part of the JID which belongs to the user for
     *                    whom we want to obtain avatar src
     * @return string value of avatar's 'src' attribute
     */
    public static String getThumbnailSrc(WebDriver perspective, String endpointId)
    {
        return getSrcByXPath(perspective, MeetUIUtils.getAvatarXpathForParticipant(endpointId));
    }

    /**
     * Gets avatar SRC attribute for the one displayed on local video thumbnail.
     * @param perspective where are we checking this ?
     * @return string value of avatar's 'src' attribute
     */
    public static String getLocalThumbnailSrc(WebDriver perspective)
    {
        return getSrcByXPath(perspective, MeetUIUtils.getAvatarXpathForLocal());
    }

    /**
     * Gets avatar SRC attribute for the one displayed on large video.
     * @param perspective where are we checking this ?
     * @return string value of avatar's 'src' attribute
     */
    public static String getLargeVideoSrc(WebDriver perspective)
    {
        return getSrcByXPath(
            perspective,
            "//img[@id='dominantSpeakerAvatar']");
    }
}
