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

        WebDriver owner = getParticipant1().getDriver();

        WebDriver secondParticipant = getParticipant2().getDriver();
        WebDriver thirdParticipant = getParticipant3().getDriver();

        assertEquals(
            3, MeetUIUtils.getThumbnails(owner).size(),
            "number of thumbnails");
        assertEquals(
            2, MeetUIUtils.getVisibleThumbnails(owner).size(),
            "number of visible thumbnails");

        MeetUIUtils.assertAudioMuted(secondParticipant, owner, "owner");
        MeetUIUtils.assertAudioMuted(owner, secondParticipant, "getParticipant2()");
        MeetUIUtils.assertAudioMuted(owner, thirdParticipant, "getParticipant3()");

        // unmute second participant
        MuteTest muteTest = new MuteTest(this);
        muteTest.unMuteParticipantAndCheck();

        // so now he should be active speaker
        assertTrue(
            MeetUIUtils.isActiveSpeaker(owner, secondParticipant),
            "second participant is active speaker for the owner");
        assertActiveSpeakerThumbIsVisible(owner, secondParticipant);
        assertTrue(
            MeetUIUtils.isActiveSpeaker(thirdParticipant, secondParticipant),
            "second participant is active speaker for the third participant");
        assertActiveSpeakerThumbIsVisible(thirdParticipant, secondParticipant);

        muteTest.muteParticipantAndCheck();

        // unmute third participant
        muteTest.unMuteThirdParticipantAndCheck();

        // so now he should be active speaker
        assertTrue(
            MeetUIUtils.isActiveSpeaker(owner, thirdParticipant),
            "third participant is active speaker for the owner");
        assertActiveSpeakerThumbIsVisible(owner, thirdParticipant);
        assertTrue(
            MeetUIUtils.isActiveSpeaker(secondParticipant, thirdParticipant),
            "third participant is active speaker for the second participant");
        assertActiveSpeakerThumbIsVisible(secondParticipant, thirdParticipant);
    }

    /**
     * Assert that observer has only 2 visible thumbnails, and second one is
     * testee's thumbnail.
     */
    private void assertActiveSpeakerThumbIsVisible(
        WebDriver observer, WebDriver testee)
    {
        List<WebElement> thumbs = MeetUIUtils.getVisibleThumbnails(observer);

        assertEquals(
            2, thumbs.size(),
            "number of visible thumbnails");

        // remove local thumbnail from the list
        String localContainerId = "localVideoContainer";
        thumbs.removeIf(
            thumb -> localContainerId.equals(thumb.getAttribute("id")));

        assertEquals(1, thumbs.size());

        WebElement testeeThumb = thumbs.get(0);
        String testeeJid = MeetUtils.getResourceJid(testee);
        assertEquals(
            "participant_" + testeeJid, testeeThumb.getAttribute("id"),
            "active speaker thumbnail id");
    }

    @Override
    public boolean skipTestByDefault()
    {
        return true;
    }
}
