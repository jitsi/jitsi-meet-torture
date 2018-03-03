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

import java.util.*;

/**
 * Holds web specific options.
 */
public class WebParticipantOptions
    extends ParticipantOptions
{
    private static final String FAKE_AUDIO_PROP = "fakeStreamAudioFile";
    private static final String FAKE_VIDEO_PROP = "fakeStreamVideoFile";
    private static final String REMOTE_PROP = "isRemote";
    private static final String BINARY_PROP = "binary";
    private static final String VERSION_PROP = "version";

    /**
     * The id of the chrome extension that will be loaded
     * on opening participant driver.
     */
    private static final String CHROME_EXTENSION_ID = "chromeExtensionID";

    /**
     * A prefix for global options (not per participant).
     */
    private static final String GLOBAL_PROP_PREFIX = "jitsi-meet";

    /**
     * Constructs new WebParticipantOptions, filling it with default values.
     */
    public WebParticipantOptions()
    {
        this.setProperty(FAKE_AUDIO_PROP, "resources/fakeAudioStream.wav");
    }

    @Override
    public void load(
        Properties config,
        String configPrefix,
        ParticipantOptions overrides)
    {
        this.loadConfigProperty(config, GLOBAL_PROP_PREFIX, FAKE_AUDIO_PROP);
        this.loadConfigProperty(config, GLOBAL_PROP_PREFIX, FAKE_VIDEO_PROP);

        this.loadConfigProperty(config, configPrefix, REMOTE_PROP);
        this.loadConfigProperty(config, configPrefix, BINARY_PROP);
        this.loadConfigProperty(config, configPrefix, VERSION_PROP);

        super.load(config, configPrefix, overrides);
    }

    /**
     * Sets the name of wav audio file which will be streamed through fake audio
     * device by participants. The file location is relative to working folder.
     * For remote drivers a parent folder can be set and the file will be
     * searched in there.
     *
     * @param fakeStreamAudioFile full name of wav file for the fake audio
     *                            device.
     * @return a reference to this object.
     */
    public WebParticipantOptions setFakeStreamAudioFile(
        String fakeStreamAudioFile)
    {
        this.setProperty(FAKE_AUDIO_PROP, fakeStreamAudioFile);

        return this;
    }

    /**
     * Sets the name of y4m video file which will be streamed through fake video
     * device by participants. The file location is relative to working folder.
     * For remote drivers a parent folder can be set and the file will be
     * searched in there.
     *
     * @param fakeStreamVideoFile full name of y4m file for the fake video
     *                            device.
     * @return a reference to this object.
     */
    public WebParticipantOptions setFakeStreamVideoFile(
        String fakeStreamVideoFile)
    {
        this.setProperty(FAKE_VIDEO_PROP, fakeStreamVideoFile);

        return this;
    }

    /**
     * Sets the chrome extension id to be used.
     * @param extensionId the chrome extension id to be used.
     * @return a reference to this object.
     */
    public WebParticipantOptions setChromeExtensionId(String extensionId)
    {
        this.setProperty(CHROME_EXTENSION_ID, extensionId);

        return this;
    }

    /**
     * Returns version for the driver, if available.
     * @return version for the driver, if available.
     */
    public String getVersion()
    {
        return (String)this.getProperty(VERSION_PROP);
    }

    /**
     * The binary to use when starting the driver.
     * @return the binary to use when starting the driver.
     */
    public String getBinary()
    {
        return (String)this.getProperty(BINARY_PROP);
    }

    /**
     * Returns is the driver remote.
     * @return is the driver remote.
     */
    public boolean isRemote()
    {
        return this.getBooleanProperty(REMOTE_PROP);
    }

    /**
     * The file location of wav audio file which will be streamed through fake
     * audio device by participants.
     * @return The file location of the fake audio file.
     */
    public String getFakeStreamAudioFile()
    {
        return (String)this.getProperty(FAKE_AUDIO_PROP);
    }

    /**
     * The file location of y4m video file which will be streamed through fake
     * video device by participants.
     * @return The file location of the fake video file.
     */
    public String getFakeStreamVideoFile()
    {
        return (String)this.getProperty(FAKE_VIDEO_PROP);
    }

    /**
     * Returns the chrome extension id option.
     * @return the chrome extension id option.
     */
    public String getChromeExtensionId()
    {
        return (String)this.getProperty(CHROME_EXTENSION_ID);
    }
}
