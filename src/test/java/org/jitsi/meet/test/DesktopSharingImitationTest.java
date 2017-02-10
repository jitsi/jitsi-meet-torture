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
package org.jitsi.meet.test;

import junit.framework.*;

import org.jitsi.meet.test.util.*;
import org.openqa.selenium.*;

/**
 * A test which switches on/imitate desktop sharing to check whether it is
 * functioning ok.
 *
 * @author Damian Minkov
 */
public class DesktopSharingImitationTest
    extends TestCase
{
    /**
     * Used to toggle desktop sharing imitation.
     */
    private boolean desktopImitated = false;

    /**
     * Currently we imitate desktop sharing in order to check video stretch.
     */
    public void testDesktopSharing()
    {
        ConferenceFixture.closeAllParticipantsExceptTheOwner();

        toggleDesktopSharing();

        ConferenceFixture.ensureTwoParticipants();
        TestUtils.waitMillis(2000);

        checkExpandingDesktopSharingLargeVideo();

        toggleDesktopSharing();

        // now let's clean up, quit second and join him again
        ConferenceFixture.closeSecondParticipant();
        TestUtils.waitMillis(1000);
        ConferenceFixture.ensureTwoParticipants();
    }

    /**
     * Checks the video layout, after we imitate desktop sharing.
     */
    private void checkExpandingDesktopSharingLargeVideo()
    {
        // Remote video tests:
        checkLargeVideoSize(ConferenceFixture.getSecondParticipant());

        // Local video part:
        MeetUIUtils.selectLocalVideo(ConferenceFixture.getOwner());
        checkLargeVideoSize(ConferenceFixture.getOwner());

    }

    /**
     * Checks the size of the large video for given participant.
     * @param participant the {@code WebDriver}.
     */
    private void checkLargeVideoSize(WebDriver participant) {
        // check layout
        new VideoLayoutTest().doLargeVideoSizeCheck(participant, true);

        // hide thumbs
        MeetUIUtils.clickOnHideFilmstripButton(participant);

        TestUtils.waitMillis(5000);

        // check layout
        new VideoLayoutTest().doLargeVideoSizeCheck(participant, true);
    }

    /**
     * Enable/Disable desktop sharing.
     * For now we just imitate desktop sharing.
     */
    private void toggleDesktopSharing()
    {
        String videoType = desktopImitated ? "camera" : "desktop";

        // change the type of video that we will report
        setLocalVideoType(videoType);
        sendVideoTypeToParticipants(videoType);

        desktopImitated = !desktopImitated;
    }

    /**
     * Enable/Disable desktop sharing for the local participant. Sets all the
     * variables for the local participant to imitate starting/stopping
     * desktop sharing.
     * @param videoType the new video type to be set for the local video.
     */
    private void setLocalVideoType(String videoType) {
        String script =  "APP.conference._room.getLocalTracks().forEach("
            + "function(stream) {"
                + "if(stream.type === 'video') {"
                    + "stream.videoType = '" + videoType + "';"
                    + "APP.conference.isScreenSharing = true;"
                    + "APP.UI.addLocalStream(stream);"
                    + "APP.UI.updateDesktopSharingButtons();"
                + "}"
            + "});";
        System.err.println(script);
        ((JavascriptExecutor) ConferenceFixture.getOwner())
            .executeScript(script);
    }

    /**
     * Sends the passed video type to the remote participants in order to
     * imitate starting/stopping desktop sharing.
     * @param videoType the new video type to be sent
     */
    private void sendVideoTypeToParticipants(String videoType) {
        ((JavascriptExecutor) ConferenceFixture.getOwner())
            .executeScript("APP.conference._room.removeCommand('videoType');"
                + "APP.conference._room.sendCommand('videoType', {"
                    + "value: '" + videoType + "', "
                    + "attributes: {"
                        + "xmlns: 'http://jitsi.org/jitmeet/video'"
                    + "}"
                + "});");
    }
}
