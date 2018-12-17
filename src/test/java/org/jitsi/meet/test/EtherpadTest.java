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
package org.jitsi.meet.test;

import org.jitsi.meet.test.util.*;
import org.jitsi.meet.test.web.*;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;
import org.testng.*;
import org.testng.annotations.*;

import static org.testng.Assert.*;

/**
 * Adds various tests with the etherpad functionality.
 * Checks after closing etherpad we are able to click on videos.
 * Directly click on videos without closing etherpad.
 * @author Damian Minkov
 */
public class EtherpadTest
    extends WebTestBase
{
    /**
     * Whether the test is enabled. The test will be dynamically disabled,
     * unless etherpad is detected as enabled for the jitsi-meet instance (by
     * inspecting window.config.etherpad_base).
     */
    private static boolean enabled = true;

    @Override
    public void setupClass()
    {
        super.setupClass();

        ensureTwoParticipants();
    }

    /**
     * Clicks on share document button and check whether etherpad is loaded
     * and video is hidden.
     */
    @Test
    public void enterEtherpad()
    {
        if (!enabled)
        {
            return;
        }

        WebDriver driver1 = getParticipant1().getDriver();

        if (!MeetUtils.isEtherpadEnabled(driver1))
        {
            EtherpadTest.enabled = false;
            throw new SkipException(
                "No etherpad configuration detected. Disabling test.");
        }

        getParticipant1().getToolbar().clickEtherpadButton();

        TestUtils.waitMillis(5000);

        TestUtils.waitForNotDisplayedElementByID(
            driver1, "largeVideo", 20);
    }

    /**
     * Write some text and check on the other side, whether the text
     * is visible.
     */
    @Test(dependsOnMethods = { "enterEtherpad" })
    public void writeTextAndCheck()
    {
        if (!enabled)
        {
            return;
        }

        WebDriver driver1 = getParticipant1().getDriver();
        try
        {
            // give time for the internal frame to load and attach to the page.
            TestUtils.waitMillis(2000);

            WebDriverWait wait = new WebDriverWait(driver1, 30);
            wait.until(
                ExpectedConditions.frameToBeAvailableAndSwitchToIt(
                    By.id("etherpadIFrame")));

            wait = new WebDriverWait(driver1, 30);
            wait.until(
                ExpectedConditions.frameToBeAvailableAndSwitchToIt(
                    By.name("ace_outer")));

            wait = new WebDriverWait(driver1, 30);
            wait.until(
                ExpectedConditions.frameToBeAvailableAndSwitchToIt(
                    By.name("ace_inner")));

            String textToEnter = "SomeTestText";

            // Give etherpad some time to complete loading its editor content.
            TestUtils.waitForDisplayedElementByID(driver1, "innerdocbody", 5);

            driver1.findElement(By.id("innerdocbody")).sendKeys(textToEnter);

            TestUtils.waitMillis(2000);

            // now search and check the text
            String txt
                = driver1.findElement(
                    By.xpath("//span[contains(@class, 'author')]")).getText();

            assertEquals(txt, textToEnter, "Texts do not match");
        }
        finally
        {
            driver1.switchTo().defaultContent();
        }
    }

    /**
     * Close the etherpad and checks whether video is still visible.
     */
    @Test(dependsOnMethods = { "writeTextAndCheck" })
    public void closeEtherpadCheck()
    {
        if (!enabled)
        {
            return;
        }

        getParticipant1().getToolbar().clickEtherpadButton();

        TestUtils.waitMillis(5000);

        WebDriver driver1 = getParticipant1().getDriver();

        TestUtils.waitForDisplayedElementByID(driver1, "largeVideo", 10);
    }

    /**
     * Click on local video thumbnail and checks whether the large video
     * is the local one.
     */
    @Test(dependsOnMethods = { "closeEtherpadCheck" })
    public void participant1ClickOnLocalVideoAndTest()
    {
        if (!enabled)
        {
            return;
        }

        new SwitchVideoTest(this).participant1ClickOnLocalVideoAndTest();
    }

    /**
     * Clicks on the remote video thumbnail and checks whether the large video
     * is the remote one.
     */
    @Test(dependsOnMethods = {"participant1ClickOnLocalVideoAndTest"})
    public void participant1ClickOnRemoteVideoAndTest()
    {
        if (!enabled)
        {
            return;
        }

        SwitchVideoTest switchVideoTest = new SwitchVideoTest(this);
        try
        {
            switchVideoTest.participant1ClickOnRemoteVideoAndTest();
        }
        finally
        {
            switchVideoTest.participant1UnpinRemoteVideoAndTest();
        }
    }

    @Test(dependsOnMethods = {"participant1ClickOnRemoteVideoAndTest"})
    public void enterEtherpadClickOnVideosWithoutWriteFocus()
    {
        enterEtherpad();
        participant1ClickOnLocalVideoAndTest();
        participant1ClickOnRemoteVideoAndTest();
    }

    /**
     * Skips tests if etherpad is not enabled.
     */
    @BeforeMethod
    public void isEnabled()
    {
        if (!enabled)
        {
            throw new SkipException(
                "No etherpad configuration detected. Disabling test.");
        }
    }
}
