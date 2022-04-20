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
package org.jitsi.meet.test.util;

import org.jitsi.meet.test.pageobjects.web.ParticipantsPane;
import org.jitsi.meet.test.web.*;
import org.openqa.selenium.*;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.interactions.*;
import org.openqa.selenium.support.ui.*;

import java.util.*;

import static org.testng.Assert.*;

/**
 * Class contains utility operations specific to jitsi-meet user interface.
 *
 * @author Pawel Domas
 * @author Damian Minkov
 */
public class MeetUIUtils
{
    /**
     * The string template for finding thumbnail by endpoint id using xpath.
     */
    private static String REMOTE_THUMB_BY_ENDPOINT_ID_TMPL
        = "//span[starts-with(@id, 'participant_%s') and contains(@class,'videocontainer')]";

    /**
     * The string template for finding thumbnail by id using xpath.
     * ID can be `localVideoContainer` or "participant_" + participantEndpointId.
     */
    private static String AVATAR_XPATH_TMPL = "//span[@id='%s']//img[contains(@class,'userAvatar')]";

    /**
     * Check if on the large video there is "grey" avatar displayed and if not
     * fails the current test. The check consists of 3 steps:
     * 1. check if the "user is having networking issues" message is displayed
     *    on the large video (with 2 seconds timeout).
     * 2. Check if the avatar is displayed on the large video
     *    (with 2 seconds timeout)
     * 3. Verify that the large video HTML video element is hidden(with 2
     *    seconds timeout).
     * 4. Checks if the avatar has the grey filter class applied.
     * @param driver the <tt>WebDriver</tt> instance of the participant where
     * the check will be performed.
     */
    public static void assertGreyAvatarOnLarge(WebDriver driver)
    {
        String largeVideoXPath
            = "//video[@id='largeVideo' and @class='remoteVideoProblemFilter']";

        String largeAvatarXPath = "//div[@id='dominantSpeaker']";

        String userDisconnectedMsgXPath
            = "//div[@id='largeVideoContainer']"
                + "/span[@id='remoteConnectionMessage']";

        // Check if the message is displayed
        TestUtils.waitForDisplayedElementByXPath(
                driver, userDisconnectedMsgXPath, 2);

        // Avatar is displayed
        TestUtils.waitForDisplayedElementByXPath(
                driver, largeAvatarXPath, 2);

        // but video is not
        TestUtils.waitForNotDisplayedElementByXPath(
                driver, largeVideoXPath, 2);

        // Check if the avatar is "grey"
        WebElement avatarElement
            = driver.findElement(By.xpath(largeAvatarXPath));
        String avatarClass = avatarElement.getAttribute("class");
        assertTrue(
            avatarClass.contains("remoteVideoProblemFilter"),
            "Avatar on large video is not \"grey\": " + avatarClass);

    }

    /**
     * Checks whether the video thumbnail for given the participant with the
     * given {@code endpointId} has "grey" avatar currently displayed (which
     * means that the user is having connectivity issues and no video was
     * rendered). If the verification fails the currently running test will fail.
     *
     * @param driver the <tt>WebDriver</tt> instance where the check will
     * be performed.
     * @param endpointId the endpoint ID of the participant whose video
     * thumbnail will be verified.
     */
    public static void assertGreyAvatarDisplayed(
            WebDriver driver, String endpointId)
    {
        String avatarXPath
            = "//span[@id='participant_" + endpointId + "']"
               +  "/div[@class='avatar-container videoThumbnailProblemFilter']";

        // Avatar image
        TestUtils.waitForDisplayedElementByXPath(driver, avatarXPath, 5);

        // User's video if available should be hidden
        TestUtils.waitForElementNotPresentOrNotDisplayedByXPath(
                driver,
                "//span[@id='participant_" + endpointId + "']//video",
                5);
    }

    /**
     * Checks if the video thumbnail for the participant with the given
     * {@code endpointId} is currently displaying grey video. Which means that
     * the participant's media connection is interrupted, but at the time when
     * it happened we had some image rendered and can apply grey filter on top.
     * If the check does not succeed withing 3 seconds the currently running
     * test will fail.
     *
     * @param driver the <tt>WebDriver</tt> where the check wil be performed.
     * @param endpointId the endpoint ID of the participant whose video
     * thumbnail will be verified.
     */
    public static void assertGreyVideoThumbnailDisplayed(
            WebDriver driver, String endpointId)
    {
        assertNotNull(endpointId);

        String thumbnailXPath = "span[@id='participant_" + endpointId + "']";
        String videoFilterXPath
            = "video[@class='videoThumbnailProblemFilter']";
        String videoContainerFilterXPath
            = "div[contains(@class, 'videoThumbnailProblemFilter')"
                + " and contains(@class, 'videocontainer__video_wrapper')]";

        String greyVideoXPath
            = "//" + thumbnailXPath
                + "//" + videoFilterXPath
                + "|"
                + "//" + videoContainerFilterXPath;

        TestUtils.waitForDisplayedElementByXPath(
                driver, greyVideoXPath, 3);
    }

