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

import junit.framework.*;

import org.jitsi.meet.test.util.*;

import org.openqa.selenium.*;

import java.util.*;

import org.openqa.selenium.support.ui.*;

/**
 * Launches a hook script that will restart prosody and jicofo and checks
 * if the client reloads the conference and if the conference is working after
 * the reload. 
 */
public class ReloadTest
    extends TestCase
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

    /**
     * Constructs test
     * @param name the method name for the test.
     */
    public ReloadTest(String name)
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
        
        final String hookScript = System.getProperty(HOOK_SCRIPT);
        final String host = System.getProperty(HOST);

        if(hookScript == null || host == null)
            return suite;
        
        suite.addTest(new ReloadTest("restartParticipants"));
        suite.addTest(new ReloadTest("testProsodyRestart"));
        suite.addTest(new ReloadTest("ownerVideoMuteAndCheck"));
        suite.addTest(new ReloadTest("ownerAudioMuteAndCheck"));
        suite.addTest(new ReloadTest("ownerSetDisplayNameAndCheck"));
        suite.addTest(new ReloadTest("testJicofoRestart"));
        suite.addTest(new ReloadTest("ownerCheckVideoMuted"));
        suite.addTest(new ReloadTest("ownerCheckAudioMuted"));
        suite.addTest(new ReloadTest("ownerCheckDisplayName"));

        return suite;
    }

    /**
     * Restarts the two participants so we clear states of previous tests.
     */
    public void restartParticipants()
    {
        ConferenceFixture.restartParticipants();

        MeetUtils.waitForSendReceiveData(
            ConferenceFixture.getOwner());
        MeetUtils.waitForSendReceiveData(
            ConferenceFixture.getSecondParticipant());
    }

    /**
     * Restarts prosody, verifies that all participants have been reloaded,
     * checks that all participant have joined successfully the conference 
     * and audio/video data is transmitted. Also dismisses Video Bridge not 
     * available error dialog (which is shown because of prosody restart)
     */
    public void testProsodyRestart()
    {
        System.err.println("Start testProsodyRestart.");
        setupListeners();
        startReloadScript(new String[]{"--restart-prosody"});
        waitForReloadAndTest();
        dismissBridgeNotAvailableDialog(ConferenceFixture.getOwner());
        dismissBridgeNotAvailableDialog(ConferenceFixture.getSecondParticipant());
    }
    
    /**
     * Restarts jicofo, verifies that all participants have been reloaded,
     * checks that all participant have joined successfully the conference 
     * and audio/video data is transmitted.
     */
    public void testJicofoRestart()
    {
        System.err.println("Start testJicofoRestart.");
        setupListeners();
        startReloadScript(new String[]{"--restart-jicofo"});
        waitForReloadAndTest(true);
    }
    
    /**
     * Executes {@link StopVideoTest#stopVideoOnOwnerAndCheck()}.
     */
    public void ownerVideoMuteAndCheck()
    {
        new StopVideoTest("stopVideoOnOwnerAndCheck")
            .stopVideoOnOwnerAndCheck();
    }
    
    /**
     * Executes {@link MuteTest#muteOwnerAndCheck()}.
     */
    public void ownerAudioMuteAndCheck()
    {
        new MuteTest("muteOwnerAndCheck").muteOwnerAndCheck();
    }
    
    /**
     * Executes {@link DisplayNameTest#checkDisplayNameChange()}.
     */
    public void ownerSetDisplayNameAndCheck()
    {
        new DisplayNameTest().checkDisplayNameChange(DISPLAY_NAME);
    }
    
    /**
     * Checks video mute status for the local video for the owner and 
     * the remote video for second participant.
     */
    public void ownerCheckVideoMuted()
    {
        System.err.println("Start ownerCheckVideoMuted.");
        MeetUIUtils.assertMuteIconIsDisplayed(
            ConferenceFixture.getOwner(),
            ConferenceFixture.getOwner(),
            true,
            true,
            "owner");
        MeetUIUtils.assertMuteIconIsDisplayed(
            ConferenceFixture.getSecondParticipant(),
            ConferenceFixture.getOwner(),
            true,
            true,
            "owner");
    }
    
    /**
     * Checks audio mute status for the local video for the owner and 
     * the remote video for second participant.
     */
    public void ownerCheckAudioMuted()
    {
        System.err.println("Start ownerCheckAudioMuted.");
        MeetUIUtils.assertMuteIconIsDisplayed(
            ConferenceFixture.getOwner(),
            ConferenceFixture.getOwner(),
            true,
            false,
            "owner");
        MeetUIUtils.assertMuteIconIsDisplayed(
            ConferenceFixture.getSecondParticipant(),
            ConferenceFixture.getOwner(),
            true,
            false,
            "owner");
    }
    
    /**
     * Checks the display name of the owner.
     */
    public void ownerCheckDisplayName()
    {
        System.err.println("Start ownerCheckDisplayName.");
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
        System.err.println("Start dismissBridgeNotAvailableDialog.");
        WebElement element;

        try {
            element = participant.findElement(By.name("jqi_state0_buttonOk"));
        } catch (org.openqa.selenium.NoSuchElementException ex) {
            element = null;
        }
         
        if(element != null)
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

        if(hookScript == null || host == null)
            return;

        CmdExecutor exec = new CmdExecutor();
        try
        {
            List<String> cmd = new ArrayList<>();
            cmd.add(hookScript);
            cmd.add(host);
            Collections.addAll(cmd, params);

            int result = exec.executeCmd(cmd);

            assertEquals("Script returned non-zero value", 0, result);
        }
        catch (Exception hookException)
        {
            assertFalse("Error executing hook script:"
                + hookException.getMessage(), true);
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
        WebDriver[] drivers = {ConferenceFixture.getOwner(), 
            ConferenceFixture.getSecondParticipant()};
        String script
            = "APP.conference._room.addEventListener("
                + "        JitsiMeetJS.events.conference.CONFERENCE_LEFT,"
                + "        function (o) {"
                + "            APP.conference._room.conference_left_event = true;"
                + "        });"
                + "";
        for(int i = 0; i < drivers.length; i++) 
        {
            TestUtils.executeScript(drivers[i], script);
        }
    }

    /**
     * Detects reload, waits for ice connected state event and verifies that 
     * audio/video data is transmitted. 
     * @param isOwnerMuted <tt>true</tt> if owner is muted, false otherwise.
     */
    private void waitForReloadAndTest(boolean isOwnerMuted)
    {
        System.err.println("Start waitForReloadAndTest.");
        WebDriver[] drivers = {ConferenceFixture.getOwner(), 
            ConferenceFixture.getSecondParticipant()};
        final String checkForConferenceLeftScript = "return "
            + "APP.conference._room.conference_left_event;";
        for(int i = 0; i < drivers.length; i++) 
        {
            (new WebDriverWait(drivers[i], 200))
                .until(new ExpectedCondition<Boolean>()
                {
                    public Boolean apply(WebDriver d)
                    {
                        Object res = 
                            ((JavascriptExecutor) d)
                            .executeScript(checkForConferenceLeftScript);
                        return res != null && res.equals(Boolean.TRUE);
                    }
                });
        }
        
        System.err.println("Reload detected");
        
        for(int i = 0; i < drivers.length; i++) 
        {
            TestUtils.executeScript(drivers[i], 
                "APP.conference._room.conference_left_event = false;");
        }
        
        System.err.println("Wait for ice connected.");
        for(int i = 0; i < drivers.length; i++) 
        {
            MeetUtils.waitForIceConnected(drivers[i], 60);
        }
        
        System.err.println("Wait for send receive data on the owner side.");
        MeetUtils.waitForSendReceiveData(ConferenceFixture.getOwner());
        
        if(isOwnerMuted)
        {
            System.err.println("Wait for send data on the second "
                + "participant side.");
            final WebDriver participant 
                = ConferenceFixture.getSecondParticipant();
            new WebDriverWait(participant, 15)
            .until(new ExpectedCondition<Boolean>()
            {
                public Boolean apply(WebDriver d)
                {
                    Map stats = (Map) ((JavascriptExecutor) participant)
                            .executeScript("return APP.conference.getStats();");

                    Map<String, Long> bitrate =
                            (Map<String, Long>) stats.get("bitrate");

                    if (bitrate != null)
                    {
                        long upload = bitrate.get("upload");
                        if (upload > 0)
                            return true;
                    }

                    return false;
                }
            });
        }
        else 
        {
            System.err.println("Wait for send receive data on the second "
                + "participant side.");
            MeetUtils.waitForSendReceiveData(ConferenceFixture.getSecondParticipant());
        }
        
        System.err.println("Reload finished.");
    }
}
