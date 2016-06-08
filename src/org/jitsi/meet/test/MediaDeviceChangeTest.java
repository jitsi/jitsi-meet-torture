/*
 * Copyright @ 2016 Atlassian Pty Ltd
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

import java.util.ArrayList;
import java.util.List;

/**
 * Test how jitsi-meet reacts to discovery of new media devices
 * like microphones and webcams.
 *
 * @author Kostiantyn Tsaregradskyi
 */
public class MediaDeviceChangeTest
    extends TestCase
{
    private MediaDeviceInfo mic1 = new MediaDeviceInfo(
            "audio", "mic1", "", "Mic1");
    private MediaDeviceInfo mic2 = new MediaDeviceInfo(
            "audio", "mic2", "", "Mic2");
    private MediaDeviceInfo cam1 = new MediaDeviceInfo(
            "video", "cam1", "", "Cam1");
    private MediaDeviceInfo cam2 = new MediaDeviceInfo(
            "video", "cam2", "", "Cam2");
    private MediaDeviceInfo output1 = new MediaDeviceInfo(
            "audiooutput", "default", "", "DefaultAudioOutput");
    
    /**
     * Constructs test
     * @param name the method name for the test.
     */
    public MediaDeviceChangeTest(String name)
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

        suite.addTest(new MediaDeviceChangeTest("testMediaDeviceChange"));

        return suite;
    }

    /**
     * The list of devices should be disabled and have single entry "None"
     * if no devices are available
     */
    public void testMediaDeviceChange()
    {
        System.err.println("Start testMediaDeviceChange.");

        WebDriver driver = ConferenceFixture.getOwner();

        storeOriginalEnumerateDevices(driver);
        storeOriginalGetSources(driver);
        storeOriginalCreateLocalTracks(driver);

        try {
            checkEmptyDevices(driver);
            checkDevicesGetSelectedAfterBeingPlugged(driver);
        } finally {
            restoreOriginalEnumerateDevices(driver);
            restoreOriginalGetSources(driver);
            restoreOriginalCreateLocalTracks(driver);
        }
    }

    /**
     * Here we will check case when there are no devices at all.
     * The list of devices should be disabled and have single entry "None"
     * if no devices are available.
     * @param driver the {@code WebDriver}.
     */
    private void checkEmptyDevices(WebDriver driver) {
        stopLocalTracks(driver);

        List<MediaDeviceInfo> devices = new ArrayList<>();

        mockEnumerateDevices(driver, devices);
        mockGetSources(driver, devices);

        MeetUIUtils.clickOnToolbarButton(driver, "toolbar_button_settings");

        TestUtils.waitMillis(6000);

        boolean isChrome = ConferenceFixture.getBrowserType(driver) ==
                ConferenceFixture.BrowserType.chrome;

        WebElement cameraSelect = driver
                .findElement(By.xpath("//select[@id='selectCamera']"));
        WebElement micSelect = driver
                .findElement(By.xpath("//select[@id='selectMic']"));
        WebElement audioOutputSelect = driver
                .findElement(By.xpath("//select[@id='selectAudioOutput']"));
        WebElement muteCameraToolbarButton = driver
                .findElement(By.xpath("//a[@id='toolbar_button_camera']"));
        WebElement muteMicToolbarButton = driver
                .findElement(By.xpath("//a[@id='toolbar_button_mute']"));

        List<WebElement> cameraOptions = driver
                .findElements(By.xpath("//select[@id='selectCamera']/option"));
        List<WebElement> micOptions = driver
                .findElements(By.xpath("//select[@id='selectMic']/option"));
        List<WebElement> audioOutputOptions = driver.findElements(
                    By.xpath("//select[@id='selectAudioOutput']/option"));

        assertFalse(cameraSelect.isEnabled());
        assertFalse(micSelect.isEnabled());
        assertFalse(audioOutputSelect.isEnabled());

        assertEquals(muteCameraToolbarButton.getAttribute("disabled"), "true");
        assertEquals(muteMicToolbarButton.getAttribute("disabled"), "true");

        assertTrue(muteMicToolbarButton.getAttribute("class")
                .contains("icon-mic-disabled"));
        assertTrue(muteCameraToolbarButton.getAttribute("class")
                .contains("icon-camera-disabled"));

        // Browsers different from Chrome do not support 'audiooutput' devices,
        // so <select> is hidden in this case. This might change in future.
        if (isChrome) {
            assertTrue(audioOutputSelect.isDisplayed());
        } else {
            assertFalse(audioOutputSelect.isDisplayed());
        }

        assertEquals(cameraOptions.size(), 1);
        assertEquals(micOptions.size(), 1);
        assertEquals(audioOutputOptions.size(), 1);

        assertEquals(cameraOptions.get(0).getText(), "None");
        assertEquals(micOptions.get(0).getText(), "None");
        assertEquals(audioOutputOptions.get(0).getText(), "None");
    }

    /**
     * Now we check that devices should be selected and tracks created after
     * we plugged in.
     * @param driver the {@code WebDriver}.
     */
    private void checkDevicesGetSelectedAfterBeingPlugged(WebDriver driver) {
        List<MediaDeviceInfo> devices = new ArrayList<>();
        devices.add(mic1);
        devices.add(cam1);
        devices.add(output1);

        resetCreateLocalTracksGlobalVars(driver);
        mockCreateLocalTracks(driver);
        mockEnumerateDevices(driver, devices);
        mockGetSources(driver, devices);

        TestUtils.waitMillis(6000);

        WebElement cameraSelect = driver
                .findElement(By.xpath("//select[@id='selectCamera']"));
        WebElement micSelect = driver
                .findElement(By.xpath("//select[@id='selectMic']"));
        WebElement audioOutputSelect = driver
                .findElement(By.xpath("//select[@id='selectAudioOutput']"));
        WebElement muteCameraToolbarButton = driver
                .findElement(By.xpath("//a[@id='toolbar_button_camera']"));
        WebElement muteMicToolbarButton = driver
                .findElement(By.xpath("//a[@id='toolbar_button_mute']"));

        List<WebElement> cameraOptions = driver
                .findElements(By.xpath("//select[@id='selectCamera']/option"));
        List<WebElement> micOptions = driver
                .findElements(By.xpath("//select[@id='selectMic']/option"));
        List<WebElement> audioOutputOptions = driver.findElements(
                By.xpath("//select[@id='selectAudioOutput']/option"));

        assertTrue(getCreateLocalTracksWasCalled(driver) > 0);
        assertEquals(getCameraIdFromCreateLocalTracksCall(driver, 0),
                cam1.deviceId);
        assertEquals(getMicIdFromCreateLocalTracksCall(driver, 0),
                mic1.deviceId);

        assertTrue(cameraSelect.isEnabled());
        assertTrue(micSelect.isEnabled());
        assertTrue(audioOutputSelect.isEnabled());

        assertEquals(muteCameraToolbarButton.getAttribute("disabled"), null);
        assertEquals(muteMicToolbarButton.getAttribute("disabled"), null);

        assertFalse(muteMicToolbarButton.getAttribute("class")
                .contains("icon-mic-disabled"));
        assertFalse(muteCameraToolbarButton.getAttribute("class")
                .contains("icon-camera-disabled"));

        assertEquals(cameraOptions.size(), 1);
        assertEquals(micOptions.size(), 1);
        assertEquals(audioOutputOptions.size(), 1);

        assertEquals(cameraOptions.get(0).getText(), cam1.label);
        assertEquals(micOptions.get(0).getText(), mic1.label);
        assertEquals(audioOutputOptions.get(0).getText(), output1.label);
    }

    /**
     * Mocks navigator.mediaDevices.enumerateDevices() method
     */
    private void mockEnumerateDevices(WebDriver driver,
                                      List<MediaDeviceInfo> devices) {
        String[] devicesAsStrings = new String[devices.size()];

        for (int i = 0; i < devices.size(); i++) {
            devicesAsStrings[i] = devices.get(i).toJsonString(false);
        }

        ((JavascriptExecutor) driver).executeScript(
            "if (navigator.mediaDevices && navigator.mediaDevices.enumerateDevices) {" +
            "    navigator.mediaDevices.enumerateDevices = function() {" +
            "        return Promise.resolve(["+ String.join(",", (CharSequence[]) devicesAsStrings) + "]);" +
            "    };" +
            "}"
        );
    }

    /**
     * Mocks MediaStreamTrack.getSources() method
     */
    private void mockGetSources(WebDriver driver, List<MediaDeviceInfo> devices) {
        String[] devicesAsStrings = new String[devices.size()];

        for (int i = 0; i < devices.size(); i++) {
            devicesAsStrings[i] = devices.get(i).toJsonString(true);
        }

        ((JavascriptExecutor) driver).executeScript(
            "if (MediaStreamTrack && MediaStreamTrack.getSources) {" +
            "    MediaStreamTrack.getSources = function(callback) {" +
            "        callback(["+ String.join(",", (CharSequence[]) devicesAsStrings) + "]);" +
            "    };" +
            "}"
        );
    }

    private void stopLocalTracks(WebDriver driver) {
        ((JavascriptExecutor) driver).executeScript(
            "APP.conference.useVideoStream(null);" +
            "APP.conference.useAudioStream(null);"
        );
    }

    private long getCreateLocalTracksWasCalled(WebDriver driver) {
        return (long)((JavascriptExecutor) driver).executeScript(
            "return window._createLocalTracksCalled;"
        );
    }

    private String getMicIdFromCreateLocalTracksCall(WebDriver driver, int call) {
        return (String)((JavascriptExecutor) driver).executeScript(
            "return window._createLocalTracksCallOptions[" + call + "].micDeviceId;"
        );
    }

    private String getCameraIdFromCreateLocalTracksCall(WebDriver driver, int call) {
        return (String)((JavascriptExecutor) driver).executeScript(
            "return window._createLocalTracksCallOptions[" + call + "].cameraDeviceId;"
        );
    }

    private void mockCreateLocalTracks(WebDriver driver) {
        ((JavascriptExecutor) driver).executeScript(
            "window._createLocalTracksCallOptions = [];" +
            "window._createLocalTracksCalled = 0;" +
            "JitsiMeetJS.createLocalTracks = function(options) {" +
            "    window._createLocalTracksCallOptions.push(JSON.parse(JSON.stringify(options)));" +
            "    window._createLocalTracksCalled++;" +
            "    return window._origCreateLocalTracks(options);" +
            "}"
        );
    }

    private void resetCreateLocalTracksGlobalVars(WebDriver driver) {
        ((JavascriptExecutor) driver).executeScript(
            "window._createLocalTracksCalled = 0;" +
            "window._createLocalTracksCallOptions = [];"
        );
    }

    private void restoreOriginalCreateLocalTracks(WebDriver driver) {
        ((JavascriptExecutor) driver).executeScript(
            "JitsiMeetJS.createLocalTracks = window._origCreateLocalTracks;" +
            "delete window._createLocalTracksCalled;" +
            "delete window._createLocalTracksCallOptions;"
        );
    }

    /**
     * Stores initial navigator.mediaDevices.enumerateDevices() method
     */
    private void storeOriginalEnumerateDevices(WebDriver driver) {
        ((JavascriptExecutor) driver).executeScript(
            "if (!window._origEnumerateDevices && navigator.mediaDevices && navigator.mediaDevices.enumerateDevices) {" +
            "    window._origEnumerateDevices = navigator.mediaDevices.enumerateDevices;" +
            "}"
        );
    }

    /**
     * Stores initial MediaStreamTrack.getSources() method
     */
    private void storeOriginalGetSources(WebDriver driver) {
        ((JavascriptExecutor) driver).executeScript(
            "if (!window._origGetSources && MediaStreamTrack && MediaStreamTrack.getSources) {" +
            "    window._origGetSources = MediaStreamTrack.getSources;" +
            "}"
        );
    }

    /**
     * Stores initial JitsiMeetJS.createLocalTracks() method
     */
    private void storeOriginalCreateLocalTracks(WebDriver driver) {
        ((JavascriptExecutor) driver).executeScript(
            "window._origCreateLocalTracks = JitsiMeetJS.createLocalTracks;"
        );
    }

    /**
     * Restores initial navigator.mediaDevices.enumerateDevices() method
     */
    private void restoreOriginalEnumerateDevices(WebDriver driver) {
        ((JavascriptExecutor) driver).executeScript(
            "if (window._origEnumerateDevices && navigator.mediaDevices && navigator.mediaDevices.enumerateDevices) {" +
            "    navigator.mediaDevices.enumerateDevices = window._origEnumerateDevices;" +
            "    delete window._origEnumerateDevices;" +
            "}"
        );
    }

    /**
     * Restores initial MediaStreamTrack.getSources() method
     */
    private void restoreOriginalGetSources(WebDriver driver) {
        ((JavascriptExecutor) driver).executeScript(
            "if (window._origGetSources && MediaStreamTrack && MediaStreamTrack.getSources) {" +
            "    MediaStreamTrack.getSources = window._origGetSources;" +
            "    delete window._origGetSources;" +
            "}"
        );
    }

    private class MediaDeviceInfo {
        private String kind;
        private String deviceId;
        private String groupId;
        private String label;

        MediaDeviceInfo(
                String kind, String deviceId, String groupId, String label) {
            this.kind = kind;
            this.deviceId = deviceId;
            this.groupId = groupId;
            this.label = label;
        }

        String toJsonString(Boolean isGetSourcesFormat) {
            return "{" +
                        "\"kind\":\"" + (this.kind.equals("audiooutput")
                                ? this.kind
                                : (isGetSourcesFormat
                                    ? this.kind
                                    : this.kind + "input")) + "\"," +
                        "\"deviceId\":\"" + this.deviceId + "\"," +
                        "\"groupId\":\"" + this.groupId + "\"," +
                        "\"label\":\"" + this.label + "\"," +
                    "}";
        }
    }
}
