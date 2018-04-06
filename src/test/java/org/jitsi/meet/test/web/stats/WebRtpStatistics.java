/*
 * Copyright @ Atlassian Pty Ltd
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
package org.jitsi.meet.test.web.stats;

import org.jitsi.meet.test.base.stats.*;
import org.jitsi.meet.test.web.*;

import java.util.*;

/**
 * The web version of {@link RtpStatistics}.
 */
public class WebRtpStatistics
    implements RtpStatistics
{
    /**
     * The bitrate part of the RTP statistics state retrieved from the app.
     */
    private final Map<String, Long> bitrateMap;

    /**
     * Creates new {@link WebRtpStatistics}.
     *
     * @param webParticipant the participant for whom the stats will be created.
     */
    public WebRtpStatistics(WebParticipant webParticipant)
    {
        Map stats
            = (Map) webParticipant.executeScript(
                    "return APP.conference.getStats();");
        this.bitrateMap
            = (Map<String, Long>) stats.get("bitrate");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getDownloadBitrate()
    {
        return bitrateMap != null ? bitrateMap.get("upload") : 0L;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getUploadBitrate()
    {
        return bitrateMap != null ? bitrateMap.get("download") : 0L;
    }
}
