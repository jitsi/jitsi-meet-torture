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
package org.jitsi.meet.test.base;

import org.jitsi.meet.test.util.*;
import org.openqa.selenium.*;

import java.util.concurrent.*;

/**
 * The participant instance holding the {@link WebDriver}.
 * @param <T> the driver type.
 */
public class Participant<T extends WebDriver>
{
    /**
     * The driver.
     */
    private final T driver;

    /**
     * The participant type chrome, firefox etc.
     */
    private final ParticipantFactory.ParticipantType type;

    /**
     * The name of the participant.
     */
    private final String name;

    /**
     * The url to join conferences.
     */
    private final String meetURL;

    /**
     * Is hung up.
     */
    private boolean hungUp = true;

    /**
     * Constructs a Participant.
     * @param name the name.
     * @param driver its driver instance.
     * @param type the type (type of browser).
     * @param meetURL the url to use when joining room.
     */
    public Participant(
        String name,
        T driver,
        ParticipantFactory.ParticipantType type,
        String meetURL)
    {
        this.name = name;
        this.driver = driver;
        this.type = type;
        this.meetURL = meetURL;
    }

    /**
     * Joins a conference.
     * @param roomName the room name to join.
     */
    public void joinConference(String roomName)
    {
        this.joinConference(roomName, null);
    }

    /**
     * Joins a conference.
     * @param roomName the room name to join.
     * @param fragment adds the given string to the fragment part of the URL.
     */
    public void joinConference(String roomName, String fragment)
    {
        String URL = this.meetURL + "/" + roomName;
        URL += "#config.requireDisplayName=false";
        URL += "&config.debug=true";
        URL += "&config.disableAEC=true";
        URL += "&config.disableNS=true";
        URL += "&config.callStatsID=false";
        URL += "&config.alwaysVisibleToolbar=true";
        URL += "&config.p2p.enabled=false";
        URL += "&config.disable1On1Mode=true";

        if(fragment != null)
            URL += "&" + fragment;

        System.err.println(name + " is opening URL: " + URL);

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
            driver.manage().timeouts()
                .pageLoadTimeout(30, TimeUnit.SECONDS);
            try
            {
                driver.get(URL);
            }
            catch (org.openqa.selenium.TimeoutException ex)
            {
                ex.printStackTrace();
                System.err.println("TimeoutException while loading page, "
                    + "will skip it and continue:" + ex.getMessage());
            }
        }
        MeetUtils.waitForPageToLoad(driver);

        // disables animations
        ((JavascriptExecutor) driver)
            .executeScript("try { jQuery.fx.off = true; } catch(e) {}");

        ((JavascriptExecutor) driver)
            .executeScript("APP.UI.dockToolbar(true);");

        // disable keyframe animations (.fadeIn and .fadeOut classes)
        ((JavascriptExecutor) driver)
            .executeScript("$('<style>.notransition * { "
                + "animation-duration: 0s !important; "
                + "-webkit-animation-duration: 0s !important; } </style>')"
                + ".appendTo(document.head);");
        ((JavascriptExecutor) driver)
            .executeScript("$('body').toggleClass('notransition');");

        // Hack-in disabling of callstats (old versions of jitsi-meet don't
        // handle URL parameters)
        ((JavascriptExecutor) driver)
            .executeScript("config.callStatsID=false;");

        String version
            = TestUtils.executeScriptAndReturnString(driver,
                "return JitsiMeetJS.version;");
        System.err.println(name + " lib-jitsi-meet version: " + version);

        ((JavascriptExecutor) driver)
            .executeScript("document.title='" + name + "'");

        this.hungUp = false;
    }

    /**
     * Quits the driver.
     */
    public void quit()
    {
        try
        {
            driver.quit();

            TestUtils.waitMillis(500);
        }
        catch(Throwable t)
        {
            t.printStackTrace();
        }

    }

    /**
     * Hangup participant using the UI.
     */
    public void hangUp()
    {
        MeetUIUtils.clickOnToolbarButton(
            driver, "toolbar_button_hangup", false);

        TestUtils.waitMillis(500);

        this.hungUp = true;

        System.err.println("Hung up in " + name + ".");

        // open a blank page after hanging up, to make sure
        // we will successfully navigate to the new link containing the
        // parameters, which change during testing
        driver.get("about:blank");
        MeetUtils.waitForPageToLoad(driver);
    }

    /**
     * Check whether participant has hangup or is still in a conference.
     * @return
     */
    public boolean isHungUp()
    {
        return this.hungUp;
    }

    /**
     * The driver instance.
     * @return driver instance.
     */
    public T getDriver()
    {
        return driver;
    }

    @Override
    public String toString()
    {
        return super.toString();
    }

    /**
     * Returns the participant type.
     * @return the participant type.
     */
    public ParticipantFactory.ParticipantType getType()
    {
        return type;
    }

    /**
     * Returns participant's name.
     * @return
     */
    public String getName()
    {
        return name;
    }
}
