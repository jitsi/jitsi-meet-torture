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

import java.net.*;
import java.util.*;

/**
 * Convenience class for dealing with Jitsi Meet conference URL components.
 *
 * @author Pawel Domas
 * @author George Politis
 */
public class JitsiMeetUrl
    implements Cloneable
{
    /**
     * In the example URL:
     * "https://server.com/room1?login=true#config.debug=true" it's
     * "config.debug=true". Note that the "#" sign is not stored in the field,
     * but added when the URL string is being constructed. At the same time any
     * sub parameters should be joined with the "&" sign when passed to
     * {@link #setHashConfigPart(String)}. For convenience
     * {@link #appendConfig(String)} will do that automatically.
     */
    private final Map<String, String> hashConfigPart = new HashMap<>();

    /**
     * In the example URL:
     * "https://server.com/room1?login=true#config.debug=true" it's "room1".
     */
    private String roomName;

    /**
     * In the example URL:
     * "https://server.com/room1?login=true#config.debug=true"
     * it's "login=true". Note that "?" sign is added automatically when the URL
     * string is being constructed, but at the same time any sub parameters
     * should be joined with the "&" sign.
     */
    private String roomParameters;

    /**
     * In the example URL:
     * "https://server.com/room1?login=true#config.debug=true"
     * it's "https://server.com".
     */
    private String serverUrl;

    /**
     * If set, this should instruct the driver opening the page, that there is
     * an iframe loaded and we need to navigate to it, once it is loaded.
     */
    private String iframeToNavigateTo;

    /**
     * Returns the iframeToNavigateTo value.
     * @return the iframeToNavigateTo value.
     */
    public String getIframeToNavigateTo()
    {
        return iframeToNavigateTo;
    }

    /**
     * Sets the iframeToNavigateTo value.
     * @param iframeToNavigateTo the new value.
     * @return a reference to this object.
     */
    public JitsiMeetUrl setIframeToNavigateTo(String iframeToNavigateTo)
    {
        this.iframeToNavigateTo = iframeToNavigateTo;
        return this;
    }

    /**
     * Adds extra config parameters.
     *
     * @param extraConfig extra config params to be added at the end of the
     * current {@link #hashConfigPart}, without "?" nor "&" at the beginning.
     * @return a reference to this object.
     */
    public JitsiMeetUrl appendConfig(String extraConfig)
    {
        if (StringUtils.isBlank(extraConfig))
        {
            return this;
        }

        String[] pairs = extraConfig.split("&");
        for (String pair : pairs)
        {
            String[] keyValue = pair.split("=");
            if (keyValue.length > 1)
            {
                hashConfigPart.put(keyValue[0], keyValue[1]);
            }
            else
            {
                hashConfigPart.remove(keyValue[0]);
            }
        }

        return this;
    }

    /**
     * Overriding this method will make a field-to-field copy of the URL.
     *
     * @return a field-to-field copy of this instance.
     */
    @Override
    protected Object clone()
    {
        try
        {
            return super.clone();
        }
        catch (CloneNotSupportedException e)
        {
            // We don't want to handle that it all places, because we claim
            // to support it.
            throw new RuntimeException(e);
        }
    }

    /**
     * A {@link URL} constructed from the result of {@link #toString()}
     *
     * @return a full Jitsi Meet conference <tt>URL</tt>.
     * @throws MalformedURLException all the fields to not combine into a valid
     * URL.
     */
    public URL toUrl()
        throws MalformedURLException
    {
        return new URL(toString());
    }

    /**
     * @return a host part of the URL returned by {@link #toUrl()}.
     * @throws MalformedURLException the same as in {@link #toUrl()}.
     */
    public String getHost()
        throws MalformedURLException
    {
        return toUrl().getHost();
    }

    /**
     * @return obtains {@link #roomName} part of the conference URL.
     */
    public String getRoomName()
    {
        return roomName;
    }

    /**
     * @return obtains {@link #roomParameters} part of the conference URL.
     */
    public String getRoomParameters()
    {
        return roomParameters;
    }

    /**
     * @return obtains {@link #serverUrl} part of the conference  URL.
     */
    public String getServerUrl()
    {
        return serverUrl;
    }

    /**
     * Sets the {@link #roomName} part of the conference URL.
     *
     * @param roomName a room name without any special characters.
     * @return a reference to this object.
     */
    public JitsiMeetUrl setRoomName(String roomName)
    {
        this.roomName = roomName;
        return this;
    }

    /**
     * Sets the {@link #roomParameters} part of the conference URL.
     *
     * @param roomParameters the conference room parameters without "?" sign at
     * the beginning, but with "&" between each of the params which are part of
     * a single string passed here as an argument.
     * @return a reference to this object.
     */
    public JitsiMeetUrl setRoomParameters(String roomParameters)
    {
        this.roomParameters = roomParameters;
        return this;
    }

    /**
     * Sets the {@link #serverUrl} part of the conference URL.
     *
     * @param serverUrl a Jitsi Meet server URL (see {@link #serverUrl} for more
     * details).
     */
    public void setServerUrl(String serverUrl)
    {
        this.serverUrl = serverUrl;
    }

    /**
     * This will put all of the URL components together and print them as
     * a string.
     *
     * @return a string which represents a full Jitsi Meet conference URL.
     */
    @Override
    public String toString()
    {
        String url = serverUrl + "/" + roomName;

        if (StringUtils.isNotBlank(roomParameters))
        {
            url += "?" + roomParameters;
        }

        boolean appendHash = true;
        StringBuilder urlBuilder = new StringBuilder(url);
        for (Map.Entry<String, String> entry : hashConfigPart.entrySet())
        {
            if (appendHash)
            {
                urlBuilder.append("#");
                appendHash = false;
            }
            else
            {
                urlBuilder.append("&");
            }

            urlBuilder.append(entry.getKey());
            urlBuilder.append("=");
            urlBuilder.append(entry.getValue());
        }

        return urlBuilder.toString();
    }
}
