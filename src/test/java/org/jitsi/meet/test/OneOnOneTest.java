/*
 * Copyright @ 2017 Atlassian Pty Ltd
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
import org.jitsi.meet.test.util.*;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedCondition;

/**
 * Tests 1-on-1 remote video thumbnail display in the filmstrip.
 *
 * @author Leonard Kim
 */
public class OneOnOneTest
    extends TestCase
{
    private final static String filmstripRemoteVideosXpath
        = "//div[@id='filmstripRemoteVideosContainer']";
    private final int filmstripVisibilityWait = 5;

    /**
     * Constructs test
     * @param name the method name for the test.
     */
    public OneOnOneTest(String name)
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

        suite.addTest(new OneOnOneTest("testFilmstripHiddenInOneOnOne"));
        suite.addTest(new OneOnOneTest("testFilmstripVisibleWithMoreThanTwo"));
        suite.addTest(
            new OneOnOneTest("testFilmstripDisplayWhenReturningToOneOnOne"));
        suite.addTest(new OneOnOneTest("testFilmstripVisibleOnSelfViewFocus"));
        suite.addTest(new OneOnOneTest("testShareVideoShowsRemoteVideos"));
        suite.addTest(new OneOnOneTest("testStopOneOnOneTest"));

        return suite;
    }

    /**
     * Tests remote videos in filmstrip do not display in a 1-on-1 call.
     */
    public void testFilmstripHiddenInOneOnOne()
    {
        // Close the browsers first and then load the meeting so hash changes to
        // the config are detected by the browser.
        ConferenceFixture.close(ConferenceFixture.getOwner());
        ConferenceFixture.close(ConferenceFixture.getSecondParticipant());

        ConferenceFixture.startOwner("config.disable1On1Mode=false");
        ConferenceFixture.startSecondParticipant(
            "config.disable1On1Mode=false");

        verifyRemoteVideosDisplay(ConferenceFixture.getOwner(), false);
        verifyRemoteVideosDisplay(
            ConferenceFixture.getSecondParticipant(), false);
    }

    /**
     * Tests remote videos in filmstrip do display when in a call with more than
     * two total participants.
     */
    public void testFilmstripVisibleWithMoreThanTwo() {
        // Close the third participant's browser and reopen so hash changes to
        // the config are detected by the browser.
        ConferenceFixture.waitForThirdParticipantToConnect();
        ConferenceFixture.close(ConferenceFixture.getThirdParticipant());
        ConferenceFixture.startThirdParticipant("config.disable1On1Mode=false");

        verifyRemoteVideosDisplay(ConferenceFixture.getOwner(), true);
        verifyRemoteVideosDisplay(
            ConferenceFixture.getSecondParticipant(), true);
        verifyRemoteVideosDisplay(
            ConferenceFixture.getThirdParticipant(), true);
    }

    /**
     * Tests remote videos in filmstrip do not display after transitioning to
     * 1-on-1 mode. Also tests remote videos in filmstrip do display when
     * focused on self and transitioning back to 1-on-1 mode.
     */
    public void testFilmstripDisplayWhenReturningToOneOnOne() {
        MeetUIUtils.clickOnLocalVideo(ConferenceFixture.getSecondParticipant());

        ConferenceFixture.closeThirdParticipant();

        verifyRemoteVideosDisplay(ConferenceFixture.getOwner(), false);
        verifyRemoteVideosDisplay(
            ConferenceFixture.getSecondParticipant(), true);
    }

    /**
     * Tests remote videos in filmstrip become visible when focused on self view
     * while in a 1-on-1 call.
     */
    public void testFilmstripVisibleOnSelfViewFocus() {
        MeetUIUtils.clickOnLocalVideo(ConferenceFixture.getOwner());
        verifyRemoteVideosDisplay(ConferenceFixture.getOwner(), true);

        MeetUIUtils.clickOnLocalVideo(ConferenceFixture.getOwner());
        verifyRemoteVideosDisplay(ConferenceFixture.getOwner(), false);
    }

    /**
     * Tests remote videos in filmstrip become visible when sharing video, even
     * when in a lonely call.
     */
    public void testShareVideoShowsRemoteVideos() {
        SharedVideoTest sharedVideoTest = new SharedVideoTest("startSharingVideo");

        sharedVideoTest.startSharingVideo();

        ConferenceFixture.closeSecondParticipant();

        WebDriver owner = ConferenceFixture.getOwner();
        TestUtils.waitForCondition(owner, 5,
            new ExpectedCondition<Boolean>()
            {
                public Boolean apply(WebDriver d)
                {
                    return MeetUIUtils.getRemoteVideos(d).size() == 0;
                }
            });

        verifyRemoteVideosDisplay(ConferenceFixture.getOwner(), true);
    }

    /**
     * Ensures participants reopen their browsers without 1-on-1 mode enabled.
     */
    public void testStopOneOnOneTest() {
        ConferenceFixture.restartParticipants();
    }

    /**
     * Check if remote videos in filmstrip are visible.
     *
     * @param testee the <tt>WebDriver</tt> of the participant for whom we're
     *               checking the status of filmstrip remote video visibility.
     * @param isDisplayed whether or not filmstrip remote videos should be
     *                    visible
     */
    private void verifyRemoteVideosDisplay(
        WebDriver testee, boolean isDisplayed)
    {
        TestUtils.waitForDisplayedOrNotByXPath(
            testee,
            filmstripRemoteVideosXpath,
            filmstripVisibilityWait,
            isDisplayed);
    }
}
