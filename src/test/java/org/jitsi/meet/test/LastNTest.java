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
import org.testng.annotations.*;

import java.util.*;

import static org.testng.Assert.*;

/**
 * The tests for LastN feature.
 *
 */
public class LastNTest
    extends WebTestBase
{
    /**
     * Last N test scenario.
     */
    @Test
    public void testLastN()
    {
        ensureThreeParticipants(
            getJitsiMeetUrl().appendConfig(
                "config.startAudioMuted=0&config.channelLastN=1"),
            null, null);

        WebDriver driver1 = getParticipant1().getDriver();
        WebDriver driver2 = getParticipant2().getDriver();
        WebDriver driver3 = getParticipant3().getDriver();

        assertEquals(
            MeetUIUtils.getThumbnails(driver1).size(),
            3,
            "number of thumbnails");
        assertEquals(
            MeetUIUtils.getVisibleThumbnails(driver1).size(),
            2,
            "number of visible thumbnails");

        getParticipant2().getFilmstrip()
            .assertAudioMuteIcon(getParticipant1(), true);
        getParticipant1().getFilmstrip()
            .assertAudioMuteIcon(getParticipant2(), true);
        getParticipant1().getFilmstrip()
            .assertAudioMuteIcon(getParticipant3(), true);

        // unmute participant1
        MuteTest muteTest = new MuteTest(this);
        muteTest.unmuteParticipant2AndCheck();

        // now participant2 should be active speaker
        assertTrue(
            MeetUIUtils.isActiveSpeaker(driver1, driver2),
            "participant1 should see participant2 as the active speaker");
        assertActiveSpeakerThumbIsVisible(driver1, driver2);
        assertTrue(
            MeetUIUtils.isActiveSpeaker(driver3, driver2),
            "participant3 should see participant2 as the active speaker");
        assertActiveSpeakerThumbIsVisible(driver3, driver2);

        muteTest.muteParticipant2AndCheck();

        // unmute participant3
        muteTest.unmuteParticipant3AndCheck();

        // now participant3 should be active speaker
        assertTrue(
            MeetUIUtils.isActiveSpeaker(driver1, driver3),
            "participant1 should see participant3 as the active speaker");
        assertActiveSpeakerThumbIsVisible(driver1, driver3);
        assertTrue(
            MeetUIUtils.isActiveSpeaker(driver2, driver3),
            "participant2 should see participant3 as the active speaker");
        assertActiveSpeakerThumbIsVisible(driver2, driver3);
    }

    /**
     * Assert that observer has only 2 visible thumbnails, and the second one is
     * testee's thumbnail.
     */
    private void assertActiveSpeakerThumbIsVisible(
        WebDriver observer, WebDriver testee)
    {
        List<WebElement> thumbs = MeetUIUtils.getVisibleThumbnails(observer);

        assertEquals(
            thumbs.size(),
            2,
            "number of visible thumbnails");

        // remove local thumbnail from the list
        String localContainerId = "localVideoContainer";
        thumbs.removeIf(
            thumb -> localContainerId.equals(thumb.getAttribute("id")));

        assertEquals(thumbs.size(), 1);

        WebElement testeeThumb = thumbs.get(0);
        String testeeJid = MeetUtils.getResourceJid(testee);
        assertEquals(
            testeeThumb.getAttribute("id"),
            "participant_" + testeeJid,
            "active speaker thumbnail id");
    }

    @Override
    public boolean skipTestByDefault()
    {
        return true;
    }
}
