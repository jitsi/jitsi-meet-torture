package org.jitsi.meet.test.web;

import org.apache.commons.lang3.*;
import org.jitsi.meet.test.base.*;

import java.util.*;

public class WebParticipantOptions
    extends ParticipantOptions
{
    private static final String FAKE_AUDIO_PROP = "fakeStreamAudioFile";
    private static final String FAKE_VIDEO_PROP = "fakeStreamVideoFile";
    private static final String REMOTE_PROP = "isRemote";
    private static final String BINARY_PROP = "binary";
    private static final String VERSION_PROP = "version";

    private static final String GLOBAL_PROP_PREFIX = "jitsi-meet";

    public WebParticipantOptions()
    {
        this.initDefaults();
    }

    private void initDefaults()
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
     */
    public WebParticipantOptions setFakeStreamVideoFile(
        String fakeStreamVideoFile)
    {
        this.setProperty(FAKE_VIDEO_PROP, fakeStreamVideoFile);

        return this;
    }

    public String getVersion()
    {
        return (String)this.getProperty(VERSION_PROP);
    }

    public String getBinary()
    {
        return (String)this.getProperty(BINARY_PROP);
    }

    public boolean isRemote()
    {
        return this.getBooleanProperty(REMOTE_PROP);
    }

    public String getFakeStreamAudioFile()
    {
        return (String)this.getProperty(FAKE_AUDIO_PROP);
    }

    public String getFakeStreamVideoFile()
    {
        return (String)this.getProperty(FAKE_VIDEO_PROP);
    }


}
