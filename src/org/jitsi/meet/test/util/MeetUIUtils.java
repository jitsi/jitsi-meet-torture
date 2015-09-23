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
            By.xpath("//a[@id='" + buttonID + "']")).click();
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
            By.xpath("//a[@class='button " + buttonClass + "']"))
            .click();
    }

    /**
     * Returns resource JIUD of the user who is currently displayed on large
     * video area from given <tt>participant</tt> perspective.
     *
     * @param participant <tt>WebDriver</tt> of the participant for whom the
     *                    resource of large video will be extracted.
     * @return resource JIUD of the user who is currently displayed on large
     * video area from given <tt>participant</tt> perspective.
     */
    public static String getLargeVideoResource(WebDriver participant)
    {
        return (String)((JavascriptExecutor) participant)
                .executeScript("return APP.UI.getLargeVideoResource();");
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
            clickOnToolbarButton(user, "toolbar_button_settings");

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
        verifyMutedStatus(name, toBeVerified, observer, isMuted, false);
    }

    /**
     * Verifies if given peer's video is muted from other peer's perspective.
     * Will fail the test if the verification is negative.
     *
     * @param name the name that will be displayed in eventual failure message.
     * @param toBeVerified <tt>WebDriver</tt> instance of the participant whose
     *                     video muted status will be verified.
     * @param observer observer's <tt>WebDriver</tt> instance.
     * @param isMuted <tt>true</tt> to verify if the participant is muted or
     *                <tt>false</tt> to check for video unmuted status.
     */
    public static void verifyVideoMuted( String name,
                                         WebDriver toBeVerified,
                                         WebDriver observer,
                                         boolean   isMuted  )
    {
        verifyMutedStatus(name, toBeVerified, observer, isMuted, true);
    }

    private static void verifyMutedStatus( String name,
                                           WebDriver toBeVerified,
                                           WebDriver observer,
                                           boolean   isMuted,
                                           boolean   isVideo )
    {
        String resourceJid = MeetUtils.getResourceJid(toBeVerified);

        String icon = isVideo
            ? "/span[@class='videoMuted']/i[@class='icon-camera-disabled']"
            : "/span[@class='audioMuted']/i[@class='icon-mic-disabled']";

        String mutedIconXPath
            = "//span[@id='participant_" + resourceJid +"']" + icon;

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
                    + " be muted at this point, xpath: " + mutedIconXPath);
        }
    }

    /**
     * Method will fail currently running test if avatar will not be displayed
     * on user's thumbnail and/or video will not be hidden.
     * @param where instance of <tt>WebDriver</tt> on which we'll perform
     *              the verification.
     * @param resource the resource part of MUC JID which identifies user's
     *                 video thumbnail.
     */
    public static void verifyAvatarDisplayed(WebDriver where, String resource)
    {
        // Avatar image
        TestUtils.waitsForDisplayedElementByXPath(
            where, "//span[@id='participant_" + resource + "']" +
                    "/img[@class='userAvatar']",
            5);

        // User's video
        TestUtils.waitsForNotDisplayedElementByXPath(
            where, "//span[@id='participant_" + resource + "']/video", 5);
    }

    /**
     * Method will fail currently running test if user's display name will not
     * be displayed on video thumbnail and either avatar or video will not be
     * hidden.
     * @param where instance of <tt>WebDriver</tt> on which we'll perform
     *              the verification.
     * @param resource the resource part of MUC JID which identifies user's
     *                 video thumbnail.
     */
    public static void verifyDisplayNameVisible(WebDriver where, String resource)
    {
        // Avatar image - hidden
        TestUtils.waitsForNotDisplayedElementByXPath(
            where, "//span[@id='participant_" + resource + "']" +
                   "/img[@class='userAvatar']", 5);

        // User's video - hidden
        TestUtils.waitsForNotDisplayedElementByXPath(
            where, "//span[@id='participant_" + resource + "']/video", 5);

        // Display name - visible
        TestUtils.waitsForDisplayedElementByXPath(
            where, "//span[@id='participant_" + resource + "']" +
                "/span[@class='displayname']", 5);
    }

    /**
     * Click on given peer's video thumbnail.
     * @param where instance of <tt>WebDriver</tt> where we'll perform the click
     * @param resourceJid the resource part of MUC JID which identifies user's
     *                    video thumbnail.
     */
    public static void clickOnRemoteVideo(WebDriver where, String resourceJid)
    {
        where.findElement(
            By.xpath("//span[@id='participant_" + resourceJid + "']")).click();
    }

    /**
     * Click on local video thumbnail.
     * @param where instance of <tt>WebDriver</tt> where we'll perform the click
     */
    public static void clickOnLocalVideo(WebDriver where)
    {
        where.findElement(
            By.xpath("//span[@id='localVideoContainer']" +
                      "/span[@class='focusindicator']")
        ).click();
    }

    /**
     * Method will fail currently running test unless the video is displayed on
     * local thumbnail and avatar is hidden.
     * @param where instance of <tt>WebDriver</tt> on which we'll perform
     *              the verification.
     */
    public static void verifyVideoOnLocalThumbnail(WebDriver where)
    {
        TestUtils.waitsForNotDisplayedElementByXPath(
            where, "//span[@id='localVideoContainer']/img[@class='userAvatar']",
            5);
        TestUtils.waitsForDisplayedElementByXPath(
            where, "//span[@id='localVideoWrapper']/video", 5);
    }

    /**
     * Method will fail currently running test unless the avatar is displayed on
     * local thumbnail and video is hidden.
     * @param where instance of <tt>WebDriver</tt> on which we'll perform
     *              the verification.
     */
    public static void verifyAvatarOnLocalThumbnail(WebDriver where)
    {
        TestUtils.waitsForDisplayedElementByXPath(
            where, "//span[@id='localVideoContainer']/img[@class='userAvatar']",
            5);
        TestUtils.waitsForElementNotPresentOrNotDisplayedByXPath(
            where, "//span[@id='localVideoWrapper']/video", 5);
    }
}
