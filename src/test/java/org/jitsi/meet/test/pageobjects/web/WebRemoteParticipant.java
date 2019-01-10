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
    public void mute()
    {
        clickOnRemoteMenuLink("mutelink");
    }

    @Override
    public void kick()
    {
        clickOnRemoteMenuLink("kicklink");
    }

    /**
     * Opens the remote menu and waits for the desired link to appear.
     * @param linkClassname the class name to use identifying the link.
     */
    private void clickOnRemoteMenuLink(String linkClassname)
    {
        // Open the remote video menu
        WebElement cntElem
            = driver.findElement(By.id("participant_" + this.getEndpointId()));
        String remoteVideoMenuButtonXPath
            = TestUtils.getXPathStringForClassName(
                "//span", "remotevideomenu");
        WebElement elem
            = driver.findElement(By.xpath(remoteVideoMenuButtonXPath));

        Actions action = new Actions(driver);
        action.moveToElement(cntElem);
        action.moveToElement(elem);
        action.perform();

        // give time for the menu to appear
        TestUtils.waitForDisplayedElementByXPath(
            driver,
            "//ul[@class='popupmenu']//a[contains(@class, '"
                + linkClassname + "')]",
            5);

        // click the button
        MeetUIUtils.clickOnElement(driver,
            "ul.popupmenu a." + linkClassname, true);

        // wait for confirm muting to display
        TestUtils.waitForDisplayedElementByXPath(
            driver,
            "//button[@id='modal-dialog-ok-button']",
            5);

        // confirm muting
        MeetUIUtils.clickOnButton(driver,
            "modal-dialog-ok-button", true);

        action.release();
    }
}
