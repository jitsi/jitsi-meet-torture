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

import org.jitsi.meet.test.base.*;
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
    public static String EMAIL = "example@jitsi.org";
    public static String HASH = "dc47c9b1270a4a25a60bab7969e7632d";

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

        ensureTwoParticipants();
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
        MeetUIUtils.muteVideoAndCheck(driver1, null);

        // Check if avatar on large video is the same as on local thumbnail
        String participant1ThumbSrc = getLocalThumbnailSrc(driver1);
        final String participant1LargeSrc = getLargeVideoSrc(driver1);
        assertTrue(
            participant1LargeSrc.startsWith(participant1ThumbSrc),
            "invalid avatar on the large video: " + participant1LargeSrc +
                    ", should start with: " + participant1ThumbSrc);

        // Join participant2
        ensureTwoParticipants();
        WebDriver driver2 = getParticipant2().getDriver();
        String participant2EndpointId = getParticipant2().getEndpointId();

        // Verify that participant1 is muted from the perspective of
        // participant2
        MeetUIUtils.assertMuteIconIsDisplayed(
                driver2,
                driver1,
                true,
                true, //video
                "participant1");
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

        MeetUIUtils.assertMuteIconIsDisplayed(
                driver2,
                driver1,
                false,
                true, //video
                "participant1");
        MeetUIUtils.assertLocalThumbnailShowsVideo(driver1);

        // Now both participant1 and participant2 have video muted
        MeetUIUtils.muteVideoAndCheck(driver1, driver2);
        MeetUIUtils.assertMuteIconIsDisplayed(
                driver2,
                driver1,
                true,
                true, //video
                "participant1");
        MeetUIUtils.muteVideoAndCheck(driver2, driver1);
        MeetUIUtils.assertMuteIconIsDisplayed(
                driver1,
                driver2,
                true,
                true, //video
                "participant2");

        // we check whether avatar of participant2 is same on both sides
        // and we check whether it had changed after reloading the page
        assertEquals(
            participant2AvatarSrc,
            getLocalThumbnailSrc(driver2));
        assertEquals(
            participant2AvatarSrc,
            getThumbnailSrc(driver1, participant2EndpointId));

        // Start the third participant
        ensureThreeParticipants();
        WebDriver driver3 = getParticipant3().getDriver();

        String participant2Src = getLocalThumbnailSrc(driver2);

        // Pin local video and verify avatars are displayed
        MeetUIUtils.clickOnLocalVideo(driver3);

        MeetUIUtils.assertAvatarDisplayed(driver3, participant1EndpointId);
        MeetUIUtils.assertAvatarDisplayed(driver3, participant2EndpointId);

        assertEquals(
            participant2Src,
            getThumbnailSrc(driver3, participant2EndpointId));
        assertEquals(
            participant1ThumbSrc,
            getThumbnailSrc(driver3, participant1EndpointId));

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
        MeetUIUtils.assertMuteIconIsDisplayed(
                driver2,
                driver1,
                false,
                true, //video
                "participant1");
        stopVideoTest.startVideoOnParticipantAndCheck();
        MeetUIUtils.assertMuteIconIsDisplayed(
                driver2,
                driver1,
                false,
                true, //video
                "participant2");
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

        WebDriver driver1 = getParticipant1().getDriver();

        MeetUIUtils.clickOnToolbarButton(driver1, "toolbar_button_profile");
        TestUtils.waitForDisplayedElementByXPath(
            driver1, "//input[@id='setEmail']", 5);

        String currentEmailValue
            = driver1.findElement(By.xpath("//input[@id='setEmail']"))
                .getAttribute("value");
        assertEquals(
            currentEmailValue,
            EMAIL,
            "Current email has wrong value");

        getParticipant1().hangUp();
        ensureTwoParticipants();

        driver1 = getParticipant1().getDriver();
        MeetUIUtils.clickOnToolbarButton(driver1, "toolbar_button_profile");
        TestUtils.waitForDisplayedElementByXPath(
            driver1,
            "//input[@id='setEmail']",
            5);

        currentEmailValue
            = driver1.findElement(By.xpath("//input[@id='setEmail']"))
                .getAttribute("value");
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
        Participant participant1 = getParticipant1();
        Participant participant2 = getParticipant2();
        final WebDriver driver1 = participant1.getDriver();
        final WebDriver driver2 = participant2.getDriver();

        final String participant1EndpointId = participant1.getEndpointId();

        String participant1AvatarXPath
            = "//span[@id='participant_" + participant1EndpointId
                + "']//img[@class='userAvatar']";

        // Wait for the avatar element to be created
        TestUtils.waitForElementByXPath(
            driver2, participant1AvatarXPath, 20);

        final String srcOneSecondParticipant =
            getSrcByXPath(driver2, participant1AvatarXPath);

        // change the email for participant1
        MeetUIUtils.clickOnToolbarButton(
            driver1, "toolbar_button_profile");
        TestUtils.waitForDisplayedElementByXPath(
            driver1, "//input[@id='setEmail']", 5);

        // set the value of the field through the jquery, or on FF we can
        // activate the key listener and m can mute the call and break tests
        participant1.executeScript("$('#setEmail').val('" + EMAIL + "').focusout();");

        //check if the local avatar in the settings menu has changed
        TestUtils.waitForCondition(
            driver1,
            5,
            (ExpectedCondition<Boolean>) d
                -> getSrcByXPath(driver1, "//img[@id='avatar']")
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

        MeetUIUtils.clickOnToolbarButton(
            driver1, "toolbar_button_profile");

        // we check whether avatar of participant2 is same on both sides
        // and we stored to check it after reload
        participant2AvatarSrc = getLocalThumbnailSrc(driver2);
        String participant2EndpointId = participant2.getEndpointId();
        assertEquals(
            participant2AvatarSrc,
            getThumbnailSrc(driver1, participant2EndpointId));

        // the problem on FF where we can send keys to the input field,
        // and the m from the text can mute the call, check whether we are muted
        MeetUIUtils.assertMuteIconIsDisplayed(
            driver2,
            driver1,
            false,
            false, //audio
            "participant1");
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
    private String getThumbnailSrc(WebDriver perspective, String endpointId)
    {
        return getSrcByXPath(
            perspective,
            "//span[@id='participant_" + endpointId
                + "']//img[@class='userAvatar']");
    }

    /**
     * Gets avatar SRC attribute for the one displayed on local video thumbnail.
     * @param perspective where are we checking this ?
     * @return string value of avatar's 'src' attribute
     */
    private String getLocalThumbnailSrc(WebDriver perspective)
    {
        return getSrcByXPath(
            perspective,
            "//span[@id='localVideoContainer']//img[@class='userAvatar']");
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
            "//div[@id='dominantSpeaker']/img[@id='dominantSpeakerAvatar']");
    }
}
