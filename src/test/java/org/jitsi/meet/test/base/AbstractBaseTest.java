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
package org.jitsi.meet.test.base;

import org.openqa.selenium.*;
import org.testng.*;
import org.testng.annotations.*;

import java.lang.reflect.*;
import java.util.*;
import java.util.stream.*;

import static org.jitsi.meet.test.util.TestUtils.*;

/**
 * The base of all tests that require participant management.
 *
 * @author Damian Minkov
 * @author George Politis
 */
public abstract class AbstractBaseTest
    extends AbstractTest
{
    /**
     * The current room name used.
     */
    protected final String currentRoomName;

    /**
     * The participants pool created/used by this test instance.
     */
    protected ParticipantHelper participants;

    /**
     * Default.
     */
    protected AbstractBaseTest()
    {
        currentRoomName
            = "torture" + String.valueOf((int)(Math.random()*1000000));
        participants = null;
    }

    /**
     * Constructs new AbstractBaseTest with predefined baseTest, to
     * get its participants and room name.
     *
     * @param baseTest the parent test.
     */
    protected AbstractBaseTest(AbstractBaseTest baseTest)
    {
        currentRoomName = baseTest.currentRoomName;
        participants = new ParticipantHelper(baseTest.participants);
    }

    /**
     * Create {@link ParticipantFactory} which will be used by the test to
     * create new participants.
     *
     * @param config the config which will be the source of all properties
     * required to create {@link Participant}s.
     *
     * @return a new factory initialized with the given config.
     */
    protected abstract ParticipantFactory createParticipantFactory(
            Properties config);

    /**
     * Method is called on "before class". {@link AbstractTest} will figure out
     * if the test should be skipped in which case this method will not be
     * called.
     *
     * @param config The configuration to run this test with.
     */
    @Override
    public void setupClass(Properties config)
    {
        super.setupClass(config);

        ParticipantFactory factory = createParticipantFactory(config);
        participants = new ParticipantHelper(factory);

        setupClass();
    }

    /**
     * Method is called on "before class". {@link AbstractTest} will figure out
     * if the test should be skipped in which case this method will not be
     * called.
     */
    public void setupClass()
    {
        // Currently does nothing.
    }

    /**
     * Returns all {@link Participant}s held by the underlying
     * {@link ParticipantHelper}.
     */
    public List<Participant<? extends WebDriver>>  getAllParticipants()
    {
        return participants.getAll();
    }

    /**
     * Return new {@link JitsiMeetUrl} instance which has only
     * {@link JitsiMeetUrl#serverUrl} field initialized with the value from
     * {@link ParticipantFactory#JITSI_MEET_URL_PROP} system property.
     *
     * @return a new instance of {@link JitsiMeetUrl}.
     */
    public JitsiMeetUrl getJitsiMeetUrl()
    {
        return participants.getJitsiMeetUrl().setRoomName(currentRoomName);
    }

    /**
     * Method called "AfterClass". Will clean up any dangling
     * {@link Participant}s held by the {@link ParticipantHelper}.
     */
    @AfterClass
    public void cleanupClass()
    {
        this.participants.cleanup();
    }
}
