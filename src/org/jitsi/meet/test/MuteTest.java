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
import org.openqa.selenium.interactions.*;

/**
 * Mutes and unmutes tests.
 * @author Damian Minkov
 */
public class MuteTest
    extends TestCase
{
    /**
     * Constructs test
     * @param name the method name for the test.
     */
    public MuteTest(String name)
    {
        super(name);
    }

    /**
     * Orders the tests.
     * @return the suite with order tests.
     */
    public static junit.framework.Test suite()
    {
        TestSuite suite = new TestSuite();

        suite.addTest(new MuteTest("muteOwnerAndCheck"));
        suite.addTest(new MuteTest("unMuteOwnerAndCheck"));
        suite.addTest(new MuteTest("muteParticipantAndCheck"));
        suite.addTest(new MuteTest("unMuteParticipantAndCheck"));
        suite.addTest(new MuteTest("ownerMutesParticipantAndCheck"));
        suite.addTest(new MuteTest(
                    "participantUnMutesAfterOwnerMutedHimAndCheck"));
        suite.addTest(new MuteTest(
                    "muteOwnerBeforeSecondParticipantJoins"));

        return suite;
    }

    /**
     * Mutes the owner and checks at other participant is this is visible.
     */
    public void muteOwnerAndCheck()
    {
        MeetUIUtils.clickOnToolbarButton(ConferenceFixture.getOwner(), "toolbar_button_mute");

        MeetUIUtils.verifyIsMutedStatus(
            "owner",
            ConferenceFixture.getOwner(),
            ConferenceFixture.getSecondParticipant(),
            true);
    }

    /**
     * Unmutes owner and checks at other participant is this is visible.
     */
    public void unMuteOwnerAndCheck()
    {
        MeetUIUtils.clickOnToolbarButton(ConferenceFixture.getOwner(), "toolbar_button_mute");

        MeetUIUtils.verifyIsMutedStatus(
            "owner",
            ConferenceFixture.getOwner(),
            ConferenceFixture.getSecondParticipant(),
            false);
    }

    /**
     * Mutes the participant and checks at owner side.
     */
    public void muteParticipantAndCheck()
    {
        MeetUIUtils.clickOnToolbarButton(
            ConferenceFixture.getSecondParticipant(), "toolbar_button_mute");

        MeetUIUtils.verifyIsMutedStatus(
            "participant2",
            ConferenceFixture.getSecondParticipant(),
            ConferenceFixture.getOwner(),
            true);
    }

    /**
     * UnMutes the participant and checks at owner side.
     */
    public void unMuteParticipantAndCheck()
    {
        MeetUIUtils.clickOnToolbarButton(
            ConferenceFixture.getSecondParticipant(), "toolbar_button_mute");

        MeetUIUtils.verifyIsMutedStatus(
            "participant2",
            ConferenceFixture.getSecondParticipant(),
            ConferenceFixture.getOwner(),
            false);
    }

    /**
     * Mutes the participant and checks at owner side.
     */
    public void muteThirdParticipantAndCheck()
    {
        MeetUIUtils.clickOnToolbarButton(
            ConferenceFixture.getThirdParticipant(), "toolbar_button_mute");

        MeetUIUtils.verifyIsMutedStatus(
            "participant3",
            ConferenceFixture.getThirdParticipant(),
            ConferenceFixture.getOwner(),
            true);
    }

    /**
     * UnMutes the participant and checks at owner side.
     */
    public void unMuteThirdParticipantAndCheck()
    {
        MeetUIUtils.clickOnToolbarButton(
            ConferenceFixture.getThirdParticipant(), "toolbar_button_mute");

        MeetUIUtils.verifyIsMutedStatus(
            "participant3",
            ConferenceFixture.getThirdParticipant(),
            ConferenceFixture.getOwner(),
            false);
    }

    /**
     * Finds the menu that can be used by the owner to control the participant.
     * Hovers over it. Finds the mute link and mute it.
     * Then checks in the second participant page whether it is muted
     */
    public void ownerMutesParticipantAndCheck()
    {
        WebElement elem = ConferenceFixture.getOwner().findElement(By.xpath(
            "//span[@class='remotevideomenu']/i[@class='fa fa-angle-down']"));

        Actions action = new Actions(ConferenceFixture.getOwner());
        action.moveToElement(elem);
        action.perform();

        TestUtils.waitsForDisplayedElementByXPath(
            ConferenceFixture.getOwner(),
            "//ul[@class='popupmenu']/li/a[@class='mutelink']",
            5);

        ConferenceFixture.getOwner().findElement(
                By.xpath("//ul[@class='popupmenu']/li/a[@class='mutelink']"))
            .click();

        // and now check whether second participant is muted
        TestUtils.waitsForElementByXPath(
            ConferenceFixture.getSecondParticipant(),
            "//span[@class='audioMuted']/i[@class='icon-mic-disabled']", 5);

        action.release();
    }

    /**
     * UnMutes once again the second participant and checks in the owner page
     * does this change is reflected.
     */
    public void participantUnMutesAfterOwnerMutedHimAndCheck()
    {
        MeetUIUtils.clickOnToolbarButton(
            ConferenceFixture.getSecondParticipant(), "toolbar_button_mute");

        TestUtils.waitsForElementNotPresentByXPath(
            ConferenceFixture.getOwner(),
            "//span[@class='audioMuted']/i[@class='icon-mic-disabled']", 5);

        // lets give time to the ui to reflect the change in the ui of the owner
        TestUtils.waits(1000);
    }

    /**
     * Closes the participant and leaves the owner alone in the room.
     * Mutes the owner and then joins new participant and checks the status
     * of the mute icon.
     * At the end unmutes to clear the state.
     */
    public void muteOwnerBeforeSecondParticipantJoins()
    {
        ConferenceFixture.quit(ConferenceFixture.getSecondParticipant());

        // just in case wait
        TestUtils.waits(1000);

        MeetUIUtils.clickOnToolbarButton(ConferenceFixture.getOwner(), "toolbar_button_mute");

        ConferenceFixture.startParticipant();

        ConferenceFixture.checkParticipantToJoinRoom(
            ConferenceFixture.getSecondParticipant(), 10);

        ConferenceFixture.waitsParticipantToJoinConference(
            ConferenceFixture.getSecondParticipant());

        TestUtils.waitsForElementByXPath(
            ConferenceFixture.getSecondParticipant(),
            "//span[@class='audioMuted']/i[@class='icon-mic-disabled']", 5);

        // now lets unmute
        unMuteOwnerAndCheck();
    }
}
