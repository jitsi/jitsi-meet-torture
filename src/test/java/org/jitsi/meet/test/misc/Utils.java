/*
 * Copyright @ 2018 8x8 Pty Ltd
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
package org.jitsi.meet.test.misc;

public class Utils
{
    /**
     * Checks for a missing(null) or empty config values.
     * @param configs the Strings to check.
     * @return <tt>true</tt> if there is any missing configuration, <tt>false</tt> otherwise.
     */
    public static boolean checkForMissingConfig(String ...configs)
    {
        for (String c: configs)
        {
            if (c == null || c.trim().length() == 0)
            {
                return true;
            }
        }

        return false;
    }
}
