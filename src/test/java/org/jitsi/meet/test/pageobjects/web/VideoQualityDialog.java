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

import org.jitsi.meet.test.web.*;
import org.openqa.selenium.*;

import java.util.*;

/**
 * Represents the video quality dialog in a particular {@link WebParticipant},
 * which allows for changing receive video quality and audio only mode.
 */
public class VideoQualityDialog
{
    /**
     * CCS selectors for finding WebElements within the
     * {@link VideoQualityDialog}.
     */
    private final static String CLOSE_BUTTON = "#modal-dialog-ok-button";
    private final static String DIALOG = ".video-quality-dialog";
    private final static String QUALITY_SLIDER = ".video-quality-dialog-slider";

    /**
     * The participant used to interact with this {@link VideoQualityDialog}.
     */
    private final WebParticipant participant;

    /**
     * Initializes a new {@link VideoQualityDialog} instance.
     *
     * @param participant the participant for this {@link VideoQualityDialog}.
     */
    public VideoQualityDialog(WebParticipant participant)
    {
        this.participant = Objects.requireNonNull(participant, "participant");
    }

    /**
     * Dismisses the {@link VideoQualityDialog}.
     */
    public void close()
    {
        if (!isOpen())
        {
            return;
        }

        participant.getDriver()
            .findElement(By.cssSelector(CLOSE_BUTTON))
            .click();
    }

    /**
     * Finds and returns the video quality dialog.
     *
     * @return The {@code WebElement} for the video quality dialog or null if it
     * cannot be found.
     */
    public WebElement get()
    {
        List<WebElement> elements = participant.getDriver().findElements(
            By.cssSelector(DIALOG));

        return elements.isEmpty() ? null : elements.get(0);
    }

    /**
     * Checks if this {@link VideoQualityDialog} is currently displayed.
     *
     * @return {@code true} if the dialog is displayed, {@code false} otherwise.
     */
    public boolean isOpen()
    {
        WebElement dialog = get();

        return dialog != null;
    }

    /**
     * Triggers display of the {@link VideoQualityDialog}.
     */
    public void open()
    {
        if (isOpen())
        {
            return;
        }

        participant.getToolbar().clickVideoQualityButton();
    }

    /**
     * Interacts with the video quality slider to either enable or disable audio
     * only mode.
     *
     * @param audioOnly {@code true} to enable audio only, {@code false} to
     * disable.
     */
    public void setAudioOnly(boolean audioOnly)
    {
        // Calculate how far to move the quality slider and in which direction.
        WebElement videoQualitySlider = participant.getDriver().findElement(
            By.cssSelector(QUALITY_SLIDER));

        int audioOnlySliderValue
                = Integer.parseInt(videoQualitySlider.getAttribute("min"));
        int maxDefinitionSliderValue
                = Integer.parseInt(videoQualitySlider.getAttribute("max"));

        int activeValue
                = Integer.parseInt(videoQualitySlider.getAttribute("value"));
        int targetValue
                = audioOnly ? audioOnlySliderValue : maxDefinitionSliderValue;

        int distanceToTargetValue = targetValue - activeValue;
        Keys keyDirection = distanceToTargetValue > 0 ? Keys.RIGHT : Keys.LEFT;

        // Move the slider to the target value.
        for (int i = 0; i < Math.abs(distanceToTargetValue); i++)
        {
            videoQualitySlider.sendKeys(keyDirection);
        }
    }
}