    /**
     * Checks if the large video is displaying with the grey filter on top.
     * Which means that the user currently displayed is having connectivity
     * issues.
     *
     * @param driver the <tt>WebDriver</tt> instance where the check will be
     * performed.
     */
    public static void assertLargeVideoIsGrey(WebDriver driver)
    {
        String largeVideoXPath
            = "//video[@id='largeVideo' and @class='remoteVideoProblemFilter']";

        String largeAvatarXPath = "//div[@id='dominantSpeaker']";

        String userDisconnectedMsgXPath
            = "//div[@id='largeVideoContainer']"
                + "/span[@id='remoteConnectionMessage']";

        // 2 seconds timeout on each check
        int timeout = 2;

        // Large video
        TestUtils.waitForDisplayedElementByXPath(
                driver, largeVideoXPath, timeout);

        // Avatar should not be visible
        TestUtils.waitForNotDisplayedElementByXPath(
                driver, largeAvatarXPath, timeout);

        // Check if the message is displayed
        TestUtils.waitForDisplayedElementByXPath(
                driver, userDisconnectedMsgXPath, timeout);
    }

    /**
     * Checks if the large video is displayed without the grey scale filter
     * applied on top.
     *
     * @param driver the <tt>WebDriver</tt> instance where the check will be
     * performed.
     */
    public static void assertLargeVideoNotGrey(WebDriver driver)
    {
        final String largeVideoXPath = "//video[@id='largeVideo']";

        String largeAvatarXPath = "//div[@id='dominantSpeaker']";

        String userDisconnectedMsgXPath
            = "//div[@id='largeVideoContainer']"
                + "/span[@id='remoteConnectionMessage']";

        // For not displayed we have to wait for key frame sometimes,
        // before the video resumes
        int timeout = 2;

        // Large video
        TestUtils.waitForCondition(
            driver,
            5,
            (ExpectedCondition<Boolean>)
                webDriver ->
                {
                    WebElement el
                        = webDriver.findElement(By.xpath(largeVideoXPath));

                    return !el.getAttribute("class").contains("remoteVideoProblemFilter");
                });

        // Check if the message is displayed
        TestUtils.waitForDisplayedOrNotByXPath(
                driver, userDisconnectedMsgXPath, timeout, false);

        // Avatar should not be visible
        TestUtils.waitForNotDisplayedElementByXPath(
                driver, largeAvatarXPath, timeout);
    }

    /**
     * Shows the toolbar and clicks on a button identified by ID.
     * @param driver the {@code WebDriver}.
     * @param buttonID the id of the button to click.
     * @param failOnMissing whether to fail if button is missing
     */
    public static void clickOnButton(
            final WebDriver driver,
            final String buttonID,
            final boolean failOnMissing)
    {
        clickOnElement(driver, '#' + buttonID, failOnMissing);
    }

    /**
     * Clicks on a button specified by selector (using document.querySelector).
     * @param driver the {@code WebDriver}.
     * @param selector the button selector to click.
     * @param failOnMissing whether to fail if button is missing
     */
    public static void clickOnElement(
        final WebDriver driver,
        final String selector,
        final boolean failOnMissing)
    {
        Object clickResult = ((JavascriptExecutor) driver).executeScript(
            "try {" +
                "var e = document.querySelector('" + selector + "');" +
                "if (!e) { return false;}" +
                "APP.debugLogs.getLogs().push(new Date().toISOString() + ' [MeetUIUtils] Clicking on: "
                    + selector + "');" +
                "e.click();" +
                "return true;" +
                "} catch (err) { return 'error: ' + err; }");

        if (clickResult != null
            && clickResult.equals(Boolean.FALSE))
        {
            if (failOnMissing)
            {
                throw new NoSuchElementException(
                    "No element found for:" + selector);
            }

            // there is no element, so its not visible, just continue
            // cause failOnMissing is false
            TestUtils.print("Element is missing:" + selector);
        }
        else if (clickResult != null
            && !clickResult.equals(Boolean.TRUE))
        {
            TestUtils.print("Error clicking element:" + selector
                + " " + clickResult);
        }
    }

    /**
     * Shows the toolbar and clicks on a button identified by ID.
     * @param driver the {@code WebDriver}.
     * @param buttonID the id of the button to click.
     */
    public static void clickOnToolbarButton(
            WebDriver driver, String buttonID)
    {
        clickOnButton(driver, buttonID, true);
    }

    /**
     * Returns resource part of the JID of the user who is currently displayed
     * in the large video area in {@code participant}.
     *
     * @param driver <tt>WebDriver</tt> of the participant.
     * @return the resource part of the JID of the user who is currently
     * displayed in the large video area in {@code participant}.
     */
    public static String getLargeVideoResource(WebDriver driver)
    {
        return (String)((JavascriptExecutor) driver)
                .executeScript("return APP.UI.getLargeVideoID();");
    }

    /**
     * Returns <video> element for the local video.
     * @param driver the <tt>WebDriver</tt> from which local video element
     * will be obtained.
     * @return <tt>WebElement</tt> of the local video.
     */
    public static WebElement getLocalVideo(WebDriver driver)
    {
        List<WebElement> thumbs
            = driver.findElements(
                By.xpath("//video[starts-with(@id, 'localVideo_')]"));

        return thumbs.get(0);
    }

