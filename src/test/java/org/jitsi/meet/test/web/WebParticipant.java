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
import org.jitsi.meet.test.pageobjects.web.*;
import org.jitsi.meet.test.util.*;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.*;
import org.openqa.selenium.remote.*;
import org.openqa.selenium.support.ui.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.logging.*;

/**
 * The web specific participant implementation.
 */
public class WebParticipant extends Participant<WebDriver>
    implements JavascriptExecutor
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

    private ChatPanel chatPanel;
    private DialInNumbersPage dialInNumbersPage;
    private InfoDialog infoDialog;
    private Toolbar toolbar;

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
        super(name, driver, type);
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

        // Hack-in disabling of callstats (old versions of jitsi-meet don't
        // handle URL parameters)
        executeScript("config.callStatsID=false;");

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
        MeetUIUtils.clickOnButton(driver, "toolbar_button_hangup", false);

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
    public boolean isP2pConnected()
    {
        // FIXME hmm this checks for ICE connected state which does not
        // necessarily mean that the conference is currently in the P2P mode.
        // It may turn out we're not really checking if P2P is connected
        // and the method name may be confusing.
        return TestUtils.getBooleanResult(
                executeScript(MeetUtils.ICE_CONNECTED_CHECK_SCRIPT));
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
     * @return a representation of the info dialog of this participant.
     */
    public InfoDialog getInfoDialog()
    {
        if (infoDialog == null)
        {
            infoDialog = new InfoDialog(this);
        }

        return infoDialog;
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
}
