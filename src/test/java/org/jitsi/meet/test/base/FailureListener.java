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

import org.apache.commons.io.*;
import org.jitsi.meet.test.util.*;
import org.openqa.selenium.*;
import org.testng.*;

import java.io.*;
import java.lang.management.*;
import java.util.*;
import java.util.logging.*;

public class FailureListener
    implements ITestListener,
               IConfigurationListener
{
    /**
     * A script to be executed on failure. The script will be executed
     * and the test will cleanup and finish once the script finishes executing.
     */
    private static final String ON_FAILURE_SCRIPT
        = "test.failure.script";

    /**
     * The folder where the logs will be saved.
     */
    private static File outputLogsParentFolder = null;

    /**
     * The folder where the screenshots will be saved.
     */
    private static File outputScreenshotsParentFolder = null;

    /**
     * The folder where the html files will be saved.
     */
    private File outputHtmlSourceParentFolder = null;

    /**
     * Returns the default reports folder or the custom one, specified by
     * system property.
     * @return
     */
    private static String getReportFolder()
    {
        return System.getProperty(
            "test.report.directory", "target/surefire-reports");
    }

    /**
     * Gets the screenshots parent output folder.
     *
     * @return a <tt>File</tt> instance.
     */
    synchronized public static File getScreenshotsOutputFolder()
    {
        if (outputScreenshotsParentFolder == null)
        {
            // This property will be defined when the test are running on
            // AWS mobile device farm for example. If it's not we go with
            // the tests results location.
            String appiumScreenshotDir
                = System.getProperty("appium.screenshots.dir");

            if (appiumScreenshotDir != null)
            {
                outputScreenshotsParentFolder = new File(appiumScreenshotDir);
            }
            else
            {
                outputScreenshotsParentFolder
                    = new File(getReportFolder() + "/screenshots");
            }

            if (!outputScreenshotsParentFolder.exists()
                    && !outputScreenshotsParentFolder.mkdirs())
            {
                TestUtils.print(
                    "Failed to create output screenshots parent folder: "
                        + outputScreenshotsParentFolder.toString());
            }
        }
        return outputScreenshotsParentFolder;
    }

    @Override
    public void onTestStart(ITestResult iTestResult)
    {}

    @Override
    public void onTestSuccess(ITestResult iTestResult)
    {}

    @Override
    public void onTestSkipped(ITestResult iTestResult)
    {}

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult iTestResult)
    {}

    @Override
    public void onStart(ITestContext iTestContext)
    {
        // default reports folder
        outputHtmlSourceParentFolder
            = new File(getReportFolder() + "/html-sources");
        if (!outputHtmlSourceParentFolder.exists()
            && !outputHtmlSourceParentFolder.mkdirs())
        {
            TestUtils.print(
                "Failed to create output HTML source parent folder: "
                    + outputHtmlSourceParentFolder);
        }

        createLogsFolder();
    }

    @Override
    public void onFinish(ITestContext iTestContext)
    {
        // move all failed configurations to the failed tests set
        // in order to easily spot them in the html report
        for (ITestResult r : iTestContext.getFailedConfigurations().getAllResults())
        {
            iTestContext.getFailedTests().addResult(r, r.getMethod());
        }
    }

    @Override
    public void onTestFailure(ITestResult testResult)
    {
        Throwable error = testResult.getThrowable();
        if (error != null)
        {
            // FF crash workaround, marks test as skipped
            if (error instanceof WebDriverException
                && error.getMessage() != null
                && error.getMessage().startsWith(
                    "Failed to decode response from marionette"))
            {
                TestUtils.print("!!!! There is a test failure in "
                    + testResult.getInstanceName() + "."
                        + testResult.getMethod().getMethodName()
                    + ". We will skip this test results, "
                        + "due to known FF random crash.");

                testResult.setStatus(ITestResult.SKIP);

                return;
            }

            TestUtils.print("TestFailure:");
            error.printStackTrace();
        }

        String cmd = System.getProperty(ON_FAILURE_SCRIPT);
        if (cmd != null && cmd.trim().length() > 0)
        {
            try
            {
                Process p = Runtime.getRuntime().exec(cmd);
                p.waitFor();
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }

        try
        {
            AbstractBaseTest testInstance
                = (AbstractBaseTest)testResult.getInstance();

            List<Participant<? extends WebDriver>> participants
                = testInstance.getAllParticipants();

            String fileNamePrefix
                = testResult.getTestClass().getRealClass().getCanonicalName();

            takeScreenshots(fileNamePrefix, participants);

            saveHtmlSources(fileNamePrefix, participants);

            saveMeetDebugLog(fileNamePrefix, participants);

            saveMeetRTPStats(fileNamePrefix, participants);

            saveBrowserLogs(fileNamePrefix, participants);

            saveThreadDump(fileNamePrefix);
        }
        catch(Throwable ex)
        {
            Logger.getGlobal().log(Level.SEVERE, "Failed to gather debug information for ", ex);
        }
    }

    @Override
    public void onConfigurationSuccess(ITestResult itr)
    {}

    @Override
    public void onConfigurationFailure(ITestResult itr)
    {
        // we want to catch failures to configure a test, so we can record
        // logs, screenshots and any information as we do as normal test fails
        // and we skip any other configuration failures as @AfterClass
        Object testInstance = itr.getInstance();
        if (testInstance instanceof AbstractBaseTest
            && itr.getMethod().isBeforeClassConfiguration())
        {
            // record whatever we can
            onTestFailure(itr);

            // make sure if setup fails we will cleanup
            // when configure method @BeforeClass fails @AfterClass is not
            // executed
            ((AbstractBaseTest)testInstance).cleanupClass();
        }
    }

    @Override
    public void onConfigurationSkip(ITestResult itr)
    {}

    /**
     * Takes screenshot of all participants.
     *
     * @param fileName the filename to use.
     * @param participants the participants for whom the screenshots will be
     * taken.
     */
    private void takeScreenshots(
            String fileName,
            List<Participant<? extends WebDriver>> participants)
    {
        participants
            .forEach(
                p -> p.takeScreenshot(
                        getScreenshotsOutputFolder(),
                        fileName + "-" + p.getName() + ".png"));
    }

    /**
     * Saves html sources of participant1 and participant2 in the moment of failure.
     *
     * @param fileName the filename to use.
     * @param participants the participants for whom the HTML sources will be
     * saved.
     */
    private void saveHtmlSources(
            String fileName,
            List<Participant<? extends WebDriver>> participants)
    {
        participants
            .forEach(
                p -> p.saveHtmlSource(
                        outputHtmlSourceParentFolder,
                        fileName + "-" + p.getName() + ".html"));
    }

    /**
     * Saves the log from meet. Normally when clicked it is saved in Downloads
     * if we do not find it we skip it.
     */
    private void saveMeetDebugLog(
            String fileNamePrefix,
            List<Participant<? extends WebDriver>> participants)
    {
        participants
            .forEach(
                p -> saveMeetDebugLog(
                        p,
                        fileNamePrefix + "-meetlog-" + p.getName() + ".json"));
    }

    /**
     * Saves the log from meet. Normally when clicked it is saved in Downloads
     * if we do not find it we skip it.
     */
    private void saveMeetDebugLog(Participant participant, String fileName)
    {
        try
        {
            Logger.getGlobal().info("get debug log for:" + participant.getName());

            String log = participant.getMeetDebugLog();
            if (log != null)
            {
                FileUtils.write(
                    new File(outputLogsParentFolder, fileName), log);
            }
        }
        catch (Exception e)
        {
            Logger.getGlobal()
                .log(
                    Level.SEVERE,
                    "Failed to write meet logs for " + participant.getName(),
                    e);
        }
    }

    /**
     * Saves the rtp stats from meet.
     */
    private void saveMeetRTPStats(
        String fileNamePrefix,
        List<Participant<? extends WebDriver>> participants)
    {
        participants.forEach(p -> saveMeetRTPStats(p, fileNamePrefix + "-rtpstats-" + p.getName() + ".json"));
    }

    /**
     * Saves the rtp stats from meet.
     */
    private void saveMeetRTPStats(Participant participant, String fileName)
    {
        try
        {
            String log = participant.getRTPStats();
            if (log != null)
            {
                FileUtils.write(
                    new File(outputLogsParentFolder, fileName), log);
            }
        }
        catch (Exception e)
        {
            TestUtils.print("Failed to write rtp stats for " + participant.getName() + " :" + e.getMessage());
        }
    }

    /**
     * Saves browser console logs.
     */
    private void saveBrowserLogs(String fileNamePrefix, List<Participant<? extends WebDriver>> participants)
    {
        participants.forEach(p ->
        {
            try
            {
                TestUtils.print("Saving browser logs for:" + p.getName());
                saveBrowserLogs(p, fileNamePrefix, "-console-" + p.getName(), ".log");
            }
            catch(Throwable e)
            {
                TestUtils.print("Error obtaining browser logs for " + p.getName() + ": " + e.getMessage());
            }
        });
    }

    /**
     * Saves current java thread dump.
     */
    private void saveThreadDump(String fileNamePrefix)
    {
        StringBuilder dump = new StringBuilder();
        ThreadMXBean tbean = ManagementFactory.getThreadMXBean();
        ThreadInfo[] threads
            = tbean.getThreadInfo(tbean.getAllThreadIds(), 150);
        for (ThreadInfo tinfo : threads)
        {
            dump.append(tinfo);
            dump.append("\n");
        }

        try
        {
            BufferedWriter out = new BufferedWriter(new FileWriter(
                new File(outputLogsParentFolder,
                    fileNamePrefix + ".tdump")));
            out.write(dump.toString());
            out.flush();
            out.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Saves browser console logs.
     */
    private void saveBrowserLogs(Participant p, String fileNamePrefix, String suffix, String extension)
        throws Exception
    {
        List logs = p.getBrowserLogs();

        if (logs != null)
        {
            File outputFile = new File(outputLogsParentFolder, fileNamePrefix + suffix + "-driver" + extension);

            try (BufferedWriter out = new BufferedWriter(new FileWriter(outputFile)))
            {
                for (Object e : logs)
                {
                    out.write(e.toString());
                    out.newLine();
                }
                out.flush();
            }
        }
    }

    /**
     * Creates the logs folder.
     * @return the logs folder.
     */
    public static String createLogsFolder()
    {
        if (outputLogsParentFolder == null)
        {
            outputLogsParentFolder
                = new File(getReportFolder() + "/logs");

            if (!outputLogsParentFolder.exists()
                    && !outputLogsParentFolder.mkdirs())
            {
                TestUtils.print(
                    "Failed to create output logs parent folder: "
                        + outputLogsParentFolder.toString());
            }
        }

        return outputLogsParentFolder.getAbsolutePath();
    }
}
