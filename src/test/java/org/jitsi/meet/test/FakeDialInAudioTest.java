/*
 * Copyright @ 2018 8x8 Pty Ltd
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

import org.jitsi.meet.test.web.*;

/**
 * A test that overrides DialInAudioTest so we do not depend
 * on external service for running the test.
 * The goal of this test is to run regularly dial-in test with
 * jitsi-meet changes.
 */
public class FakeDialInAudioTest
    extends DialInAudioTest
{
    @Override
    public boolean skipTestByDefault()
    {
        return false;
    }

    @Override
    public void enterAndReadDialInPin()
    {
        System.setProperty(DIAL_IN_PARTICIPANT_REST_URL, "FAKE_URL");

        super.enterAndReadDialInPin();
    }

    /**
     * We always return that dial-in is enabled as we are faking normal
     * and dial-in participant.
     *
     * @param participant The participant to check.
     * @return true.
     */
    @Override
    protected boolean isDialInEnabled(WebParticipant participant)
    {
        return true;
    }

    /**
     * Returns a dummy pin.
     *
     * @param participant The participant which info dialog to use.
     * @return
     */
    @Override
    protected String retrievePin(WebParticipant participant)
    {
        return "2131271746";
    }

    @Override
    public void enterDialInParticipant()
    {
        try
        {
            // join in a room
            ensureTwoParticipants();
        }
        catch(Throwable t)
        {
            t.printStackTrace();
            return;
        }
    }
}
