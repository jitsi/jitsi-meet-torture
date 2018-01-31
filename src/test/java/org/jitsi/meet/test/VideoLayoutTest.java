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

import org.jitsi.meet.test.base.*;
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
    public void setup()
    {
        super.setup();

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
        driverVideoLayoutTest(getParticipant1());
    }

    /**
     * The webdriver to test.
     * @param participant to test.
     */
    void driverVideoLayoutTest(Participant participant)
    {
        WebDriver driver = participant.getDriver();
        String chatXPath = "//div[@id='chat_container']";
        String contactListXPath = "//div[@id='contacts_container']";
        String settingsXPath = "//div[@id='settings_container']";

        WebElement chatElem = driver.findElement(By.xpath(chatXPath));
        WebElement contactListElem
            = driver.findElement(By.xpath(contactListXPath));
        WebElement settingsElem
            = driver.findElement(By.xpath(settingsXPath));

        if (!chatElem.isDisplayed()
                && !contactListElem.isDisplayed()
                && !settingsElem.isDisplayed())
        {
            doLargeVideoSizeCheck(participant);
        }
    }

    /**
     * Checks if the video container fits the inner window width and height.
     *
     * @param participant <tt>WebDriver</tt> instance of the participant for whom
     *                  we'll try to check the video size
     */
    private void doLargeVideoSizeCheck(Participant participant)
    {
        Long innerWidth
            = (Long) participant.executeScript("return window.innerWidth;");

        Long innerHeight
            = (Long) participant.executeScript("return window.innerHeight;");

        WebElement largeVideo
            = participant.getDriver().findElement(
                By.xpath("//div[@id='largeVideoContainer']"));

        assertEquals(largeVideo.getSize().getWidth(), innerWidth.intValue());
        assertEquals(largeVideo.getSize().getHeight(), innerHeight.intValue());

        // now let's check whether the video wrapper take all the height
        // this should not be the case only for desktop sharing with thumbs
        // visible
        WebElement largeVideoWrapper
            = participant.getDriver().findElement(
                By.xpath("//div[@id='largeVideoWrapper']"));

        assertEquals(largeVideoWrapper.getSize().getHeight(),
            innerHeight.intValue());
    }
}