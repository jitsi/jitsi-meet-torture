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
import org.jitsi.meet.test.pageobjects.web.*;
import org.jitsi.meet.test.util.*;
import org.jitsi.meet.test.web.*;

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
     * Parameters to attach to the meeting url to enable One-On-One behavior
     * and have toolbars dismiss faster, as remote video visibility is also
     * tied to toolbar visibility.
     */
    private static final String ONE_ON_ONE_CONFIG_OVERRIDES
        = "config.disable1On1Mode=false"
        + "&interfaceConfig.TOOLBAR_TIMEOUT=250"
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

        participant1.getFilmstrip().assertRemoteVideosDisplay(false);
        participant2.getFilmstrip().assertRemoteVideosDisplay(false);
    }

    /**
     * Tests remote videos in filmstrip do display when in a call with more than
     * two total participants.
     */
    @Test(dependsOnMethods = { "testFilmstripHiddenInOneOnOne" })
    public void testFilmstripVisibleWithMoreThanTwo() {
        ensureThreeParticipants(
            null, null,
            getJitsiMeetUrl().appendConfig(ONE_ON_ONE_CONFIG_OVERRIDES));

        configureToolbarsToHideQuickly(getParticipant3());

        getParticipant1().getFilmstrip().assertRemoteVideosDisplay(true);
        getParticipant2().getFilmstrip().assertRemoteVideosDisplay(true);
        getParticipant3().getFilmstrip().assertRemoteVideosDisplay(true);
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

        getParticipant1().getFilmstrip().assertRemoteVideosDisplay(false);
        getParticipant2().getFilmstrip().assertRemoteVideosDisplay(true);
    }

    /**
     * Tests remote videos in filmstrip become visible when focused on self view
     * while in a 1-on-1 call.
     */
    @Test(dependsOnMethods = { "testFilmstripDisplayWhenReturningToOneOnOne" })
    public void testFilmstripVisibleOnSelfViewFocus()
    {
        WebParticipant participant1 = getParticipant1();

        participant1.getFilmstrip().setLocalParticipantPin(true);
        getParticipant1().getFilmstrip().assertRemoteVideosDisplay(true);

        participant1.getFilmstrip().setLocalParticipantPin(false);
        getParticipant1().getFilmstrip().assertRemoteVideosDisplay(false);
    }

    /**
     * Tests remote videos in filmstrip stay visible when hovering over when the
     * filmstrip is hovered over.
     */
    @Test(dependsOnMethods = { "testFilmstripVisibleOnSelfViewFocus" })
    public void testFilmstripHoverShowsVideos()
    {
        WebFilmstrip filmstrip = getParticipant1().getFilmstrip();

        filmstrip.triggerDisplay();
        filmstrip.assertRemoteVideosDisplay(true);
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
