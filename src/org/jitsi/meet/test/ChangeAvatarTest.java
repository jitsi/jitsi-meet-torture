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
        WebDriver owner = ConferenceFixture.getOwner();
        final WebDriver secondParticipant
            = ConferenceFixture.getSecondParticipant();

        final String ownerResourceJid = (String)((JavascriptExecutor) owner)
            .executeScript(
                "return APP.xmpp.myResource();");

        final String srcOneSecondParticipant =
            getParticipantSrc(secondParticipant,
                "//span[@id='participant_" + ownerResourceJid
                    + "']/img[@class='userAvatar']");

        //change the email for the conference owner
        TestUtils.clickOnToolbarButton(owner, "settingsButton");
        TestUtils.waitsForDisplayedElementByXPath(
            owner, "//input[@id='setEmail']", 5);
        owner.findElement(By.xpath("//input[@id='setEmail']")).sendKeys(EMAIL);
        owner.findElement(By.id("updateSettings")).click();

        TestUtils.waits(1000);

        //check if the local avatar in the settings menu has changed
        checkSrcIsCorrect(owner, "//img[@id='avatar']");
        //check if the avatar in the local thumbnail has changed
        checkSrcIsCorrect(owner,
            "//span[@id='localVideoContainer']/img[@class='userAvatar']");
        //check if the avatar in the contact list has changed
        checkSrcIsCorrect(owner,
            "//div[@id='contactlist']/ul/li[@id='"
                + ownerResourceJid + "']/img");

        // waits till the src changes so we can continue with the check
        // sometimes the notification for the avatar change can be more
        // than 5 seconds
        (new WebDriverWait(secondParticipant, 15))
                .until(new ExpectedCondition<Boolean>()
                {
                    public Boolean apply(WebDriver d)
                    {
                        String currentSrc =
                            getParticipantSrc(secondParticipant,
                                "//span[@id='participant_" + ownerResourceJid
                                    + "']/img[@class='userAvatar']");
                        return !currentSrc.equals(srcOneSecondParticipant);
                    }
                });

        //check if the avatar in the thumbnail for the other participant has
        // changed
        checkSrcIsCorrect(secondParticipant,
            "//span[@id='participant_" + ownerResourceJid
                + "']/img[@class='userAvatar']");
        //check if the avatar in the contact list has changed for the other
        // participant
        checkSrcIsCorrect(secondParticipant,
            "//div[@id='contactlist']/ul/li[@id='"
                + ownerResourceJid + "']/img");
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
        String src = getParticipantSrc(participant, xpath);
        assertTrue("The avatar has the correct src", src.contains(HASH));
    }

    /**
     * Return the participant avatar src that we will check
     * @return the participant avatar src that we will check
     */
    private String getParticipantSrc(WebDriver participant, String xpath)
    {
        return participant.findElement(By.xpath(xpath)).getAttribute("src");
    }

}
