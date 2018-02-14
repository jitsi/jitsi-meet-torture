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

import org.jitsi.meet.test.mobile.*;
import org.jitsi.meet.test.pageobjects.mobile.base.*;

/**
 * Page object for the conference view on mobile.
 */
public class ConferenceView extends AbstractMobilePage
{
    /**
     * Root element with "Conference" accessibility label.
     */
    @AndroidFindBy(accessibility = "Conference")
    @iOSFindBy(accessibility = "Conference")
    private MobileElement conference;

    private ToolbarView toolbar;

    /**
     * Creates new <tt>{@link ConferenceView}</tt>.
     *
     * @param mobileParticipant <tt>MobileParticipant</tt> instance.
     */
    public ConferenceView(MobileParticipant mobileParticipant)
    {
        super(mobileParticipant);
    }

    MobileParticipant getParticipant()
    {
        return participant;
    }

    /**
     * Tries to obtain root container of the conference view.
     *
     * @return a <tt>MobileElement</tt> proxy object.
     */
    public MobileElement getRootView()
    {
        return conference;
    }

    /**
     * @return the {@link ToolbarView}.
     */
    public ToolbarView getToolbarView()
    {
        if (toolbar == null)
        {
            toolbar = new ToolbarView(this);
        }

        return toolbar;
    }
}
