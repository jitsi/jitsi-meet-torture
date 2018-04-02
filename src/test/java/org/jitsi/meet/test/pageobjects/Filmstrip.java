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
package org.jitsi.meet.test.pageobjects;

import org.jitsi.meet.test.base.*;

/**
 * Represents the filmstrip with all the videos - remotes and local one.
 *
 * @author Damian Minkov
 */
public interface Filmstrip<T extends Participant>
{
    /**
     * Asserts that {@code participant} shows or doesn't show the audio
     * mute icon for the conference participant identified by
     * {@code participantToCheck}.
     *
     * @param participantToCheck the {@code Participant} for whom we're
     * checking the status of audio muted icon.
     * @param isDisplayed if {@code true}, the method will assert the presence
     * of the "mute" icon; otherwise, it will assert its absence.
     */
    public void assertAudioMuteIcon(T participantToCheck, boolean isDisplayed);

    /**
     * Asserts that {@code participant} shows or doesn't show the
     * video mute icon for the conference participant identified by
     * {@code participantToCheck}.
     *
     * @param participantToCheck the {@code Participant} for whom we're
     * checking the status of video muted icon.
     * @param isDisplayed if {@code true}, the method will assert the presence
     * of the "mute" icon; otherwise, it will assert its absence.
     */
    public void assertVideoMuteIcon(T participantToCheck, boolean isDisplayed);
}
