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
import org.testng.annotations.*;

import static org.testng.Assert.*;

/**
 * Tests shared video functionality.
 *
 * @author Damian Minkov
 */
public class SharedVideoTest
    extends WebTestBase
{
    /**
     * Links to youtube videos and their expected urls, used for loading and
     * testing.
     */
    private static String V1_VIDEO_ID = "xNXN7CZk8X0";
    private static String V1_LINK = "https://www.youtube.com/watch?v=" + V1_VIDEO_ID;
    public static String V2_LINK = "https://archive.org/download/BigBuckBunny_124/Content/big_buck_bunny_720p_surround.mp4";
    private static String V2_VIDEO_ID = V2_LINK;

    /**
     * Assign to variable c the shared video container.
     * Used to obtain the player.
     */
    private static String JS_GET_SHARED_VIDEO_CONTAINER
        = "var c = window; ";

    /**
     * The javascript code which returns the current time of the player.
     */
    private static final String JS_GET_PLAYER_CURRENT_TIME
        = JS_GET_SHARED_VIDEO_CONTAINER + "return c._sharedVideoPlayer.getTime();";

    /**
     * The javascript code which returns the video URL from the player.
     */
    private static final String JS_GET_PLAYER_VIDEO_URL
        = JS_GET_SHARED_VIDEO_CONTAINER + "return c._sharedVideoPlayer.props._videoUrl;";

    private static String currentVideoId;

    @Override
    public void setupClass()
    {
        super.setupClass();

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
        getParticipant1().getToolbar().clickSharedVideoButton();

        WebDriver driver1 = getParticipant1().getDriver();

        TestUtils.waitForElementByXPath(
            driver1, "//input[@name='sharedVideoUrl']", 5);

        //getParticipant1().findElement(
        //    By.xpath("//input[@name='sharedVideoUrl']")).sendKeys(url);
        // sendKeys is not working for FF, seems the input has size of 20
        // and only 21 chars goes in, the size is not visible in
        // web-development console
        driver1.findElement(By.xpath("//input[@name='sharedVideoUrl']"))
                .sendKeys(url);

        currentVideoId = expectedId;

        TestUtils.click(
            driver1,
            By.id("modal-dialog-ok-button"));

        // give time for the internal frame to load and attach to the page.
        TestUtils.waitMillis(2000);

        // make sure we are in meet, not in the frame
        driver1.switchTo().defaultContent();

        assertTrue(
            driver1.findElement(By.id("sharedVideoPlayer")).isDisplayed(),
            "Video not displayed:");

        if (checkSecondParticipantState)
        {
            // Now let's check the second participant state
            WebDriver driver2 = getParticipant2().getDriver();
            // make sure we are in meet, not in the frame
            driver2.switchTo().defaultContent();

            TestUtils.waitForDisplayedElementByID(
                driver2,
                "sharedVideoPlayer",
                10);
        }
    }

    /**
     * Waits for player to be defined and checks its state.
     * @param participant the driver to operate on.
     * @param state the state to check
     */
    private void checkPlayerLoadedAndInState(
        Participant participant, String state)
    {
        TestUtils.waitForBoolean(
            participant.getDriver(),
            JS_GET_SHARED_VIDEO_CONTAINER
                + "return (c && c._sharedVideoPlayer "
                    + "&& c._sharedVideoPlayer.getPlaybackStatus() === '" + state + "');",
            15);
    }

    /**
     * Checks for url on both sides, compare with current video playing url.
     */
    private void checkUrls()
    {
        String participant1VideoUrl
            = TestUtils.executeScriptAndReturnString(
                getParticipant1().getDriver(), JS_GET_PLAYER_VIDEO_URL);

        assertTrue(
            participant1VideoUrl.contains(currentVideoId),
            "Different video url found for participant1");

        String participant2VideoUrl
            = TestUtils.executeScriptAndReturnString(
                getParticipant2().getDriver(), JS_GET_PLAYER_VIDEO_URL);

        assertTrue(
            participant2VideoUrl.contains(currentVideoId),
            "Different video url found on second participant");
    }

    /**
     * Checks that both players are playing.
     */
    private void checkRunning()
    {
        checkPlayerLoadedAndInState(
            getParticipant1(), "playing");
        checkPlayerLoadedAndInState(
            getParticipant2(), "playing");
    }

    /**
     * Pause player and check whether its paused on both sides.
     */
    @Test(dependsOnMethods = { "startSharingVideo" })
    public void pauseTest()
    {
        WebParticipant participant1 = getParticipant1();
        participant1.executeScript(
            JS_GET_SHARED_VIDEO_CONTAINER + "c._sharedVideoPlayer.pause();");

        checkPlayerLoadedAndInState(
            participant1,
            "pause");
        checkPlayerLoadedAndInState(
            getParticipant2(),
            "pause");
    }

    /**
     * Play player and check whether its playing on both sides.
     */
    @Test(dependsOnMethods = { "pauseTest" })
    public void playTest()
    {
        WebParticipant participant1 = getParticipant1();
        participant1.executeScript(
            JS_GET_SHARED_VIDEO_CONTAINER + "c._sharedVideoPlayer.play();");

        checkPlayerLoadedAndInState(
            participant1,
            "playing"
        );
        checkPlayerLoadedAndInState(
            getParticipant2(),
            "playing"
        );
    }

    /**
     * Fast-forward player and check whether its playing on both sides and its
     * positions are no more than 10 secs. away.
     */
    @Test(dependsOnMethods = { "playTest" })
    public void seekTest()
    {
        WebParticipant participant1 = getParticipant1();
        WebParticipant participant2 = getParticipant2();

        participant1.executeScript(
            JS_GET_SHARED_VIDEO_CONTAINER + "c._sharedVideoPlayer.seek(86);");

        // let's wait for the fast forward
        String waitForFastForwardScript
            = JS_GET_SHARED_VIDEO_CONTAINER
                + "return (c._sharedVideoPlayer.getPlaybackStatus() === 'playing'"
                + " && c._sharedVideoPlayer.getTime() > 86);";
        TestUtils.waitForBoolean(
            participant1.getDriver(),
            waitForFastForwardScript,
            10);
        TestUtils.waitForBoolean(
            getParticipant2().getDriver(),
            waitForFastForwardScript,
            10);

        double time1
            = getDouble(participant1.executeScript(JS_GET_PLAYER_CURRENT_TIME));
        double time2
            = getDouble(participant2.executeScript(JS_GET_PLAYER_CURRENT_TIME));

        // let's check that time difference is less than 10 seconds
        assertTrue(
            (Math.abs(time1 - time2) < 10),
            "Players time differ. Participant1:" + time1
                + ", participant2:" + time2);
    }

    private double getDouble(Object o)
    {
        double d = 0;
        if (o instanceof Double)
        {
            d = (Double) o;
        }
        else if (o instanceof Long)
        {
            d = (Long) o;
        }

        return d;
    }

    /**
     * Mutes the current volume and checks for the change on the other side
     */
    @Test(dependsOnMethods = { "playTest" })
    public void muteTest()
    {
        WebParticipant participant1 = getParticipant1();
        participant1.executeScript(
            JS_GET_SHARED_VIDEO_CONTAINER
                + "c._sharedVideoPlayer.mute();");
        // now check for volume on both sides
        TestUtils.waitForBoolean(
            participant1.getDriver(),
            JS_GET_SHARED_VIDEO_CONTAINER
                + "return (c._sharedVideoPlayer.getPlaybackStatus() === 'playing'"
                + " && c._sharedVideoPlayer.isMuted());",
            10);
        TestUtils.waitForBoolean(
            getParticipant2().getDriver(),
            JS_GET_SHARED_VIDEO_CONTAINER
                + "return (c._sharedVideoPlayer.getPlaybackStatus() === 'playing'"
                + " && (c._sharedVideoPlayer.isMuted() || c._sharedVideoPlayer.getVolume() == 0));",
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
        TestUtils.click(
            getParticipant2().getDriver(),
            By.id("sharedVideoContainer")
        );

        TestUtils.waitMillis(1000);

        assertTrue(
            getParticipant2().getDriver()
                .findElement(By.id("sharedVideoPlayer"))
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
        WebDriver driver1 = getParticipant1().getDriver();

        // video should be visible on both sides
        WebDriver driver2 = getParticipant2().getDriver();
        assertTrue(
            driver1.findElement(By.id("sharedVideoPlayer")).isDisplayed(),
            "Video not displayed:");
        assertTrue(
            driver2.findElement(By.id("sharedVideoPlayer")).isDisplayed(),
            "Video not displayed:");

        getParticipant1().getToolbar().clickSharedVideoButton();

        try
        {
            TestUtils.waitForNotDisplayedElementByID(
                driver1, "sharedVideoPlayer", 5);
        }
        catch (StaleElementReferenceException ex)
        {
            // if the element is detached in a process of checking its display
            // status, means its not visible anymore
        }
        try
        {
            TestUtils.waitForNotDisplayedElementByID(
                driver2, "sharedVideoPlayer", 5);
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
     * Participant1 leaves and we need to check that shared video is removed
     * on second participant.
     */
    @Test(dependsOnMethods = { "startSharingDifferentVideoAfterStop" })
    public void videoParticipant1LeavesTest()
    {
        getParticipant1().hangUp();

        TestUtils.waitForNotDisplayedElementByID(
            getParticipant2().getDriver(), "sharedVideoPlayer", 5);
    }

    /**
     * Start shared video before second participant joins.
     * This tests
     */
    @Test(dependsOnMethods = {"videoParticipant1LeavesTest"})
    public void shareVideoBeforeOthersJoin()
    {
        shareVideoBeforeOthersJoin(false);

        checkRunning();

        checkUrls();
    }

    /**
     * Shares the video when only participant1 is in room, and if needed pause.
     * @param pause whether to pause before starting others
     */
    private void shareVideoBeforeOthersJoin(boolean pause)
    {
        hangUpAllParticipants();

        TestUtils.waitMillis(1000);

        ensureOneParticipant();
        WebParticipant participant1 = getParticipant1();

        participant1.getToolbar().waitForSharedVideoButtonDisplay();

        // just wait a little after button is displayed
        // sometimes the button is reported as
        // Element is not clickable at point (566, -10)
        TestUtils.waitMillis(1000);

        startSharingVideoByUrl(V2_LINK, V2_VIDEO_ID, false);
        checkPlayerLoadedAndInState(participant1, "playing");

        String stateToCheck = "playing";
        if (pause)
        {
            stateToCheck = "pause";
            participant1.executeScript(
                JS_GET_SHARED_VIDEO_CONTAINER + "c._sharedVideoPlayer.seek(15);");
            participant1.executeScript(
                JS_GET_SHARED_VIDEO_CONTAINER + "c._sharedVideoPlayer.pause();");
        }

        ensureTwoParticipants();

        // Now let's check the second participant state
        Participant participant2 = getParticipant2();
        WebDriver driver2 = participant2.getDriver();
        // make sure we are in meet, not in the frame
        driver2.switchTo().defaultContent();

        checkPlayerLoadedAndInState(participant2, stateToCheck);

        assertTrue(
            driver2.findElement(By.id("sharedVideoPlayer")).isDisplayed(),
            "Video not displayed:");
    }

    @Test(dependsOnMethods = { "shareVideoBeforeOthersJoin" })
    public void sharePausedVideoBeforeOthersJoin()
    {
        shareVideoBeforeOthersJoin(true);

        WebParticipant participant1 = getParticipant1();
        WebParticipant participant2 = getParticipant2();

        checkPlayerLoadedAndInState(participant1, "pause");
        checkPlayerLoadedAndInState(participant2, "pause");

        double time1
            = getDouble(participant1.executeScript(JS_GET_PLAYER_CURRENT_TIME));
        double time2
            = getDouble(participant2.executeScript(JS_GET_PLAYER_CURRENT_TIME));

        // let's check that time difference is less than 10 seconds
        assertTrue(
            (Math.abs(time1 - time2) < 10),
            "Players time differ. Participant1:" + time1
                + " participant2:" + time2);

        checkUrls();

        stopSharingTest();
    }
}
