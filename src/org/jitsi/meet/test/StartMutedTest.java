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
     * Test if the second participant is muted and the owner is unmuted.
     */
    public void checkboxesTest()
    {
        ConferenceFixture.quit(ConferenceFixture.getSecondParticipant());
        TestUtils.waits(1000);
        WebDriver owner = ConferenceFixture.getOwner();

        // The settings panel is opened from the previous test.
        TestUtils.waitsForDisplayedElementByXPath(
            owner, "//input[@id='startAudioMuted']", 5);
        TestUtils.waitsForDisplayedElementByXPath(
            owner, "//input[@id='startVideoMuted']", 5);

        owner.findElement(By.id("startAudioMuted")).click();
        owner.findElement(By.id("startVideoMuted")).click();
        owner.findElement(By.id("updateSettings")).click();

        ConferenceFixture.startParticipant();
        ConferenceFixture.checkParticipantToJoinRoom(
            ConferenceFixture.getSecondParticipant(), 10);

        ConferenceFixture.waitsParticipantToJoinConference(
            ConferenceFixture.getSecondParticipant());

        checkSecondParticipantForMute();

    }

    /**
     * Opens new room and sets start muted config parameters trough the URL.
     * Test if the second participant is muted and the owner is unmuted.
     */
    public void configOptionsTest()
    {
        ConferenceFixture.quit(ConferenceFixture.getSecondParticipant());
        ConferenceFixture.quit(ConferenceFixture.getOwner());
        TestUtils.waits(1000);
        ConferenceFixture.startOwner("config.startAudioMuted=1&"
            + "config.startVideoMuted=1");
        ConferenceFixture.checkParticipantToJoinRoom(
            ConferenceFixture.getOwner(), 10);

        ConferenceFixture.waitsParticipantToJoinConference(
            ConferenceFixture.getOwner());

        ConferenceFixture.startParticipant();

        ConferenceFixture.checkParticipantToJoinRoom(
            ConferenceFixture.getSecondParticipant(), 10);

        ConferenceFixture.waitsParticipantToJoinConference(
            ConferenceFixture.getSecondParticipant());

        checkSecondParticipantForMute();
    }

    /**
     * Tests if the second participant is muted and the first participant is
     * unmuted.
     */
    private void checkSecondParticipantForMute()
    {
        WebDriver secondParticipant = ConferenceFixture.getSecondParticipant();
        WebDriver owner = ConferenceFixture.getOwner();

        final String ownerResourceJid
            = (String)((JavascriptExecutor) owner)
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
            "//span[@id='participant_" + ownerResourceJid + "']/"
                + "span[@class='audioMuted']/i[@class='icon-mic-disabled']", 25);

        TestUtils.waitsForElementNotPresentByXPath(
            secondParticipant,
            "//span[@id='participant_" + ownerResourceJid + "']/"
                + "span[@class='videoMuted']/i[@class='icon-camera-disabled']",
                25);


    }

}
