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
package org.jitsi.meet.test.mobile.base;

import org.jitsi.meet.test.base.*;
import org.jitsi.meet.test.mobile.*;

/**
 * Base class of mobile test cases.
 *
 * @author Damian Minkov
 * @author Pawel Domas
 */
public class MobileTestBase extends AbstractBaseTest
{
    /**
     * Name of the config property which can be used to override
     * {@link #PARTICIPANT_1_DEFAULT_PREFIX}.
     */
    private static final String PARTICIPANT_1_PREFIX_CFG_PROP
        = "mobile.participant1Prefix";

    /**
     * Default config prefix for 1st mobile participant.
     */
    private static final String PARTICIPANT_1_DEFAULT_PREFIX
        = "mobile.participant1";

    /**
     * @return the config prefix for 1st mobile participant.
     */
    private static String getParticipant1Prefix()
    {
        String defaultParticipant
            = System.getProperty(
                    PARTICIPANT_1_PREFIX_CFG_PROP,
                    PARTICIPANT_1_DEFAULT_PREFIX);

        System.err.println("Default participant: " + defaultParticipant);

        return defaultParticipant;
    }

    /**
     * Creates the 1st mobile participant for
     * {@link #PARTICIPANT_1_DEFAULT_PREFIX} config prefix.
     * The {@link #PARTICIPANT_1_PREFIX_CFG_PROP} can be used to override it.
     *
     * @return {@link MobileParticipant}
     */
    protected MobileParticipant createParticipant1()
    {
        return
            (MobileParticipant) participants
                .createParticipant(getParticipant1Prefix());
    }
}
