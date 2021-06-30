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

import org.jitsi.meet.test.web.*;
import org.testng.annotations.*;
import org.openqa.selenium.*;

import static org.testng.Assert.*;

/**
 * Checks resulting embed code integrity
 * *
 * @author Tudor Avram
 */
public class EmbedMeetingTest
    extends WebTestBase
{
    private static String EMBED_MEETING_TEXTAREA = "embed-meeting-code";

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
    public void testEmbedMeetingCode()
    {
        WebParticipant participant = getParticipant1();
        WebDriver driver = participant.getDriver();
        
        participant.getToolbar().clickEmbedMeetingButton();

        WebElement textArea = driver.findElement(
            By.className(EMBED_MEETING_TEXTAREA));

        
        assertEquals(textArea.getAttribute("innerHTML"), "&lt;iframe allow=\"camera; microphone; display-capture\" src=\"" + driver.getCurrentUrl() + "\" allowfullscreen=\"true\" style=\"height: 100%; width: 100%; border: 0px;\"&gt;&lt;/iframe&gt;");
    }
}
