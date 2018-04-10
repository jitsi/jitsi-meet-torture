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

import java.util.*;

/**
 * Base class of mobile test cases.
 *
 * @author Damian Minkov
 * @author Pawel Domas
 */
public class MobileTestBase
    extends TypedBaseTest<MobileParticipant, MobileParticipantFactory>
{
    /**
     * This prefix is used to configure properties consumed by
     * {@link MobileTestBase} and it's subclasses.
     */
    private static final String _MOBILE_TEST_PREFIX = "mobileTest.";

    /**
     * Default config prefix for 1st mobile participant.
     */
    private static final String DEFAULT_PARTICIPANT_1_PREFIX
        = _MOBILE_TEST_PREFIX + "participant1";

    /**
     * Name of the config property which can be used to override
     * {@link #DEFAULT_PARTICIPANT_1_PREFIX}.
     */
    private static final String PROP_PARTICIPANT_1_PREFIX
        = _MOBILE_TEST_PREFIX + "participant1Prefix";

    /**
     * @return the config prefix for 1st mobile participant.
     */
    private static String getParticipant1Prefix()
    {
        String defaultParticipant
            = System.getProperty(
                    PROP_PARTICIPANT_1_PREFIX,
                    DEFAULT_PARTICIPANT_1_PREFIX);

        System.err.println("Default participant: " + defaultParticipant);

        return defaultParticipant;
    }

    /**
     * Creates new {@link MobileTestBase}.
     */
    public MobileTestBase()
    {
        super(MobileParticipantFactory.class);
    }

    /**
     * Creates the 1st mobile participant for
     * {@link #DEFAULT_PARTICIPANT_1_PREFIX} config prefix.
     * The {@link #PROP_PARTICIPANT_1_PREFIX} can be used to override it.
     *
     * @return {@link MobileParticipant}
     */
    protected MobileParticipant createParticipant1()
    {
        return participants.createParticipant(getParticipant1Prefix());
    }
}
