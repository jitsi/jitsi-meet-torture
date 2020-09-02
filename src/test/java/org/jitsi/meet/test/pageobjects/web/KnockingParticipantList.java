/*
 * Copyright @ 2018 8x8 Pty Ltd
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
package org.jitsi.meet.test.pageobjects.web;

import org.jitsi.meet.test.util.*;
import org.jitsi.meet.test.web.*;
import org.openqa.selenium.*;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.interactions.*;

import java.util.*;
import java.util.logging.*;

/**
 * Representation of the list of knocking participants with allow and reject action.
 * Appears to moderators when Lobby is enabled for a room.
 */
public class KnockingParticipantList
{
    /**
     * The id of the participants list element.
     */
    private final static String KNOCKING_PARTICIPANT_LIST_ID = "knocking-participant-list";

    /**
     * The xpath to locate all knocking participants list.
     */
    private final static String PARTICIPANTS_XPATH = "//div[@id='" + KNOCKING_PARTICIPANT_LIST_ID + "']//ul//li";

    /**
     * TestIds for the participant details: avatar, name, email and the action buttons for allow and reject.
     */
    private final static String PARTICIPANT_AVATAR_TEST_ID = "knockingParticipant.avatar";
    private final static String PARTICIPANT_EMAIL_TEST_ID = "knockingParticipant.email";
    private final static String PARTICIPANT_NAME_TEST_ID = "knockingParticipant.name";
    private final static String PARTICIPANT_ALLOW_TEST_ID = "lobby.allow";
    private final static String PARTICIPANT_REJECT_TEST_ID = "lobby.reject";

    /**
     * The participant used to interact with the knocking participants list.
     */
    private final WebParticipant participant;

    /**
     * Initializes a new {@link KnockingParticipantList} instance.
     *
     * @param participant the participant for this {@link KnockingParticipantList}.
     */
    public KnockingParticipantList(WebParticipant participant)
    {
        this.participant = participant;
    }

    /**
     * Returns the list of knocking participants. If the UI list is not found will return empty list of participants.
     * @return list of knocking participants.
     */
    public List<Participant> getParticipants()
    {
        List<Participant> participants = new ArrayList<>();

        try
        {
            TestUtils.waitForDisplayedElementByID(participant.getDriver(), KNOCKING_PARTICIPANT_LIST_ID, 3);
        }
        catch(TimeoutException ex)
        {
            // if the list is missing return empty list of participants
            Logger.getGlobal().log(Level.WARNING, "No knocking participant list ");
            return participants;
        }

        List<WebElement> listElements = participant.getDriver().findElements(By.xpath(PARTICIPANTS_XPATH));

        listElements.forEach(el -> {
            String avatarUrl = el.findElement(ByTestId.testId(PARTICIPANT_AVATAR_TEST_ID)).getAttribute("src");
            String name = el.findElement(ByTestId.testId(PARTICIPANT_NAME_TEST_ID)).getText();
            String email = null;
                try
                {
                    email = el.findElement(By.id(PARTICIPANT_EMAIL_TEST_ID)).getText();
                }
                catch(NoSuchElementException e)
                {
                    // email is optional and may be missing
                }
            participants.add(new Participant(avatarUrl, name, email));
        });

        return participants;
    }

    /**
     * Will wait 3 seconds for the knocking participants to disappear and return true or will return false.
     * @return <tt>true</tt> if the knocking participants list was not displayed.
     */
    public boolean waitForHideOfKnockingParticipants()
    {
        try
        {
            TestUtils.waitForNotDisplayedElementByID(participant.getDriver(), KNOCKING_PARTICIPANT_LIST_ID, 3);

            return true;
        }
        catch(TimeoutException ex)
        {
            return false;
        }
    }


    /**
     * Presentation for the participants in the knocking participants list.
     */
    public class Participant
    {
        private final String avatarUrl;
        private final String name;
        private final String email;

        public Participant(String avatarUrl, String name, String email)
        {
            this.avatarUrl = avatarUrl;
            this.name = name;
            this.email = email;
        }

        public String getAvatarUrl()
        {
            return avatarUrl;
        }

        public String getName()
        {
            return name;
        }

        public String getEmail()
        {
            return email;
        }

        /**
         * Allow participant to join meeting.
         */
        public void allow()
        {
            clickButton(PARTICIPANT_ALLOW_TEST_ID);
        }

        /**
         * Denies participant access.
         */
        public void reject()
        {
            clickButton(PARTICIPANT_REJECT_TEST_ID);
        }

        /**
         * Clicks the action button for the current participant.
         * @param buttonId the button Id to click.
         */
        private void clickButton(String buttonId)
        {
            WebElement participantElement = getParticipantElement();

            if (participantElement == null)
                throw new NoSuchElementException("Participant not found");

            // there are occasions where the click is not triggered, especially when few browsers run on the same
            // machine, so we will retry several times before giving up
            // the element should disappear once it was successfully clicked
            int retries = 5;
            while(participantElement != null && retries > 0)
            {
                new Actions(participant.getDriver())
                    .click(participantElement.findElement(ByTestId.testId(buttonId)))
                    .perform();

                TestUtils.waitMillis(1000);

                retries--;
                participantElement = getParticipantElement();
            }

            if (participantElement != null)
                throw new TimeoutException("Clicking " + buttonId + " did not triggered hiding the UI button.");
        }

        /**
         * Returns the knocking participant element that holds approve and deny buttons.
         * @return the element with approve/deny buttons.
         */
        private WebElement getParticipantElement()
        {
            List<WebElement> listElements = participant.getDriver().findElements(By.xpath(PARTICIPANTS_XPATH));

            return listElements.stream().filter(participant -> {
                String name = participant.findElement(ByTestId.testId(PARTICIPANT_NAME_TEST_ID)).getText();

                if (name != null && name.equals(this.name))
                    return true;

                return false;
            }).findAny().orElse(null);
        }
    }
}
