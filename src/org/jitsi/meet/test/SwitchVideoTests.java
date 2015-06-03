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
            "ownerClickOnLocalVideoAndTest"));
        suite.addTest(new SwitchVideoTests(
            "ownerClickOnRemoteVideoAndTest"));
        suite.addTest(new SwitchVideoTests(
            "participantClickOnLocalVideoAndTest"));
        suite.addTest(new SwitchVideoTests(
            "participantClickOnRemoteVideoAndTest"));

        return suite;
    }

    /**
     * Click on local video thumbnail and checks whether the large video
     * is the local one.
     */
    public void ownerClickOnLocalVideoAndTest()
    {
        clickOnLocalVideoAndTest(ConferenceFixture.getOwner());
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

        driver.findElement(By.className("focusindicator")).click();

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
    public void ownerClickOnRemoteVideoAndTest()
    {
        clickOnRemoteVideoAndTest(ConferenceFixture.getOwner());
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

        TestUtils.waitsForElementByXPath(
            driver,
            remoteThumbVideoXpath,
            5
        );

        WebElement remoteThumb = driver
            .findElement(By.xpath(remoteThumbVideoXpath));

        assertNotNull("Remote video not found", remoteThumb);

        // click on remote
        driver.findElement(By.xpath(remoteThumbXpath))
            .click();

        TestUtils.waits(1000);

        // Obtain the remote video src *after* we have clicked the thumbnail
        // and have waited. With simulcast enabled, the remote stream may
        // change.
        String remoteVideoSrc = remoteThumb.getAttribute("src");

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
        clickOnLocalVideoAndTest(ConferenceFixture.getOwner());
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
