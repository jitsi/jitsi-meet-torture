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
    public void checkDisplayNameChange(String newName)
    {
        System.err.println("Start checkDisplayNameChange.");

        checkForDefaultDisplayNames();

        changeDisplayName(newName);

        doLocalDisplayNameCheck(newName);

        doRemoteDisplayNameCheck(newName);
    }

    /**
     * Checks whether default display names are set and shown, when
     * both sides still miss the displayname.
     */
    private void checkForDefaultDisplayNames()
    {
        // default remote displayname
        String defaultDisplayName =
            (String)((JavascriptExecutor) ConferenceFixture.getOwner())
                .executeScript(
                    "return interfaceConfig.DEFAULT_REMOTE_DISPLAY_NAME;");

        // check on first browser
        checkRemoteVideoForName(ConferenceFixture.getOwner(),
            ConferenceFixture.getSecondParticipant(),
            defaultDisplayName);
        // check on second browser
        checkRemoteVideoForName(ConferenceFixture.getSecondParticipant(),
            ConferenceFixture.getOwner(),
            defaultDisplayName);
    }

    /**
     * Checks a remote video for a display name.
     * @param local the local participant
     * @param remote teh remote one
     * @param nameToCheck the name to check.
     */
    private void checkRemoteVideoForName(
        WebDriver local,
        WebDriver remote,
        String nameToCheck)
    {
        String remoteParticipantResourceJid
            = MeetUtils.getResourceJid(remote);

        // check on local participant remote video
        WebElement displayNameElem =
            local.findElement(By.xpath(
                "//span[@id='participant_" + remoteParticipantResourceJid +
                    "']//span[@id='participant_" +
                    remoteParticipantResourceJid + "_name']"));

        boolean isFF
            = ConferenceFixture.getBrowserType(remote)
                    == ConferenceFixture.BrowserType.firefox;
        if (!isFF)
        {
            assertTrue("Display name not visible",
                displayNameElem.isDisplayed());
        }

        String displayNameText = displayNameElem.getText();
        if (isFF)
        {
            displayNameText = displayNameElem.getAttribute("innerHTML");
        }

        assertTrue("Wrong display name! Content of elem is:"
            + displayNameText, displayNameText.contains(nameToCheck));
    }

    /**
     * Check whether name has changed locally.
     * @param newName the name we changed
     */
    public void doLocalDisplayNameCheck(String newName)
    {
        System.err.println("Start doLocalDisplayNameCheck for " + newName + ".");

        WebDriver secondParticipant = ConferenceFixture.getSecondParticipant();

        // make sure we hover over other element first, cause when hovering
        // local element we may not out of it when editing
        WebElement remoteVideoElem =
            secondParticipant.findElement(By.xpath(
                "//span[@id='participant_" + MeetUtils
                    .getResourceJid(ConferenceFixture.getOwner()) + "']"));
        Actions action0 = new Actions(secondParticipant);
        action0.moveToElement(remoteVideoElem);
        action0.perform();

        // now lets check whether display name is set locally
        WebElement displayNameElem
            = secondParticipant.findElement(By.xpath(
                "//span[@id='localVideoContainer']/span[@id='localDisplayName']"));

        // hover over local display name
        WebElement localVideoContainerElem
            = secondParticipant.findElement(By.xpath(
                "//span[@id='localVideoContainer']"));
        Actions action = new Actions(secondParticipant);
        action.moveToElement(localVideoContainerElem);
        action.build().perform();

        boolean isFF
            = ConferenceFixture.getBrowserType(secondParticipant) ==
                    ConferenceFixture.BrowserType.firefox;
        if (!isFF)
        {
            assertTrue("Display name not visible",
                displayNameElem.isDisplayed());
        }

        String displayNameText = displayNameElem.getText();
        // there is a bug in FF with hovering over elements
        // so we workaround this
        if (isFF)
        {
            displayNameText = displayNameElem.getAttribute("innerHTML");
        }

        if(newName != null && newName.length() > 0)
            assertTrue("Display name not changed! Content of elem is: "
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
    public void doRemoteDisplayNameCheck(String newName)
    {
        System.err.println("Start doRemoteDisplayNameCheck for " + newName + ".");

        WebDriver owner = ConferenceFixture.getOwner();
        WebDriver secondParticipant = ConferenceFixture.getSecondParticipant();

        // first when checking make sure we click on video so we avoid
        // the situation of dominant speaker detection and changing display
        MeetUIUtils.clickOnRemoteVideo(
            owner, MeetUtils.getResourceJid(secondParticipant));

        final String secondParticipantResourceJid = MeetUtils
            .getResourceJid(secondParticipant);

        WebElement localVideoWrapperElem =
            owner.findElement(By.xpath(
                "//span[@id='participant_" + secondParticipantResourceJid + "']"));
        Actions action = new Actions(owner);
        action.moveToElement(localVideoWrapperElem);
        action.perform();

        checkRemoteVideoForName(owner, secondParticipant, newName);

        MeetUIUtils.clickOnRemoteVideo(
            owner, MeetUtils.getResourceJid(secondParticipant));
    }

    /**
     * Changes the display name.
     * @param newName the name to change.
     */
    private void changeDisplayName(String newName)
    {
        System.err.println("Start changeDisplayName for " + newName + ".");

        WebDriver secondParticipant
            = ConferenceFixture.getSecondParticipant();

        WebElement elem =
            secondParticipant.findElement(By.xpath(
                "//span[@id='localVideoContainer']"
                + "//span[@class='displayname']"));
        elem.click();

        WebElement inputElem =
            secondParticipant.findElement(By.xpath(
                "//span[@id='localVideoContainer']"
                + "//input[@class='editdisplayname']"));
        Actions action = new Actions(secondParticipant);
        action.moveToElement(inputElem);
        action.perform();

        if(newName != null && newName.length() > 0)
            inputElem.sendKeys(newName);
        else
            inputElem.sendKeys(Keys.BACK_SPACE);

        inputElem.sendKeys(Keys.RETURN);
        // just click somewhere to lose focus, to make sure editing has ended
        String ownerResource
            = MeetUtils.getResourceJid(ConferenceFixture.getOwner());
        MeetUIUtils.clickOnRemoteVideo(secondParticipant, ownerResource);
        MeetUIUtils.clickOnRemoteVideo(secondParticipant, ownerResource);
    }
}
