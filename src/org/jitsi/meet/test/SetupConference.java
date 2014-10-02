/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jitsi.meet.test;

import org.junit.*;
import org.junit.runners.*;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;

/**
 * This test will setup the conference and will end when both
 * participants are connected.
 * We order tests alphabetically and use the stage1,2,3... to order them.
 *
 * @author Damian Minkov
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SetupConference
{
    @Test
    public void stage1_StartFocus()
    {
        ConferenceFixture.startFocus();
    }

    @Test
    public void stage2_CheckFocusStart()
    {
        // first lets wait 10 secs to join
        checkParticipantToJoinRoom(ConferenceFixture.focus, 10);
    }

    private void checkParticipantToJoinRoom(WebDriver participant, long timeout)
    {
        (new WebDriverWait(participant, timeout))
            .until(new ExpectedCondition<Boolean>()
            {
                public Boolean apply(WebDriver d)
                {
                    Object res = ((JavascriptExecutor) ConferenceFixture.focus)
                        .executeScript("return connection.emuc.joined;");
                    return res != null && res.equals(Boolean.TRUE);
                }
            });
    }

    @Test
    public void stage3_StartSecondParticipant()
    {
        ConferenceFixture.startSecondParticipant();
    }

    @Test
    public void stage4_CheckSecondParticipant()
    {
        checkParticipantToJoinRoom(ConferenceFixture.secondParticipant, 10);
    }
}
