/*
 * Copyright @ 2015-2018 Atlassian Pty Ltd
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
    private final static String ENABLE_FILMSTRIP_ONLY_MODE
        = "interfaceConfig.filmStripOnly=true";

    @Override
    public void setupClass()
    {
        super.setupClass();

        ensureTwoParticipants(
            getJitsiMeetUrl().appendConfig(ENABLE_FILMSTRIP_ONLY_MODE),
            null);
    }

    /**
     * Checks if filmstrip only mode can load.
     */
    @Test
    public void testLoadsOnlyTheFilmstrip()
    {
        WebDriver driver1 = getParticipant1().getDriver();
        String participant2EndpointId = getParticipant2().getEndpointId();

        MeetUIUtils.assertLocalThumbnailShowsVideo(driver1);

        // Remove video should display in a thumbnail.
        TestUtils.waitForDisplayedElementByXPath(
            driver1,
            "//span[@id='participant_" + participant2EndpointId + "']",
            5);

        // The toolbox should display.
        TestUtils.waitForDisplayedElementByXPath(
            driver1,
            "//div[contains(@class, 'toolbox')]",
            5
        );
    }

    /**
     * Checks if device selection popup can load.
     */
    @Test(dependsOnMethods = { "testLoadsOnlyTheFilmstrip" })
    public void testDisplaysDeviceSelection()
    {
        WebDriver driver1 = getParticipant1().getDriver();

        MeetUIUtils.clickOnToolbarButton(
            driver1,
            "toolbar_button_fodeviceselection");

        // give some time for the window to open and load
        TestUtils.waitMillis(2000);

        Set<String> windowHandles = driver1.getWindowHandles();
        Iterator<String> handleIterator = windowHandles.iterator();
        String mainWindowHandle = handleIterator.next();
        String deviceSelectionHandle = handleIterator.next();

        driver1.switchTo().window(deviceSelectionHandle);

        // Ensure the device selection modal content is displayed.
        TestUtils.waitForDisplayedElementByXPath(
            driver1,
            "//div[contains(@class, 'device-selection')]",
            5);

        driver1.close();
        driver1.switchTo().window(mainWindowHandle);
    }
}
