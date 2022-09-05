/*
 * Copyright @ 2018 8x8 Pty Ltd
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
package com.bipmeet.test;

import org.jitsi.meet.test.KickTest;
import org.jitsi.meet.test.base.JitsiMeetUrl;
import org.jitsi.meet.test.web.WebParticipant;
import org.jitsi.meet.test.web.WebTestBase;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.testng.SkipException;
import org.testng.annotations.Test;

import java.util.logging.Level;
import java.util.logging.Logger;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Tests the kick functionality.
 * This test validates moderated user kick functions and overrides #KickTest class
 * * The tests needs few configs
 *  *  - server url
 *  *  - room name
 *  *  - a jwt token to connect as moderator
 * @author Damian Minkov
 */
public class BMKickTest extends KickTest
{

    public static final String MODERATOR_TOKEN_PNAME = "org.jitsi.moderated.room.token";


    @Override
    public void setupClass()
    {
        String token = getStringConfigValue(MODERATOR_TOKEN_PNAME);

        if ( token == null || token.trim().length() == 0)
        {
            throw new SkipException("missing configurations");
        }

        JitsiMeetUrl url = getJitsiMeetUrl();

        ensureTwoParticipants(url.copy().setRoomParameters("jwt=" + token), url);

        assertTrue(getParticipant1().isModerator(), "Participant 1 must be moderator");
        assertFalse(getParticipant2().isModerator(), "Participant 2 must not be moderator");
    }



    /**
     * Kick participant2 and checks at participant1 is this is visible.
     * and whether participant2 sees a notification that was kicked.
     */

    /**click added.
     * bipMeet functions added.
     */

    @Test
    public void kickParticipant2AndCheck()
    {
        WebParticipant participant1 = getParticipant1();
        WebParticipant participant2 = getParticipant2();

        WebDriver driver = participant1.getDriver();

        WebElement thumbnail = driver.findElement(By.xpath("//span[@id='participant_"
                + participant2.getEndpointId() + "']"));
        Actions action1 = new Actions(driver);
        action1.moveToElement(thumbnail);
        action1.click();
        action1.perform();

        WebElement menuElement = driver.findElement(By.xpath(
                "//span[@id='participant_" + participant2.getEndpointId()
                        + "']//span[@id='remotevideomenu']"));

        Actions action = new Actions(driver);
        action.moveToElement(menuElement);
        action.click();
        action.perform();

        if (participant1.isModerator())
        {
            participant1.getRemoteParticipants().get(0).kick();

            participant1.waitForParticipants(0);

            // check that the kicked participant sees the notification
            assertTrue(
                participant2.getNotifications().bipmeetHasKickedNotification(),
                "The second participant should see a warning that was kicked.");
        }
        else
        {
            Logger.getGlobal().log(Level.WARNING, "Not testing kick as torture is not moderator.");
            throw new SkipException("skip as test's participant cannot be moderator");
        }
    }

    /**
     * Executes the kick test kickParticipant2AndCheck in p2p mode.
     */
    @Test(dependsOnMethods = {"kickParticipant2AndCheck"})
    public void kickP2PParticipant2AndCheck()
    {
        String token = getStringConfigValue(MODERATOR_TOKEN_PNAME);
        hangUpAllParticipants();

        JitsiMeetUrl url
                = getJitsiMeetUrl().appendConfig("config.p2p.enabled=true");

        ensureTwoParticipants(url.copy().setRoomParameters("jwt=" + token), url);

        kickParticipant2AndCheck();
    }
}
