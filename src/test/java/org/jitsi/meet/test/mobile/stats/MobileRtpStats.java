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
package org.jitsi.meet.test.mobile.stats;

import org.jitsi.meet.test.base.stats.*;
import org.jitsi.meet.test.pageobjects.base.*;
import org.json.*;

import java.util.*;

/**
 * Mobile {@link RtpStatistics}. Makes use of the {@link TestHint} which
 * contains the stats information encoded in the JSON format.
 */
public class MobileRtpStats implements RtpStatistics
{
    /**
     * The {@link TestHint} which contains the RTP stats encoded in the
     * following JSON format:
     * {
     *     bitrate: {
     *         download: 0,
     *         upload: 0
     *     }
     * }
     */
    private final TestHint rtpStats;

    /**
     * Initializes new {@link MobileRtpStats}.
     *
     * @param rtpStats - A {@link TestHint} which contains the RTP stats encoded
     * in JSON format.
     */
    public MobileRtpStats(TestHint rtpStats)
    {
        this.rtpStats = Objects.requireNonNull(rtpStats, "rtpStats");
    }

    private JSONObject getBitrate()
    {
        return getStatsJSON().getJSONObject("bitrate");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getDownloadBitrate()
    {
        return getBitrate().getLong("download");
    }

    /**
     * Gets the root {@link JSONObject} of the RTP stats.
     *
     * @return a {@link JSONObject}.
     */
    private JSONObject getStatsJSON()
    {
        return new JSONObject(
                Objects.requireNonNull(
                        rtpStats.getValue(), "rtpStats value"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getUploadBitrate()
    {
        return getBitrate().getLong("upload");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RtpTransport getRtpTransport()
    {
        throw new RuntimeException("Not implemented.");
    }
}
