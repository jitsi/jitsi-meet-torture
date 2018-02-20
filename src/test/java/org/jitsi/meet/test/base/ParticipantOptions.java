package org.jitsi.meet.test.base;

import org.apache.commons.lang3.*;
import org.jitsi.meet.test.util.*;

import java.util.*;

public class ParticipantOptions
{
    public static final String TYPE_PROP = "TYPE";
    public static final String NAME_PROP = "NAME";

    private String configPrefix;
    protected Properties config;

    private final Properties backend = new Properties();

    public ParticipantOptions(Properties config)
    {
        this.config = config;
    }

    /**
     *
     */
    public void load(String configPrefix)
    {
        this.configPrefix = configPrefix;

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
        this.setProperty(TYPE_PROP, participantType);

        String name = configPrefix.substring(configPrefix.indexOf('.') + 1);
        this.setProperty(NAME_PROP, name);
    }

    public ParticipantType getParticipantType()
    {
        return (ParticipantType)this.getProperty(TYPE_PROP);
    }

    public String getName()
    {
        return (String)this.getProperty(NAME_PROP);
    }

    public String getConfigPrefix()
    {
        return this.configPrefix;
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
            return (boolean)v;
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

    protected void loadConfigProperty(String configPrefix, String key)
    {
        this.loadConfigProperty(configPrefix, key, null);
    }

    protected void loadConfigProperty(
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
