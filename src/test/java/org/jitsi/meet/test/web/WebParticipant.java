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
package org.jitsi.meet.test.web;

import org.jitsi.meet.test.base.*;
import org.jitsi.meet.test.pageobjects.web.ChatPanel;
import org.jitsi.meet.test.pageobjects.web.DialInNumbersPage;
import org.jitsi.meet.test.pageobjects.web.InfoDialog;
import org.jitsi.meet.test.util.*;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.*;
import org.openqa.selenium.support.ui.*;

import java.util.*;
import java.util.concurrent.*;

/**
 * The web specific participant implementation.
 */
public class WebParticipant extends Participant<WebDriver>
{
    /**
     * The javascript code which returns {@code true} if we are joined in
     * the muc.
     */
    private static final String IS_MUC_JOINED =
        "return APP.conference.isJoined();";

    /**
     * The javascript code which returns {@code true} if the ICE connection
     * is in state 'connected'.
     */
    public static final String ICE_CONNECTED_CHECK_SCRIPT =
        "return APP.conference.getConnectionState() === 'connected';";

    /**
     * Default config for Web participants.
     */
    public static final String DEFAULT_CONFIG
        = "config.requireDisplayName=false"
            + "&config.debug=true"
            + "&config.disableAEC=true"
            + "&config.disableNS=true"
            + "&config.callStatsID=false"
            + "&config.alwaysVisibleToolbar=true"
            + "&config.p2p.enabled=false"
            + "&config.disable1On1Mode=true";

    private ChatPanel chatPanel;
    private DialInNumbersPage dialInNumbersPage;
    private InfoDialog infoDialog;

