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
package com.bipmeet.test;

import org.apache.tools.ant.taskdefs.Sleep;
import org.jitsi.meet.test.ChatPanelTest;
import org.jitsi.meet.test.pageobjects.web.ChatPanel;
import org.jitsi.meet.test.pageobjects.web.Toolbar;
import org.jitsi.meet.test.web.WebParticipant;
import org.jitsi.meet.test.web.WebTestBase;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.Test;

import java.util.concurrent.TimeUnit;

/**
 * Checks that the chat panel can be opened and closed with a shortcut and
 * with the toolbar button.
 *
 * TODO: Add tests for sending/receiving messages.
 *
 * @author Boris Grozev
 */
public class BMChatPanelTest extends ChatPanelTest {
    /**
     * {@inheritDoc}
     */
    @Override
    public void setupClass()
    {
        super.setupClass();

        ensureOneParticipant();
    }

    /**
     * Opens and closes the chat panel with the toolbar button and with the
     * keyboard shortcut, and checks that the open/closed state is correct.
     */
    @Test
    public void testChatPanel()
    {
        WebParticipant participant = getParticipant1();

        // The chat panel requires a display name to be set.
        ChatPanel chatPanel = participant.getChatPanel();
        Toolbar toolbar = participant.getToolbar();

       /* participant.setDisplayName("bla");

        toolbar.clickOverflowButton();*/


        chatPanel.assertClosed();

        // The chat panel should be open after clicking the button
        toolbar.clickChatButton();
        chatPanel.assertOpen();

        // The chat panel should be closed after pressing the shortcut
        toolbar.bipmeetLoseFocus();
        chatPanel.pressShortcut();
        chatPanel.assertClosed();

        // The chat panel should be open after pressing the shortcut
        chatPanel.pressShortcut();
        chatPanel.assertOpen();

        // The chat panel should be closed after clicking the button
        toolbar.clickChatButton();
        chatPanel.assertClosed();
    }
}
