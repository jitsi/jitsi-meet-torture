/*
 * Copyright @ Atlassian Pty Ltd
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

/**
 * The available participant type value.
 */
public enum ParticipantType
{
    android,
    chrome,
    edge,
    firefox,
    ios,
    safari;

    /**
     * Converts a string value to {@link ParticipantType}
     *
     * @param type the participant type string
     * @return the participant type enum item or <tt>null</tt> if there's no
     * match.
     */
    public static ParticipantType valueOfString(String type)
    {
        return type != null
            ? ParticipantType.valueOf(type.toLowerCase()) : null;
    }

    /**
     * Tells if this participant is Android.
     *
     * @return a boolean
     */
    public boolean isAndroid()
    {
        return this == android;
    }

    /**
     * Tells if this browser type is Chrome.
     *
     * @return a boolean
     */
    public boolean isChrome()
    {
        return this == chrome;
    }

    /**
     * Tells if this browser type is Firefox.
     *
     * @return a boolean
     */
    public boolean isFirefox()
    {
        return this == firefox;
    }

    /**
     * Tells if this participant is iOS.
     *
     * @return a boolean
     */
    public boolean isIOS()
    {
        return this == ios;
    }

    /**
     * Tells if this participant type is a mobile device type of driver.
     *
     * @return a boolean
     */
    public boolean isMobile()
    {
        switch(this)
        {
        case android:
        case ios:
            return true;
        default:
            return false;
        }
    }

    /**
     * Tells if this browser type is Safari.
     *
     * @return a boolean
     */
    public boolean isSafari()
    {
        return this == safari;
    }

    /**
     * Tells if this participant type instance is a web type of participant.
     *
     * @return a boolean
     */
    public boolean isWeb()
    {
        switch (this)
        {
        case chrome:
        case edge:
        case firefox:
        case safari:
            return true;
        default:
            return false;
        }
    }
}
