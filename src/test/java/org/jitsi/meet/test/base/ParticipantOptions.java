package org.jitsi.meet.test.base;

import org.apache.commons.lang3.*;
import org.jitsi.meet.test.util.*;

import java.util.*;

public class ParticipantOptions
{
    public static final String TYPE_PROP = "TYPE";
    public static final String NAME_PROP = "NAME";

    private final Properties backend = new Properties();

    static ParticipantType getParticipantType(
        Properties config, String configPrefix)
    {
        // It will be Chrome by default...
        ParticipantType participantType
            =  ParticipantType.valueOfString(
            config.getProperty(configPrefix + ".type"));

        if (participantType == null)
        {
            TestUtils.print(
                "No participant type specified for prefix: "
                    + configPrefix+", will use Chrome...");
            participantType = ParticipantType.chrome;
        }

        return participantType;
    }

    /**
     *
     */
    public void load(
        Properties config,
        String configPrefix,
        ParticipantOptions overrides)
    {
        this.setProperty(TYPE_PROP, getParticipantType(config, configPrefix));

        String name = configPrefix.substring(configPrefix.indexOf('.') + 1);
        this.setProperty(NAME_PROP, name);

        if (overrides != null)
            this.backend.putAll(overrides.backend);
    }

    public ParticipantType getParticipantType()
    {
        return (ParticipantType)this.getProperty(TYPE_PROP);
    }

    public String getName()
    {
        return (String)this.getProperty(NAME_PROP);
    }

    public boolean getBooleanProperty(String key)
    {
        Object v = this.getProperty(key);
        if (v == null)
        {
            return false;
        }
        else
        {
            return new Boolean((String)v);
        }
    }

    protected Object getProperty(String key)
    {
        return this.backend.get(key);
    }

    protected ParticipantOptions setProperty(String key, Object value)
    {
        this.backend.put(key, value);
        return this;
    }

    protected void loadConfigProperty(
        Properties config, String configPrefix, String key)
    {
        this.loadConfigProperty(config, configPrefix, key, null);
    }

    protected void loadConfigProperty(
        Properties config,
        String configPrefix, String key, String defaultValue)
    {
        String configValue = config.getProperty(configPrefix + "." + key);
        if (StringUtils.isNotBlank(configValue))
        {
            this.setProperty(key, configValue.trim());
        }
        else if (defaultValue != null)
        {
            this.setProperty(key, defaultValue);
        }
    }
}
