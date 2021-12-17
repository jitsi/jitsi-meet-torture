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
package org.jitsi.meet.test;

import org.jitsi.meet.test.base.*;
import org.jitsi.meet.test.util.*;
import org.jitsi.meet.test.web.*;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.*;
import org.testng.*;
import org.testng.annotations.*;

import static org.testng.Assert.*;

/**
 * This test validates moderated_subdomains from
 * https://github.com/jitsi/jitsi-meet/blob/master/resources/prosody-plugins/mod_muc_allowners.lua#L11.
 * The tests needs few configs
 *  - the tenant configured on the deployment which will be test
 *  - a jwt token to connect as moderator
 */
public class ModeratedRoomsTest
    extends WebTestBase
{
    public static final String TENANT_NAME_PNAME = "org.jitsi.moderated.room.tenant_name";
    public static final String MODERATOR_TOKEN_PNAME = "org.jitsi.moderated.room.token";

    /**
     * Two participants join, first one with token as moderator and second one as guest.
     * For now we just check the remote participant context menu for kick and mute button.
     */
    @Test
    public void testModeratedTenant()
    {
        String tenantName = System.getProperty(TENANT_NAME_PNAME);
        String token = System.getProperty(MODERATOR_TOKEN_PNAME);

        if (tenantName == null || tenantName.trim().length() == 0
            || token == null || token.trim().length() == 0)
        {
            throw new SkipException("missing configurations");
        }

        JitsiMeetUrl url = getJitsiMeetUrl();
        url.setRoomName(tenantName + '/' + url.getRoomName());

        ensureTwoParticipants(url.copy().setRoomParameters("jwt=" + token), url);

        assertTrue(getParticipant1().isModerator(), "Participant 1 must be moderator");
        assertFalse(getParticipant2().isModerator(), "Participant 2 must not be moderator");

        checkModeratorMenuItems(getParticipant1(), getParticipant2());
        checkModeratorMenuItems(getParticipant2(), getParticipant1());
    }

    /**
     * Checks whether 'mute' and 'kick' items are available or not in remote participant menu, based on whether it is
     * moderator.
     *
     * @param localParticipant The local participant where checks will be performed.
     * @param remoteParticipant The remote participant which menu will be checked.
     */
    public static void checkModeratorMenuItems(WebParticipant localParticipant, WebParticipant remoteParticipant)
    {
        WebDriver driver = localParticipant.getDriver();

        // Open the remote video menu for that participant
        WebElement thumbnail = driver.findElement(By.xpath("//span[@id='participant_"
                + remoteParticipant.getEndpointId() + "']"));
        Actions action1 = new Actions(driver);
        action1.moveToElement(thumbnail);
        action1.perform();

        WebElement menuElement = driver.findElement(By.xpath(
            "//span[@id='participant_" + remoteParticipant.getEndpointId()
                + "']//span[@id='remotevideomenu']"));

        Actions action = new Actions(driver);
        action.moveToElement(menuElement);
        action.perform();

        if (localParticipant.isModerator())
        {
            // give time for the menu to appear
            TestUtils.waitForDisplayedElementByXPath(driver,
                "//div[@class='popover']//div[contains(@class, 'kicklink')]",
                2);
            TestUtils.waitForDisplayedElementByXPath(driver,
                "//div[@class='popover']//div[contains(@class, 'mutelink')]",
                2);
        }
        else
        {
            TestUtils.waitForNotDisplayedElementByXPath(driver,
                "//div[@class='popover']//div[contains(@class, 'kicklink')]",
                2);
            TestUtils.waitForNotDisplayedElementByXPath(driver,
                "//div[@class='popover']//div[contains(@class, 'mutelink')]",
                1);
        }
    }
}
