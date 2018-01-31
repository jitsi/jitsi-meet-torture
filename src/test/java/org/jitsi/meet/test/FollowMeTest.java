/*
 * Copyright @ 2016 Atlassian Pty Ltd
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

import org.jitsi.meet.test.base.*;
import org.jitsi.meet.test.util.*;
import org.jitsi.meet.test.web.*;

import org.openqa.selenium.*;
import org.testng.annotations.*;

import static org.testng.Assert.*;

/**
 * A test which tests "Follow Me" feature that allows moderators to enable
 * a mode in the conference where all participants are seeing what the
 * moderator is seeing.
 *
 * @author Kostiantyn Tsaregradskyi
 */
public class FollowMeTest
    extends WebTestBase
{
    private final static String filmstripXPath = "//div[@id='remoteVideos']";
    private final static String etherpadXPath = "//div[@id='etherpad']/iframe";
    private final static String followMeCheckboxXPath =
            "//input[@id='followMeCheckBox']";

    @Override
    public void setup()
    {
        super.setup();

        ensureTwoParticipants();

        oneTimeSetUp();
    }

    private void oneTimeSetUp() {
        WebDriver owner = getParticipant1().getDriver();
        WebDriver secondParticipant = getParticipant2().getDriver();

        MeetUIUtils.displaySettingsPanel(owner);
        MeetUIUtils.displaySettingsPanel(secondParticipant);

        TestUtils.waitForDisplayedElementByXPath(
                owner, followMeCheckboxXPath, 5);

        owner.findElement(By.id("followMeCheckBox")).click();

        // give time for follow me to be enabled on all participants
        TestUtils.waitMillis(5000);
    }

    /**
     * Checks that "Follow me" checkbox is only visible for moderator.
     * If all moderators is enabled skip this check.
     */
    @Test
    public void testFollowMeCheckboxVisibleOnlyForModerator()
    {
        Participant secondParticipant = getParticipant2();

        Boolean allModeratorsEnabled = (Boolean)(
            secondParticipant.executeScript(
                "return !!interfaceConfig.DISABLE_FOCUS_INDICATOR;"));
        // if all are moderators skip this check
        if (allModeratorsEnabled) {
            print("All moderators enabled, skipping check!");
            return;
        }

        TestUtils.waitForElementNotPresentByXPath(
                secondParticipant.getDriver(), followMeCheckboxXPath, 5);
    }

    /**
     * Checks if launching and then exiting Etherpad is executed for the second
     * participant.
     */
    @Test(dependsOnMethods = { "testFollowMeCheckboxVisibleOnlyForModerator" })
    public void testShareDocumentCommandsAreFollowed()
    {
        WebDriver owner = getParticipant1().getDriver();
        WebDriver secondParticipant = getParticipant2().getDriver();

        if (!MeetUtils.isEtherpadEnabled(owner))
        {
            print(
                "No etherpad configuration detected. Disabling test.");
            return;
        }

        MeetUIUtils.clickOnToolbarButton(owner, "toolbar_button_etherpad");

        TestUtils.waitForDisplayedElementByXPath(owner, etherpadXPath, 5);
        TestUtils.waitForDisplayedElementByXPath(
                secondParticipant, etherpadXPath, 5);

        MeetUIUtils.clickOnToolbarButton(owner, "toolbar_button_etherpad");

        TestUtils.waitForElementNotPresentOrNotDisplayedByXPath(
                owner, etherpadXPath, 5);
        TestUtils.waitForElementNotPresentOrNotDisplayedByXPath(
                secondParticipant, etherpadXPath, 5);
    }

    /**
     * Checks if hiding/showing filmstrip is executed for the second
     * participant.
     */
    @Test(dependsOnMethods = { "testShareDocumentCommandsAreFollowed" })
    public void testFilmstripCommandsAreFollowed()
    {
        WebDriver owner = getParticipant1().getDriver();
        WebDriver secondParticipant = getParticipant2().getDriver();

        MeetUIUtils.displayFilmstripPanel(owner);
        MeetUIUtils.displayFilmstripPanel(secondParticipant);

        owner.findElement(By.id("toggleFilmstripButton")).click();

        TestUtils.waitForElementContainsClassByXPath(
                owner, filmstripXPath, "hidden", 10);
        TestUtils.waitForElementContainsClassByXPath(
                secondParticipant, filmstripXPath, "hidden", 10);

        owner.findElement(By.id("toggleFilmstripButton")).click();

        TestUtils.waitForElementAttributeValueByXPath(
                owner, filmstripXPath, "class", "filmstrip__videos", 10);
        TestUtils.waitForElementAttributeValueByXPath(
                secondParticipant, filmstripXPath, "class", "filmstrip__videos", 10);
    }

    /**
     * Checks if selecting a video for moderator selects large video for the
     * second participant.
     */
    @Test(dependsOnMethods = { "testFilmstripCommandsAreFollowed" })
    public void testNextOnStageCommandsAreFollowed()
    {
        Participant owner = getParticipant1();
        WebDriver secondParticipant = getParticipant2().getDriver();

        String secondParticipantResource
            = MeetUtils.getResourceJid(secondParticipant);

        // let's make video of second participant active
        owner.executeScript(
            "$(\"span[id='participant_"
                + secondParticipantResource + "']\").click()");

        TestUtils.waitMillis(5000);

        // and now check that it's active for second participant too
        WebElement localVideoThumb =
                MeetUIUtils.getLocalVideo(secondParticipant);

        assertEquals(
            MeetUIUtils.getVideoElementID(secondParticipant, localVideoThumb),
            MeetUIUtils.getLargeVideoID(secondParticipant));
    }
}
