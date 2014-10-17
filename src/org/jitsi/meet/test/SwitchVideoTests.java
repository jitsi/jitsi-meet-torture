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

        suite.addTest(new SwitchVideoTests("focusClickOnLocalVideoAndTest"));
        suite.addTest(new SwitchVideoTests("focusClickOnRemoteVideoAndTest"));

        return suite;
    }

    /**
     * Click on local video thumbnail and checks whether the large video
     * is the local one.
     */
    public void focusClickOnLocalVideoAndTest()
    {
        // click on local
        String localVideoSrc = ConferenceFixture.getFocus().findElement(
                By.xpath("//span[@id='localVideoWrapper']/video"))
            .getAttribute("src");

        ConferenceFixture.getFocus().findElement(
            By.id("localVideoWrapper")).click();

        TestUtils.waits(1000);

        // test is this the video seen
        assertEquals("Video didn't change to local",
            localVideoSrc, getLargeVideoSource());
    }

    /**
     * Returns the source of the large video currently shown.
     * @return the source of the large video currently shown.
     */
    private String getLargeVideoSource()
    {
        return ConferenceFixture.getFocus().findElement(By.xpath(
            "//div[@id='largeVideoContainer']/video[@id='largeVideo']"))
                .getAttribute("src");
    }

    /**
     * Clicks on the remote video thumbnail and checks whether the large video
     * is the remote one.
     */
    public void focusClickOnRemoteVideoAndTest()
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
            ConferenceFixture.getFocus(),
            remoteThumbVideoXpath,
            5000
        );

        WebElement remoteThumb = ConferenceFixture.getFocus()
            .findElement(By.xpath(remoteThumbVideoXpath));

        assertNotNull("Remote video not found", remoteThumb);

        String remoteVideoSrc = remoteThumb.getAttribute("src");

        // click on remote
        ConferenceFixture.getFocus().findElement(By.xpath(remoteThumbXpath))
            .click();

        TestUtils.waits(1000);

        // test is this the video seen
        assertEquals("Video didn't change to remote one",
            remoteVideoSrc, getLargeVideoSource());
    }
}