    /**
     * Get's the id of local video element.
     * @param driver the <tt>WebDriver</tt> instance of the participant for
     * whom we want to obtain local video element's ID
     * @return a <tt>String</tt> with the ID of the local video element.
     */
    public static String getLocalVideoID(WebDriver driver)
    {
        return getLocalVideo(driver).getAttribute("id");
    }

    /**
     * Returns all remote video elements for given <tt>WebDriver</tt> instance.
     * @param driver the <tt>WebDriver</tt> instance which will be used to
     * obtain remote video elements.
     * @return a list of <tt>WebElement</tt> with the remote videos.
     */
    public static List<WebElement> getRemoteVideos(WebDriver driver)
    {
        return driver.findElements(
                By.xpath("//video[starts-with(@id, 'remoteVideo_')]"));
    }

    /**
     * Obtains the ids for all remote participants <video> elements.
     * @param driver the <tt>WebDriver</tt> instance for which remote video
     * ids will be fetched.
     * @return a list of <tt>String</tt> with the ids of remote participants
     * video elements.
     */
    public static List<String> getRemoteVideoIDs(WebDriver driver)
    {
        List<WebElement> remoteThumbs = getRemoteVideos(driver);

        List<String> ids = new ArrayList<>();
        for (WebElement thumb : remoteThumbs)
        {
            ids.add(thumb.getAttribute("id"));
        }

        return ids;
    }

    /**
     * Displays the filmstrip, if not displayed.
     *
     * @param driver <tt>WebDriver</tt> instance of the participant for
     * whom we'll try to open the settings panel.
     * @throws TimeoutException if we fail to open the settings panel.
     */
    public static void displayFilmstripPanel(WebDriver driver)
    {
        String filmstripXPath = "//div[@id='remoteVideos' and @class='hidden']";
        WebElement filmstrip;

        try
        {
            filmstrip = driver.findElement(By.xpath(filmstripXPath));
        }
        catch (NoSuchElementException ex)
        {
            filmstrip = null;
        }

        if (filmstrip != null)
        {
            clickOnToolbarButton(driver, "toolbar_film_strip");

            TestUtils.waitForElementNotPresentByXPath(
                    driver, filmstripXPath, 5);
        }
    }

    /**
     * Asserts that {@code participant} shows or doesn't show the audio or
     * video mute icon for the conference participant identified by
     * {@code resource}.
     *
     * @param name the name that will be displayed in eventual failure message.
     * @param observer the {@code WebDriver} where the check will be
     * performed.
     * @param testee the <tt>WebDriver</tt> of the participant for whom we're
     *               checking the status of audio muted icon.
     * @param isMuted if {@code true}, the method will assert the presence of
     * the "mute" icon; otherwise, it will assert its absence.
     */
    public static void assertMuteIconIsDisplayed(
            WebDriver observer,
            WebDriver testee,
            boolean isMuted,
            String name)
    {
        String id;
        if (testee != observer)
        {
            String resource = MeetUtils.getResourceJid(testee);
            id = "participant_" + resource;
        }
        else
        {
            id = "localVideoContainer";
        }

        String mutedIconXPath
            = "//span[@id='%s']//span[contains(@id, '%sMuted')]"
                + "//*[local-name() = 'svg' and @id = '%s-disabled']";

        mutedIconXPath = String.format(mutedIconXPath, id, "audio", "mic");

        try
        {
            if (isMuted)
            {
                TestUtils.waitForElementByXPath(observer, mutedIconXPath, 5);
            }
            else
            {
                TestUtils.waitForElementNotPresentOrNotDisplayedByXPath(
                    observer, mutedIconXPath, 5);
            }
        }
        catch (TimeoutException exc)
        {
            fail(
                name + (isMuted ? " should" : " shouldn't")
                    + " be muted at this point, xpath: " + mutedIconXPath);
        }
    }

