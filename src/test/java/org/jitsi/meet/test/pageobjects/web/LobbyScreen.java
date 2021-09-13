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
import org.openqa.selenium.interactions.*;
import org.openqa.selenium.support.ui.*;

/**
 * The Lobby screen representation.
 */
public class LobbyScreen
{
    /**
     * The participant used to interact with the lobby screen.
     */
    private final WebParticipant participant;

    /**
     * The id we can use to check whether participant is in pre join screen.
     */
    private static final String LOBBY_SCREEN_ID = "lobby-screen";

    /**
     * The testId of the display name input.
     */
    private final static String DISPLAY_NAME_TEST_ID = "lobby.nameField";

    /**
     * The testId of the join meeting button.
     */
    private final static String JOIN_BUTTON_TEST_ID = "lobby.knockButton";

    /**
     * The testId of the join meeting button.
     */
    private final static String PASSWORD_BUTTON_TEST_ID = "lobby.enterPasswordButton";

    /**
     * JS script to check whether lobby room is joined or not.
     */
    private static final String CHECK_LOBBY_JOINED_SCRIPT
        = "return APP.conference._room.room.getLobby().lobbyRoom" +
            " && APP.conference._room.room.getLobby().lobbyRoom.joined;";

    public LobbyScreen(WebParticipant participant)
    {
        this.participant = participant;
    }

    /**
     * Returns the password button.
     * @return the password button.
     */
    public WebElement getPasswordButton()
    {
        return participant.getDriver().findElement(ByTestId.testId(PASSWORD_BUTTON_TEST_ID));
    }

    /**
     * The input for display name.
     * @return input for display name.
     */
    public WebElement getDisplayNameInput()
    {
        return participant.getDriver().findElement(ByTestId.testId(DISPLAY_NAME_TEST_ID));
    }

    /**
     * Interacts with the view to enter a display name.
     */
    public void enterDisplayName(String displayName)
    {
        WebElement displayNameInput = getDisplayNameInput();

        // element.clear does not always work, make sure we delete the content
        while (!displayNameInput.getAttribute("value").equals(""))
        {
            displayNameInput.sendKeys(Keys.BACK_SPACE);
        }

        displayNameInput.sendKeys(displayName);
    }

    /**
     * Interacts with the view to enter a password.
     */
    public void enterPassword(String password)
    {
        WebElement passwordButton = getPasswordButton();

        new Actions(this.participant.getDriver()).moveToElement(passwordButton).click().perform();

        new WebDriverWait(this.participant.getDriver(), 3).until(
            (ExpectedCondition<Boolean>) d ->
                d.findElements(ByTestId.testId("lobby.password")).size() > 0);

        WebElement passwordInput
            = participant.getDriver().findElement(ByTestId.testId("lobby.password"));

        passwordInput.clear();
        passwordInput.sendKeys(password);

        WebElement joinButton = participant.getDriver().findElement(ByTestId.testId("lobby.passwordJoinButton"));

        new Actions(this.participant.getDriver()).moveToElement(joinButton).click().perform();
    }

    /**
     * The join button.
     * @return join button.
     */
    public WebElement getJoinButton()
    {
        return participant.getDriver().findElement(ByTestId.testId(JOIN_BUTTON_TEST_ID));
    }

    /**
     * Is join button available.
     * @return <tt>true</tt> if join button is present on the page.
     */
    public boolean hasJoinButton()
    {
        return participant.getDriver().findElements(ByTestId.testId(JOIN_BUTTON_TEST_ID)).size() > 0;
    }

    /**
     * Clicks the join button.
     */
    public void join()
    {
        WebElement joinButton = getJoinButton();
        if (joinButton == null)
        {
            throw new NoSuchElementException("Lobby join button not found!");
        }

        new Actions(this.participant.getDriver()).moveToElement(joinButton).click().perform();
    }

    /**
     * Waits for lobby screen to load.
     */
    public void waitForLoading()
    {
        // we wait for LOBBY_SCREEN_ID to successfully appear
        new WebDriverWait(this.participant.getDriver(), 5).until(
            (ExpectedCondition<Boolean>) d -> d.findElements(By.className(LOBBY_SCREEN_ID)).size() > 0);
    }

    /**
     * Checks internally whether lobby room is joined.
     * @return true if lobby room is joined.
     */
    public boolean isLobbyRoomJoined()
    {
        return TestUtils.executeScriptAndReturnBoolean(this.participant.getDriver(), CHECK_LOBBY_JOINED_SCRIPT);
    }
}
