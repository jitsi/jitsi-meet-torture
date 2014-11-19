/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jitsi.meet.test;

import junit.framework.*;
import org.openqa.selenium.*;

import java.io.*;
import java.util.*;

/**
 * WARNING: This will play with firewall rules, make sut
 * WARNING: This tests need external system configuration
 * the tests expect that the current user running the tests has sudo
 * right to execute the firewall utility.
 * Linux: sudo iptables
 * you can add the following in /etc/sudoers:
 * <username> ALL=NOPASSWD:/sbin/iptables
 * <username> ALL=NOPASSWD:/sbin/ip6tables
 * Macosx:
 * you can add the following in /etc/sudoers:
 * <username> ALL=(ALL) NOPASSWD: /sbin/ipfw
 *
 * To indicate that this is set an explicit property must be added to the
 * tests command line. -Djitsi-meet.sudo.fw.configured=true
 *
 * @author Damian Minkov
 */
public class TCPTest
    extends TestCase
{
    /**
     * The property that will indicate that user has configured sudo execution
     * of the firewall utility.
     */
    public static final String JITSI_MEET_SUDO_CONFIGED_PROP
        = "jitsi-meet.sudo.fw.configured";

    /**
     * Constructs test.
     * @param name the method name for the test.
     */
    public TCPTest(String name)
    {
        super(name);
    }

    /**
     * Orders the tests.
     * @return the suite with order tests.
     */
    public static junit.framework.Test suite()
    {
        TestSuite suite = new TestSuite();

        suite.addTest(new TCPTest("tcpTest"));
        suite.addTest(new TCPTest("removeRules"));

        return suite;
    }

    /**
     * Gets the currently connected addresses from focus and prints them.
     * Stops current instances.
     * Adds rules to forbid udp traffic.
     * Starts the focus and participant again.
     * Takes the currently connected addresses from focus and prints them.
     * Checks whether were are connected with TCP.
     * Clears the added firewall rules.
     */
    public void tcpTest()
    {
        // just waits the stats to become available
        TestUtils.waits(5000);

        Map<String,String> focusIPAddresses = printFocusConnectInfo();

        new DisposeConference().testDispose();
        TestUtils.waits(1000);

        // lets remove udp connection
        try
        {
            addFWRules();
        }
        catch(Throwable t)
        {
            t.printStackTrace();

            fail("Cannot deny udp connection to the bridge");
        }

        SetupConference setup = new SetupConference(getName());
        setup.startFocus();
        setup.checkFocusJoinRoom();
        setup.startSecondParticipant();
        setup.checkSecondParticipantJoinRoom();
        setup.waitsFocusToJoinConference();
        setup.waitsSecondParticipantToJoinConference();

        // just waits the stats to become available
        TestUtils.waits(5000);
        Map<String,String> focusIPAddressesToCheck = printFocusConnectInfo();

        for(String p : focusIPAddressesToCheck.values())
        {
            assertEquals("We must be connected through tcp", "tcp", p);
        }

        setup.waitForFocusSendReceiveData();
        setup.waitForSecondParticipantSendReceiveData();
    }

    /**
     * Make sure we remove rules in separate method (test), if tcp test
     * fails make sure we will remove the rules in any case.
     */
    public void removeRules()
    {
        // lets remove rules we have added
        try
        {
            removeFWRules();
        }
        catch(Throwable t)
        {
            t.printStackTrace();

            fail("Cannot restore firewall as it was");
        }
    }

    /**
     * Add firewall rules to stop udp traffic except dns.
     * Works on MAC and Linux.
     * @throws IOException if we cannot execute the commands, maybe sudo is
     * not setup.
     */
    private void addFWRules()
        throws IOException
    {
        if(TestUtils.IS_MAC)
        {
            executeFirewallRule(
                "sudo ipfw add 01000 allow udp from any to any dst-port 53");
            executeFirewallRule(
                "sudo ipfw add 01002 allow udp from any 53 to any");
            executeFirewallRule(
                "sudo ipfw add 01003 deny udp from any to any");
        }
        else if(TestUtils.IS_LINUX)
        {
            executeFirewallRule(
                "sudo iptables -I OUTPUT -p udp -j DROP");
            executeFirewallRule(
                "sudo iptables -I OUTPUT -p udp --dport 53 -j ACCEPT");
            executeFirewallRule(
                "sudo iptables -I INPUT -p udp -j DROP");
            executeFirewallRule(
                "sudo iptables -I INPUT -p udp --sport 53 -j ACCEPT");
            executeFirewallRule(
                "sudo ip6tables -I OUTPUT -p udp -j DROP");
            executeFirewallRule(
                "sudo ip6tables -I OUTPUT -p udp --dport 53 -j ACCEPT");
            executeFirewallRule(
                "sudo ip6tables -I INPUT -p udp -j DROP");
            executeFirewallRule(
                "sudo ip6tables -I INPUT -p udp --sport 53 -j ACCEPT");
        }
    }

    /**
     * Removes added firewall rules. Works on MAC and Linux.
     * @throws IOException if we cannot execute the commands, maybe sudo is
     * not setup.
     */
    private void removeFWRules()
        throws IOException
    {
        if(TestUtils.IS_MAC)
        {
            executeFirewallRule("sudo ipfw delete 01000");
            executeFirewallRule("sudo ipfw delete 01002");
            executeFirewallRule("sudo ipfw delete 01003");
        }
        else if(TestUtils.IS_LINUX)
        {
            executeFirewallRule(
                "sudo iptables -D OUTPUT -p udp -j DROP");
            executeFirewallRule(
                "sudo iptables -D OUTPUT -p udp --dport 53 -j ACCEPT");
            executeFirewallRule(
                "sudo iptables -D INPUT -p udp -j DROP");
            executeFirewallRule(
                "sudo iptables -D INPUT -p udp --sport 53 -j ACCEPT");
            executeFirewallRule(
                "sudo ip6tables -D OUTPUT -p udp -j DROP");
            executeFirewallRule(
                "sudo ip6tables -D OUTPUT -p udp --dport 53 -j ACCEPT");
            executeFirewallRule(
                "sudo ip6tables -D INPUT -p udp -j DROP");
            executeFirewallRule(
                "sudo ip6tables -D INPUT -p udp --sport 53 -j ACCEPT");
        }
    }

    /**
     * Executes the command, do not fail in any case and waits
     * a little before executing.
     * @param rule the rule
     */
    private void executeFirewallRule(String rule)
    {
        // give some time for the utils to work, do not burst commands
        // tries to fix a problem where on linux some rules stay after
        // execution, and breaks future tests
        TestUtils.waits(500);
        try
        {
            Runtime.getRuntime().exec(rule);
        }
        catch(Throwable t)
        {
            t.printStackTrace();
        }
    }

    /**
     * Returns the currently connected addresses from focus and prints them.
     * @return the currently connected addresses from focus and prints them.
     */
    private Map<String,String> printFocusConnectInfo()
    {
        Map stats = (Map)((JavascriptExecutor) ConferenceFixture.getFocus())
            .executeScript("return ConnectionQuality.getStats();");

        Map<String,String> focusIPAddresses = new HashMap<String, String>();
        List<Map<String,String>> transports = (List)stats.get("transport");

        System.out.println("Currently connected to:");

        for(Map<String,String> t : transports)
        {
            String protocol = t.get("type");
            String address = t.get("ip");
            String ipAddress = address.substring(0, address.lastIndexOf(':'));
            focusIPAddresses.put(ipAddress, protocol);

            System.out.println(protocol + ":" + ipAddress);
        }

        return focusIPAddresses;
    }

}
