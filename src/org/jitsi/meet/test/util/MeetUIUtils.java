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

import org.openqa.selenium.*;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.interactions.*;
import org.openqa.selenium.support.ui.*;

import java.util.*;

import static org.junit.Assert.*;

/**
 * Class contains utility operations specific to jitsi-meet user interface.
 *
 * @author Pawel Domas
 * @author Damian Minkov
 */
public class MeetUIUtils
{
    /**
     * Check if on the large video there is "grey" avatar displayed and if not
     * fails the current test. The check consists of 3 steps:
     * 1. check if the "user is having networking issues" message is displayed
     *    on the large video(with 2 seconds timeout).
     * 2. Check if the avatar is displayed on the large video
     *    (with 2 seconds timeout)
     * 3. Verify that the large video HTML video element is hidden(with 2
     *    seconds timeout).
     * 4. Checks if the avatar has the grey filter class applied.
     * @param where the <tt>WebDriver</tt> instance of the participant where
     * the check will be performed.
     */
    public static void assertGreyAvatarOnLarge(WebDriver where)
    {
        String largeVideoXPath
            = "//video[@id='largeVideo' and @class='remoteVideoProblemFilter']";

        String largeAvatarXPath = "//div[@id='dominantSpeaker']";

        String userDisconnectedMsgXPath
            = "//div[@id='largeVideoContainer']"
                + "/span[@id='remoteConnectionMessage']";

        // Check if the message is displayed
        TestUtils.waitForDisplayedElementByXPath(
                where, userDisconnectedMsgXPath, 2);

        // Avatar is displayed
        TestUtils.waitForDisplayedElementByXPath(
                where, largeAvatarXPath, 2);

        // but video is not
        TestUtils.waitForNotDisplayedElementByXPath(
                where, largeVideoXPath, 2);

        // Check if the avatar is "grey"
        WebElement avatarElement
            = where.findElement(By.xpath(largeAvatarXPath));
        String avatarClass = avatarElement.getAttribute("class");
        assertTrue(
                "Avatar on large video is not \"grey\": " + avatarClass,
                avatarClass.contains("remoteVideoProblemFilter"));

    }

    /**
     * Checks whether the video thumbnail for given {@code peer} has "grey"
     * avatar currently displayed(which means that the user is having
     * connectivity issues and no video was rendered). If the verification fails
     * the currently running test will fail.
     *
     * @param observer the <tt>WebDriver</tt> instance where the check will
     * be performed.
     * @param peer the <tt>WebDriver</tt> instance of the participant whose
     * video thumbnail will be verified.
     */
    public static void assertGreyAvatarDisplayed(
            WebDriver observer, WebDriver peer)
    {
        String resource = MeetUtils.getResourceJid(peer);
        String avatarXPath
            = "//span[@id='participant_" + resource
                + "']/img[@class='userAvatar videoThumbnailProblemFilter']";

        // Avatar image
        TestUtils.waitForDisplayedElementByXPath(observer, avatarXPath, 5);

        // User's video if available should be hidden
        TestUtils.waitForElementNotPresentOrNotDisplayedByXPath(
                observer,
                "//span[@id='participant_" + resource + "']/video", 5);
    }

    /**
     * Checks if the video thumbnail for given <tt>peer</tt> is currently
     * displaying grey video. Which means that the user's media connection is
     * interrupted, but at the time when it happened we had some image rendered
     * and can apply grey filter on top. If the check does not succeed withing
     * 3 seconds the currently running test will fail.
     *
     * @param observer the <tt>WebDriver</tt> where the check wil be performed.
     * @param peer the <tt>WebDriver</tt> instance of the user who whose
     * thumbnail is to be verified.
     */
    public static void assertGreyVideoThumbnailDisplayed(
            WebDriver observer, WebDriver peer)
    {
        String peerId = MeetUtils.getResourceJid(peer);
        assertNotNull(peerId);

        String greyVideoXPath
            = "//span[@id='participant_" + peerId
                + "']/video[@class='videoThumbnailProblemFilter']";

        TestUtils.waitForDisplayedElementByXPath(
                observer, greyVideoXPath, 3);
    }

