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

import org.openqa.selenium.*;
import org.testng.annotations.*;

import java.util.*;

/**
 * The tests for LastN feature.
 *
 */
public class LastNTest
    extends AbstractBaseTest
{
    /**
     * Last N test scenario.
     */
    @Test
    public void testLastN()
    {
        ensureOneParticipant("config.startAudioMuted=0&config.channelLastN=1");
        WebDriver owner = participant1.getDriver();

        ensureThreeParticipants();

        WebDriver secondParticipant = participant2.getDriver();
        WebDriver thirdParticipant = participant3.getDriver();

        assertEquals("number of thumbnails", 3,
                     MeetUIUtils.getThumbnails(owner).size());
        assertEquals("number of visible thumbnails", 2,
                     MeetUIUtils.getVisibleThumbnails(owner).size());

        MeetUIUtils.assertAudioMuted(secondParticipant, owner, "owner");
        MeetUIUtils.assertAudioMuted(owner, secondParticipant, "participant2");
        MeetUIUtils.assertAudioMuted(owner, thirdParticipant, "participant3");

        // unmute second participant
        MuteTest muteTest
            = new MuteTest(participant1, participant2, participant3);
        muteTest.unMuteParticipantAndCheck();

        // so now he should be active speaker
        assertTrue(
            "second participant is active speaker for the owner",
            MeetUIUtils.isActiveSpeaker(owner, secondParticipant));
        assertActiveSpeakerThumbIsVisible(owner, secondParticipant);
        assertTrue(
            "second participant is active speaker for the third participant",
            MeetUIUtils.isActiveSpeaker(thirdParticipant, secondParticipant));
        assertActiveSpeakerThumbIsVisible(thirdParticipant, secondParticipant);

        muteTest.muteParticipantAndCheck();

        // unmute third participant
        muteTest.unMuteThirdParticipantAndCheck();

        // so now he should be active speaker
        assertTrue(
            "third participant is active speaker for the owner",
            MeetUIUtils.isActiveSpeaker(owner, thirdParticipant));
        assertActiveSpeakerThumbIsVisible(owner, thirdParticipant);
        assertTrue(
            "third participant is active speaker for the second participant",
            MeetUIUtils.isActiveSpeaker(secondParticipant, thirdParticipant));
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

        assertEquals("number of visible thumbnails", 2, thumbs.size());

        // remove local thumbnail from the list
        String localContainerId = "localVideoContainer";
        Iterator<WebElement> it = thumbs.iterator();
        while (it.hasNext()) {
            WebElement thumb = it.next();
            if (localContainerId.equals(thumb.getAttribute("id"))) {
                it.remove();
            }
        }

        assertEquals(1, thumbs.size());

        WebElement testeeThumb = thumbs.get(0);
        String testeeJid = MeetUtils.getResourceJid(testee);
        assertEquals("active speaker thumbnail id",
                     "participant_" + testeeJid, testeeThumb.getAttribute("id"));
    }

    @Override
    public boolean skipTestByDefault()
    {
        return true;
    }
}
