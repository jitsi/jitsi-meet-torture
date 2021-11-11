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

import org.jitsi.meet.test.pageobjects.base.*;
import org.jitsi.meet.test.util.*;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.*;

/**
 * The web implementation of <tt>RemoteParticipant/tt>.
 *
 * @author Damian Minkov
 */
public class WebRemoteParticipant
    extends RemoteParticipant<WebDriver>
{
    /**
     * Constructs <tt>WebRemoteParticipant</tt>.
     * @param driver the driver.
     * @param endpointId the endpoint id.
     */
    public WebRemoteParticipant(WebDriver driver, String endpointId)
    {
        super(driver, endpointId);
    }

    @Override
    public void grantModerator()
    {
        clickOnRemoteMenuLink("grantmoderatorlink", true);
    }

    @Override
    public void mute()
    {
        clickOnRemoteMenuLink("mutelink", false);
    }

    @Override
    public void kick()
    {
        clickOnRemoteMenuLink("kicklink", true);
    }

    @Override
    public void stopVideo()
    {
        clickOnRemoteMenuLink("mutevideolink", true);
    }

    /**
     * Opens the remote menu and waits for the desired link to appear.
     * @param linkClassname the class name to use identifying the link.
     * @param dialogConfirm whether or not the action opens a confirmation dialog
     */
    private void clickOnRemoteMenuLink(String linkClassname, Boolean dialogConfirm)
    {
        WebElement thumbnail = driver.findElement(By.xpath("//span[@id='participant_" + this.getEndpointId()+ "']"));
        Actions action1 = new Actions(driver);
        action1.moveToElement(thumbnail);
        action1.perform();

        WebElement menuElement = driver.findElement(
            By.xpath("//span[@id='participant_" + this.getEndpointId()
                    + "']//span[@id='remotevideomenu']"));

        Actions action = new Actions(driver);
        action.moveToElement(menuElement);
        action.perform();

        // give time for the menu to appear
        TestUtils.waitForDisplayedElementByXPath(
            driver,
            "//div[@class='popover']//div[contains(@class, '"
                + linkClassname + "')]",
            5);

        // click the button
        MeetUIUtils.clickOnElement(driver,
            "div.popover div." + linkClassname, true);

        if (dialogConfirm)
        {
            // confirm the action
            ModalDialogHelper.clickOKButton(driver);
        }

        action.release();
    }
}
