/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jitsi.meet.test;

import junit.framework.*;
import org.junit.runner.*;
import org.junit.runners.*;

/**
 * The main test suite which will order tests.
 * @author Damian Minkov
 */
@RunWith(AllTests.class)
public class TestsRunner
{
    public static TestSuite suite()
    {
        TestSuite suite = new TestSuite();
        suite.addTest(SetupConference.suite());
        suite.addTestSuite(DisposeConference.class);
        return suite;
    }
}
