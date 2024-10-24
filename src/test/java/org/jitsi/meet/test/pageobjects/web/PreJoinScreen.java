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

import java.time.*;

/**
 * The pre join screen representation.
 */
public class PreJoinScreen
    extends ParentPreMeetingScreen
{
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

    /**
     * The testId of the display name input.
     */
    private final static String DISPLAY_NAME_TEST_ID = "premeeting-name-input";


    public PreJoinScreen(WebParticipant participant)
    {
        super(participant);
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

    /**
     * The input for display name.
     * @return input for display name.
     */
    public WebElement getDisplayNameInput()
    {
        return participant.getDriver().findElement(By.id(DISPLAY_NAME_TEST_ID));
    }

    @Override
    protected String getScreenTestId()
    {
        return PREJOIN_SCREEN_TEST_ID;
    }

    @Override
    public String getJoinButtonTestId()
    {
        return JOIN_BUTTON_TEST_ID;
    }
}
