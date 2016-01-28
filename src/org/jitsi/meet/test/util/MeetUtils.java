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
package org.jitsi.meet.test.util;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.*;

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
     * @return resource JID which corresponds to XMPP MUC nickname of the given
     * <tt>participant</tt>.
     */
    public static String getResourceJid(WebDriver participant)
    {
        return (String)((JavascriptExecutor) participant)
            .executeScript("return APP.conference.getMyUserId();");
    }

    /**
     * Checks whether given <tt>WebDriver</tt> instance has RTP stats support.
     *
     * @param driver the <tt>WebDriver</tt> for which we're getting the info
     *
     * @return <tt>true</tt> if given <tt>driver</tt> has RTP stats support.
     */
    public static boolean areRtpStatsSupported(WebDriver driver)
    {
        return driver instanceof ChromeDriver;
    }

    /**
     * NOTE: audioLevel == null also when it is 0
     * Returns the audio level for a participant.
     *
     * @param observer
     * @param participant
     * @return
     */
    public static Double getPeerAudioLevel(WebDriver observer,
                                           WebDriver participant)
    {

        String jid = MeetUtils.getResourceJid(participant);

        String script = "" +
            "var level = APP.conference." +
            "getPeerSSRCAudioLevel(\"" + jid + "\");" +
            "return level ? level.toFixed(2) : null;";

        Object levelObj
            = ((JavascriptExecutor) observer).executeScript(script);

        return levelObj != null ?
            Double.valueOf(String.valueOf(levelObj)) : null;
    }
}
