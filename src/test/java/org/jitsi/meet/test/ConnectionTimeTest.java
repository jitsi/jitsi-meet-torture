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

import java.util.*;

import org.jitsi.meet.test.util.*;
import org.jitsi.meet.test.web.*;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;
import org.testng.annotations.*;

import static org.jitsi.meet.test.util.TestUtils.*;
import static org.testng.Assert.*;

/**
 * This test is going to get the connection times measurements from Jitsi Meet
 * and fail if they are too slow.
 *
 * @author Hristo Terezov
 */
public class ConnectionTimeTest
    extends WebTestBase
{
    /**
     * Number of conferences that are going to be started and closed to 
     * gather the data.
     */
    private static int NUMBER_OF_CONFERENCES = 10;
    
    /**
     * Script that checks if the mandatory objects that are going to be used to
     * get the connection time measurements are created or not. 
     */
    private static final String CHECK_OBJECTS_CREATED_SCRIPT 
        = "return (APP && APP.connection "
            + "&& APP.conference && APP.conference._room)? true : false";

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

        // FIXME Changed the threshold from 150.0 to 300.0, once we
        // update the code and improve the setup time we can return it back to
        // to the original value
        ICE_CHECKING("return APP.conference._room.getConnectionTimes()"
            + "['ice.state.checking']", SESSION_INITIATE, 300.0),
        
        ICE_CONNECTED("return APP.conference._room.getConnectionTimes()"
            + "['ice.state.connected']", ICE_CHECKING, 500.0),
        
        AUDIO_RENDER(
            "return APP.conference._room.getConnectionTimes()['audio.render']",
            ICE_CONNECTED, 200.0),

        // FIXME Changed the threshold from 200.0 to 550.0, once we
        // update the code and improve the setup time we can return it back to
        // to the original value
        VIDEO_RENDER(
            "return APP.conference._room.getConnectionTimes()['video.render']",
            ICE_CONNECTED, 550.0),

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
         * Executes the script property for the given {@link WebDriver}
         * and returns a time measurement. (?)
         * @param driver the {@link WebDriver}
         * @return time in ms for the measurement.
         */
        public Double execute(WebDriver driver)
        {
            Object res = ((JavascriptExecutor) driver).executeScript(script);

            if (res instanceof Number)
            {
                return ((Number) res).doubleValue();
            }
            else if (res == null)
            {
                return null;
            }
            else
            {
                fail("Wrong type returned from selenium!");
            }

            return null;
        }
        
        /**
         * Executes CHECK_OBJECTS_CREATED_SCRIPT for passed WebDriver and
         * returns the result. That way we can check if all objects that are
         * used to get the time measurements are created or not.
         * @param driver the {@link WebDriver}.
         * @return true if ready and false if not.
         */
        public static Boolean isReadyToStart(WebDriver driver)
        {
            Object res = ((JavascriptExecutor) driver).executeScript(
                CHECK_OBJECTS_CREATED_SCRIPT);

            if (res instanceof Boolean)
            {
                return (Boolean) res;
            }
            else
            {
                fail("Wrong type returned from selenium!");
            }
            return null;
        }
        
        
    }

    @Override
    public void setupClass()
    {
        super.setupClass();

        ensureTwoParticipants();
    }

    @DataProvider(name = "dp")
    public Object[][] createData()
    {
        // If the tests is not in the list of tests to be executed,
        // skip executing the DataProvider.
        if (isSkipped())
        {
            return new Object[0][0];
        }

        Double[][] data = collectData();

        boolean isUsingAttach = isUsingAttach();

        return new Object[][]
        {
            new Object[] { data, TimeMeasurements.INDEX_LOADED, null },
            new Object[] { data, TimeMeasurements.DOCUMENT_READY, null },

            // If jiconop is enabled the value will be
            // TimeMeasurements.CONNECTION_ATTACHING otherwise the value will be
            // TimeMeasurements.CONNECTION_CONNECTING.
            new Object[]
            {
                data,
                isUsingAttach
                    ? TimeMeasurements.CONNECTION_ATTACHING
                    :  TimeMeasurements.CONNECTION_CONNECTING,
                null
            },

            // If jiconop is enabled the value will be
            // TimeMeasurements.CONNECTION_ATTACHED otherwise the value will be
            // TimeMeasurements.CONNECTION_CONNECTED.
            new Object[]
            {
                data,
                isUsingAttach
                    ? TimeMeasurements.CONNECTION_ATTACHED
                    :  TimeMeasurements.CONNECTION_CONNECTED,
                null
            },

            new Object[] { data,
                TimeMeasurements.MUC_JOINED,
                isUsingAttach
                    ? TimeMeasurements.CONNECTION_ATTACHED
                    :  TimeMeasurements.CONNECTION_CONNECTED
            },
            new Object[] { data, TimeMeasurements.SESSION_INITIATE, null },
            new Object[] { data, TimeMeasurements.ICE_CHECKING, null },
            new Object[] { data, TimeMeasurements.ICE_CONNECTED, null },
            new Object[] { data, TimeMeasurements.AUDIO_RENDER, null },
            new Object[] { data, TimeMeasurements.VIDEO_RENDER, null },
            new Object[] { data, TimeMeasurements.DATA_CHANNEL_OPENED, null }
        };
    }

    /**
     * Evaluates passed time measurement value. Fails if the difference between
     * the previous measurement and the current one is bigger than the
     * threshold for the current measurement.
     * @param data the data to check
     * @param s the time measurement that will be evaluated.
     * @param connectedTime the connected time, optional, needed only for
     * TimeMeasurements.MUC_JOINED.
     */
    @Test(dataProvider = "dp")
    public void check(
        Double[][] data,
        TimeMeasurements s,
        TimeMeasurements connectedTime)
    {
        if (s == TimeMeasurements.MUC_JOINED)
        {
            checkThreshold(data, data[connectedTime.ordinal()], s);
        }
        else
        {
            Double[] prevStepData
                = s.getPrevStep() == null
                    ? null : data[s.getPrevStep().ordinal()];
            checkThreshold(data, prevStepData, s);
        }
    }

    /**
     * Checks which connect method is used - attach or connect.
     */
    private boolean isUsingAttach()
    {
        return (Boolean)
            getParticipant2()
                .executeScript("return !!config.externalConnectUrl;");
    }

    /**
     * Fails if the array has null elements.
     * @param array the array to be checked
     */
    private void failIfContainsNull(Double[] array)
    {
        if (array == null)
        {
            fail("Array is null");
        }

        if (Arrays.asList(array).contains(null))
        {
            fail("Array contains null");
        }
    }

    /**
     * Gets the time measurements for NUMBER_OF_CONFERENCES different
     * conferences and stores them in data array. Initializes
     * connectingTime and connectedTime that are going to be used later.
     */
    public Double[][] collectData()
    {
        Double[][] data
            = new Double[TimeMeasurements.length][NUMBER_OF_CONFERENCES];

        for (int i = 0; i < NUMBER_OF_CONFERENCES; i++)
        {
            refreshParticipant2();

            waitForMeasurements();

            for (TimeMeasurements s : TimeMeasurements.values())
            {
                data[s.ordinal()][i]
                    = s.execute(getParticipant2().getDriver());
                print(s + ": " + data[s.ordinal()][i] );
            }
        }

        for (TimeMeasurements s : TimeMeasurements.values())
        {
            print(s + ": " + Arrays.toString(data[s.ordinal()]) );
        }

        return data;
    }
    
    /**
     * Refreshes the second participant.
     */
    private void refreshParticipant2()
    {
        // initially the second participant is not connected
        if (getParticipant2() != null)
        {
            getParticipant2().hangUp();
        }

        ensureTwoParticipants();
    }
    
    /**
     * Waits for all measurements to be complete. We only wait for VIDEO_RENDER,
     * AUDIO_RENDER and DATA_CHANNEL_OPEN, assuming all the rest would have
     * completed before these three.
     */
    private void waitForMeasurements()
    {
        TestUtils.waitForCondition(
            getParticipant2().getDriver(),
            10,
            (ExpectedCondition<Boolean>) w
                -> TimeMeasurements.isReadyToStart(w)
                    && TimeMeasurements.AUDIO_RENDER.execute(w) != null
                    && TimeMeasurements.VIDEO_RENDER.execute(w) != null
                    && TimeMeasurements.DATA_CHANNEL_OPENED.execute(w) != null);
    }

    /**
     * Compares the threshold for the passed time measurement with the
     * median of the subtracted values of the data from the passed time
     * measurement and the passed array.
     * @param previousStepTimes array with times that will be subtract
     * @param s the time measurement
     */
    private void checkThreshold(
        Double[][] data, Double[] previousStepTimes, TimeMeasurements s)
    {
        Double[] difference
            = (previousStepTimes == null)
                ? data[s.ordinal()] :
                    subtractArrays(previousStepTimes, data[s.ordinal()]);

        failIfContainsNull(difference);

        Double medianValue = getMedian(difference);
        print(s + ":" + medianValue);
        assertTrue(
            medianValue < s.getThreshold(),
            "Expected:" + s.getThreshold() + ", was:" + medianValue);
    }
    
    /**
     * Returns the median from passed array.
     * @param data the array
     * @return the median
     */
    private static Double getMedian(Double[] data)
    {
        Arrays.sort(data);
        return data[data.length/2];
    }
    
    /**
     * Returns array with elements constructed by subtracting element from a
     * from element from b with the same index.
     * @param a the array which elements will checked.
     * @param b the resulting array without the elements from a
     * @return new array
     */
    private static Double[] subtractArrays(Double[] a, Double[] b)
    {
        Double[] res = b.clone();
        for (int i = 0; i < res.length; i++)
        {
            if (res[i] == null && a[i] == null)
            {
                fail("Null value is measured");
            }

            res[i] -= a[i];
        }
        return res;
    }
}
