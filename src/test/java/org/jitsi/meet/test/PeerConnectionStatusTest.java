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

/**
 * The test checks the UI for displaying notifications about participants media
 * connectivity status(connection between the peer and the JVB).
 *
 * 1. Join with 2 participants {@link #testInitialize()}.
 * 2. Block media flow on 2nd and check if is indicated as disconnected
 *    {@link #test2ndPeerInterruptedDuringCall()}.
 * 3. Unblock the ports and see if the UI updated accordingly
 *    {@link #test2ndPeerRestored()}.
 * 4. Block the 2nd peer media ports and wait until channels expire on
 *    the bridge then join with the 3rd participant and test switching between
 *    participants {@link #testJoin3rdWhile2ndExpired()}.
 * 5. Re-join 2nd with "failICE" and see if others are notified
 *    {@link #test2ndPeerInterruptedDuringCall()}. This checks if the JVB will
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
     * MUST always drop traffic from any to JVB's TCP port(4443 by default)
     * <p>
     * 2. "--unblock-port {port number}" will remove the rules blocking given
     * port(should revert "--block-port {port number}").
     */
    private static String firewallScript;

    /**
     * Stores the RTP bundle port number of the 2nd participant.
     */
    private static int peer2bundlePort = -1;

    @Override
    public boolean skipTestByDefault()
    {
        return true;
    }

    @Override
    public void setup()
    {
        super.setup();

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
            throw new IllegalArgumentException("Trying to block port -1");

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
            throw new IllegalArgumentException("Trying to unblock port -1");

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
        ensureTwoParticipants(TCPTest.DISABLE_TCP_URL_FRAGMENT);
    }

    /**
     * Checks the UI indication for the participant whose connection is
     * disrupted during the call.
     *
     * @throws Exception if something goes wrong.
     */
    @Test(dependsOnMethods = { "testInitialize" })
    public void test2ndPeerInterruptedDuringCall()
        throws Exception
    {
        WebDriver owner = getParticipant1().getDriver();
        assertNotNull(owner);
        WebDriver secondPeer = getParticipant2().getDriver();
        assertNotNull(secondPeer);

        // 1. Block media flow on 2nd and check if is indicated as disconnected
        peer2bundlePort = MeetUtils.getBundlePort(secondPeer);
        print(
            "Local bundle port for 2: " + peer2bundlePort);
        blockPort(peer2bundlePort);

        // 2. Select 2nd participant on Owner
        MeetUIUtils.selectRemoteVideo(owner, secondPeer);
        // At this point user 2 thumb should be a display name
        MeetUIUtils.assertDisplayNameVisible(owner, secondPeer);

        // Check if 2nd user is marked as "disconnected" from 1st peer view
        MeetUIUtils.verifyUserConnStatusIndication(
            owner, secondPeer, false /* disconnected */);
        MeetUIUtils.assertLargeVideoIsGrey(owner);

        // 3. Switch to local video
        MeetUIUtils.selectLocalVideo(owner);
        // Now we're expecting to see gray video thumbnail
        MeetUIUtils.assertGreyVideoThumbnailDisplayed(owner, secondPeer);

        // Check also 2nd participant's local UI for indication
        MeetUIUtils.verifyLocalConnStatusIndication(
            secondPeer, false /* disconnected */);

        // 4. Select remote again
        MeetUIUtils.selectRemoteVideo(owner, secondPeer);

        // Mute video(this is not real live scenario, but we want to make
        // sure that the grey avatar is displayed if the connection gets
        // disrupted while video muted)
        StopVideoTest stopVideoTest = new StopVideoTest(this);
        stopVideoTest.stopVideoOnParticipantAndCheck();
        MeetUIUtils.assertGreyAvatarOnLarge(owner);

        // The avatar should remain
        stopVideoTest.startVideoOnParticipantAndCheck();
        MeetUIUtils.assertGreyAvatarOnLarge(owner);
    }

    /**
     * Unblocks 2nd participant's ports and see if UI has been updated
     * accordingly.
     *
     * @throws Exception if anything unexpected happens.
     */
    @Test(dependsOnMethods = { "test2ndPeerInterruptedDuringCall" })
    public void test2ndPeerRestored()
        throws Exception
    {
        WebDriver owner = getParticipant1().getDriver();
        WebDriver secondPeer = getParticipant2().getDriver();

        // 1. Unlock the port and see if we recover
        unblockPort(peer2bundlePort);

        // 2. Verify if connection has restored
        MeetUIUtils.verifyUserConnStatusIndication(owner, secondPeer, true);
        MeetUIUtils.assertLargeVideoNotGrey(owner);

        // 3. Check local status
        MeetUIUtils.verifyLocalConnStatusIndication(secondPeer, true);
    }

    /**
     * Blocks 2nd participant's ports until they expire on the JVB and then join
     * with 3rd to see if the notification has been sent as expected.
     *
     * @throws Exception if something goes wrong
     */
    @Test(dependsOnMethods = { "test2ndPeerRestored" })
    public void test2ndPeerExpired()
        throws Exception
    {
        WebDriver owner = getParticipant1().getDriver();
        assertNotNull(owner);
        WebDriver secondPeer = getParticipant2().getDriver();
        assertNotNull(secondPeer);

        // The purpose of next steps is to check if JVB sends
        // "interrupted" notifications about the user's whose channels have
        // expired.

        // Block the port and wait for the channels to expire on
        // the bridge(unrecoverable)
        blockPort(peer2bundlePort);
        MeetUIUtils.verifyUserConnStatusIndication(owner, secondPeer, false);
        MeetUIUtils.verifyLocalConnStatusIndication(secondPeer, false);

        // FIXME it would be better to expire channels somehow
        TestUtils.waitMillis(65000);
    }

    /**
     * Join with the 3rd participant while 2nd stays in the room with expired
     * channels. The point of these is to make sure that the JVB has notified
     * the 3rd peer correctly.
     */
    @Test(dependsOnMethods = { "test2ndPeerExpired" })
    public void testJoin3rdWhile2ndExpired()
        throws Exception
    {
        // 8. Join with 3rd and see if the user is marked as interrupted
        ensureThreeParticipants();

        WebDriver owner = getParticipant1().getDriver();
        assertNotNull(owner);
        WebDriver secondPeer = getParticipant2().getDriver();
        assertNotNull(secondPeer);
        WebDriver thirdPeer = getParticipant3().getDriver();
        assertNotNull(thirdPeer);

        MeetUIUtils.verifyUserConnStatusIndication(
            thirdPeer, secondPeer, false);
        MeetUIUtils.verifyUserConnStatusIndication(
            thirdPeer, owner, true);

        // Select local video
        MeetUIUtils.selectLocalVideo(thirdPeer);
        // User 2 should be a grey avatar
        MeetUIUtils.assertGreyAvatarDisplayed(thirdPeer, secondPeer);
        // Select disconnected participant
        MeetUIUtils.selectRemoteVideo(thirdPeer, secondPeer);
        MeetUIUtils.assertGreyAvatarOnLarge(thirdPeer);

        // Unblock the port
        unblockPort(peer2bundlePort);
    }

    /**
     * Closes 2nd participant and joins with special flag "failICE" set to true
     * in order to fail ICE initially. The bridge should notify others when
     * the user fails to establish ICE connection.
     */
    @Test(dependsOnMethods = { "testJoin3rdWhile2ndExpired" })
    public void test2ndFailsICEOnJoin()
    {
        WebDriver owner = getParticipant1().getDriver();
        assertNotNull(owner);
        WebDriver secondPeer = getParticipant2().getDriver();
        assertNotNull(secondPeer);
        WebDriver thirdPeer = getParticipant3().getDriver();
        assertNotNull(thirdPeer);

        // Close 2nd and join with failICE=true
        getParticipant2().hangUp();
        ensureTwoParticipants("config.failICE=true");

        secondPeer = getParticipant2().getDriver();
        assertNotNull(secondPeer);

        // It will be marked as disconnected only after 15 seconds since
        // the channels have been allocated.
        TestUtils.waitMillis(17000);
        // Now see if others will see him
        MeetUIUtils.verifyUserConnStatusIndication(
            owner, secondPeer, false);
        MeetUIUtils.verifyUserConnStatusIndication(
            thirdPeer, secondPeer, false);

        getParticipant3().hangUp();
    }

    /**
     * Executed at the end of the suite to clear any blocked ports
     * in case any of the tests fails.
     */
    @Override
    public void cleanup()
    {
        super.cleanup();

        try
        {
            if (peer2bundlePort != -1)
            {
                unblockPort(peer2bundlePort);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}