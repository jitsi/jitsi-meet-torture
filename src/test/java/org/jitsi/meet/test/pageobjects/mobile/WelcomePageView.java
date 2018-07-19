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
 * Page object for the mobile welcome page.
 */
public class WelcomePageView extends AbstractMobilePage
{
    /**
     * Conference room name input locator.
     */
    @AndroidFindBy(accessibility = "Enter room name")
    @iOSFindBy(accessibility = "Enter room name")
    private MobileElement roomNameInput;

    /**
     * Join conference room button.
     */
    @AndroidFindBy(accessibility = "Tap to join")
    @iOSFindBy(accessibility = "Tap to join")
    private MobileElement joinRoomButton;

    /**
     * Creates new <tt>WelcomePageView</tt>.
     *
     * @param participant <tt>MobileParticipant</tt> instance.
     */
    public WelcomePageView(MobileParticipant participant)
    {
        super(participant);
    }

    /**
     * Conference room input field.
     *
     * @return <tt>MobileElement</tt> proxy object.
     */
    public MobileElement getRoomNameInput()
    {
        return roomNameInput;
    }

    /**
     * Join conference room button.
     *
     * @return <tt>MobileElement</tt> proxy object.
     */
    public MobileElement getJoinRoomButton()
    {
        return joinRoomButton;
    }
}
