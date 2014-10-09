/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jitsi.meet.test;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.*;

/**
 * The static fixture which holds the drivers to access the conference
 * participant pages.
 *
 * @author Damian Minkov
 */
public class ConferenceFixture
{
    public static final String JITSI_MEET_URL_PROP = "jitsi-meet.instance.url";

    public static String currentRoomName;

    public static WebDriver focus;
    public static WebDriver secondParticipant;

    /**
     * Starts the focus, the first participant that generates the random
     * room name.
     */
    public static void startFocus()
    {
        focus = startChromeInstance();

        currentRoomName = "torture"
            + String.valueOf((int)(Math.random()*1000000));

        openRoom(focus);
    }

    /**
     * Opens the room for the given participant.
     * @param participant to open the current test room.
     */
    public static void openRoom(WebDriver participant)
    {
        participant.get(System.getProperty(JITSI_MEET_URL_PROP) + "/"
            + currentRoomName);

        // fighting a bug where clicking on buttons on the toolbar sometimes
        // fail and some of the tests fail from time to time, those with
        // stopping the second participant
        ((JavascriptExecutor) participant)
            .executeScript("interfaceConfig.TOOLBAR_TIMEOUT = 20000;");
    }

    /**
     * Starts chrome instance using some default settings.
     * @return the webdriver instance.
     */
    private static WebDriver startChromeInstance()
    {
        ChromeOptions ops = new ChromeOptions();
        ops.addArguments("use-fake-ui-for-media-stream");
        ops.addArguments("use-fake-device-for-media-stream");
        ops.addArguments("vmodule=\"*media/*=3,*turn*=3\"");
        return new ChromeDriver(ops);
    }

    /**
     * Start another participant reusing the already generated room name.
     */
    public static void startParticipant()
    {
        secondParticipant = startChromeInstance();

        openRoom(secondParticipant);
    }

    /**
     * Checks whether participant has joined the room
     * @param participant where we check
     * @param timeout the time to wait for the event.
     */
    public static void checkParticipantToJoinRoom(
        WebDriver participant, long timeout)
    {
        TestUtils.waitsForBoolean(
            participant,
            "return connection.emuc.joined;",
            timeout);
    }

    /**
     * Waits the focus to get event for iceConnectionState that changes
     * to connected.
     */
    public static void waitsFocusToJoinConference()
    {
        TestUtils.waitsForBoolean(
            focus,
            "return focus !== null"
            ,10);
        TestUtils.waitsForBoolean(
            focus,
            "return (typeof focus.peerconnection !== 'undefined');"
            ,10);

        // lets wait till the state becomes connected
        TestUtils.waitsForEqualsStrings(
            focus,
            "return focus.peerconnection.iceConnectionState;",
            "connected",
            30
        );
    }

    /**
     * Waits the participant to get event for iceConnectionState that changes
     * to connected.
     */
    public static void waitsSecondParticipantToJoinConference()
    {
        TestUtils.waitsForBoolean(
            secondParticipant,
            "for (sid in connection.sessions) {" +
                "if (connection.sessions[sid].iceConnectionState " +
                "!== 'connected')" +
                "return false;" +
                "}" +
                "return true;"
            ,30);
    }

    /**
     * Quits the participant.
     * @param participant to quit.
     */
    public static void quit(WebDriver participant)
    {
        try
        {
            participant.close();

            participant.quit();
        }
        catch(Throwable t)
        {
            t.printStackTrace();
        }
    }
}