    /**
     * Constructs a Participant.
     *
     * @param name    the name.
     * @param driver  its driver instance.
     * @param type    the type (type of browser).
     * @param meetURL the url to use when joining room.
     */
    public WebParticipant(
            String name, WebDriver driver, ParticipantType type, String meetURL)
    {
        super(name, driver, type, meetURL, DEFAULT_CONFIG);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doJoinConference(JitsiMeetUrl conferenceUrl)
    {
        // with chrome v52 we start getting error:
        // "Timed out receiving message from renderer" and
        // "Navigate timeout: cannot determine loading status"
        // seems its a bug or rare problem, maybe concerns async loading
        // of resources ...
        // https://bugs.chromium.org/p/chromedriver/issues/detail?id=402
        // even there is a TimeoutException the page is loaded correctly
        // and driver is operating, we just lower the page load timeout
        // default is 3 minutes and we log and skip this exception
        driver.manage().timeouts().pageLoadTimeout(30, TimeUnit.SECONDS);
        try
        {
            driver.get(conferenceUrl.toString());
        }
        catch (org.openqa.selenium.TimeoutException ex)
        {
            ex.printStackTrace();
            TestUtils.print("TimeoutException while loading page, "
                + "will skip it and continue:" + ex.getMessage());
        }

        MeetUtils.waitForPageToLoad(driver);

        // disables animations
        executeScript("try { jQuery.fx.off = true; } catch(e) {}");

        executeScript("APP.UI.dockToolbar(true);");

        // disable keyframe animations (.fadeIn and .fadeOut classes)
        executeScript("$('<style>.notransition * { "
            + "animation-duration: 0s !important; "
            + "-webkit-animation-duration: 0s !important; transition:none; }"
            + " </style>').appendTo(document.head);");
        executeScript("$('body').toggleClass('notransition');");

        // disable the blur effect in firefox as it has some performance issues
        if (this.type.isFirefox())
        {
            executeScript(
                "try { var blur "
                    + "= document.querySelector('.video_blurred_container'); "
                    + "if (blur) { "
                    + "document.querySelector('.video_blurred_container')"
                    + ".style.display = 'none' "
                    + "} } catch(e) {}");
        }

        // Hack-in disabling of callstats (old versions of jitsi-meet don't
        // handle URL parameters)
        executeScript("config.callStatsID=false;");

        String version
            = TestUtils.executeScriptAndReturnString(driver,
            "return JitsiMeetJS.version;");
        TestUtils.print(name + " lib-jitsi-meet version: " + version);

        executeScript("document.title='" + name + "'");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doHangUp()
    {
        MeetUIUtils.clickOnButton(driver, "toolbar_button_hangup", false);

        TestUtils.waitMillis(500);
        // open a blank page after hanging up, to make sure
        // we will successfully navigate to the new link containing the
        // parameters, which change during testing
        driver.get("about:blank");
        MeetUtils.waitForPageToLoad(driver);
    }

    /**
     * Waits until this participant joins the MUC.
     * @param timeout the maximum time to wait in seconds.
     */
    public void waitToJoinMUC(long timeout)
    {
        TestUtils.waitForBoolean(
            getDriver(),
            IS_MUC_JOINED,
            timeout);
    }

    /**
     * Checks whether a participant is in the MUC.
     *
     * @return {@code true} if the this participant has joined the
     * room; otherwise, {@code false}
     */
    public boolean isInMuc()
    {
        Object res = executeScript(IS_MUC_JOINED);
        return res != null && res.equals(Boolean.TRUE);
    }

    /**
     * Waits 15 sec for the given participant to enter the ICE 'connected'
     * state.
     */
    public void waitForIceConnected()
    {
        waitForIceConnected(15);
    }

    /**
     * Waits for the given participant to enter the ICE 'connected' state.
     * @param timeout timeout in seconds.
     */
    public void waitForIceConnected(long timeout)
    {
        TestUtils.waitForBoolean(
            getDriver(), ICE_CONNECTED_CHECK_SCRIPT, timeout);
    }

    /**
     * Waits until data has been sent and received over the ICE connection
     * in this participant.
     */
    public void waitForSendReceiveData()
    {
        new WebDriverWait(getDriver(), 15)
            .until((ExpectedCondition<Boolean>) d -> {
                Map stats
                    = (Map) executeScript("return APP.conference.getStats();");

                Map<String, Long> bitrate =
                    (Map<String, Long>) stats.get("bitrate");

                if (bitrate != null)
                {
                    long download = bitrate.get("download");
                    long upload = bitrate.get("upload");

                    if (download > 0 && upload > 0)
                        return true;
                }

                return false;
            });
    }

    /**
     * Waits for number of remote streams.
     * @param n number of remote streams to wait for.
     */
    public void waitForRemoteStreams(int n)
    {
        new WebDriverWait(getDriver(), 15)
            .until(
                (ExpectedCondition<Boolean>) d
                    -> (Boolean) executeScript(
                        "return APP.conference"
                        + ".getNumberOfParticipantsWithTracks() >= " + n + ";"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDisplayName(String name)
    {
        WebDriver driver = getDriver();

        WebElement elem =
            driver.findElement(By.xpath(
                "//span[@id='localVideoContainer']"
                    + "//span[@id='localDisplayName']"));
        // hover the element before clicking
        Actions actions = new Actions(driver);
        actions.moveToElement(elem);
        actions.perform();

        elem.click();

        WebElement inputElem =
            driver.findElement(By.xpath(
                "//span[@id='localVideoContainer']"
                    + "//input[@id='editDisplayName']"));
        actions = new Actions(driver);
        actions.moveToElement(inputElem);
        actions.perform();

        if (name != null && name.length() > 0)
        {
            inputElem.sendKeys(name);
        }
        else
        {
            inputElem.sendKeys(Keys.BACK_SPACE);
        }

        inputElem.sendKeys(Keys.RETURN);
        // just click somewhere to lose focus, to make sure editing has ended
        MeetUIUtils.clickOnLocalVideo(driver);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void pressShortcut(Character shortcut)
    {
        // We just need some element to which to send the shortcut (so that
        // the focused element doesn't swallow them).
        WebDriver driver = getDriver();
        WebElement largeVideo = driver.findElement(By.id("largeVideo"));
        Actions actions = new Actions(driver);
        actions.moveToElement(largeVideo);
        actions.sendKeys(largeVideo, shortcut.toString());
        actions.perform();
    }

    /**
     * @return a representation of the chat panel of this participant.
     */
    public ChatPanel getChatPanel()
    {
        if (chatPanel == null)
        {
            chatPanel = new ChatPanel(this);
        }

        return chatPanel;
    }

    /**
     * @return a representation of the dial in page of this participant.
     */
    public DialInNumbersPage getDialInNumbersPage() {
        if (dialInNumbersPage == null)
        {
            dialInNumbersPage = new DialInNumbersPage(this);
        }

        return dialInNumbersPage;
    }

    /**
     * @return a representation of the info dialog of this participant.
     */
    public InfoDialog getInfoDialog() {
        if (infoDialog == null)
        {
            infoDialog = new InfoDialog(this);
        }

        return infoDialog;
    }
}
