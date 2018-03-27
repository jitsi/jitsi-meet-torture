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
import org.openqa.selenium.support.ui.*;
import org.testng.annotations.*;

import static org.testng.Assert.*;

/**
 * A test making sure that the contact list panel can be open, that it contains
 * lines for every participant in the call and that when clicked the participant
 * will be focused in the call.
 *
 * @author Yana Stamcheva
 */
public class ContactListTest
    extends WebTestBase
{
    @Override
    public void setupClass()
    {
        super.setupClass();

        // We make sure that we have a conference with at least 2 participants.
        ensureTwoParticipants();
    }

    /**
     * Tests if the contact list contains the right number of participants, but
     * also if the list contains every participant in the call. Tests if a click
     * pins a participant.
     */
    @Test
    public void testContactList()
    {
        WebParticipant participant1 = getParticipant1();

        // Make sure that the contact list panel is open.
        MeetUIUtils.displayContactListPanel(participant1);

        // Make sure we have the 2 initial participants shown in the contact
        // list.
        doContactCountCheck(participant1, 2);

        // Add a third participant to the conference.
        ensureThreeParticipants();

        // Make sure we have a line in the contact list for every participant on
        // the call.
        doContactIdsCheck();

        // Pins a participant clicking on a contact list entry.
        doPinParticipant2Check();
    }

    /**
     * Check contact list count corresponds to the expected count.
     *
     * @param participant the {@link Participant} for whom we'll try to check
     * the contact list count.
     * @param expectedCount the expected count of contact list participants
     */
    private void doContactCountCheck(Participant participant, int expectedCount)
    {
        String contactListXPath = "//ul[@id='contacts']";
        WebElement contactListElem
            = participant.getDriver().findElement(By.xpath(contactListXPath));

        int contactCount
            = contactListElem.findElements(By.className("clickable")).size();

        assertEquals(contactCount, expectedCount);
    }

    /**
     * Checks if we have a line in the contact list for every participant in
     * the call.
     */
    private void doContactIdsCheck()
    {
        WebDriver driver1 = getParticipant1().getDriver();

        String participant1LiXPath
            = getContactListParticipantXPath(getParticipant1().getEndpointId());
        String participant2LiXPath
            = getContactListParticipantXPath(getParticipant2().getEndpointId());
        String participant3LiXPath
            = getContactListParticipantXPath(getParticipant3().getEndpointId());

        TestUtils.waitForElementByXPath(driver1, participant1LiXPath, 5);
        TestUtils.waitForElementByXPath(driver1, participant2LiXPath, 5);
        TestUtils.waitForElementByXPath(driver1, participant3LiXPath, 5);
    }

    /**
     * Checks if clicking on a participant in the contact list would pin the
     * participant small and large videos.
     *
     */
    private void doPinParticipant2Check()
    {
        WebDriver driver1 = getParticipant1().getDriver();
        final String participant2EndpointId = getParticipant2().getEndpointId();
        WebElement participant2Li
            = driver1.findElement(
                By.xpath(getContactListParticipantXPath(
                    participant2EndpointId)));

        participant2Li.click();

        TestUtils.waitForDisplayedElementByXPath(
            driver1,
            "//span[@id='participant_" + participant2EndpointId
                + "' and contains(@class,'videoContainerFocused') ]",
            5);


        // Verify that the user is now the focused participant from the
        // perspective of participant1
        try
        {
            new WebDriverWait(driver1, 10).until(
                (ExpectedCondition<Boolean>) d -> participant2EndpointId.equals(
                        MeetUIUtils.getLargeVideoResource(d)));
        }
        catch (TimeoutException exc)
        {
            assertEquals(
                MeetUIUtils.getLargeVideoResource(driver1),
                participant2EndpointId,
                "Pinned participant not displayed on large video");
        }
        finally
        {
            // make sure we unpin the participant, as this may break other tests
            participant2Li.click();
        }
    }

    /**
     * Returns the XPath string corresponding to the contact list participant
     * element with the given jid.
     *
     * @param endpointId the endpoint id (i.e. resource part of the occupant JID
     * in the MUC) of the participant we're looking for.
     * @return a String corresponding to the XPath element we're looking for
     */
    private String getContactListParticipantXPath(String endpointId)
    {
        return "//li[@id='" + endpointId + "']";
    }
}