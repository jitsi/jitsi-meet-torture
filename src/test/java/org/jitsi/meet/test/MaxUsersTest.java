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


import org.jitsi.meet.test.base.*;
import org.jitsi.meet.test.util.*;
import org.jitsi.meet.test.web.*;

import org.openqa.selenium.*;
import org.testng.annotations.*;

import java.util.*;

import static org.testng.Assert.*;

/**
 * Test for max users prosody module.
 * 
 * NOTE: The tests assumes that the module is deployed and configured.
 *
 * @author Hristo Terezov
 */
public class MaxUsersTest
    extends WebTestBase
{
    /**
     * Number of participants in the call.
     */
    public static int MAX_USERS = 3;
    
    /**
     * The property to change MAX_USERS variable.
     */
    public static String MAX_USERS_PROP = "max_users_tests.max_users";

    @Override
    public boolean skipTestByDefault()
    {
        return true;
    }

    @Override
    public void setupClass()
    {
        super.setupClass();

        ensureTwoParticipants();
    }

    /**
     * Scenario tests whether an error dialog is displayed when MAX_USERSth
     * participant join the conference.
     */
    @Test
    public void enterWithMaxParticipantsAndCheckDialog() 
    {
        String maxUsersString = System.getProperty(MAX_USERS_PROP);
        if (maxUsersString != null)
        {
            MAX_USERS = Integer.parseInt(maxUsersString);
        }
            
        if (MAX_USERS > 2)
        {
            boolean failed = false;
            // FIXME simplify index logic by removing setupClass
            // Assuming we have 2 participants already started we have to
            // start MAX_USERS - 2 participants more to have MAX_USERS
            // participants in the call in order to exceed the limit.
            Participant[] participants = new Participant[MAX_USERS - 2];
            try
            {
                for (int i = 0; i < participants.length; i++)
                {
                    participants[i] =
                        this.participants
                            .createParticipant("web.participant" + (i + 4));
                    participants[i].joinConference(currentRoomName);
                }
                // Check if the error dialog is displayed for
                // the last participant.
                int lastParticipantIdx = participants.length - 1;
                checkDialog(participants[lastParticipantIdx].getDriver());
            } 
            catch(TimeoutException timeout)
            {
                // There was no dialog, so we fail the test !
                failed = true;
            }
            finally
            {
                // Clean up the participants in participants array
                Arrays.stream(participants).forEach(Participant::close);
            }

            if (failed)
            {
                fail("There was no error dialog.");
            }
        }
        else
        {
            checkDialog(getParticipant2().getDriver());
        }
    }

    /**
     * Check if the error dialog is displayed for participant.
     * @param driver the participant
     */
    private void checkDialog(WebDriver driver)
    {
        TestUtils.waitForElementByXPath(driver,
            "//span[@data-i18n='dialog.maxUsersLimitReached']", 5);
    }

}
