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

import java.io.*;
import java.util.*;

/**
 * Loads custom settings.properties an populates them in system properties.
 *
 * @author Damian Minkov
 */
public class TestSettings
{
    /**
     * The settings file we will use to load properties needed for tests.
     */
    private static final String SETTINGS_PROPERTIES_LOCATION
        = "settings.properties";

    /**
     * Initialize the settings by loading the properties file and use all
     * its values as system properties.
     *
     * @return a properties set containing all the stuff from
     * {@link #SETTINGS_PROPERTIES_LOCATION} file merged with the
     * {@link System#getProperties()} set.
     */
    public static Properties initSettings()
    {
        // will load properties from settings.properties files
        try (
            InputStream is
                = TestSettings.class.getClassLoader()
                    .getResourceAsStream(SETTINGS_PROPERTIES_LOCATION))
        {
            // settings.properties file is missing
            if (is == null)
            {
                throw new RuntimeException("Missing settings.properties file.");
            }

            Properties p = new Properties();

            // take current system properties as we will set merged props
            // to system
            p.putAll(System.getProperties());

            p.load(is);

            // setting system properties
            System.setProperties(p);

            return p;
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }
}
