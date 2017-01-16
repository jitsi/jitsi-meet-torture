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
package org.jitsi.meet.test.util;

import org.apache.http.*;
import org.apache.http.client.*;
import org.apache.http.client.methods.*;
import org.apache.http.conn.*;
import org.apache.http.entity.*;
import org.apache.http.impl.client.*;

import java.io.*;
import java.util.regex.*;

/**
 * Class gather utility methods for JVB operations.
 *
 * @author Pawel Domas
 */
public class JvbUtil
{

    /**
     * Triggers either force or graceful bridge shutdown and waits for it to
     * complete.
     *
     * @param jvbEndpoint the REST API endpoint of the bridge to be turned off.
     * @param force <tt>true</tt> if force shutdown should be performed or
     *              <tt>false</tt> to shutdown the bridge gracefully.
     *
     * @throws IOException if something goes wrong
     * @throws InterruptedException if the waiting thread gets interrupted at
     *                              any point.
     */
    static public void shutdownBridge(String jvbEndpoint, boolean force)
        throws IOException,
               InterruptedException
    {
        CloseableHttpClient client = HttpClientBuilder.create().build();

        try
        {
            triggerShutdown(client, jvbEndpoint, force);

            try
            {
                waitForBridgeShutdown(client, jvbEndpoint);
            }
            catch (HttpHostConnectException connectException)
            {
                // We ignore connect exception as JVB endpoint
                // dies on shutdown and may not always send the OK response
            }
        }
        finally
        {
            client.close();
        }
    }

    static private void triggerShutdown(HttpClient   client,
                                        String       jvbEndpoint,
                                        boolean      force)
        throws IOException
    {
        String url = jvbEndpoint + "/colibri/shutdown";

        HttpPost post = new HttpPost(url);

        StringEntity requestEntity = new StringEntity(
            force ?
                "{ \"force-shutdown\": \"true\" }"
                : "{ \"graceful-shutdown\": \"true\" }",
            ContentType.APPLICATION_JSON);

        post.setEntity(requestEntity);

        System.out.println("\nSending 'POST' request to URL : " + url);
        System.out.println("Post parameters : " + post.getEntity());

        HttpResponse response = client.execute(post);

        int responseCode = response.getStatusLine().getStatusCode();
        if (200 != responseCode)
        {
            throw new RuntimeException(
                "Failed to trigger graceful shutdown on: "
                    + jvbEndpoint + ", response code: " + responseCode);
        }
    }

    static private void waitForBridgeShutdown(HttpClient    client,
                                              String        jvbEndpoint)
        throws IOException,
               InterruptedException
    {
        String url = jvbEndpoint + "/colibri/stats";

        HttpGet httpGet = new HttpGet(url);

        int conferenceCount;
        do
        {
            System.out.println("\nSending 'GET' request to URL : " + url);

            HttpResponse response = client.execute(httpGet);

            int responseCode = response.getStatusLine().getStatusCode();
            if (200 != responseCode)
            {
                throw new RuntimeException(
                    "Failed to trigger graceful shutdown on: "
                        + jvbEndpoint + ", response code: " + responseCode);
            }


            // Read the response
            BufferedReader rd
                = new BufferedReader(
                        new InputStreamReader(
                                response.getEntity().getContent()));

            StringBuffer result = new StringBuffer();
            String line;
            while ((line = rd.readLine()) != null)
            {
                result.append(line);
            }

            conferenceCount = getConferenceCount(result);

            if (conferenceCount != 0)
            {
                Thread.sleep(5000);
            }
        }
        while (conferenceCount != 0);
    }

    static private int getConferenceCount(CharSequence jvbStatsJson)
    {
        int conferenceCount = -1;

        Pattern confPattern
            = Pattern.compile("\"conferences\": +\"?((\\d+))\"?");

        Matcher confMatcher = confPattern.matcher(jvbStatsJson);

        if (confMatcher.find())
        {
            String countTxt = confMatcher.group(1);

            return Integer.parseInt(countTxt);
        }

        return conferenceCount;
    }
}
