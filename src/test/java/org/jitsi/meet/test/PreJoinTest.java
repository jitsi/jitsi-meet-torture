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
package org.jitsi.meet.test;

import org.jitsi.meet.test.base.*;
import org.jitsi.meet.test.pageobjects.web.*;
import org.jitsi.meet.test.util.*;
import org.jitsi.meet.test.web.*;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;
import org.testng.annotations.*;

import static org.testng.Assert.*;

public class PreJoinTest
    extends WebTestBase
{
    @Test
    public void testPreJoinForRoomWithoutLobby()
    {
        JitsiMeetUrl meetingUrl = getJitsiMeetUrl();
        meetingUrl.removeFragmentParam("config.prejoinPageEnabled");
        meetingUrl.appendConfig("config.prejoinPageEnabled=true");

        joinFirstParticipant(meetingUrl);
        PreJoinScreen preJoinScreen = getParticipant1().getPreJoinScreen();

        preJoinScreen.waitForLoading();

        WebElement joinButton = preJoinScreen.getJoinButton();

        assertTrue(joinButton.isDisplayed(), "Join button not displayed");

        String classes = joinButton.getAttribute("class");
        assertFalse(classes.contains("disabled"), "The join button should not be disabled");

        getParticipant1().hangUp();
    }

    @Test
    public void testJoinWithoutAudio()
    {

        JitsiMeetUrl meetingUrl = getJitsiMeetUrl();
        meetingUrl.removeFragmentParam("config.prejoinPageEnabled");
        meetingUrl.appendConfig("config.prejoinPageEnabled=true");

        joinFirstParticipant(meetingUrl);
        PreJoinScreen preJoinScreen = getParticipant1().getPreJoinScreen();

        preJoinScreen.waitForLoading();
        preJoinScreen.getJoinOptions().click();

        WebDriverWait wait = new WebDriverWait(getParticipant1().getDriver(), 5);
        WebElement joinWithoutAudioBtn = wait.until(
            ExpectedConditions.elementToBeClickable(
                preJoinScreen.getJoinWithoutAudioButton()));
        joinWithoutAudioBtn.click();

        getParticipant1().waitToJoinMUC();

        boolean micButtonIsDisabled = getParticipant1()
            .getDriver()
            .findElement(
                By.cssSelector(".audio-preview .toolbox-icon.toggled.disabled"))
            .isDisplayed();

        assertTrue(micButtonIsDisabled);

    }

    @Test(dependsOnMethods = {"testPreJoinForRoomWithoutLobby"})
    public void testPreJoinForRoomWithLobby()
    {
        ensureOneParticipant();

        // now enable lobby
        WebParticipant participant1 = getParticipant1();
        SecurityDialog securityDialog1 = participant1.getSecurityDialog();
        securityDialog1.open();

        assertFalse(securityDialog1.isLobbyEnabled());

        securityDialog1.toggleLobby();

        LobbyTest.waitForLobbyEnabled(participant1, true);

        // now the second participant checks pre-join
        JitsiMeetUrl meetingUrl = getJitsiMeetUrl();
        meetingUrl.removeFragmentParam("config.prejoinPageEnabled");
        meetingUrl.appendConfig("config.prejoinPageEnabled=true");

        joinSecondParticipant(meetingUrl);

        PreJoinScreen preJoinScreen = getParticipant2().getPreJoinScreen();

        preJoinScreen.waitForLoading();

        WebElement joinButton = preJoinScreen.getJoinButton();

        assertTrue(joinButton.isDisplayed(), "Join button not displayed");

        // The event that display name is required takes a while to be received
        // after showing the UI.
        TestUtils.waitForCondition(
            getParticipant2().getDriver(),
            5,
            (ExpectedCondition<Boolean>) d -> {
                WebElement button = preJoinScreen.getJoinButton();
                String classes = button.getAttribute("class");

                // The join button should be disabled
                return classes.contains("disabled");
            });
    }


    @Override
    public boolean skipTestByDefault()
    {
        return true;
    }
}