    /**
     * For the participant to have his audio muted/unmuted from given observer's
     * perspective. The method will fail the test if something goes wrong or
     * the audio muted status is different than the expected one. We wait up to
     * 3 seconds for the expected status to appear.
     *
     * @param observer <tt>WebDriver</tt> instance of the participant that
     * observes the audio status
     * @param testee <tt>WebDriver</tt> instance of the participant for whom we're
     * checking the audio muted status.
     * @param testeeName the name of the testee that will be printed in failure
     * logs
     * @param muted <tt>true</tt> to wait for audio muted status or
     * <tt>false</tt> to wait for the participant to unmute.
     */
    public static void waitForAudioMuted(final WebDriver observer,
                                         final WebDriver testee,
                                         final String testeeName,
                                         final boolean muted)
    {
        // Waits for the correct icon
        assertMuteIconIsDisplayed(
            observer,
            testee,
            muted,
            testeeName);

        // The code below check verifies audio muted status by checking the
        // participant's audio levels on the observer side. Statistics support
        // is required to do that
        if (!MeetUtils.areRtpStatsSupported(observer))
        {
            return;
        }

        // Extended timeout for 'unmuted' to make tests more resilient to
        // unexpected glitches.
        int timeout = muted ? 3 : 6;

        // Give it 3 seconds to not get any audio or to receive some
        // depending on "muted" argument
        try
        {
            TestUtils.waitForCondition(
                observer,
                timeout,
                (ExpectedCondition<Boolean>) webDriver -> {
                    Double audioLevel
                        = MeetUtils.getRemoteAudioLevel(webDriver, testee);

                    // NOTE: audioLevel == null also when it is 0

                    // When testing for muted we want audio level to stay
                    // 'null' or 0, so we wait to timeout this condition
                    if (muted)
                    {
                        if (audioLevel != null && audioLevel > 0.1)
                        {
                            TestUtils.print(
                                    "muted exiting on: " + audioLevel);
                            return true;
                        }
                        else
                        {
                            return false;
                        }
                    }
                    // When testing for unmuted we wait for first sound
                    else
                    {
                        if (audioLevel != null && audioLevel > 0.1)
                        {
                            TestUtils.print(
                                    "unmuted exiting on: " + audioLevel);
                            return true;
                        }
                        else
                        {
                            return false;
                        }
                    }
                }
            );
            // When testing for muted we don't want to have
            // the condition succeeded
            if (muted)
            {
                fail("There was some sound coming from muted: '" +
                    testeeName + "'");
            }
            // else we're good for unmuted participant
        }
        catch (TimeoutException timeoutExc)
        {
            if (!muted)
            {
                fail("There was no sound from unmuted: '" +
                    testeeName + "'");
            }
            // else we're good for muted participant
        }
    }

    /**
     * Makes sure that a user's avatar is displayed, and that the user's video
     * is not displayed. Allows for the these conditions to not hold
     * immediately, but within a time interval of 5 seconds.
     * @param driver instance of <tt>WebDriver</tt> on which we'll perform
     * the verification.
     * @param endpointId the resource part of MUC JID which identifies user's
     * video thumbnail.
     */
    public static void assertAvatarDisplayed(
            WebDriver driver, String endpointId)
    {
        // Avatar image
        TestUtils.waitForDisplayedElementByXPath(
            driver, MeetUIUtils.getAvatarXpathForParticipant(endpointId),
            5);

        // User's video if available should be hidden, the element is missing
        // if remote participant started muted when we joined
        String videoElementXPath
            = "//span[@id='participant_" + endpointId + "']//video";
        List<WebElement> videoElems
            = driver.findElements(By.xpath(videoElementXPath));
        if (videoElems.size() > 0)
        {
            TestUtils.waitForNotDisplayedElementByXPath(
                driver, videoElementXPath, 5);
        }
    }

    /**
     * Makes sure that a user's display name is displayed.
     * @param driver instance of <tt>WebDriver</tt> on which we'll perform
     * the verification.
     * @param endpointId the resource part of MUC JID which identifies user's
     * video thumbnail.
     */
    public static void assertDisplayNameVisible(
            WebDriver driver, String endpointId)
    {
        // Display name - visible
        TestUtils.waitForDisplayedElementByXPath(
            driver,
            "//span[@id='participant_" + endpointId + "']" +
                "//span[starts-with(@class, 'displayname')]",
            5);
    }

    /**
     * Helper for formatting the string to be used as a CSS selector for
     * an accessibility label.
     *
     * @param accessibilityLabel The accessibility label to be used to search
     * for a WebElement on the page.
     * @return String intended to be used with By#cssSelector.
     */
    public static String getAccessibilityCSSSelector(String accessibilityLabel)
    {
        return String.format("[aria-label=\"%s\"]", accessibilityLabel);
    }

    /**
     * Helper for formatting the string to be used as a CSS selector for
     * a test id.
     *
     * @param testId The test id to be used to search
     * for a WebElement on the page.
     * @return String intended to be used with By#cssSelector.
     */
    public static String getTestIdCSSSelector(String testId)
    {
        return String.format("[data-testid=\"%s\"]", testId);
    }

    /**
     * Retrieves a remote participant display name.
     *
     * @param driver The driver where to check for the display name.
     * @param remoteParticipantEndpointId The participant id which display name to be returned.
     * @return The current display name of a participant.
     */
    public static String getRemoteDisplayName(WebDriver driver, String remoteParticipantEndpointId)
    {
        WebElement displayNameElem = driver.findElement(By.xpath(
            "//span[@id='participant_" + remoteParticipantEndpointId + "']//span[@id='participant_" +
                    remoteParticipantEndpointId + "_name']"));
        Actions action = new Actions(driver);
        action.moveToElement(displayNameElem);
        action.build().perform();

        return displayNameElem.getText();
    }

    /**
     * Clicks on a given participant's (identified by the resource part of it's
     * JID) video thumbnail.
     * @param driver instance of <tt>WebDriver</tt> where we'll perform the
     * click.
     * @param endpointId the resource part of MUC JID which identifies user's
     * video thumbnail.
     */
    public static void clickOnRemoteVideo(
            WebDriver driver, String endpointId)
    {
        driver.findElement(
            By.xpath("//span[@id='participant_" + endpointId + "']")).click();
    }

