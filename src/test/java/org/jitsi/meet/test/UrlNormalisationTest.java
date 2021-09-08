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
import org.jitsi.meet.test.web.*;
import org.openqa.selenium.*;
import org.testng.annotations.*;

import java.net.*;

import static junit.framework.Assert.assertEquals;

/**
 * Test if we correctly normalise the tenant and room name.
 */
public class UrlNormalisationTest
        extends WebTestBase
{
    @Override
    public void setupClass()
    {
        super.setupClass();

        JitsiMeetUrl jitsiMeetUrl = participants.getJitsiMeetUrl();

        jitsiMeetUrl.setRoomName(currentRoomName + "@example.com");
        jitsiMeetUrl.setTenantName("tenant@example.com");
        ensureTwoParticipants(jitsiMeetUrl, jitsiMeetUrl);
    }

    /**
     * Hang up the call and check if we're redirected to the main page.
     */
    @Test
    public void test() throws MalformedURLException
    {
        final WebDriver driver1 = getParticipant1().getDriver();
        final URL url = new URL(driver1.getCurrentUrl());
        String[] path = url.getPath().split("/");

        assertEquals("tenantexample.com", path[1]);
        assertEquals(currentRoomName + "example.com", path[2]);
    }
}
