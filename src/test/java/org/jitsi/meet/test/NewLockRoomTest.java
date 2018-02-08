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

import org.jitsi.meet.test.pageobjects.web.InfoDialog;
import org.jitsi.meet.test.web.WebParticipant;
import org.openqa.selenium.*;
import org.testng.annotations.*;

import static org.testng.Assert.*;

/**
 * ALERT: This is a copy of LockRoomTest. NewLockRoomTest exists while
 * transitioning the lock feature from the invite modal to the info dialog.
 */

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
public class NewLockRoomTest
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
        ROOM_KEY = String.valueOf((int)(Math.random()*1000000));

        WebParticipant participant = (WebParticipant) getParticipant1();
        InfoDialog infoDialog = participant.getInfoDialog();
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
        WebParticipant ownerParticipant = (WebParticipant) getParticipant1();
        InfoDialog ownerInfoDialog = ownerParticipant.getInfoDialog();
        ownerInfoDialog.open();
        assertTrue(ownerInfoDialog.isLocked());

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


        WebParticipant secondWebParticipant
            = (WebParticipant) getParticipant2();
        InfoDialog secondInfoDialog = secondWebParticipant.getInfoDialog();
        secondInfoDialog.open();
        assertTrue(secondInfoDialog.isLocked());
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

        WebParticipant ownerParticipant = (WebParticipant) getParticipant1();
        InfoDialog infoDialog = ownerParticipant.getInfoDialog();
        infoDialog.removePassword();
    }

    /**
     * Owner unlocks the room.
     */
    private void ownerUnlockRoom()
    {
        WebParticipant participant = (WebParticipant) getParticipant1();
        InfoDialog infoDialog = participant.getInfoDialog();
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

        WebParticipant participant = (WebParticipant) getParticipant2();
        InfoDialog infoDialog = participant.getInfoDialog();
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
        ownerLockRoom();

        WebParticipant participant = (WebParticipant) getParticipant2();
        InfoDialog infoDialog = participant.getInfoDialog();
        infoDialog.open();
        assertTrue(infoDialog.isLocked());

        ownerUnlockRoom();

        assertFalse(infoDialog.isLocked());
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

        WebParticipant participant = (WebParticipant) getParticipant2();
        InfoDialog infoDialog = participant.getInfoDialog();
        infoDialog.open();
        assertFalse(infoDialog.isLocked());
    }
}
