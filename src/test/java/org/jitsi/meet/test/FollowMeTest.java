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

import java.util.stream.*;

import org.jitsi.meet.test.pageobjects.web.*;
import org.jitsi.meet.test.util.*;
import org.jitsi.meet.test.web.*;

import org.openqa.selenium.*;
import org.testng.annotations.*;

import static org.jitsi.meet.test.util.TestUtils.*;
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

    @Override
    public void setupClass()
    {
        super.setupClass();

        ensureTwoParticipants();

        oneTimeSetUp();
    }

    private void oneTimeSetUp()
    {
        WebParticipant participant1 = getParticipant1();
        participant1.getToolbar().clickSettingsButton();

        SettingsDialog settingsDialog = participant1.getSettingsDialog();
        settingsDialog.waitForDisplay();
        settingsDialog.setFollowMe(true);
        settingsDialog.submit();

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
        WebParticipant participant2 = getParticipant2();

        Boolean allModeratorsEnabled = Stream
            .of(getParticipant1(), participant2)
            .allMatch(WebParticipant::isModerator);

        // if all are moderators skip this check
        if (allModeratorsEnabled)
        {
            print("All moderators enabled, skipping check!");
            return;
        }

        participant2.getToolbar().clickSettingsButton();
        SettingsDialog settingsDialog = participant2.getSettingsDialog();
        settingsDialog.waitForDisplay();
        assertFalse(settingsDialog.isFollowMeDisplayed());
        settingsDialog.close();
    }

    /**
     * Checks if launching and then exiting Etherpad is executed for the second
     * participant.
     */
    @Test(dependsOnMethods = { "testFollowMeCheckboxVisibleOnlyForModerator" })
    public void testShareDocumentCommandsAreFollowed()
    {
        WebDriver driver1 = getParticipant1().getDriver();
        WebDriver driver2 = getParticipant2().getDriver();

        if (!MeetUtils.isEtherpadEnabled(driver1))
        {
            print("No etherpad configuration detected. Disabling test.");
            return;
        }

        getParticipant1().getToolbar().clickEtherpadButton();

        TestUtils.waitForDisplayedElementByXPath(driver1, etherpadXPath, 5);
        TestUtils.waitForDisplayedElementByXPath(driver2, etherpadXPath, 5);

        getParticipant1().getToolbar().clickEtherpadButton();

        TestUtils.waitForElementNotPresentOrNotDisplayedByXPath(
            driver1, etherpadXPath, 5);
        TestUtils.waitForElementNotPresentOrNotDisplayedByXPath(
            driver2, etherpadXPath, 5);
    }

    /**
     * Checks if hiding/showing filmstrip is executed for the second
     * participant.
     */
    @Test(dependsOnMethods = { "testShareDocumentCommandsAreFollowed" })
    public void testFilmstripCommandsAreFollowed()
    {
        WebDriver driver1 = getParticipant1().getDriver();
        WebDriver driver2 = getParticipant2().getDriver();

        MeetUIUtils.displayFilmstripPanel(driver1);
        MeetUIUtils.displayFilmstripPanel(driver2);

        driver1.findElement(By.id("toggleFilmstripButton")).click();

        TestUtils.waitForElementContainsClassByXPath(
                driver1, filmstripXPath, "hidden", 10);
        TestUtils.waitForElementContainsClassByXPath(
                driver2, filmstripXPath, "hidden", 10);

        driver1.findElement(By.id("toggleFilmstripButton")).click();

        TestUtils.waitForElementAttributeValueByXPath(
                driver1,
                filmstripXPath,
                "class",
                "filmstrip__videos",
                10);
        TestUtils.waitForElementAttributeValueByXPath(
                driver2,
                filmstripXPath,
                "class",
                "filmstrip__videos",
                10);
    }

    /**
     * Tests if all participants enter and exit tile view.
     */
    @Test(dependsOnMethods = { "testFilmstripCommandsAreFollowed" })
    public void testTileViewCommandsAreFollowed()
    {
        joinThirdParticipant();

        // with three participants we default to tile view now, but it's decided a bit
        // later than when the 3rd part. joins, so we need to wait for that.
        MeetUIUtils.waitForTileViewDisplay(getParticipant1(), true);

        getParticipant1().getToolbar().clickTileViewButton();

        getAllParticipants().forEach(participant ->
            MeetUIUtils.waitForTileViewDisplay(participant, false));

        getParticipant1().getToolbar().clickTileViewButton();

        getAllParticipants().forEach(participant ->
            MeetUIUtils.waitForTileViewDisplay(participant, true));
    }

    /**
     * Checks if selecting a video for moderator selects large video for the
     * second participant.
     */
    @Test(dependsOnMethods = { "testTileViewCommandsAreFollowed" })
    public void testNextOnStageCommandsAreFollowed()
    {
        WebParticipant participant1 = getParticipant1();
        WebParticipant participant2 = getParticipant2();
        WebDriver driver2 = participant2.getDriver();
        String participant2EndpointId = participant2.getEndpointId();

        // let's make video of the second participant active
        participant1.executeScript(
            "$(\"span[id='participant_"
                + participant2EndpointId + "']\").click()");

        TestUtils.waitMillis(5000);

        // and now check that it's active for second participant too
        WebElement localVideoThumb = MeetUIUtils.getLocalVideo(driver2);

        assertEquals(
            MeetUIUtils.getVideoElementID(driver2, localVideoThumb),
            MeetUIUtils.getLargeVideoID(driver2));
    }
}
