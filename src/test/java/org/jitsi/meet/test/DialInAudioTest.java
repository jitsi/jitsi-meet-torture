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

import com.google.gson.*;
import org.apache.http.*;
import org.apache.http.client.config.*;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.*;
import org.apache.http.util.*;
import org.jitsi.meet.test.pageobjects.web.*;
import org.jitsi.meet.test.util.*;
import org.jitsi.meet.test.web.*;
import org.testng.*;
import org.testng.annotations.*;

import java.net.*;
import java.text.*;

import static org.jitsi.meet.test.util.TestUtils.*;
import static org.testng.Assert.*;

/**
 * A simple tests that enters a conference, reads the dial-in info and executes
 * a REST http call, which is suppose to add a dial-in participant in the call.
 * We wait for some time for new participant, then wait for its audio and we
 * kick it and consider this test successful.
 *
 * @author Damian Minkov
 */
public class DialInAudioTest
    extends WebTestBase
{
    /**
     * Name of the system property which holds the url to execute which will
     * create a dial-in participant.
     * The url must be a valid url, with properly escaped parameters.
     * The url can contain {0}, to pass the conference pin.
     * For example: {"pin":"{0}"}, needs to be: %7B%22pin%22%3A%22{0}%22%7D
     */
    protected static final String DIAL_IN_PARTICIPANT_REST_URL
        = "dialIn.rest.url";

    private String dialInPin = null;

    private String restURLString = null;

    /**
     * We change this once the user had joined. We want to avoid situations
     * where the grid has no available nodes, or the browser crashes/fails to
     * load and to report a dial in failure in this case.
     * When the value is false after we called <tt>ensureOneParticipant</tt>
     * we skip all checks and return success.
     */
    private boolean userJoined = false;

    @Override
    public boolean skipTestByDefault()
    {
        return true;
    }

    /**
     * Checks whether dial-in is enabled.
     *
     * @param participant The participant to check.
     * @return true if dial-in is enabled for this participant.
     */
    protected boolean isDialInEnabled(WebParticipant participant)
    {
        return MeetUtils.isDialInEnabled(participant.getDriver());
    }

    /**
     * Extracts the conference pin from the info dialog.
     * @param participant The participant which info dialog to use.
     * @return the retrieved pin with no spaces.
     */
    protected String retrievePin(WebParticipant participant)
    {
        InviteDialog inviteDialog = participant.getInviteDialog();
        try
        {
            inviteDialog.open();

            // get the dial-in pin
            String dialInPin = inviteDialog.getPinNumber();

            // removes any blanks
            return dialInPin.replaceAll(" ", "");
        }
        finally
        {
            inviteDialog.close();
        }
    }

    /**
     * Creates a web participant to enter a room and read the dial-in info.
     */
    @Test
    public void enterAndReadDialInPin()
    {
        this.restURLString = System.getProperty(DIAL_IN_PARTICIPANT_REST_URL);
        Assert.assertTrue(
            this.restURLString != null,
            "REST Url missing. Pass it using " +
                "-D" + DIAL_IN_PARTICIPANT_REST_URL + "=");

        try
        {
            // join in a room
            ensureOneParticipant();

            userJoined = true;
        }
        catch(Throwable t)
        {
            t.printStackTrace();
            return;
        }

        WebParticipant participant = getParticipant1();

        if (!isDialInEnabled(participant))
        {
            throw new SkipException(
                "No dial in configuration detected. Disabling test.");
        }

        // get the dial-in pin
        dialInPin = retrievePin(participant);

        assertTrue(dialInPin.length() > 1);
        print("Dial-in pin retrieved:" + dialInPin);
    }

    /**
     * Executes the REST-configured web call, replacing the dial-in pin in the
     * url and waits 10 seconds for the participant to join.
     */
    @Test(dependsOnMethods = { "enterAndReadDialInPin" })
    public void enterDialInParticipant()
    {
        if (!userJoined)
        {
            return;
        }
        try
        {
            URI restURI = new URI(
                MessageFormat.format(
                    this.restURLString,
                    this.dialInPin));

            RequestConfig defaultRequestConfig = RequestConfig.custom()
                .setSocketTimeout(5000)
                .setConnectTimeout(5000)
                .setConnectionRequestTimeout(5000)
                .build();
            CloseableHttpClient httpclient = HttpClientBuilder.create()
                .setDefaultRequestConfig(defaultRequestConfig).build();

            HttpHost targetHost = new HttpHost(
                restURI.getHost(),
                restURI.getPort(),
                restURI.getScheme());

            HttpGet httpget = new HttpGet(restURI);
            try (CloseableHttpResponse response = httpclient.execute(
                targetHost, httpget))
            {
                if (response.getStatusLine().getStatusCode() != 200)
                {
                    fail("REST returned error:" + response.getStatusLine());
                }
                else
                {
                    print("Rest api returned:" + response.getStatusLine());
                }

                HttpEntity entity = response.getEntity();
                String value = EntityUtils.toString(entity);

                JsonElement jsonElem = new JsonParser().parse(value);

                Assert.assertFalse(
                    "1".equals(jsonElem.getAsJsonObject().get("result")),
                    "Something is wrong, cannot join dial-in participant!");
            }
        }
        catch (Exception e)
        {
            fail("Error sending REST request:" + e.getMessage());
        }
    }

    /**
     * Waits for audio from the second participant. When audio is present,
     * kick it and terminate without an error.
     */
    @Test(dependsOnMethods = { "enterDialInParticipant" })
    public void waitForAudioFromDialIn()
    {
        if (!userJoined)
        {
            return;
        }

        WebParticipant participant = getParticipant1();

        participant.waitForParticipants(1);

        participant.waitForIceConnected();
        participant.waitForRemoteStreams(1);
        participant.waitForSendReceiveData();
    }

    /**
     * Cleanups the participant we had created through the rest api.
     */
    @AfterClass
    public void cleanupClass()
    {
        try
        {
            // now let's kick the participant
            WebParticipant participant = getParticipant1();
            if (participant != null)
            {
                participant.getRemoteParticipants().get(0).kick();

                participant.waitForParticipants(0);
            }
        }
        catch(Exception e)
        {
            // ignore if we cannot kick participant
        }
        finally
        {
            super.cleanupClass();
        }
    }
}
