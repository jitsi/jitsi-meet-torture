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
 * Adds various tests with the etherpad functionality.
 * @author Damian Minkov
 */
public class EtherpadTests
    extends TestCase
{
    /**
     * Constructs test.
     * @param name the method name for the test.
     */
    public EtherpadTests(String name)
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

        suite.addTest(new EtherpadTests("enterEtherpad"));
        suite.addTest(new EtherpadTests("writeTextAndCheck"));
        suite.addTest(new EtherpadTests("closeEtherpadCheck"));
        // lets check after closing etherpad we are able to click on videos
        suite.addTest(new EtherpadTests("focusClickOnLocalVideoAndTest"));
        suite.addTest(new EtherpadTests("focusClickOnRemoteVideoAndTest"));
        suite.addTest(new EtherpadTests("enterEtherpad"));
        //suite.addTest(new EtherpadTests("writeTextAndCheck"));
        //lets not directly click on videos without closing etherpad
        suite.addTest(new SwitchVideoTests("focusClickOnLocalVideoAndTest"));
        suite.addTest(new SwitchVideoTests("focusClickOnRemoteVideoAndTest"));

        return suite;
    }

    /**
     * Clicks on share document button and check whether etherpad is loaded
     * and video is hidden.
     */
    public void enterEtherpad()
    {
        TestUtils.clickOnToolbarButtonByClass(ConferenceFixture.getFocus(),
            "icon-share-doc");

        TestUtils.waits(5000);

        TestUtils.waitsForNotDisplayedElementByID(
            ConferenceFixture.getFocus(), "largeVideo", 10);
    }

    /**
     * Write some text and check on the other side, whether the text
     * is visible.
     */
    public void writeTextAndCheck()
    {
        TestUtils.waits(10000);

        ConferenceFixture.getFocus().switchTo().frame(
            ConferenceFixture.getFocus().findElement(By.tagName("iframe")));
        ConferenceFixture.getFocus().switchTo().frame(
            ConferenceFixture.getFocus().findElement(By.name("ace_outer")));
        ConferenceFixture.getFocus().switchTo().frame(
            ConferenceFixture.getFocus().findElement(By.name("ace_inner")));

        String textToEnter = "SomeTestText";

        ConferenceFixture.getFocus().findElement(
            By.id("innerdocbody")).sendKeys(textToEnter);

        TestUtils.waits(2000);

        // now search and check the text
        String txt = ConferenceFixture.getFocus().findElement(
            By.xpath("//span[contains(@class, 'author')]")).getText();

        assertEquals("Texts do not match", textToEnter, txt);

        ConferenceFixture.getFocus().switchTo().defaultContent();
    }

    /**
     * Close the etherpad and checks whether video is still visible.
     */
    public void closeEtherpadCheck()
    {
        TestUtils.clickOnToolbarButtonByClass(ConferenceFixture.getFocus(),
            "icon-share-doc");

        TestUtils.waits(5000);

        TestUtils.waitsForDisplayedElementByID(
            ConferenceFixture.getFocus(), "largeVideo", 10);
    }

    /**
     * Click on local video thumbnail and checks whether the large video
     * is the local one.
     */
    public void focusClickOnLocalVideoAndTest()
    {
        new SwitchVideoTests("focusClickOnLocalVideoAndTest")
            .focusClickOnLocalVideoAndTest();
    }

    /**
     * Clicks on the remote video thumbnail and checks whether the large video
     * is the remote one.
     */
    public void focusClickOnRemoteVideoAndTest()
    {
        new SwitchVideoTests("focusClickOnRemoteVideoAndTest")
            .focusClickOnRemoteVideoAndTest();
    }


}
