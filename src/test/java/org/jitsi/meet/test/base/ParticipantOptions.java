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
package org.jitsi.meet.test.base;

import org.apache.commons.lang3.*;
import org.jitsi.meet.test.util.*;

import java.util.*;

/**
 * Participant options used for creating the participant(its driver).
 */
public class ParticipantOptions
{
    /**
     * The property name for the type of the participant option.
     */
    public static final String TYPE_PROP = "TYPE";

    /**
     * The property name for the name of the participant option.
     */
    public static final String NAME_PROP = "NAME";

    /**
     * The backend map of properties for the participant options.
     */
    private final Properties backend = new Properties();

    /**
     * Parses the configuration to extract participant tyep.
     * @param config - A <tt>Properties</tt> instance holding configuration
     * properties required to setup new participants.
     * @param configPrefix the config prefix which is used to identify
     * the config properties which describe the new participant.
     * @return the participant type.
     */
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
     * Loads value from the configuration into the participant options backend.
     * @param config - A <tt>Properties</tt> instance holding configuration
     * properties required to setup new participants.
     * @param configPrefix the config prefix which is used to identify
     * the config properties which describe the new participant.
     * @param overrides custom options to be used for the participant.
     */
    public void load(
        Properties config,
        String configPrefix,
        ParticipantOptions overrides)
    {
        this.setProperty(TYPE_PROP, getParticipantType(config, configPrefix));

        String name = configPrefix.substring(configPrefix.indexOf('.') + 1);
        this.setProperty(NAME_PROP, name);

        this.merge(overrides);
    }

    /**
     * Overrides backend properties with the values from supplied map.
     * @param overrides the options to use for overriding.
     */
    private void merge(ParticipantOptions overrides)
    {
        if (overrides != null)
        {
            this.backend.putAll(overrides.backend);
        }
    }

    /**
     * Returns the participant type.
     * @return the participant type.
     */
    public ParticipantType getParticipantType()
    {
        return (ParticipantType)this.getProperty(TYPE_PROP);
    }

    /**
     * Returns the name of the participant.
     * @return the name of the participant.
     */
    public String getName()
    {
        return (String)this.getProperty(NAME_PROP);
    }

    /**
     * Utility method to extract a boolena property.
     * @param key the key to search for.
     * @return the boolean property value or false if not found.
     */
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

    /**
     * Returns a property value.
     * @param key the key to search for.
     * @return
     */
    protected Object getProperty(String key)
    {
        return this.backend.get(key);
    }

    /**
     * Changes a property.
     * @param key the key to search for.
     * @param value the value to use.
     * @return a reference to this object.
     */
    protected ParticipantOptions setProperty(String key, Object value)
    {
        this.backend.put(key, value);
        return this;
    }

    /**
     * Loads a config property.
     * @param config - A <tt>Properties</tt> instance holding configuration
     * properties required to setup new participants.
     * @param configPrefix the config prefix which is used to identify
     * the config properties which describe the new participant.
     * @param key the key to load.
     */
    protected void loadConfigProperty(
        Properties config, String configPrefix, String key)
    {
        this.loadConfigProperty(config, configPrefix, key, null);
    }

    /**
     * Loads a config property.
     * @param config - A <tt>Properties</tt> instance holding configuration
     * properties required to setup new participants.
     * @param configPrefix the config prefix which is used to identify
     * the config properties which describe the new participant.
     * @param key the key to load.
     * @param defaultValue a default value to use for the key if none is found
     * in the config.
     */
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
