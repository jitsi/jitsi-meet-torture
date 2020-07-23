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
     * The test id of the notification that a someone's access was denied.
     */
    private static final String LOBBY_PARTICIPANT_ACCESS_DENIED_TEST_ID = "lobby.notificationLobbyAccessDenied";

    /**
     * The test id of the notification that a someone's access was approved.
     */
    private static final String LOBBY_PARTICIPANT_ACCESS_GRANTED_TEST_ID = "lobby.notificationLobbyAccessGranted";

    /**
     * The test id for the notification on participants page when meeting has ended.
     */
    private static final String SESSION_TERMINATED_TEST_ID = "dialog.sessTerminated";

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
        WebElement notification = TestUtils.waitForElementBy(this.participant.getDriver(), ByTestId.testId(testId), 3);

        return notification != null ? notification.getText() : null;
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

    /**
     * Closes a specific notification.
     * @param testId the test id for the notification to close.
     */
    private void close(String testId)
    {
        TestUtils.waitForElementBy(this.participant.getDriver(), ByTestId.testId(testId), 3);

        List<WebElement> lobbyNotifications
            = this.participant.getDriver().findElements(ByTestId.testId(LOBBY_NOTIFICATIONS_TITLE_TEST_ID));

        WebElement notification = lobbyNotifications.stream()
            .filter(webElement -> webElement.findElements(ByTestId.testId(testId)).size() > 0)
            .findFirst().orElse(null);

        if (notification != null)
        {
            WebElement closeButton = notification.findElement(By.tagName("button"));

            new Actions(this.participant.getDriver()).moveToElement(closeButton).click().perform();
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
        close(LOBBY_ENABLED_TEST_ID);
    }

    /**
     * The notification test that a someone's access was denied.
     * @return the notification test that a someone's access was denied.
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
        close(LOBBY_PARTICIPANT_ACCESS_DENIED_TEST_ID);
    }

    /**
     * The notification that a someone's access was approved.
     * @return the notification that a someone's access was approved.
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
        close(LOBBY_PARTICIPANT_ACCESS_GRANTED_TEST_ID);
    }

    /**
     * Returns whether the notification for access denied to enter lobby is shown.
     * @return whether the notification for access denied to enter lobby is shown.
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
}
