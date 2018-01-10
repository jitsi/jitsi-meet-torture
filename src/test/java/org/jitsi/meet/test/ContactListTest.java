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
    extends AbstractBaseTest
{
    @Override
    public void setup()
    {
        super.setup();

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
        WebDriver owner = getParticipant1().getDriver();

        // Make sure that the contact list panel is open.
        MeetUIUtils.displayContactListPanel(owner);

        // Make sure we have the 2 initial participants shown in the contact
        // list.
        doContactCountCheck(owner, 2);

        // Add a third participant to the call.
        ensureThreeParticipants();

        // Make sure we have a line in the contact list for every participant on
        // the call.
        doContactIdsCheck(owner);

        // Pins a participant clicking on a contact list entry.
        doPinSecondParticipantCheck(owner);
    }

    /**
     * Check contact list count corresponds to the expected count.
     *
     * @param user <tt>WebDriver</tt> instance of the participant for whom we'll
     *             try to check the contact list count
     * @param expectedCount the expected count of contact list participants
     */
    private void doContactCountCheck(WebDriver user, int expectedCount)
    {
        String contactListXPath = "//ul[@id='contacts']";
        WebElement contactListElem
            = user.findElement(By.xpath(contactListXPath));

        int contactCount
            = contactListElem.findElements(By.className("clickable")).size();

        assertEquals(expectedCount, contactCount);
    }

    /**
     * Checks if we have a line in the contact list for every participant in
     * the call.
     *
     * @param owner the owner of the conference
     */
    private void doContactIdsCheck(WebDriver owner)
    {
        String ownerJid = MeetUtils.getResourceJid(owner);
        String secondParticipantJid
            = MeetUtils.getResourceJid(getParticipant2().getDriver());
        String thirdParticipantJid
            = MeetUtils.getResourceJid(getParticipant3().getDriver());

        String ownerLiXPath = getContactListParticipantXPath(ownerJid);
        String secondParticipantLiXPath
            = getContactListParticipantXPath(secondParticipantJid);
        String thirdParticipantLiXPath
            = getContactListParticipantXPath(thirdParticipantJid);

        TestUtils.waitForElementByXPath(owner, ownerLiXPath, 5);
        TestUtils.waitForElementByXPath(owner, secondParticipantLiXPath, 5);
        TestUtils.waitForElementByXPath(owner, thirdParticipantLiXPath, 5);
    }

    /**
     * Checks if clicking on a participant in the contact list would pin the
     * participant small and large videos.
     *
     * @param owner <tt>WebDriver</tt> instance of the participant for whom we'll
     *             try to check the contact list pin
     */
    private void doPinSecondParticipantCheck(WebDriver owner)
    {
        final String secondParticipantJid
            = MeetUtils.getResourceJid(getParticipant2().getDriver());
        WebElement secondPartLi
            = owner.findElement(By.xpath(getContactListParticipantXPath(
                secondParticipantJid)));

        secondPartLi.click();

        TestUtils.waitForDisplayedElementByXPath(owner,
                "//span[@id='participant_" + secondParticipantJid
                        + "' and contains(@class,'videoContainerFocused') ]", 5);


        // Verify that the user is now the focused participant from the owner's
        // perspective.
        try
        {
            new WebDriverWait(owner, 10).until(
                (ExpectedCondition<Boolean>) d -> secondParticipantJid.equals(
                        MeetUIUtils.getLargeVideoResource(d)));
        }
        catch (TimeoutException exc)
        {
            assertEquals(
                secondParticipantJid,
                MeetUIUtils.getLargeVideoResource(owner),
                "Pinned participant not displayed on large video");
        }
        finally
        {
            // make sure we unpin the participant, as this may brake other tests
            secondPartLi.click();
        }
    }

    /**
     * Returns the XPath string corresponding to the contact list participant
     * element with the given jid.
     *
     * @param jid the jid of the participant we're looking for
     * @return a String corresponding to the XPath element we're looking for
     */
    private String getContactListParticipantXPath(String jid)
    {
        return "//li[@id='" + jid + "']";
    }
}