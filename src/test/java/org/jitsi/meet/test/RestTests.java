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
import org.apache.http.*;
import org.apache.http.client.config.*;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.*;
import org.apache.http.util.*;

import org.jitsi.meet.test.web.*;

import org.testng.annotations.*;

import java.io.*;
import java.net.*;

import static org.testng.Assert.*;

/**
 * This test will assume for now jicofo and videobridge are on the same machine
 * will query both for health. And as the tests had allocated one conference
 * will check whether such is reported by the jvb rest api.
 *
 * @author Damian Minkov
 */
public class RestTests
    extends WebTestBase
{
    private String serverAddress = null;

    @Override
    public boolean skipTestByDefault()
    {
        return true;
    }

    @Test
    public void testRestAPI()
        throws MalformedURLException
    {
        serverAddress = getJitsiMeetUrl().getHost();

        checkJicofoHealth();
        checkJVBHealth();
        checkConferencesThroughREST();
    }

    /**
     * Just checks jicofo's health through REST api.
     */
    private void checkJicofoHealth()
    {
        runRestClient(serverAddress, 8888, "/about/health");
    }

    /**
     * Just checks jvb's health through REST api.
     */
    private void checkJVBHealth()
    {
        runRestClient(serverAddress, 8080, "/about/health");
    }

    /**
     * Checks the number of conferences, should be more than 0.
     */
    private void checkConferencesThroughREST()
    {
        String conferences
            = runRestClient(serverAddress, 8080, "/colibri/conferences");

        JsonArray confs = new Gson().fromJson(conferences, JsonArray.class);
        if (confs == null || confs.size() == 0)
        {
            fail("Expected at least one conference");
        }
    }

    /**
     * Just checks jvb's health through REST api.
     * @param serverAddress the address to query
     * @param port the port to use
     * @param queryURL the url part without the host
     * @return the body content from the response
     */
    private String runRestClient(
        String serverAddress, int port, String queryURL)
    {
        RequestConfig defaultRequestConfig = RequestConfig.custom()
            .setSocketTimeout(5000)
            .setConnectTimeout(5000)
            .setConnectionRequestTimeout(5000)
            .build();
        CloseableHttpClient httpclient = HttpClientBuilder.create()
            .setDefaultRequestConfig(defaultRequestConfig).build();

        HttpHost targetHost = new HttpHost(serverAddress, port, "http");

        try
        {
            HttpGet httpget = new HttpGet(queryURL);
            try (CloseableHttpResponse response = httpclient.execute(
                targetHost, httpget))
            {
                if (response.getStatusLine().getStatusCode() != 200)
                {
                    fail("REST returned error:" + response.getStatusLine());
                }

                return EntityUtils.toString(response.getEntity());
            }
        }
        catch (IOException e)
        {
            fail("REST no enabled or not reachable:" + e.getMessage());
        }

        return null;
    }
}
