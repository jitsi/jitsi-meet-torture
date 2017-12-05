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

import junit.extensions.TestSetup;
import junit.framework.*;
import org.jitsi.meet.test.util.*;

import org.openqa.selenium.*;

/**
 * A test which tests "Follow Me" feature that allows moderators to enable
 * a mode in the conference where all participants are seeing what the
 * moderator is seeing.
 *
 * @author Kostiantyn Tsaregradskyi
 */
public class FollowMeTest
    extends TestCase
{
    private final static String filmstripXPath = "//div[@id='remoteVideos']";
    private final static String etherpadXPath = "//div[@id='etherpad']/iframe";
    private final static String followMeCheckboxXPath =
            "//input[@id='followMeCheckBox']";

    /**
     * Constructs test.
     * @param name the method name for the test.
     */
    public FollowMeTest(String name)
    {
        super(name);
    }

    /**
     * Orders the tests.
     * @return the suite with order tests.
     */
    public static junit.framework.Test suite()
    {
        TestSuite suite = new TestSuite();

        suite.addTest(new FollowMeTest(
                "testFollowMeCheckboxVisibleOnlyForModerator"));
        suite.addTest(new FollowMeTest(
                "testShareDocumentCommandsAreFollowed"));
        suite.addTest(new FollowMeTest(
                "testFilmstripCommandsAreFollowed"));
        suite.addTest(new FollowMeTest(
                "testNextOnStageCommandsAreFollowed"));

        return new TestSetup(suite) {
            protected void setUp() {
                oneTimeSetUp();
            }

            protected void tearDown() {
                oneTimeTearDown();
            }
        };
    }

    private static void oneTimeSetUp() {
        System.err.println("Enabling 'Follow Me' for moderator.");

        WebDriver owner = ConferenceFixture.getOwner();
        WebDriver secondParticipant = ConferenceFixture.getSecondParticipant();

        MeetUIUtils.displaySettingsPanel(owner);
        MeetUIUtils.displaySettingsPanel(secondParticipant);

        TestUtils.waitForDisplayedElementByXPath(
                owner, followMeCheckboxXPath, 5);

        owner.findElement(By.id("followMeCheckBox")).click();

        // give time for follow me to be enabled on all participants
        TestUtils.waitMillis(5000);
    }

    private static void oneTimeTearDown() {
        System.err.println("Disabling 'Follow Me' and lose the state.");

        ConferenceFixture.restartParticipants();

        // give some time
        TestUtils.waitMillis(5000);
    }

    /**
     * Checks that "Follow me" checkbox is only visible for moderator.
     * If all moderators is enabled skip this check.
     */
    public void testFollowMeCheckboxVisibleOnlyForModerator()
    {
        System.err.println("Start testFollowMeCheckboxVisibleOnlyForModerator");

        Boolean allModeratorsEnabled = (Boolean)(
            (JavascriptExecutor) ConferenceFixture.getSecondParticipant())
            .executeScript(
                "return !!interfaceConfig.DISABLE_FOCUS_INDICATOR;");
        // if all are moderators skip this check
        if (allModeratorsEnabled) {
            System.err.println("All moderators enabled, skipping check!");
            return;
        }

        WebDriver secondParticipant = ConferenceFixture.getSecondParticipant();

        TestUtils.waitForElementNotPresentByXPath(
                secondParticipant, followMeCheckboxXPath, 5);
    }

    /**
     * Checks if launching and then exiting Etherpad is executed for the second
     * participant.
     */
    public void testShareDocumentCommandsAreFollowed() {
        System.err.println("Start testShareDocumentCommandsAreFollowed");

        WebDriver owner = ConferenceFixture.getOwner();
        WebDriver secondParticipant = ConferenceFixture.getSecondParticipant();

        if (!MeetUtils.isEtherpadEnabled(owner))
        {
            System.err.println(
                "No etherpad configuration detected. Disabling test.");
            return;
        }

        MeetUIUtils.clickOnToolbarButtonByClass(owner, "icon-share-doc");

        TestUtils.waitForDisplayedElementByXPath(owner, etherpadXPath, 5);
        TestUtils.waitForDisplayedElementByXPath(
                secondParticipant, etherpadXPath, 5);

        MeetUIUtils.clickOnToolbarButtonByClass(owner, "icon-share-doc");

        TestUtils.waitForElementNotPresentOrNotDisplayedByXPath(
                owner, etherpadXPath, 5);
        TestUtils.waitForElementNotPresentOrNotDisplayedByXPath(
                secondParticipant, etherpadXPath, 5);
    }

    /**
     * Checks if hiding/showing filmstrip is executed for the second
     * participant.
     */
    public void testFilmstripCommandsAreFollowed() {
        System.err.println("Start testFilmstripCommandsAreFollowed");

        WebDriver owner = ConferenceFixture.getOwner();
        WebDriver secondParticipant = ConferenceFixture.getSecondParticipant();

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
    public void testNextOnStageCommandsAreFollowed() {
        System.err.println("Start testNextOnStageCommandsAreFollowed");

        WebDriver owner = ConferenceFixture.getOwner();
        WebDriver secondParticipant = ConferenceFixture.getSecondParticipant();

        String secondParticipantResource
            = MeetUtils.getResourceJid(secondParticipant);

        // let's make video of second participant active
        ((JavascriptExecutor)owner).executeScript(
            "$(\"span[id='participant_" + secondParticipantResource + "']\").click()");

        TestUtils.waitMillis(5000);

        // and now check that it's active for second participant too
        WebElement localVideoThumb =
                MeetUIUtils.getLocalVideo(secondParticipant);

        assertEquals(
            MeetUIUtils.getVideoElementID(secondParticipant, localVideoThumb),
            MeetUIUtils.getLargeVideoID(secondParticipant));
    }
}
