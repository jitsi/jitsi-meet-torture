package org.jitsi.meet.test;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.jitsi.meet.test.util.MeetUIUtils;
import org.jitsi.meet.test.util.MeetUtils;
import org.jitsi.meet.test.util.TestUtils;
import org.openqa.selenium.WebDriver;

import java.util.Iterator;
import java.util.Set;

/**
 * Tests features of filmstrip only mode.
 *
 * @author Leonard Kim
 */
public class FilmstripOnlyTest extends TestCase {
    private final String enableFilmstripOnlyMode
        = "interfaceConfig.filmStripOnly=true";

    /**
     * Constructs test
     * @param name the method name for the test.
     */
    public FilmstripOnlyTest(String name)
    {
        super(name);
    }

    /**
     * Orders the tests.
     * @return the suite with order tests.
     */
    public static junit.framework.Test suite()
    {
        TestSuite suite = new TestSuite();

        suite.addTest(
            new FilmstripOnlyTest("testLoadsOnlyTheFilmstrip"));
        suite.addTest(
            new FilmstripOnlyTest("testDisplaysDeviceSelection"));
        suite.addTest(
            new FilmstripOnlyTest("testCleanup"));
        return suite;
    }

    /**
     * Checks if filmstrip only mode can load.
     */
    public void testLoadsOnlyTheFilmstrip() {
        WebDriver owner =  ConferenceFixture.getOwner();
        WebDriver remote = ConferenceFixture.getSecondParticipant();
        String remoteResourceJid = MeetUtils.getResourceJid(remote);

        ConferenceFixture.close(owner);
        ConferenceFixture.startOwner(enableFilmstripOnlyMode);

        MeetUIUtils.assertLocalThumbnailShowsVideo(owner);

        // Remove video should display in a thumbnail.
        TestUtils.waitForDisplayedElementByXPath(
            owner,
            "//span[@id='participant_" + remoteResourceJid + "']",
            5);

        // The toolbox should display.
        TestUtils.waitForDisplayedElementByXPath(
            owner,
            "//div[contains(@class, 'toolbox')]",
            5
        );
    }

    /**
     * Checks if device selection popup can load.
     */
    public void testDisplaysDeviceSelection() {
        WebDriver owner =  ConferenceFixture.getOwner();

        MeetUIUtils.clickOnToolbarButton(
            owner,
            "toolbar_button_fodeviceselection");

        Set<String> windowHandles = owner.getWindowHandles();
        Iterator<String> handleIterator = windowHandles.iterator();
        String mainWindowHandle = handleIterator.next();
        String deviceSelectionHandle = handleIterator.next();

        owner.switchTo().window(deviceSelectionHandle);

        // Ensure the device selection modal content is displayed.
        TestUtils.waitForDisplayedElementByXPath(
            owner,
            "//div[contains(@class, 'device-selection')]",
            5);

        owner.close();
        owner.switchTo().window(mainWindowHandle);
    }

    /**
     * Resets the meeting states of the participants. Necessary as each test
     * does not necessarily ensure a clean state on start and being in
     * filmstrip only mode could affect tests.
     */
    public void testCleanup() {
        ConferenceFixture.restartParticipants();
    }
}
