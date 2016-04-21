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

import junit.framework.*;
import org.jitsi.meet.test.util.*;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;

import java.util.*;

/**
 * The tests for LastN feature.
 *
 */
public class LastNTest
    extends TestCase
{
    /**
     * Constructs test
     * @param name the method name for the test.
     */
    public LastNTest(String name)
    {
        super(name);
    }

    /**
     * Orders the tests.
     * @return the suite with ordered tests.
     */
    public static Test suite()
    {
        TestSuite suite = new TestSuite();

        suite.addTest(new LastNTest("testLastN"));
        suite.addTest(new LastNTest("restartParticipants"));

        return suite;
    }

    /**
     * Last N test scenario.
     */
    public void testLastN()
    {
        System.err.println("Start testLastN.");

        // close everything first
        ConferenceFixture.closeAllParticipants();

        WebDriver owner = ConferenceFixture.startOwner(
            "config.startAudioMuted=0&config.channelLastN=1");
        ConferenceFixture.waitForSecondParticipantToConnect();
        WebDriver secondParticipant = ConferenceFixture.getSecondParticipant();

        ConferenceFixture.waitForThirdParticipantToConnect();
        WebDriver thirdParticipant = ConferenceFixture.getThirdParticipant();

        assertEquals("number of thumbnails", 3,
                     MeetUIUtils.getThumbnails(owner).size());
        assertEquals("number of visible thumbnails", 2,
                     MeetUIUtils.getVisibleThumbnails(owner).size());

        MeetUIUtils.assertAudioMuted(secondParticipant, owner, "owner");
        MeetUIUtils.assertAudioMuted(owner, secondParticipant, "participant2");
        MeetUIUtils.assertAudioMuted(owner, thirdParticipant, "participant3");

        // unmute second participant
        new MuteTest("unMuteParticipantAndCheck").unMuteParticipantAndCheck();

        // so now he should be active speaker
        assertTrue(
            "second participant is active speaker for the owner",
            MeetUIUtils.isActiveSpeaker(owner, secondParticipant));
        assertActiveSpeakerThumbIsVisible(owner, secondParticipant);
        assertTrue(
            "second participant is active speaker for the third participant",
            MeetUIUtils.isActiveSpeaker(thirdParticipant, secondParticipant));
        assertActiveSpeakerThumbIsVisible(thirdParticipant, secondParticipant);

        new MuteTest("muteParticipantAndCheck").muteParticipantAndCheck();

        // unmute third participant
        new MuteTest("unMuteThirdParticipantAndCheck")
            .unMuteThirdParticipantAndCheck();

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

    /**
     * Retart participants.
     */
    public void restartParticipants()
    {
        new StartMutedTest("restartParticipants").restartParticipants();
    }
}
