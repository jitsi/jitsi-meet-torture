/*
 * Copyright @ 2018 Atlassian Pty Ltd
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
package org.jitsi.meet.test.web;

import org.jitsi.meet.test.base.*;
import org.jitsi.meet.test.util.*;
import org.openqa.selenium.*;

public class WebChatPanel
    implements ChatPanel
{
    private final Participant participant;

    WebChatPanel(Participant participant)
    {
        this.participant = participant;
    }

    @Override
    public boolean isOpen()
    {
        Object o =
            ((JavascriptExecutor) participant.getDriver()).executeScript(
                "return APP.UI.isChatVisible();");
        return o != null && Boolean.parseBoolean(o.toString());
    }

    @Override
    public void clickToolbarButton()
    {
        MeetUIUtils.clickOnToolbarButton(
            participant.getDriver(),
            "toolbar_button_chat",
            false);
    }

    @Override
    public void pressShortcut()
    {
        participant.pressShortcut('c');
    }
}
