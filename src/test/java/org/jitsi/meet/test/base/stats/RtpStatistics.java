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
package org.jitsi.meet.test.base.stats;

import org.jitsi.meet.test.base.*;

/**
 * It's the interface describing {@link Participant}'s RTP statistics.
 */
public interface RtpStatistics
{
    /**
     * The download (receive) RTP bitrate.
     *
     * @return a non negative value expressed in bits per second.
     */
    long getDownloadBitrate();

    /**
     * The upload (send) RTP bitrate.
     *
     * @return a non negative value expressed in bits per second.
     */
    long getUploadBitrate();

    /**
     * @return the transport part of the {@link Participant}'s RTP statistics.
     */
    RtpTransport getRtpTransport();

    /**
     * It's the interface describing the transport part of {@link Participant}'s RTP statistics.
     */
    interface RtpTransport
    {
        /**
         * @return the socket of the remote endpoint.
         */
        String getRemoteSocket();

        /**
         * @return true if the transport is peer-to-peer, false otherwise.
         */
        boolean isP2P();
    }
}
