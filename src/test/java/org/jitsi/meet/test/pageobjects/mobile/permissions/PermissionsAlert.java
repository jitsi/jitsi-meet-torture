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
package org.jitsi.meet.test.pageobjects.mobile.permissions;

import io.appium.java_client.*;
import io.appium.java_client.pagefactory.*;

import org.jitsi.meet.test.mobile.*;
import org.jitsi.meet.test.pageobjects.mobile.base.*;

/**
 * This is the camera/mic permissions alert.
 *
 * FIXME Locating OK button on iOS takes a lot of time. My guess is that it uses
 * few different methods for locating the element and it succeeds with the last
 * one, after timing out few others that come before it.
 */
public class PermissionsAlert extends AbstractMobilePage
{
    @AndroidFindBy(
        id = "com.android.packageinstaller:id/permission_allow_button"
    )
    @iOSFindBy(
        id = "OK"
    )
    private MobileElement allowButton;

    /**
     * Creates new <tt>{@link PermissionsAlert}</tt>.
     * @param participant <tt>{@link MobileParticipant}</tt>
     */
    public PermissionsAlert(MobileParticipant participant)
    {
        super(participant);
    }

    /**
     * Obtains the OK/Allow button used to grant permission to use
     * camera/microphone.
     *
     * @return a <tt>{@link MobileElement}</tt> proxy object.
     */
    public MobileElement getAllowButton()
    {
        return allowButton;
    }
}
