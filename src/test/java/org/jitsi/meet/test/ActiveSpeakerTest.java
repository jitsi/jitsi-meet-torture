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
import org.openqa.selenium.support.ui.*;
import org.testng.*;
import org.testng.annotations.*;

import java.util.*;

import static org.testng.Assert.*;

/**
 * The tests for active speaker detection feature.
 *
 * @author Pawel Domas
 */
public class ActiveSpeakerTest
    extends AbstractBaseTest
{
    @Override
    public void setup()
    {
        super.setup();

        // This test requires a conference of three
        ensureThreeParticipants();
    }

    /**
     * Active speaker test scenario.
     */
    @Test
    public void testActiveSpeaker()
    {
        // skip if we are not chrome
        if (!getParticipant1().getType().isChrome())
        {
            throw new SkipException("skip as it is not chrome");
        }

        // Mute all
        muteAllParticipants();

        // Owner becomes active speaker - check from 2nd peer's perspective
        testActiveSpeaker(
            getParticipant1(), getParticipant2(), getParticipant3());
        // 3rd peer becomes active speaker - check from 2nd peer's perspective
        testActiveSpeaker(getParticipant3(), getParticipant2(), getParticipant1());
        // 2nd peer becomes active speaker - check from owner's perspective
        testActiveSpeaker(getParticipant2(), getParticipant1(), getParticipant3());

        // check the displayed speakers, there should be only one speaker
        assertOneDominantSpeaker(getParticipant1().getDriver());
        assertOneDominantSpeaker(getParticipant2().getDriver());
        assertOneDominantSpeaker(getParticipant3().getDriver());
    }

    /**
     * Asserts that the number of small videos with the dominant speaker
     * indicator displayed equals 1.
     * @param driver the participant to check
     */
    private void assertOneDominantSpeaker(WebDriver driver)
    {
        List<WebElement> dominantSpeakerIndicators =
            driver.findElements(By.xpath(
                "//span[contains(@id,'dominantspeakerindicator')]"
                ));

        int speakers = 0;
        for (WebElement el : dominantSpeakerIndicators)
        {
            if (el.isDisplayed())
                speakers++;
        }

        assertEquals(
            1, speakers,
            "Wrong number of dominant speaker indicators.");
    }

    private void muteAllParticipants()
    {
        MuteTest muteTest = new MuteTest(this);
        muteTest.muteOwnerAndCheck();
        muteTest.muteParticipantAndCheck();
        muteTest.muteThirdParticipantAndCheck();
    }

    /**
     * Tries to make given participant an active speaker by un-muting him.
     * Verifies from <tt>peer2</tt> perspective if he has been displayed on
     * the large video area. Mutes him back.
     *
     * @param activeSpeaker <tt>Participant</tt> instance of the
     * participant who will be testes as an active speaker.
     * @param peer2 <tt>Participant</tt> of the participant who will
     * be observing and verifying active speaker change.
     * @param peer3 used only to print some debugging info
     */
    private void testActiveSpeaker(
        Participant activeSpeaker,
        Participant peer2,
        Participant peer3)
    {
        // we cannot use firefox as active speaker as it uses constant beep
        // audio which is not detected as speech
        if (!activeSpeaker.getType().isChrome())
        {
            throw new SkipException("skip as it is not chrome");
        }

        print("Start testActiveSpeaker for participant: "
            + activeSpeaker.getName());

        WebDriver peer2driver = peer2.getDriver();

        final String speakerEndpoint
            = MeetUtils.getResourceJid(activeSpeaker.getDriver());

        // just a debug print to go in logs
        activeSpeaker.executeScript(
                "console.log('Unmuting in testActiveSpeaker');");
        // Unmute
        MeetUIUtils.clickOnToolbarButton(
            activeSpeaker.getDriver(),
            "toolbar_button_mute");
        // just a debug print to go in logs
        peer2.executeScript(
                "console.log('Participant unmuted in testActiveSpeaker "
                    + speakerEndpoint + "');");
        // just a debug print to go in logs
        peer3.executeScript(
                "console.log('Participant unmuted in testActiveSpeaker "
                    + speakerEndpoint + "');");
        MeetUIUtils.assertMuteIconIsDisplayed(
                peer2.getDriver(),
                activeSpeaker.getDriver(),
                false,
                false, //audio
                speakerEndpoint);

        // Verify that the user is now an active speaker from peer2 perspective
        try
        {
            new WebDriverWait(peer2driver, 10).until(
                (ExpectedCondition<Boolean>) d -> speakerEndpoint.equals(
                    MeetUIUtils.getLargeVideoResource(d)));
        }
        catch (TimeoutException exc)
        {
            assertEquals(
                speakerEndpoint, MeetUIUtils.getLargeVideoResource(peer2driver),
                "Active speaker not displayed on large video " + new Date());
        }

        // just a debug print to go in logs
        activeSpeaker.executeScript(
                "console.log('Muting in testActiveSpeaker');");
        // Mute back again
        MeetUIUtils.clickOnToolbarButton(
            activeSpeaker.getDriver(),
            "toolbar_button_mute");
        // just a debug print to go in logs
        peer2.executeScript(
                "console.log('Participant muted in testActiveSpeaker "
                    + speakerEndpoint + "');");
        // just a debug print to go in logs
        peer3.executeScript(
                "console.log('Participant muted in testActiveSpeaker "
                    + speakerEndpoint + "');");
        MeetUIUtils.assertMuteIconIsDisplayed(
                peer2driver,
                activeSpeaker.getDriver(),
                true,
                false, //audio
                speakerEndpoint);
    }
}
