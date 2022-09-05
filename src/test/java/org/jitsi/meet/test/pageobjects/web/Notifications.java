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

/**
 * Gathers all notifications in the UI and obtaining those.
 */
public class Notifications
{
    /**
     * The participant on current page.
     */
    private final WebParticipant participant;

    /**
     * The test id for the notification on participants page which access was denied.
     */
    private static final String LOBBY_ACCESS_DENIED_TEST_ID = "lobby.joinRejectedMessage";

    /**
     * The test id for the notification on participants page when Lobby is being enabled or disabled.
     */
    private static final String LOBBY_ENABLED_TEST_ID = "lobby.notificationLobbyEnabled";

    /**
     * The title of all lobby notifications, used to locate the notification and its close button, in order to close it.
     */
    private static final String LOBBY_NOTIFICATIONS_TITLE_TEST_ID = "lobby.notificationTitle";

    /**
     * The test id of the notification that someone's access was denied.
     */
    private static final String LOBBY_PARTICIPANT_ACCESS_DENIED_TEST_ID = "lobby.notificationLobbyAccessDenied";

    /**
     * The test id of the notification that someone's access was approved.
     */
    private static final String LOBBY_PARTICIPANT_ACCESS_GRANTED_TEST_ID = "lobby.notificationLobbyAccessGranted";

    /**
     * The test id of the button in the notification to approve access.
     */
    private static final String LOBBY_PARTICIPANT_ADMIT_TEST_ID = "lobby.admit";

    /**
     * The test id of the button in the notification to reject access.
     */
    private static final String LOBBY_PARTICIPANT_REJECT_TEST_ID = "lobby.reject";

    /**
     * The xpath to find the notification for single knocking participant.
     */
    private static final String LOBBY_KNOCKING_PARTICIPANT_NOTIFICATION_XPATH
        = "//div[@data-testid='notify.participantWantsToJoin']//div[not(contains(@class,'participant'))]/span";

    /**
     * The test id for the notification on participants page when meeting has ended.
     */
    private static final String SESSION_TERMINATED_TEST_ID = "dialog.sessTerminated";

    /**
     * The test id of the notification shown to kicked users.
     */
    private static final String KICKED_NOTIFICATION_TEST_ID = "dialog.kickTitle";

    /**
     * The test id of the notification shown to moderator when participant wants to speak.
     */
    private static final String RAISE_HAND_NOTIFICATION_ID = "notify.raisedHand";

    /**
     * The test id of the notification shown to participant when moderator asks to unmute.
     */
    private static final String ASK_TO_UNMUTE_NOTIFICATION_ID = "notify.hostAskedUnmute";

    /**
     * The test id of the notification shown to participant when the self view is hidden.
     */
    private static final String REENABLE_SELF_VIEW_NOTIFICATION_ID = "notify.selfViewTitle";

    /**
     * The id of the close button of the notification shown to participant when the self view is hidden.
     */
    private static final String REENABLE_SELF_VIEW_CLOSE_NOTIFICATION = "notify.selfViewTitle-dismiss";

    /**
     * The id of the close button of the notification shown to participant when moderator asks to unmute.
     */
    private static final String ASK_TO_UNMUTE_CLOSE_NOTIFICATION = "notify.hostAskedUnmute-dismiss";

    /**
     * The id of the close button of the notification shown to participant when moderator mutes them.
     */
    private static final String REMOTELY_MUTED_CLOSE_NOTIFICATION = "notify.mutedRemotelyTitle-dismiss";

    /**
     * The id of the close button of the notification shown to participant when moderator video mutes them.
     */
    private static final String REMOTELY_VIDEO_MUTED_CLOSE_NOTIFICATION = "notify.videoMutedRemotelyTitle-dismiss";

    private static final String BIPMEET_KICK_NOTIFICATION = "modal-dialog-form";

    public Notifications(WebParticipant participant)
    {
        this.participant = participant;
    }

    /**
     * Returns notification text if the notification is found in the next few seconds.
     * @return the notification text.
     */
    private String getNotificationText(String testId)
    {
        return TestUtils.waitForElementTextBy(this.participant.getDriver(), ByTestId.testId(testId), 3);
    }

    /**
     * Whether a notification exists on the page.
     * @param testId the test id to search for.
     * @return whether a notification exists on the page.
     */
    private boolean hasNotification(String testId)
    {
        return this.participant.getDriver().findElements(ByTestId.testId(testId)).size() > 0;
    }

    private boolean bipMeetKickNotification(String id)
    {
        return this.participant.getDriver().findElements(By.id(id)).size() > 0;
    }

