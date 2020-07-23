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

/**
 * The pre join screen representation.
 */
public class PreJoinScreen
{
    /**
     * The participant used to interact with the pre join screen.
     */
    private final WebParticipant participant;

    /**
     * The testId of the join meeting button.
     */
    private final static String JOIN_BUTTON_TEST_ID = "prejoin.joinMeeting";

    public PreJoinScreen(WebParticipant participant)
    {
        this.participant = participant;
    }

    /**
     * Waits for pre join screen to load.
     */
    public void waitForLoading()
    {
        // pre join and lobby share the same id for the UI
        this.participant.getLobbyScreen().waitForLoading();
    }

    /**
     * The join button.
     * @return join button.
     */
    public WebElement getJoinButton()
    {
        return participant.getDriver().findElement(ByTestId.testId(JOIN_BUTTON_TEST_ID));
    }

}
