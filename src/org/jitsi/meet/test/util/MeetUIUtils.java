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
package org.jitsi.meet.test.util;

import org.openqa.selenium.*;

import static org.junit.Assert.fail;

/**
 * Class contains utility operations specific to jitsi-meet user interface.
 *
 * @author Pawel Domas
 * @author Damian Minkov
 */
public class MeetUIUtils
{
    /**
     * First shows the toolbar then clicks the button.
     * @param driver the page
     * @param buttonID the id of the button to click.
     */
    public static void clickOnToolbarButton(WebDriver driver, String buttonID)
    {
        driver.findElement(
            By.xpath("//a[@class='button']/i[@id='" + buttonID + "']")).click();
    }

    /**
     * First shows the toolbar then clicks the button.
     * @param driver the page
     * @param buttonClass the class of the button to click.
     */
    public static void clickOnToolbarButtonByClass(WebDriver driver,
                                                   String buttonClass)
    {
        driver.findElement(
            By.xpath("//a[@class='button']/i[@class='" + buttonClass + "']"))
            .click();
    }

    /**
     * Returns resource JIUD of the user who is currently displayed on large
     * video area from given <tt>participant</tt> perspective.
     *
     * @param participant <tt>WebDriver</tt> of the participant for whom the
     *                    resource of large video will be extracted.
     */
    public static String getLargeVideoResource(WebDriver participant)
    {
        return TestUtils.getResourceFromJid(
            (String)((JavascriptExecutor) participant)
                .executeScript("return APP.UI.getLargeVideoJid();"));
    }

    /**
     * Makes sure that settings panel is displayed. If not then opens it.
     *
     * @param user <tt>WebDriver</tt> instance of the participant for whom we'll
     *             try to open the settings panel.
     *
     * @throws TimeoutException if we fail to open the settings panel
     */
    public static void makeSureSettingsAreDisplayed(WebDriver user)
    {
        String settingsXPath = "//div[@id='settingsmenu']";
        WebElement settings = user.findElement(By.xpath(settingsXPath));
        if (!settings.isDisplayed())
        {
            clickOnToolbarButton(user, "settingsButton");

            TestUtils.waitsForDisplayedElementByXPath(
                user, settingsXPath, 5);
        }
    }

    /**
     * Verifies if given peer is muted from other peer's perspective. Will fail
     * the test if the verification is negative.
     *
     * @param name the name that will be displayed in eventual failure message.
     * @param toBeVerified <tt>WebDriver</tt> instance of the participant whose
     *                     "is muted" status will be verified.
     * @param observer observer's <tt>WebDriver</tt> instance.
     * @param isMuted <tt>true</tt> to verify if the participant is muted or
     *                <tt>false</tt> to check for "is unmuted" status.
     */
    public static void verifyIsMutedStatus( String name,
                                            WebDriver toBeVerified,
                                            WebDriver observer,
                                            boolean   isMuted  )
    {
        String resourceJid = MeetUtils.getResourceJid(toBeVerified);

        String mutedIconXPath =
            "//span[@id='participant_" + resourceJid +"']" +
                "/span[@class='audioMuted']/i[@class='icon-mic-disabled']";

        try
        {
            if (isMuted)
            {
                TestUtils.waitsForElementByXPath(
                    observer, mutedIconXPath, 5);
            }
            else
            {
                TestUtils.waitsForElementNotPresentByXPath(
                    observer, mutedIconXPath, 5);
            }
        }
        catch (TimeoutException exc)
        {
            fail(
                name + (isMuted ? " should" : " shouldn't")
                    + " be muted at this point");
        }
    }
}
