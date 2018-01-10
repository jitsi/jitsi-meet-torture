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

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;
import org.testng.annotations.*;

import static org.testng.Assert.*;

/**
 * A test that tests ring overlay. Uses a dummy jwt token to activate ring
 * overlay tests for expected values, enters a second participant and checks
 * the values seen (display name and avatar).
 * @author Damian Minkov
 */
public class RingOverlayTest
    extends AbstractBaseTest
{
    /**
     * A test token to use. This is the encoded values:
     *
     * {
     *    "context": {
     *       "callee": {
     *           "email": "callee@jitsi.org",
     *           "name": "Callee DisplayName",
     *           "avatarUrl":
     *              "https://www.gravatar.com/avatar/c6560f2a9280b30cdac10adcdea9153d?d=wavatar"
     *       },
     *       "user": {
     *           "email": "example1@jitsi.org",
     *           "name": "User DisplayName",
     *           "avatarUrl":
     *              "https://www.gravatar.com/avatar/521bd6db706fb24bfef68e6cffe887e6?d=wavatar"
     *       }
     *   }
     *}
     */
    private final static String JWT_TOKEN = "?jwt=eyJhbGciOiJIUzI1NiIsInR5cCI6I"
        + "kpXVCJ9.eyJjb250ZXh0Ijp7ImNhbGxlZSI6eyJlbWFpbCI6ImNhbGxlZUBqaXRzaS5v"
        + "cmciLCJuYW1lIjoiQ2FsbGVlIERpc3BsYXlOYW1lIiwiYXZhdGFyVXJsIjoiaHR0cHM6"
        + "Ly93d3cuZ3JhdmF0YXIuY29tL2F2YXRhci9jNjU2MGYyYTkyODBiMzBjZGFjMTBhZGNk"
        + "ZWE5MTUzZD9kPXdhdmF0YXIifSwidXNlciI6eyJlbWFpbCI6ImV4YW1wbGUxQGppdHNp"
        + "Lm9yZyIsIm5hbWUiOiJVc2VyIERpc3BsYXlOYW1lIiwiYXZhdGFyVXJsIjoiaHR0cHM6"
        + "Ly93d3cuZ3JhdmF0YXIuY29tL2F2YXRhci81MjFiZDZkYjcwNmZiMjRiZmVmNjhlNmNm"
        + "ZmU4ODdlNj9kPXdhdmF0YXIifX19.pKGgctfU7xPgvbZXfYXjvz-qNohw5L_mut1yQbT"
        + "tffY";

    /**
     * Callee values.
     */
    private final static String CALLEE_EMAIL = "callee@jitsi.org";
    private final static String CALLEE_NAME = "Callee DisplayName";
    private final static String CALLEE_AVATAR_URL =
        "https://www.gravatar.com/avatar/c6560f2a9280b30cdac10adcdea9153d?d=wavatar";
    /**
     * User values.
     */
    private final static String USER_EMAIL = "example1@jitsi.org";
    private final static String USER_NAME = "User DisplayName";
    private final static String USER_AVATAR_URL =
        "https://www.gravatar.com/avatar/521bd6db706fb24bfef68e6cffe887e6?d=wavatar";

    /**
     * Runs the test.
     */
    @Test
    public void testRingOverlay()
    {
        ensureOneParticipant(JWT_TOKEN, null);

        WebDriver owner = getParticipant1().getDriver();

        // test the values show on ring overlay about the user we are calling to
        String ringOverlayDivXpath = "div[@id='ringOverlay']";
        WebElement calleeNameElem =
            owner.findElement(By.xpath(
                "//" + ringOverlayDivXpath
                + "//div[@class='ringing__caller-info']"
                + "/p"));
        String calleeNameText = calleeNameElem.getText();
        assertTrue(
            calleeNameText.equals(CALLEE_NAME),
            "Callee name is not correct! (" + CALLEE_NAME + ")");

        WebElement calleeAvatarElem =
            owner.findElement(By.xpath(
                "//" + ringOverlayDivXpath
                + "//img[@class='ringing__avatar']"));
        assertEquals(CALLEE_AVATAR_URL, calleeAvatarElem.getAttribute("src"));

        // Test local user values that were set by the token string
        // these are display name, email and avatar
        String emailInput = "//input[@id='setEmail']";
        // wait for the toolbar to be visible before clicking on it
        TestUtils.waitForDisplayedElementByXPath(
            owner, "//div[contains(@class, 'toolbar_secondary')]", 3);
        MeetUIUtils.clickOnToolbarButton(owner, "toolbar_button_profile");
        TestUtils.waitForDisplayedElementByXPath(
            owner, emailInput, 5);
        assertEquals(USER_EMAIL,
            owner.findElement(By.xpath(emailInput)).getAttribute("value"));

        assertEquals(USER_NAME,
            owner.findElement(By.xpath("//input[@id='setDisplayName']"))
                .getAttribute("value"));

        assertEquals(USER_AVATAR_URL,
            owner.findElement(By.xpath(
                    "//div[contains(@class, 'toolbar_secondary')]//img[@id='avatar']"))
                .getAttribute("src"));

        // now let's join a second participant and check that there is no
        // ring overlay and the second participant sees the same display name
        // and avatar

        // Join with second participant
        ensureTwoParticipants();
        WebDriver secondParticipant = getParticipant2().getDriver();

        // overlay absent or hidden
        TestUtils.waitForElementNotPresentOrNotDisplayedByXPath(
            owner, "//" + ringOverlayDivXpath, 2);

        new DisplayNameTest(this)
            .checkRemoteVideoForName(
                secondParticipant, getParticipant1(), USER_NAME);

        // let's check the avatar now

        // Mute owner video
        String ownerResource = MeetUtils.getResourceJid(owner);
        MeetUIUtils.muteVideoAndCheck(owner, secondParticipant);

        MeetUIUtils.clickOnRemoteVideo(secondParticipant, ownerResource);
        // Check if owner's avatar is on large video now
        TestUtils.waitForCondition(secondParticipant, 5,
            (ExpectedCondition<Boolean>) d -> {
                String currentSrc = AvatarTest.getLargeVideoSrc(d);
                return currentSrc.equals(USER_AVATAR_URL);
            });

    }
}
