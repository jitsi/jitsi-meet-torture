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
 * Tests switching video of participants.
 * @author Damian Minkov
 */
public class SwitchVideoTests
    extends TestCase
{
    /**
     * Constructs test.
     * @param name the method name for the test.
     */
    public SwitchVideoTests(String name)
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

        suite.addTest(new SwitchVideoTests(
            "focusClickOnLocalVideoAndTest"));
        suite.addTest(new SwitchVideoTests(
            "focusClickOnRemoteVideoAndTest"));
        suite.addTest(new SwitchVideoTests(
            "participantClickOnLocalVideoAndTest"));
//        suite.addTest(new SwitchVideoTests(
//            "participantClickOnRemoteVideoAndTest"));

        return suite;
    }

    /**
     * Click on local video thumbnail and checks whether the large video
     * is the local one.
     */
    public void focusClickOnLocalVideoAndTest()
    {
        clickOnLocalVideoAndTest(ConferenceFixture.getFocus());
    }

    /**
     * Click on local video thumbnail and checks whether the large video
     * is the local one.
     */
    private void clickOnLocalVideoAndTest(WebDriver driver)
    {
        // click on local
        String localVideoSrc = driver.findElement(
            By.xpath("//span[@id='localVideoWrapper']/video"))
            .getAttribute("src");

        driver.findElement(By.id("localVideoWrapper")).click();

        TestUtils.waits(1000);

        // test is this the video seen
        assertEquals("Video didn't change to local",
            localVideoSrc, getLargeVideoSource(driver));
    }

    /**
     * Returns the source of the large video currently shown.
     * @return the source of the large video currently shown.
     */
    private String getLargeVideoSource(WebDriver driver)
    {
        return driver.findElement(By.xpath(
            "//div[@id='largeVideoContainer']/video[@id='largeVideo']"))
                .getAttribute("src");
    }

    /**
     * Clicks on the remote video thumbnail and checks whether the large video
     * is the remote one.
     */
    public void focusClickOnRemoteVideoAndTest()
    {
        clickOnRemoteVideoAndTest(ConferenceFixture.getFocus());
    }

    /**
     * Clicks on the remote video thumbnail and checks whether the large video
     * is the remote one.
     */
    private void clickOnRemoteVideoAndTest(WebDriver driver)
    {
        // first wait for remote video to be visible
        String remoteThumbXpath
            = "//span[starts-with(@id, 'participant_') " +
            "           and contains(@class,'videocontainer')]";
        // has the src attribute, those without it is the videobridge video tag
        // which is not displayed, it is used for rtcp
        String remoteThumbVideoXpath
            = remoteThumbXpath
                + "/video[starts-with(@id, 'remoteVideo_') and @src]";

        TestUtils.waitsForDisplayedElementByXPath(
            driver,
            remoteThumbVideoXpath,
            5
        );

        WebElement remoteThumb = driver
            .findElement(By.xpath(remoteThumbVideoXpath));

        assertNotNull("Remote video not found", remoteThumb);

        String remoteVideoSrc = remoteThumb.getAttribute("src");

        // click on remote
        driver.findElement(By.xpath(remoteThumbXpath))
            .click();

        TestUtils.waits(1000);

        // test is this the video seen
        assertEquals("Video didn't change to remote one",
            remoteVideoSrc, getLargeVideoSource(driver));
    }

    /**
     * Click on local video thumbnail and checks whether the large video
     * is the local one.
     */
    public void participantClickOnLocalVideoAndTest()
    {
        clickOnLocalVideoAndTest(ConferenceFixture.getFocus());
    }

    /**
     * Clicks on the remote video thumbnail and checks whether the large video
     * is the remote one.
     */
    public void participantClickOnRemoteVideoAndTest()
    {
        clickOnRemoteVideoAndTest(ConferenceFixture.getSecondParticipant());
    }

}
