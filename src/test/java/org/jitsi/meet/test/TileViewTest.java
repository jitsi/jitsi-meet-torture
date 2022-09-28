/*
 * Copyright @ 2018 Atlassian Pty Ltd
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
import org.testng.*;
import org.testng.annotations.*;

import java.util.logging.*;

import static org.testng.Assert.*;

public class TileViewTest
    extends WebTestBase
{

    /**
     * The CSS selector for local video when outside of tile view. It should
     * be in a container separate from remote videos so remote videos can
     * scroll while local video stays docked.
     */
    private static final String FILMSTRIP_VIEW_LOCAL_VIDEO_CSS_SELECTOR
        = "#filmstripLocalVideo #localVideoContainer";

    /**
     * The CSS selector for local video tile view is enabled. It should display
     * at the end of all the other remote videos, as the last tile.
     */
    private static final String TILE_VIEW_LOCAL_VIDEO_CSS_SELECTOR
        = ".remote-videos #localVideoContainer";

    @Override
    public void setupClass()
    {
        super.setupClass();

        ensureTwoParticipants();
    }

    /**
     * Tests tile view is automatically exited when etherpad is opened.
     */
    @Test
    public void testEtherpadExitsTileView()
    {
        if (MeetUtils.isEtherpadEnabled(getParticipant1().getDriver()))
        {
            enterTileView();

            getParticipant1().getToolbar().clickEtherpadButton();
            MeetUIUtils.waitForTileViewDisplay(getParticipant1(), false);

            getParticipant1().getToolbar().clickEtherpadButton();
            MeetUIUtils.waitForTileViewDisplay(getParticipant1(), false);

        }
    }

    /**
     * Tests tile view is exited on participant pin.
     */
    @Test(dependsOnMethods = { "testEtherpadExitsTileView" })
    public void testPinningExitsTileView()
    {
        enterTileView();

        MeetUIUtils.waitForTileViewDisplay(getParticipant1(), true);

        MeetUIUtils.clickOnRemoteVideo(
            getParticipant1().getDriver(),
            getParticipant2().getEndpointId());

        MeetUIUtils.waitForTileViewDisplay(getParticipant1(), false);
    }

    /**
     * Tests local video has been successfully moved to the end of the remote
     * videos, so it should be displayed as the last video in tile view.
     */
    @Test(dependsOnMethods = { "testPinningExitsTileView" })
    public void testLocalVideoDisplaysAtEnd()
    {
        enterTileView();

        MeetUIUtils.waitForTileViewDisplay(getParticipant1(), true);

        WebDriver driver = getParticipant1().getDriver();

        TestUtils.waitForElementBy(
            driver,
            By.cssSelector(TILE_VIEW_LOCAL_VIDEO_CSS_SELECTOR),
            5);

        TestUtils.waitForElementNotPresentBy(
            driver,
            By.cssSelector(FILMSTRIP_VIEW_LOCAL_VIDEO_CSS_SELECTOR),
            5
        );
    }

    /**
     * Tests tile view can be toggled off.
     */
    @Test(dependsOnMethods = { "testLocalVideoDisplaysAtEnd" })
    public void testCanExitTileView()
    {
        getParticipant1().getToolbar().clickTileViewButton();
        MeetUIUtils.waitForTileViewDisplay(getParticipant1(), false);
    }

    /**
     * Tests local video displays outside of the remote videos once tile view
     * has been exited.
     */
    @Test(dependsOnMethods = { "testCanExitTileView" })
    public void testLocalVideoDisplaysIndependentlyFromRemote()
    {
        WebDriver driver = getParticipant1().getDriver();

        TestUtils.waitForElementNotPresentBy(
            driver,
            By.cssSelector(TILE_VIEW_LOCAL_VIDEO_CSS_SELECTOR),
            5);

        TestUtils.waitForElementBy(
            driver,
            By.cssSelector(FILMSTRIP_VIEW_LOCAL_VIDEO_CSS_SELECTOR),
            5
        );
    }

    /**
     * Attemps to enter tile view and verifies tile view has been entered.
     */
    private void enterTileView()
    {
        getParticipant1().getToolbar().clickTileViewButton();
        MeetUIUtils.waitForTileViewDisplay(getParticipant1(), true);
    }

    /**
     * The first one is dominant, a third enters with lastN=1 and sees second participant's video as a dominant speaker.
     * The dominant speaker changes to the second one and we start seeing its video and is ninja no more.
     */
    @Test(dependsOnMethods = { "testLocalVideoDisplaysIndependentlyFromRemote" })
    public void testLastNAndTileView()
    {
        if (getParticipant1().getType().isFirefox() || getParticipant2().getType().isFirefox())
        {
            Logger.getGlobal().log(Level.WARNING, "Not testing as second participant cannot be dominant speaker.");
            throw new SkipException("Firefox does not support external audio file as input.");
        }

        getParticipant2().getToolbar().clickAudioMuteButton();

        // let's mute the third so it does not become dominant speaker
        ensureThreeParticipants(null, null,
            getJitsiMeetUrl().appendConfig("config.channelLastN=1")
                .appendConfig("config.startWithAudioMuted=true"));

        WebDriver driver3 = getParticipant3().getDriver();

        // one inactive icon should appear in few seconds
        MeetUIUtils.waitForNinjaIcon(driver3);

        String participant1EndpointId = getParticipant1().getEndpointId();

        // should have video for participant 1
        MeetUIUtils.waitForRemoteVideo(driver3, participant1EndpointId, true);

        String participant2EndpointId = getParticipant2().getEndpointId();

        assertTrue(MeetUIUtils.hasNinjaUserConnStatusIndication(driver3, participant2EndpointId));

        // no video for participant 2
        MeetUIUtils.waitForRemoteVideo(driver3, participant2EndpointId, false);

        getParticipant1().getToolbar().clickAudioMuteButton();
        getParticipant2().getToolbar().clickAudioMuteButton();

        MeetUIUtils.waitForDominantSpeaker(driver3, participant2EndpointId);

        // check video of participant 2 should be received
        MeetUIUtils.waitForRemoteVideo(driver3, participant2EndpointId, true);
    }
}
