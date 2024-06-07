package org.jitsi.meet.test;

import org.jitsi.meet.test.util.*;
import org.jitsi.meet.test.web.*;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.*;
import org.testng.*;
import org.testng.annotations.*;

import java.util.logging.*;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

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
            checkSubject(participant1);
            checkSubject(getParticipant2());
        }
        else
        {
            Logger.getGlobal().log(Level.WARNING, "Not testing subject as torture is not moderator.");
            throw new SkipException("skip as test's participant cannot be moderator");
        }
    }

    private void checkSubject(WebParticipant participant)
    {
        WebDriver driver = participant.getDriver();

        WebElement localTile = driver.findElement(By.xpath(SUBJECT_XPATH));
        Actions hoverOnLocalTile = new Actions(driver);
        hoverOnLocalTile.moveToElement(localTile);
        hoverOnLocalTile.perform();

        String txt = driver.findElement(By.xpath(SUBJECT_XPATH)).getText();

        assertTrue(txt.startsWith(MY_TEST_SUBJECT), "Subject does not match for " + participant.getName());
    }
}
