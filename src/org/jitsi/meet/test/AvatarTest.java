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

import junit.framework.*;

import org.jitsi.meet.test.util.*;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;

/**
 * Tests for users' avatars.
 */
public class AvatarTest
    extends TestCase
{
    /**
     * Constructs test
     * @param name the method name for the test.
     */
    public AvatarTest(String name)
    {
        super(name);
    }

    public static String EMAIL = "example@jitsi.org";
    public static String HASH = "dc47c9b1270a4a25a60bab7969e7632d";

    /**
     * Orders the tests.
     * @return the suite with order tests.
     */
    public static junit.framework.Test suite()
    {
        TestSuite suite = new TestSuite();

        suite.addTest(new AvatarTest("changeAvatarAndCheck"));
        suite.addTest(new AvatarTest("avatarWhenVideoMuted"));

        return suite;
    }

    /**
     * Scenario tests few cases for the avatar to be displayed when user mutes
     * his video.
     */
    public void avatarWhenVideoMuted()
    {
        System.err.println("Start avatarWhenVideoMuted.");

        ConferenceFixture.closeAllParticipantsExceptTheOwner();

        // Start owner
        WebDriver owner = ConferenceFixture.getOwner();

        // Mute owner video
        TestUtils.waitForDisplayedElementByXPath(
                owner, "//a[@id='toolbar_button_camera']", 10);
        MeetUIUtils.clickOnToolbarButton(owner, "toolbar_button_camera");
        TestUtils.waitForElementByXPath(
                owner,
                "//span[@class='videoMuted']/i[@class='icon-camera-disabled']",
                5);

        // Check if avatar on large video is the same as on local thumbnail
        String ownerThumbSrc = getLocalThumbnailSrc(owner);
        String ownerLargeSrc = getLargeVideoSrc(owner);
        assertTrue(
                "invalid avatar on the large video: " + ownerLargeSrc +
                        ", should start with: " + ownerThumbSrc,
                ownerLargeSrc.startsWith(ownerThumbSrc));

        // Join with second participant
        ConferenceFixture.waitForSecondParticipantToConnect();
        WebDriver secondParticipant
                = ConferenceFixture.getSecondParticipantInstance();

        TestUtils.waitMillis(10000);

        // Verify that the owner is muted from 2nd peer perspective
        MeetUIUtils.assertMuteIconIsDisplayed(
                secondParticipant,
                owner,
                true,
                true, //video
                "owner");
        // Pin owner's thumbnail
        SwitchVideoTests.clickOnRemoteVideoAndTest(secondParticipant);
        // Check if owner's avatar is on large video now
        assertEquals(ownerLargeSrc, getLargeVideoSrc(secondParticipant));

        // Owner pins second participant's video
        SwitchVideoTests.clickOnRemoteVideoAndTest(owner);
        // Check if avatar is displayed on owner's local video thumbnail
        MeetUIUtils.assertLocalThumbnailShowsAvatar(owner);
        // Unmute - now local avatar should be hidden and local video displayed
        new StopVideoTest("startVideoOnOwnerAndCheck")
            .startVideoOnOwnerAndCheck();

        MeetUIUtils.assertMuteIconIsDisplayed(
                secondParticipant,
                owner,
                false,
                true, //video
                "owner");
        MeetUIUtils.assertLocalThumbnailShowsVideo(owner);

        // Now both owner and 2nd have video muted
        new StopVideoTest("stopVideoOnOwnerAndCheck")
            .stopVideoOnOwnerAndCheck();
        MeetUIUtils.assertMuteIconIsDisplayed(
                secondParticipant,
                owner,
                true,
                true, //video
                "owner");
        new StopVideoTest("stopVideoOnParticipantAndCheck")
            .stopVideoOnParticipantAndCheck();
        MeetUIUtils.assertMuteIconIsDisplayed(
                owner,
                secondParticipant,
                true,
                true, //video
                "secondParticipant");

        // Start the third participant
        ConferenceFixture.waitForThirdParticipantToConnect();
        WebDriver thirdParticipant = ConferenceFixture.getThirdParticipant();

        String secondPeerSrc = getLocalThumbnailSrc(secondParticipant);
        String secondPeerResource = MeetUtils.getResourceJid(secondParticipant);
        String ownerResource = MeetUtils.getResourceJid(owner);

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
        assertEquals(ownerResource,
                     MeetUIUtils.getLargeVideoResource(thirdParticipant));
        MeetUIUtils.assertDisplayNameVisible(thirdParticipant, ownerResource);
        MeetUIUtils.assertAvatarDisplayed(thirdParticipant, secondPeerResource);
        MeetUIUtils.assertLocalThumbnailShowsVideo(thirdParticipant);

        // Click on second participant's video
        MeetUIUtils.clickOnRemoteVideo(thirdParticipant, secondPeerResource);
        assertEquals(secondPeerResource,
                     MeetUIUtils.getLargeVideoResource(thirdParticipant));
        MeetUIUtils.assertDisplayNameVisible(thirdParticipant, secondPeerResource);
        MeetUIUtils.assertAvatarDisplayed(thirdParticipant, ownerResource);
        MeetUIUtils.assertLocalThumbnailShowsVideo(thirdParticipant);

        ConferenceFixture.close(thirdParticipant);

        TestUtils.waitMillis(1500);

        // Unmute owner and 2nd videos
        new StopVideoTest("startVideoOnOwnerAndCheck")
            .startVideoOnOwnerAndCheck();
        MeetUIUtils.assertMuteIconIsDisplayed(
                secondParticipant,
                owner,
                false,
                true, //video
                "owner");
        new StopVideoTest("startVideoOnParticipantAndCheck")
            .startVideoOnParticipantAndCheck();
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
    public void changeAvatarAndCheck()
    {
        System.err.println("Start changeAvatarAndCheck.");

        ConferenceFixture.ensureTwoParticipants();

        final WebDriver owner = ConferenceFixture.getOwner();
        final WebDriver secondParticipant
            = ConferenceFixture.getSecondParticipant();

        final String ownerResourceJid = MeetUtils.getResourceJid(owner);

        String ownerAvatarXPath
            = "//span[@id='participant_" + ownerResourceJid
                                         + "']/img[@class='userAvatar']";

        // Wait for the avatar element to be created
        TestUtils.waitForElementByXPath(
            secondParticipant, ownerAvatarXPath, 5000);

        final String srcOneSecondParticipant =
            getSrcByXPath(secondParticipant, ownerAvatarXPath);

        //change the email for the conference owner
        MeetUIUtils.clickOnToolbarButton(owner, "toolbar_button_settings");
        TestUtils.waitForDisplayedElementByXPath(
            owner, "//input[@id='setEmail']", 5);

        // set the value of the field through the jquery, or on FF we can
        // activate the key listener and m can mute the call and break tests
        ((JavascriptExecutor) owner)
            .executeScript("$('#setEmail').val('" + EMAIL + "');");

        owner.findElement(By.id("updateSettings")).click();

        //check if the local avatar in the settings menu has changed
        TestUtils.waitForCondition(owner, 5,
            new ExpectedCondition<Boolean>()
            {
                public Boolean apply(WebDriver d)
                {
                    return getSrcByXPath(owner, "//img[@id='avatar']")
                        .contains(HASH);
                }
            });

        //check if the avatar in the local thumbnail has changed
        checkSrcIsCorrect(getLocalThumbnailSrc(owner));
        //check if the avatar in the contact list has changed
        checkSrcIsCorrect(getContactSrc(owner, ownerResourceJid));

        // waits till the src changes so we can continue with the check
        // sometimes the notification for the avatar change can be more
        // than 5 seconds
        TestUtils.waitForCondition(secondParticipant, 15,
            new ExpectedCondition<Boolean>()
            {
                public Boolean apply(WebDriver d)
                {
                    String currentSrc =
                        getThumbnailSrc(secondParticipant, ownerResourceJid);
                    return !currentSrc.equals(srcOneSecondParticipant);
                }
            });

        //check if the avatar in the thumbnail for the other participant has
        // changed
        checkSrcIsCorrect(getThumbnailSrc(secondParticipant, ownerResourceJid));
        //check if the avatar in the contact list has changed for the other
        // participant
        checkSrcIsCorrect(getContactSrc(secondParticipant, ownerResourceJid));
        //check if the avatar displayed on large video has changed for the other
        // participant
        TestUtils.waitForCondition(secondParticipant, 5,
            new ExpectedCondition<Boolean>()
            {
                public Boolean apply(WebDriver d)
                {
                    String currentSrc = getLargeVideoSrc(secondParticipant);
                    return currentSrc.contains(HASH);
                }
            });

        MeetUIUtils.clickOnToolbarButton(owner, "toolbar_button_settings");

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
            "The avatar invalid src: " + src +" hash: " + HASH,
            src.contains(HASH));
    }

    /**
     * Return the participant avatar src that we will check
     * @return the participant avatar src that we will check
     */
    private String getSrcByXPath(WebDriver participant, String xpath)
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
                + "']/img[@class='userAvatar']");
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
            "//div[@id='contactlist']/ul/li[@id='" + resourceJid + "']/img");
    }

    /**
     * Gets avatar SRC attribute for the one displayed on local video thumbnail.
     * @param perspective where are we checking this ?
     * @return string value of avatar's 'src' attribute
     */
    private String getLocalThumbnailSrc(WebDriver perspective)
    {
        return getSrcByXPath(perspective,
            "//span[@id='localVideoContainer']/img[@class='userAvatar']");
    }

    /**
     * Gets avatar SRC attribute for the one displayed on large video.
     * @param perspective where are we checking this ?
     * @return string value of avatar's 'src' attribute
     */
    private String getLargeVideoSrc(WebDriver perspective)
    {
        return getSrcByXPath(perspective,
            "//div[@id='activeSpeaker']/img[@id='activeSpeakerAvatar']");
    }
}
