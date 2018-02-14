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
package org.jitsi.meet.test.base;

/**
 * The interface used to limit direct access to {@link ParticipantFactory} by
 * exposing only methods which are not related to {@link Participant} instance
 * management like createParticipant.
 */
public interface ParticipantFactoryConfig
{
    /**
     * Sets the name of wav audio file which will be streamed through fake audio
     * device by participants. The file location is relative to working folder.
     * For remote drivers a parent folder can be set and the file will be
     * searched in there.
     *
     * @param fakeStreamAudioFile full name of wav file for the fake audio
     *                            device.
     */
    void setFakeStreamAudioFile(String fakeStreamAudioFile);

    /**
     * Sets the name of y4m video file which will be streamed through fake video
     * device by participants. The file location is relative to working folder.
     * For remote drivers a parent folder can be set and the file will be
     * searched in there.
     *
     * @param fakeStreamVideoFile full name of y4m file for the fake video
     *                            device.
     */
    void setFakeStreamVideoFile(String fakeStreamVideoFile);
}
