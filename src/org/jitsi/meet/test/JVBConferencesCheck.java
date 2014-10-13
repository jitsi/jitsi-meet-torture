/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jitsi.meet.test;

import junit.framework.*;
import org.apache.http.*;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.*;
import org.apache.http.protocol.*;
import org.apache.http.util.*;
import org.json.*;

import java.net.*;
import java.util.*;

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
    extends TestCase
{
    /**
     * List used to save the list of available conferences on the first run
     * of the test.
     */
    private static List<String> firstRunConferences = null;

    /**
     * Just gets the url to connect to the jvb instance and saves
     * the current allocated conferences on first run. On second compare current
     * with first run list.
     */
    public void testJVBConferences()
    {
        String jvbAddress = System.getProperty("jitsi-meet.jvb.address");

        HttpHost targetHost = null;

        if(jvbAddress == null)
        {
            String meetAddress =
                System.getProperty(ConferenceFixture.JITSI_MEET_URL_PROP);
            try
            {
                String host = new URL(meetAddress).getHost();

                targetHost = new HttpHost(host, 8080, "http");
            }
            catch(Throwable t)
            {
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
                t.printStackTrace();
            }
        }

        CloseableHttpClient httpClient = HttpClientBuilder.create().build();

        HttpGet httpget = new HttpGet("/colibri/conferences");

        CloseableHttpResponse response = null;

        ArrayList<String> conferencesList = new ArrayList<String>();
        try
        {
            response = httpClient.execute(
                targetHost, httpget, (HttpContext)null);

            HttpEntity entity = response.getEntity();
            String value = EntityUtils.toString(entity);

            JSONArray jsonArray = new JSONArray(value);
            for(int i = 0; i < jsonArray.length(); i++)
            {
                conferencesList.add(
                    (String)((JSONObject)jsonArray.get(i)).get("id"));
            }
        }
        catch(Throwable t)
        {
            t.printStackTrace();
        }
        finally
        {
            if(response != null)
            {
                try
                {
                    response.close();
                }
                catch(Throwable t){}
            }
        }

        if(firstRunConferences == null)
        {
            firstRunConferences = conferencesList;
        }
        else
        {
            conferencesList.removeAll(firstRunConferences);

            System.out.println("NEW_CONFERENCES=" + conferencesList);

            assertFalse("The list of conferences must not be empty",
                conferencesList.isEmpty());
        }
    }
}
