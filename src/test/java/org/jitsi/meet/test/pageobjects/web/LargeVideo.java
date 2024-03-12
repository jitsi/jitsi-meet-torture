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

import java.util.*;

import org.jitsi.meet.test.util.*;
import org.jitsi.meet.test.web.*;
import org.openqa.selenium.support.ui.*;

/**
 * Represents the large video view for a particular {@link WebParticipant}.
 *
 * @author Leonard Kim
 */
public class LargeVideo
{
    /**
     * The participant used to interact with the large video.
     */
    private final WebParticipant participant;

    /**
     * Initializes a new {@link LargeVideo} instance.
     * @param participant the participant for this {@link LargeVideo}.
     */
    public LargeVideo(WebParticipant participant)
    {
        this.participant = Objects.requireNonNull(participant, "participant");
    }

    /**
     * Returns whether or not the video element on {@code LargeVideo} is
     * currently playing.
     *
     * @return {@code boolean} True if the video element is progressing through
     * video, false if no play progress is detected.
     */
    public void isVideoPlaying()
    {
        TestUtils.waitForCondition(
            participant.getDriver(),
            5,
            (ExpectedCondition<Boolean>) w -> {
                double startTime = this.getVideoPlaytime();

                TestUtils.waitMillis(500);

                double newTime = this.getVideoPlaytime();
                System.out.println(startTime + " " + newTime);
                return newTime > startTime;
            });
    }

    /**
     * Returns the elapsed time at which video has been playing.
     *
     * @return {@code double} The current play time of the video element.
     */
    private double getVideoPlaytime()
    {
        return TestUtils.executeScriptAndReturnDouble(
            participant.getDriver(),
            "return document.getElementById('largeVideo').currentTime");
    }
}
