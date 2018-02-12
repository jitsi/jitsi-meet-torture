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
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;
import org.testng.annotations.*;

import static org.testng.Assert.*;

/**
 * Launches a hook script that will launch a participant that will join
 * the conference and that participant will be sharing its screen.
 */
public class DesktopSharingTest
    extends WebTestBase
{
    /**
     * The id of the desktop sharing button.
     */
    private static String DS_BUTTON_ID = "toolbar_button_desktopsharing";

    /**
     * The XPATH of the desktop sharing button.
     */
    private static String DS_BUTTON_XPATH = "//a[@id='" + DS_BUTTON_ID + "']";

    @Override
    public void setupClass()
    {
        super.setupClass();

        ensureOneParticipant();

        ensureTwoParticipants(
            null, null, null,
            new WebParticipantOptions().setChromeExtensionId(
                (String) MeetUtils.getConfigValue(
                    getParticipant1().getDriver(),
                    "desktopSharingChromeExtId")
            ));
    }

    /**
     * Check desktop sharing start.
     */
    @Test
    public void testDesktopSharingStart()
    {
        startDesktopSharing();
        checkExpandingDesktopSharingLargeVideo(true);
        testDesktopSharingInPresence("desktop");

    }

    /**
     * Check desktop sharing stop.
     */
    @Test
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
        // now lets check whether his stream is screen
        String participant1Jid
            = MeetUtils.getResourceJid(getParticipant2().getDriver());

        // holds the last retrieved value for the remote type
        final Object[] remoteVideoType = new Object[1];
        try
        {
            final String scriptToExecute = "return APP.UI.getRemoteVideoType('"
                + participant1Jid + "');";
            (new WebDriverWait(
                    getParticipant2().getDriver(), 5))
                .until((ExpectedCondition<Boolean>) d -> {
                    Object res =
                        getParticipant1().executeScript(scriptToExecute);
                    remoteVideoType[0] = res;

                    return res != null && res.equals(expectedResult);
                });
        }
        catch (TimeoutException e)
        {
            assertEquals(
                    expectedResult, remoteVideoType[0],
                    "Wrong video type, maybe desktop sharing didn't work");
        }
    }

    /**
     * Starts desktop sharing.
     */
    private void startDesktopSharing()
    {
        TestUtils.waitForElementByXPath(
                getParticipant2().getDriver(),
                DS_BUTTON_XPATH,
                5);
        MeetUIUtils.clickOnToolbarButton(getParticipant2().getDriver(),
                DS_BUTTON_ID);
        TestUtils.waitForElementContainsClassByXPath(
                getParticipant2().getDriver(), DS_BUTTON_XPATH,
                "toggled", 2);
    }

    /**
     * Stops desktop sharing.
     */
    private void stopDesktopSharing()
    {
        TestUtils.waitForElementByXPath(
                getParticipant2().getDriver(),
                DS_BUTTON_XPATH,
                5);
        MeetUIUtils.clickOnToolbarButton(getParticipant2().getDriver(),
                DS_BUTTON_ID);
        TestUtils.waitForElementNotContainsClassByXPath(
                getParticipant2().getDriver(), DS_BUTTON_XPATH,
                "toggled", 2);
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

        TestUtils.waitMillis(5000);

        // check layout
        new VideoLayoutTest().driverVideoLayoutTest(
            getParticipant1(), isScreenSharing);

        // show thumbs
        MeetUIUtils.clickOnToolbarButton(
            getParticipant1().getDriver(), "toggleFilmstripButton");

        TestUtils.waitMillis(5000);
    }
}
