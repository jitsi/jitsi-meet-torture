/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jitsi.meet.test;

import junit.framework.*;
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
        TestUtils.clickOnToolbarButton(ConferenceFixture.getOwner(), "mute");

        TestUtils.waitsForElementByXPath(
            ConferenceFixture.getSecondParticipant(),
            "//span[@class='audioMuted']/i[@class='icon-mic-disabled']", 5);

    }

    /**
     * Unmutes owner and checks at other participant is this is visible.
     */
    public void unMuteOwnerAndCheck()
    {
        TestUtils.clickOnToolbarButton(ConferenceFixture.getOwner(), "mute");

        TestUtils.waitsForElementNotPresentByXPath(
            ConferenceFixture.getSecondParticipant(),
            "//span[@class='audioMuted']/i[@class='icon-mic-disabled']", 5);
    }

    /**
     * Mutes the participant and checks at owner side.
     */
    public void muteParticipantAndCheck()
    {
        TestUtils.clickOnToolbarButton(
            ConferenceFixture.getSecondParticipant(), "mute");

        TestUtils.waitsForElementByXPath(
            ConferenceFixture.getOwner(),
            "//span[@class='audioMuted']/i[@class='icon-mic-disabled']", 5);
    }

    /**
     * UnMutes the participant and checks at owner side.
     */
    public void unMuteParticipantAndCheck()
    {
        TestUtils.clickOnToolbarButton(
            ConferenceFixture.getSecondParticipant(), "mute");

        TestUtils.waitsForElementNotPresentByXPath(
            ConferenceFixture.getOwner(),
            "//span[@class='audioMuted']/i[@class='icon-mic-disabled']", 5);
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
        TestUtils.clickOnToolbarButton(
            ConferenceFixture.getSecondParticipant(), "mute");

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

        TestUtils.clickOnToolbarButton(ConferenceFixture.getOwner(), "mute");

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
