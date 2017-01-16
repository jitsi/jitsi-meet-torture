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
import org.openqa.selenium.JavascriptExecutor;

/**
 * A test which switches on/imitate desktop sharing to check whether it is
 * functioning ok.
 *
 * NOTE: This test is currently not relevant. Expanding the video after hiding
 * the filmstrip will try to fit the video in the window without cutting it,
 * but it can be that it will not expand at all, which is exactly the current
 * situation for the failures.
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
     * Currently we imitate desktop sharing in order to check video stretch
     * on the other side.
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
     * Checks the video layout on the other side, after we imitate
     * desktop sharing.
     */
    private void checkExpandingDesktopSharingLargeVideo()
    {
        // hide thumbs
        MeetUIUtils.clickOnToolbarButton(
            ConferenceFixture.getSecondParticipant(),
            "toolbar_film_strip");

        TestUtils.waitMillis(5000);

        // check layout
        new VideoLayoutTest()
            .driverVideoLayoutTest(ConferenceFixture.getSecondParticipant());
    }

    /**
     * Enable/Disable desktop sharing.
     * For now we jus imitate desktop sharing so we can check it on the other
     * side.
     */
    private void toggleDesktopSharing()
    {
        String videoType;
        if(desktopImitated)
            videoType = "camera";
        else
            videoType = "screen";

        // change the type of video that we will report
        ((JavascriptExecutor) ConferenceFixture.getOwner())
            .executeScript(
            "APP.xmpp.getConnection().emuc.presMap['videoType'] = '"
                + videoType + "'");

        desktopImitated = !desktopImitated;
    }
}
