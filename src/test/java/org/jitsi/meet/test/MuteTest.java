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

import org.jitsi.meet.test.base.*;
import org.jitsi.meet.test.util.*;

import org.openqa.selenium.*;
import org.openqa.selenium.interactions.*;
import org.testng.annotations.*;

/**
 * Tests the muting and unmuting of the participants in Meet conferences.
 *
 * @author Damian Minkov
 * @author Lyubomir Marinov
 */
public class MuteTest
    extends AbstractBaseTest
{
    /**
     * Default constructor.
     */
    public MuteTest()
    {
    }

    /**
     * Constructs MuteTest with already allocated participants.
     * @param baseTest the parent test.
     */
    public MuteTest(AbstractBaseTest baseTest)
    {
        super(baseTest);
    }

    @Override
    public void setup()
    {
        super.setup();

        ensureTwoParticipants();
    }

    /**
     * Mutes the owner and checks at other participant is this is visible.
     */
    @Test
    public void muteOwnerAndCheck()
    {
        toggleMuteAndCheck(
            "muteOwnerAndCheck",
            getParticipant1().getDriver(),
            "owner",
            getParticipant2().getDriver(),
            true);

        TestUtils.waitMillis(2000);
    }

    /**
     * Unmutes owner and checks at other participant is this is visible.
     */
    @Test(dependsOnMethods = { "muteOwnerAndCheck" })
    public void unMuteOwnerAndCheck()
    {
        toggleMuteAndCheck(
            "unMuteOwnerAndCheck",
            getParticipant1().getDriver(),
            "owner",
            getParticipant2().getDriver(),
            false);
    }

    /**
     * Mutes the participant and checks at owner side.
     */
    @Test(dependsOnMethods = { "unMuteOwnerAndCheck" })
    public void muteParticipantAndCheck()
    {
        toggleMuteAndCheck(
            "muteParticipantAndCheck",
            getParticipant2().getDriver(),
            "getParticipant2()",
            getParticipant1().getDriver(),
            true);
    }

    /**
     * UnMutes the participant and checks at owner side.
     */
    @Test(dependsOnMethods = { "muteParticipantAndCheck" })
    public void unMuteParticipantAndCheck()
    {
        toggleMuteAndCheck(
            "unMuteParticipantAndCheck",
            getParticipant2().getDriver(),
            "getParticipant2()",
            getParticipant1().getDriver(),
            false);
    }

    /**
     * Mutes the participant and checks at owner side.
     */
    public void muteThirdParticipantAndCheck()
    {
        ensureThreeParticipants();

        toggleMuteAndCheck(
            "muteThirdParticipantAndCheck",
            getParticipant3().getDriver(),
            "getParticipant3()",
            getParticipant1().getDriver(),
            true);
    }

    /**
     * UnMutes the participant and checks at owner side.
     */
    public void unMuteThirdParticipantAndCheck()
    {
        toggleMuteAndCheck(
            "unMuteThirdParticipantAndCheck",
            getParticipant3().getDriver(),
            "getParticipant3()",
            getParticipant1().getDriver(),
            false);
    }

    /**
     * Finds the menu that can be used by the owner to control the participant.
     * Hovers over it. Finds the mute link and mute it.
     * Then checks in the second participant page whether it is muted
     */
    @Test(dependsOnMethods = { "unMuteParticipantAndCheck" })
    public void ownerMutesParticipantAndCheck()
    {
        WebDriver owner = getParticipant1().getDriver();
        WebDriver secondParticipant = getParticipant2().getDriver();

        String secondParticipantResource
            = MeetUtils.getResourceJid(secondParticipant);

        // Open the remote video menu
        WebElement cntElem = owner.findElement(By.id(
            "participant_" + secondParticipantResource));
        String remoteVideoMenuButtonXPath
            = TestUtils.getXPathStringForClassName("//span", "remotevideomenu");
        WebElement elem = owner.findElement(
            By.xpath(remoteVideoMenuButtonXPath));

        Actions action = new Actions(owner);
        action.moveToElement(cntElem);
        action.moveToElement(elem);
        action.perform();

        // open the mute dialog
        String muteParticipantLinkXPath
            = "//ul[@class='popupmenu']//a[contains(@class, 'mutelink')]";
        TestUtils.waitForDisplayedElementByXPath(
            owner,
            muteParticipantLinkXPath,
            5);
        owner.findElement(By.xpath(muteParticipantLinkXPath))
            .click();

        // confirm muting
        String muteConfirmButton
            = "//button[contains(@id, 'modal-dialog-ok-button')]";
        TestUtils.waitForDisplayedElementByXPath(
            owner,
            muteConfirmButton,
            5);
        owner.findElement(By.xpath(muteConfirmButton))
            .click();

        // and now check whether second participant is muted
        String participantMutedIconXPath
            = TestUtils.getXPathStringForClassName("//span", "audioMuted")
                + "//i[@class='icon-mic-disabled']";
        TestUtils.waitForElementByXPath(
            getParticipant2().getDriver(),
            participantMutedIconXPath,
            5);

        action.release();
    }

    /**
     * UnMutes once again the second participant and checks in the owner page
     * does this change is reflected.
     */
    @Test(dependsOnMethods = { "ownerMutesParticipantAndCheck" })
    public void participantUnMutesAfterOwnerMutedHimAndCheck()
    {
        TestUtils.waitMillis(1000);

        MeetUIUtils.clickOnToolbarButton(
            getParticipant2().getDriver(), "toolbar_button_mute");

        TestUtils.waitMillis(1000);

        MeetUIUtils.assertMuteIconIsDisplayed(
            getParticipant1().getDriver(),
            getParticipant2().getDriver(),
            false, //should be unmuted
            false, //audio
            "getParticipant2()"
        );

        // lets give time to the ui to reflect the change in the ui of the owner
        TestUtils.waitMillis(1000);
    }

    /**
     * Closes the participant and leaves the owner alone in the room.
     * Mutes the owner and then joins new participant and checks the status
     * of the mute icon.
     * At the end unmutes to clear the state.
     */
    @Test(dependsOnMethods = { "participantUnMutesAfterOwnerMutedHimAndCheck" })
    public void muteOwnerBeforeSecondParticipantJoins()
    {
        WebDriver owner = getParticipant1().getDriver();
        getParticipant2().hangUp();

        // just in case wait
        TestUtils.waitMillis(1000);

        MeetUIUtils.clickOnToolbarButton(owner, "toolbar_button_mute");

        ensureTwoParticipants();

        MeetUIUtils.assertMuteIconIsDisplayed(
                getParticipant2().getDriver(),
                owner,
                true, //should be muted
                false, //audio
                "owner");

        // now lets unmute
        unMuteOwnerAndCheck();
    }

    /**
     * Toggles the mute state of a specific Meet conference participant and
     * verifies that a specific other Meet conference participants sees a
     * specific mute state for the former.
     *
     * @param testName the name of test (to be logged)
     * @param testee the {@code WebDriver} which represents the Meet conference
     * participant whose mute state is to be toggled
     * @param testeeName the name of {@code testee} to be displayed should the
     * test fail
     * @param observer the {@code WebDriver} which represents the Meet
     * conference participant to verify the mute state of {@code testee}
     * @param muted the mute state of {@code testee} expected to be observed by
     * {@code observer}
     */
    private void toggleMuteAndCheck(
            String testName,
            WebDriver testee,
            String testeeName,
            WebDriver observer,
            boolean muted)
    {
        MeetUIUtils.clickOnToolbarButton(testee, "toolbar_button_mute");
        MeetUIUtils.assertMuteIconIsDisplayed(
                observer,
                testee,
                muted,
                false, //audio
                testeeName);
        MeetUIUtils.assertMuteIconIsDisplayed(
            testee,
            testee,
            muted,
            false, //audio
            testeeName);
    }
}
