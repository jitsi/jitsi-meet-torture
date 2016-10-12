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
import org.jitsi.meet.test.util.*;
import org.openqa.selenium.*;

import java.util.*;

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
    extends TestCase
{
    public static String ROOM_KEY = null;

    /**
     * Constructs test.
     * @param name the method name for the test.
     */
    public LockRoomTest(String name)
    {
        super(name);
    }

    /**
     * Orders the tests.
     * @return the suite with order tests.
     */
    public static junit.framework.Test suite()
    {
        TestSuite suite = new TestSuite();

        suite.addTest(new LockRoomTest("lockRoom"));
        suite.addTest(new LockRoomTest("enterParticipantInLockedRoom"));
        suite.addTest(new LockRoomTest("unlockRoom"));// stops participant
        suite.addTest(new LockRoomTest("enterParticipantInUnlockedRoom"));
        suite.addTest(new LockRoomTest(
            "updateLockedStateWhileParticipantInRoom"));
        suite.addTest(new LockRoomTest(
            "unlockAfterParticipantEnterWrongPassword"));

        return suite;
    }

    /**
     * Stops the participant. And locks the room from the owner.
     */
    public void lockRoom()
    {
        System.err.println("Start lockRoom.");

        ConferenceFixture.close(ConferenceFixture.getSecondParticipant());

        // just in case wait
        TestUtils.waitMillis(1000);

        ownerLockRoom();
    }

    /**
     * Owner locks the room.
     */
    private void ownerLockRoom()
    {
        WebDriver owner = ConferenceFixture.getOwner();
        testRoomIsUnlocked(owner);

        MeetUIUtils.clickOnToolbarButton(owner, "toolbar_button_link");

        // fill in the dialog

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

    /**
    * Closing invite dialog
    */
    private void closeInviteDialog(WebDriver user)
    {
      String closeXPath = "//div[contains(@class, 'jqiclose')]";
      WebElement closeBtn = user.findElement(By.xpath(closeXPath));
      closeBtn.click();
    }

    /**
    * Checks whether room is locked
    */
    private void testRoomIsLocked(WebDriver user) {
      MeetUIUtils.clickOnToolbarButton(user, "toolbar_button_link");
      String lockedXPath = "//div[@data-jqi-name='locked']";

      TestUtils.waitForDisplayedElementByXPath(user, lockedXPath, 5);
      WebElement elem = user.findElement(By.xpath(lockedXPath));

      assertTrue("Room must be locked", elem.isDisplayed());

      closeInviteDialog(user);
    }

    /**
    * Checks whether room is unlocked
    */
    private void testRoomIsUnlocked(WebDriver user) {
      MeetUIUtils.clickOnToolbarButton(user, "toolbar_button_link");
      String unlockedXPath = "//div[@data-jqi-name='unlocked']";

      TestUtils.waitForDisplayedElementByXPath(user, unlockedXPath, 5);
      WebElement elem = user.findElement(By.xpath(unlockedXPath));

      assertTrue("Room must be unlocked", elem.isDisplayed());

      closeInviteDialog(user);
    }

    /**
     * first wrong pin then correct one
     */
    public void enterParticipantInLockedRoom()
    {
        System.err.println("Start enterParticipantInLockedRoom.");

        testRoomIsLocked(ConferenceFixture.getOwner());

        WebDriver secondParticipant
            = ConferenceFixture.startSecondParticipant();

        try
        {
            MeetUtils.waitForParticipantToJoinMUC(secondParticipant);

            fail("The second participant must not be able to join the room.");
        }
        catch(TimeoutException e)
        {}

        secondParticipant.findElement(
            By.xpath("//input[@name='lockKey']")).sendKeys(ROOM_KEY + "1234");
        secondParticipant.findElement(
            By.name("jqi_state0_buttonspandatai18ndialogOkOkspan")).click();

        try
        {
            MeetUtils.waitForParticipantToJoinMUC(secondParticipant);

            fail("The second participant must not be able to join the room.");
        }
        catch(TimeoutException e)
        {}

        secondParticipant.findElement(
            By.xpath("//input[@name='lockKey']")).sendKeys(ROOM_KEY);
        secondParticipant.findElement(
            By.name("jqi_state0_buttonspandatai18ndialogOkOkspan")).click();

        MeetUtils.waitForParticipantToJoinMUC(secondParticipant);

        testRoomIsLocked(secondParticipant);
    }

    /**
     * Unlock room. Check wheter room is still
     * locked. Click remove and check whether it is unlocked.
     */
    public void unlockRoom()
    {
        System.err.println("Start unlockRoom.");

        ConferenceFixture.close(ConferenceFixture.getSecondParticipant());

        // just in case wait
        TestUtils.waitMillis(1000);

        ownerUnlockRoom();
    }

    /**
     * Owner unlocks the room.
     */
    private void ownerUnlockRoom()
    {
        WebDriver owner = ConferenceFixture.getOwner();

        MeetUIUtils.clickOnToolbarButton(owner, "toolbar_button_link");

        WebElement removeButton = TestUtils.waitForElementBy(
            owner,
            By.id("inviteDialogRemovePassword"),
            1);
        assertNotNull("Missing remove button", removeButton);
        removeButton.click();
        closeInviteDialog(owner);

        // Wait for the lock icon to disappear
        testRoomIsUnlocked(owner);
    }

    /**
     * Just enter the room and check that is not locked.
     */
    public void enterParticipantInUnlockedRoom()
    {
        System.err.println("Start enterParticipantInUnlockedRoom.");

        WebDriver secondParticipant
            = ConferenceFixture.startSecondParticipant();

        // if we fail to unlock the room this one will detect it
        // as participant will fail joining
        MeetUtils.waitForParticipantToJoinMUC(secondParticipant);
        MeetUtils.waitForIceConnected(secondParticipant);
        MeetUtils.waitForSendReceiveData(secondParticipant);

        testRoomIsUnlocked(secondParticipant);
    }

    /**
     * Both participants are in unlocked room, lock it and see whether the
     * change is reflected on the second participant icon.
     */
    public void updateLockedStateWhileParticipantInRoom()
    {
        System.err.println("Start updateLockedStateWhileParticipantInRoom.");

        ownerLockRoom();

        WebDriver secondParticipant = ConferenceFixture.getSecondParticipant();
        testRoomIsLocked(secondParticipant);
        ownerUnlockRoom();
        testRoomIsUnlocked(secondParticipant);
    }

    /**
     * Owner locks the room. Participant tries to enter using wrong password.
     * Owner unlocks the room and Participant cancels the password prompt and
     * he should enter of unlocked room.
     */
    public void unlockAfterParticipantEnterWrongPassword()
    {
        System.err.println("Start unlockAfterParticipantEnterWrongPassword.");

        ConferenceFixture.close(ConferenceFixture.getSecondParticipant());

        // just in case wait
        TestUtils.waitMillis(1000);

        ownerLockRoom();

        WebDriver secondParticipant
            = ConferenceFixture.startSecondParticipant();

        testRoomIsLocked(ConferenceFixture.getOwner());

        try
        {
            MeetUtils.waitForParticipantToJoinMUC(secondParticipant);

            fail("The second participant must not be able to join the room.");
        }
        catch(TimeoutException e)
        {}

        secondParticipant.findElement(
            By.xpath("//input[@name='lockKey']")).sendKeys(ROOM_KEY + "1234");
        secondParticipant.findElement(
            By.name("jqi_state0_buttonspandatai18ndialogOkOkspan")).click();

        try
        {
            MeetUtils.waitForParticipantToJoinMUC(secondParticipant);

            fail("The second participant must not be able to join the room.");
        }
        catch(TimeoutException e)
        {}

        ownerUnlockRoom();

        // just in case wait
        TestUtils.waitMillis(500);

        secondParticipant.findElement(
            By.name("jqi_state0_buttonspandatai18ndialogCancelCancelspan"))
            .click();

        MeetUtils.waitForParticipantToJoinMUC(secondParticipant);
        testRoomIsUnlocked(secondParticipant);
    }
}
