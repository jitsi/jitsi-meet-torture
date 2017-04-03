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
 * Tests shared video functionality.
 * @author Damian Minkov
 */
public class SharedVideoTest
    extends TestCase
{
    /**
     * Links to youtube videos and their titles, used for loading and testing.
     */
    private static String V1_LINK
        = "https://www.youtube.com/watch?v=xNXN7CZk8X0";
    private static String V1_TITLE
        = "[FOSDEM 2014] Jitsi Videobridge and WebRTC";
    private static String V2_LINK = "https://youtu.be/x-YGw3bgB_s";
    private static String V2_TITLE = "Jitsi Videobridge and WebRTC";

    /**
     * Assign to variable c the shared video container.
     * Used to obtain the player.
     */
    private static String JS_GET_SHARED_VIDEO_CONTAINER
        = "var c = APP.UI.getLargeVideo().containers['sharedvideo']; ";

    private static String currentVideoTitle;

    /**
     * Constructs test.
     * @param name method name.
     */
    public SharedVideoTest(String name)
    {
        super(name);
    }

    /**
     * Orders tests.
     * @return the suite with order tests.
     */
    public static junit.framework.Test suite()
    {
        TestSuite suite = new TestSuite();

        suite.addTest(new SharedVideoTest("startSharingVideo"));
        suite.addTest(new SharedVideoTest("checkRunning"));
        suite.addTest(new SharedVideoTest("checkTitles"));
        suite.addTest(new SharedVideoTest("pauseTest"));
        suite.addTest(new SharedVideoTest("playTest"));
        suite.addTest(new SharedVideoTest("seekTest"));
        suite.addTest(new SharedVideoTest("volumeTest"));
        suite.addTest(new SharedVideoTest("muteTest"));
        suite.addTest(new SharedVideoTest("switchVideoTest"));
        suite.addTest(new SharedVideoTest("backToSharedVideoTest"));
        suite.addTest(new SharedVideoTest("stopSharingTest"));
        suite.addTest(new SharedVideoTest("startSharingDifferentVideo"));
        suite.addTest(new SharedVideoTest("checkRunning"));
        suite.addTest(new SharedVideoTest("checkTitles"));
        suite.addTest(new SharedVideoTest("stopSharingTest"));
        suite.addTest(new SharedVideoTest("startSharingDifferentVideo"));
        suite.addTest(new SharedVideoTest("checkRunning"));
        suite.addTest(new SharedVideoTest("checkTitles"));
        suite.addTest(new SharedVideoTest("videoOwnerLeavesTest"));
        suite.addTest(new SharedVideoTest("shareVideoBeforeOthersJoin"));
        suite.addTest(new SharedVideoTest("checkRunning"));
        suite.addTest(new SharedVideoTest("checkTitles"));
        suite.addTest(new SharedVideoTest("sharePausedVideoBeforeOthersJoin"));
        suite.addTest(new SharedVideoTest("checkTitles"));
        suite.addTest(new SharedVideoTest("stopSharingTest"));

        return suite;
    }

    /**
     * Starts a shared video and checks whether it is visible and is playing.
     */
    public void startSharingVideo()
    {
        System.err.println("Start startSharingVideo.");

        startSharingVideoByUrlAndTitle(V1_LINK, V1_TITLE, true);
    }

    /**
     * Start shared video.
     * @param url
     * @param title
     * @param checkSecondParticipantState whether second participant state
     *                                    needs to be checked
     */
    private void startSharingVideoByUrlAndTitle(String url, String title,
        boolean checkSecondParticipantState)
    {
        WebDriver owner = ConferenceFixture.getOwner();
        MeetUIUtils.clickOnToolbarButton(owner, "toolbar_button_sharedvideo");

        TestUtils.waitForElementByXPath(
            owner, "//input[@name='sharedVideoUrl']", 5);

        //owner.findElement(
        //    By.xpath("//input[@name='sharedVideoUrl']")).sendKeys(url);
        // sendKeys is not working for FF, seems the input has size of 20
        // and only 21 chars goes in, the size is not visible in
        // web-development console
        MeetUIUtils.setAttribute(
            owner,
            owner.findElement(
                By.xpath("//input[@name='sharedVideoUrl']")),
            "value",
            url);

        currentVideoTitle = title;

        owner.findElement(
            By.name("jqi_state0_buttonspandatai18ndialogShareSharespan"))
            .click();

        // give time for the internal frame to load and attach to the page.
        TestUtils.waitMillis(2000);

        WebDriverWait wait = new WebDriverWait(owner, 30);
        wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(
            By.id("sharedVideoIFrame")));
        // make sure we are in meet, not in the frame
        owner.switchTo().defaultContent();

        assertEquals("Video not displayed:", true,
            owner.findElement(By.id("sharedVideoIFrame")).isDisplayed());

        if(checkSecondParticipantState)
        {
            // Now let's check the second participant state
            WebDriver secondParticipant =
                ConferenceFixture.getSecondParticipant();
            // make sure we are in meet, not in the frame
            secondParticipant.switchTo().defaultContent();

            TestUtils.waitForDisplayedElementByID(secondParticipant,
                "sharedVideoIFrame", 10);
        }
    }

    /**
     * Waits for player to be defined and checks its state.
     * @param participant
     * @param state
     */
    private void checkPlayerLoadedAndInState(
        WebDriver participant, String state)
    {
        TestUtils.waitForBoolean(participant,
            JS_GET_SHARED_VIDEO_CONTAINER
                + "return (c && c.player "
                    + "&& c.player.getPlayerState() === " + state + ");", 15);
    }

    /**
     * Checks for title on both sides, compare with current video playing title.
     */
    public void checkTitles()
    {
        System.err.println("Start checkTitles.");

        String ownerVideoTitle
            = TestUtils.executeScriptAndReturnString(
                ConferenceFixture.getOwner(),
                    JS_GET_SHARED_VIDEO_CONTAINER
                    + "return c.player.getVideoData().title;");
        assertEquals("Different title found on owner",
            currentVideoTitle, ownerVideoTitle);

        String secondVideoTitle
            = TestUtils.executeScriptAndReturnString(
                ConferenceFixture.getSecondParticipant(),
                    JS_GET_SHARED_VIDEO_CONTAINER
                    + "return c.player.getVideoData().title;");
        assertEquals("Different title found on second participant",
            currentVideoTitle, secondVideoTitle);
    }

    /**
     * Checks that both players are playing.
     */
    public void checkRunning()
    {
        System.err.println("Start checkRunning.");

        checkPlayerLoadedAndInState(
            ConferenceFixture.getOwner(), "YT.PlayerState.PLAYING");
        checkPlayerLoadedAndInState(
            ConferenceFixture.getSecondParticipant(), "YT.PlayerState.PLAYING");
    }

    /**
     * Pause player and check whether its paused on both sides.
     */
    public void pauseTest()
    {
        System.err.println("Start pauseTest.");

        ((JavascriptExecutor) ConferenceFixture.getOwner()).executeScript(
            JS_GET_SHARED_VIDEO_CONTAINER
            + "c.player.pauseVideo();");

        checkPlayerLoadedAndInState(
            ConferenceFixture.getOwner(),
            "YT.PlayerState.PAUSED"
        );
        checkPlayerLoadedAndInState(
            ConferenceFixture.getSecondParticipant(),
            "YT.PlayerState.PAUSED"
        );
    }

    /**
     * Play player and check whether its playing on both sides.
     */
    public void playTest()
    {
        System.err.println("Start playTest.");

        ((JavascriptExecutor) ConferenceFixture.getOwner()).executeScript(
            JS_GET_SHARED_VIDEO_CONTAINER
            + "c.player.playVideo();");

        checkPlayerLoadedAndInState(
            ConferenceFixture.getOwner(),
            "YT.PlayerState.PLAYING"
        );
        checkPlayerLoadedAndInState(
            ConferenceFixture.getSecondParticipant(),
            "YT.PlayerState.PLAYING"
        );
    }

    /**
     * Fast-forward player and check whether its playing on both sides and its
     * positions are no more than 10 secs. away.
     */
    public void seekTest()
    {
        System.err.println("Start seekTest.");

        ((JavascriptExecutor) ConferenceFixture.getOwner()).executeScript(
            JS_GET_SHARED_VIDEO_CONTAINER
                + "c.player.seekTo(86);");

        // let's wait for the fast forward
        TestUtils.waitForBoolean(ConferenceFixture.getOwner(),
            JS_GET_SHARED_VIDEO_CONTAINER
                + "return (c.player.getPlayerState() === YT.PlayerState.PLAYING"
                + " && c.player.getCurrentTime() > 86);", 10);
        TestUtils.waitForBoolean(ConferenceFixture.getSecondParticipant(),
            JS_GET_SHARED_VIDEO_CONTAINER
                + "return (c.player.getPlayerState() === YT.PlayerState.PLAYING"
                + " && c.player.getCurrentTime() > 86);", 10);

        Object ownerTimeObj = ((JavascriptExecutor)
            ConferenceFixture.getOwner()).executeScript(
            JS_GET_SHARED_VIDEO_CONTAINER
            + "return c.player.getCurrentTime();");

        Object secondTimeObj = ((JavascriptExecutor)
            ConferenceFixture.getSecondParticipant()).executeScript(
            JS_GET_SHARED_VIDEO_CONTAINER
            + "return c.player.getCurrentTime();");

        double ownerTime = 0;
        if (ownerTimeObj instanceof Double)
            ownerTime = (Double)ownerTimeObj;
        else if (ownerTimeObj instanceof Long)
            ownerTime = (Long)ownerTimeObj;

        double secondTime = 0;
        if (secondTimeObj instanceof Double)
            secondTime = (Double)secondTimeObj;
        else if (secondTimeObj instanceof Long)
            secondTime = (Long)secondTimeObj;

        // let's check that time difference is less than 10 seconds
        assertTrue("Players time differ owner:" + ownerTime
            + " second:" + secondTime,
                (Math.abs(ownerTime - secondTime) < 10));
    }

    /**
     * Changes the current volume and checks for the change on the other side
     */
    public void volumeTest()
    {
        System.err.println("Start volumeTest.");

        Long ownerVolume = (Long)((JavascriptExecutor)
            ConferenceFixture.getOwner()).executeScript(
            JS_GET_SHARED_VIDEO_CONTAINER
                + "return c.player.getVolume();");

        long volumeToSet = ownerVolume - 20;

        ((JavascriptExecutor)
            ConferenceFixture.getOwner()).executeScript(
            JS_GET_SHARED_VIDEO_CONTAINER
                + "return c.player.setVolume(" + volumeToSet + ");");
        // now check for volume on both sides
        TestUtils.waitForBoolean(ConferenceFixture.getOwner(),
            JS_GET_SHARED_VIDEO_CONTAINER
                + "return (c.player.getPlayerState() === YT.PlayerState.PLAYING"
                + " && c.player.getVolume() == " + volumeToSet + ");", 10);
        TestUtils.waitForBoolean(ConferenceFixture.getSecondParticipant(),
            JS_GET_SHARED_VIDEO_CONTAINER
                + "return (c.player.getPlayerState() === YT.PlayerState.PLAYING"
                + " && c.player.getVolume() == " + volumeToSet + ");", 10);
    }

    /**
     * Mutes the current volume and checks for the change on the other side
     */
    public void muteTest()
    {
        System.err.println("Start muteTest.");

        ((JavascriptExecutor)
            ConferenceFixture.getOwner()).executeScript(
            JS_GET_SHARED_VIDEO_CONTAINER
                + "return c.player.mute();");
        // now check for volume on both sides
        TestUtils.waitForBoolean(ConferenceFixture.getOwner(),
            JS_GET_SHARED_VIDEO_CONTAINER
                + "return (c.player.getPlayerState() === YT.PlayerState.PLAYING"
                + " && c.player.isMuted());", 10);
        TestUtils.waitForBoolean(ConferenceFixture.getSecondParticipant(),
            JS_GET_SHARED_VIDEO_CONTAINER
                + "return (c.player.getPlayerState() === YT.PlayerState.PLAYING"
                + " && (c.player.isMuted() || c.player.getVolume() == 0));",
            10);
    }

    /**
     * Switches video to another participant.
     */
    public void switchVideoTest()
    {
        System.err.println("Start clickOnRemoteVideoAndTest.");

        new SwitchVideoTest("participantClickOnRemoteVideoAndTest")
            .participantClickOnRemoteVideoAndTest();
    }

    /**
     * Clicks back to shared video and checks is video is visible.
     */
    public void backToSharedVideoTest()
    {
        System.err.println("Start backToSharedVideoTest.");

        ConferenceFixture.getSecondParticipant()
            .findElement(By.id("sharedVideoContainer"))
            .click();
        TestUtils.waitMillis(1000);

        assertEquals("Video not displayed:", true,
            ConferenceFixture.getSecondParticipant()
                .findElement(By.id("sharedVideoIFrame"))
                .isDisplayed());
    }

    /**
     * Tries to stop shared video and cancels, check its still displayed on
     * both sides.
     * Stops shared video and checks on both sides its not played.
     */
    public void stopSharingTest()
    {
        System.err.println("Start stopSharingTest.");

        WebDriver owner = ConferenceFixture.getOwner();
        MeetUIUtils.clickOnToolbarButton(owner, "toolbar_button_sharedvideo");

        TestUtils.waitForElementByXPath(
            owner, "//button[@name="
                + "'jqi_state0_buttonspandatai18ndialogCancelCancelspan']", 5);

        // let's cancel
        owner.findElement(
            By.name("jqi_state0_buttonspandatai18ndialogCancelCancelspan"))
            .click();

        // video should be visible on both sides
        WebDriver secondParticipant = ConferenceFixture.getSecondParticipant();
        assertEquals("Video not displayed:", true,
            owner.findElement(By.id("sharedVideoIFrame"))
                .isDisplayed());
        assertEquals("Video not displayed:", true,
            secondParticipant.findElement(By.id("sharedVideoIFrame"))
                .isDisplayed());

        MeetUIUtils.clickOnToolbarButton(owner, "toolbar_button_sharedvideo");

        // now lets stop sharing
        TestUtils.waitForElementByXPath(
            owner, "//button[@name="
                + "'jqi_state0_buttonspandatai18ndialogRemoveRemovespan']", 5);
        owner.findElement(
            By.name("jqi_state0_buttonspandatai18ndialogRemoveRemovespan"))
            .click();

        try
        {
            TestUtils.waitForNotDisplayedElementByID(
                owner, "sharedVideoIFrame", 5);
        }
        catch (StaleElementReferenceException ex)
        {
            // if the element is detached in a process of checking its display
            // status, means its not visible anymore
        }
        try
        {
            TestUtils.waitForNotDisplayedElementByID(
                secondParticipant, "sharedVideoIFrame", 5);
        }
        catch (StaleElementReferenceException ex)
        {
            // if the element is detached in a process of checking its display
            // status, means its not visible anymore
        }
    }

    /**
     * Starts sharing another video.
     */
    public void startSharingDifferentVideo()
    {
        System.err.println("Start startSharingDifferentVideo.");

        startSharingVideoByUrlAndTitle(V2_LINK, V2_TITLE, true);
    }

    /**
     * The owner leaves and we need to check that shared video is removed
     * on second participant.
     */
    public void videoOwnerLeavesTest()
    {
        System.err.println("Start videoOwnerLeavesTest.");

        ConferenceFixture.close(ConferenceFixture.getOwner());

        TestUtils.waitForNotDisplayedElementByID(
            ConferenceFixture.getSecondParticipant(), "sharedVideoIFrame", 5);
    }

    /**
     * Start shared video before second participant joins.
     * This tests
     */
    public void shareVideoBeforeOthersJoin()
    {
        System.err.println("Start shareVideoBeforeOthersJoin.");

        shareVideoBeforeOthersJoin(false);
    }

    /**
     * Shares the video when only owner is in room, and if needed pause.
     * @param pause whether to pause before starting others
     */
    private void shareVideoBeforeOthersJoin(boolean pause)
    {
        ConferenceFixture.closeAllParticipants();

        TestUtils.waitMillis(1000);

        WebDriver owner = ConferenceFixture.getOwner();

        MeetUtils.waitForParticipantToJoinMUC(owner, 10);
        TestUtils.waitForDisplayedElementByID(owner,
            "toolbar_button_sharedvideo", 10);

        // just wait a little after button is displayed
        // sometimes the button is reported as
        // Element is not clickable at point (566, -10)
        TestUtils.waitMillis(1000);

        startSharingVideoByUrlAndTitle(V2_LINK, V2_TITLE, false);
        checkPlayerLoadedAndInState(owner, "YT.PlayerState.PLAYING");

        String stateToCheck = "YT.PlayerState.PLAYING";
        if(pause)
        {
            stateToCheck = "YT.PlayerState.PAUSED";
            ((JavascriptExecutor) ConferenceFixture.getOwner()).executeScript(
                JS_GET_SHARED_VIDEO_CONTAINER
                    + "c.player.seekTo(15);");
            ((JavascriptExecutor) ConferenceFixture.getOwner()).executeScript(
                JS_GET_SHARED_VIDEO_CONTAINER
                    + "c.player.pauseVideo();");
        }

        ConferenceFixture.waitForSecondParticipantToConnect();
        MeetUtils.waitForIceConnected(owner);
        MeetUtils.waitForSendReceiveData(owner);

        // Now let's check the second participant state
        WebDriver secondParticipant =
            ConferenceFixture.getSecondParticipant();
        // make sure we are in meet, not in the frame
        secondParticipant.switchTo().defaultContent();

        checkPlayerLoadedAndInState(secondParticipant, stateToCheck);

        assertEquals("Video not displayed:", true,
            secondParticipant.findElement(By.id("sharedVideoIFrame"))
                .isDisplayed());
    }

    public void sharePausedVideoBeforeOthersJoin()
    {
        System.err.println("Start sharePausedVideoBeforeOthersJoin.");

        shareVideoBeforeOthersJoin(true);

        checkPlayerLoadedAndInState(
            ConferenceFixture.getOwner(),
            "YT.PlayerState.PAUSED"
        );
        checkPlayerLoadedAndInState(
            ConferenceFixture.getSecondParticipant(),
            "YT.PlayerState.PAUSED"
        );

        Object ownerTimeObj = ((JavascriptExecutor)
            ConferenceFixture.getOwner()).executeScript(
            JS_GET_SHARED_VIDEO_CONTAINER
                + "return c.player.getCurrentTime();");

        Object secondTimeObj = ((JavascriptExecutor)
            ConferenceFixture.getSecondParticipant()).executeScript(
            JS_GET_SHARED_VIDEO_CONTAINER
                + "return c.player.getCurrentTime();");

        double ownerTime = 0;
        if (ownerTimeObj instanceof Double)
            ownerTime = (Double)ownerTimeObj;
        else if (ownerTimeObj instanceof Long)
            ownerTime = (Long)ownerTimeObj;

        double secondTime = 0;
        if (secondTimeObj instanceof Double)
            secondTime = (Double)secondTimeObj;
        else if (secondTimeObj instanceof Long)
            secondTime = (Long)secondTimeObj;

        // let's check that time difference is less than 10 seconds
        assertTrue("Players time differ owner:" + ownerTime
                + " second:" + secondTime,
            (Math.abs(ownerTime - secondTime) < 10));
    }
}
