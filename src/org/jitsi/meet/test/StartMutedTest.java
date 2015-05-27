/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jitsi.meet.test;

import junit.framework.*;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;

/**
 * Start muted tests
 * @author Hristo Terezov
 */
public class StartMutedTest
    extends TestCase
{
    /**
     * Constructs test
     * @param name the method name for the test.
     */
    public StartMutedTest(String name)
    {
        super(name);
    }

    /**
     * Orders the tests.
     * @return the suite with order tests.
     */
    public static Test suite()
    {
        TestSuite suite = new TestSuite();

        suite.addTest(new StartMutedTest("checkboxesTest"));
        suite.addTest(new StartMutedTest("configOptionsTest"));
        return suite;
    }

    /**
     * Restarts the second participant tab and checks start muted checkboxes.
     * Test if the second participant is muted and the focus is unmuted.
     */
    public void checkboxesTest()
    {
        ConferenceFixture.quit(ConferenceFixture.getSecondParticipant());

        WebDriver focus = ConferenceFixture.getFocus();

        // The settings panel is opened from the previous test.
        TestUtils.waitsForDisplayedElementByXPath(
            focus, "//input[@id='startAudioMuted']", 5);
        TestUtils.waitsForDisplayedElementByXPath(
            focus, "//input[@id='startVideoMuted']", 5);

        focus.findElement(By.id("startAudioMuted")).click();
        focus.findElement(By.id("startVideoMuted")).click();
        focus.findElement(By.id("updateSettings")).click();

        ConferenceFixture.startParticipant();
        checkSecondParticipantForMute();

    }

    /**
     * Opens new room and sets start muted config parameters trough the URL.
     * Test if the second participant is muted and the focus is unmuted.
     */
    public void configOptionsTest()
    {
        ConferenceFixture.quit(ConferenceFixture.getSecondParticipant());
        ConferenceFixture.quit(ConferenceFixture.getFocus());
        ConferenceFixture.startFocus("config.startAudioMuted=2&"
            + "config.startVideoMuted=2");
        ConferenceFixture.startParticipant();
        checkSecondParticipantForMute();
    }

    /**
     * Tests if the second participant is muted and the first participant is
     * unmuted.
     */
    private void checkSecondParticipantForMute()
    {
        WebDriver secondParticipant = ConferenceFixture.getSecondParticipant();
        WebDriver focus = ConferenceFixture.getFocus();

        final String focusResourceJid
            = (String)((JavascriptExecutor) focus)
                .executeScript("return APP.xmpp.myResource();");

        TestUtils.waitsForElementByXPath(
            secondParticipant,
            "//span[@id='localVideoContainer']/span[@class='audioMuted']/"
            + "i[@class='icon-mic-disabled']", 25);

        TestUtils.waitsForElementByXPath(
            secondParticipant,
            "//span[@id='localVideoContainer']/span[@class='videoMuted']/"
            + "i[@class='icon-camera-disabled']", 25);

        TestUtils.waitsForElementNotPresentByXPath(
            secondParticipant,
            "//span[@id='participant_" + focusResourceJid + "']/"
                + "span[@class='audioMuted']/i[@class='icon-mic-disabled']", 25);

        TestUtils.waitsForElementNotPresentByXPath(
            secondParticipant,
            "//span[@id='participant_" + focusResourceJid + "']/"
                + "span[@class='videoMuted']/i[@class='icon-camera-disabled']",
                25);


    }

}
