/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jitsi.meet.test;

import junit.framework.*;

/**
 * A test which always needs to be the last to clear all resources (browsers)
 * that were open.
 *
 * @author Damian Minkov
 */
public class DisposeConference
    extends TestCase
{
    /**
     * Disposes the secondParticipant and the owner.
     */
    public void testDispose()
    {
        ConferenceFixture.quit(ConferenceFixture.getSecondParticipant());
        ConferenceFixture.quit(ConferenceFixture.getOwner());
    }
}
