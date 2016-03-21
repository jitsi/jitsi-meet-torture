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
 * Tests the muting and unmuting of the participants in Meet conferences.
 *
 * @author Damian Minkov
 * @author Lyubomir Marinov
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
        toggleMuteAndCheck(
                "muteOwnerAndCheck",
                ConferenceFixture.getOwner(),
                "owner",
                ConferenceFixture.getSecondParticipant(),
                true);

        TestUtils.waitMillis(2000);
    }

    /**
     * Unmutes owner and checks at other participant is this is visible.
     */
    public void unMuteOwnerAndCheck()
    {
        toggleMuteAndCheck(
                "unMuteOwnerAndCheck",
                ConferenceFixture.getOwner(),
                "owner",
                ConferenceFixture.getSecondParticipant(),
                false);
    }

    /**
     * Mutes the participant and checks at owner side.
     */
    public void muteParticipantAndCheck()
    {
        toggleMuteAndCheck(
                "muteParticipantAndCheck",
                ConferenceFixture.getSecondParticipant(),
                "participant2",
                ConferenceFixture.getOwner(),
                true);
    }

    /**
     * UnMutes the participant and checks at owner side.
     */
    public void unMuteParticipantAndCheck()
    {
        toggleMuteAndCheck(
                "unMuteParticipantAndCheck",
                ConferenceFixture.getSecondParticipant(),
                "participant2",
                ConferenceFixture.getOwner(),
                false);
    }

    /**
     * Mutes the participant and checks at owner side.
     */
    public void muteThirdParticipantAndCheck()
    {
        toggleMuteAndCheck(
                "muteThirdParticipantAndCheck",
                ConferenceFixture.getThirdParticipant(),
                "participant3",
                ConferenceFixture.getOwner(),
                true);
    }

    /**
     * UnMutes the participant and checks at owner side.
     */
    public void unMuteThirdParticipantAndCheck()
    {
        toggleMuteAndCheck(
                "unMuteThirdParticipantAndCheck",
                ConferenceFixture.getThirdParticipant(),
                "participant3",
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
        System.err.println("Start ownerMutesParticipantAndCheck.");

        WebDriver owner = ConferenceFixture.getOwner();

        WebElement elem = owner.findElement(By.xpath(
            "//span[@class='remotevideomenu']/i[@class='fa fa-angle-down']"));

        Actions action = new Actions(owner);
        action.moveToElement(elem);
        action.perform();

        // for some reason equivalent xpath selector doesn't work
        // By.xpath("//ul[@class='popupmenu']//a[@class='mutelink']")
        owner.findElement(By.cssSelector("ul.popupmenu a.mutelink")).click();

        // and now check whether second participant is muted
        TestUtils.waitForElementByXPath(
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
        System.err.println("Start participantUnMutesAfterOwnerMutedHimAndCheck.");

        TestUtils.waitMillis(1000);

        MeetUIUtils.clickOnToolbarButton(
            ConferenceFixture.getSecondParticipant(), "toolbar_button_mute");

        TestUtils.waitMillis(1000);

        MeetUIUtils.assertMuteIconIsDisplayed(
                ConferenceFixture.getOwner(),
                ConferenceFixture.getSecondParticipant(),
                false, //should be unmuted
                false, //audio
                "participant2"
        );

        // lets give time to the ui to reflect the change in the ui of the owner
        TestUtils.waitMillis(1000);
    }

    /**
     * Closes the participant and leaves the owner alone in the room.
     * Mutes the owner and then joins new participant and checks the status
     * of the mute icon.
     * At the end unmutes to clear the state.
     */
    public void muteOwnerBeforeSecondParticipantJoins()
    {
        System.err.println("Start muteOwnerBeforeSecondParticipantJoins.");

        WebDriver owner = ConferenceFixture.getOwner();
        ConferenceFixture.close(ConferenceFixture.getSecondParticipant());

        // just in case wait
        TestUtils.waitMillis(1000);

        MeetUIUtils.clickOnToolbarButton(owner, "toolbar_button_mute");

        WebDriver secondParticipant
            = ConferenceFixture.startSecondParticipant();

        ConferenceFixture.waitForParticipantToJoinMUC(
                secondParticipant, 15);

        ConferenceFixture.waitForIceCompleted(secondParticipant);

        MeetUIUtils.assertMuteIconIsDisplayed(
                secondParticipant,
                owner,
                true, //should be muted
                false, //audio
                "owner");

        // now lets unmute
        unMuteOwnerAndCheck();
    }

    /**
     * Toggles the mute state of a specific Meet conference participant and
     * verifies that a specific other Meet conference participants sees a
     * specific mute state for the former.
     *
     * @param testName the name of test (to be logged)
     * @param testee the {@code WebDriver} which represents the Meet conference
     * participant whose mute state is to be toggled
     * @param testeeName the name of {@code testee} to be displayed should the
     * test fail
     * @param observer the {@code WebDriver} which represents the Meet
     * conference participant to verify the mute state of {@code testee}
     * @param muted the mute state of {@code testee} expected to be observed by
     * {@code observer}
     */
    private void toggleMuteAndCheck(
            String testName,
            WebDriver testee,
            String testeeName,
            WebDriver observer,
            boolean muted)
    {
        System.err.println("Start " + testName + ".");
        MeetUIUtils.clickOnToolbarButton(testee, "toolbar_button_mute");
        MeetUIUtils.assertMuteIconIsDisplayed(
                observer,
                testee,
                muted,
                false, //audio
                testeeName);
    }
}
