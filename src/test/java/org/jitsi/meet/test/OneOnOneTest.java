/*
 * Copyright @ 2017 Atlassian Pty Ltd
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
import org.jitsi.meet.test.web.*;

import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.testng.annotations.*;

/**
 * Tests 1-on-1 remote video thumbnail display in the filmstrip.
 *
 * @author Leonard Kim
 */
public class OneOnOneTest
    extends WebTestBase
{
    /**
     * The duration to wait, in seconds, remote videos in filmstrip to display
     * and complete animations.
     */
    private final int filmstripVisibilityWait = 5;

    /**
     * Parameters to attach to the meeting url to enable One-On-One behavior
     * and have toolbars dismiss faster, as remote video visibility is also
     * tied to toolbar visibility.
     */
    private final String oneOnOneConfigOverrides
        = "config.disable1On1Mode=false"
        + "&interfaceConfig.TOOLBAR_TIMEOUT=250"
        + "&interfaceConfig.INITIAL_TOOLBAR_TIMEOUT=250"
        + "&config.alwaysVisibleToolbar=false";

    /**
     * Tests remote videos in filmstrip do not display in a 1-on-1 call.
     */
    @Test
    public void testFilmstripHiddenInOneOnOne()
    {
        JitsiMeetUrl url
            = getJitsiMeetUrl().appendConfig(oneOnOneConfigOverrides);
        ensureTwoParticipants(url, url);

        Participant owner =  getParticipant1();
        Participant secondParticipant = getParticipant2();

        // Prevent toolbar from being always displayed as filmstrip visibility
        // is tied to toolbar visibility.
        stopDockingToolbar(owner);
        stopDockingToolbar(secondParticipant);

        verifyRemoteVideosDisplay(owner, false);
        verifyRemoteVideosDisplay(secondParticipant, false);
    }

    /**
     * Tests remote videos in filmstrip do display when in a call with more than
     * two total participants.
     */
    @Test(dependsOnMethods = { "testFilmstripHiddenInOneOnOne" })
    public void testFilmstripVisibleWithMoreThanTwo() {
        ensureThreeParticipants(
            null, null,
            getJitsiMeetUrl().appendConfig(oneOnOneConfigOverrides));

        Participant thirdParticipant = getParticipant3();
        stopDockingToolbar(thirdParticipant);

        verifyRemoteVideosDisplay(getParticipant1(), true);
        verifyRemoteVideosDisplay(getParticipant2(), true);
        verifyRemoteVideosDisplay(thirdParticipant, true);
    }

    /**
     * Tests remote videos in filmstrip do not display after transitioning to
     * 1-on-1 mode. Also tests remote videos in filmstrip do display when
     * focused on self and transitioning back to 1-on-1 mode.
     */
    @Test(dependsOnMethods = { "testFilmstripVisibleWithMoreThanTwo" })
    public void testFilmstripDisplayWhenReturningToOneOnOne() {
        MeetUIUtils.clickOnLocalVideo(getParticipant2().getDriver());

        getParticipant3().hangUp();

        verifyRemoteVideosDisplay(getParticipant1(), false);
        verifyRemoteVideosDisplay(getParticipant2(), true);
    }

    /**
     * Tests remote videos in filmstrip become visible when focused on self view
     * while in a 1-on-1 call.
     */
    @Test(dependsOnMethods = { "testFilmstripDisplayWhenReturningToOneOnOne" })
    public void testFilmstripVisibleOnSelfViewFocus() {
        MeetUIUtils.clickOnLocalVideo(getParticipant1().getDriver());
        verifyRemoteVideosDisplay(getParticipant1(), true);

        MeetUIUtils.clickOnLocalVideo(getParticipant1().getDriver());
        verifyRemoteVideosDisplay(getParticipant1(), false);
    }

    /**
     * Tests remote videos in filmstrip stay visible when hovering over when the
     * filmstrip is hovered over.
     */
    @Test(dependsOnMethods = { "testFilmstripVisibleOnSelfViewFocus" })
    public void testFilmstripHoverShowsVideos() {
        Participant owner = getParticipant1();
        WebDriver ownerDriver = owner.getDriver();

        WebElement toolbar = ownerDriver.findElement(By.id("localVideoContainer"));
        Actions hoverOnToolbar = new Actions(ownerDriver);
        hoverOnToolbar.moveToElement(toolbar);
        hoverOnToolbar.perform();

        verifyRemoteVideosDisplay(owner, true);
    }

    /**
     * Check if remote videos in filmstrip are visible.
     *
     * @param testee the <tt>WebDriver</tt> of the participant for whom we're
     *               checking the status of filmstrip remote video visibility.
     * @param isDisplayed whether or not filmstrip remote videos should be
     *                    visible
     */
    private void verifyRemoteVideosDisplay(
        Participant testee, boolean isDisplayed)
    {
        waitForToolbarsHidden(testee);

        String filmstripRemoteVideosXpath
            = "//div[@id='filmstripRemoteVideosContainer']";

        TestUtils.waitForDisplayedOrNotByXPath(
            testee.getDriver(),
            filmstripRemoteVideosXpath,
            filmstripVisibilityWait,
            isDisplayed);
    }

    /**
     * Disables permanent display (docking) of the toolbars.
     *
     * @param testee the <tt>WebDriver</tt> of the participant for whom we're
     *               no longer want to dock toolbars.

     */
    private void stopDockingToolbar(Participant testee) {
        testee.executeScript("APP.UI.dockToolbar(false);");
    }

    /**
     * Waits until the toolbars are no longer displayed.
     *
     * @param testee the <tt>WebDriver</tt> of the participant for whom we're
     *               waiting to no longer see toolbars.
     */
    private void waitForToolbarsHidden(Participant testee) {
        // Wait for the visible filmstrip to no longer be displayed.
        String visibleToolbarXpath
            = "//*[contains(@class, 'toolbar_secondary')"
            + "and contains(@class ,'slideInExtX')]";

        TestUtils.waitForElementNotPresentByXPath(
            testee.getDriver(),
            visibleToolbarXpath,
            filmstripVisibilityWait);
    }
}
