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

import org.jitsi.meet.test.web.*;

import org.openqa.selenium.*;
import org.testng.annotations.*;

import static org.testng.Assert.*;

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
    extends WebTestBase
{
    @Override
    public void setupClass()
    {
        super.setupClass();

        ensureOneParticipant();
    }

    /**
     * Tests the video layout. This is meant to prove that video is correctly
     * aligned and sized at the beginning of the call.
     *
     * TODO: Add tests which turn on/off screen sharing and then check if
     * the video would fit back to the screen.
     */
    @Test
    public void testVideoLayout()
    {
        driverVideoLayoutTest(getParticipant1(), false);
    }

    /**
     * TODO DOCUMENT
     * @param participant TODO DOCUMENT
     * @param isScreenSharing <tt>true</tt> if SS is started and <tt>false</tt>
     * otherwise.
     */
    void driverVideoLayoutTest(
            WebParticipant participant, boolean isScreenSharing)
    {
        doLargeVideoSizeCheck(participant, isScreenSharing);
    }

    /**
     * Checks if the video container fits the inner window width and height.
     *
     * @param participant The the participant for whom we'll try to check the
     * video size.
     * @param isScreenSharing <tt>true</tt> if SS is started and <tt>false</tt>
     * otherwise.
     */
    private void doLargeVideoSizeCheck(
        WebParticipant participant,
        boolean isScreenSharing)
    {
        Integer innerWidth
            = ((Long) participant.executeScript("return window.innerWidth;"))
                .intValue();
        Integer innerHeight
            = ((Long) participant.executeScript("return window.innerHeight;"))
                .intValue();

        WebElement largeVideo
            = participant.getDriver().findElement(
                By.xpath("//div[@id='largeVideoContainer']"));

        String filmstripXPath = "//div[contains(@class, 'filmstrip')]";
        WebElement filmstrip
            = participant.getDriver().findElement(By.xpath(filmstripXPath));
        int filmstripWidth
            = (filmstrip == null
                || !filmstrip.isDisplayed()
                || !isScreenSharing)
                    ? 0 : filmstrip.getSize().getWidth();


        assertTrue(
            (largeVideo.getSize().getWidth() == innerWidth - filmstripWidth)
                || (largeVideo.getSize().getHeight() == innerHeight),
            "TODO: add a description of what is expected");
    }
}
