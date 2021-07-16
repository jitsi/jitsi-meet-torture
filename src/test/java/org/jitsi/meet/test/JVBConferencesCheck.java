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

import org.apache.http.*;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.*;
import org.apache.http.protocol.*;
import org.apache.http.util.*;
import com.google.gson.*;

import org.jitsi.meet.test.web.*;

import org.testng.annotations.*;

import java.net.*;
import java.util.*;

import static org.testng.Assert.*;
import static org.jitsi.meet.test.util.TestUtils.*;

/**
 * Test that connects to JVB instance and saves a list of conferences on the
 * first run. On the second run of the test it will check whether the new list
 * has new conferences created or fail otherwise.
 * This test can be used to test whether a meet instance is alive.
 * The JVB instance can be on the same machine as meet or on a specified by
 * the property jitsi-meet.jvb.address.
 * @author Damian Minkov
 */
public class JVBConferencesCheck
    extends WebTestBase
{
    /**
     * List used to save the list of available conferences on the first run
     * of the test.
     */
    private static List<String> firstRunConferences = null;

    @Override
    public boolean skipTestByDefault()
    {
        return true;
    }

    /**
     * Just gets the url to connect to the jvb instance and saves
     * the current allocated conferences on first run. On second compare current
     * with first run list.
     */
    @Test
    public void testJvbConferences()
    {
        ensureTwoParticipants();

        String jvbAddress = System.getProperty("jitsi-meet.jvb.address");

        HttpHost targetHost = null;

        if (jvbAddress == null)
        {
            String meetAddress = getJitsiMeetUrl().getServerUrl();
            try
            {
                String host = new URL(meetAddress).getHost();

                targetHost = new HttpHost(host, 8080, "http");
            }
            catch(Throwable t)
            {
                // FIXME: shouldn't the test fail here ?
                t.printStackTrace();
            }
        }
        else
        {
            try
            {
                URL url = new URL(jvbAddress);
                targetHost = new HttpHost(
                    url.getHost(), url.getPort(), url.getProtocol());
            }
            catch(Throwable t)
            {
                // FIXME: shouldn't the test fail here ?
                t.printStackTrace();
            }
        }

        CloseableHttpClient httpClient = HttpClientBuilder.create().build();

        HttpGet httpget = new HttpGet("/colibri/conferences");

        CloseableHttpResponse response = null;

        ArrayList<String> conferencesList = new ArrayList<>();
        try
        {
            response
                = httpClient.execute(targetHost, httpget, (HttpContext) null);

            HttpEntity entity = response.getEntity();
            String value = EntityUtils.toString(entity);

            JsonElement jsonElem = new JsonParser().parse(value);
            if (jsonElem.isJsonArray())
            {
                JsonArray jsonArray = jsonElem.getAsJsonArray();
                for (int i = 0; i < jsonArray.size(); i++)
                {
                    conferencesList.add(
                        ((JsonObject)jsonArray.get(i)).get("id").getAsString());
                }
            }
        }
        catch(Throwable t)
        {
            // FIXME: shouldn't the test fail here ?
            t.printStackTrace();
        }
        finally
        {
            if (response != null)
            {
                try
                {
                    response.close();
                }
                catch(Throwable t)
                {}
            }
        }

        if (firstRunConferences == null)
        {
            firstRunConferences = conferencesList;
            hangUpAllParticipants();
            ensureTwoParticipants();
            testJvbConferences();
        }
        else
        {
            conferencesList.removeAll(firstRunConferences);

            print("NEW_CONFERENCES=" + conferencesList);

            assertFalse(
                conferencesList.isEmpty(),
                "The list of conferences must not be empty");
        }
    }
}
