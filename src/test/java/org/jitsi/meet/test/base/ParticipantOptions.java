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

import java.util.*;

/**
 * Participant options used for creating the participant(its driver).
 */
public class ParticipantOptions
{
    /**
     * A prefix for global options (not per participant).
     */
    public static final String GLOBAL_PROP_PREFIX = "jitsi-meet";

    /**
     * The url of the deployment to connect to.
     */
    public static final String JITSI_MEET_URL_PROP
        = GLOBAL_PROP_PREFIX + ".instance.url";

    /**
     * The property name for the type of the participant option.
     */
    private static final String PROP_TYPE = "type";

    /**
     * The property name for the name of the participant option.
     */
    private static final String PROP_NAME = "name";

    /**
     * The backend map of properties for the participant options.
     */
    private final Properties backend = new Properties();

    /**
     * The map is used as a fallback in {@link #getProperty(String)} in case
     * a value for a key does not exist in {@link #backend}.
     */
    private final Properties defaults;

    /**
     * Loads values from the configuration into the participant options backend.
     * @param config - A <tt>Properties</tt> instance holding configuration
     * properties.
     * @param configPrefix the configuration prefix which is used to identify
     * the config properties which describe the new {@link ParticipantOptions}
     * set.
     */
    public void load(Properties config, String configPrefix)
    {
        for (String key : config.stringPropertyNames())
        {
            if (key.startsWith(configPrefix + "."))
            {
                // Only strings are allowed in the backend, so if config
                // contains object it will throw class cast exception.
                String value = config.getProperty(key);

                backend.setProperty(
                    key.substring(configPrefix.length() + 1), value);
            }
        }
    }

    /**
     * Creates emtpy {@link ParticipantOptions}.
     */
    protected ParticipantOptions()
    {
        this.defaults = initDefaults();
    }

    /**
     * Copies all of the mapping from given {@link ParticipantOptions} to this
     * instance. It will result in an overwrite of any matching properties.
     *
     * @param toAdd the options to be merged into this instance.
     */
    public void putAll(ParticipantOptions toAdd)
    {
        // It was a conscious decision
        Properties srcBackend = toAdd.backend;

        for (String key : srcBackend.stringPropertyNames())
        {
            setProperty(key, srcBackend.getProperty(key));
        }
    }

    /**
     * Returns the participant type.
     * @return the participant type.
     */
    public ParticipantType getParticipantType()
    {
        return ParticipantType.valueOfString(this.getProperty(PROP_TYPE));
    }

    /**
     * Returns the name of the participant.
     * @return the name of the participant.
     */
    public String getName()
    {
        return this.getProperty(PROP_NAME);
    }

    /**
     * Utility method to extract a boolena property.
     * @param key the key to search for.
     * @return the boolean property value or false if not found.
     */
    public boolean getBooleanProperty(String key)
    {
        return Boolean.valueOf(this.getProperty(key));
    }

    /**
     * Returns a property value.
     * @param key the key to search for.
     * @return
     */
    protected String getProperty(String key)
    {
        String value = (String) this.backend.get(key);

        return value != null ? value : (String) defaults.get(key);
    }

    /**
     * Initializes the default set of properties. Subclasses should override
     * this method to provide custom defaults.
     *
     * @return a <tt>Properties</tt> instance which will serve as a source for
     * the default values.
     */
    protected Properties initDefaults()
    {
        return new Properties();
    }

    /**
     * Sets the name option.
     * @param name a string to be set as name.
     */
    public void setName(String name)
    {
        setProperty(PROP_NAME, name);
    }

    /**
     * Sets new {@link ParticipantType} on this options instance.
     * @param participantType the new {@link ParticipantType} to set, or
     * <tt>null</tt> to clear the value.
     *
     * @return this instance.
     */
    public ParticipantOptions setParticipantType(
            ParticipantType participantType)
    {
        setProperty(
            PROP_TYPE,
            participantType != null
                ? participantType.toString()
                : null /* remove key */);

        return this;
    }

    /**
     * Changes a property. If a <tt>null</tt> value is passed then the given
     * <tt>key</tt> will be removed from the properties set.
     * @param key the key to search for.
     * @param value the value to use. Only String can be used here, as all
     * properties have to be converted to String when set and eventually to
     * the target type when read through the getter.
     */
    protected void setProperty(String key, String value)
    {
        if (StringUtils.isNotBlank(value))
        {
            this.backend.put(key, value.trim());
        }
        else
        {
            this.backend.remove(key);
        }
    }
}