    /**
     * Clicks on the local video thumbnail.
     * @param driver the instance of <tt>WebDriver</tt> where we'll
     * perform the click.
     */
    public static void clickOnLocalVideo(WebDriver driver)
    {
        TestUtils.executeScript(
            driver,
            "document.getElementById('localVideoContainer').click()");
    }

    /**
     * Makes sure that the local video is displayed in the local thumbnail and
     * that the avatar is not displayed.
     * @param driver the instance of <tt>WebDriver</tt> on which we'll
     * perform the verification.
     */
    public static void assertLocalThumbnailShowsVideo(WebDriver driver)
    {
        TestUtils.waitForNotDisplayedElementByXPath(
            driver,
            MeetUIUtils.getAvatarXpathForLocal(),
            5);
        TestUtils.waitForDisplayedElementByXPath(
            driver, "//span[@id='localVideoWrapper']//video", 5);
    }

    /**
     * Makes sure that the avatar is displayed in the local thumbnail and that
     * the video is not displayed.
     * @param driver the instance of <tt>WebDriver</tt> on which we'll
     * perform the verification.
     */
    public static void assertLocalThumbnailShowsAvatar(WebDriver driver)
    {
        TestUtils.waitForDisplayedElementByXPath(
            driver,
            MeetUIUtils.getAvatarXpathForLocal(),
            5);
        TestUtils.waitForElementNotPresentOrNotDisplayedByXPath(
            driver, "//span[@id='localVideoWrapper']//video", 5);
    }

    /**
     * Returns the source of the large video currently shown.
     * @return the source of the large video currently shown.
     */
    public static String getLargeVideoID(WebDriver driver)
    {
        WebElement videoElement = driver.findElement(By.id("largeVideo"));

        return getVideoElementID(driver, videoElement);
    }

    /**
     * Checks whether the large video has changed to the desired resource.
     * @param driver the driver to check
     * @param endpointId the expected resource
     */
    public static void waitsForLargeVideoSwitch(
        WebDriver driver, final String endpointId)
    {
        TestUtils.waitForCondition(
            driver,
            5,
            (ExpectedCondition<Boolean>)
                d -> endpointId.equals(getLargeVideoResource(d)));
    }

    /**
     * Returns the identifier of the video element.
     *
     * @param driver the driver
     * @param element the video element
     * @return unique identifier.
     */
    public static String getVideoElementID(WebDriver driver, WebElement element)
    {
        Object res = ((JavascriptExecutor) driver)
            .executeScript(
                "var el = arguments[0]; " +
                    "var srcObject = el.srcObject || el.mozSrcObject;" +
                    "return srcObject? srcObject.id : el.src;", element);
        return (String)res;
    }

    /**
     * Returns list of participant's thumbnails.
     * @param participant the instance of <tt>WebDriver</tt>
     * @return list of thumbnails <tt>WebElement</tt> elements
     */
    public static List<WebElement> getThumbnails(WebDriver participant)
    {
        return participant.findElements(By.xpath(
            "//div[@id='remoteVideos']/span[contains(@class,'videocontainer')]"));
    }

    /**
     * Returns list of currently visible participant's thumbnails.
     * @param participant the instance of <tt>WebDriver</tt>
     * @return list of thumbnails <tt>WebElement</tt> elements
     */
    public static List<WebElement> getVisibleThumbnails(WebDriver participant)
    {
        List<WebElement> thumbnails = getThumbnails(participant);

        thumbnails.removeIf(thumb -> !thumb.isDisplayed());

        return thumbnails;
    }

    /**
     * Checks if observer's active speaker is testee.
     * @param observer <tt>WebDriver</tt> instance of the participant that
     * should check active speaker.
     * @param testee <tt>WebDriver</tt> instance of the participant for may be
     * active speaker for the observer.
     * @return <tt>true</tt> true if observer's active speaker is testee,
     * <tt>false</tt> otherwise.
     */
    public static boolean isActiveSpeaker(
        WebDriver observer, WebDriver testee)
    {
        final String expectedJid =
            MeetUtils.getResourceJid(testee);

        try
        {
            TestUtils.waitForCondition(
                observer,
                5,
                (ExpectedCondition<Boolean>)
                    d ->
                    {
                        String currentJid = MeetUIUtils.getLargeVideoResource(d);

                        return expectedJid.equals(currentJid);
                    });

            return true;
        }
        catch (TimeoutException ignored)
        {
            return false;
        }
    }

    /**
     * Click on the local video thumbnail and waits until it's displayed on
     * "the large video".
     *
     * @param driver the <tt>WebDriver</tt> instance on which the action is to
     * be performed.
     */
    static public void selectLocalVideo(WebDriver driver)
    {
        // click on local
        MeetUIUtils.clickOnLocalVideo(driver);

        WebElement localVideoElem
            = driver.findElement(
                    By.xpath("//span[@id='localVideoWrapper']//video"));

        final String largeVideoID
            = MeetUIUtils.getVideoElementID(driver, localVideoElem);

        waitForLargeVideoSwitch(driver, largeVideoID);
    }

