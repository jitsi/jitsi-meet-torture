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
package com.bipmeet.test;

import org.jitsi.meet.test.LockRoomTest;
import org.jitsi.meet.test.base.JitsiMeetUrl;
import org.jitsi.meet.test.pageobjects.web.ModalDialogHelper;
import org.jitsi.meet.test.pageobjects.web.SecurityDialog;
import org.jitsi.meet.test.util.TestUtils;
import org.jitsi.meet.test.web.WebParticipant;
import org.jitsi.meet.test.web.WebTestBase;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.testng.SkipException;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * 1. Lock the room (make sure the image changes to locked)
 * 2. Join with a second browser/tab
 * 3. Make sure we are required to enter a password.
 * (Also make sure the padlock is locked)
 * 4. Enter wrong password, make sure we are not joined in the room
 * 5. Unlock the room (Make sure the padlock is unlocked)
 * 6. Join again and make sure we are not asked for a password and that
 * the padlock is unlocked.
 *
 * @author Damian Minkov
 * @author Leonard Kim
 */
public class BMLockRoomTest extends LockRoomTest
{
    public static String ROOM_KEY = null;
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

        ensureOneParticipant(url.copy().setRoomParameters("jwt=" + token));
    }

    /**
     * Stops the participant. And locks the room from participant1.
     */
    @Test
    public void lockRoom()
    {
        // just in case wait
        TestUtils.waitMillis(1000);

        participant1LockRoom();
    }

    /**
     * participant1 locks the room.
     */
    private void participant1LockRoom()
    {
        WebParticipant participant1 = getParticipant1();
        WebDriver driver = participant1.getDriver();

        WebElement passwordIcon = driver.findElement
                (By.xpath("//div[contains(@data-testid,'Meeting Password--container')]"));
        passwordIcon.click();

        participantSetPasswordForBipMeet();

        TestUtils.waitForDisplayedElementByXPath(driver, "//div[@aria-label='Meeting Password' and @aria-pressed='LOCKED_LOCALLY']", 3);

    }
    private void participantSetPasswordForBipMeet(){

        WebParticipant participant1 = getParticipant1();
        WebDriver driver = participant1.getDriver();
        List<WebElement> passwordBoxes = driver.findElements(By.xpath("//input[contains(@name,'enteredValue')]"));
        WebElement box;
        StringBuilder sb = new StringBuilder();

        for(int i = 0 ; i<passwordBoxes.size(); i++){
            int randomPw = (int) (Math.random() * 10);
            String pw = String.valueOf(randomPw);
            box = passwordBoxes.get(i);
            box.sendKeys(""+pw);
            
            sb.append(pw);
        }
        ROOM_KEY = sb.toString();

        WebElement updateButton = driver.findElement(By.id("modal-dialog-ok-button"));
        updateButton.click();
    }

    /**
     * first wrong pin then correct one
     */
    @Test(dependsOnMethods = { "lockRoom" })
    public void enterParticipantInLockedRoom()
    {
        WebParticipant participant1 = getParticipant1();
        SecurityDialog securityDialog1 = participant1.getSecurityDialog();
        securityDialog1.open();
        assertTrue(securityDialog1.isLocked());

        joinSecondParticipant();

        WebParticipant participant2 = getParticipant2();
        WebDriver driver2 = participant2.getDriver();

        // wait for password prompt
        waitForPasswordDialog(driver2);

        submitPassword(driver2, ROOM_KEY + "1234");

        // wait for password prompt
        waitForPasswordDialog(driver2);

        submitPassword(driver2, ROOM_KEY);

        participant2.waitToJoinMUC(5);

        SecurityDialog securityDialog2 = participant2.getSecurityDialog();
        securityDialog2.open();
        assertTrue(securityDialog2.isLocked());
    }

    /**
     * Unlock room. Check whether room is still locked. Click remove and check
     * whether it is unlocked.
     */
    @Test(dependsOnMethods = { "enterParticipantInLockedRoom" })
    public void unlockRoom()
    {
        getParticipant2().hangUp();

        // just in case wait
        TestUtils.waitMillis(1000);

        WebParticipant participant1 = getParticipant1();
        SecurityDialog securityDialog = participant1.getSecurityDialog();
        securityDialog.removePassword();
    }

    /**
     * participant1
     * unlocks the room.
     */
    private void participant1UnlockRoom()
    {
        WebParticipant participant1 = getParticipant1();
        SecurityDialog securityDialog = participant1.getSecurityDialog();
        securityDialog.open();
        securityDialog.removePassword();

        // just in case wait
        TestUtils.waitMillis(2000);

        assertFalse(securityDialog.isLocked());
    }

    /**
     * Just enter the room and check that is not locked.
     */
    @Test(dependsOnMethods = { "unlockRoom" })
    public void enterParticipantInUnlockedRoom()
    {
        // if we fail to unlock the room this one will detect it
        // as participant will fail joining
        ensureTwoParticipants();

        WebParticipant participant2 = getParticipant2();
        SecurityDialog securityDialog = participant2.getSecurityDialog();
        securityDialog.open();

        assertFalse(securityDialog.isLocked());
    }

    /**
     * Both participants are in unlocked room, lock it and see whether the
     * change is reflected on the second participant icon.
     */
    @Test(dependsOnMethods = { "enterParticipantInUnlockedRoom" })
    public void updateLockedStateWhileParticipantInRoom()
    {
        participant1LockRoom();

        WebParticipant participant = getParticipant2();
        SecurityDialog securityDialog = participant.getSecurityDialog();
        securityDialog.open();
        assertTrue(securityDialog.isLocked());

        participant1UnlockRoom();

        assertFalse(securityDialog.isLocked());
    }

    /**
     * participant1 locks the room. Participant tries to enter using wrong
     * password. participant1 unlocks the room and Participant submits the
     * password prompt with no password entered and he should enter of unlocked
     * room.
     */
    @Test(dependsOnMethods = { "updateLockedStateWhileParticipantInRoom" })
    public void unlockAfterParticipantEnterWrongPassword()
    {
        getParticipant2().hangUp();

        // just in case wait
        TestUtils.waitMillis(1000);

        participant1LockRoom();

        joinSecondParticipant();

        WebParticipant participant2 = getParticipant2();
        WebDriver driver2 = participant2.getDriver();

        // wait for password prompt
        waitForPasswordDialog(driver2);

        submitPassword(driver2, ROOM_KEY + "1234");

        // wait for password prompt
        waitForPasswordDialog(driver2);

        participant1UnlockRoom();

        // just in case wait
        TestUtils.waitMillis(500);

        ModalDialogHelper.clickOKButton(driver2);

        participant2.waitToJoinMUC(5);

        SecurityDialog securityDialog = participant2.getSecurityDialog();
        securityDialog.open();
        assertFalse(securityDialog.isLocked());
    }

    /**
     * Interacts with the password modal to enter and submit a password.
     *
     * @param driver the participant that should be used to interact with the
     * password modal
     * @param password the password to enter and submit
     */
    public static void submitPassword(WebDriver driver, String password)
    {
        waitForPasswordDialog(driver);

        WebElement passwordInput = driver.findElement(
            By.xpath("//input[@name='lockKey']"));

        passwordInput.clear();
        passwordInput.sendKeys(password);

        ModalDialogHelper.clickOKButton(driver);
    }

    /**
     * Waits till the password dialog is shown.
     * @param driver the participant that should be used to interact with the
     * password modal
     */
    public static void waitForPasswordDialog(WebDriver driver)
    {
        TestUtils.waitForElementBy(
            driver,
            By.xpath("//input[@name='lockKey']"),
            5);
    }
}
