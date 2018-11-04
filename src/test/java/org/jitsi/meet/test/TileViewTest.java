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
import org.testng.annotations.*;

import static org.testng.Assert.*;

public class TileViewTest
    extends WebTestBase
{
    @Override
    public void setupClass()
    {
        super.setupClass();

        ensureTwoParticipants();
    }


    /**
     * Tests tile view is automatically exited when etherpad is open and it
     * automatically re-entered when etherpad is exited.
     */
    @Test
    public void testEtherpadExitsTileView()
    {
        if (MeetUtils.isEtherpadEnabled(getParticipant1().getDriver()))
        {
            getParticipant1().getEtherpad().open();
            assertFalse(getParticipant1().isInTileView());

            getParticipant1().getEtherpad().close();
            assertFalse(getParticipant1().isInTileView());
        }
    }

    /**
     * Tests tile view is exited on participant pin.
     */
    @Test(dependsOnMethods = { "testEtherpadExitsTileView" })
    public void testPinningExitsTileView()
    {
        getParticipant1()
            .getFilmstrip()
            .setRemoteParticipantPin(getParticipant2(), true);

        assertFalse(getParticipant1().isInTileView());

        getParticipant1()
            .getFilmstrip()
            .setRemoteParticipantPin(getParticipant2(), false);

        assertFalse(getParticipant1().isInTileView());
    }

    /**
     * Tests local video has been successfully moved to the end of the remote
     * videos, so it should be displayed as the last video in tile view.
     */
    @Test(dependsOnMethods = { "testPinningExitsTileView" })
    public void testLocalVideoDisplaysAtEnd()
    {
        enterTileView();

        getParticipant1().getFilmstrip().assertLocalThumbnailDock(false);
    }

    /**
     * Tests tile view can be toggled off.
     */
    @Test(dependsOnMethods = { "testLocalVideoDisplaysAtEnd" })
    public void testCanExitTileView()
    {
        getParticipant1().getToolbar().clickTileViewButton();
        assertFalse(getParticipant1().isInTileView());
    }

    /**
     * Tests local video displays outside of the remote videos once tile view
     * has been exited.
     */
    @Test(dependsOnMethods = { "testCanExitTileView" })
    public void testLocalVideoDisplaysIndependentlyFromRemote()
    {
        getParticipant1().getFilmstrip().assertLocalThumbnailDock(true);
    }

    /**
     * Attemps to enter tile view and verifies tile view has been entered.
     */
    private void enterTileView()
    {
        getParticipant1().getToolbar().clickTileViewButton();

        assertTrue(MeetUIUtils.isInTileView(getParticipant1()));
    }
}
