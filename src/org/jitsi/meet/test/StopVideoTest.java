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
 * To stop the video on focus and participant side.
 * @author Damian Minkov
 */
public class StopVideoTest
    extends TestCase
{
    /**
     * Constructs test.
     * @param name the method name for the test.
     */
    public StopVideoTest(String name)
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

        suite.addTest(new StopVideoTest("stopVideoOnFocusAndCheck"));
        suite.addTest(new StopVideoTest("startVideoOnFocusAndCheck"));
        suite.addTest(new StopVideoTest("stopVideoOnParticipantAndCheck"));
        suite.addTest(new StopVideoTest("startVideoOnParticipantAndCheck"));
        suite.addTest(new StopVideoTest(
            "stopFocusVideoBeforeSecondParticipantJoins"));

        return suite;
    }

    /**
     * Stops the video on focus.
     */
    public void stopVideoOnFocusAndCheck()
    {
        TestUtils.clickOnToolbarButton(ConferenceFixture.focus, "video");

        TestUtils.waitsForElementByXPath(
            ConferenceFixture.secondParticipant,
            "//span[@class='videoMuted']/i[@class='icon-camera-disabled']", 5);
    }

    /**
     * Starts the video on focus.
     */
    public void startVideoOnFocusAndCheck()
    {
        TestUtils.clickOnToolbarButton(ConferenceFixture.focus, "video");

        TestUtils.waitsForElementNotPresentByXPath(
            ConferenceFixture.secondParticipant,
            "//span[@class='videoMuted']/i[@class='icon-camera-disabled']", 10);
    }

    /**
     * Stops the video on participant.
     */
    public void stopVideoOnParticipantAndCheck()
    {
        TestUtils.clickOnToolbarButton(
            ConferenceFixture.secondParticipant, "video");

        TestUtils.waitsForElementByXPath(
            ConferenceFixture.focus,
            "//span[@class='videoMuted']/i[@class='icon-camera-disabled']", 5);
    }

    /**
     * Starts the video on participant.
     */
    public void startVideoOnParticipantAndCheck()
    {
        TestUtils.clickOnToolbarButton(
            ConferenceFixture.secondParticipant, "video");

        TestUtils.waitsForElementNotPresentByXPath(
            ConferenceFixture.focus,
            "//span[@class='videoMuted']/i[@class='icon-camera-disabled']", 5);
    }

    /**
     * Closes the participant and leaves the focus alone in the room.
     * Stops video of the focus and then joins new participant and
     * checks the status of the stopped video icon.
     * At the end starts the video to clear the state.
     */
    public void stopFocusVideoBeforeSecondParticipantJoins()
    {
        ConferenceFixture.secondParticipant.close();

        // just in case wait
        TestUtils.waits(1000);

        TestUtils.clickOnToolbarButton(ConferenceFixture.focus, "video");

        ConferenceFixture.startParticipant();

        ConferenceFixture.checkParticipantToJoinRoom(
            ConferenceFixture.secondParticipant, 10);

        ConferenceFixture.waitsSecondParticipantToJoinConference();

        TestUtils.waitsForElementByXPath(
            ConferenceFixture.secondParticipant,
            "//span[@class='videoMuted']/i[@class='icon-camera-disabled']", 5);

        // now lets start video for focus
        startVideoOnFocusAndCheck();
    }

}
