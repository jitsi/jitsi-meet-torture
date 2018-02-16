package org.jitsi.meet.test.base;

import org.jitsi.meet.test.util.*;

import java.util.*;

public class ParticipantOptions
    extends Properties
{
    public static final String TYPE_PROP = "TYPE";
    public static final String NAME_PROP = "NAME";

    private String configPrefix;
    protected Properties config;

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
                    System.getProperty(configPrefix + ".type"));

        if (participantType == null)
        {
            TestUtils.print(
                "No participant type specified for prefix: "
                    + configPrefix+", will use Chrome...");
            participantType = ParticipantType.chrome;
        }
        this.put(TYPE_PROP, participantType);

        String name = configPrefix.substring(configPrefix.indexOf('.') + 1);
        this.put(NAME_PROP, name);
    }

    public ParticipantType getParticipantType()
    {
        return (ParticipantType)this.get(TYPE_PROP);
    }

    public String getName()
    {
        return (String)this.get(NAME_PROP);
    }

    public String getConfigPrefix()
    {
        return this.configPrefix;
    }
}
