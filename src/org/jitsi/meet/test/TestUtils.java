/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jitsi.meet.test;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;

/**
 * Utility class.
 * @author Damian Minkov
 */
public class TestUtils
{
    public static void waitsForBoolean(
        WebDriver participant,
        final String scriptToExecute,
        long timeout)
    {
        (new WebDriverWait(participant, timeout))
            .until(new ExpectedCondition<Boolean>()
            {
                public Boolean apply(WebDriver d)
                {
                    Object res = ((JavascriptExecutor) ConferenceFixture.focus)
                        .executeScript(scriptToExecute);
                    return res != null && res.equals(Boolean.TRUE);
                }
            });
    }
}
