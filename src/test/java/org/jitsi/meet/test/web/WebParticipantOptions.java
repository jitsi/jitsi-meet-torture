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

import java.net.*;
import java.util.*;

/**
 * Holds web specific options.
 */
public class WebParticipantOptions
    extends ParticipantOptions
{
    /**
     * The default value for {@link #PROP_CHROME_DISABLE_SANDBOX}.
     */
    private static final String DEFAULT_CHROME_DISABLE_SANDBOX
        = Boolean.TRUE.toString();

    /**
     * The default address used to connect remote selenium drivers.
     * ...wait it's the localhost ?
     */
    private static final String DEFAULT_REMOTE_ADDRESS_NAME
        = "http://localhost:4444/wd/hub";

    /**
     * Whether to allow insecure certs, by default is false.
     */
    private static final String PROP_ALLOW_INSECURE_CERTS = "allowInsecureCerts";

    /**
     * This parameter gets a string 'MAP somedomain.com ip-address'.
     * These are custom host resolver rules passed to chrome to be able to use an ip address of a particular shard
     * but still using the domain name.
     */
    private static final String PROP_HOST_RESOLVER_RULES = "hostResolverRules";

    private static final String PROP_BINARY = "binary";

    private static final String PROP_APP_NAME = "applicationName";

    /**
     * The name of the system property that holds the path to the packet
     * delivery trace file to use for the downlink direction in the mahimahi
     * shell.
     */
    private static final String PROP_DOWNLINK = "downlink";

    private static final String PROP_FAKE_AUDIO = "fakeStreamAudioFile";

    private static final String PROP_FAKE_VIDEO = "fakeStreamVideoFile";

    /**
     * The name of the property that holds the user data/profile directory to
     * use for the launched browser instance.
     */
    private static final String PROP_PROFILE_DIR = "profiledir";

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
     *
     * XXX There is a typo in 'nosanbox', but it was like that before
     * the property was moved from WebParticipantFactory (not really sure...).
     */
    @Deprecated
    private static final String PROP_DISABLE_NOSANBOX
        = "chrome.disable.nosanbox";

    /**
     * The property to disable Chrome sandbox through the 'no-sandbox'
     * argument passed to the driver.
     */
    private static final String PROP_CHROME_DISABLE_SANDBOX
        = "chrome.disable.sandbox";

    /**
     * The property to enable headless parameter.
     */
    private static final String PROP_ENABLE_HEADLESS = "enable.headless";

    /**
     * The property to enable headless parameter for.
     * To be removed.
     */
    private static final String PROP_ENABLE_HEADLESS_LEGACY = "chrome.enable.headless";

    /**
     * The name of the system property that holds the path to the packet
     * delivery trace file to use for the uplink direction in the mahimahi
     * shell.
     */
    private static final String PROP_UPLINK = "uplink";

    private static final String PROP_VERSION = "version";

    /**
     * By default WebTestBase will set a display name, this property can skip this step.
     */
    private static final String PROP_SKIP_DISPLAYNAME = "skip-displayname";

    /**
     * Whether the Participant is using a the load test URL (and thus certain operations
     * can be skipped).
     */
    private static final String PROP_LOADTEST = "isLoadTest";

    /**
     * Get web specific global property names. See
     * {@link ParticipantFactory#moveSystemGlobalProperties()} for more info.
     *
     */
    public static List<String> getSystemGlobalPropNames()
    {
        List<String> globalKeys = new LinkedList<>();

        globalKeys.add(PROP_ALLOW_INSECURE_CERTS);
        globalKeys.add(PROP_DISABLE_NOSANBOX);
        globalKeys.add(PROP_ENABLE_HEADLESS);
        globalKeys.add(PROP_ENABLE_HEADLESS_LEGACY);
        globalKeys.add(PROP_HOST_RESOLVER_RULES);
        globalKeys.add(PROP_REMOTE_ADDRESS_NAME);
        globalKeys.add(PROP_REMOTE_RESOURCE_PARENT_PATH_NAME);

        return globalKeys;
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
        defaults.setProperty(
                PROP_CHROME_DISABLE_SANDBOX, DEFAULT_CHROME_DISABLE_SANDBOX);
        defaults.setProperty(PROP_SKIP_DISPLAYNAME, Boolean.FALSE.toString());
        defaults.setProperty(PROP_ALLOW_INSECURE_CERTS, Boolean.FALSE.toString());
        defaults.setProperty(PROP_LOADTEST, Boolean.FALSE.toString());

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
     * The getter for {@link #PROP_ALLOW_INSECURE_CERTS}.
     *
     * @return <tt>true</tt> or <tt>false</tt>.
     */
    public boolean allowsInsecureCerts()
    {
        return getBooleanProperty(PROP_ALLOW_INSECURE_CERTS);
    }

    /**
     * The getter for {@link #PROP_HOST_RESOLVER_RULES}.
     *
     * @return the rules as string.
     */
    public String hostResolverRules()
    {
        return getProperty(PROP_HOST_RESOLVER_RULES);
    }

    /**
     * The getter for {@link #PROP_CHROME_DISABLE_SANDBOX}.
     *
     * @return <tt>true</tt> or <tt>false</tt>.
     */
    public boolean isChromeSandboxDisabled()
    {
        // Go with the legacy option first
        if (getProperty(PROP_DISABLE_NOSANBOX) != null)
        {
            return !getBooleanProperty(PROP_DISABLE_NOSANBOX);
        }
        else
        {
            return getBooleanProperty(PROP_CHROME_DISABLE_SANDBOX);
        }
    }

    /**
     * The getter for {@link #PROP_ENABLE_HEADLESS}.
     *
     * @return <tt>true</tt> or <tt>false</tt>.
     */
    public boolean isHeadless()
    {
        return getBooleanProperty(PROP_ENABLE_HEADLESS) || getBooleanProperty(PROP_ENABLE_HEADLESS_LEGACY);
    }

    /**
     * Sets whether to use headless mode.
     * @param value new value.
     * @return a reference to this object.
     */
    public WebParticipantOptions setHeadless(boolean value)
    {
        setProperty(PROP_ENABLE_HEADLESS, String.valueOf(value));

        return this;
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
     * Sets the path to the packet delivery trace file to use for the uplink
     * direction in the mahimahi shell.
     */
    public WebParticipantOptions setUplink(String uplink)
    {
        setProperty(PROP_UPLINK, uplink);
        return this;
    }

    /**
     * Sets the path to the packet delivery trace file to use for the downlink
     * direction in the mahimahi shell.
     */
    public WebParticipantOptions setDownlink(String downlink)
    {
        setProperty(PROP_DOWNLINK, downlink);
        return this;
    }

    /**
     * Sets the user data/profile directory to use for the launched browser
     * instance.
     */
    public WebParticipantOptions setProfileDirectory(String profileDirectory)
    {
        setProperty(PROP_PROFILE_DIR, profileDirectory);
        return this;
    }

    /**
     * Sets whether we need to skip setting display name for this participant by default in web base tests.
     */
    public WebParticipantOptions setSkipDisplayNameSet(boolean value)
    {
        setProperty(PROP_SKIP_DISPLAYNAME, Boolean.toString(value));
        return this;
    }

    /**
     * Sets whether this participant is using the load test interface
     */
    public WebParticipantOptions setLoadTest(boolean value)
    {
        setProperty(PROP_LOADTEST, Boolean.toString(value));
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
     * Sets the app name capability that the node must have.
     *
     * @param applicationName the app name capability that the node must have.
     */
    public WebParticipantOptions setApplicationName(String applicationName)
    {
        setProperty(PROP_APP_NAME, applicationName);
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
     * Sets the binary to use when starting the driver.
     */
    public WebParticipantOptions setBinary(String binary)
    {
        setProperty(PROP_BINARY, binary);
        return this;
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
     * Whether to skip default display name set.
     * @return Whether to skip default display name set
     */
    public boolean getSkipDisplayNameSet()
    {
        return Boolean.valueOf(getProperty(PROP_SKIP_DISPLAYNAME));
    }

    /**
     * Whether this is a load test URL
     * @return Whether this is a load test URL.
     */
    public boolean getLoadTest()
    {
        return Boolean.parseBoolean(getProperty(PROP_LOADTEST));
    }

    /**
     * Gets the path to the packet delivery trace file to use for the uplink
     * direction in the mahimahi shell.
     */
    public String getUplink()
    {
        return getProperty(PROP_UPLINK);
    }

    /**
     * Gets the path to the packet delivery trace file to use for the downlink
     * direction in the mahimahi shell.
     */
    public String getDownlink()
    {
        return getProperty(PROP_DOWNLINK);
    }

    /**
     * Gets the user data/profile directory to use for the launched browser
     * instance.
     */
    public String getProfileDirectory()
    {
        return getProperty(PROP_PROFILE_DIR);
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
     * @return the app name capability that the node must have.
     */
    public String getApplicationName()
    {
        return getProperty(PROP_APP_NAME);
    }
}
