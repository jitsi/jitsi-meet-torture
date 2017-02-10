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
 * @author Yana Stamcheva
 */
public class VideoLayoutTest
    extends TestCase
{
    /**
     * Tests the video layout. This is meant to prove that video is correctly
     * aligned and sized at the beginning of the call.
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
        doLargeVideoSizeCheck(webDriver, false);
    }

    /**
     * Checks if the video container fits the inner window width and height.
     *
     * @param webDriver <tt>WebDriver</tt> instance of the participant for whom
     *                  we'll try to check the video size
     * @param  isDesktopSharing Whether the large video is showing
     *                          desktop stream(<tt>true</tt>) or
     *                          camera stream (<tt>false</tt>)
     */
    public void doLargeVideoSizeCheck(WebDriver webDriver,
                                      boolean isDesktopSharing)
    {
        int innerWidth = ((Long)((JavascriptExecutor) webDriver)
                .executeScript("return window.innerWidth;")).intValue();

        int innerHeight = ((Long)((JavascriptExecutor) webDriver)
                .executeScript("return window.innerHeight;")).intValue();

        WebElement largeVideo = webDriver.findElement(
                By.xpath("//div[@id='largeVideoWrapper']"));
        int largeVideoH = largeVideo.getSize().getHeight(),
            largeVideoW = largeVideo.getSize().getWidth();
        if(isDesktopSharing) {
            int filmstripH = 0;

            WebElement filmstrip = webDriver.findElement(
                    By.xpath("//div[@class='filmstrip']"));
            WebElement remoteVideos = webDriver.findElement(
                By.xpath("//div[@id='remoteVideos']"));
            boolean isFilmstripDisplayed = remoteVideos.isDisplayed();
            if (isFilmstripDisplayed) {
                filmstripH = filmstrip.getSize().getHeight();
            }

            int totalHeight = largeVideoH + filmstripH;
            System.err.println("innerHeight=" + innerHeight
                + "; innerWidth=" + innerWidth + "; largeVideoW="
                + largeVideoW + "; largeVideoH=" + largeVideoH
                + "; filmstripH=" + filmstripH);
            assertTrue("The size of the large video is incorrect",
                (totalHeight == innerHeight)
                || (largeVideoW == innerWidth && totalHeight < innerHeight));
        } else {
            System.err.println("innerHeight=" + innerHeight
                + "; innerWidth=" + innerWidth + "; largeVideoW="
                + largeVideoW + "; largeVideoH=" + largeVideoH);
            assertEquals(largeVideoH, innerHeight);
            assertEquals(largeVideoW, innerWidth);
        }
    }
}