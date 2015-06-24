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
package org.jitsi.meet.test;

import junit.framework.*;

/**
 * A test which always needs to be the last to clear all resources (browsers)
 * that were open.
 *
 * @author Damian Minkov
 * @author Pawel Domas
 */
public class DisposeConference
    extends TestCase
{
    public DisposeConference()
    {

    }

    public DisposeConference(String testName)
    {
        super(testName);
    }

    /**
     * Disposes the secondParticipant and the owner.
     */
    public void testDispose()
    {
        ConferenceFixture.quit(ConferenceFixture.getSecondParticipant());
        ConferenceFixture.quit(ConferenceFixture.getOwner());
        disposeThirdParticipant();
    }

    /**
     * Disposes the third participant.
     */
    public void disposeThirdParticipant()
    {
        ConferenceFixture.quit(ConferenceFixture.getThirdParticipant());
    }
}
