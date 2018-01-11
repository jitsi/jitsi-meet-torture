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
import org.jitsi.meet.test.util.*;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.*;
import org.openqa.selenium.support.ui.*;

import java.util.*;

/**
 * The web specific participant implementation.
 * @param <T>
 */
public class WebParticipant<T extends WebDriver>
    extends Participant
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
     * Constructs a Participant.
     *
     * @param name    the name.
     * @param driver  its driver instance.
     * @param type    the type (type of browser).
     * @param meetURL the url to use when joining room.
     */
    public WebParticipant(String name, WebDriver driver,
        ParticipantFactory.ParticipantType type, String meetURL)
    {
        super(name, driver, type, meetURL);
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
        Object res = ((JavascriptExecutor) getDriver())
            .executeScript(IS_MUC_JOINED);
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
                Map stats = (Map) ((JavascriptExecutor) d)
                    .executeScript("return APP.conference.getStats();");

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
            .until((ExpectedCondition<Boolean>) d
                        -> (Boolean)((JavascriptExecutor) d)
                .executeScript(
                    "return APP.conference"
                        + ".getNumberOfParticipantsWithTracks() >= "
                        + n + ";"));
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

}
