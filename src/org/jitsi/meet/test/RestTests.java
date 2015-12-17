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

import com.google.gson.*;
import junit.framework.*;
import org.apache.http.*;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.*;
import org.apache.http.util.*;

import java.io.*;
import java.net.*;

/**
 * This test will assume for now jicofo and videobridge are on the same machine
 * will query both for health. And as the tests had allocated one conference
 * will check whether such is reported by the jvb rest api.
 * @author Damian Minkov
 */
public class RestTests
    extends TestCase
{
    /**
     * Property to enable/disable rest tests.
     */
    public static final String ENABLE_REST_API_TESTS
        = "jitsi-meet.tests.rest.enabled";

    private String serverAddress = null;

    public void testRestAPI()
        throws MalformedURLException
    {
        serverAddress = new URL(
            System.getProperty(ConferenceFixture.JITSI_MEET_URL_PROP))
            .getHost();

        checkJicofoHealth();
        checkJVBHealth();
        checkConferencesThroughREST();
    }

    /**
     * Just checks jicofo's health through REST api.
     */
    private void checkJicofoHealth()
    {
        System.err.println("checkJicofoHealth");

        runRestClient(serverAddress, 8888, "/about/health");
    }

    /**
     * Just checks jvb's health through REST api.
     */
    private void checkJVBHealth()
    {
        System.err.println("checkJVBHealth");

        runRestClient(serverAddress, 8080, "/about/health");
    }

    /**
     * Checks the number of conferences, should be more than 0.
     */
    private void checkConferencesThroughREST()
    {
        System.err.println("checkConferencesThroughREST");

        String conferences
            = runRestClient(serverAddress, 8080, "/colibri/conferences");

        JsonArray confs = new Gson().fromJson(conferences, JsonArray.class);
        if(confs == null || confs.size() == 0)
            assertFalse("Expected at least one conference", true);
    }

    /**
     * Just checks jvb's health through REST api.
     * @param serverAddress the address to query
     * @param port the port to use
     * @param queryURL the url part without the host
     * @return the body content from the response
     */
    private static String runRestClient(
        String serverAddress, int port, String queryURL)
    {
        CloseableHttpClient httpclient = HttpClientBuilder.create().build();

        HttpHost targetHost = new HttpHost(serverAddress, port, "http");

        try
        {
            HttpGet httpget = new HttpGet(queryURL);
            CloseableHttpResponse response = httpclient.execute(
                targetHost, httpget);
            try
            {
                if(response.getStatusLine().getStatusCode() != 200)
                    assertFalse("REST returned error:"
                        + response.getStatusLine(), true);

                HttpEntity entity = response.getEntity();
                return EntityUtils.toString(entity);
            }
            finally
            {
                response.close();
            }
        }
        catch (IOException e)
        {
            assertFalse("REST no enabled or not reachable:" + e.getMessage(),
                true);
        }

        return null;
    }

}
