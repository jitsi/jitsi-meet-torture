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

import org.jitsi.meet.test.base.*;
import org.jitsi.meet.test.util.*;

import org.testng.annotations.*;

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
    extends AbstractBaseTest
{
    /**
     * Used to toggle desktop sharing imitation.
     */
    private boolean desktopImitated = false;

    @Override
    public void setup()
    {
        super.setup();

        ensureOneParticipant();
    }

    @Override
    public boolean skipTestByDefault()
    {
        return true;
    }

    /**
     * Currently we imitate desktop sharing in order to check video stretch
     * on the other side.
     */
    @Test
    public void testDesktopSharing()
    {
        toggleDesktopSharing();

        ensureTwoParticipants();
        TestUtils.waitMillis(2000);

        checkExpandingDesktopSharingLargeVideo();

        toggleDesktopSharing();
    }

    /**
     * Checks the video layout on the other side, after we imitate
     * desktop sharing.
     */
    private void checkExpandingDesktopSharingLargeVideo()
    {
        // hide thumbs
        MeetUIUtils.clickOnToolbarButton(
            getParticipant2().getDriver(),
            "toolbar_film_strip");

        TestUtils.waitMillis(5000);

        // check layout
        new VideoLayoutTest().driverVideoLayoutTest(getParticipant2());
    }

    /**
     * Enable/Disable desktop sharing.
     * For now we jus imitate desktop sharing so we can check it on the other
     * side.
     */
    private void toggleDesktopSharing()
    {
        String videoType;
        if (desktopImitated)
            videoType = "camera";
        else
            videoType = "screen";

        // change the type of video that we will report
        getParticipant1().executeScript(
            "APP.xmpp.getConnection().emuc.presMap['videoType'] = '"
                + videoType + "'");

        desktopImitated = !desktopImitated;
    }
}
