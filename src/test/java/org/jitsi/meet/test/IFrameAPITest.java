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
import org.openqa.selenium.support.ui.*;
import org.testng.annotations.Test;

/**
 * Test that loads a page using the iframe API to load a meeting.
 * Loads the meeting and we switch to that iframe and then run several
 * tests over it, to make sure iframe API is working fine.
 *
 * @author Damian Minkov
 */
public class IFrameAPITest
    extends AbstractBaseTest
{
    /**
     * A url for a page that loads the iframe API.
     */
    private static final String IFRAME_API_LOAD_PAGE
        = "https://cdn.jitsi.net/jitsi-meet-torture/v1/iframeAPITest.html"
            + "?domain=%s"
            + "&room=%s"
            // default settings, used in Participant join function.
            + "&config={"
                + "\"debug\":true,"
                + "\"disableAEC\":true,"
                + "\"callStatsID\":false,"
                + "\"alwaysVisibleToolbar\":true,"
                + "\"p2p.enabled\":false,"
                + "\"disable1On1Mode\":true"
            + "}";

    @Test
    public void testIFrameAPI()
    {
        // uses a custom join, so we can load the page with iframe api
        ensureOneParticipant(this::joinConference);
        ensureThreeParticipants();

        SwitchVideoTest switchVideoTest = new SwitchVideoTest(this);
        switchVideoTest.ownerClickOnLocalVideoAndTest();
        switchVideoTest.ownerClickOnRemoteVideoAndTest();
        switchVideoTest.ownerUnpinRemoteVideoAndTest();
        switchVideoTest.participantClickOnLocalVideoAndTest();
        switchVideoTest.participantClickOnRemoteVideoAndTest();
        switchVideoTest.participantUnpinRemoteVideo();

        MuteTest muteTest = new MuteTest(this);
        muteTest.muteOwnerAndCheck();
        muteTest.muteParticipantAndCheck();
        muteTest.muteThirdParticipantAndCheck();
    }

    /**
     * Private implementation of joining a participants, where we load a page
     * which loads the conference using iframe API.
     * @param roomName the roomname
     * @param fragment extra params that can be passed.
     */
    private void joinConference(String roomName, String fragment)
    {
        WebDriver participant = getParticipant1().getDriver();

        String domain = System.getProperty("jitsi-meet.instance.url")
            .replace("https://", "");

        String pageToLoad
            = String.format(IFRAME_API_LOAD_PAGE, domain, roomName);

        TestUtils.print(
            getParticipant1().getName() + " is opening URL: " + pageToLoad);

        participant.navigate().to(pageToLoad);

        // let's wait for loading and switch to that iframe so we can continue
        // with regular tests
        WebDriverWait wait = new WebDriverWait(participant, 10);
        wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(
            By.id("jitsiConferenceFrame0")));
    }
}
