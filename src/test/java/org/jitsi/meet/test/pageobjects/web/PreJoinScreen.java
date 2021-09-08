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
import org.openqa.selenium.support.ui.*;

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
    private final static String PREJOIN_SCREEN_TEST_ID = "prejoin.screen";

    /**
     * The testId of the join meeting button.
     */
    private final static String JOIN_BUTTON_TEST_ID = "prejoin.joinMeeting";

    /**
     * The testId of the join meeting button.
     */
    private final static String ERROR_ON_JOIN = "prejoin.errorMessage";

    /**
     * The testId of the join options button.
     */
    private final static String OPTIONS_BUTTON = "prejoin.joinOptions";

    /**
     * The testId of the join without audio button.
     */
    private final static String JOIN_WITHOUT_AUDIO = "prejoin.joinWithoutAudio";

    public PreJoinScreen(WebParticipant participant)
    {
        this.participant = participant;
    }

    /**
     * Waits for pre join screen to load.
     */
    public void waitForLoading()
    {
        new WebDriverWait(this.participant.getDriver(), 3).until(
            (ExpectedCondition<Boolean>) d -> d.findElements(ByTestId.testId(PREJOIN_SCREEN_TEST_ID)).size() > 0);
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
     * The error displayed when joining without name if name is required.
     * @return error element.
     */
    public WebElement getErrorOnJoin()
    {
        return participant.getDriver().findElement(ByTestId.testId(ERROR_ON_JOIN));
    }

  /**
     * The join options button.
     * @return option button.
     */
    public WebElement getJoinOptions()
    {
        return participant.getDriver().findElement(ByTestId.testId(OPTIONS_BUTTON));
    }

    /**
     * The 'joinWithoutAudio' button.
     * @return join without audio button
     */
    public WebElement getJoinWithoutAudioButton()
    {
        return participant.getDriver().findElement(ByTestId.testId(JOIN_WITHOUT_AUDIO));
    }
}
