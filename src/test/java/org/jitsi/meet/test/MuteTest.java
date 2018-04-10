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
import org.jitsi.meet.test.web.*;

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
    extends WebTestBase
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
     * @deprecated see
     * {@link AbstractBaseTest#AbstractBaseTest(AbstractBaseTest)}
     */
    public MuteTest(AbstractBaseTest baseTest)
    {
        super(baseTest);
    }

    @Override
    public void setupClass()
    {
        super.setupClass();

        ensureTwoParticipants();

        // Make sure the info dialog is closed because it may overlap the
        // remove video menus.
        getParticipant1().getInfoDialog().close();
        getParticipant2().getInfoDialog().close();
    }

    /**
     * Mutes participant1 and checks at other participant is this is visible.
     */
    @Test
    public void muteParticipant1AndCheck()
    {
        toggleMuteAndCheck(
            getParticipant1(),
            "participant1",
            getParticipant2(),
            true);

        TestUtils.waitMillis(2000);
    }

    /**
     * Unmutes participant1 and checks at other participant is this is visible.
     */
    @Test(dependsOnMethods = {"muteParticipant1AndCheck"})
    public void unmuteParticipant1AndCheck()
    {
        toggleMuteAndCheck(
            getParticipant1(),
            "participant1",
            getParticipant2(),
            false);
    }

    /**
     * Mutes the participant and checks at participant1's side.
     */
    @Test(dependsOnMethods = {"unmuteParticipant1AndCheck"})
    public void muteParticipant2AndCheck()
    {
        toggleMuteAndCheck(
            getParticipant2(),
            "getParticipant2()",
            getParticipant1(),
            true);
    }

    /**
     * UnMutes the participant and checks at participant1's side.
     */
    @Test(dependsOnMethods = {"muteParticipant2AndCheck"})
    public void unmuteParticipant2AndCheck()
    {
        toggleMuteAndCheck(
            getParticipant2(),
            "participant2",
            getParticipant1(),
            false);
    }

    /**
     * Mutes participant3 and checks at participant1's side.
     */
    public void muteParticipant3AndCheck()
    {
        ensureThreeParticipants();

        toggleMuteAndCheck(
            getParticipant3(),
            "participant3",
            getParticipant1(),
            true);
    }

    /**
     * Unmutes participant3 and checks at participant1's side.
     */
    public void unmuteParticipant3AndCheck()
    {
        toggleMuteAndCheck(
            getParticipant3(),
            "getParticipant3()",
            getParticipant1(),
            false);
    }

    /**
     * Finds the menu that can be used by participant1 to control the
     * participant. Hovers over it. Finds the mute link and mute it. Then
     * checks in the second participant page whether it is muted
     */
    @Test(dependsOnMethods = {"unmuteParticipant2AndCheck"})
    public void participant1MutesParticipant2AndCheck()
    {
        WebDriver driver1 = getParticipant1().getDriver();
        String participant2EndpointId = getParticipant2().getEndpointId();

        // Open the remote video menu
        WebElement cntElem
            = driver1.findElement(
                By.id("participant_" + participant2EndpointId));
        String remoteVideoMenuButtonXPath
            = TestUtils.getXPathStringForClassName("//span", "remotevideomenu");
        WebElement elem
            = driver1.findElement(By.xpath(remoteVideoMenuButtonXPath));

        Actions action = new Actions(driver1);
        action.moveToElement(cntElem);
        action.moveToElement(elem);
        action.perform();

        // give time for the menu to appear
        TestUtils.waitForDisplayedElementByXPath(
            driver1,
            "//ul[@class='popupmenu']//a[contains(@class, 'mutelink')]",
            5);

        // click the button
        MeetUIUtils.clickOnElement(driver1, "ul.popupmenu a.mutelink", true);

        // wait for confirm muting to display
        TestUtils.waitForDisplayedElementByXPath(
            driver1,
            "//button[@id='modal-dialog-ok-button']",
            5);

        // confirm muting
        MeetUIUtils.clickOnButton(driver1, "modal-dialog-ok-button", true);

        // and now check whether second participant is muted
        getParticipant2().getFilmstrip()
            .assertAudioMuteIcon(getParticipant2(), true);

        action.release();
    }

    /**
     * Unmutes participant2 and checks in the participant1's page whether this
     * change is reflected.
     */
    @Test(dependsOnMethods = {"participant1MutesParticipant2AndCheck"})
    public void participant2UnmutesAfterParticipant1MutedItAndCheck()
    {
        TestUtils.waitMillis(1000);

        getParticipant2().getToolbar().clickAudioMuteButton();

        TestUtils.waitMillis(1000);

        getParticipant1().getFilmstrip()
            .assertAudioMuteIcon(getParticipant2(), false);

        // lets give time to the ui to reflect the change in the ui of
        // participant1
        TestUtils.waitMillis(1000);
    }

    /**
     * Closes participant2 and leaves participant1 alone in the room.
     * Mutes participant1 and then joins new participant and checks the status
     * of the mute icon. At the end unmutes to clear the state.
     */
    @Test(dependsOnMethods = {"participant2UnmutesAfterParticipant1MutedItAndCheck"})
    public void muteParticipant1BeforeParticipant2Joins()
    {
        getParticipant2().hangUp();

        // just in case wait
        TestUtils.waitMillis(1000);

        getParticipant1().getToolbar().clickAudioMuteButton();

        WebParticipant participant = joinSecondParticipant();

        // if the participant is audio only, if audio is muted we will not
        // receive any data, so skip download check
        participant.waitForSendReceiveData(
            true, !participant.isAudioOnlyParticipant());

        getParticipant2().getFilmstrip()
            .assertAudioMuteIcon(getParticipant1(), true);

        // now lets unmute
        unmuteParticipant1AndCheck();
    }

    /**
     * Toggles the mute state of a specific Meet conference participant and
     * verifies that a specific other Meet conference participants sees a
     * specific mute state for the former.
     *
     * @param testee the {@code WebParticipant} which represents the Meet
     * conference participant whose mute state is to be toggled
     * @param testeeName the name of {@code testee} to be displayed should the
     * test fail
     * @param observer the {@code WebParticipant} which represents the Meet
     * conference participant to verify the mute state of {@code testee}
     * @param muted the mute state of {@code testee} expected to be observed by
     * {@code observer}
     */
    private void toggleMuteAndCheck(
        WebParticipant testee,
        String testeeName,
        WebParticipant observer,
        boolean muted)
    {
        testee.getToolbar().clickAudioMuteButton();

        observer.getFilmstrip()
            .assertAudioMuteIcon(testee, muted);
        testee.getFilmstrip()
            .assertAudioMuteIcon(testee, muted);
    }
}
