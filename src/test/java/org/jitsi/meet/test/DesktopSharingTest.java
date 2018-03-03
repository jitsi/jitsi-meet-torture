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

import org.jitsi.meet.test.util.*;
import org.jitsi.meet.test.web.*;
import org.testng.annotations.*;
import org.testng.*;


/**
 * Launches a hook script that will launch a participant that will join
 * the conference and that participant will be sharing its screen.
 */
public class DesktopSharingTest
    extends WebTestBase
{
    /**
     * The xpath for the local video element.
     */
    private static final String LOCAL_VIDEO_XPATH
        = "//span[@id='localVideoWrapper']";

    /**
     * Check desktop sharing start.
     */
    @Test
    public void testDesktopSharingStart()
    {
        ensureOneParticipant();

        String extId
                = (String) getParticipant1().getConfigValue(
                "desktopSharingChromeExtId");

        if(extId == null) {
            throw new SkipException(
                "No Desktop sharing configuration detected. Disabling test.");
        }

        ensureTwoParticipants(
                null, null, null,
                new WebParticipantOptions().setChromeExtensionId(extId));
        startDesktopSharing();
        checkExpandingDesktopSharingLargeVideo(true);
        testDesktopSharingInPresence("desktop");

    }

    /**
     * Check desktop sharing stop.
     */
    @Test(dependsOnMethods = { "testDesktopSharingStart" })
    public void testDesktopSharingStop()
    {
        stopDesktopSharing();
        checkExpandingDesktopSharingLargeVideo(false);
        testDesktopSharingInPresence("camera");
    }

    /**
     * Checks the status of desktop sharing received from the presence and
     * compares it to the passed expected result.
     * @param expectedResult camera/desktop
     */
    private void testDesktopSharingInPresence(final String  expectedResult)
    {
        String participant1Jid
            = MeetUtils.getResourceJid(getParticipant2().getDriver());

        TestUtils.waitForStrings(
            getParticipant2().getDriver(),
                "return APP.UI.getRemoteVideoType('" + participant1Jid + "');",
            expectedResult,
            5);
    }

    /**
     * Starts desktop sharing.
     */
    private void startDesktopSharing()
    {
        ((WebParticipant)getParticipant2())
            .getToolbar().clickDesktopSharingButton();
    }

    /**
     * Stops desktop sharing.
     */
    private void stopDesktopSharing()
    {
        ((WebParticipant)getParticipant2())
            .getToolbar().clickDesktopSharingButton();
    }

    /**
     * Checks the video layout on the other side, after we imitate
     * desktop sharing.
     * @param isScreenSharing <tt>true</tt> if SS is started and <tt>false</tt>
     *                        otherwise.
     */
    private void checkExpandingDesktopSharingLargeVideo(boolean isScreenSharing)
    {
        // check layout
        new VideoLayoutTest().driverVideoLayoutTest(
            getParticipant1(), isScreenSharing);

        // hide thumbs
        MeetUIUtils.clickOnToolbarButton(
            getParticipant1().getDriver(), "toggleFilmstripButton");

        TestUtils.waitForNotDisplayedElementByXPath(
            getParticipant1().getDriver(),
            LOCAL_VIDEO_XPATH,
            5);

        // check layout
        new VideoLayoutTest().driverVideoLayoutTest(
            getParticipant1(), isScreenSharing);

        // show thumbs
        MeetUIUtils.clickOnToolbarButton(
            getParticipant1().getDriver(), "toggleFilmstripButton");

        TestUtils.waitForDisplayedElementByXPath(
            getParticipant1().getDriver(),
            LOCAL_VIDEO_XPATH,
            5);
    }
}
