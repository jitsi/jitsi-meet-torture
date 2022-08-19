package com.bipmeet.test;

import org.jitsi.meet.test.ModeratedRoomsTest;
import org.jitsi.meet.test.base.JitsiMeetUrl;
import org.jitsi.meet.test.util.TestUtils;
import org.jitsi.meet.test.web.WebParticipant;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.testng.SkipException;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * This test validates moderated user menus and overrides #ModeratedRoomsTest class
 * The tests needs few configs
 *  - server url
 *  - room name
 *  - a jwt token to connect as moderator
 *  TODO: Add lock room, change pwd etc.
 */
public class BMModeratedRoomTest extends ModeratedRoomsTest {

    /**
     * Overrides #{@link ModeratedRoomsTest#testModeratedTenant}
     *
     * Changes:
     *  - tenant for moderator is not applicable for bip-meet
     *  - token is retrieved by getStringConfigValue method
     */
    @Override
    public void testModeratedTenant() {

        String token = getStringConfigValue(MODERATOR_TOKEN_PNAME);

        if ( token == null || token.trim().length() == 0)
        {
            throw new SkipException("missing configurations");
        }

        JitsiMeetUrl url = getJitsiMeetUrl();

        ensureTwoParticipants(url.copy().setRoomParameters("jwt=" + token), url);

        assertTrue(getParticipant1().isModerator(), "Participant 1 must be moderator");
        assertFalse(getParticipant2().isModerator(), "Participant 2 must not be moderator");

        checkModeratorMenuItems(getParticipant1(), getParticipant2());
        checkModeratorMenuItems(getParticipant2(), getParticipant1());
    }

    /**
     * Overrides #{@link ModeratedRoomsTest#checkModeratorMenuItems}
     *
     * Changes:
     *  - click added remote user controls menu button
     *  - mute control is removed from guest user, because guest users can mute remote users on bip-meet
     */
    public static void checkModeratorMenuItems(WebParticipant localParticipant, WebParticipant remoteParticipant)
    {
        WebDriver driver = localParticipant.getDriver();

        // Open the remote video menu for that participant
        WebElement thumbnail = driver.findElement(By.xpath("//span[@id='participant_"
                + remoteParticipant.getEndpointId() + "']"));
        Actions action1 = new Actions(driver);
        action1.moveToElement(thumbnail);
        action1.click();
        action1.perform();

        WebElement menuElement = driver.findElement(By.xpath(
                "//span[@id='participant_" + remoteParticipant.getEndpointId()
                        + "']//span[@id='remotevideomenu']"));

        Actions action = new Actions(driver);
        action.moveToElement(menuElement);
        action.click();
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
        }
    }
}
