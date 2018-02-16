package org.jitsi.meet.test;

import org.jitsi.meet.test.util.*;
import org.jitsi.meet.test.web.*;

import org.openqa.selenium.*;
import org.testng.annotations.*;

import java.util.*;

/**
 * Tests features of filmstrip only mode.
 *
 * @author Leonard Kim
 */
public class FilmstripOnlyTest
    extends WebTestBase
{
    private final String enableFilmstripOnlyMode
        = "interfaceConfig.filmStripOnly=true";

    @Override
    public void setupClass()
    {
        super.setupClass();

        ensureTwoParticipants(
            getJitsiMeetUrl().appendConfig(enableFilmstripOnlyMode),
            null);
    }

    /**
     * Checks if filmstrip only mode can load.
     */
    @Test
    public void testLoadsOnlyTheFilmstrip() {
        WebDriver owner =  getParticipant1().getDriver();
        WebDriver remote = getParticipant2().getDriver();
        String remoteResourceJid = MeetUtils.getResourceJid(remote);

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
    @Test(dependsOnMethods = { "testLoadsOnlyTheFilmstrip" })
    public void testDisplaysDeviceSelection() {
        WebDriver owner =  getParticipant1().getDriver();

        MeetUIUtils.clickOnToolbarButton(
            owner,
            "toolbar_button_fodeviceselection");

        // give some time for the window to open and load
        TestUtils.waitMillis(2000);

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
}
