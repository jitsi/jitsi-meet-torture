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

import org.apache.commons.lang3.*;
import org.jitsi.meet.test.base.*;

import java.net.*;
import java.util.*;

/**
 * Holds web specific options.
 */
public class WebParticipantOptions
    extends ParticipantOptions
{
    /**
     * The default address used to connect remote selenium drivers.
     * ...wait it's the localhost ?
     */
    private static final String DEFAULT_REMOTE_ADDRESS_NAME
        = "http://localhost:4444/wd/hub";
    /**
     * A prefix for global options (not per participant).
     */
    static final String GLOBAL_PROP_PREFIX = "jitsi-meet";

    private static final String PROP_BINARY = "binary";

    /**
     * The id of the chrome extension that will be loaded
     * on opening participant driver.
     */
    private static final String PROP_CHROME_EXTENSION_ID = "chromeExtensionID";

    private static final String PROP_FAKE_AUDIO = "fakeStreamAudioFile";

    private static final String PROP_FAKE_VIDEO = "fakeStreamVideoFile";

    /**
     * The property to change remote selenium grid URL, defaults to
     * {@link #DEFAULT_REMOTE_ADDRESS_NAME} if requiring remote browser and
     * property is not set.
     */
    private static final String PROP_REMOTE_ADDRESS_NAME = "remote.address";

    private static final String PROP_REMOTE = "isRemote";

    /**
     * The property to evaluate for parent path of the resources used when
     * loading chrome remotely like audio/video files.
     */
    private static final String PROP_REMOTE_RESOURCE_PARENT_PATH_NAME
        = "remote.resource.path";

    /**
     * The property to disable no-sandbox parameter for chrome.
     */
    private static final String PROP_DISABLE_NOSANBOX
        = "chrome.disable.nosanbox";

    /**
     * The property to enable headless parameter for chrome.
     */
    private static final String PROP_ENABLE_HEADLESS
        = "chrome.enable.headless";

    private static final String PROP_VERSION = "version";

    /**
     * This will move all global properties which used to be specified without
     * the {@link #GLOBAL_PROP_PREFIX} under that prefix. This will allow to
     * simplify the code which has to deal with global properties.
     *
     * @param properties - The config instance which will be modified in place.
     *
     * @return the same {@link Properties} instance which was passed as
     * an argument.
     */
    static Properties moveLegacyGlobalProperties(Properties properties)
    {
        moveLegacyGlobalProperty(properties, PROP_DISABLE_NOSANBOX);
        moveLegacyGlobalProperty(properties, PROP_ENABLE_HEADLESS);
        moveLegacyGlobalProperty(
            properties, PROP_REMOTE_RESOURCE_PARENT_PATH_NAME);

        return properties;
    }

    /**
     * A subroutine for {@link #moveLegacyGlobalProperties(Properties)}. Move
     * specified property over to the key with added
     * {@link #GLOBAL_PROP_PREFIX} and deletes the old key.
     *
     * @param props - The {@link Properties} which will be modified in place.
     * @param key - The global property key which is to be moved under
     * the {@link #GLOBAL_PROP_PREFIX}.
     */
    private static void moveLegacyGlobalProperty(Properties props, String key)
    {
        String value = props.getProperty(key);

        if (StringUtils.isNotBlank(value))
        {
            props.setProperty(
                GLOBAL_PROP_PREFIX + "." + key,
                value);
        }

        // Clear the global one
        props.remove(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Properties initDefaults()
    {
        Properties defaults = super.initDefaults();

        defaults.setProperty(
                PROP_FAKE_AUDIO, "resources/fakeAudioStream.wav");
        defaults.setProperty(
                PROP_REMOTE_ADDRESS_NAME, DEFAULT_REMOTE_ADDRESS_NAME);

        return defaults;
    }

    /**
     * A getter for {@link #PROP_REMOTE_RESOURCE_PARENT_PATH_NAME}.
     * @return a String or <tt>null</tt>.
     */
    public String getRemoteResourcePath()
    {
        return getProperty(PROP_REMOTE_RESOURCE_PARENT_PATH_NAME);
    }

    /**
     * Returns the remote driver address.
     *
     * FIXME: it's very similar to Mobile's Appium URL.
     *
     * @return the remote driver address.
     */
    public URL getRemoteDriverAddress()
    {
        String remoteAddress = getProperty(PROP_REMOTE_ADDRESS_NAME);

        try
        {
            return new URL(remoteAddress);
        }
        catch (MalformedURLException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * The getter for {@link #PROP_DISABLE_NOSANBOX}.
     *
     * @return <tt>true</tt> or <tt>false</tt>.
     */
    public boolean isDisableNoSandbox()
    {
        return getBooleanProperty(PROP_DISABLE_NOSANBOX);
    }

    /**
     * The getter for {@link #PROP_ENABLE_HEADLESS}.
     *
     * @return <tt>true</tt> or <tt>false</tt>.
     */
    public boolean isEnabledHeadless()
    {
        return getBooleanProperty(PROP_ENABLE_HEADLESS);
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
        setProperty(PROP_FAKE_AUDIO, fakeStreamAudioFile);

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
        setProperty(PROP_FAKE_VIDEO, fakeStreamVideoFile);

        return this;
    }

    /**
     * Sets the chrome extension id to be used.
     * @param extensionId the chrome extension id to be used.
     * @return a reference to this object.
     */
    public WebParticipantOptions setChromeExtensionId(String extensionId)
    {
        setProperty(PROP_CHROME_EXTENSION_ID, extensionId);

        return this;
    }

    /**
     * Returns version for the driver, if available.
     * @return version for the driver, if available.
     */
    public String getVersion()
    {
        return getProperty(PROP_VERSION);
    }

    /**
     * The binary to use when starting the driver.
     * @return the binary to use when starting the driver.
     */
    public String getBinary()
    {
        return getProperty(PROP_BINARY);
    }

    /**
     * Returns is the driver remote.
     * @return is the driver remote.
     */
    public boolean isRemote()
    {
        return getBooleanProperty(PROP_REMOTE);
    }

    /**
     * The file location of wav audio file which will be streamed through fake
     * audio device by participants.
     * @return The file location of the fake audio file.
     */
    public String getFakeStreamAudioFile()
    {
        return getProperty(PROP_FAKE_AUDIO);
    }

    /**
     * The file location of y4m video file which will be streamed through fake
     * video device by participants.
     * @return The file location of the fake video file.
     */
    public String getFakeStreamVideoFile()
    {
        return getProperty(PROP_FAKE_VIDEO);
    }

    /**
     * Returns the chrome extension id option.
     * @return the chrome extension id option.
     */
    public String getChromeExtensionId()
    {
        return getProperty(PROP_CHROME_EXTENSION_ID);
    }
}
