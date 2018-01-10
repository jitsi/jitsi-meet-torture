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
import org.jitsi.meet.test.util.*;

import org.openqa.selenium.*;
import org.testng.annotations.*;

import static org.testng.Assert.*;

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
 */
public class LockRoomTest
    extends AbstractBaseTest
{
    public static String ROOM_KEY = null;

    @Override
    public void setup()
    {
        super.setup();

        ensureOneParticipant();
    }

    /**
     * Stops the participant. And locks the room from the owner.
     */
    @Test
    public void lockRoom()
    {
        // just in case wait
        TestUtils.waitMillis(1000);

        ownerLockRoom();
    }

    /**
     * Owner locks the room.
     */
    private void ownerLockRoom()
    {
        WebDriver owner = getParticipant1().getDriver();
        testRoomIsUnlocked(owner);

        MeetUIUtils.clickOnToolbarButton(owner, "toolbar_button_link");

        // fill in the dialog
        togglePasswordEdit(owner);

        String inputXPath = "//input[@id='newPasswordInput']";
        TestUtils.waitForElementByXPath(owner, inputXPath, 5);
        ROOM_KEY = String.valueOf((int)(Math.random()*1000000));
        owner.findElement(
            By.xpath(inputXPath)).sendKeys(ROOM_KEY);

        owner.findElement(
            By.id("addPasswordBtn")).click();

        TestUtils.waitMillis(1000);

        closeInviteDialog(owner);
        testRoomIsLocked(owner);
    }

    private void togglePasswordEdit(WebDriver user) {
        By addPasswordXPath = By.xpath("//a[contains(@class, 'password-overview-toggle-edit')]");
        TestUtils.waitForElementBy(user, addPasswordXPath, 5);
        user.findElement(addPasswordXPath).click();
    }

    /**
    * Closing invite dialog
    */
    private void closeInviteDialog(WebDriver user)
    {
        String closeXPath = "//button[@id='modal-dialog-ok-button']";
        WebElement closeBtn = user.findElement(By.xpath(closeXPath));
        closeBtn.click();
    }

    /**
    * Checks whether room is locked
    */
    private void testRoomIsLocked(WebDriver user)
    {
        testRoomLockState(user, "is-locked");
    }

    /**
    * Checks whether room is unlocked
    */
    private void testRoomIsUnlocked(WebDriver user)
    {
        testRoomLockState(user, "is-unlocked");
    }

    /**
     * Checks room for particular state.
     * @param user the Driver.
     * @param state the state to test locked/unlocked.
     */
    private void testRoomLockState(WebDriver user, String state)
    {
        MeetUIUtils.clickOnToolbarButton(user, "toolbar_button_link");

        // Add a wait till the dialog loads
        // when we have dial-in option it can change the ui a little
        // when we load numbers
        // loading is done only the first time we open the dialog
        // but here in the tests we have a lot of closing/opening/join/leave
        // of participants so we make it always wait a little
        TestUtils.waitMillis(1200);

        String unlockedXPath = "//div[contains(@class, '" + state + "')]";

        TestUtils.waitForDisplayedElementByXPath(user, unlockedXPath, 5);
        WebElement elem = user.findElement(By.xpath(unlockedXPath));

        assertTrue(elem.isDisplayed(), "Room must be " + state);

        closeInviteDialog(user);
    }

    /**
     * first wrong pin then correct one
     */
    @Test(dependsOnMethods = { "lockRoom" })
    public void enterParticipantInLockedRoom()
    {
        testRoomIsLocked(getParticipant1().getDriver());

        try
        {
            ensureTwoParticipants();

            fail("The second participant must not be able to join the room.");
        }
        catch(TimeoutException e)
        {}

        WebDriver secondParticipant = getParticipant2().getDriver();

        secondParticipant.findElement(
            By.xpath("//input[@name='lockKey']")).sendKeys(ROOM_KEY + "1234");
        secondParticipant.findElement(
            By.id("modal-dialog-ok-button")).click();

        try
        {
            getParticipant2().waitToJoinMUC(5);

            fail("The second participant must not be able to join the room.");
        }
        catch(TimeoutException e)
        {}

        secondParticipant.findElement(
            By.xpath("//input[@name='lockKey']")).sendKeys(ROOM_KEY);
        secondParticipant.findElement(
            By.id("modal-dialog-ok-button")).click();

        getParticipant2().waitToJoinMUC(5);

        testRoomIsLocked(secondParticipant);
    }

    /**
     * Unlock room. Check wheter room is still
     * locked. Click remove and check whether it is unlocked.
     */
    @Test(dependsOnMethods = { "enterParticipantInLockedRoom" })
    public void unlockRoom()
    {
        getParticipant2().hangUp();

        // just in case wait
        TestUtils.waitMillis(1000);

        ownerUnlockRoom();
    }

    /**
     * Owner unlocks the room.
     */
    private void ownerUnlockRoom()
    {
        WebDriver owner = getParticipant1().getDriver();

        MeetUIUtils.clickOnToolbarButton(owner, "toolbar_button_link");

        togglePasswordEdit(owner);

        WebElement removeButton = TestUtils.waitForElementBy(
            owner,
            By.id("inviteDialogRemovePassword"),
            1);
        assertNotNull(removeButton, "Missing remove button");
        removeButton.click();
        closeInviteDialog(owner);

        // Wait for the lock icon to disappear
        testRoomIsUnlocked(owner);
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

        testRoomIsUnlocked(getParticipant2().getDriver());
    }

    /**
     * Both participants are in unlocked room, lock it and see whether the
     * change is reflected on the second participant icon.
     */
    @Test(dependsOnMethods = { "enterParticipantInUnlockedRoom" })
    public void updateLockedStateWhileParticipantInRoom()
    {
        ownerLockRoom();

        WebDriver secondParticipant = getParticipant2().getDriver();
        testRoomIsLocked(secondParticipant);
        ownerUnlockRoom();
        testRoomIsUnlocked(secondParticipant);
    }

    /**
     * Owner locks the room. Participant tries to enter using wrong password.
     * Owner unlocks the room and Participant submits the password prompt with
     * no password entered and he should enter of unlocked room.
     */
    @Test(dependsOnMethods = { "updateLockedStateWhileParticipantInRoom" })
    public void unlockAfterParticipantEnterWrongPassword()
    {
        getParticipant2().hangUp();

        // just in case wait
        TestUtils.waitMillis(1000);

        ownerLockRoom();

        testRoomIsLocked(getParticipant1().getDriver());

        try
        {
            ensureTwoParticipants();

            fail("The second participant must not be able to join the room.");
        }
        catch(TimeoutException e)
        {}

        WebDriver secondParticipant = getParticipant2().getDriver();

        secondParticipant.findElement(
            By.xpath("//input[@name='lockKey']")).sendKeys(ROOM_KEY + "1234");
        secondParticipant.findElement(
            By.id("modal-dialog-ok-button")).click();

        try
        {
            getParticipant2().waitToJoinMUC(5);

            fail("The second participant must not be able to join the room.");
        }
        catch(TimeoutException e)
        {}

        ownerUnlockRoom();

        // just in case wait
        TestUtils.waitMillis(500);

        secondParticipant.findElement(
            By.id("modal-dialog-ok-button")).click();

        getParticipant2().waitToJoinMUC(5);
        testRoomIsUnlocked(secondParticipant);
    }
}
