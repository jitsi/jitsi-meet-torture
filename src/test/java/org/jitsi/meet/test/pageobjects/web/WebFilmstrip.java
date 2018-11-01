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
}
