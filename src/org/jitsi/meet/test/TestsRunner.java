/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jitsi.meet.test;

import org.junit.runner.*;
import org.junit.runners.*;

/**
 * The main test suite which will order tests.
 * @author Damian Minkov
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    SetupConference.class,
    DisposeConference.class
})
public class TestsRunner
{}
