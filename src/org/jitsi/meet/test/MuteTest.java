/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jitsi.meet.test;

import junit.framework.*;
import org.openqa.selenium.*;

/**
 * Mutes and unmutes tests.
 * @TODO add tests to first mute focus and then to join participant,
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

        suite.addTest(new MuteTest("muteFocusAndCheck"));
        suite.addTest(new MuteTest("unMuteFocusAndCheck"));
        suite.addTest(new MuteTest("muteParticipantAndCheck"));
        suite.addTest(new MuteTest("unMuteParticipantAndCheck"));
        suite.addTest(new MuteTest("focusMutesParticipantAndCheck"));
        suite.addTest(new MuteTest(
                    "participantUnMutesAfterFocusMutedHimAndCheck"));

        return suite;
    }

    /**
     * Mutes the focus and checks at other participant is this is visible.
     */
    public void muteFocusAndCheck()
    {
        ConferenceFixture.focus.findElement(By.id("mute")).click();

        TestUtils.waitsForElementByXPath(
            ConferenceFixture.secondParticipant,
            "//span[@class='audioMuted']/i[@class='icon-mic-disabled']", 5);

    }

    /**
     * Unmutes focus and checks at other participant is this is visible.
     */
    public void unMuteFocusAndCheck()
    {
        ConferenceFixture.focus.findElement(By.id("mute")).click();

        TestUtils.waitsForElementNotPresentByXPath(
            ConferenceFixture.secondParticipant,
            "//span[@class='audioMuted']/i[@class='icon-mic-disabled']", 5);
    }

    /**
     * Mutes the participant and checks at focus side.
     */
    public void muteParticipantAndCheck()
    {
        ConferenceFixture.secondParticipant.findElement(By.id("mute")).click();

        TestUtils.waitsForElementByXPath(
            ConferenceFixture.focus,
            "//span[@class='audioMuted']/i[@class='icon-mic-disabled']", 5);
    }

    /**
     * UnMutes the participant and checks at focus side.
     */
    public void unMuteParticipantAndCheck()
    {
        ConferenceFixture.secondParticipant.findElement(By.id("mute")).click();

        TestUtils.waitsForElementNotPresentByXPath(
            ConferenceFixture.focus,
            "//span[@class='audioMuted']/i[@class='icon-mic-disabled']", 5);
    }

    public void focusMutesParticipantAndCheck()
    {
        // TODO
    }

    public void participantUnMutesAfterFocusMutedHimAndCheck()
    {
        // TODO
    }
}
