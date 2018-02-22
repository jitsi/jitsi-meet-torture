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

import java.util.*;

import org.jitsi.meet.test.util.*;
import org.jitsi.meet.test.pageobjects.web.*;
import org.jitsi.meet.test.web.*;
import org.openqa.selenium.*;
import org.testng.*;
import org.testng.annotations.*;

import static org.testng.Assert.*;

/**
 * Test for conference invite information.
 *
 * @author Leonard Kim
 */
public class InviteTest extends WebTestBase
{
    /**
     * The single participant which will be used in this test.
     */
    private WebParticipant participant;

    @Override
    public void setupClass()
    {
        super.setupClass();

        ensureOneParticipant();
        participant = getParticipant1();
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
    public void testViewMoreNumbers()
    {
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
