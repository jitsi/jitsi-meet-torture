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
import org.jitsi.meet.test.base.stats.*;
import org.jitsi.meet.test.pageobjects.base.*;
import org.jitsi.meet.test.pageobjects.web.*;
import org.jitsi.meet.test.util.*;
import org.jitsi.meet.test.web.stats.*;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.*;
import org.openqa.selenium.remote.*;
import org.openqa.selenium.support.ui.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.logging.*;
import java.util.stream.*;

/**
 * The web specific participant implementation.
 */
public class WebParticipant extends Participant<WebDriver>
    implements JavascriptExecutor
{
    /**
     * Default config for Web participants.
     */
    private static final String DEFAULT_CONFIG
        = "config.requireDisplayName=false"
            + "&config.debug=true"
            + "&config.testing.testMode=true"
            + "&config.disableAEC=true"
            + "&config.disableNS=true"
            + "&config.enableTalkWhileMuted=false"
            + "&config.callStatsID=false"
            + "&config.alwaysVisibleToolbar=true"
            + "&config.p2p.enabled=false"
            + "&config.p2p.useStunTurn=false"
            + "&config.gatherStats=true"
            + "&config.disable1On1Mode=true"
            + "&config.analytics.disabled=true"
            + "&interfaceConfig.SHOW_CHROME_EXTENSION_BANNER=false"
            + "&interfaceConfig.DISABLE_FOCUS_INDICATOR=true";

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
     *  The javascript code which returns an array of remote participant IDs.
     */
    public static final String GET_REMOTE_PARTICIPANT_IDS =
        "return APP.conference._room.getParticipants().map(p => p._id);";

    private ChatPanel chatPanel;
    private DialInNumbersPage dialInNumbersPage;
    private InviteDialog inviteDialog;
    private LargeVideo largeVideo;
    private SecurityDialog securityDialog;
    private SettingsDialog settingsDialog;
    private Toolbar toolbar;
    private WebFilmstrip filmstrip;

    /**
     * Constructs a Participant.
     *
     * @param name    the name.
     * @param driver  its driver instance.
     * @param type    the type (type of browser).
     */
    public WebParticipant(
            String name, WebDriver driver, ParticipantType type)
    {
        super(name, driver, type, DEFAULT_CONFIG);
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

        if (conferenceUrl.getIframeToNavigateTo() != null)
        {
            // let's wait for loading and switch to that iframe so we can continue
            // with regular tests
            WebDriverWait wait = new WebDriverWait(driver, 10);
            wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(
                By.id(conferenceUrl.getIframeToNavigateTo())));
        }
        else
        {
            MeetUtils.waitForPageToLoad(driver);
        }

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

        if ("false".equals(conferenceUrl.getFragmentParam("config.callStatsID")))
        {
            // Hack-in disabling of callstats (old versions of jitsi-meet don't
            // handle URL parameters)
            executeScript("config.callStatsID=false;");
        }

        String version = TestUtils.executeScriptAndReturnString(driver,
            "return JitsiMeetJS.version;");
        TestUtils.print(name + " lib-jitsi-meet version: " + version
            + (driver instanceof RemoteWebDriver ?
                " sessionID: "
                    + ((RemoteWebDriver)driver).getSessionId() : ""));

        executeScript("document.title='" + name + "'");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doHangUp()
    {
        getToolbar().clickHangUpButton();

        TestUtils.waitMillis(500);
        // open a blank page after hanging up, to make sure
        // we will successfully navigate to the new link containing the
        // parameters, which change during testing
        driver.get("about:blank");
        MeetUtils.waitForPageToLoad(driver);
    }

    /**
     * Executes a script in this {@link WebParticipant}'s {@link WebDriver}.
     * See {@link JavascriptExecutor#executeScript(String, Object...)}.
     */
    @Override
    public Object executeScript(String var1, Object... var2)
    {
        return getJSExecutor().executeScript(var1, var2);
    }

    /**
     * Executes a script asynchronously in this {@link WebParticipant}'s
     * {@link WebDriver}.
     * See {@link JavascriptExecutor#executeScript(String, Object...)}.
     */
    @Override
    public Object executeAsyncScript(String var1, Object... var2)
    {
        return getJSExecutor().executeAsyncScript(var1, var2);
    }

    private JavascriptExecutor getJSExecutor()
    {
        if (driver instanceof JavascriptExecutor)
        {
            return (JavascriptExecutor) driver;
        }
        else
        {
            throw new RuntimeException(
                "The driver is not capable of executing JavaScript");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getEndpointId()
    {
        Object o = executeScript("return APP.conference.getMyUserId();");
        return o == null ? null : o.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getMeetDebugLog()
    {
        try
        {
            Object log
                = executeScript(
                "try{ "
                        + "return JSON.stringify("
                        + "  APP.conference.getLogs(), null, '    ');"
                        + "}catch (e) {}");

            return log instanceof String ? (String) log : null;
        }
        catch (Exception e)
        {
            Logger.getGlobal().log(
                    Level.SEVERE,
                    "Failed to get meet logs from " + name,
                    e);

            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getRTPStats()
    {
        try
        {
            // we default all tests to not use p2p, so we default using jvb here
            Object log
                = MeetUtils.getRtpStats(this.getDriver(), true);

            return log instanceof String ? (String) log : null;
        }
        catch (Exception e)
        {
            Logger.getGlobal().log(
                    Level.SEVERE,
                    "Failed to get meet rtp stats from " + name,
                    e);

            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getProtocol()
    {
        Object protocol
            = executeScript(
            "try {"
                    + "return APP.conference.getStats().transport[0].type;"
                    + "} catch (err) { return 'error: '+err; }");

        return (protocol == null) ? null : protocol.toString().toLowerCase();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected RtpStatistics getRtpStatistics()
    {
        return new WebRtpStatistics(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isP2pConnected()
    {
        return TestUtils.getBooleanResult(
                executeScript(MeetUtils.P2P_ICE_CONNECTED_CHECK_SCRIPT));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isXmppConnected()
    {
        return TestUtils.getBooleanResult(
            executeScript(
                "return APP.conference._room.xmpp.connection.connected;"));
    }

    /**
     * Whether this participant supports video, based on the browser type.
     * @return {@code true} if the participant browser do not support video.
     */
    public boolean isAudioOnlyParticipant()
    {
        return getType() == ParticipantType.safari;
    }

    /**
     * {@inheritDoc}
     */
    protected boolean isIceConnected()
    {
        return TestUtils.executeScriptAndReturnBoolean(
                driver, ICE_CONNECTED_CHECK_SCRIPT);
    }

    /**
     * Checks whether this participant is in the MUC.
     *
     * @return {@code true} if the this participant has joined the
     * room; otherwise, {@code false}
     */
    @Override
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
     * Waits for number of remote streams.
     * @param n number of remote streams to wait for.
     */
    public void waitForRemoteStreams(int n)
    {
        waitForCondition(
            () -> (Boolean) executeScript(
                "return APP.conference"
                    + ".getNumberOfParticipantsWithTracks() >= " + n + ";"),
                15,
                "waitForRemoteStreams:" + n);
    }

    /**
     * Waits for number of participants.
     * @param n number of participants to wait for.
     */
    public void waitForParticipants(int n)
    {
        waitForCondition(
            () -> (Boolean) executeScript(
                "return APP.conference"
                    + ".listMembers().length === " + n + ";"),
                15,
                "waitForParticipants:" + n);
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
        // We've observed intermittent failures with the keys being sent to the
        // wrong element (e.g. the chat input field). Selecting "body" instead
        // of another element seems to make this condition appear less often.
        WebDriver driver = getDriver();
        WebElement body = driver.findElement(By.tagName("body"));
        Actions actions = new Actions(driver);
        actions.moveToElement(body);
        actions.sendKeys(body, shortcut.toString());
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
     * Returns the value for the given <tt>key</tt> from the config.js loaded
     * for the participant.
     *
     * @param key the <tt>String</tt> key from config.js.
     * @return the value for the given <tt>key</tt> from the config.js loaded
     * for the participant.
     */
    public Object getConfigValue(String key)
    {
        return executeScript("return config." + key);
    }

    /**
     * @return a representation of the dial in page of this participant.
     */
    public DialInNumbersPage getDialInNumbersPage()
    {
        if (dialInNumbersPage == null)
        {
            dialInNumbersPage = new DialInNumbersPage(this);
        }

        return dialInNumbersPage;
    }

    /**
     * @return a representation of the invite dialog of this participant.
     */
    public InviteDialog getInviteDialog()
    {
        if (inviteDialog == null)
        {
            inviteDialog = new InviteDialog(this);
        }

        return inviteDialog;
    }

    /**
     * @return a representation of the large video of this participant.
     */
    public LargeVideo getLargeVideo()
    {
        if (largeVideo == null)
        {
            largeVideo = new LargeVideo(this);
        }

        return largeVideo;
    }

    /**
     * @return a representation of the security dialog of this participant.
     */
    public SecurityDialog getSecurityDialog()
    {
        if (securityDialog == null)
        {
            securityDialog = new SecurityDialog(this);
        }

        return securityDialog;
    }

    /**
     * @return a representation of the settings dialog of this participant.
     */
    public SettingsDialog getSettingsDialog()
    {
        if (settingsDialog == null)
        {
            settingsDialog = new SettingsDialog(this);
        }

        return settingsDialog;
    }

    /**
     * @return a representation of the toolbar of this participant.
     */
    public Toolbar getToolbar()
    {
        if (toolbar == null)
        {
             toolbar = new Toolbar(this);
        }

        return toolbar;
    }

    /**
     * @return a representation of the filmstrip of this participant.
     */
    public WebFilmstrip getFilmstrip()
    {
        if (filmstrip == null)
        {
            filmstrip = new WebFilmstrip(this);
        }

        return filmstrip;
    }

    @Override
    public List<RemoteParticipant<WebDriver>> getRemoteParticipants()
    {
        List<String> remoteParticipantIDs
            = (List) executeScript(GET_REMOTE_PARTICIPANT_IDS);

        return remoteParticipantIDs.stream()
            .map(id -> new WebRemoteParticipant(this.getDriver(), id))
            .collect(Collectors.toList());
    }

    /**
     /**
     * A list of log string entries.
     *
     * @return a list of log entries.
     */
    @Override
    public List getBrowserLogs()
    {
        try
        {
            return (List) executeScript("return APP.debugLogs ? APP.debugLogs.getLogs() : [];");
        }
        catch (RuntimeException t)
        {
            // if APP is missing or debugLogs missing
            Logger.getGlobal().log(
                Level.SEVERE,
                "Failed to obtain browser logs:" + t.getMessage());
            return null;
        }
    }
}