    /**
     * Returns the notification if found or null.
     * @param testId the testId to search for.
     * @return the web element found or null.
     */
    private WebElement getLobbyNotificationByTestId(String testId)
    {
        WebDriver driver = this.participant.getDriver();
        List<WebElement> lobbyNotifications = driver.findElements(ByTestId.testId(LOBBY_NOTIFICATIONS_TITLE_TEST_ID));

        return lobbyNotifications.stream()
            .filter(webElement -> webElement.findElements(ByTestId.testId(testId)).size() > 0)
            .findFirst().orElse(null);
    }

    /**
     * Closes a specific lobby notification.
     * @param testId the test id for the notification to close.
     */
    private void closeLobbyNotification(String testId)
    {
        WebDriver driver = this.participant.getDriver();
        TestUtils.waitForElementBy(driver, ByTestId.testId(testId), 3);

        WebElement notification = getLobbyNotificationByTestId(testId);

        if (notification != null)
        {
            try
            {
                // wait for the element to be available (notification maybe still animating)
                TestUtils.waitForCondition(driver, 2, d ->
                    !notification.findElements(By.tagName("button")).isEmpty());

                WebElement closeButton = notification.findElement(By.tagName("button"));

                new Actions(driver).moveToElement(closeButton).click().perform();
            }
            catch(StaleElementReferenceException ex)
            {
                // retries closing the notification
                WebElement updatedNotification = getLobbyNotificationByTestId(testId);
                WebElement closeButton = updatedNotification.findElement(By.tagName("button"));
                new Actions(driver).moveToElement(closeButton).click().perform();
            }
        }
        else
        {
            throw new NoSuchElementException("Notification not found:" + testId);
        }
    }

    /**
     * The notification on participants page when Lobby is being enabled or disabled.
     * @return the lobby enable/disable notification.
     */
    public String getLobbyEnabledText()
    {
        return getNotificationText(LOBBY_ENABLED_TEST_ID);
    }

    /**
     * Closes the notification.
     */
    public void closeLobbyEnabled()
    {
        closeLobbyNotification(LOBBY_ENABLED_TEST_ID);
    }

    /**
     * The notification test that someone's access was denied.
     * @return the notification test that someone's access was denied.
     */
    public String getLobbyParticipantAccessDenied()
    {
        return getNotificationText(LOBBY_PARTICIPANT_ACCESS_DENIED_TEST_ID);
    }

    /**
     * Closes the notification.
     */
    public void closeLobbyParticipantAccessDenied()
    {
        closeLobbyNotification(LOBBY_PARTICIPANT_ACCESS_DENIED_TEST_ID);
    }

    /**
     * The notification that someone's access was approved.
     * @return the notification that someone's access was approved.
     */
    public String getLobbyParticipantAccessGranted()
    {
        return getNotificationText(LOBBY_PARTICIPANT_ACCESS_GRANTED_TEST_ID);
    }

    /**
     * Closes the notification.
     */
    public void closeLobbyParticipantAccessGranted()
    {
        closeLobbyNotification(LOBBY_PARTICIPANT_ACCESS_GRANTED_TEST_ID);
    }

    /**
     * Returns whether the notification for access denied for entering the lobby is shown.
     * @return whether the notification for access denied for entering the lobby is shown.
     */
    public boolean hasLobbyAccessDenied()
    {
        return hasNotification(LOBBY_ACCESS_DENIED_TEST_ID);
    }

    /**
     * The notification on participants page when meeting has ended.
     * @return the notification on participants page when meeting has ended.
     */
    public boolean hasMeetingEndedNotification()
    {
        return hasNotification(SESSION_TERMINATED_TEST_ID);
    }

    /**
     * The notification on participants page when the participant was kicked.
     * @return the notification on participants page when the participant was kicked.
     */
    public boolean hasKickedNotification()
    {
        return hasNotification(KICKED_NOTIFICATION_TEST_ID);
    }
    public boolean bipmeetHasKickedNotification()
    {
        return bipMeetKickNotification(BIPMEET_KICK_NOTIFICATION);
    }
    /**
     * The notification on moderators page when the participant tries to unmute.
     * @return the notification on moderators page when the participant wants to unmute.
     */
    public String getRaisedHandNotification()
    {
        return getNotificationText(RAISE_HAND_NOTIFICATION_ID);
    }

    /**
     * Whether there is a notification on participants page for raised hand.
     * @return <tt>true</tt> if the notification is found.
     */
    public boolean hasRaisedHandNotification()
    {
        return hasNotification(RAISE_HAND_NOTIFICATION_ID);
    }

