/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jitsi.meet.test;

import junit.framework.*;

import org.openqa.selenium.*;

/**
 * This test will setup the conference and will end when both
 * participants are connected.
 * We order tests alphabetically and use the stage1,2,3... to order them.
 *
 * @author Damian Minkov
 */
public class SetupConference
    extends TestCase
{
    /**
     * Constructs test.
     * @param name method name.
     */
    public SetupConference(String name)
    {
        super(name);
    }

    /**
     * Orders tests.
     * @return the suite with order tests.
     */
    public static junit.framework.Test suite()
    {
        TestSuite suite = new TestSuite();

        suite.addTest(new SetupConference("startFocus"));
        suite.addTest(new SetupConference("checkFocusJoinRoom"));
        suite.addTest(new SetupConference("startSecondParticipant"));
        suite.addTest(new SetupConference("checkSecondParticipantJoinRoom"));
        suite.addTest(new SetupConference("waitsFocusToJoinConference"));
        suite.addTest(new SetupConference("waitsSecondParticipantToJoinConference"));

        return suite;
    }

    /**
     * First starts the focus.
     */
    public void startFocus()
    {
        ConferenceFixture.startFocus();
    }

    /**
     * Checks whether focus joined the room.
     */
    public void checkFocusJoinRoom()
    {
        // first lets wait 10 secs to join
        checkParticipantToJoinRoom(ConferenceFixture.focus, 10);
    }

    /**
     * Checks whether participant has joined the room
     * @param participant where we check
     * @param timeout the time to wait for the event.
     */
    private void checkParticipantToJoinRoom(WebDriver participant, long timeout)
    {
        TestUtils.waitsForBoolean(
            participant,
            "return connection.emuc.joined;",
            timeout);
    }

    /**
     * Starts the second participant.
     */
    public void startSecondParticipant()
    {
        ConferenceFixture.startParticipant();
    }

    /**
     * Checks whether the second participant has joined the room.
     */
    public void checkSecondParticipantJoinRoom()
    {
        checkParticipantToJoinRoom(ConferenceFixture.secondParticipant, 10);
    }

    /**
     * Waits the focus to get event for iceConnectionState that changes
     * to connected.
     */
    public void waitsFocusToJoinConference()
    {
        TestUtils.waitsForBoolean(
            ConferenceFixture.focus,
            "return focus !== null"
            ,10);
        TestUtils.waitsForBoolean(
            ConferenceFixture.focus,
            "return (typeof focus.peerconnection !== 'undefined');"
            ,10);

        // lets wait till the state becomes connected
        TestUtils.waitsForEqualsStrings(
            ConferenceFixture.focus,
            "return focus.peerconnection.iceConnectionState;",
            "connected",
            30
        );
    }

    /**
     * Waits the participant to get event for iceConnectionState that changes
     * to connected.
     */
    public void waitsSecondParticipantToJoinConference()
    {
        TestUtils.waitsForBoolean(
            ConferenceFixture.focus,
            "for (sid in connection.sessions) {" +
                "if (connection.sessions[sid].iceConnectionState " +
                        "!== 'connected')" +
                    "return false;" +
            "}" +
            "return true;"
            ,30);
    }
}
