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
import org.openqa.selenium.logging.*;
import org.testng.*;

import java.io.*;
import java.lang.management.*;
import java.util.*;
import java.util.logging.*;

public class FailureListener
    implements ITestListener
{
    /**
     * The folder where the screenshost will be saved.
     */
    private File outputScreenshotsParentFolder = null;

    /**
     * The folder where the htmls will be saved.
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
     * The folder where the logs will be saved.
     */
    private static File outputLogsParentFolder = null;

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
        outputScreenshotsParentFolder
            = new File(getReportFolder() + "/screenshots");
        outputHtmlSourceParentFolder
            = new File(getReportFolder() + "/html-sources");
        outputHtmlSourceParentFolder.mkdirs();

        createLogsFolder();
    }

    @Override
    public void onFinish(ITestContext iTestContext)
    {}

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

        try
        {
            AbstractBaseTest testInstance
                = (AbstractBaseTest)testResult.getInstance();

            String fileNamePrefix
                = testResult.getTestClass().getRealClass().getCanonicalName();

            takeScreenshots(fileNamePrefix, testInstance);

            saveHtmlSources(fileNamePrefix, testInstance);

            saveMeetDebugLog(fileNamePrefix, testInstance);

            saveBrowserLogs(fileNamePrefix, testInstance);

            saveThreadDump(fileNamePrefix);
        }
        catch(Throwable ex)
        {
            ex.printStackTrace();
        }
    }

    /**
     * Takes screenshot of all participants.
     *
     * @param fileName the filename to use.
     * @param participantHelper The participant helper which holds participant
     * instances.
     */
    private void takeScreenshots(
            String fileName, AbstractParticipantHelper participantHelper)
    {
        participantHelper
            .getAllParticipants()
            .forEach(
                p -> p.takeScreenshot(
                        outputScreenshotsParentFolder,
                        fileName + "-" + p.getName() + ".png"));
    }

    /**
     * Saves html sources of owner and participant in the moment of failure.
     *
     * @param fileName the filename to use.
     */
    private void saveHtmlSources(
            String fileName, AbstractParticipantHelper participantHelper)
    {
        participantHelper
            .getAllParticipants()
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
        String fileNamePrefix, AbstractParticipantHelper participantHelper)
    {
        participantHelper
            .getAllParticipants()
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
     * Saves browser console logs.
     */
    private void saveBrowserLogs(
        String fileNamePrefix, AbstractParticipantHelper participantHelper)
    {
        participantHelper
            .getAllParticipants()
            .forEach(
                p -> saveBrowserLogs(
                    p,
                    fileNamePrefix,
                    "-console-" + p.getName(),
                    ".log"));
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
    private void saveBrowserLogs(
            Participant p, String fileNamePrefix,
            String suffix, String extension)
    {
        try
        {
            LogEntries logs = p.getBrowserLogs();

            if (logs != null)
            {
                File outputFile
                    = new File(
                            outputLogsParentFolder,
                            fileNamePrefix + suffix + "-driver" + extension);

                try (BufferedWriter out
                         = new BufferedWriter(
                                new FileWriter(outputFile)))
                {
                    for (LogEntry e : logs)
                    {
                        out.write(e.toString());
                        out.newLine();
                        out.newLine();
                    }
                    out.flush();
                    out.close();
                }
            }

            //
            File srcFile = p.getChromeWebDriverLogFile();
            if (srcFile != null)
            {
                FileUtils.copyFile(
                    srcFile,
                    new File(outputLogsParentFolder,
                        fileNamePrefix + suffix + "-chrome" + extension));
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
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
            outputLogsParentFolder = new File(getReportFolder() + "/logs");
            outputLogsParentFolder.mkdirs();
        }

        return outputLogsParentFolder.getAbsolutePath();
    }
}
