package org.jitsi.meet.test.web;

import org.jitsi.meet.test.base.*;

import java.util.*;

public class WebParticipantOptions
    extends ParticipantOptions
{
    public static final String FAKE_AUDIO_PROP = "FAKE_AUDIO";
    public static final String FAKE_VIDEO_PROP = "FAKE_VIDEO";
    public static final String REMOTE_PROP = "REMOTE";
    public static final String BINARY_PROP = "BINARY";
    public static final String VERSION_PROP = "VERSION";


    private static final String FAKE_AUDIO_FNAME_PROP
        = "jitsi-meet.fakeStreamAudioFile";

    private static final String FAKE_VIDEO_FNAME_PROP
        = "jitsi-meet.fakeStreamVideoFile";

    public WebParticipantOptions(Properties config)
    {
        super(config);

        initDefaults();
    }

    private void initDefaults()
    {
        String fakeStreamAudioFile = config.getProperty(FAKE_AUDIO_FNAME_PROP);
        if (fakeStreamAudioFile == null)
        {
            fakeStreamAudioFile = "resources/fakeAudioStream.wav";
        }
        this.put(FAKE_AUDIO_PROP, fakeStreamAudioFile);

        String fakeStreamVideoFile = config.getProperty(FAKE_VIDEO_FNAME_PROP);
        if (fakeStreamVideoFile != null
            && fakeStreamVideoFile.trim().length() > 0)
        {
            this.put(FAKE_VIDEO_PROP, fakeStreamVideoFile.trim());
        }
    }

    @Override
    public void load(String configPrefix)
    {
        super.load(configPrefix);

        String isRemote = config.getProperty(configPrefix + ".isRemote");
        if (isRemote != null)
            this.put(REMOTE_PROP, Boolean.valueOf(isRemote));

        String browserBinary = config.getProperty(configPrefix + ".binary");
        if (browserBinary != null)
            this.put(BINARY_PROP, browserBinary);

        String version = config.getProperty(configPrefix + ".version");
        if (version != null)
        this.put(VERSION_PROP, version);
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
        this.put(FAKE_AUDIO_PROP, fakeStreamAudioFile);

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
        this.put(FAKE_VIDEO_PROP, fakeStreamVideoFile);

        return this;
    }
}
