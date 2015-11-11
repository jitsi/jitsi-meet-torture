/*
 * Copyright @ 2015 Atlassian Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jitsi.meet.test;
import junit.framework.*;

import org.openqa.selenium.*;

/**
 * Tests the video layout by checking its width and height compared to the
 * window width and height. This is meant to prove that video is correctly
 * aligned and sized at the beginning of the call.
 *
 * TODO: We may add additional checks asserting video is the correct size,
 * when the chat is opened or after a desktop sharing session.
 *
 * @author Yana Stamcheva
 */
public class VideoLayoutTest
    extends TestCase
{
    /**
     * Tests the video layout. This is meant to prove that video is correctly
     * aligned and sized at the beginning of the call.
     *
     * TODO: Add tests which turn on/off screen sharing and then check if
     * the video would fit back to the screen.
     */
    public void testVideoLayout()
    {
        System.err.println("Start testVideoLayout.");

        WebDriver owner = ConferenceFixture.getOwner();
        driverVideoLayoutTest(owner);
    }

    /**
     * The webdriver to test.
     * @param webDriver to test.
     */
    public void driverVideoLayoutTest(WebDriver webDriver)
    {
        String chatXPath = "//div[@id='chatspace']";
        String contactListXPath = "//div[@id='contactlist']";
        String settingsXPath = "//div[@id='settingsmenu']";

        WebElement chatElem = webDriver.findElement(By.xpath(chatXPath));
        WebElement contactListElem
            = webDriver.findElement(By.xpath(contactListXPath));
        WebElement settingsElem
            = webDriver.findElement(By.xpath(settingsXPath));

        if (!chatElem.isDisplayed()
                && !contactListElem.isDisplayed()
                && !settingsElem.isDisplayed())
        {
            doLargeVideoSizeCheck(webDriver);
        }
    }

    /**
     * Checks if the video container fits the inner window width and height.
     *
     * @param webDriver <tt>WebDriver</tt> instance of the participant for whom
     *                  we'll try to check the video size
     */
    private void doLargeVideoSizeCheck(WebDriver webDriver)
    {
        Long innerWidth = (Long)((JavascriptExecutor) webDriver)
                .executeScript("return window.innerWidth;");

        Long innerHeight = (Long)((JavascriptExecutor) webDriver)
                .executeScript("return window.innerHeight;");

        WebElement largeVideo = webDriver.findElement(
                By.xpath("//div[@id='largeVideoContainer']"));

        assertEquals(largeVideo.getSize().getWidth(), innerWidth.intValue());
        assertEquals(largeVideo.getSize().getHeight(), innerHeight.intValue());

        // now let's check whether the video wrapper take all the height
        // this should not be the case only for desktop sharing with thumbs
        // visible
        WebElement largeVideoWrapper = webDriver.findElement(
            By.xpath("//div[@id='largeVideoWrapper']"));

        assertEquals(largeVideoWrapper.getSize().getHeight(),
            innerHeight.intValue());
    }
}