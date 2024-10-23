/*
 * Copyright @ 2024 8x8 Pty Ltd
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
import org.openqa.selenium.support.ui.*;

import java.time.*;
import java.util.*;

public abstract class ParentPreMeetingScreen
{
    /**
     * JS script to check whether lobby room is joined or not.
     */
    private static final String CHECK_LOBBY_JOINED_SCRIPT
            = "return APP.conference._room?.room?.getLobby()?.lobbyRoom?.joined == true;";

    /**
     * The testId of the join meeting button.
     */
    private final static String PASSWORD_BUTTON_TEST_ID = "lobby.enterPasswordButton";

    /**
     * The participant used to interact with the lobby screen.
     */
    protected final WebParticipant participant;

    public ParentPreMeetingScreen(WebParticipant participant)
    {
        this.participant = participant;
    }

    protected abstract String getScreenTestId();

    /**
     * Waits for pre join screen to load.
     */
    public void waitForLoading()
    {
        new WebDriverWait(this.participant.getDriver(), Duration.ofSeconds(3)).until(
                (ExpectedCondition<Boolean>) d -> !d.findElements(ByTestId.testId(getScreenTestId())).isEmpty());
    }

    /**
     * Checks internally whether lobby room is joined.
     * @return true if lobby room is joined.
     */
    public boolean isLobbyRoomJoined()
    {
        return TestUtils.executeScriptAndReturnBoolean(this.participant.getDriver(), CHECK_LOBBY_JOINED_SCRIPT);
    }

    protected abstract String getJoinButtonTestId();

    /**
     * The join button.
     * @return join button.
     */
    public WebElement getJoinButton()
    {
        return participant.getDriver().findElement(ByTestId.testId(getJoinButtonTestId()));
    }

    public abstract WebElement getDisplayNameInput();

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
     * Returns the password button.
     * @return the password button.
     */
    public WebElement getPasswordButton()
    {
        return participant.getDriver().findElement(ByTestId.testId(PASSWORD_BUTTON_TEST_ID));
    }

    /**
     * Interacts with the view to enter a password.
     */
    public void enterPassword(String password)
    {
        WebElement passwordButton = getPasswordButton();

        new Actions(this.participant.getDriver()).moveToElement(passwordButton).click().perform();

        new WebDriverWait(this.participant.getDriver(), Duration.ofSeconds(3)).until(
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
     * Is join button available.
     * @return <tt>true</tt> if join button is present on the page.
     */
    public boolean hasJoinButton()
    {
        List<WebElement> joinButton = this.participant.getDriver().findElements(ByTestId.testId(getJoinButtonTestId()));
        return !joinButton.isEmpty() && !joinButton.get(0).isEnabled();
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
}
