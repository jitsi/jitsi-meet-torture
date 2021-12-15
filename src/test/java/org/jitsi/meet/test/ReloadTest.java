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

import org.jitsi.meet.test.util.*;
import org.jitsi.meet.test.web.*;

import org.openqa.selenium.*;
import org.testng.*;
import org.testng.annotations.*;

import java.util.*;

import static org.testng.Assert.*;
import static org.jitsi.meet.test.util.TestUtils.*;

/**
 * Launches a hook script that will restart prosody and jicofo and checks
 * if the client reloads the conference and if the conference is working after
 * the reload. 
 */
public class ReloadTest
    extends WebTestBase
{
    /**
     * A property to specify the external script that will be used to
     * restart prosody or jicofo.
     */
    public final static String HOOK_SCRIPT = "reloads.hook.script";
    
    /**
     * TODO document
     */
    public final static String HOST = "reloads.host";
    
    /**
     * The display name value.
     */
    private final static String DISPLAY_NAME = "testDisplayName";

    @Override
    public void setupClass()
    {
        super.setupClass();

        final String hookScript = System.getProperty(HOOK_SCRIPT);
        final String host = System.getProperty(HOST);

        if (hookScript == null || host == null)
        {
            cleanupClass();
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
    @Test(dependsOnMethods = {"participant1SetDisplayNameAndCheck"})
    public void testJicofoRestart()
    {
        setupListeners();
        startReloadScript(new String[]{"--restart-jicofo"});
        waitForReloadAndTest(true);
    }
    
    /**
     * Executes {@link StopVideoTest#stopVideoOnParticipant1AndCheck()}.
     */
    @Test(dependsOnMethods = { "testProsodyRestart" })
    public void participant1VideoMuteAndCheck()
    {
        MeetUIUtils.muteVideoAndCheck(getParticipant1(), getParticipant2());
    }
    
    /**
     * Executes {@link MuteTest#muteParticipant1AndCheck()}.
     */
    @Test(dependsOnMethods = {"participant1VideoMuteAndCheck"})
    public void participant1AudioMuteAndCheck()
    {
        new MuteTest(this).muteParticipant1AndCheck();
    }
    
    /**
     * Executes {@link DisplayNameTest#checkDisplayNameChange}.
     */
    @Test(dependsOnMethods = {"participant1AudioMuteAndCheck"})
    public void participant1SetDisplayNameAndCheck()
    {
        new DisplayNameTest(this).checkDisplayNameChange(DISPLAY_NAME);
    }
    
    /**
     * Checks video mute status for the local video for participant1 and the
     * remote video for participant2.
     */
    @Test(dependsOnMethods = { "testJicofoRestart" })
    public void participant1CheckVideoMuted()
    {
        getParticipant1().getParticipantsPane().assertIsParticipantVideoMuted(getParticipant1(), true);
        getParticipant2().getParticipantsPane().assertIsParticipantVideoMuted(getParticipant1(), true);
    }
    
    /**
     * Checks audio mute status for the local video for participant1 and the
     * remote video for participant2.
     */
    @Test(dependsOnMethods = {"participant1CheckVideoMuted"})
    public void participant1CheckAudioMuted()
    {
        getParticipant1().getFilmstrip()
            .assertAudioMuteIcon(getParticipant1(), true);
        getParticipant2().getFilmstrip()
            .assertAudioMuteIcon(getParticipant1(), true);
    }
    
    /**
     * Checks the display name of participant1.
     */
    @Test(dependsOnMethods = {"participant1CheckAudioMuted"})
    public void participant1CheckDisplayName()
    {
        DisplayNameTest test = new DisplayNameTest();
        test.doLocalDisplayNameCheck(DISPLAY_NAME);
        test.doRemoteDisplayNameCheck(DISPLAY_NAME);
    }
    
    /**
     * Dismisses Video Bridge not available error dialog for a participant
     * @param driver the participant
     */
    private void dismissBridgeNotAvailableDialog(WebDriver driver)
    {
        WebElement element;

        try
        {
            element = driver.findElement(By.name("jqi_state0_buttonOk"));
        }
        catch (org.openqa.selenium.NoSuchElementException ex)
        {
            element = null;
        }
         
        if (element != null)
        {
            element.click();
        }
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
        {
            return;
        }

        CmdExecutor exec = new CmdExecutor();
        try
        {
            List<String> cmd = new ArrayList<>();
            cmd.add(hookScript);
            cmd.add(host);
            Collections.addAll(cmd, params);

            int result = exec.executeCmd(cmd);

            assertEquals(result, 0, "Script returned non-zero value");
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
     * @param isParticipant1Muted <tt>true</tt> if participant1 is muted, false
     * otherwise.
     */
    private void waitForReloadAndTest(boolean isParticipant1Muted)
    {
        List<WebParticipant> participants
            = Arrays.asList(getParticipant1(), getParticipant2());

        final String checkForConferenceLeftScript = "return "
            + "APP.conference._room.conference_left_event;";

        participants.forEach(
            p -> p.waitForCondition(() -> {
                    Object res
                        = p.executeScript(checkForConferenceLeftScript);

                    return TestUtils.getBooleanResult(res);
                },
                // FIXME 200 seconds ? isn't that too much ?
                200,
                "Script: " + checkForConferenceLeftScript));
        
        print("Reload detected");

        participants.forEach(
            p -> p.executeScript(
                "APP.conference._room.conference_left_event = false;"));
        
        print("Wait for ice connected.");
        participants.forEach(p -> p.waitForIceConnected(60));
        
        print("Wait for send receive data on the side of participant1.");
        getParticipant1().waitForSendReceiveData();

        if (isParticipant1Muted)
        {
            print("Wait for send data on the second "
                + "participant side.");
            WebParticipant participant = getParticipant2();

            participant.waitForSendReceiveData(
                    true /* send */, false /* receive */);
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
