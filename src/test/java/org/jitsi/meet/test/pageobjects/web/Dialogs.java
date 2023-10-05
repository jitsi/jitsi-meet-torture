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

/**
 * Manages dialogs
 */
public class Dialogs
{
    /**
     * The participant on current page.
     */
    private final WebParticipant participant;

    /**
     * The test id for the notification when a participant joins.
     */
    private static final String LEAVE_REASON_ID = "dialog.leaveReason";

    public Dialogs(WebParticipant participant)
    {
        this.participant = participant;
    }

    /**
     * Whether the given dialog is open.
     * @param testId the test id to search for.
     * @return whether the given dialog is open.
     */
    private boolean isDialogOpen(String testId)
    {
        return this.participant.getDriver().findElements(ByTestId.testId(testId)).size() > 0;
    }

    /**
     * Whether the leave reason dialog is open.
     * @return whether the leave reason dialog is open.
     */
    public boolean isLeaveReasonDialogOpen()
    {
        return isDialogOpen(LEAVE_REASON_ID);
    }
}
