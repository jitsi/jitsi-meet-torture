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

import org.jitsi.meet.test.util.*;
import org.jitsi.meet.test.web.*;

import org.openqa.selenium.*;
import org.testng.*;
import org.testng.annotations.*;

import java.util.*;

import static org.testng.Assert.*;
import static org.jitsi.meet.test.util.TestUtils.*;

/**
 * The test checks the UI for displaying notifications about participants media
 * connectivity status (connection between the participant and the JVB).
 *
 * 1. Join with 2 participants {@link #testInitialize()}.
 * 2. Block media flow on participant2 and check if is indicated as disconnected
 *    {@link #testParticipant2InterruptedDuringCall()}.
 * 3. Unblock the ports and see if the UI updated accordingly
 *    {@link #testParticipant2Restored()}.
 * 4. Block participant2's media ports and wait until channels expire on
 *    the bridge then join with participant3 and test switching between
 *    participants {@link #testJoinParticipant3WhileParticipant2Expired()}.
 * 5. Re-join participant2 with "failICE" and see if others are notified
 *    {@link #testParticipant2InterruptedDuringCall()}. This checks if the JVB will
 *    send disconnected notification for the participant who has never
 *    connected, but for whom the channels have been allocated.
 *
 * @author Pawel Domas
 */
public class PeerConnectionStatusTest
    extends WebTestBase
{
    /**
     * Name of the system property which point to the firewall script used to
     * block the ports. See {@link #firewallScript}.
     */
    private static final String FIREWALL_SCRIPT_PROP_NAME = "firewall.script";

    /**
     * Default firewall script which will work only on linux.
     */
    private static final String DEFAULT_FIREWALL_SCRIPT
        = "scripts/firewall_script.sh";

    /**
     * Stores the path to the firewall script. It is expected that the script
     * supports two commands:
     * <p>
     * 1. "--block-port {port number}" will adjust firewall rules to block both
     * UDP and TCP (inbound+outbound) traffic on the given port number.
     * If the script is called twice with different ports it is not important
     * if the previously blocked port gets unblocked.
     * MUST always drop traffic from any to JVB's TCP port (4443 by default)
     * <p>
     * 2. "--unblock-port {port number}" will remove the rules blocking given
     * port(should revert "--block-port {port number}").
     */
    private static String firewallScript;

    /**
     * Stores the RTP bundle port number of participant2.
     */
    private static int bundlePort2 = -1;

    @Override
    public boolean skipTestByDefault()
    {
        return true;
    }

    @Override
    public void setupClass()
    {
        super.setupClass();

        firewallScript = System.getProperty(FIREWALL_SCRIPT_PROP_NAME);
        if (firewallScript == null)
        {
            if ("linux".equalsIgnoreCase(System.getProperty("os.name")))
            {
                firewallScript = DEFAULT_FIREWALL_SCRIPT;
            }
            else
            {
                print(
                    "WARN no firewall script has been specified and "
                        + "the PeerConnectionStatusTest will not be "
                        + "executed!");
                cleanupClass();
                throw new SkipException(
                    "no firewall script has been specified");
            }
        }

        ensureOneParticipant();
    }

    /**
     * Calls {@link #firewallScript} to block given port.
     *
     * @param portNumber the port number to be blocked.
     * @throws Exception if anything goes wrong.
     */
    private static void blockPort(int portNumber)
        throws Exception
    {
        if (portNumber == -1)
        {
            throw new IllegalArgumentException("Trying to block port -1");
        }

        CmdExecutor cmdExecutor = new CmdExecutor();

        List<String> cmdArgs = new LinkedList<>();

        cmdArgs.add(firewallScript);
        cmdArgs.add("--block-port");
        cmdArgs.add(String.valueOf(portNumber));

        print("Will block port: " + portNumber);

        cmdExecutor.executeCmd(cmdArgs);
    }

    /**
     * Unblocks the previously blocked port.
     *
     * @param portNumber the port number to unblock.
     * @throws Exception if anything goes wrong.
     */
    private static void unblockPort(int portNumber)
        throws Exception
    {
        if (portNumber == -1)
        {
            throw new IllegalArgumentException("Trying to unblock port -1");
        }

        CmdExecutor cmdExecutor = new CmdExecutor();

        List<String> cmdArgs = new LinkedList<>();

        cmdArgs.add(firewallScript);
        cmdArgs.add("--unblock-port");
        cmdArgs.add(String.valueOf(portNumber));

        print("Will unblock port: " + portNumber);

        cmdExecutor.executeCmd(cmdArgs);
    }

    /**
     * Makes sure that there are 2 participants in the conference.
     */
    @Test
    public void testInitialize()
    {
        // Start the second participant with TCP disabled, so that we can
        // force it to disconnect just by blocking the UDP port.
        ensureTwoParticipants(
            null,
            getJitsiMeetUrl()
                .appendConfig(TCPTest.DISABLE_TCP_URL_FRAGMENT));
    }

    /**
     * Checks the UI indication for the participant whose connection is
     * disrupted during the call.
     *
     * @throws Exception if something goes wrong.
     */
    @Test(dependsOnMethods = { "testInitialize" })
    public void testParticipant2InterruptedDuringCall()
        throws Exception
    {
        WebDriver driver1 = getParticipant1().getDriver();
        assertNotNull(driver1);
        WebDriver driver2 = getParticipant2().getDriver();
        assertNotNull(driver2);

        // 1. Block media flow on 2nd and check if is indicated as disconnected
        // XXX getting the bundle port won't work correctly on FF because
        // it relies on webrtc-stats collection (which is disabled in FF) and
        // on the Conn-audio-1-0-googLocalAddress which is a goog prefixed
        // webrtc-stat and doesn't exist on FF. The old way of getting the
        // bundle port doesn't work on the grid because of trickle-ice: the
        // SDP contains only host candidates, whereas the nodes connect with
        // each other using peer reflexive candidates.
        bundlePort2 = MeetUtils.getBundlePort(driver2, true);
        print("Local bundle port for participant2: " + bundlePort2);
        blockPort(bundlePort2);

        // 2. Select 2nd participant on Owner
        MeetUIUtils.selectRemoteVideo(
            driver1, getParticipant2().getEndpointId());
        // At this point user 2 thumb should be a display name
        MeetUIUtils.assertDisplayNameVisible(
            driver1, getParticipant2().getEndpointId());

        // Check if participant2 is marked as "disconnected" from participant1's
        // view
        MeetUIUtils.verifyUserConnStatusIndication(
            driver1, getParticipant2().getEndpointId(), false /* disconnected */);
        MeetUIUtils.assertLargeVideoIsGrey(driver1);

        // 3. Switch to local video
        MeetUIUtils.selectLocalVideo(driver1);
        // Now we're expecting to see gray video thumbnail
        MeetUIUtils.assertGreyVideoThumbnailDisplayed(
            driver1, getParticipant2().getEndpointId());

        // Check also participant2's local UI for indication
        MeetUIUtils.verifyLocalConnStatusIndication(
            driver2, false /* disconnected */);

        // 4. Select remote again
        MeetUIUtils.selectRemoteVideo(
            driver1, getParticipant2().getEndpointId());

        // Mute video(this is not real live scenario, but we want to make
        // sure that the grey avatar is displayed if the connection gets
        // disrupted while video muted)
        StopVideoTest stopVideoTest = new StopVideoTest(this);
        stopVideoTest.stopVideoOnParticipantAndCheck();
        MeetUIUtils.assertGreyAvatarOnLarge(driver1);

        // The avatar should remain
        stopVideoTest.startVideoOnParticipantAndCheck();
        MeetUIUtils.assertGreyAvatarOnLarge(driver1);
    }

    /**
     * Unblocks participant2's ports and see if UI has been updated
     * accordingly.
     *
     * @throws Exception if anything unexpected happens.
     */
    @Test(dependsOnMethods = {"testParticipant2InterruptedDuringCall"})
    public void testParticipant2Restored()
        throws Exception
    {
        WebDriver driver1 = getParticipant1().getDriver();
        WebDriver driver2 = getParticipant2().getDriver();

        // 1. Unlock the port and see if we recover
        unblockPort(bundlePort2);

        // 2. Verify if connection has restored
        MeetUIUtils.verifyUserConnStatusIndication(
            driver1, getParticipant2().getEndpointId(), true);
        MeetUIUtils.assertLargeVideoNotGrey(driver1);

        // 3. Check local status
        MeetUIUtils.verifyLocalConnStatusIndication(driver2, true);
    }

    /**
     * Blocks participant2's ports until they expire on the JVB and then join
     * with participant3 to see if the notification has been sent as expected.
     *
     * @throws Exception if something goes wrong
     */
    @Test(dependsOnMethods = {"testParticipant2Restored"})
    public void testParticipant2Expired()
        throws Exception
    {
        WebDriver driver1 = getParticipant1().getDriver();
        assertNotNull(driver1);
        WebDriver driver2 = getParticipant2().getDriver();
        assertNotNull(driver2);

        // The purpose of next steps is to check if JVB sends
        // "interrupted" notifications about the user's whose channels have
        // expired.

        // Block the port and wait for the channels to expire on
        // the bridge(unrecoverable)
        blockPort(bundlePort2);
        MeetUIUtils.verifyUserConnStatusIndication(
            driver1, getParticipant2().getEndpointId(), false);
        MeetUIUtils.verifyLocalConnStatusIndication(driver2, false);

        // FIXME it would be better to expire channels somehow
        TestUtils.waitMillis(65000);
    }

    /**
     * Join with participant3 while participant2 stays in the room with expired
     * channels. The point of these is to make sure that the JVB has notified
     * participant3 correctly.
     */
    @Test(dependsOnMethods = {"testParticipant2Expired"})
    public void testJoinParticipant3WhileParticipant2Expired()
        throws Exception
    {
        // 8. Join with participant3 and see if the user is marked as interrupted
        ensureThreeParticipants();

        WebDriver driver1 = getParticipant1().getDriver();
        assertNotNull(driver1);
        WebDriver driver2 = getParticipant2().getDriver();
        assertNotNull(driver2);
        WebDriver driver3 = getParticipant3().getDriver();
        assertNotNull(driver3);

        MeetUIUtils.verifyUserConnStatusIndication(
            driver3, getParticipant2().getEndpointId(), false);
        MeetUIUtils.verifyUserConnStatusIndication(
            driver3, getParticipant1().getEndpointId(), true);

        // Select local video
        MeetUIUtils.selectLocalVideo(driver3);
        // User 2 should be a grey avatar
        MeetUIUtils.assertGreyAvatarDisplayed(
            driver3, getParticipant2().getEndpointId());
        // Select disconnected participant
        MeetUIUtils.selectRemoteVideo(
            driver3, getParticipant2().getEndpointId());
        MeetUIUtils.assertGreyAvatarOnLarge(driver3);

        // Unblock the port
        unblockPort(bundlePort2);
    }

    /**
     * Closes participant2 and joins with special flag "failICE" set to true
     * in order to fail ICE initially. The bridge should notify others when
     * the user fails to establish ICE connection.
     */
    @Test(dependsOnMethods = {"testJoinParticipant3WhileParticipant2Expired"})
    public void testParticipant2FailsICEOnJoin()
    {
        WebDriver driver1 = getParticipant1().getDriver();
        assertNotNull(driver1);
        WebDriver driver2 = getParticipant2().getDriver();
        assertNotNull(driver2);
        WebDriver driver3 = getParticipant3().getDriver();
        assertNotNull(driver3);

        // Close participant2 and join with failICE=true
        getParticipant2().hangUp();
        ensureTwoParticipants(
            null,
            getJitsiMeetUrl().appendConfig("config.failICE=true"));

        driver2 = getParticipant2().getDriver();
        assertNotNull(driver2);

        // It will be marked as disconnected only after 15 seconds since
        // the channels have been allocated.
        TestUtils.waitMillis(17000);
        // Now see if others will see him
        MeetUIUtils.verifyUserConnStatusIndication(
            driver1, getParticipant2().getEndpointId(), false);
        MeetUIUtils.verifyUserConnStatusIndication(
            driver3, getParticipant2().getEndpointId(), false);

        getParticipant3().hangUp();
    }

    /**
     * Executed at the end of the suite to clear any blocked ports
     * in case any of the tests fails.
     */
    @Override
    public void cleanupClass()
    {
        super.cleanupClass();

        try
        {
            if (bundlePort2 != -1)
            {
                unblockPort(bundlePort2);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
