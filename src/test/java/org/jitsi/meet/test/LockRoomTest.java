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

import org.jitsi.meet.test.util.*;

import org.jitsi.meet.test.pageobjects.web.InfoDialog;
import org.jitsi.meet.test.web.*;
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
 * @author Leonard Kim
 */
public class LockRoomTest
    extends WebTestBase
{
    public static String ROOM_KEY = null;

    @Override
    public void setupClass()
    {
        super.setupClass();

        ensureOneParticipant();
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
        ROOM_KEY = String.valueOf((int) (Math.random() * 1_000_000));

        WebParticipant participant1 = getParticipant1();
        InfoDialog infoDialog = participant1.getInfoDialog();
        infoDialog.open();

        assertFalse(infoDialog.isLocked());

        infoDialog.addPassword(ROOM_KEY);
        TestUtils.waitMillis(1000);
        infoDialog.close();
        infoDialog.open();

        assertTrue(infoDialog.isLocked());
    }

    /**
     * first wrong pin then correct one
     */
    @Test(dependsOnMethods = { "lockRoom" })
    public void enterParticipantInLockedRoom()
    {
        WebParticipant participant1 = getParticipant1();
        InfoDialog infoDialog1 = participant1.getInfoDialog();
        infoDialog1.open();
        assertTrue(infoDialog1.isLocked());

        try
        {
            ensureTwoParticipants();

            fail("participant2 must not be able to join the room.");
        }
        catch(TimeoutException e)
        {}

        WebParticipant participant2 = getParticipant2();
        WebDriver driver2 = participant2.getDriver();

        driver2.findElement(
            By.xpath("//input[@name='lockKey']")).sendKeys(ROOM_KEY + "1234");
        driver2.findElement(By.id("modal-dialog-ok-button")).click();

        try
        {
            participant2.waitToJoinMUC(5);
            fail("participant2 must not be able to join the room.");
        }
        catch(TimeoutException e)
        {}

        driver2.findElement(
            By.xpath("//input[@name='lockKey']")).sendKeys(ROOM_KEY);
        driver2.findElement(By.id("modal-dialog-ok-button")).click();

        participant2.waitToJoinMUC(5);

        InfoDialog infoDialog2 = participant2.getInfoDialog();
        infoDialog2.open();
        assertTrue(infoDialog2.isLocked());
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
        InfoDialog infoDialog = participant1.getInfoDialog();
        infoDialog.removePassword();
    }

    /**
     * participant1
     * unlocks the room.
     */
    private void participant1UnlockRoom()
    {
        WebParticipant participant1 = getParticipant1();
        InfoDialog infoDialog = participant1.getInfoDialog();
        infoDialog.open();
        infoDialog.removePassword();

        // just in case wait
        TestUtils.waitMillis(1000);

        assertFalse(infoDialog.isLocked());
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
        InfoDialog infoDialog = participant2.getInfoDialog();
        infoDialog.open();

        assertFalse(infoDialog.isLocked());
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
        InfoDialog infoDialog = participant.getInfoDialog();
        infoDialog.open();
        assertTrue(infoDialog.isLocked());

        participant1UnlockRoom();

        assertFalse(infoDialog.isLocked());
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

        try
        {
            ensureTwoParticipants();

            fail("participant2 must not be able to join the room.");
        }
        catch(TimeoutException e)
        {}

        WebParticipant participant2 = getParticipant2();
        WebDriver driver2 = participant2.getDriver();

        driver2.findElement(
            By.xpath("//input[@name='lockKey']")).sendKeys(ROOM_KEY + "1234");
        driver2.findElement(By.id("modal-dialog-ok-button")).click();

        try
        {
            participant2.waitToJoinMUC(5);

            fail("participant2 must not be able to join the room.");
        }
        catch(TimeoutException e)
        {}

        participant1UnlockRoom();

        // just in case wait
        TestUtils.waitMillis(500);

        driver2.findElement(By.id("modal-dialog-ok-button")).click();

        participant2.waitToJoinMUC(5);

        InfoDialog infoDialog = participant2.getInfoDialog();
        infoDialog.open();
        assertFalse(infoDialog.isLocked());
    }
}