    /**
     * Selects the remote video to be displayed on "the large video" by clicking
     * participant's video thumbnail and waits for the switch to take place
     * using {@link #waitForLargeVideoSwitch(WebDriver, String)}.
     *
     * @param driver the <tt>WebDriver</tt> instance on which the selection will
     * be performed.
     * @param endpointId the <tt>WebDriver</tt> instance which belongs to
     * the participant that is to be displayed on "the large video". Is used to
     * obtain user's identifier.
     */
    static public void selectRemoteVideo(WebDriver driver,
                                         String endpointId)
    {
        String remoteThumbXpath = String.format(REMOTE_THUMB_BY_ENDPOINT_ID_TMPL, endpointId);

        WebElement remoteThumbnail
            = TestUtils.waitForElementByXPath(
                    driver, remoteThumbXpath, 5,
                    "Remote thumbnail not found: " + remoteThumbXpath);

        // click on remote
        remoteThumbnail.click();

        waitForLargeVideoSwitchToEndpoint(driver, endpointId);
    }

    /**
     * Waits for large video to switch to video for the endpoint id.
     * @param driver the <tt>WebDriver</tt> instance on which the wait will
     * be performed.
     * @param endpointId the <tt>WebDriver</tt> instance which belongs to
     * the participant that is to be displayed on "the large video".
     */
    static public void waitForLargeVideoSwitchToEndpoint(WebDriver driver, String endpointId)
    {
        String remoteThumbVideoXpath
            = String.format(REMOTE_THUMB_BY_ENDPOINT_ID_TMPL, endpointId) + "//video[starts-with(@id, 'remoteVideo_')]";

        WebElement remoteThumbnailVideo
            = TestUtils.waitForElementByXPath(
                    driver, remoteThumbVideoXpath, 5,
                    "Remote video not found: " + remoteThumbVideoXpath);

        String videoSrc = getVideoElementID(driver, remoteThumbnailVideo);

        waitForLargeVideoSwitch(driver, videoSrc);
    }

    /**
     * Sets an attribute on an element
     * @param element the element
     * @param attributeName the attribute name to set
     * @param attributeValue the value to set
     */
    public static void setAttribute(
        WebDriver driver,
        WebElement element, String attributeName, String attributeValue)
    {
        ((JavascriptExecutor)driver).executeScript(
            "arguments[0][arguments[1]] = arguments[2];",
            element, attributeName, attributeValue);
    }

    /**
     * Waits up to 3 seconds for the large video update to happen.
     *
     * @param driver the <tt>WebDriver</tt> instance where the large video update
     * will be performed.
     * @param expectedVideoSrc a string which identifies the video stream
     * (the source set on HTML5 video element to attach WebRTC media stream).
     */
    public static void waitForLargeVideoSwitch(
        WebDriver driver,
        final String expectedVideoSrc)
    {
        new WebDriverWait(driver, 3)
            .withMessage(
                    "Failed to switch the large video at: "
                        + MeetUtils.getResourceJid(driver)
                        + " to : " + expectedVideoSrc)
            .until(
                (ExpectedCondition<Boolean>) where -> expectedVideoSrc.equals(
                    MeetUIUtils.getLargeVideoID(where)));
    }

    /**
     * Verifies the UI indication of remote participant's media connection
     * status. If the verification fails the currently running test will fail.
     *
     * @param driver the <tt>WebDriver</tt> instance where the check will be
     * performed.
     * @param endpointId the endpoint ID of the participant whose connection
     * status will be checked.
     * @param isConnected tells whether the check is for
     * connected(<tt>true</tt>) or disconnected(<tt>false</tt>).
     */
    public static void verifyUserConnStatusIndication(
            WebDriver driver, String endpointId, boolean isConnected)
    {
        assertNotNull(endpointId);

        // may take time for the video to recover(key frame) than disrupt
        int timeout = isConnected ? 60 : 15;

        // Wait for the logic to tell that the user is disconnected
        TestUtils.waitForBoolean(
                driver,
                MeetUtils.isConnectionActiveScript(endpointId, isConnected),
                timeout);

        // Check connection status indicators
        String connectionLostXpath
            = "//span[@id='participant_" + endpointId
                + "']//span[@class='connection_lost']";

        // Check "connection lost" icon
        TestUtils.waitForDisplayedOrNotByXPath(
                driver, connectionLostXpath, 2, !isConnected);
    }

    /**
     * Checks whether the remote participant's media connection status is ninja.
     *
     * @param driver the <tt>WebDriver</tt> instance where the check will be
     * performed.
     * @param endpointId the endpoint ID of the participant whose connection
     * status will be checked.
     * @return <tt>true</tt> if the indicator is ninja.
     */
    public static boolean hasNinjaUserConnStatusIndication(WebDriver driver, String endpointId)
    {
        assertNotNull(endpointId);

        // Check "connection ninja" icon
        return driver.findElements(
            By.xpath("//span[@id='participant_" + endpointId + "']//span[@class='connection_ninja']"))
                .size() > 0;
    }

