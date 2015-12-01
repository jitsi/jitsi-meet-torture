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
 * The tests for active speaker detection feature.
 *
 * @author Pawel Domas
 */
public class ActiveSpeakerTest
    extends TestCase
{
    /**
     * Constructs test
     * @param name the method name for the test.
     */
    public ActiveSpeakerTest(String name)
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

        suite.addTest(new ActiveSpeakerTest("testActiveSpeaker"));

        return suite;
    }

    /**
     * Active speaker test scenario.
     */
    public void testActiveSpeaker()
    {
        System.err.println("Start testActiveSpeaker.");

        WebDriver owner = ConferenceFixture.getOwner();

        // skip if we are not chrome
        if (ConferenceFixture.getBrowserType(owner) !=
                ConferenceFixture.BrowserType.chrome)
        {
            return;
        }

        // This test requires a conference of three
        ConferenceFixture.ensureThreeParticipants();

        WebDriver secondParticipant = ConferenceFixture.getSecondParticipant();
        WebDriver thirdParticipant = ConferenceFixture.getThirdParticipant();

        // Mute all
        muteAllParticipants();

        // Owner becomes active speaker - check from 2nd peer's perspective
        testActiveSpeaker(owner, secondParticipant);
        // 3rd peer becomes active speaker - check from 2nd peer's perspective
        testActiveSpeaker(thirdParticipant, secondParticipant);
        // 2nd peer becomes active speaker - check from owner's perspective
        testActiveSpeaker(secondParticipant, owner);

        // check the displayed speakers, there should be only one speaker
        checkDisplayNames(owner);
        checkDisplayNames(secondParticipant);
        checkDisplayNames(thirdParticipant);

        // Dispose 3rd
        ConferenceFixture.close(thirdParticipant);

        // Unmuted owner and the 2nd
        unMuteOwnerAndSecond();
    }

    /**
     * Checks the number of Speakers shown, it should be only one.
     * @param driver the participant to check
     */
    private void checkDisplayNames(WebDriver driver)
    {
        List<WebElement> displayNamesElem =
            driver.findElements(By.xpath(
                "//span[contains(@class,'dominantspeakerindicator')]"
                ));

        int speakers = 0;
        for (WebElement el : displayNamesElem)
        {
            if(el.isDisplayed())
                speakers++;
        }

        assertEquals("Speakers should not be more then one", 1, speakers);
    }

    private void muteAllParticipants()
    {
        System.err.println("Start muteAllParticipants.");

        new MuteTest("muteOwnerAndCheck").muteOwnerAndCheck();
        new MuteTest("muteParticipantAndCheck").muteParticipantAndCheck();
        new MuteTest("muteThirdParticipantAndCheck")
                .muteThirdParticipantAndCheck();
    }

    private void unMuteOwnerAndSecond()
    {
        System.err.println("Start unMuteOwnerAndSecond.");

        new MuteTest("unMuteOwnerAndCheck").unMuteOwnerAndCheck();
        new MuteTest("unMuteParticipantAndCheck").unMuteParticipantAndCheck();
    }

    /**
     * Tries to make given participant an active speaker by un-muting him.
     * Verifies from <tt>peer2</tt> perspective if he has been displayed on
     * the large video area. Mutes him back.
     *
     * @param activeSpeaker <tt>WebDriver</tt> instance of the participant who
     *                      will be testes as an active speaker.
     * @param peer2 <tt>WebDriver</tt> of the participant who will be observing
     *              and verifying active speaker change.
     */
    private void testActiveSpeaker(WebDriver activeSpeaker, WebDriver peer2)
    {
        System.err.println("Start testActiveSpeaker.");

        final String speakerEndpoint = MeetUtils.getResourceJid(activeSpeaker);

        // Unmute
        MeetUIUtils.clickOnToolbarButton(activeSpeaker, "toolbar_button_mute");
        MeetUIUtils.assertMuteIconIsDisplayed(
                peer2,
                activeSpeaker,
                false,
                false, //audio
                speakerEndpoint);

        // Verify that the user is now an active speaker from peer2 perspective
        try
        {
            new WebDriverWait(peer2, 10).until(
                    new ExpectedCondition<Boolean>()
                    {
                        public Boolean apply(WebDriver d)
                        {
                            return speakerEndpoint.equals(
                                MeetUIUtils.getLargeVideoResource(d));
                        }
                    });
        }
        catch (TimeoutException exc)
        {
            assertEquals(
                "Active speaker not displayed on large video " + new Date(),
                speakerEndpoint, MeetUIUtils.getLargeVideoResource(peer2));
        }

        // Mute back again
        MeetUIUtils.clickOnToolbarButton(activeSpeaker, "toolbar_button_mute");
        MeetUIUtils.assertMuteIconIsDisplayed(
                peer2,
                activeSpeaker,
                true,
                false, //audio
                speakerEndpoint);
    }
}
