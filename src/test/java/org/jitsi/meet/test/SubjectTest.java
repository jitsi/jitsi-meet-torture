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

import org.jitsi.meet.test.util.*;
import org.jitsi.meet.test.web.*;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.*;
import org.openqa.selenium.support.ui.*;
import org.testng.*;
import org.testng.annotations.*;

import java.util.logging.*;

import static org.testng.Assert.*;

/**
 * TSets subject via config url hash param and check locally and remote for it.
 *
 * @author Damian Minkov
 */
public class SubjectTest
    extends WebTestBase
{
    private static final String MY_TEST_SUBJECT = "My Test Subject";

    /**
     * The subject xpath.
     */
    private final static String SUBJECT_XPATH = "//div[starts-with(@class, 'subject-text')]";

    @Override
    public void setupClass()
    {
        super.setupClass();

        ensureTwoParticipants(getJitsiMeetUrl().appendConfig("config.subject=\"" + MY_TEST_SUBJECT + "\""), null);
    }

    /**
     * Kick participant2 and checks at participant1 is this is visible.
     * and whether participant2 sees a notification that was kicked.
     */
    @Test
    public void changeSubjectAndCheck()
    {
        WebParticipant participant1 = getParticipant1();

        if (participant1.isModerator())
        {
            checkSubject(participant1, MY_TEST_SUBJECT);
            checkSubject(getParticipant2(), MY_TEST_SUBJECT);
        }
        else
        {
            Logger.getGlobal().log(Level.WARNING, "Not testing subject as torture is not moderator.");
            throw new SkipException("skip as test's participant cannot be moderator");
        }
    }

    public static void checkSubject(WebParticipant participant, String subject)
    {
        WebDriver driver = participant.getDriver();

        WebElement localTile = driver.findElement(By.xpath(SUBJECT_XPATH));
        Actions hoverOnLocalTile = new Actions(driver);
        hoverOnLocalTile.moveToElement(localTile);
        hoverOnLocalTile.perform();

        String txt = driver.findElement(By.xpath(SUBJECT_XPATH)).getText();

        TestUtils.waitForCondition(driver, "Subject does not match for " + participant.getName(), 5,
           (ExpectedCondition<Boolean>) d ->
                   d.findElement(By.xpath(SUBJECT_XPATH)).getText().startsWith(subject));
    }
}
