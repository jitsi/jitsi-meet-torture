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

import java.time.*;

/**
 * The Lobby screen representation.
 */
public class LobbyScreen
    extends ParentPreMeetingScreen
{
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

    public LobbyScreen(WebParticipant participant)
    {
        super(participant);
    }

    /**
     * The input for display name.
     * @return input for display name.
     */
    public WebElement getDisplayNameInput()
    {
        return participant.getDriver().findElement(ByTestId.testId(DISPLAY_NAME_TEST_ID));
    }

    @Override
    protected String getScreenTestId()
    {
        return LOBBY_SCREEN_ID;
    }

    @Override
    public String getJoinButtonTestId()
    {
        return JOIN_BUTTON_TEST_ID;
    }

    /**
     * Waits for lobby screen to load.
     */
    @Override
    public void waitForLoading()
    {
        // we wait for LOBBY_SCREEN_ID to successfully appear
        new WebDriverWait(this.participant.getDriver(), Duration.ofSeconds(5)).until(
                (ExpectedCondition<Boolean>) d -> d.findElements(By.className(LOBBY_SCREEN_ID)).size() > 0);
    }
}
