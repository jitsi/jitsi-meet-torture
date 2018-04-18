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

import org.jitsi.meet.test.mobile.*;
import org.jitsi.meet.test.pageobjects.base.*;
import org.jitsi.meet.test.pageobjects.mobile.base.*;

/**
 * Page object for the conference view on mobile.
 */
public class ConferenceView extends AbstractMobilePage
{
    /**
     * The conference name test hint.
     */
    @TestHintLocator(id = "org.jitsi.meet.conference.name")
    private TestHint conferenceName;

    /**
     * The large video test hint.
     */
    @TestHintLocator(id = "org.jitsi.meet.LargeVideo")
    private TestHint largeVideo;

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

    public String getName()
    {
        return conferenceName.getValue();
    }

    MobileParticipant getParticipant()
    {
        return participant;
    }

    /**
     * Gets the large video {@link TestHint}.
     *
     * @return a {@link TestHint} instance for the "large video" element.
     */
    public TestHint getLargeVideo()
    {
        return largeVideo;
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
