/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jitsi.meet.test.util;

import java.io.*;
import java.util.*;

/**
 * Utility class for loading .properties files.
 *
 * @author Pawel Domas
 */
public class PropertiesUtils
{
    static public Properties loadPropertiesFile(String fileName)
        throws IOException
    {
        Properties properties = new Properties();
        InputStream input = null;

        try
        {
            input = new FileInputStream(fileName);

            properties.load(input);

            return properties;
        }
        finally
        {
            if (input != null)
            {
                input.close();
            }
        }
    }
}
