/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jitsi.meet.test;

import junit.framework.*;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;

import java.util.*;

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
        suite.addTest(new SetupConference("waitForFocusSendReceiveData"));
        suite.addTest(new SetupConference("waitForSecondParticipantSendReceiveData"));

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
        ConferenceFixture.checkParticipantToJoinRoom(
            ConferenceFixture.getFocus(), 10);
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
        ConferenceFixture.checkParticipantToJoinRoom(
            ConferenceFixture.getSecondParticipant(), 10);
    }

    /**
     * Waits the focus to get event for iceConnectionState that changes
     * to connected.
     */
    public void waitsFocusToJoinConference()
    {
        ConferenceFixture.waitsParticipantToJoinConference(
            ConferenceFixture.getFocus());
    }

    /**
     * Waits the participant to get event for iceConnectionState that changes
     * to connected.
     */
    public void waitsSecondParticipantToJoinConference()
    {
        ConferenceFixture.waitsParticipantToJoinConference(
            ConferenceFixture.getSecondParticipant());
    }

    /**
     * Checks statistics for received and sent bitrate.
     * @param participant the participant to check.
     */
    private void waitForSendReceiveData(WebDriver participant)
    {
        new WebDriverWait(participant, 15)
            .until(new ExpectedCondition<Boolean>()
            {
                public Boolean apply(WebDriver d)
                {
                    Map stats = (Map)((JavascriptExecutor) ConferenceFixture.getFocus())
                        .executeScript("return connectionquality.getStats();");

                    Map<String,Long> bitrate =
                        (Map<String,Long>)stats.get("bitrate");

                    if(bitrate != null)
                    {
                        long download =  bitrate.get("download");
                        long upload = bitrate.get("upload");

                        if(download > 0 && upload > 0)
                            return true;
                    }

                    return false;
                }
            });
    }

    /**
     * Checks statistics for received and sent bitrate.
     */
    public void waitForFocusSendReceiveData()
    {
        waitForSendReceiveData(ConferenceFixture.getFocus());
    }

    /**
     * Checks statistics for received and sent bitrate.
     */
    public void waitForSecondParticipantSendReceiveData()
    {
        waitForSendReceiveData(ConferenceFixture.getSecondParticipant());
    }
}
