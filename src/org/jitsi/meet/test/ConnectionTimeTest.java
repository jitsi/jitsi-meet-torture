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
package org.jitsi.meet.test;

import java.util.Arrays;

import org.jitsi.meet.test.util.*;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;

import junit.framework.*;

/**
 * This test is going to get the connection times measurements from Jitsi Meet
 * and fail if they are too slow.
 * 
 *
 * @author Hristo Terezov
 */
public class ConnectionTimeTest
    extends TestCase
{
    /**
     * Number of conferences that are going to be started and closed to 
     * gather the data.
     */
    private static int NUMBER_OF_CONFERENCES = 10;
    
    /**
     * The second participant. We are using that property to refresh the second
     * tab.
     */
    private static WebDriver participant 
        = ConferenceFixture.getSecondParticipant();
    
    /**
     * Enum that represents the types of time measurements. We are storing the
     * scripts that is used to get the value for that type, a previous step and
     * threshold.
     */
    private enum TimeMeasurements
    {
        INDEX_LOADED("return APP.connectionTimes['index.loaded']", null, 200.0),
        
        DOCUMENT_READY("return APP.connectionTimes['document.ready']", 
            INDEX_LOADED, 600.0),
        
        CONNECTION_ATTACHING(
            "return APP.connection.getConnectionTimes()['attaching']",
            DOCUMENT_READY, 500.0),
        
        CONNECTION_ATTACHED(
            "return APP.connection.getConnectionTimes()['attached']", 
            CONNECTION_ATTACHING, 5.0),
        
        CONNECTION_CONNECTING(
            "return APP.connection.getConnectionTimes()['connecting']", 
            DOCUMENT_READY, 500.0),
        
        CONNECTION_CONNECTED(
            "return APP.connection.getConnectionTimes()['connected']", 
            CONNECTION_CONNECTING, 1000.0),
        
        MUC_JOINED(
            "return APP.conference._room.getConnectionTimes()['muc.joined']",
            null, 500.0),
        
        SESSION_INITIATE("return APP.conference._room.getConnectionTimes()"
            + "['session.initiate']", MUC_JOINED, 600.0),
        
        ICE_CHECKING("return APP.conference._room.getConnectionTimes()"
            + "['ice.state.checking']", SESSION_INITIATE, 150.0),
        
        ICE_CONNECTED("return APP.conference._room.getConnectionTimes()"
            + "['ice.state.connected']", ICE_CHECKING, 500.0),
        
        AUDIO_RENDER("return APP.connectionTimes['audio.render']", 
            ICE_CONNECTED, 200.0),
        
        VIDEO_RENDER("return APP.connectionTimes['video.render']", 
            ICE_CONNECTED, 200.0),

        // The data channel should open about 2 RTTs after DTLS completes, so
        // this threshold should go down to something like 200ms.
        // However, there is currently a bug in jitsi-videobridge which adds a
        // delay of about 3 seconds. Another bug, recently fixed, would cause
        // a delay of ~15 seconds, which is why we now use a threshold of 4s.
        DATA_CHANNEL_OPENED(
            "return APP.conference._room.getConnectionTimes()"
                + "['data.channel.opened']",
            ICE_CONNECTED, 4000.0);

        /**
         * The script used to get the data for a time measurement type.
         */
        private String script;
        
        /**
         * Max time between the previous measurement and 
         * the current one
         */
        private Double threshold;
        
        /**
         * The previous executed time measurement. We are going to compare the 
         * period of time between 2 consecutive time measurements and the 
         * threshold.
         */
        private TimeMeasurements prevStep;
        
        /**
         * The number of time measurements/
         */
        public static final int length = TimeMeasurements.values().length;
        
        /** 
         * Construct new TimeMeasurements instance.
         * @param script The script used to get the data for a time 
         * measurement type.
         * @param prevStep previous measurement.
         * @param threshold Max time between the previous measurement and 
         * the current one 
         */
        TimeMeasurements(String script, TimeMeasurements prevStep,
            Double threshold)
        {
            this.script = script;
            this.prevStep = prevStep;
            this.threshold = threshold;
        }
        
        /**
         * Returns the threshold property.
         * @return the threshold property.
         */
        public Double getThreshold()
        {
            return threshold;
        }
        
        /**
         * Returns prevStep property.
         * @return prevStep property.
         */
        public TimeMeasurements getPrevStep()
        {
            return prevStep;
        }
        
        /**
         * Executes the script property for passed WebDriver and returns
         * time measurement
         * @param w participant
         * @return time in ms for the measurement.
         */
        public Double execute(WebDriver w) 
        {
            Object res = ((JavascriptExecutor) w).executeScript(script);
            
            if(res instanceof Number)
                return ((Number)res).doubleValue();
            else if(res == null)
                return null;
            else 
                fail("Wrong type returned from selenium!");
            return null;
        }
        
    }

    /**
     * If jiconop is enabled the value will be 
     * TimeMeasurements.CONNECTION_ATTACHING otherwise the value will be
     * TimeMeasurements.CONNECTION_CONNECTING.
     */
    private static TimeMeasurements connectingTime;

    /**
     * If jiconop is enabled the value will be 
     * TimeMeasurements.CONNECTION_ATTACHED otherwise the value will be
     * TimeMeasurements.CONNECTION_CONNECTED.
     */
    private static TimeMeasurements connectedTime;
    
    /**
     * The time measurement type that is currently evaluated.  
     */
    private TimeMeasurements timeMeasurementToProcess = null;
    
    /**
     * Property used to store the gathered data.
     */
    private static Double[][] data 
        = new Double[TimeMeasurements.length][NUMBER_OF_CONFERENCES];
    
    /**
     * Constructs test
     * @param name the method name for the test.
     * @param t time measurement that will be tested
     */
    public ConnectionTimeTest(String name, TimeMeasurements t)
    {
        super(name);
        this.timeMeasurementToProcess = t;
    }
    
    /**
     * Orders the tests.
     * @return the suite with order tests.
     */
    public static junit.framework.Test suite()
    {
        TestSuite suite = new TestSuite();
        suite.addTest(
            new ConnectionTimeTest("collectData", null));
        suite.addTest(
            new ConnectionTimeTest("checkIndexLoaded", TimeMeasurements.INDEX_LOADED));
        suite.addTest(
            new ConnectionTimeTest("checkDocumentReady", TimeMeasurements.DOCUMENT_READY));
        suite.addTest(
            new ConnectionTimeTest("checkConnecting", null));
        suite.addTest(
            new ConnectionTimeTest("checkConnected", null));
        suite.addTest(
            new ConnectionTimeTest("checkMUCJoined", TimeMeasurements.MUC_JOINED));
        suite.addTest(
            new ConnectionTimeTest("checkSessionInitiate", TimeMeasurements.SESSION_INITIATE));
        suite.addTest(
            new ConnectionTimeTest("checkIceChecking", TimeMeasurements.ICE_CHECKING));
        suite.addTest(
            new ConnectionTimeTest("checkIceConnected", TimeMeasurements.ICE_CONNECTED));
        suite.addTest(
            new ConnectionTimeTest("checkAudioRender", TimeMeasurements.AUDIO_RENDER));
        suite.addTest(
            new ConnectionTimeTest("checkVideoRender", TimeMeasurements.VIDEO_RENDER));
        suite.addTest(
                new ConnectionTimeTest(
                        "checkDataChannelOpen", TimeMeasurements.VIDEO_RENDER));

        return suite;
    }
    
    /**
     * Defines how a test is going to be executed
     */
    public void runTest() {
        switch(getName())
        {
        case "collectData":
            collectData();
            break;
        case "checkConnecting":
            checkTime(ConnectionTimeTest.connectingTime);
            break;
        case "checkConnected":
            checkTime(ConnectionTimeTest.connectedTime);
            break;
        default:
            checkTime(this.timeMeasurementToProcess);
        }
    }

    /**
     * Gets the time measurements for NUMBER_OF_CONFERENCES different 
     * conferences and stores them in data array. Initializes
     * connectingTime and connectedTime that are going to be used later. 
     */
    public static void collectData() 
    {
        for(int i = 0; i < NUMBER_OF_CONFERENCES; i++)
        {
            refreshSecondParticipant();
            
            waitForMeasurements();
            
            for(TimeMeasurements s : TimeMeasurements.values())
            {
                data[s.ordinal()][i] 
                    = s.execute(participant);
            }
            
        }
        
        if(data[TimeMeasurements.CONNECTION_ATTACHED.ordinal()][0] == null
            && data[TimeMeasurements.CONNECTION_CONNECTED.ordinal()][0] == null)
        {
            // conference failed
            fail("Conference failed!");
            return;
        }
        else if(data[TimeMeasurements.CONNECTION_ATTACHED.ordinal()][0] != null)
        {
            connectingTime = TimeMeasurements.CONNECTION_ATTACHING;
            connectedTime = TimeMeasurements.CONNECTION_ATTACHED;
        }
        else if(data[TimeMeasurements.CONNECTION_CONNECTED.ordinal()][0] != null)
        {
            connectingTime = TimeMeasurements.CONNECTION_CONNECTING;
            connectedTime = TimeMeasurements.CONNECTION_CONNECTED;
        }
    }
    
    /**
     * Refreshes the second participant.
     */
    private static void refreshSecondParticipant()
    {
        ConferenceFixture.close(participant);
        participant = ConferenceFixture.startSecondParticipant();
    }
    
    /**
     * Waits for all measurements to be complete. We only wait for VIDEO_RENDER,
     * AUDIO_RENDER and DATA_CHANNEL_OPEN, assuming all the rest would have
     * completed before these three.
     */
    private static void waitForMeasurements()
    {
        TestUtils.waitForCondition(
            participant, 10, new ExpectedCondition<Boolean>()
        {
            @Override
            public Boolean apply(WebDriver w)
            {
                return
                    TimeMeasurements.DATA_CHANNEL_OPENED.execute(w) != null
                    && TimeMeasurements.VIDEO_RENDER.execute(w) != null
                    && TimeMeasurements.AUDIO_RENDER.execute(w) != null;
            }
        });
    }
    
    /**
     * Evaluates passed time measurement value. Fails if the difference between
     * the previous measurement and the current one is bigger than the 
     * threshold for the current measurement. 
     * @param s the time measurement that will be evaluated.
     */
    private void checkTime(TimeMeasurements s)
    {
        if(s == TimeMeasurements.MUC_JOINED)
        {
            checkThreshold(data[connectedTime.ordinal()], s);
        }
        else
        {
            Double[] prevStepData = s.getPrevStep() == null ? null : 
                data[s.getPrevStep().ordinal()];
            checkThreshold(prevStepData, s);
        }
    }
    
    /**
     * Compares the threshold for the passed time measurement with the 
     * median of the subtracted values of the data from the passed time 
     * measurement and the passed array. 
     * @param previousStepTimes array with times that will be subtract
     * @param s the time measurement
     */
    private void checkThreshold(Double[] previousStepTimes, TimeMeasurements s)
    {
        Double[] difference = (previousStepTimes == null)?  data[s.ordinal()] :
            subtractArrays(previousStepTimes, data[s.ordinal()]);
        Double medianValue = getMedian(difference);
        System.err.println(s + ":" + medianValue);
        assertTrue(
            "Expected:" + s.getThreshold() + ", was:" + medianValue,
            medianValue < s.getThreshold());
    }
    
    /**
     * Returns the median from passed array.
     * @param data the array
     * @return the median
     */
    private static Double getMedian(Double[] data) {
        Arrays.sort(data);
        return data[data.length/2];
    }
    
    /**
     * Retuns array with elements constructed by subtracting element from a 
     * from element from b with the same index.
     * @param a
     * @param b 
     * @return new array
     */
    private static Double[] subtractArrays(Double[] a, Double[] b)
    {
        Double[] res = b.clone();
        for(int i = 0; i < res.length; i++) {
            res[i] -= a[i]; 
        }
        return res;
    }
}
