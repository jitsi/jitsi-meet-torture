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

    public static void startFocus()
    {
        focus = startChromeInstance();

        currentRoomName = "torture"
            + String.valueOf((int)(Math.random()*1000000));

        focus.get(System.getProperty(JITSI_MEET_URL_PROP) + "/"
            + currentRoomName);
    }

    private static WebDriver startChromeInstance()
    {
        ChromeOptions ops = new ChromeOptions();
        ops.addArguments("use-fake-ui-for-media-stream");
        ops.addArguments("use-fake-device-for-media-stream");
        ops.addArguments("vmodule=\"*media/*=3,*turn*=3\"");
        return new ChromeDriver(ops);
    }

    public static void startSecondParticipant()
    {
        secondParticipant = startChromeInstance();

        secondParticipant.get(System.getProperty(JITSI_MEET_URL_PROP) + "/"
            + currentRoomName);
    }
}