    /**
     * Waits for ninja icon to appear in remote videos.
     * @param driver the <tt>WebDriver</tt> instance where the check will be
     * performed.
     */
    public static void waitForNinjaIcon(WebDriver driver)
    {
        TestUtils.waitForElementByXPath(driver,
            "//span[contains(@class,'videocontainer')]//span[contains(@class,'connection_ninja')]", 5);
    }

    /**
     * Waits for ninja icon to appear in endpoint's remote videos.
     * @param endpointId the endpoint ID of the participant whose connection
     * status will be checked.
     * @param driver the <tt>WebDriver</tt> instance where the check will be
     * performed.
     */
    public static void waitForNinjaIcon(WebDriver driver, String endpointId)
    {
        TestUtils.waitForElementByXPath(driver,
            "//span[@id='participant_" + endpointId + "']//span[@class='connection_ninja']", 15);
    }

    /**
     * Waits for dominant speaker icon to appear in remote video of a participant.
     * @param driver the <tt>WebDriver</tt> instance where the check will be
     * performed.
     * @param endpointId the endpoint ID of the participant whose dominant speaker icon
     * status will be checked.
     */
    public static void waitForDominantSpeaker(WebDriver driver, String endpointId)
    {
        TestUtils.waitForElementByXPath(driver,
            "//span[@id='participant_" + endpointId + "' and contains(@class, 'dominant-speaker')]", 5);
    }

    /**
     * Waits for remote video state - receiving or not receive for a participant.
     * @param driver the <tt>WebDriver</tt> instance where the check will be
     * performed.
     * @param endpointId the endpoint ID of the participant which video will be checked.
     */
    public static void waitForRemoteVideo(WebDriver driver, String endpointId, boolean received)
    {
        TestUtils.waitForBoolean(driver,
            "return " + (received ? "" : "!" ) +"JitsiMeetJS.app.testing.isRemoteVideoReceived('" + endpointId + "');",
            10);
    }

    /**
     * This method verifies whether or not given participant is having
     * connectivity issues, but in a different way that it's done in
     * {@link MeetUIUtils#verifyUserConnStatusIndication(WebDriver, String, boolean)}
     * It will wait for 5 seconds and fail if the connection status will differ
     * from the expected one.
     *
     * If we're looking to verify if the user is connected this method will make
     * pass only if  that connection stayed "connected" for 5 seconds and was
     * never interrupted during that period. In other words it's to make sure
     * that the opposite state never kicks in at any point during
     * the verification period (5 seconds).
     *
     * @param driver the participant where the other participant is checked
     * @param endpointId the endpoint ID of the participant to be checked
     * @param isConnected <tt>true</tt> if we're making sure that the connection
     * remains connected. <tt>false</tt> to check if the connection stays
     * disconnected.
     */
    public static void verifyUserConnStatusIndicationLong(
        WebDriver driver, String endpointId, boolean isConnected)
    {
        // 5 seconds
        long timeout = 5;

        assertNotNull(endpointId);

        // Wait for the logic to tell that the user is disconnected
        try
        {
            TestUtils.waitForBoolean(
                driver,
                MeetUtils.isConnectionActiveScript(endpointId, !isConnected),
                timeout);

            fail(
                endpointId
                    + (isConnected ? " was " : "was not ")
                    + "having connectivity issues "
                    + "(according to the 'isConnectionActiveScript')");

        }
        catch (TimeoutException e)
        {
            // OK
        }

        // Check connection status indicators
        String connectionLostXpath
            = "//span[@id='participant_" + endpointId
                + "']//span[@class='connection_lost']";

        // Check "connection lost" icon
        try
        {
            TestUtils.waitForDisplayedOrNotByXPath(
                driver, connectionLostXpath, 1, !isConnected);
        }
        catch (TimeoutException e)
        {
            fail("the connection problems status icon for "
                + endpointId
                + (isConnected ? "did not" : " did ")
                + "appear.");
        }
    }

    /**
     * Verifies the UI indication of local media connection status. If
     * the verification fails the currently running test will fail.
     *
     * @param driver the <tt>WebDriver</tt> instance where the check will be
     * performed.
     * @param isConnected tells whether the check is for
     * connected(<tt>true</tt>) or disconnected(<tt>false</tt>).
     */
    public static void verifyLocalConnStatusIndication(
            WebDriver driver, boolean isConnected)
    {
        // Wait for the logic to reflect the status first
        TestUtils.waitForBoolean(
                driver,
                MeetUtils.isLocalConnectionActiveScript(isConnected),
                15);

        // Check connection status indicators
        String connectionLostXpath
            = "//span[@id='localVideoContainer']"
                + "//span[@class='connection_lost']";

        // Check "connection lost" icon
        TestUtils.waitForDisplayedOrNotByXPath(
                driver, connectionLostXpath, 3, !isConnected);
    }

    /**
     * Mutes participant's video and checks local indication for that.
     * @param participant the participant's video to be muted.
     * @param participantCheck another participant in the room which we want to
     * test whether he is seeing the muted participant as really muted. Can be
     * null if we want to skip this check.
     */
    public static void muteVideoAndCheck(WebParticipant participant,
                                         WebParticipant participantCheck)
    {
        WebDriver driver = participant.getDriver();

        // Mute participant1's video
        participant.getToolbar().waitForVideoMuteButtonDisplay();
        participant.getToolbar().clickVideoMuteButton();

        // Check if local video muted icon appears on local thumbnail
        participant.getParticipantsPane().assertIsParticipantVideoMuted(participant, true);

        if (participantCheck != null)
        {
            participantCheck.getParticipantsPane().assertIsParticipantVideoMuted(participant, true);
        }
    }

