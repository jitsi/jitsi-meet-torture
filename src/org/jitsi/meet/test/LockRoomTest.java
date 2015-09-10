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

        return suite;
    }

    /**
     * Stops the participant. And locks the room from the owner.
     */
    public void lockRoom()
    {
        System.err.println("Start lockRoom.");

        ConferenceFixture.quit(ConferenceFixture.getSecondParticipant());

        // just in case wait
        TestUtils.waits(1000);

        List<WebElement> elems = ConferenceFixture.getOwner().findElements(
            By.xpath("//span[@id='toolbar']/a[@class='button "
                + "icon-security-locked']"));

        assertTrue("Icon must be unlocked when starting the test",
            elems.isEmpty());

        MeetUIUtils.clickOnToolbarButton(
            ConferenceFixture.getOwner(), "toolbar_button_security");

        // fill in the dialog
        TestUtils.waitsForElementByXPath(ConferenceFixture.getOwner(),
            "//input[@name='lockKey']", 5);
        ROOM_KEY = String.valueOf((int)(Math.random()*1000000));
        ConferenceFixture.getOwner().findElement(
            By.xpath("//input[@name='lockKey']")).sendKeys(ROOM_KEY);

        ConferenceFixture.getOwner().findElement(
            By.name("jqi_state0_buttonspandatai18ndialogSaveSavespan")).click();

        TestUtils.waits(1000);

        TestUtils.waitsForElementByXPath(ConferenceFixture.getOwner(),
            "//span[@id='toolbar']/a[@class='button icon-security-locked']", 5);
    }

    /**
     * first wrong pin then correct one
     */
    public void enterParticipantInLockedRoom()
    {
        System.err.println("Start enterParticipantInLockedRoom.");

        ConferenceFixture.startParticipant();

        TestUtils.waitsForElementByXPath(ConferenceFixture.getOwner(),
            "//span[@id='toolbar']/a[@class='button icon-security-locked']", 5);

        try
        {
            ConferenceFixture.checkParticipantToJoinRoom(
                ConferenceFixture.getSecondParticipant(), 5);

            assertTrue("Second participant cannot join room as it is locked",
                false);
        }
        catch(TimeoutException e)
        {}

        ConferenceFixture.getSecondParticipant().findElement(
            By.xpath("//input[@name='lockKey']")).sendKeys(ROOM_KEY + "1234");
        ConferenceFixture.getSecondParticipant().findElement(
            By.name("jqi_state0_buttonspandatai18ndialogOkOkspan")).click();

        try
        {
            ConferenceFixture.checkParticipantToJoinRoom(
                ConferenceFixture.getSecondParticipant(), 5);

            assertTrue("Second participant cannot join room as it is locked",
                false);
        }
        catch(TimeoutException e)
        {}

        ConferenceFixture.getSecondParticipant().findElement(
            By.xpath("//input[@name='lockKey']")).sendKeys(ROOM_KEY);
        ConferenceFixture.getSecondParticipant().findElement(
            By.name("jqi_state0_buttonspandatai18ndialogOkOkspan")).click();

        ConferenceFixture.checkParticipantToJoinRoom(
            ConferenceFixture.getSecondParticipant(), 5);

        TestUtils.waitsForElementByXPath(
            ConferenceFixture.getSecondParticipant(),
            "//span[@id='toolbar']/a[@class='button icon-security-locked']", 5);
    }

    /**
     * Unlock room. Click cancel removing key and test whether it is still
     * locked. Click remove and check whether it is unlocked.
     */
    public void unlockRoom()
    {
        System.err.println("Start unlockRoom.");

        ConferenceFixture.quit(ConferenceFixture.getSecondParticipant());

        // just in case wait
        TestUtils.waits(1000);

        List<WebElement> elems = ConferenceFixture.getOwner().findElements(
            By.xpath("//span[@id='toolbar']/a[@class='button']/" +
                "i[@class='icon-security']"));

        assertTrue("Icon must be locked when starting this test",
            elems.isEmpty());

        MeetUIUtils.clickOnToolbarButton(
            ConferenceFixture.getOwner(), "toolbar_button_security");

        ConferenceFixture.getOwner().findElement(
            By.name("jqi_state0_buttonspandatai18ndialogCancelCancelspan"))
                .click();

        elems = ConferenceFixture.getOwner().findElements(
            By.xpath("//span[@id='toolbar']/a[@class='button']/" +
                "i[@class='icon-security']"));

        assertTrue("Icon must be locked after clicking cancel on remove key " +
                "for room",
            elems.isEmpty());

        MeetUIUtils.clickOnToolbarButton(
            ConferenceFixture.getOwner(), "toolbar_button_security");

        ConferenceFixture.getOwner().findElement(
            By.name("jqi_state0_buttonspandatai18ndialogRemoveRemovespan"))
                .click();

        // Wait for the lock icon to disappear
        try
        {
            TestUtils.waitsForElementNotPresentByXPath(
                ConferenceFixture.getOwner(),
                "//span[@id='toolbar']/a[@class='button icon-security-locked']",
                10);
        }
        catch (TimeoutException exc)
        {
            fail("Icon must be unlocked after removing the key");
        }
    }

    /**
     * Just enter the room and check that is not locked.
     */
    public void enterParticipantInUnlockedRoom()
    {
        System.err.println("Start enterParticipantInUnlockedRoom.");

        ConferenceFixture.startParticipant();

        // if we fail to unlock the room this one will detect it
        // as participant will fail joining
        ConferenceFixture.checkParticipantToJoinRoom(
            ConferenceFixture.getSecondParticipant(), 5);

        List<WebElement> elems = ConferenceFixture.getSecondParticipant()
            .findElements(
                By.xpath("//span[@id='toolbar']/a[@class='button "
                    + "icon-security-locked']"));

        assertTrue("Icon must be unlocked when starting the test",
            elems.isEmpty());
    }
}