    /**
     * Checks if the large video is displaying with the grey filter on top.
     * Which means that the user currently displayed is having connectivity
     * issues.
     *
     * @param where the <tt>WebDriver</tt> instance where the check will be
     * performed.
     */
    public static void assertLargeVideoIsGrey(WebDriver where)
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
                where, largeVideoXPath, timeout);

        // Avatar should not be visible
        TestUtils.waitForNotDisplayedElementByXPath(
                where, largeAvatarXPath, timeout);

        // Check if the message is displayed
        TestUtils.waitForDisplayedElementByXPath(
                where, userDisconnectedMsgXPath, timeout);
    }

    /**
     * Checks if the large video is displayed without the grey scale filter
     * applied on top.
     *
     * @param where the <tt>WebDriver</tt> instance where the check will be
     * performed.
     */
    public static void assertLargeVideoNotGrey(WebDriver where)
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
        TestUtils.waitForCondition(where, 5, new ExpectedCondition<Boolean>()
        {
            @Override
            public Boolean apply(WebDriver webDriver)
            {
                WebElement el
                    = webDriver.findElement(
                            By.xpath(largeVideoXPath));

                return !el.getAttribute("class")
                          .contains("remoteVideoProblemFilter");
            }
        });

        // Check if the message is displayed
        TestUtils.waitForDisplayedOrNotByXPath(
                where, userDisconnectedMsgXPath, timeout, false);

        // Avatar should not be visible
        TestUtils.waitForNotDisplayedElementByXPath(
                where, largeAvatarXPath, timeout);
    }

    /**
     * Shows the toolbar and clicks on a button identified by ID.
     * @param participant the {@code WebDriver}.
     * @param buttonID the id of the button to click.
     * @param failOnMissing whether to fail if button is missing
     */
    public static void clickOnToolbarButton(
            WebDriver participant, String buttonID, boolean failOnMissing)
    {
        try
        {
            WebElement button
                = participant
                .findElement(By.xpath("//a[@id='" + buttonID + "']"));
            // if element missing and we do not want fail continue
            if (!failOnMissing && (button == null || !button.isDisplayed()))
                return;

            button.click();
        }
        catch (NoSuchElementException e)
        {
            if(failOnMissing)
                throw e;

            // there is no element, so its not visible, just continue
            // cause failOnMissing is false
            System.err.println("Button is missing:" + buttonID);
        }
    }

    /**
     * Shows the toolbar and clicks on a button identified by ID.
     * @param participant the {@code WebDriver}.
     * @param buttonID the id of the button to click.
     */
    public static void clickOnToolbarButton(
            WebDriver participant, String buttonID)
    {
        clickOnToolbarButton(participant, buttonID, true);
    }

    /**
     * Shows the toolbar and clicks on a button identified by class name.
     * @param participant the {@code WebDriver}.
     * @param buttonClass the class of the button to click.
     */
    public static void clickOnToolbarButtonByClass(WebDriver participant,
                                                   String buttonClass)
    {
        participant.findElement(
            By.xpath("//a[@class='button " + buttonClass + "']"))
            .click();
    }

    /**
     * Returns resource part of the JID of the user who is currently displayed
     * in the large video area in {@code participant}.
     *
     * @param participant <tt>WebDriver</tt> of the participant.
     * @return the resource part of the JID of the user who is currently
     * displayed in the large video area in {@code participant}.
     */
    public static String getLargeVideoResource(WebDriver participant)
    {
        return (String)((JavascriptExecutor) participant)
                .executeScript("return APP.UI.getLargeVideoID();");
    }

    /**
     * Returns <video> element for the local video.
     * @param participant the <tt>WebDriver</tt> from which local video element
     * will be obtained.
     * @return <tt>WebElement</tt> of the local video.
     */
    public static WebElement getLocalVideo(WebDriver participant)
    {
        List<WebElement> peerThumbs = participant.findElements(
                By.xpath("//video[starts-with(@id, 'localVideo_')]"));

        return peerThumbs.get(0);
    }

    /**
     * Get's the id of local video element.
     * @param participant the <tt>WebDriver</tt> instance of the participant for
     * whom we want to obtain local video element's ID
     * @return a <tt>String</tt> with the ID of the local video element.
     */
    public static String getLocalVideoID(WebDriver participant)
    {
        return getLocalVideo(participant).getAttribute("id");
    }

    /**
     * Returns all remote video elements for given <tt>WebDriver</tt> instance.
     * @param participant the <tt>WebDriver</tt> instance which will be used to
     * obtain remote video elements.
     * @return a list of <tt>WebElement</tt> with the remote videos.
     */
    public static List<WebElement> getRemoteVideos(WebDriver participant)
    {
        return participant.findElements(
                By.xpath("//video[starts-with(@id, 'remoteVideo_')]"));
    }

    /**
     * Obtains the ids for all remote participants <video> elements.
     * @param participant the <tt>WebDriver</tt> instance for which remote video
     * ids will be fetched.
     * @return a list of <tt>String</tt> with the ids of remote participants
     * video elements.
     */
    public static List<String> getRemoteVideoIDs(WebDriver participant)
    {
        List<WebElement> remoteThumbs = getRemoteVideos(participant);

        List<String> ids = new ArrayList<>();
        for (WebElement thumb : remoteThumbs)
        {
            ids.add(thumb.getAttribute("id"));
        }

        return ids;
    }

    /**
     * Displays film strip, if not displayed.
     *
     * @param participant <tt>WebDriver</tt> instance of the participant for
     * whom we'll try to open the settings panel.
     * @throws TimeoutException if we fail to open the settings panel.
     */
    public static void displayFilmStripPanel(WebDriver participant)
    {
        String filmStripXPath = "//div[@id='remoteVideos' and @class='hidden']";
        WebElement filmStrip;

        try {
            filmStrip = participant.findElement(By.xpath(filmStripXPath));
        } catch (NoSuchElementException ex) {
            filmStrip = null;
        }

        if (filmStrip != null)
        {
            clickOnToolbarButton(participant, "toolbar_film_strip");

            TestUtils.waitForElementNotPresentByXPath(
                    participant, filmStripXPath, 5);
        }
    }

    /**
     * Opens the settings panel, if not open.
     *
     * @param participant <tt>WebDriver</tt> instance of the participant for
     * whom we'll try to open the settings panel.
     * @throws TimeoutException if we fail to open the settings panel.
     */
    public static void displaySettingsPanel(WebDriver participant)
    {
        String settingsXPath = "//div[@id='settings_container']";
        WebElement settings = participant.findElement(By.xpath(settingsXPath));
        if (!settings.isDisplayed())
        {
            clickOnToolbarButton(participant, "toolbar_button_settings");

            TestUtils.waitForDisplayedElementByXPath(
                participant, settingsXPath, 5);
        }
    }

    /**
     * Hides the settings panel, if not hidden.
     *
     * @param participant <tt>WebDriver</tt> instance of the participant for
     * whom we'll try to hide the settings panel.
     * @throws TimeoutException if we fail to hide the settings panel.
     */
    public static void hideSettingsPanel(WebDriver participant)
    {
        String settingsXPath = "//div[@id='settings_container']";
        WebElement settings = participant.findElement(By.xpath(settingsXPath));
        if (settings.isDisplayed())
        {
            clickOnToolbarButton(participant, "toolbar_button_settings");

            TestUtils.waitForNotDisplayedElementByXPath(
                    participant, settingsXPath, 5);
        }
    }

    /**
     * Opens the contact list panel, if not open.
     *
     * @param participant <tt>WebDriver</tt> instance of the participant for
     * whom we'll try to open the contact list panel.
     * @throws TimeoutException if we fail to open the contact list panel
     */
    public static void displayContactListPanel(WebDriver participant)
    {
        String contactListXPath = "//div[@id='contacts_container']";
        WebElement contactList
            = participant.findElement(By.xpath(contactListXPath));

        if (!contactList.isDisplayed())
        {
            clickOnToolbarButton(participant, "toolbar_contact_list");

            TestUtils.waitForDisplayedElementByXPath(
                participant, contactListXPath, 5);
        }

        // move away from the button as user will do, to remove the tooltips
        Actions action = new Actions(participant);
        action.moveToElement(
            participant.findElement(By.id("largeVideoWrapper")));
        action.perform();
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
     * @param isVideo if {@code true} the icon for "video mute" will be checked;
     * otherwise, the "audio mute" icon will be checked.
     */
    public static void assertMuteIconIsDisplayed(
            WebDriver observer,
            WebDriver testee,
            boolean isMuted,
            boolean isVideo,
            String name)
    {
        String id = "";
        if(testee != observer) 
        {
            String resource = MeetUtils.getResourceJid(testee);
            id = "participant_" + resource;
        }
        else {
            id = "localVideoContainer";
        }
        

        String icon = isVideo
            ? TestUtils.getXPathStringForClassName("//span", "videoMuted")
                + "/i[@class='icon-camera-disabled']"
            : TestUtils.getXPathStringForClassName("//span", "audioMuted")
                + "/i[@class='icon-mic-disabled']";

        String mutedIconXPath
            = "//span[@id='" + id +"']" + icon;

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
     * For for the peer to have his audio muted/unmuted from given observer's
     * perspective. The method will fail the test if something goes wrong or
     * the audio muted status is different than the expected one. We wait up to
     * 3 seconds for the expected status to appear.
     *
     * @param observer <tt>WebDriver</tt> instance of the participant that
     *                 observes the audio status
     * @param testee <tt>WebDriver</tt> instance of the peer for whom we're
     *               checking the audio muted status.
     * @param testeeName the name of the testee that will be printed in failure
     *                   logs
     * @param muted <tt>true</tt> to wait for audio muted status or
     *              <tt>false</tt> to wait for the peer to unmute.
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
            false, //audio
            testeeName);

        // The code below check verifies audio muted status by checking peer's
        // audio levels on the oberver side. Statistics support is required to
        // do that
        if (!MeetUtils.areRtpStatsSupported(observer))
            return;

        // Give it 3 seconds to not get any audio or to receive some
        // depending on "muted" argument
        try
        {
            TestUtils.waitForCondition(
                observer,
                3,
                new ExpectedCondition<Boolean>()
                {
                    @Override
                    public Boolean apply(WebDriver webDriver)
                    {
                        Double audioLevel
                            = MeetUtils.getPeerAudioLevel(webDriver, testee);

                        // NOTE: audioLevel == null also when it is 0

                        // When testing for muted we want audio level to stay
                        // 'null' or 0, so we wait to timeout this condition
                        if (muted)
                        {
                            if (audioLevel != null && audioLevel > 0.1)
                            {
                                System.err.println(
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
                                System.err.println(
                                        "unmuted exiting on: " + audioLevel);
                                return true;
                            }
                            else
                            {
                                return false;
                            }
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
            // else we're good for unmuted peer
        }
        catch (TimeoutException timeout)
        {
            if (!muted)
            {
                fail("There was no sound from unmuted: '" +
                    testeeName + "'");
            }
            // else we're good for muted peer
        }
    }

    /**
     * Makes sure that a user's avatar is displayed, and that the user's video
     * is not displayed. Allows for the these conditions to not hold
     * immediately, but within a time interval of 5 seconds.
     * @param participant instance of <tt>WebDriver</tt> on which we'll perform
     * the verification.
     * @param resource the resource part of MUC JID which identifies user's
     * video thumbnail.
     */
    public static void assertAvatarDisplayed(
            WebDriver participant, String resource)
    {
        // Avatar image
        TestUtils.waitForDisplayedElementByXPath(
            participant, "//span[@id='participant_" + resource + "']" +
                    "/img[@class='userAvatar']",
            5);

        // User's video if available should be hidden, the element is missing
        // if remote participant started muted when we joined
        String videoElementXPath
            = "//span[@id='participant_" + resource + "']/video";
        List<WebElement> videoElems
            = participant.findElements(By.xpath(videoElementXPath));
        if(videoElems.size() > 0)
        {
            TestUtils.waitForNotDisplayedElementByXPath(
                participant, videoElementXPath, 5);
        }
    }

    /**
     * A method overload for
     * {@link #assertDisplayNameVisible(WebDriver, String)} which does
     * automatically obtain the "resource JID" from the participant's
     * <tt>WebDriver</tt> instance.
     * @param where instance of <tt>WebDriver</tt> on which we'll perform
     * the verification.
     * @param whose the <tt>WebDriver</tt> instance of the participant to whom
     * the video thumbnail being checked belongs to.
     */
    public static void assertDisplayNameVisible(WebDriver where,
                                                WebDriver whose)
    {
        String resourceJid = MeetUtils.getResourceJid(whose);
        assertDisplayNameVisible(where, resourceJid);
    }

    /**
     * Makes sure that a user's display name is displayed, and that
     * the user's thumbnail and avatar are not displayed. Allows for the these
     * conditions to not hold immediately, but within a time interval of 5
     * seconds.
     * @param participant instance of <tt>WebDriver</tt> on which we'll perform
     * the verification.
     * @param resource the resource part of MUC JID which identifies user's
     * video thumbnail.
     */
    public static void assertDisplayNameVisible(
            WebDriver participant, String resource)
    {
        // Avatar image - hidden
        TestUtils.waitForNotDisplayedElementByXPath(
            participant, "//span[@id='participant_" + resource + "']" +
                   "/img[contains(@class, 'userAvatar')]", 5);

        // User's video - hidden, if it is available
        String videoElementXPath
            = "//span[@id='participant_" + resource + "']/video";
        List<WebElement> videoElems
            = participant.findElements(By.xpath(videoElementXPath));
        if(videoElems.size() > 0)
        {
            TestUtils.waitForNotDisplayedElementByXPath(
                participant, videoElementXPath, 5);
        }

        // Display name - visible
        TestUtils.waitForDisplayedElementByXPath(
            participant, "//span[@id='participant_" + resource + "']" +
                "//span[@class='displayname']", 5);
    }

    /**
     * Clicks on a given participant's (identified by the resource part of it's
     * JID) video thumbnail.
     * @param participant instance of <tt>WebDriver</tt> where we'll perform the
     * click.
     * @param resourceJid the resource part of MUC JID which identifies user's
     * video thumbnail.
     */
    public static void clickOnRemoteVideo(
            WebDriver participant, String resourceJid)
    {
        participant.findElement(
            By.xpath("//span[@id='participant_" + resourceJid + "']")).click();
    }

    /**
     * Clicks on the local video thumbnail.
     * @param participant the instance of <tt>WebDriver</tt> where we'll
     * perform the click.
     */
    public static void clickOnLocalVideo(WebDriver participant)
    {
        TestUtils.executeScript(participant,
            "document.getElementById('localVideoContainer').click()");
    }

    /**
     * Makes sure that the local video is displayed in the local thumbnail and
     * that the avatar is not displayed.
     * @param participant the instance of <tt>WebDriver</tt> on which we'll
     * perform the verification.
     */
    public static void assertLocalThumbnailShowsVideo(WebDriver participant)
    {
        TestUtils.waitForNotDisplayedElementByXPath(
            participant,
            "//span[@id='localVideoContainer']/img[@class='userAvatar']",
            5);
        TestUtils.waitForDisplayedElementByXPath(
            participant, "//span[@id='localVideoWrapper']/video", 5);
    }

    /**
     * Makes sure that the avatar is displayed in the local thumbnail and that
     * the video is not displayed.
     * @param participant the instance of <tt>WebDriver</tt> on which we'll
     * perform the verification.
     */
    public static void assertLocalThumbnailShowsAvatar(WebDriver participant)
    {
        TestUtils.waitForDisplayedElementByXPath(
            participant,
            "//span[@id='localVideoContainer']/img[@class='userAvatar']",
            5);
        TestUtils.waitForElementNotPresentOrNotDisplayedByXPath(
            participant, "//span[@id='localVideoWrapper']/video", 5);
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
     * @param resource the expected resource
     */
    public static void waitsForLargeVideoSwitch(
        WebDriver driver, final String resource)
    {
        TestUtils.waitForCondition(driver, 5,
            new ExpectedCondition<Boolean>()
            {
                public Boolean apply(WebDriver d)
                {
                    return resource.equals(
                        getLargeVideoResource(d));
                }
            });
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
        return participant.findElements(By.xpath("//div[@id='remoteVideos']/span[@class='videocontainer']"));
    }

    /**
     * Returns list of currently visible participant's thumbnails.
     * @param participant the instance of <tt>WebDriver</tt>
     * @return list of thumbnails <tt>WebElement</tt> elements
     */
    public static List<WebElement> getVisibleThumbnails(WebDriver participant)
    {
        List<WebElement> thumbnails = getThumbnails(participant);

        Iterator<WebElement> it = thumbnails.iterator();
        while (it.hasNext()) {
            WebElement thumb = it.next();
            if (!thumb.isDisplayed()) {
                it.remove();
            }
        }

        return thumbnails;
    }

    /**
     * Checks if observer's active speaker is testee.
     * @param observer <tt>WebDriver</tt> instance of the participant that
     *                 should check active speaker.
     * @param testee <tt>WebDriver</tt> instance of the peer for may be
     *               active speaker for the observer.
     * @return <tt>true</tt> true if observer's active speaker is testee,
     *               <tt>false</tt> otherwise.
     */
    public static boolean isActiveSpeaker(
        WebDriver observer, WebDriver testee)
    {
        final String expectedJid =
            MeetUtils.getResourceJid(testee);

        try {
            TestUtils.waitForCondition(observer, 5,
                new ExpectedCondition<Boolean>()
                {
                    public Boolean apply(WebDriver d)
                    {
                        String currentJid = MeetUIUtils.getLargeVideoResource(d);

                        return expectedJid.equals(currentJid);
                    }
                });

            return true;
        }
        catch (TimeoutException ignored)
        {
            return false;
        }
    }

    /**
     * Asserts that observer shows the audio mute icon for the testee.
     *
     * @param observer <tt>WebDriver</tt> instance of the participant that
     *                 observes the audio status
     * @param testee <tt>WebDriver</tt> instance of the peer for whom we're
     *               checking the audio muted status.
     * @param testeeName the name of the testee that will be printed in failure
     *                   logs
     */
    public static void assertAudioMuted(
        WebDriver observer, WebDriver testee, String testeeName)
    {
        MeetUIUtils.assertMuteIconIsDisplayed(
            observer,
            testee,
            true, //should be muted
            false, //audio
            testeeName);
    }

    /**
     * Click on the local video thumbnail and waits until it's displayed on
     * "the large video".
     *
     * @param where the <tt>WebDriver</tt> instance on which the action is to
     * be performed.
     */
    static public void selectLocalVideo(WebDriver where)
    {
        // click on local
        MeetUIUtils.clickOnLocalVideo(where);

        WebElement localVideoElem
            = where.findElement(
                    By.xpath("//span[@id='localVideoWrapper']/video"));

        final String largeVideoID
            = MeetUIUtils.getVideoElementID(where, localVideoElem);

        waitForLargeVideoSwitch(where, largeVideoID);
    }

    /**
     * Selects the remote video to be displayed on "the large video" by clicking
     * participant's video thumbnail and waits for the switch to take place
     * using {@link #waitForLargeVideoSwitch(WebDriver, String)}.
     *
     * @param where the <tt>WebDriver</tt> instance on which the selection will
     * be performed.
     * @param toBeSelected the <tt>WebDriver</tt> instance which belongs to
     * the participant that is to be displayed on "the large video". Is used to
     * obtain user's identifier.
     */
    static public void selectRemoteVideo(WebDriver where,
                                         WebDriver toBeSelected)
    {
        String resourceJid = MeetUtils.getResourceJid(toBeSelected);
        String remoteThumbXpath
            = "//span[starts-with(@id, 'participant_" + resourceJid + "') "
                + " and contains(@class,'videocontainer')]";

        String remoteThumbVideoXpath
            = remoteThumbXpath + "/video[starts-with(@id, 'remoteVideo_')]";

        WebElement remoteThumbnail
            = TestUtils.waitForElementByXPath(
                    where, remoteThumbXpath, 5,
                    "Remote thumbnail not found: " + remoteThumbXpath);

        // click on remote
        remoteThumbnail.click();

        WebElement remoteThumbnailVideo
            = TestUtils.waitForElementByXPath(
                    where, remoteThumbVideoXpath, 5,
                    "Remote video not found: " + remoteThumbVideoXpath);

        String videoSrc = getVideoElementID(where, remoteThumbnailVideo);

        waitForLargeVideoSwitch(where, videoSrc);
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
     * @param where the <tt>WebDriver</tt> instance where the large video update
     * will be performed.
     * @param expectedVideoSrc a string which identifies the video stream
     * (the source set on HTML5 video element to attach WebRTC media stream).
     */
    static private void waitForLargeVideoSwitch(WebDriver    where,
                                                final String expectedVideoSrc)
    {
        new WebDriverWait(where, 3)
            .withMessage(
                    "Failed to switch the large video at: "
                        + MeetUtils.getResourceJid(where)
                        + " to : " + expectedVideoSrc)
            .until(new ExpectedCondition<Boolean>()
            {
                @Override
                public Boolean apply(WebDriver where)
                {
                    return expectedVideoSrc.equals(
                            MeetUIUtils.getLargeVideoID(where));
                }
            });
    }

    /**
     * Verifies the UI indication of remote participant's media connection
     * status. If the verification fails the currently running test will fail.
     *
     * @param observer the <tt>WebDriver</tt> instance where the check will be
     * performed.
     * @param peer the <tt>WebDriver</tt> of the participant whose connection
     * status will be checked.
     * @param isConnected tells whether the check is for
     * connected(<tt>true</tt>) or disconnected(<tt>false</tt>).
     */
    public static void verifyUserConnStatusIndication(
            WebDriver observer, WebDriver peer, boolean isConnected)
    {
        String peerId = MeetUtils.getResourceJid(peer);
        assertNotNull(peerId);

        // may take time for the video to recover(key frame) than disrupt
        int timeout = isConnected ? 20 : 7;

        // Wait for the logic to tell that the user is disconnected
        TestUtils.waitForBoolean(
                observer,
                MeetUtils.isConnectionActiveScript(peerId, isConnected),
                timeout);

        // Check connection status indicators
        String connectionLostXpath
            = "//span[@id='participant_" + peerId
                + "']/div/span[@class='connection connection_lost']";

        // Check "connection lost" icon
        TestUtils.waitForDisplayedOrNotByXPath(
                observer, connectionLostXpath, 2, !isConnected);
    }

    /**
     * Verifies the UI indication of local media connection status. If
     * the verification fails the currently running test will fail.
     *
     * @param where the <tt>WebDriver</tt> instance where the check will be
     * performed.
     * @param isConnected tells whether the check is for
     * connected(<tt>true</tt>) or disconnected(<tt>false</tt>).
     */
    public static void verifyLocalConnStatusIndication(
            WebDriver where, boolean isConnected)
    {
        // Wait for the logic to reflect the status first
        TestUtils.waitForBoolean(
                where,
                MeetUtils.isLocalConnectionActiveScript(isConnected),
                15);

        // Check connection status indicators
        String connectionLostXpath
            = "//span[@id='localVideoContainer']"
                + "/div/span[@class='connection connection_lost']";

        // Check "connection lost" icon
        TestUtils.waitForDisplayedOrNotByXPath(
                where, connectionLostXpath, 3, !isConnected);
    }
}
