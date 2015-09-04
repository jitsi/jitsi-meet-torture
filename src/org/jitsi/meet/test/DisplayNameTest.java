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
import org.openqa.selenium.interactions.*;

/**
 * A test which changes display name and checks the reflect of the change
 * locally and remotely.
 *
 * @author Damian Minkov
 */
public class DisplayNameTest
    extends TestCase
{
    /**
     * The default display name.
     */
    private final static String DEFAULT_DISPLAY_NAME = "me";

    /**
     * Tests changing display name.
     * First change name and check locally, then exit room enter room check
     * persistent, then check on the remote side.
     * Do the same tests for removing the display name.
     */
    public void testChangingDisplayName()
    {
        System.err.println("Start testChangingDisplayName.");

        String randomName = "Name"
            + String.valueOf((int)(Math.random()*1000000));

        checkDisplayNameChange(randomName);

        // now lets clear display name
        // not supported yet
        //checkDisplayNameChange(null);
    }

    /**
     * Do the check, change display name and check locally and remotely.
     * @param newName the new name
     */
    private void checkDisplayNameChange(String newName)
    {
        System.err.println("Start checkDisplayNameChange.");

        changeDisplayName(newName);

        doLocalDisplayNameCheck(newName);

        doRemoteDisplayNameCheck(newName);
    }

    /**
     * Check whether name has changed locally.
     * @param newName the name we changed
     */
    private void doLocalDisplayNameCheck(String newName)
    {
        System.err.println("Start doLocalDisplayNameCheck.");

        // now lets check whether display name is set locally
        WebElement displayNameElem =
            ConferenceFixture.getSecondParticipant().findElement(By.xpath(
                "//span[@id='localVideoContainer']/span[@id='localDisplayName']"));

        WebElement localVideoWrapperElem =
            ConferenceFixture.getSecondParticipant().findElement(By.xpath(
                "//span[@id='localVideoWrapper']"));
        Actions action = new Actions(ConferenceFixture.getSecondParticipant());
        action.moveToElement(localVideoWrapperElem);
        action.perform();

        assertTrue("Display name not visible", displayNameElem.isDisplayed());

        String displayNameText = displayNameElem.getText();

        if(newName != null && newName.length() > 0)
            assertTrue("Display name not changed! Content of elem is:"
                + displayNameText, displayNameText.contains(newName));
        else
            assertTrue("Display name is not removed! ("
                    + displayNameText + ")",
                displayNameText.equals(DEFAULT_DISPLAY_NAME));
    }

    /**
     * Check whether name has changed remotely.
     * @param newName the name we changed
     */
    private void doRemoteDisplayNameCheck(String newName)
    {
        System.err.println("Start doRemoteDisplayNameCheck.");

        // first when checking make sure we click on video so we avoid
        // the situation of dominant speaker detection and changing display
        new SwitchVideoTests("ownerClickOnRemoteVideoAndTest")
            .ownerClickOnRemoteVideoAndTest();

        final String secondParticipantResourceJid = MeetUtils
            .getResourceJid(ConferenceFixture.getSecondParticipant());

        WebElement localVideoWrapperElem =
            ConferenceFixture.getOwner().findElement(By.xpath(
                "//span[@id='participant_" + secondParticipantResourceJid + "']"));
        Actions action = new Actions(ConferenceFixture.getOwner());
        action.moveToElement(localVideoWrapperElem);
        action.perform();

        // now lets check whether display name is set locally
        WebElement displayNameElem =
            ConferenceFixture.getOwner().findElement(By.xpath(
                "//span[@id='participant_" + secondParticipantResourceJid +
                "']/span[@id='participant_" + secondParticipantResourceJid + "_name']"));

        assertTrue("Display name not visible", displayNameElem.isDisplayed());

        String displayNameText = displayNameElem.getText();

        if(newName != null && newName.length() > 0)
            assertTrue("Display name not remotely changed! Content of elem is:"
                + displayNameText, displayNameText.contains(newName));
        else
            assertTrue("Display name is not remotely removed! ("
                    + displayNameText + ")",
                displayNameText.equals(DEFAULT_DISPLAY_NAME));
    }

    /**
     * Changes the display name.
     * @param newName the name to change.
     */
    private void changeDisplayName(String newName)
    {
        System.err.println("Start changeDisplayName.");

        WebElement elem =
            ConferenceFixture.getSecondParticipant().findElement(By.xpath(
                "//span[@id='localVideoContainer']/a[@class='displayname']"));
        elem.click();

        WebElement inputElem =
            ConferenceFixture.getSecondParticipant().findElement(By.xpath(
                "//span[@id='localVideoContainer']/input[@class='displayname']"));
        Actions action = new Actions(ConferenceFixture.getSecondParticipant());
        action.moveToElement(inputElem);
        action.perform();

        if(newName != null && newName.length() > 0)
            inputElem.sendKeys(newName);
        else
            inputElem.sendKeys(Keys.BACK_SPACE);

        inputElem.sendKeys(Keys.RETURN);
    }
}
