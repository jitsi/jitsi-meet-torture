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

import org.jitsi.meet.test.util.*;
import org.jitsi.meet.test.web.*;

import java.util.*;

/**
 * Represents the status labels in a {@link WebParticipant}, which show various
 * conference statuses, including current video quality status.
 */
public class StatusLabels
{
    /**
     * Selectors for finding WebElements within the {@link StatusLabels}.
     */
    private final static String AUDIO_ONLY_XPATH
        = "//div[@id='videoResolutionLabel'][contains(@class, 'audio-only')]";

    /**
     * The participant used to interact with {@link StatusLabels}.
     */
    private final WebParticipant participant;

    /**
     * Initializes a new {@link StatusLabels} instance.
     *
     * @param participant the participant for {@link StatusLabels}.
     */
    public StatusLabels(WebParticipant participant)
    {
        this.participant = Objects.requireNonNull(participant, "participant");
    }

    /**
     * Polls for 5 seconds until the audio only label has the passed in display
     * state.
     *
     * @param isDisplayed Whether to wait for the label to be displayed or not
     * displayed.
     */
    public void waitForAudioOnlyDisplayStatus(boolean isDisplayed)
    {
        TestUtils.waitForDisplayedOrNotByXPath(
            participant.getDriver(),
            AUDIO_ONLY_XPATH,
            5,
            isDisplayed);
    }
}
