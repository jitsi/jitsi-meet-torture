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
 * Tests shared video functionality.
 * @author Damian Minkov
 */
public class SharedVideoTest
    extends AbstractBaseTest
{
    /**
     * Links to youtube videos and their expected urls, used for loading and
     * testing.
     */
    private static String V1_VIDEO_ID = "xNXN7CZk8X0";
    private static String V1_LINK
        = "https://www.youtube.com/watch?v=" + V1_VIDEO_ID;
    private static String V2_VIDEO_ID = "x-YGw3bgB_s";
    private static String V2_LINK = "https://youtu.be/" + V2_VIDEO_ID;

    /**
     * Assign to variable c the shared video container.
     * Used to obtain the player.
     */
    private static String JS_GET_SHARED_VIDEO_CONTAINER
        = "var c = APP.UI.getLargeVideo().containers['sharedvideo']; ";

    private static String currentVideoId;

    @Override
    public void setup()
    {
        super.setup();

        ensureTwoParticipants();
    }

    /**
     * Starts a shared video and checks whether it is visible and is playing.
     */
    @Test
    public void startSharingVideo()
    {
        startSharingVideoByUrl(V1_LINK, V1_VIDEO_ID, true);

        checkRunning();

        checkUrls();
    }

    /**
     * Start shared video.
     * @param url the video to share
     * @param expectedId the expected video id
     * @param checkSecondParticipantState whether second participant state
     *                                    needs to be checked
     */
    private void startSharingVideoByUrl(String url, String expectedId,
        boolean checkSecondParticipantState)
    {
        WebDriver owner = getParticipant1().getDriver();
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

        currentVideoId = expectedId;

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

        assertTrue(
            owner.findElement(By.id("sharedVideoIFrame")).isDisplayed(),
            "Video not displayed:");

        if (checkSecondParticipantState)
        {
            // Now let's check the second participant state
            WebDriver secondParticipant = getParticipant2().getDriver();
            // make sure we are in meet, not in the frame
            secondParticipant.switchTo().defaultContent();

            TestUtils.waitForDisplayedElementByID(secondParticipant,
                "sharedVideoIFrame", 10);
        }
    }

    /**
     * Waits for player to be defined and checks its state.
     * @param participant the driver to operate on.
     * @param state the state to check
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
     * Checks for url on both sides, compare with current video playing url.
     */
    private void checkUrls()
    {
        String ownerVideoUrl
            = TestUtils.executeScriptAndReturnString(
                getParticipant1().getDriver(),
                    JS_GET_SHARED_VIDEO_CONTAINER
                    + "return c.player.getVideoUrl();");
        assertTrue(
            ownerVideoUrl.contains(currentVideoId),
            "Different video url found on owner");

        String secondVideoUrl
            = TestUtils.executeScriptAndReturnString(
                getParticipant2().getDriver(),
                    JS_GET_SHARED_VIDEO_CONTAINER
                    + "return c.player.getVideoUrl();");
        assertTrue(
            secondVideoUrl.contains(currentVideoId),
            "Different video url found on second participant");
    }

    /**
     * Checks that both players are playing.
     */
    private void checkRunning()
    {
        checkPlayerLoadedAndInState(
            getParticipant1().getDriver(), "YT.PlayerState.PLAYING");
        checkPlayerLoadedAndInState(
            getParticipant2().getDriver(), "YT.PlayerState.PLAYING");
    }

    /**
     * Pause player and check whether its paused on both sides.
     */
    @Test(dependsOnMethods = { "startSharingVideo" })
    public void pauseTest()
    {
        WebDriver owner = getParticipant1().getDriver();
        ((JavascriptExecutor) owner).executeScript(
            JS_GET_SHARED_VIDEO_CONTAINER
            + "c.player.pauseVideo();");

        checkPlayerLoadedAndInState(
            owner,
            "YT.PlayerState.PAUSED"
        );
        checkPlayerLoadedAndInState(
            getParticipant2().getDriver(),
            "YT.PlayerState.PAUSED"
        );
    }

    /**
     * Play player and check whether its playing on both sides.
     */
    @Test(dependsOnMethods = { "pauseTest" })
    public void playTest()
    {
        WebDriver owner = getParticipant1().getDriver();
        ((JavascriptExecutor) owner).executeScript(
            JS_GET_SHARED_VIDEO_CONTAINER
            + "c.player.playVideo();");

        checkPlayerLoadedAndInState(
            owner,
            "YT.PlayerState.PLAYING"
        );
        checkPlayerLoadedAndInState(
            getParticipant2().getDriver(),
            "YT.PlayerState.PLAYING"
        );
    }

    /**
     * Fast-forward player and check whether its playing on both sides and its
     * positions are no more than 10 secs. away.
     */
    @Test(dependsOnMethods = { "playTest" })
    public void seekTest()
    {
        WebDriver owner = getParticipant1().getDriver();
        ((JavascriptExecutor) owner).executeScript(
            JS_GET_SHARED_VIDEO_CONTAINER
                + "c.player.seekTo(86);");

        // let's wait for the fast forward
        TestUtils.waitForBoolean(owner,
            JS_GET_SHARED_VIDEO_CONTAINER
                + "return (c.player.getPlayerState() === YT.PlayerState.PLAYING"
                + " && c.player.getCurrentTime() > 86);", 10);
        TestUtils.waitForBoolean(getParticipant2().getDriver(),
            JS_GET_SHARED_VIDEO_CONTAINER
                + "return (c.player.getPlayerState() === YT.PlayerState.PLAYING"
                + " && c.player.getCurrentTime() > 86);", 10);

        Object ownerTimeObj = ((JavascriptExecutor)
            owner).executeScript(
            JS_GET_SHARED_VIDEO_CONTAINER
            + "return c.player.getCurrentTime();");

        Object secondTimeObj = ((JavascriptExecutor)
            getParticipant2().getDriver()).executeScript(
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
        assertTrue(
            (Math.abs(ownerTime - secondTime) < 10),
            "Players time differ owner:" + ownerTime + " second:" + secondTime);
    }

    /**
     * Changes the current volume and checks for the change on the other side
     */
    @Test(dependsOnMethods = { "seekTest" })
    public void volumeTest()
    {
        WebDriver owner = getParticipant1().getDriver();
        Long ownerVolume = (Long)((JavascriptExecutor)
            owner).executeScript(
            JS_GET_SHARED_VIDEO_CONTAINER
                + "return c.player.getVolume();");

        long volumeToSet = ownerVolume - 20;

        ((JavascriptExecutor)
            owner).executeScript(
            JS_GET_SHARED_VIDEO_CONTAINER
                + "return c.player.setVolume(" + volumeToSet + ");");
        // now check for volume on both sides
        TestUtils.waitForBoolean(owner,
            JS_GET_SHARED_VIDEO_CONTAINER
                + "return (c.player.getPlayerState() === YT.PlayerState.PLAYING"
                + " && c.player.getVolume() == " + volumeToSet + ");", 10);
        TestUtils.waitForBoolean(getParticipant2().getDriver(),
            JS_GET_SHARED_VIDEO_CONTAINER
                + "return (c.player.getPlayerState() === YT.PlayerState.PLAYING"
                + " && c.player.getVolume() == " + volumeToSet + ");", 10);
    }

    /**
     * Mutes the current volume and checks for the change on the other side
     */
    @Test(dependsOnMethods = { "volumeTest" })
    public void muteTest()
    {
        WebDriver owner = getParticipant1().getDriver();
        ((JavascriptExecutor)
            owner).executeScript(
            JS_GET_SHARED_VIDEO_CONTAINER
                + "return c.player.mute();");
        // now check for volume on both sides
        TestUtils.waitForBoolean(owner,
            JS_GET_SHARED_VIDEO_CONTAINER
                + "return (c.player.getPlayerState() === YT.PlayerState.PLAYING"
                + " && c.player.isMuted());", 10);
        TestUtils.waitForBoolean(getParticipant2().getDriver(),
            JS_GET_SHARED_VIDEO_CONTAINER
                + "return (c.player.getPlayerState() === YT.PlayerState.PLAYING"
                + " && (c.player.isMuted() || c.player.getVolume() == 0));",
            10);
    }

    /**
     * Switches video to another participant.
     */
    @Test(dependsOnMethods = { "muteTest" })
    public void switchVideoTest()
    {
        new SwitchVideoTest(this).participantClickOnRemoteVideoAndTest();
    }

    /**
     * Clicks back to shared video and checks is video is visible.
     */
    @Test(dependsOnMethods = { "switchVideoTest" })
    public void backToSharedVideoTest()
    {
        getParticipant2().getDriver()
            .findElement(By.id("sharedVideoContainer"))
            .click();
        TestUtils.waitMillis(1000);

        assertTrue(
            getParticipant2().getDriver()
                .findElement(By.id("sharedVideoIFrame"))
                .isDisplayed(),
            "Video not displayed:");
    }

    /**
     * Tries to stop shared video and cancels, check its still displayed on
     * both sides.
     * Stops shared video and checks on both sides its not played.
     */
    @Test(dependsOnMethods = { "backToSharedVideoTest" })
    public void stopSharingTest()
    {
        WebDriver owner = getParticipant1().getDriver();
        MeetUIUtils.clickOnToolbarButton(owner, "toolbar_button_sharedvideo");

        TestUtils.waitForElementByXPath(
            owner, "//button[@name="
                + "'jqi_state0_buttonspandatai18ndialogCancelCancelspan']", 5);

        // let's cancel
        owner.findElement(
            By.name("jqi_state0_buttonspandatai18ndialogCancelCancelspan"))
            .click();

        // video should be visible on both sides
        WebDriver secondParticipant = getParticipant2().getDriver();
        assertTrue(
            owner.findElement(By.id("sharedVideoIFrame")).isDisplayed(),
            "Video not displayed:");
        assertTrue(
            secondParticipant.findElement(By.id("sharedVideoIFrame"))
                .isDisplayed(),
            "Video not displayed:");

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
    @Test(dependsOnMethods = { "stopSharingTest" })
    public void startSharingDifferentVideo()
    {
        startSharingVideoByUrl(V2_LINK, V2_VIDEO_ID, true);

        checkRunning();

        checkUrls();

        stopSharingTest();
    }

    @Test(dependsOnMethods = { "startSharingDifferentVideo" })
    public void startSharingDifferentVideoAfterStop()
    {
        startSharingVideoByUrl(V2_LINK, V2_VIDEO_ID, true);

        checkRunning();

        checkUrls();
    }

    /**
     * The owner leaves and we need to check that shared video is removed
     * on second participant.
     */
    @Test(dependsOnMethods = { "startSharingDifferentVideoAfterStop" })
    public void videoOwnerLeavesTest()
    {
        getParticipant1().hangUp();

        TestUtils.waitForNotDisplayedElementByID(
            getParticipant2().getDriver(), "sharedVideoIFrame", 5);
    }

    /**
     * Start shared video before second participant joins.
     * This tests
     */
    @Test(dependsOnMethods = { "videoOwnerLeavesTest" })
    public void shareVideoBeforeOthersJoin()
    {
        shareVideoBeforeOthersJoin(false);

        checkRunning();

        checkUrls();
    }

    /**
     * Shares the video when only owner is in room, and if needed pause.
     * @param pause whether to pause before starting others
     */
    private void shareVideoBeforeOthersJoin(boolean pause)
    {
        hangUpAllParticipants();

        TestUtils.waitMillis(1000);

        ensureOneParticipant();
        WebDriver owner = getParticipant1().getDriver();

        TestUtils.waitForDisplayedElementByID(owner,
            "toolbar_button_sharedvideo", 10);

        // just wait a little after button is displayed
        // sometimes the button is reported as
        // Element is not clickable at point (566, -10)
        TestUtils.waitMillis(1000);

        startSharingVideoByUrl(V2_LINK, V2_VIDEO_ID, false);
        checkPlayerLoadedAndInState(owner, "YT.PlayerState.PLAYING");

        String stateToCheck = "YT.PlayerState.PLAYING";
        if (pause)
        {
            stateToCheck = "YT.PlayerState.PAUSED";
            ((JavascriptExecutor) owner).executeScript(
                JS_GET_SHARED_VIDEO_CONTAINER
                    + "c.player.seekTo(15);");
            ((JavascriptExecutor) owner).executeScript(
                JS_GET_SHARED_VIDEO_CONTAINER
                    + "c.player.pauseVideo();");
        }

        ensureTwoParticipants();

        // Now let's check the second participant state
        WebDriver secondParticipant = getParticipant2().getDriver();
        // make sure we are in meet, not in the frame
        secondParticipant.switchTo().defaultContent();

        checkPlayerLoadedAndInState(secondParticipant, stateToCheck);

        assertTrue(
            secondParticipant.findElement(By.id("sharedVideoIFrame"))
                .isDisplayed(),
            "Video not displayed:");
    }

    @Test(dependsOnMethods = { "shareVideoBeforeOthersJoin" })
    public void sharePausedVideoBeforeOthersJoin()
    {
        shareVideoBeforeOthersJoin(true);

        WebDriver owner = getParticipant1().getDriver();
        checkPlayerLoadedAndInState(
            owner,
            "YT.PlayerState.PAUSED"
        );
        checkPlayerLoadedAndInState(
            getParticipant2().getDriver(),
            "YT.PlayerState.PAUSED"
        );

        Object ownerTimeObj = ((JavascriptExecutor)
            owner).executeScript(
            JS_GET_SHARED_VIDEO_CONTAINER
                + "return c.player.getCurrentTime();");

        Object secondTimeObj = ((JavascriptExecutor)
            getParticipant2().getDriver()).executeScript(
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
        assertTrue(
            (Math.abs(ownerTime - secondTime) < 10),
            "Players time differ owner:" + ownerTime + " second:" + secondTime);

        checkUrls();

        stopSharingTest();
    }
}
