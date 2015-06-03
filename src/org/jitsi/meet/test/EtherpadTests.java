/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jitsi.meet.test;

import junit.framework.*;
import org.jitsi.meet.test.util.*;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;

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
        suite.addTest(new EtherpadTests("ownerClickOnLocalVideoAndTest"));
        suite.addTest(new EtherpadTests("ownerClickOnRemoteVideoAndTest"));
        suite.addTest(new EtherpadTests("enterEtherpad"));
        //suite.addTest(new EtherpadTests("writeTextAndCheck"));
        //lets not directly click on videos without closing etherpad
        suite.addTest(new SwitchVideoTests("ownerClickOnLocalVideoAndTest"));
        suite.addTest(new SwitchVideoTests("ownerClickOnRemoteVideoAndTest"));

        return suite;
    }

    /**
     * Clicks on share document button and check whether etherpad is loaded
     * and video is hidden.
     */
    public void enterEtherpad()
    {
        // waits for etherpad button to be displayed in the toolbar
        TestUtils.waitsForDisplayedElementByID(
            ConferenceFixture.getOwner(), "etherpadButton", 15);

        MeetUIUtils.clickOnToolbarButtonByClass(ConferenceFixture.getOwner(),
            "icon-share-doc");

        TestUtils.waits(5000);

        TestUtils.waitsForNotDisplayedElementByID(
            ConferenceFixture.getOwner(), "largeVideo", 10);
    }

    /**
     * Write some text and check on the other side, whether the text
     * is visible.
     */
    public void writeTextAndCheck()
    {
        try
        {
            WebDriverWait wait = new WebDriverWait(
                ConferenceFixture.getOwner(), 30);
            wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(
                By.tagName("iframe")));

            wait = new WebDriverWait(
                ConferenceFixture.getOwner(), 30);
            wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(
                By.name("ace_outer")));

            wait = new WebDriverWait(
                ConferenceFixture.getOwner(), 30);
            wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(
                By.name("ace_inner")));

            String textToEnter = "SomeTestText";

            ConferenceFixture.getOwner().findElement(
                By.id("innerdocbody")).sendKeys(textToEnter);

            TestUtils.waits(2000);

            // now search and check the text
            String txt = ConferenceFixture.getOwner().findElement(
                By.xpath("//span[contains(@class, 'author')]")).getText();

            assertEquals("Texts do not match", textToEnter, txt);
        }
        finally
        {
            ConferenceFixture.getOwner().switchTo().defaultContent();
        }
    }

    /**
     * Close the etherpad and checks whether video is still visible.
     */
    public void closeEtherpadCheck()
    {
        MeetUIUtils.clickOnToolbarButtonByClass(ConferenceFixture.getOwner(),
            "icon-share-doc");

        TestUtils.waits(5000);

        TestUtils.waitsForDisplayedElementByID(
            ConferenceFixture.getOwner(), "largeVideo", 10);
    }

    /**
     * Click on local video thumbnail and checks whether the large video
     * is the local one.
     */
    public void ownerClickOnLocalVideoAndTest()
    {
        new SwitchVideoTests("ownerClickOnLocalVideoAndTest")
            .ownerClickOnLocalVideoAndTest();
    }

    /**
     * Clicks on the remote video thumbnail and checks whether the large video
     * is the remote one.
     */
    public void ownerClickOnRemoteVideoAndTest()
    {
        new SwitchVideoTests("ownerClickOnRemoteVideoAndTest")
            .ownerClickOnRemoteVideoAndTest();
    }


}
