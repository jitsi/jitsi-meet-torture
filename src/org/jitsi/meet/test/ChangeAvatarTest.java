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
 * Change the avatar test
 */
public class ChangeAvatarTest
    extends TestCase
{
    /**
     * Constructs test
     * @param name the method name for the test.
     */
    public ChangeAvatarTest(String name)
    {
        super(name);
    }


    public static String EMAIL = "example@jitsi.org";
    public static String HASH = "dc47c9b1270a4a25a60bab7969e7632d";

    /**
     * Orders the tests.
     * @return the suite with order tests.
     */
    public static junit.framework.Test suite()
    {
        TestSuite suite = new TestSuite();

        suite.addTest(new ChangeAvatarTest("changeAvatarAndCheck"));
        return suite;
    }

    /**
     *  Changes the avatar for one participant and checks if it has changed
     *  properly everywhere
     */
    public void changeAvatarAndCheck()
    {
        WebDriver focus = ConferenceFixture.getFocus();
        WebDriver secondParticipant = ConferenceFixture.getSecondParticipant();

        //chage the email for the focus
        TestUtils.clickOnToolbarButton(focus, "settingsButton");
        TestUtils.waitsForDisplayedElementByXPath(
            focus, "//input[@id='setEmail']", 5);
        focus.findElement(By.xpath("//input[@id='setEmail']")).sendKeys(EMAIL);
        focus.findElement(By.id("updateSettings")).click();

        TestUtils.waits(1000);

        String focusResourceJid = (String)((JavascriptExecutor) focus)
            .executeScript(
            "return Strophe.getResourceFromJid(connection.emuc.myroomjid);");

        //check if the local avatar in the settings menu has changed
        checkSrcIsCorrect(focus, "//img[@id='avatar']");
        //check if the avatar in the local thumbnail has changed
        checkSrcIsCorrect(focus,
            "//span[@id='localVideoContainer']/img[@class='userAvatar']");
        //check if the avatar in the contact list has changed
        checkSrcIsCorrect(focus,
            "//div[@id='contactlist']/ul/li[@id='"
                + focusResourceJid + "']/img");

        //check if the avatar in the thumbnail for the other participant has
        // changed
        checkSrcIsCorrect(secondParticipant,
            "//span[@id='participant_" + focusResourceJid
                + "']/img[@class='userAvatar']");
        //check if the avatar in the contact list has changed for the other
        // participant
        checkSrcIsCorrect(secondParticipant,
            "//div[@id='contactlist']/ul/li[@id='"
                + focusResourceJid + "']/img");
        //check if the active spekaer avatar has changed for the other
        // participant
        checkSrcIsCorrect(secondParticipant,
            "//div[@id='activeSpeaker']/img[@id='activeSpeakerAvatar']");
    }

    /*
     *  Checks if the element with the given xpath has the correct src attribute
     */
    private void checkSrcIsCorrect(WebDriver participant, String xpath)
    {
        String src = participant.findElement(By.xpath(xpath))
            .getAttribute("src");
        assertTrue("The avatar has the correct src", src.contains(HASH));
    }

}
