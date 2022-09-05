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
package org.jitsi.meet.test.pageobjects.web;

import org.jitsi.meet.test.util.*;
import org.jitsi.meet.test.web.WebParticipant;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;
import static org.junit.Assert.*;

import java.util.*;
import java.util.concurrent.TimeUnit;

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
    public ChatPanel(WebParticipant participant)
    {
        this.participant = Objects.requireNonNull(participant, "participant");
    }

    /**
     * @return {@code true} if the chat panel is open/visible and {@code false}
     * otherwise.
     */
    public boolean isOpen()
    {
        return this.participant.getDriver()
            .findElement(By.id("sideToolbarContainer"))
            .isDisplayed();
    }

    /**
     * Returns the chat element after waiting for 3 seconds or null if not opened.
     * @return {@code WebElement}
     */
    public WebElement getChat()
    {
        try
        {
            return new WebDriverWait(this.participant.getDriver(), 3)
                .until(driver -> driver.findElement(By.className("slideInExt")));
        }
        catch(Exception e)
        {
            return null;
        }
    }

    /**
     * Clicks on the "chat" toolbar button which opens or closes the chat panel.
     */
    public void clickToolbarButton()
    {
        MeetUIUtils.clickOnButton(
            participant.getDriver(),
            "toolbar_button_chat",
            true);
    }

    /**
     * Presses the "chat" keyboard shortcut which opens or closes the chat
     * panel.
     */
    public void pressShortcut()
    {
        participant.pressShortcut('c');
    }

    /**
     * Fails if the chat panel doesn't reach the open state withing a timeout.
     */
    public void assertOpen()
    {
        assertNotNull(getChat());
    }

    /**
     * Fails if the chat panel doesn't reach the closed state withing a timeout.
     */
    public void assertClosed()
    {

        //participant.waitForCondition(this::isChatClosed, 3, "Chat panel didnot closed" );
        assertNull(getChat());
    }

    private boolean isChatClosed() {
        return getChat() == null;
    }
}
