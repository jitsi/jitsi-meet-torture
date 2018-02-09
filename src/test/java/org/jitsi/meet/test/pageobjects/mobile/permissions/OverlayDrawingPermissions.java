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

import org.jitsi.meet.test.mobile.*;

import org.jitsi.meet.test.pageobjects.mobile.base.*;
import org.openqa.selenium.*;
import org.openqa.selenium.support.*;

/**
 * Page object for the settings screen on Android which asks for drawing overlay
 * permissions defined by the system
 * com.android.settings.Settings$AppDrawOverlaySettingsActivity
 * activity.
 */
public class OverlayDrawingPermissions extends AbstractMobilePage
{
    /**
     * The allow switch.
     */
    @FindBy(
        how = How.XPATH,
        using = "//android.widget.Switch"
    )
    private WebElement allowButton;

    /**
     * The allow button's (it's a switch technically) label.
     */
    @FindBy(
        how = How.XPATH,
        using = "//android.widget.TextView"
                    + "[@text='Permit drawing over other apps']")
    private WebElement permitDescription;

    /**
     * Create new <tt>OverlayDrawingPermissions</tt>.
     *
     * @param participant <tt>MobileParticipant</tt> instance.
     */
    public OverlayDrawingPermissions(MobileParticipant participant)
    {
        super(participant);
    }

    /**
     * The allow button.
     *
     * @return a <tt>WebElement</tt> proxy object.
     */
    public WebElement getAllowSwitch()
    {
        return allowButton;
    }

    /**
     * The permission description label.
     *
     * @return a <tt>WebElement</tt> proxy object.
     */
    public WebElement getPermitDescription()
    {
        return permitDescription;
    }
}
