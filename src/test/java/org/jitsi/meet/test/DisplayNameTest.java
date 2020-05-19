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
import org.jitsi.meet.test.web.*;

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
    extends WebTestBase
{
    /**
     * The default display name.
     */
    private final static String DEFAULT_DISPLAY_NAME = "me";

    @Override
    public void setupClass()
    {
        super.setupClass();

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
     * @deprecated see
     * {@link AbstractBaseTest#AbstractBaseTest(AbstractBaseTest)}
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
        String randomName
            = "Name" + String.valueOf((int) (Math.random() * 1_000_000));

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
            + String.valueOf((int) (Math.random() * 1_000_000));

        getParticipant2().setDisplayName(randomName);

        doLocalDisplayNameCheck(randomName);

        // There is a max of 2 seconds delay in the persistency throttling
        TestUtils.waitMillis(2000);

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
     * both sides still miss the display name.
     */
    private void checkForDefaultDisplayNames()
    {
        // default remote display name
        WebParticipant participant1 = getParticipant1();
        WebParticipant participant2 = getParticipant2();
        String defaultDisplayName =
            (String) participant1.executeScript(
                    "return interfaceConfig.DEFAULT_REMOTE_DISPLAY_NAME;");

        // check on first browser
        checkRemoteVideoForName(
            participant1,
            participant2,
            defaultDisplayName);
        // check on second browser
        checkRemoteVideoForName(
            participant2,
            participant1,
            defaultDisplayName);
    }

    /**
     * Checks a remote video for a display name.
     * @param localParticipant the local participant.
     * @param remoteParticipant the remote one.
     * @param nameToCheck the name to check.
     */
    public void checkRemoteVideoForName(
        Participant localParticipant,
        Participant remoteParticipant,
        String nameToCheck)
    {
        String remoteParticipantEndpointId = remoteParticipant.getEndpointId();

        // check on local participant remote video
        WebElement displayNameElem =
            localParticipant.getDriver().findElement(By.xpath(
                "//span[@id='participant_" + remoteParticipantEndpointId +
                    "']//span[@id='participant_" +
                    remoteParticipantEndpointId + "_name']"));

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
        Participant participant1 = getParticipant1();
        WebDriver driver2 = getParticipant2().getDriver();

        // make sure we hover over other element first, cause when hovering
        // local element we may not out of it when editing
        WebElement remoteVideoElem =
            driver2.findElement(By.xpath(
                "//span[@id='participant_"
                    + participant1.getEndpointId() + "']"));
        Actions action0 = new Actions(driver2);
        action0.moveToElement(remoteVideoElem);
        action0.perform();

        // now lets check whether display name is set locally
        WebElement displayNameElem
            = driver2.findElement(By.xpath(
                "//span[@id='localVideoContainer']"
                    + "//span[@id='localDisplayName']"));

        // hover over local display name
        WebElement localVideoContainerElem
            = driver2.findElement(By.xpath(
                "//span[@id='localVideoContainer']"));
        Actions action = new Actions(driver2);
        action.moveToElement(localVideoContainerElem);
        action.build().perform();

        boolean isFF = getParticipant2().getType().isFirefox();
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
        Participant participant1 = getParticipant1();
        Participant participant2 = getParticipant2();
        WebDriver driver1 = participant1.getDriver();
        final String participant2EndpointId = participant2.getEndpointId();

        // first when checking make sure we click on video so we avoid
        // the situation of dominant speaker detection and changing display
        MeetUIUtils.clickOnRemoteVideo(
            driver1, participant2EndpointId);

        WebElement localVideoWrapperElem =
            driver1.findElement(By.xpath(
                "//span[@id='participant_" + participant2EndpointId + "']"));
        Actions action = new Actions(driver1);
        action.moveToElement(localVideoWrapperElem);
        action.perform();

        checkRemoteVideoForName(participant1, participant2, newName);

        MeetUIUtils.clickOnRemoteVideo(driver1, participant2EndpointId);
    }
}
