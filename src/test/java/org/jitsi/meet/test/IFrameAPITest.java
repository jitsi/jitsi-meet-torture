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
import org.jitsi.meet.test.web.*;

import org.testng.*;
import org.testng.annotations.*;

import java.net.*;

/**
 * Test that loads a page using the iframe API to load a meeting.
 * Loads the meeting and we switch to that iframe and then run several
 * tests over it, to make sure iframe API is working fine.
 *
 * @author Damian Minkov
 */
public class IFrameAPITest
    extends WebTestBase
{
    /**
     * The url of the page implementing the iframe, mut be hosted on https location.
     * {@link src/test/resources/files/iframeAPITest.html}
     */
    @SuppressWarnings("JavadocReference")
    public static final String IFRAME_PAGE_PATH_PNAME = "org.jitsi.iframe.page_path";

    private static final String IFRAME_ROOM_NAME = "iframeAPITest.html";

    /**
     * A url for a page that loads the iframe API.
     */
    private static final String IFRAME_ROOM_PARAMS
        = "domain=%s&room=%s"
        // default settings, used in Participant join function
        // FIXME: this must be in sync with WebParticiapnt#DEFAULT_CONFIG
        + "&config={"
        + "\"debug\":true,"
        + "\"disableAEC\":true,"
        + "\"callStatsID\":false,"
        + "\"alwaysVisibleToolbar\":true,"
        + "\"p2p.enabled\":false,"
        + "\"disable1On1Mode\":true,"
        + "\"analytics.disabled\":true,"
        + "\"prejoinPageEnabled\":false"
        + "}";

    @Test
    public void testIFrameAPI()
    {
        String pagePath = System.getProperty(IFRAME_PAGE_PATH_PNAME);

        if (pagePath == null || pagePath.trim().length() == 0)
        {
            throw new SkipException("missing configuration");
        }

        // uses a custom join, so we can load the page with iframe api
        JitsiMeetUrl iFrameUrl = getJitsiMeetUrl().copy();
        String domain;
        try
        {
            domain = iFrameUrl.getHost();
        }
        catch (MalformedURLException e)
        {
            throw new RuntimeException(e);
        }

        String roomParams
            = String.format(IFRAME_ROOM_PARAMS, domain, currentRoomName);

        // Override the server and the path part(which is s room name)
        iFrameUrl.setServerUrl(pagePath);
        iFrameUrl.setRoomName(IFRAME_ROOM_NAME);
        iFrameUrl.setRoomParameters(roomParams);
        iFrameUrl.setIframeToNavigateTo("jitsiConferenceFrame0");

        ensureOneParticipant(iFrameUrl);
        ensureThreeParticipants(iFrameUrl, null, null);

        SwitchVideoTest switchVideoTest = new SwitchVideoTest(this);
        switchVideoTest.participant1ClickOnLocalVideoAndTest();
        switchVideoTest.participant1ClickOnRemoteVideoAndTest();
        switchVideoTest.participant1UnpinRemoteVideoAndTest();
        switchVideoTest.participantClickOnLocalVideoAndTest();
        switchVideoTest.participantClickOnRemoteVideoAndTest();
        switchVideoTest.participant2UnpinRemoteVideo();

        MuteTest muteTest = new MuteTest(this);
        muteTest.muteParticipant1AndCheck();
        muteTest.muteParticipant2AndCheck();
        muteTest.muteParticipant3AndCheck(iFrameUrl, null, null);
    }
}
