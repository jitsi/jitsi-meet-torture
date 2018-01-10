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

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;
import org.testng.annotations.*;

import static org.testng.Assert.*;

/**
 * Tests for users' avatars.
 */
public class AvatarTest
    extends AbstractBaseTest
{
    public static String EMAIL = "example@jitsi.org";
    public static String HASH = "dc47c9b1270a4a25a60bab7969e7632d";

    /**
     * We store second participant avatar src when running
     * test changeAvatarAndCheck, and in avatarWhenVideoMuted we restart the
     * second participant and the value should be the same.
     * Avatars should not change after a reload.
     */
    private static String secondParticipantAvatarSrc = null;

    @Override
    public void setup()
    {
        super.setup();

        ensureTwoParticipants();
    }

    /**
     * Scenario tests few cases for the avatar to be displayed when user mutes
     * his video.
     */
    @Test(dependsOnMethods = { "changeAvatarAndCheck" })
    public void avatarWhenVideoMuted()
    {
        hangUpAllParticipantsExceptTheOwner();

        // Start owner
        WebDriver owner = getParticipant1().getDriver();
        String ownerResource = MeetUtils.getResourceJid(owner);

        // Mute owner video
        MeetUIUtils.muteVideoAndCheck(owner, null);

        // Check if avatar on large video is the same as on local thumbnail
        String ownerThumbSrc = getLocalThumbnailSrc(owner);
        final String ownerLargeSrc = getLargeVideoSrc(owner);
        assertTrue(
            ownerLargeSrc.startsWith(ownerThumbSrc),
            "invalid avatar on the large video: " + ownerLargeSrc +
                    ", should start with: " + ownerThumbSrc);

        // Join with second participant
        ensureTwoParticipants();
        WebDriver secondParticipant = getParticipant2().getDriver();
        String secondPeerResource = MeetUtils.getResourceJid(secondParticipant);

        // Verify that the owner is muted from 2nd peer perspective
        MeetUIUtils.assertMuteIconIsDisplayed(
                secondParticipant,
                owner,
                true,
                true, //video
                "owner");
        // Pin owner's thumbnail, as owner is started muted for second
        // participant, there is no video element, so don't use
        // SwitchVideoTests.clickOnRemoteVideoAndTest which will check for
        // remote video switching on large
        MeetUIUtils.clickOnRemoteVideo(secondParticipant, ownerResource);
        // Check if owner's avatar is on large video now
        TestUtils.waitForCondition(secondParticipant, 5,
            (ExpectedCondition<Boolean>) d -> {
                String currentSrc = getLargeVideoSrc(d);
                return currentSrc.equals(ownerLargeSrc);
            });

        // Owner pins second participant's video
        MeetUIUtils.clickOnRemoteVideo(owner, secondPeerResource);
        // Check if avatar is displayed on owner's local video thumbnail
        MeetUIUtils.assertLocalThumbnailShowsAvatar(owner);
        // Unmute - now local avatar should be hidden and local video displayed
        StopVideoTest stopVideoTest = new StopVideoTest(this);
        stopVideoTest.startVideoOnOwnerAndCheck();

        MeetUIUtils.assertMuteIconIsDisplayed(
                secondParticipant,
                owner,
                false,
                true, //video
                "owner");
        MeetUIUtils.assertLocalThumbnailShowsVideo(owner);

        // Now both owner and 2nd have video muted
        stopVideoTest.stopVideoOnOwnerAndCheck();
        MeetUIUtils.assertMuteIconIsDisplayed(
                secondParticipant,
                owner,
                true,
                true, //video
                "owner");
        stopVideoTest.stopVideoOnParticipantAndCheck();
        MeetUIUtils.assertMuteIconIsDisplayed(
                owner,
                secondParticipant,
                true,
                true, //video
                "secondParticipant");

        // we check whether avatar of second participant is same on both sides
        // and we check whether it had changed after reloading the page
        String avatarSecondParticipantSrc
            = getLocalThumbnailSrc(secondParticipant);
        assertEquals(secondParticipantAvatarSrc,
            avatarSecondParticipantSrc);
        assertEquals(secondParticipantAvatarSrc,
            getThumbnailSrc(owner, secondPeerResource));

        // Start the third participant
        ensureThreeParticipants();
        WebDriver thirdParticipant = getParticipant3().getDriver();

        String secondPeerSrc = getLocalThumbnailSrc(secondParticipant);

        // Pin local video and verify avatars are displayed
        MeetUIUtils.clickOnLocalVideo(thirdParticipant);

        MeetUIUtils.assertAvatarDisplayed(thirdParticipant, ownerResource);
        MeetUIUtils.assertAvatarDisplayed(thirdParticipant, secondPeerResource);

        assertEquals(secondPeerSrc,
            getThumbnailSrc(thirdParticipant, secondPeerResource));
        assertEquals(ownerThumbSrc,
            getThumbnailSrc(thirdParticipant, ownerResource));

        // Click on owner's video
        MeetUIUtils.clickOnRemoteVideo(thirdParticipant, ownerResource);
        // His avatar should be on large video and
        // display name instead of an avatar, local video displayed
        MeetUIUtils.waitsForLargeVideoSwitch(thirdParticipant, ownerResource);
        MeetUIUtils.assertDisplayNameVisible(thirdParticipant, ownerResource);
        MeetUIUtils.assertAvatarDisplayed(thirdParticipant, secondPeerResource);
        MeetUIUtils.assertLocalThumbnailShowsVideo(thirdParticipant);

        // Click on second participant's video
        MeetUIUtils.clickOnRemoteVideo(thirdParticipant, secondPeerResource);
        MeetUIUtils.waitsForLargeVideoSwitch(
            thirdParticipant, secondPeerResource);
        MeetUIUtils.assertDisplayNameVisible(thirdParticipant, secondPeerResource);
        MeetUIUtils.assertAvatarDisplayed(thirdParticipant, ownerResource);
        MeetUIUtils.assertLocalThumbnailShowsVideo(thirdParticipant);

        getParticipant3().hangUp();

        TestUtils.waitMillis(1500);

        // Unmute owner and 2nd videos
        stopVideoTest.startVideoOnOwnerAndCheck();
        MeetUIUtils.assertMuteIconIsDisplayed(
                secondParticipant,
                owner,
                false,
                true, //video
                "owner");
        stopVideoTest.startVideoOnParticipantAndCheck();
        MeetUIUtils.assertMuteIconIsDisplayed(
                secondParticipant,
                owner,
                false,
                true, //video
                "secondParticipant");
    }

    /**
     *  Changes the avatar for one participant and checks if it has changed
     *  properly everywhere
     */
    @Test
    public void changeAvatarAndCheck()
    {
        final WebDriver owner = getParticipant1().getDriver();
        final WebDriver secondParticipant = getParticipant2().getDriver();

        final String ownerResourceJid = MeetUtils.getResourceJid(owner);

        String ownerAvatarXPath
            = "//span[@id='participant_" + ownerResourceJid
                                         + "']//img[@class='userAvatar']";

        // Wait for the avatar element to be created
        TestUtils.waitForElementByXPath(
            secondParticipant, ownerAvatarXPath, 20);

        final String srcOneSecondParticipant =
            getSrcByXPath(secondParticipant, ownerAvatarXPath);

        //change the email for the conference owner
        MeetUIUtils.clickOnToolbarButton(owner, "toolbar_button_profile");
        TestUtils.waitForDisplayedElementByXPath(
            owner, "//input[@id='setEmail']", 5);

        // set the value of the field through the jquery, or on FF we can
        // activate the key listener and m can mute the call and break tests
        ((JavascriptExecutor) owner)
            .executeScript("$('#setEmail').val('" + EMAIL + "').focusout();");

        //check if the local avatar in the settings menu has changed
        TestUtils.waitForCondition(owner, 5,
            (ExpectedCondition<Boolean>) d
                -> getSrcByXPath(owner, "//img[@id='avatar']").contains(HASH));

        //check if the avatar in the local thumbnail has changed
        checkSrcIsCorrect(getLocalThumbnailSrc(owner));
        //check if the avatar in the contact list has changed
        //checkSrcIsCorrect(getContactSrc(owner, ownerResourceJid));

        // waits till the src changes so we can continue with the check
        // sometimes the notification for the avatar change can be more
        // than 5 seconds
        TestUtils.waitForCondition(secondParticipant, 15,
            (ExpectedCondition<Boolean>) d -> {
                String currentSrc =
                    getThumbnailSrc(secondParticipant, ownerResourceJid);
                return !currentSrc.equals(srcOneSecondParticipant);
            });

        //check if the avatar in the thumbnail for the other participant has
        // changed
        checkSrcIsCorrect(getThumbnailSrc(secondParticipant, ownerResourceJid));
        //check if the avatar in the contact list has changed for the other
        // participant
        //checkSrcIsCorrect(getContactSrc(secondParticipant, ownerResourceJid));
        //check if the avatar displayed on large video has changed for the other
        // participant
        TestUtils.waitForCondition(secondParticipant, 5,
            (ExpectedCondition<Boolean>) d -> {
                String currentSrc = getLargeVideoSrc(secondParticipant);
                return currentSrc.contains(HASH);
            });

        MeetUIUtils.clickOnToolbarButton(owner, "toolbar_button_profile");

        // we check whether avatar of second participant is same on both sides
        // and we stored to check it after reload
        secondParticipantAvatarSrc = getLocalThumbnailSrc(secondParticipant);
        String secondParticipantResourceJid
            = MeetUtils.getResourceJid(secondParticipant);
        assertEquals(secondParticipantAvatarSrc,
            getThumbnailSrc(owner, secondParticipantResourceJid));

        // the problem on FF where we can send keys to the input field,
        // and the m from the text can mute the call, check whether we are muted
        MeetUIUtils.assertMuteIconIsDisplayed(
            secondParticipant,
            owner,
            false,
            false, //audio
            "owner");
    }

    /*
     *  Checks if the element with the given xpath has the correct src attribute
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
    private static String getSrcByXPath(WebDriver participant, String xpath)
    {
        return participant.findElement(By.xpath(xpath)).getAttribute("src");
    }

    /**
     * Gets avatar SRC attribute for the one displayed on small video thumbnail.
     * @param perspective where are we checking this ?
     * @param resourceJid resource part of the JID which belongs to the user for
     *                    whom we want to obtain avatar src
     * @return string value of avatar's 'src' attribute
     */
    private String getThumbnailSrc(WebDriver perspective, String resourceJid)
    {
        return getSrcByXPath(perspective,
            "//span[@id='participant_" + resourceJid
                + "']//img[@class='userAvatar']");
    }

    /**
     * Gets avatar SRC attribute for the one displayed in the contact list.
     * @param perspective where are we checking this ?
     * @param resourceJid resource part of the JID which belongs to the user for
     *                    whom we want to obtain avatar src
     * @return string value of avatar's 'src' attribute
     */
    private String getContactSrc(WebDriver perspective, String resourceJid)
    {
        return getSrcByXPath(perspective,
            "//div[@id='contacts_container']/ul/li[@id='" + resourceJid + "']/img");
    }

    /**
     * Gets avatar SRC attribute for the one displayed on local video thumbnail.
     * @param perspective where are we checking this ?
     * @return string value of avatar's 'src' attribute
     */
    private String getLocalThumbnailSrc(WebDriver perspective)
    {
        return getSrcByXPath(perspective,
            "//span[@id='localVideoContainer']//img[@class='userAvatar']");
    }

    /**
     * Gets avatar SRC attribute for the one displayed on large video.
     * @param perspective where are we checking this ?
     * @return string value of avatar's 'src' attribute
     */
    public static String getLargeVideoSrc(WebDriver perspective)
    {
        return getSrcByXPath(perspective,
            "//div[@id='dominantSpeaker']/img[@id='dominantSpeakerAvatar']");
    }
}
