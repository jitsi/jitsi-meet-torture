/*
 * Copyright @ Atlassian Pty Ltd
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
package org.jitsi.meet.test.pageobjects.mobile;

import io.appium.java_client.*;
import io.appium.java_client.pagefactory.*;

import org.jitsi.meet.test.pageobjects.mobile.base.*;

/**
 * Page object for the mobile toolbar area.
 *
 * @author Pawel Domas
 */
public class ToolbarView extends AbstractMobilePage
{
    private static final String HANGUP_BTN_ACCESS_ID = "Leave the call";

    private final ConferenceView conferenceView;

    /**
     * A locator for the hangup button.
     */
    @AndroidFindBy(accessibility = HANGUP_BTN_ACCESS_ID)
    @iOSFindBy(accessibility = HANGUP_BTN_ACCESS_ID)
    private MobileElement hangup;

    /**
     * Creates new <tt>ToolbarView</tt>.
     *
     * @param conferenceView - A {@link ConferenceView} in which this toolbar is
     * located.
     */
    public ToolbarView(ConferenceView conferenceView)
    {
        super(conferenceView.getParticipant());

        this.conferenceView = conferenceView;
    }

    /**
     * Checks if the toolbar is currently open.
     *
     * @return <tt>true</tt> if open.
     */
    public boolean isOpen()
    {
        return !participant.getDriver()
                .findElementsByAccessibilityId(HANGUP_BTN_ACCESS_ID)
                .isEmpty();
    }

    /**
     * The hangup button.
     *
     * @return <tt>MobileElement</tt> proxy object.
     */
    public MobileElement getHangupButton()
    {
        return hangup;
    }

    /**
     * Opens the toolbar if it's closed.
     */
    public void open()
    {
        if (!isOpen())
        {
            conferenceView.getLargeVideo().click();
        }
    }
}
