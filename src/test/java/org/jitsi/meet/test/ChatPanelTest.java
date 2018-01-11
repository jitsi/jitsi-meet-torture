/*
 * Copyright @ 2015 Atlassian Pty Ltd
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
package org.jitsi.meet.test;

import org.jitsi.meet.test.base.*;
import org.testng.annotations.*;

import static org.testng.Assert.*;

/**
 * Checks that the chat panel can be opened and closed with a shortcut and
 * with the toolbar button.
 *
 * TODO: Add tests for sending/receiving messages.
 *
 * @author Boris Grozev
 */
public class ChatPanelTest
    extends AbstractBaseTest
{
    Participant participant;

    @Override
    public void setup()
    {
        super.setup();

        ensureOneParticipant();
        participant = getParticipant1();
    }

    /**
     * Asserts that 'owner' and 'secondParticipant' are connected via UDP.
     */
    @Test
    public void testChatPanel()
    {
        // The chat panel requires a display name to be set.
        participant.setDisplayName("bla");

        ChatPanel chatPanel = participant.getChatPanel();

        assertFalse(
            chatPanel.isOpen(),
            "The chat panel should be initially closed");

        chatPanel.clickToolbarButton();
        assertTrue(
            chatPanel.isOpen(),
            "The chat panel should be open after clicking the button");

        chatPanel.pressShortcut();
        assertFalse(
            chatPanel.isOpen(),
            "The chat panel should be closed after pressing the shortcut");

        chatPanel.pressShortcut();
        assertTrue(
            chatPanel.isOpen(),
            "The chat panel should be open after pressing the shortcut");

        chatPanel.clickToolbarButton();
        assertFalse(
            chatPanel.isOpen(),
            "The chat panel should be closed after clicking the button");
    }
}
