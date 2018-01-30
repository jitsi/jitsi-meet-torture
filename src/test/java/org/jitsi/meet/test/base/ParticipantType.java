package org.jitsi.meet.test.base;

/**
 * The available participant type value.
 */
public enum ParticipantType
{
    android,
    chrome, // default one
    edge,
    firefox,
    ios,
    safari;

    /**
     * Default is chrome.
     * @param type the participant type string
     * @return the participant type enum item.
     */
    public static ParticipantType valueOfString(String type)
    {
        if (type == null)
            return chrome;
        else
            return ParticipantType.valueOf(type);
    }

    /**
     * Tells if this browser type is Chrome.
     * @return a boolean
     */
    public boolean isChrome()
    {
        return this == chrome;
    }

    /**
     * Tells if this browser type is Firefox.
     * @return a boolean
     */
    public boolean isFirefox()
    {
        return this == firefox;
    }
}
