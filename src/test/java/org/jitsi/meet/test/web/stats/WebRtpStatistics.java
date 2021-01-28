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
import org.openqa.selenium.*;

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
     * The transport part of the RTP statistics state retrieved from the app.
     *
     * Note that the transport object that is returned from the app is a list.
     * For simplicity we keep only the first element.
     */
    private final RtpTransport rtpTransport;

    /**
     * Creates new {@link WebRtpStatistics}.
     *
     * @param javascriptExecutor the {@link JavascriptExecutor} for whom the stats will be created.
     */
    public WebRtpStatistics(JavascriptExecutor javascriptExecutor)
    {
        Map stats
            = (Map) javascriptExecutor.executeScript(
                    "return APP.conference.getStats();");
        this.bitrateMap
            = (Map<String, Long>) stats.get("bitrate");

        List transport = (List) stats.get("transport");
        this.rtpTransport = new WebRtpTransport(
            transport != null && !transport.isEmpty() ? (Map) transport.get(0) : null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getDownloadBitrate()
    {
        return bitrateMap != null ? bitrateMap.get("download") : 0L;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getUploadBitrate()
    {
        return bitrateMap != null ? bitrateMap.get("upload") : 0L;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RtpTransport getRtpTransport()
    {
        return rtpTransport;
    }

    static class WebRtpTransport implements RtpTransport
    {
        /**
         * The transport part of the RTP statistics state retrieved from the app.
         *
         * Note that the transport object that is returned from the app is a list.
         * For simplicity we keep only the first element.
         */
        private final Map transportMap;

        /**
         * Ctor.
         *
         * @param transportMap the transport part of the RTP statistics state retrieved from the app.
         */
        WebRtpTransport(Map transportMap)
        {
            this.transportMap = transportMap;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getRemoteSocket()
        {
            return transportMap != null ? (String) transportMap.get("ip") : "";
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isP2P()
        {
            return transportMap != null && (boolean) transportMap.get("p2p");
        }
    }
}
