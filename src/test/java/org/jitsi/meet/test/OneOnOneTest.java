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
import org.openqa.selenium.interactions.*;
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
    private static final int FILMSTRIP_VISIBILITY_WAIT = 5;

    /**
     * Parameters to attach to the meeting url to enable One-On-One behavior
     * and have toolbars dismiss faster, as remote video visibility is also
     * tied to toolbar visibility.
     */
    private static final String ONE_ON_ONE_CONFIG_OVERRIDES
        = "config.disable1On1Mode=false"
        + "&interfaceConfig.TOOLBAR_TIMEOUT=500"
        + "&config.alwaysVisibleToolbar=false";

    /**
     * Tests remote videos in filmstrip do not display in a 1-on-1 call.
     */
    @Test
    public void testFilmstripHiddenInOneOnOne()
    {
        JitsiMeetUrl url
            = getJitsiMeetUrl().appendConfig(ONE_ON_ONE_CONFIG_OVERRIDES);
        ensureTwoParticipants(url, url);

        WebParticipant participant1 =  getParticipant1();
        WebParticipant participant2 = getParticipant2();

        // Prevent toolbar from being always displayed as filmstrip visibility
        // is tied to toolbar visibility.
        configureToolbarsToHideQuickly(participant1);
        configureToolbarsToHideQuickly(participant2);

        verifyRemoteVideosDisplay(participant1, false);
        verifyRemoteVideosDisplay(participant2, false);
    }

    /**
     * Tests remote videos in filmstrip do display when in a call with more than
     * two total participants.
     */
    @Test(dependsOnMethods = { "testFilmstripHiddenInOneOnOne" })
    public void testFilmstripVisibleWithMoreThanTwo()
    {
        ensureThreeParticipants(
            null, null,
            getJitsiMeetUrl().appendConfig(ONE_ON_ONE_CONFIG_OVERRIDES));

        WebParticipant participant3 = getParticipant3();
        configureToolbarsToHideQuickly(participant3);

        verifyRemoteVideosDisplay(getParticipant1(), true);
        verifyRemoteVideosDisplay(getParticipant2(), true);
        verifyRemoteVideosDisplay(participant3, true);
    }

    /**
     * Tests remote videos in filmstrip do not display after transitioning to
     * 1-on-1 mode. Also tests remote videos in filmstrip do display when
     * focused on self and transitioning back to 1-on-1 mode.
     */
    @Test(dependsOnMethods = { "testFilmstripVisibleWithMoreThanTwo" })
    public void testFilmstripDisplayWhenReturningToOneOnOne()
    {
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
    public void testFilmstripVisibleOnSelfViewFocus()
    {
        Participant participant1 = getParticipant1();
        MeetUIUtils.clickOnLocalVideo(participant1.getDriver());
        verifyRemoteVideosDisplay(participant1, true);

        MeetUIUtils.clickOnLocalVideo(participant1.getDriver());
        verifyRemoteVideosDisplay(participant1, false);
    }

    /**
     * Tests remote videos in filmstrip stay visible when hovering over when the
     * filmstrip is hovered over.
     */
    @Test(dependsOnMethods = { "testFilmstripVisibleOnSelfViewFocus" })
    public void testFilmstripHoverShowsVideos()
    {
        Participant participant1 = getParticipant1();
        WebDriver driver1 = participant1.getDriver();

        WebElement toolbar = driver1.findElement(By.id("localVideoContainer"));
        Actions hoverOnToolbar = new Actions(driver1);
        hoverOnToolbar.moveToElement(toolbar);
        hoverOnToolbar.perform();

        verifyRemoteVideosDisplay(participant1, true);
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
        String filmstripRemoteVideosXpath
            = "//div[contains(@class, 'remote-videos')]/div";

        TestUtils.waitForDisplayedOrNotByXPath(
            testee.getDriver(),
            filmstripRemoteVideosXpath,
            FILMSTRIP_VISIBILITY_WAIT,
            isDisplayed);
    }

    /**
     * Disables permanent display (docking) of the toolbars and sets toolbars
     * to be dismissed more quickly.
     *
     * @param testee the <tt>WebDriver</tt> of the participant for whom we're
     *               no longer want to dock toolbars.
     */
    private void configureToolbarsToHideQuickly(WebParticipant testee)
    {
        testee.executeScript("APP.UI.dockToolbar(false);");
        testee.executeScript("APP.UI.showToolbar(250);");
    }
}
