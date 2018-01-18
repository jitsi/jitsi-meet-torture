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

import org.jitsi.meet.test.util.*;

import java.util.*;

/**
 * Represents the chat panel in a particular {@link WebParticipant}.
 *
 * @author Boris Grozev
 */
public class ChatPanel
{
    /**
     * The participant.
     */
    private final WebParticipant participant;

    /**
     * Initializes a new {@link ChatPanel} instance.
     * @param participant the participant for this {@link ChatPanel}.
     */
    ChatPanel(WebParticipant participant)
    {
        this.participant = Objects.requireNonNull(participant, "participant");
    }

    /**
     * @return {@code true} if the chat panel is open/visible and {@code false}
     * otherwise.
     */
    public boolean isOpen()
    {
        Object o = participant.executeScript("return APP.UI.isChatVisible();");
        return o != null && Boolean.parseBoolean(o.toString());
    }

    /**
     * Clicks on the "chat" toolbar button which opens or closes the chat panel.
     */
    public void clickToolbarButton()
    {
        MeetUIUtils.clickOnToolbarButton(
            participant.getDriver(),
            "toolbar_button_chat",
            false);
    }

    /**
     * Presses the "chat" keyboard shortcut which opens or closes the chat
     * panel.
     */
    public void pressShortcut()
    {
        participant.pressShortcut('c');
    }
}
