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
import org.openqa.selenium.support.ui.*;

import java.util.*;

import static org.junit.Assert.fail;

/**
 * Class contains utility operations specific to jitsi-meet user interface.
 *
 * @author Pawel Domas
 * @author Damian Minkov
 */
public class MeetUIUtils
{
    /**
     * Shows the toolbar and clicks on a button identified by ID.
     * @param participant the {@code WebDriver}.
     * @param buttonID the id of the button to click.
     */
    public static void clickOnToolbarButton(
            WebDriver participant, String buttonID)
    {
        participant.findElement(
            By.xpath("//a[@id='" + buttonID + "']")).click();
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
     * Shows the toolbar and clicks on a button identified by class name.
     * Does nothing if the element is not displayed
     * @param participant the {@code WebDriver}.
     * @param buttonClass the class of the button to click.
     */
    public static void clickOnToolbarButtonByClassIfDisplayed(
        WebDriver participant, String buttonClass)
    {
        try
        {
            WebElement hangupButton = participant.findElement(
                By.xpath("//a[@class='button " + buttonClass + "']"));
            if (hangupButton != null && hangupButton.isDisplayed())
                hangupButton.click();
        }
        catch (NoSuchElementException e)
        {
            // there is no element, so its not visible, just continue
            System.err.println("Button is missing:" + buttonClass);
        }
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
     * Opens the settings panel, if not open.
     *
     * @param participant <tt>WebDriver</tt> instance of the participant for
     * whom we'll try to open the settings panel.
     * @throws TimeoutException if we fail to open the settings panel.
     */
    public static void displaySettingsPanel(WebDriver participant)
    {
        String settingsXPath = "//div[@id='settingsmenu']";
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
        String settingsXPath = "//div[@id='settingsmenu']";
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
        String contactListXPath = "//div[@id='contactlist']";
        WebElement contactList
            = participant.findElement(By.xpath(contactListXPath));

        if (!contactList.isDisplayed())
        {
            clickOnToolbarButton(participant, "bottom_toolbar_contact_list");

            TestUtils.waitForDisplayedElementByXPath(
                participant, contactListXPath, 5);
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
        String resource = MeetUtils.getResourceJid(testee);

        String icon = isVideo
            ? "/span[@class='videoMuted']/i[@class='icon-camera-disabled']"
            : "/span[@class='audioMuted']/i[@class='icon-mic-disabled']";

        String mutedIconXPath
            = "//span[@id='participant_" + resource +"']" + icon;

        try
        {
            if (isMuted)
            {
                TestUtils.waitForElementByXPath(observer, mutedIconXPath, 5);
            }
            else
            {
                TestUtils.waitForElementNotPresentByXPath(
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
                   "/img[@class='userAvatar']", 5);

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
                "/span[@class='displayname']", 5);
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
        participant.findElement(
            By.xpath("//span[@id='localVideoContainer']" +
                      "/span[@class='focusindicator']")
        ).click();
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
    public static String getLargeVideoSource(WebDriver driver)
    {
        Object res = ((JavascriptExecutor) driver)
            .executeScript(
                "return JitsiMeetJS.util.RTCUIHelper.getVideoId(" +
                    "document.getElementById('largeVideo'))");

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
     * Checks whether video with id is displayed on the large video.
     * @param driver the driver
     * @param videoID the video
     */
    public static boolean firefoxCheckVideoDisplayedOnLarge(
            WebDriver driver, String videoID)
    {
        Object res = ((JavascriptExecutor) driver)
                .executeScript(
                        "return document.getElementById('" + videoID
                                + "').mozSrcObject "
                                + "== document.getElementById('largeVideo').mozSrcObject;");

        return  res != null && res.equals(Boolean.TRUE);
    }
}
