/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jitsi.meet.test.util;

import org.openqa.selenium.*;

/**
 * Class contains utility methods related with jitsi-meet application logic.
 *
 * @author Pawel Domas
 */
public class MeetUtils
{
    /**
     * Returns resource JID which corresponds to XMPP MUC nickname of the given
     * <tt>participant</tt>.
     *
     * @param participant the <tt>WebDriver</tt> instance which runs conference
     *                    participant.
     */
    public static String getResourceJid(WebDriver participant)
    {
        return (String)((JavascriptExecutor) participant)
            .executeScript("return APP.xmpp.myResource();");
    }
}
