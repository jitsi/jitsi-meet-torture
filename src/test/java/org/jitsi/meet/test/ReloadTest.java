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
import org.testng.*;
import org.testng.annotations.*;

import java.util.*;

import static org.testng.Assert.*;

/**
 * Launches a hook script that will restart prosody and jicofo and checks
 * if the client reloads the conference and if the conference is working after
 * the reload. 
 */
public class ReloadTest
    extends AbstractBaseTest
{
    /**
     * 
     * A property to specified the external script that will be used to
     * restart prosody or jicofo.
     */
    public final static String HOOK_SCRIPT = "reloads.hook.script";
    
    /**
     * A property to specified the external script that will be used to
     * restart prosody or jicofo.
     */
    public final static String HOST = "reloads.host";
    
    /**
     * The display name value.
     */
    private final static String DISPLAY_NAME = "testDisplayName";

    @Override
    public void setup()
    {
        super.setup();

        final String hookScript = System.getProperty(HOOK_SCRIPT);
        final String host = System.getProperty(HOST);

        if (hookScript == null || host == null)
        {
            throw new SkipException(
                "no hook script or host has been specified");
        }

        ensureTwoParticipants();
    }

    /**
     * Restarts prosody, verifies that all participants have been reloaded,
     * checks that all participant have joined successfully the conference 
     * and audio/video data is transmitted. Also dismisses Video Bridge not 
     * available error dialog (which is shown because of prosody restart)
     */
    @Test
    public void testProsodyRestart()
    {
        setupListeners();
        startReloadScript(new String[]{"--restart-prosody"});
        waitForReloadAndTest();
        dismissBridgeNotAvailableDialog(getParticipant1().getDriver());
        dismissBridgeNotAvailableDialog(getParticipant2().getDriver());
    }
    
    /**
     * Restarts jicofo, verifies that all participants have been reloaded,
     * checks that all participant have joined successfully the conference 
     * and audio/video data is transmitted.
     */
    @Test(dependsOnMethods = { "ownerSetDisplayNameAndCheck" })
    public void testJicofoRestart()
    {
        setupListeners();
        startReloadScript(new String[]{"--restart-jicofo"});
        waitForReloadAndTest(true);
    }
    
    /**
     * Executes {@link StopVideoTest#stopVideoOnOwnerAndCheck()}.
     */
    @Test(dependsOnMethods = { "testProsodyRestart" })
    public void ownerVideoMuteAndCheck()
    {
        new StopVideoTest(getParticipant1(), getParticipant2())
            .stopVideoOnOwnerAndCheck();
    }
    
    /**
     * Executes {@link MuteTest#muteOwnerAndCheck()}.
     */
    @Test(dependsOnMethods = { "ownerVideoMuteAndCheck" })
    public void ownerAudioMuteAndCheck()
    {
        new MuteTest(getParticipant1(), getParticipant2(), null).muteOwnerAndCheck();
    }
    
    /**
     * Executes {@link DisplayNameTest#checkDisplayNameChange}.
     */
    @Test(dependsOnMethods = { "ownerAudioMuteAndCheck" })
    public void ownerSetDisplayNameAndCheck()
    {
        new DisplayNameTest(getParticipant1(), getParticipant2())
            .checkDisplayNameChange(DISPLAY_NAME);
    }
    
    /**
     * Checks video mute status for the local video for the owner and 
     * the remote video for second participant.
     */
    @Test(dependsOnMethods = { "testJicofoRestart" })
    public void ownerCheckVideoMuted()
    {
        MeetUIUtils.assertMuteIconIsDisplayed(
            getParticipant1().getDriver(),
            getParticipant1().getDriver(),
            true,
            true,
            "owner");
        MeetUIUtils.assertMuteIconIsDisplayed(
            getParticipant2().getDriver(),
            getParticipant1().getDriver(),
            true,
            true,
            "owner");
    }
    
    /**
     * Checks audio mute status for the local video for the owner and 
     * the remote video for second participant.
     */
    @Test(dependsOnMethods = { "ownerCheckVideoMuted" })
    public void ownerCheckAudioMuted()
    {
        MeetUIUtils.assertMuteIconIsDisplayed(
            getParticipant1().getDriver(),
            getParticipant1().getDriver(),
            true,
            false,
            "owner");
        MeetUIUtils.assertMuteIconIsDisplayed(
            getParticipant2().getDriver(),
            getParticipant1().getDriver(),
            true,
            false,
            "owner");
    }
    
    /**
     * Checks the display name of the owner.
     */
    @Test(dependsOnMethods = { "ownerCheckAudioMuted" })
    public void ownerCheckDisplayName()
    {
        DisplayNameTest test = new DisplayNameTest();
        test.doLocalDisplayNameCheck(DISPLAY_NAME);
        test.doRemoteDisplayNameCheck(DISPLAY_NAME);
    }
    
    /**
     * Dismisses Video Bridge not available error dialog for a participant
     * @param participant the participant
     */
    private void dismissBridgeNotAvailableDialog(WebDriver participant)
    {
        WebElement element;

        try
        {
            element = participant.findElement(By.name("jqi_state0_buttonOk"));
        } catch (org.openqa.selenium.NoSuchElementException ex)
        {
            element = null;
        }
         
        if (element != null)
            element.click();
    }
    
    /**
     * Executes <tt>HOOK_SCRIPT</tt> with the passed params.
     * @param params additional parameters that will be passed to the script
     */
    private void startReloadScript(final String[] params) 
    {
        final String hookScript = System.getProperty(HOOK_SCRIPT);
        final String host = System.getProperty(HOST);

        if (hookScript == null || host == null)
            return;

        CmdExecutor exec = new CmdExecutor();
        try
        {
            List<String> cmd = new ArrayList<>();
            cmd.add(hookScript);
            cmd.add(host);
            Collections.addAll(cmd, params);

            int result = exec.executeCmd(cmd);

            assertEquals(0, result, "Script returned non-zero value");
        }
        catch (Exception hookException)
        {
            fail("Error executing hook script:" + hookException.getMessage());
        }
    }
    
    /** 
     * Executes {@link #waitForReloadAndTest(boolean)} with false parameter. 
     */
    private void waitForReloadAndTest()
    {
        waitForReloadAndTest(false);
    }
    
    /**
     * Adds JS listeners to Jitsi Meet that will detect CONFERENCE_LEFT event.
     * That means that the reload has been triggered.
     */
    private void setupListeners()
    {
        WebDriver[] drivers =
        {
            getParticipant1().getDriver(),
            getParticipant2().getDriver()
        };
        String script
            = "APP.conference._room.addEventListener("
                + "        JitsiMeetJS.events.conference.CONFERENCE_LEFT,"
                + "        function (o) {"
                + "            APP.conference._room.conference_left_event = true;"
                + "        });"
                + "";
        for (WebDriver driver : drivers)
        {
            TestUtils.executeScript(driver, script);
        }
    }

    /**
     * Detects reload, waits for ice connected state event and verifies that 
     * audio/video data is transmitted. 
     * @param isOwnerMuted <tt>true</tt> if owner is muted, false otherwise.
     */
    private void waitForReloadAndTest(boolean isOwnerMuted)
    {
        Participant[] participants =
            {
                getParticipant1(),
                getParticipant2()
            };
        final String checkForConferenceLeftScript = "return "
            + "APP.conference._room.conference_left_event;";
        for (Participant p : participants)
        {
            (new WebDriverWait(p.getDriver(), 200))
                .until((ExpectedCondition<Boolean>) d -> {
                    Object res =
                        ((JavascriptExecutor) d)
                            .executeScript(checkForConferenceLeftScript);
                    return res != null && res.equals(Boolean.TRUE);
                });
        }
        
        print("Reload detected");

        for (Participant p : participants)
        {
            TestUtils.executeScript(p.getDriver(),
                "APP.conference._room.conference_left_event = false;");
        }
        
        print("Wait for ice connected.");
        for (Participant p : participants)
        {
            p.waitForIceConnected(60);
        }
        
        print("Wait for send receive data on the owner side.");
        getParticipant1().waitForSendReceiveData();

        if (isOwnerMuted)
        {
            print("Wait for send data on the second "
                + "participant side.");
            WebDriver participant = getParticipant2().getDriver();
            new WebDriverWait(participant, 15)
            .until((ExpectedCondition<Boolean>) d -> {
                Map stats = (Map) ((JavascriptExecutor) participant)
                        .executeScript("return APP.conference.getStats();");

                Map<String, Long> bitrate =
                        (Map<String, Long>) stats.get("bitrate");

                if (bitrate != null)
                {
                    long upload = bitrate.get("upload");
                    return upload > 0;
                }

                return false;
            });
        }
        else 
        {
            print("Wait for send receive data on the second "
                + "participant side.");
            getParticipant2().waitForSendReceiveData();
        }
        
        print("Reload finished.");
    }
}
