/*
 * Copyright @ 2015-2018 Atlassian Pty Ltd
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
package org.jitsi.meet.test.pageobjects.web;

import org.jitsi.meet.test.pageobjects.*;
import org.jitsi.meet.test.util.*;
import org.jitsi.meet.test.web.*;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.*;

import java.util.*;

import static org.testng.Assert.*;

/**
 * Represents the filmstrip with all the videos - remotes and local one.
 *
 * @author Damian Minkov
 */
public class WebFilmstrip
    implements Filmstrip<WebParticipant>
{
    /**
     * Selectors for finding WebElements within the {@link WebFilmstrip}.
     */
    private final static String DOMINANT_SPEAKER_ICON_XPATH
        = "//*[contains(@id,'dominantspeakerindicator')]";
    private final static String LOCAL_VIDEO_XPATH
        = "//*[contains(@id,'localVideoContainer')]";
    private final static String PINNED_CLASS = "videoContainerFocused";
    private final static String REMOTE_VIDEOS_XPATH
        = "//div[@id='filmstripRemoteVideosContainer']";
    private final static String VISIBLE_FILMSTRIP_XPATH
        = "//*[@id='remoteVideos' and not(contains(@class, 'hidden'))]";

    /**
     * The participant.
     */
    private final WebParticipant participant;

    /**
     * Initializes a new {@link WebFilmstrip} instance.
     *
     * @param participant the participant for this {@link WebFilmstrip}.
     */
    public WebFilmstrip(WebParticipant participant)
    {
        this.participant = Objects.requireNonNull(participant, "participant");
    }

    /**
     * Checks whether or not video thumbnails in the filmstrip are displayed.
     *
     * @return {@code true} if video thumbnails are visible on screen,
     * {@code false} otherwise.
     */
    public boolean areVideoThumbnailsVisible()
    {
        WebDriver driver = participant.getDriver();
        By visibleVideosSelector = By.xpath(VISIBLE_FILMSTRIP_XPATH);

        return driver.findElements(visibleVideosSelector).size() > 0;
    }

    /**
     * Asserts that {@code participant} shows or doesn't show the audio
     * mute icon for the conference participant identified by
     * {@code participantToCheck}.
     *
     * @param participantToCheck the {@code WebParticipant} for whom we're
     * checking the status of audio muted icon.
     * @param isDisplayed if {@code true}, the method will assert the presence
     * of the "mute" icon; otherwise, it will assert its absence.
     */
    public void assertAudioMuteIcon(
        WebParticipant participantToCheck, boolean isDisplayed)
    {
        MeetUIUtils.assertMuteIconIsDisplayed(
            participant.getDriver(),
            participantToCheck.getDriver(),
            isDisplayed,
            false, // audio
            participantToCheck.getName()
        );
    }

    /**
     * Asserts that the number of small videos with the dominant speaker
     * indicator displayed equals 1.
     */
    public void assertOneDominantSpeaker()
    {
        List<WebElement> dominantSpeakerIndicators
            = participant.getDriver().findElements(
                By.xpath(DOMINANT_SPEAKER_ICON_XPATH));

        assertEquals(
            dominantSpeakerIndicators.size(),
            1,
            "Wrong number of dominant speaker indicators.");
    }

    /**
     * Polls to asserts the remote thumbnails are either displayed or not
     * displayed.
     *
     * @param isDisplayed {@code} true to assert the thumbnails are displayed,
     * {@false} to assert the thumbnails are not displayed.
     */
    public void assertRemoteVideosDisplay(boolean isDisplayed)
    {
        TestUtils.waitForDisplayedOrNotByXPath(
            participant.getDriver(),
            REMOTE_VIDEOS_XPATH,
            5,
            isDisplayed);
    }

    /**
     * Asserts that {@code participant} shows or doesn't show the
     * video mute icon for the conference participant identified by
     * {@code participantToCheck}.
     *
     * @param participantToCheck the {@code WebParticipant} for whom we're
     * checking the status of video muted icon.
     * @param isDisplayed if {@code true}, the method will assert the presence
     * of the "mute" icon; otherwise, it will assert its absence.
     */
    public void assertVideoMuteIcon(
        WebParticipant participantToCheck, boolean isDisplayed)
    {
        MeetUIUtils.assertMuteIconIsDisplayed(
            participant.getDriver(),
            participantToCheck.getDriver(),
            isDisplayed,
            true, // video
            participantToCheck.getName()
        );
    }

    /**
     * Asserts the {@code participant} can or cannot see the videos of the this
     * {@link WebFilmstrip}.
     *
     * @param shouldBeVisible if {@code true}, the method will assert the
     * visibility of the filmstrip videos, otherwise it will assert they are
     * hidden.
     */
    public void assertVideoThumbnailVisibility(boolean shouldBeVisible)
    {
        if (shouldBeVisible)
        {
            TestUtils.waitForDisplayedElementByXPath(
                participant.getDriver(),
                VISIBLE_FILMSTRIP_XPATH,
                5);
        }
        else
        {
            TestUtils.waitForElementNotPresentByXPath(
                participant.getDriver(),
                VISIBLE_FILMSTRIP_XPATH,
                5);
        }
    }

    /**
     * Gets the video src for the video element in the local participant's
     * thumbnail.
     *
     * @return The src used to play local video.
     */
    public String getVideoSrcForLocalParticipant()
    {
        WebElement localVideoElem
                = participant.getDriver().findElement(
                By.xpath("//span[@id='localVideoWrapper']//video"));

        return MeetUIUtils.getVideoElementID(
                participant.getDriver(), localVideoElem);
    }

    /**
     * Gets the video src for the video element in a remote participant's
     * thumbnail.
     *
     * @return The src used to play a remote participant's video.
     */
    public String getVideoSrcForRemoteParticipant(
        WebParticipant remoteParticipant)
    {
        String remoteThumbVideoXpath  = getThumbnailXpath(remoteParticipant)
            + "//video[starts-with(@id, 'remoteVideo_')]";
        WebElement remoteThumbnailVideo
                = TestUtils.waitForElementByXPath(
                participant.getDriver(),
                remoteThumbVideoXpath,
                5,
                "Remote video not found: " + remoteThumbVideoXpath);

        return MeetUIUtils.getVideoElementID(
                participant.getDriver(), remoteThumbnailVideo);
    }

    /**
     * Clicks the local participant thumbnail to pin or unpin the local
     * participant. No action will be taken if the local participant thumbnail
     * is detected as already being in the desired pinned state.
     *
     * @param pin {@code true} if the participant should be pinned,
     * {@code false} otherwise.
     */
    public void setLocalParticipantPin(boolean pin)
    {
        WebElement localThumbnail
            = participant.getDriver().findElement(By.xpath(LOCAL_VIDEO_XPATH));
        String classNames = localThumbnail.getAttribute("class");
        boolean isPinned = classNames.contains(PINNED_CLASS);

        if (isPinned == pin) {
            return;
        }

        MeetUIUtils.clickOnLocalVideo(participant.getDriver());
    }

    /**
     * Clicks the remote participant thumbnail to pin or unpin the participant.
     * No action will be taken if the participant thumbnail is detected as
     * already being in the desired pinned state.
     *
     * @param remoteParticipant The {@link WebParticipant} for the remote
     * participant that should be pinned.
     * @param pin {@code true} if the participant should be pinned,
     * {@code false} otherwise.
     */
    public void setRemoteParticipantPin(
        WebParticipant remoteParticipant, boolean pin)
    {
        String endpointId = remoteParticipant.getEndpointId();
        String remoteThumbXpath = getThumbnailXpath(remoteParticipant);
        WebElement remoteThumbnail = TestUtils.waitForElementByXPath(
            participant.getDriver(),
            remoteThumbXpath,
            5,
            "Remote thumbnail not found: " + remoteThumbXpath);

        boolean isCurrentlyPinned = remoteThumbnail.getAttribute("class")
            .contains("videoContainerFocused");

        if (isCurrentlyPinned == pin)
        {
            return;
        }

        remoteThumbnail.click();

        try
        {
            String pinnedThumbXpath
                = "//span[ @id='participant_" + endpointId + "'" +
                    "        and contains(@class,'videoContainerFocused') ]";

            if (pin)
            {
                TestUtils.waitForElementByXPath(
                    participant.getDriver(),
                    pinnedThumbXpath,
                    2);
            }
            else
            {
                TestUtils.waitForElementNotPresentByXPath(
                    participant.getDriver(),
                    pinnedThumbXpath,
                    2);
            }
        }
        catch (TimeoutException exc)
        {
            fail("Failed set pin to " + pin + " for " + endpointId);
        }
    }

    /**
     * Toggles video thumbnails to be hidden or visible.
     *
     * @param newVisibility {@code true} if thumbnails should be visible,
     * {@code false} if thumbnails should be hidden.
     */
    public void setVideoThumbnailVisibility(boolean newVisibility)
    {
        if (newVisibility == areVideoThumbnailsVisible())
        {
            return;
        }

        WebDriver driver = participant.getDriver();
        driver.findElement(By.id("toggleFilmstripButton")).click();

        assertVideoThumbnailVisibility(newVisibility);
    }

    /**
     * Attempts to make sure the filmstrip is displayed by mouse hovering over
     * it. The assumption is that the filmstrip should be displayed whenever
     * it is being hovered.
     */
    public void triggerDisplay()
    {
        WebDriver driver = participant.getDriver();
        WebElement localThumbnail
                = driver.findElement(By.xpath(LOCAL_VIDEO_XPATH));

        Actions hoverOnToolbar = new Actions(driver);
        hoverOnToolbar.moveToElement(localThumbnail);
        hoverOnToolbar.perform();
    }

    /**
     * Generates the Xpath to find the thumbnail within {@link WebFilmstrip}
     * for the passed in {@link WebParticipant}.
     *
     * @param participant
     * @return The Xpath for finding the participant's thumbnail.
     */
    private String getThumbnailXpath(WebParticipant participant)
    {
        return "//span[starts-with(@id, 'participant_"
            + participant.getEndpointId() + "') "
            + " and contains(@class,'videocontainer')]";
    }
}
