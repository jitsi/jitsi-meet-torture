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
package org.jitsi.meet.test.pageobjects.mobile.stats;

import org.jitsi.meet.test.base.stats.*;
import org.jitsi.meet.test.mobile.*;
import org.jitsi.meet.test.mobile.stats.*;
import org.jitsi.meet.test.pageobjects.base.*;
import org.jitsi.meet.test.pageobjects.mobile.base.*;

/**
 * The class is a direct mapping to the corresponding component in jitsi-meet
 * located at /features/testing/components/TestConnectionInfo. It should contain
 * all the things exposed there and provide Java interface for the tests to
 * operate on them.
 */
public class TestConnectionInfo
    extends AbstractMobilePage
{
    /**
     * The id of the {@link TestHint} which holds the conference connection
     * state like established, interrupted, restored, etc. Under the hood that
     * state is bound to the ICE connection state of the PeerConnection
     * currently being used by the conference.
     */
    private static final String ID_CONFERENCE_CONNECTION_STATE
        = "org.jitsi.meet.conference.connectionState";

    /**
     * The id of the {@link TestHint} which tells whether or not the conference
     * room (XMPP MUC room) has been joined. It doesn't mean that the conference
     * has been started yet.
     */
    private static final String ID_CONFERENCE_JOINED_STATE
        = "org.jitsi.meet.conference.joinedState";

    /**
     * The id of the {@link TestHint} which stores the current RTP statistics.
     * See {@link RtpStatistics} for more info.
     */
    private static final String ID_RTP_STATISTICS = "org.jitsi.meet.stats.rtp";

    /**
     * The conference connection state.
     * See {@link #ID_CONFERENCE_CONNECTION_STATE} for more info.
     */
    @TestHintLocator(id = ID_CONFERENCE_CONNECTION_STATE)
    private TestHint conferenceConnectionState;

    /**
     * The conference joined state (is MUC joined?).
     * See {@link #ID_CONFERENCE_JOINED_STATE}.
     */
    @TestHintLocator(id = ID_CONFERENCE_JOINED_STATE)
    private TestHint conferenceJoinedState;

    /**
     * The RTP stats test hint which provides the data for
     * the {@link MobileRtpStats}.
     */
    @TestHintLocator(id = ID_RTP_STATISTICS)
    private TestHint rtpStatistics;

    /**
     * Initializes with the given mobile driver instance.
     *
     * @param participant <tt>MobileParticipant</tt> must not be <tt>null</tt>.
     */
    public TestConnectionInfo(MobileParticipant participant)
    {
        super(participant);
    }

    /**
     * @return {@link MobileRtpStats}
     */
    public RtpStatistics getRtpStats()
    {
        return new MobileRtpStats(rtpStatistics);
    }

    /**
     * @return {@code true} if the MUC has been joined already or {@code false}
     * otherwise.
     */
    public boolean isConferenceJoined()
    {
        return Boolean.parseBoolean(conferenceJoinedState.getValue());
    }

    /**
     * Is ICE currently in the connected state ?
     *
     * @return {@code true} for the ICe connected state, {@code false} for any
     * other state.
     */
    public boolean isIceConnected()
    {
        String text = conferenceConnectionState.getValue();

        // XXX Define constants if you want to use those strings anywhere else
        return
            "conference.connectionRestored".equals(text)
                || "conference.connectionEstablished".equals(text);
    }
}