    /**
     * Unmutes <tt>participant</tt>'s video, checks if the local UI has been
     * updated accordingly and then does the verification from
     * the <tt>participantCheck</tt> perspective.
     * @param participant the {@link WebParticipant} of the participant to be
     * "video unmuted"
     * @param participantCheck the {@link WebParticipant} of the participant
     * which observes and checks if the video has been unmuted correctly.
     */
    public static void unmuteVideoAndCheck(WebParticipant participant,
                                           WebParticipant participantCheck)
    {
        participant.getParticipantsPane().assertIsParticipantVideoMuted(participant, true);

        // Make sure that there is the video mute button
        participant.getToolbar().waitForVideoMuteButtonDisplay();

        // Mute participant's video
        participant.getToolbar().clickVideoMuteButton();

        // Check if local video muted icon disappeared
        participant.getParticipantsPane().assertIsParticipantVideoMuted(participant, false);

        if (participantCheck != null)
        {
            participantCheck.getParticipantsPane().assertIsParticipantVideoMuted(participant, false);
        }
    }

    /**
     * Toggles mute/unmute <tt>participant</tt>'s video, checks if the local UI has been
     * updated accordingly and then does the verification from
     * the <tt>participantCheck</tt> perspective.
     * @param participant the {@link WebParticipant} of the participant to be
     * "video unmuted"
     * @param participantCheck the {@link WebParticipant} of the participant
     * which observes and checks if the video has been unmuted correctly.
     * @param isMuted the {@link Boolean} of the participant
     * which checks if the audio is muted.
     */
    public static void toggleVideoAndCheck(WebParticipant participant,
                                           WebParticipant participantCheck,
                                           Boolean isMuted)
    {
        WebDriver driver = participant.getDriver();

        // Make sure that there is the audio mute button
        participant.getToolbar().waitForVideoMuteButtonDisplay();

        // Toggle participant's video
        participant.getToolbar().clickVideoMuteButton();

        // Check local video muted icon state
        participant.getParticipantsPane().assertIsParticipantVideoMuted(participant, isMuted);

        if (participantCheck != null)
        {
            participantCheck.getParticipantsPane().assertIsParticipantVideoMuted(participant, isMuted);
        }
    }

    /**
     * Toggles mute/unmute <tt>participant</tt>'s audio, checks if the local UI has been
     * updated accordingly and then does the verification from
     * the <tt>participantCheck</tt> perspective.
     * @param participant the {@link WebParticipant} of the participant to be
     * "audio unmuted"
     * @param participantCheck the {@link WebParticipant} of the participant
     * which observes and checks if the audio has been unmuted correctly.
     * @param isMuted the {@link Boolean} of the participant
     * which checks if the audio is muted.
     */
    public static void toggleAudioAndCheck(WebParticipant participant,
                                           WebParticipant participantCheck,
                                           Boolean isMuted)
    {
        WebDriver driver = participant.getDriver();

        // Make sure that there is the audio mute button
        participant.getToolbar().waitForAudioMuteButtonDisplay();

        // Toggle participant's audio
        participant.getToolbar().clickAudioMuteButton();

        // Check local audio muted icon state
        assertMuteIconIsDisplayed(
                driver, driver, isMuted, participant.getName());

        if (participantCheck != null)
        {
            MeetUIUtils.assertMuteIconIsDisplayed(
                    participantCheck.getDriver(),
                    driver,
                    isMuted,
                    "");
        }
    }

    /**
     * Checks whether a participant is viewing the conference in tile view.
     *
     * @param participant the <tt>WebParticipant</tt> of the participant for
     *                    whom we're check is in tile view.
     * @param isDisplayed true if the expectation is tile view is to be visible.
     */
    public static void waitForTileViewDisplay(
        WebParticipant participant,
        boolean isDisplayed)
    {
        TestUtils.waitForCondition(
            participant.getDriver(),
            10,
            (ExpectedCondition<Boolean>) d -> {
                WebDriver driver = participant.getDriver();
                WebElement appRoot
                    = driver.findElement(By.id("videoconference_page"));
                boolean currentDisplay
                    = appRoot.getAttribute("class").contains("tile-view");

                return currentDisplay == isDisplayed;
            });
    }

    /**
     * The xpath for participant avatar.
     * @param endpointId the endpoint Id.
     * @return the xpath.
     */
    public static String getAvatarXpathForParticipant(String endpointId)
    {
        return String.format(AVATAR_XPATH_TMPL, "participant_" + endpointId);
    }

    /**
     * The xpath for the local participant avatar.
     * @return the xpath.
     */
    public static String getAvatarXpathForLocal()
    {
        return String.format(AVATAR_XPATH_TMPL, "localVideoContainer");
    }
}
