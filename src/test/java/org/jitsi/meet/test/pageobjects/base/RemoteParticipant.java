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
package org.jitsi.meet.test.pageobjects.base;

import org.openqa.selenium.*;

/**
 * The representation of the remote participants (thumbnails) and their menus.
 *
 * @param <T> the driver remote/appium that is used.
 */
public abstract class RemoteParticipant<T extends WebDriver>
{
    /**
     * The endpoint id of the participant.
     */
    private final String endpointId;

    /**
     * The driver.
     */
    protected final T driver;

    /**
     * Constructs <tt>RemoteParticipant</tt> instance.
      * @param driver the driver to use.
     * @param endpointId the endpoint id.
     */
    public RemoteParticipant(T driver, String endpointId)
    {
        this.driver = driver;
        this.endpointId = endpointId;
    }

    /**
     * Returns the endpoint id.
     * @return the endpoint id.
     */
    public String getEndpointId()
    {
        return endpointId;
    }

    /**
     * Promotes the participant to a moderator. This menu/operation is
     * available only for moderators on non-moderator remote participants.
     */
    public abstract void grantModerator();

    /**
     * Mutes the participant. This menu/operation is available only for
     * moderators and for all participants in case of all moderators module
     * enabled on the server.
     */
    public abstract void mute();

    /**
     * Stops the participant's video. This menu/operation is available only for
     * moderators and for all participants in case of all moderators module
     * enabled on the server.
     */
    public abstract void stopVideo();

    /**
     * Kicks the participant. This menu/operation is available only for
     * moderators and for all participants in case of all moderators module
     * enabled on the server.
     */
    public abstract void kick();

    @Override
    public String toString()
    {
        return "RemoteParticipant{" +
            "endpointId='" + endpointId + '\'' +
            '}';
    }
}
