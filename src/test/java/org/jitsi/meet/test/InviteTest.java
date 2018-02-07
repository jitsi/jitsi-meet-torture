package org.jitsi.meet.test;

import org.jitsi.meet.test.base.AbstractBaseTest;
import org.jitsi.meet.test.base.Participant;
import org.jitsi.meet.test.util.MeetUtils;
import org.jitsi.meet.test.web.DialInNumbersPage;
import org.jitsi.meet.test.web.InfoDialog;
import org.jitsi.meet.test.web.WebParticipant;
import org.openqa.selenium.WebDriver;
import org.testng.SkipException;
import org.testng.annotations.Test;

import java.util.ArrayList;

import static org.testng.Assert.assertTrue;

/**
 * Test for conference invite information.
 *
 * @author Leonard Kim
 */
public class InviteTest extends AbstractBaseTest
{
    /**
     * The single participant which will be used in this test.
     */
    private WebParticipant participant;

    /**
     * Whether the test is enabled. The test will be dynamically disabled,
     * unless dial in is detected as enabled for the jitsi-meet instance (by
     * inspecting window.config values).
     */
    private static boolean enabled = true;


    @Override
    public void setup()
    {
        super.setup();

        ensureOneParticipant();

        Participant p = getParticipant1();
        if (!(p instanceof WebParticipant))
        {
            throw new IllegalStateException(
                "This test only supports web and shouldn't be run on other "
                    + "platforms.");
        }

        participant = (WebParticipant) p;
    }


    /**
     * Checks if the info dialog shows the current conference URL.
     */
    @Test
    public void testInviteURLDisplays()
    {
        InfoDialog infoDialog = participant.getInfoDialog();
        infoDialog.open();

        String currentUrl = participant.getDriver().getCurrentUrl();
        String displayedUrl = infoDialog.getMeetingURL();

        assertTrue(currentUrl.contains(displayedUrl));
    }

    /**
     * Checks if the info dialog shows a dial in number and pin (conference ID).
     */
    @Test(dependsOnMethods = { "testInviteURLDisplays" })
    public void testDialInDisplays() {
        if (!MeetUtils.isDialInEnabled(participant.getDriver()))
        {
            InviteTest.enabled = false;

            throw new SkipException(
                "No dial in configuration detected. Disabling test.");
        }
        InfoDialog infoDialog = participant.getInfoDialog();
        infoDialog.open();

        String displayedNumber = infoDialog.getDialInNumber();
        assertTrue(displayedNumber.length() > 0);

        String displayedPin = infoDialog.getPinNumber();
        assertTrue(displayedPin.length() > 1);
    }

    /**
     * Checks if clicking to view more numbers shows the appropriate static
     * page.
     */
    @Test(dependsOnMethods = { "testDialInDisplays" })
    public void testViewMoreNumbers() {
        InfoDialog infoDialog = participant.getInfoDialog();
        infoDialog.open();

        String displayedNumber = infoDialog.getDialInNumber();
        String displayedPin = infoDialog.getPinNumber();

        infoDialog.openDialInNumbersPage();

        WebDriver driver = participant.getDriver();

        ArrayList<String> tabs = new ArrayList<>(driver.getWindowHandles());
        driver.switchTo().window(tabs.get(1));

        DialInNumbersPage dialInNumbersPage
            = participant.getDialInNumbersPage();
        dialInNumbersPage.waitForLoad();

        String conferenceIDMessage = dialInNumbersPage.getConferenceIdMessage();

        assert(conferenceIDMessage.contains(displayedPin));
        assert(dialInNumbersPage.includesNumber(displayedNumber));

        driver.close();

        driver.switchTo().window(tabs.get(0));
    }
}
