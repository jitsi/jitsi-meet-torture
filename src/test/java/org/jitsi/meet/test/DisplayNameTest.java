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
import org.openqa.selenium.interactions.*;
import org.testng.annotations.*;

import static org.testng.Assert.*;

/**
 * A test which changes display name and checks the reflect of the change
 * locally and remotely.
 *
 * @author Damian Minkov
 */
public class DisplayNameTest
    extends AbstractBaseTest
{
    /**
     * The default display name.
     */
    private final static String DEFAULT_DISPLAY_NAME = "me";

    @Override
    public void setup()
    {
        super.setup();

        ensureTwoParticipants();
    }

    /**
     * Default constructor.
     */
    public DisplayNameTest()
    {}

    /**
     * Constructs DisplayNameTest with already allocated test.
     * @param baseTest the parent test
     */
    public DisplayNameTest(AbstractBaseTest baseTest)
    {
        super(baseTest);
    }

    /**
     * Tests changing display name.
     * First change name and check locally, then exit room enter room check
     * persistent, then check on the remote side.
     * Do the same tests for removing the display name.
     */
    @Test
    public void testChangingDisplayName()
    {
        String randomName = "Name"
            + String.valueOf((int)(Math.random()*1000000));

        checkDisplayNameChange(randomName);

        // now lets clear display name
        // not supported yet
        //checkDisplayNameChange(null);
    }

    /**
     * Checks whether display name persisted after reload of the page.
     */
    @Test(dependsOnMethods = { "testChangingDisplayName" })
    public void testDisplayNamePersistence()
    {
        String randomName = "Name"
            + String.valueOf((int)(Math.random()*1000000));

        getParticipant2().setDisplayName(randomName);

        doLocalDisplayNameCheck(randomName);

        // now let's reload the second participant
        getParticipant2().hangUp();
        ensureTwoParticipants();

        doLocalDisplayNameCheck(randomName);
    }

    /**
     * Do the check, change display name and check locally and remotely.
     * @param newName the new name
     */
    public void checkDisplayNameChange(String newName)
    {
        checkForDefaultDisplayNames();

        getParticipant2().setDisplayName(newName);

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
        Participant owner = getParticipant1();
        String defaultDisplayName =
            (String) owner.executeScript(
                    "return interfaceConfig.DEFAULT_REMOTE_DISPLAY_NAME;");

        // check on first browser
        checkRemoteVideoForName(
            owner,
            getParticipant2(),
            defaultDisplayName);
        // check on second browser
        checkRemoteVideoForName(
            getParticipant2(),
            getParticipant1(),
            defaultDisplayName);
    }

    /**
     * Checks a remote video for a display name.
     * @param local the local participant
     * @param remoteParticipant teh remote one
     * @param nameToCheck the name to check.
     */
    public void checkRemoteVideoForName(
        Participant local,
        Participant remoteParticipant,
        String nameToCheck)
    {
        String remoteParticipantResourceJid
            = MeetUtils.getResourceJid(remoteParticipant.getDriver());

        // check on local participant remote video
        WebElement displayNameElem =
            local.getDriver().findElement(By.xpath(
                "//span[@id='participant_" + remoteParticipantResourceJid +
                    "']//span[@id='participant_" +
                    remoteParticipantResourceJid + "_name']"));

        boolean isFF = remoteParticipant.getType().isFirefox();
        if (!isFF)
        {
            assertTrue(
                displayNameElem.isDisplayed(),
                "Display name not visible");
        }

        String displayNameText = displayNameElem.getText();
        if (isFF)
        {
            displayNameText = displayNameElem.getAttribute("innerHTML");
        }

        assertTrue(
            displayNameText.contains(nameToCheck),
            "Wrong display name! Content of elem is:" + displayNameText);
    }

    /**
     * Check whether name has changed locally.
     * @param newName the name we changed
     */
    public void doLocalDisplayNameCheck(String newName)
    {
        WebDriver secondParticipant = getParticipant2().getDriver();

        // make sure we hover over other element first, cause when hovering
        // local element we may not out of it when editing
        WebElement remoteVideoElem =
            secondParticipant.findElement(By.xpath(
                "//span[@id='participant_" + MeetUtils
                    .getResourceJid(getParticipant1().getDriver()) + "']"));
        Actions action0 = new Actions(secondParticipant);
        action0.moveToElement(remoteVideoElem);
        action0.perform();

        // now lets check whether display name is set locally
        WebElement displayNameElem
            = secondParticipant.findElement(By.xpath(
                "//span[@id='localVideoContainer']"
                + "//span[@id='localDisplayName']"));

        // hover over local display name
        WebElement localVideoContainerElem
            = secondParticipant.findElement(By.xpath(
                "//span[@id='localVideoContainer']"));
        Actions action = new Actions(secondParticipant);
        action.moveToElement(localVideoContainerElem);
        action.build().perform();

        boolean isFF
            = getParticipant2().getType().isFirefox();
        if (!isFF)
        {
            assertTrue(
                displayNameElem.isDisplayed(),
                "Display name not visible");
        }

        String displayNameText = displayNameElem.getText();
        // there is a bug in FF with hovering over elements
        // so we workaround this
        if (isFF)
        {
            displayNameText = displayNameElem.getAttribute("innerHTML");
        }

        if (newName != null && newName.length() > 0)
        {
            assertTrue(
                displayNameText.contains(newName),
                "Display name not changed! Content of elem is: "
                    + displayNameText);
        }
        else
        {
            assertTrue(
                displayNameText.equals(DEFAULT_DISPLAY_NAME),
                "Display name is not removed! (" + displayNameText + ")");
        }
    }

    /**
     * Check whether name has changed remotely.
     * @param newName the name we changed
     */
    public void doRemoteDisplayNameCheck(String newName)
    {
        Participant owner = getParticipant1();
        WebDriver ownerDriver = owner.getDriver();
        WebDriver secondParticipant = getParticipant2().getDriver();

        // first when checking make sure we click on video so we avoid
        // the situation of dominant speaker detection and changing display
        MeetUIUtils.clickOnRemoteVideo(
            ownerDriver, MeetUtils.getResourceJid(secondParticipant));

        final String secondParticipantResourceJid = MeetUtils
            .getResourceJid(secondParticipant);

        WebElement localVideoWrapperElem =
            ownerDriver.findElement(By.xpath(
                "//span[@id='participant_" + secondParticipantResourceJid + "']"));
        Actions action = new Actions(ownerDriver);
        action.moveToElement(localVideoWrapperElem);
        action.perform();

        checkRemoteVideoForName(owner, getParticipant2(), newName);

        MeetUIUtils.clickOnRemoteVideo(
            ownerDriver, MeetUtils.getResourceJid(secondParticipant));
    }
}