    /**
     * The notification on participants page when the moderator asks to unmute.
     * @return the notification on participants page when the moderator asks to unmute.
     */
    public String getAskToUnmuteNotification()
    {
        return getNotificationText(ASK_TO_UNMUTE_NOTIFICATION_ID);
    }

    /**
     * The notification on participants page when the moderator asks to unmute.
     * @return the notification on participants page when the moderator asks to unmute.
     */
    public boolean hasAskToUnmuteNotification()
    {
        return hasNotification(ASK_TO_UNMUTE_NOTIFICATION_ID);
    }

    /**
     * The notification on participants page when the self view is hidden.
     * @return the notification on participants page when the self view is hidden.
     */
    public boolean hasReenableSelfViewNotification()
    {
        return hasNotification(REENABLE_SELF_VIEW_NOTIFICATION_ID);
    }

    /**
     * Closes a notification by specifying a testId.
     * @param testId the testId for the notification to close.
     */
    private void closeNotificationByTestId(String testId)
    {
        try
        {
            WebDriver driver = participant.getDriver();
            TestUtils.waitForCondition(driver, 5, d -> !d.findElements(ByTestId.testId(testId)).isEmpty());

            // let's give time for the animation, or we will miss the button click for closings
            TestUtils.waitMillis(200);

            WebElement closeButton = driver.findElement(ByTestId.testId(testId));

            try
            {
                new Actions(driver).moveToElement(closeButton).click().perform();
            }
            catch(StaleElementReferenceException ex)
            {
                // let's try again
                closeButton = driver.findElement(ByTestId.testId(testId));
                new Actions(driver).moveToElement(closeButton).click().perform();
            }

            TestUtils.waitForCondition(driver, 2, d ->
                d.findElements(ByTestId.testId(testId)).isEmpty());
        }
        catch(TimeoutException ex)
        {
            // if notification is not found it is closed
            TestUtils.print("No notification found for:" + testId + " exception:" + ex.getMessage());
        }
    }

    /**
     * Closes the notification for the self view is hidden.
     */
    public void closeReeanbleSelfViewNotification()
    {
        closeNotificationByTestId(REENABLE_SELF_VIEW_CLOSE_NOTIFICATION);
    }

    /**
     * Closes the notification for moderator asks you to unmute.
     */
    public void closeAskToUnmuteNotification()
    {
        closeNotificationByTestId(ASK_TO_UNMUTE_CLOSE_NOTIFICATION);
    }

    /**
     * Closes the notification for moderator remote muting.
     */
    public void closeRemoteMuteNotification()
    {
        closeNotificationByTestId(REMOTELY_MUTED_CLOSE_NOTIFICATION);
    }

    /**
     * Closes the notification for moderator remote muting.
     */
    public void closeRemoteVideoMuteNotification()
    {
        closeNotificationByTestId(REMOTELY_VIDEO_MUTED_CLOSE_NOTIFICATION);
    }

    /**
     * Returns the name of the knocking participant (the only one) that is displayed on the notification.
     * @return the name of the only participant waiting in lobby.
     */
    public String getKnockingParticipantName()
    {
        TestUtils.waitForDisplayedElementByXPath(
            participant.getDriver(), LOBBY_KNOCKING_PARTICIPANT_NOTIFICATION_XPATH, 3);

        return participant.getDriver().findElement(By.xpath(LOBBY_KNOCKING_PARTICIPANT_NOTIFICATION_XPATH)).getText();
    }

    /**
     * Will wait 3 seconds for the knocking participants to disappear and return true or will return false.
     * @return <tt>true</tt> if the knocking participants list was not displayed.
     */
    public boolean waitForHideOfKnockingParticipants()
    {
        try
        {
            TestUtils.waitForNotDisplayedElementByXPath(
                participant.getDriver(), LOBBY_KNOCKING_PARTICIPANT_NOTIFICATION_XPATH, 3);

            return true;
        }
        catch(TimeoutException ex)
        {
            return false;
        }
    }

    /**
     * Rejects the last knocking participant (it is the only one).
     * @param name - Not used at the moment. If we have a list of notifications for we can use the name to select
     * the appropriate participant to reject.
     */
    public void rejectLobbyParticipant(String name)
    {
        participant.getDriver().findElement(ByTestId.testId(LOBBY_PARTICIPANT_REJECT_TEST_ID)).click();
    }

    /**
     * Admits the last knocking participant (it is the only one).
     * @param name - Not used at the moment. If we have a list of notifications for we can use the name to select
     * the appropriate participant to allow.
     */
    public void allowLobbyParticipant(String name)
    {
        participant.getDriver().findElement(ByTestId.testId(LOBBY_PARTICIPANT_ADMIT_TEST_ID)).click();
    }
}
