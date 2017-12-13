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

import junit.framework.*;
import org.apache.commons.io.*;
import org.apache.tools.ant.taskdefs.optional.junit.*;
import org.jitsi.meet.test.util.*;
import org.openqa.selenium.*;

import org.openqa.selenium.logging.*;

import java.io.*;
import java.lang.management.*;
import java.util.*;

/**
 * Extends the xml formatter so we can detect failures and make screenshots
 * when this happen.
 * @author Damian Minkov
 */
public class FailureListener
    extends XMLJUnitResultFormatter
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
     * The folder where the logs will be saved.
     */
    private static File outputLogsParentFolder = null;

    /**
     * Creates a screenshot named by the class and name of the failure.
     * @param test the test
     * @param t the assertion error
     */
    @Override
    public void addFailure(Test test, AssertionFailedError t)
    {
        if(t != null)
        {
            System.err.println("Failure:");
            t.printStackTrace();
        }

        try
        {
            String fileNamePrefix
                = JUnitVersionHelper.getTestCaseClassName(test)
                    + "." + JUnitVersionHelper.getTestCaseName(test);

            takeScreenshots(test);

            saveHtmlSources(test);

            saveMeetDebugLog(fileNamePrefix);

            saveBrowserLogs(fileNamePrefix);

            saveThreadDump(fileNamePrefix);
        }
        catch(Throwable ex)
        {
            ex.printStackTrace();
        }

        super.addFailure(test, t);
    }

    /**
     * Creates a screenshot named by the class and name of the failure.
     * @param test the test
     * @param t the assertion error
     */
    @Override
    public void addError(Test test, Throwable t)
    {
        if(t != null)
        {
            System.err.println("Error:");
            t.printStackTrace();
        }

        try
        {
            String fileNamePrefix
                = JUnitVersionHelper.getTestCaseClassName(test)
                + "." + JUnitVersionHelper.getTestCaseName(test);

            takeScreenshots(test);

            saveHtmlSources(test);

            saveMeetDebugLog(fileNamePrefix);

            saveBrowserLogs(fileNamePrefix);

            saveThreadDump(fileNamePrefix);
        }
        catch(Throwable ex)
        {
            ex.printStackTrace();
        }

        super.addError(test, t);
    }

    /**
     * Override to access parent output to get the destination folder.
     * @param out the xml formatter output
     */
    @Override
    public void setOutput(OutputStream out)
    {
        // default reports folder
        outputScreenshotsParentFolder = new File("test-reports/screenshots");

        outputHtmlSourceParentFolder = new File("test-reports/html-sources");
        outputHtmlSourceParentFolder.mkdirs();

        createLogsFolder();

        // skip output so we do not print in console
        //super.setOutput(out);
    }

    /**
     * Creates the logs folder.
     * @return the logs folder.
     */
    public static String createLogsFolder()
    {
        if(outputLogsParentFolder == null)
        {
            outputLogsParentFolder = new File("test-reports/logs");
            outputLogsParentFolder.mkdirs();
        }

        return outputLogsParentFolder.getAbsolutePath();
    }

    /**
     * Takes screenshot of owner and participant.
     *
     * @param test which failed
     */
    private void takeScreenshots(Test test)
    {
        String fileName = JUnitVersionHelper.getTestCaseClassName(test)
            + "." + JUnitVersionHelper.getTestCaseName(test);

        takeScreenshot(ConferenceFixture.getOwnerInstance(),
            fileName + "-owner.png");

        WebDriver secondParticipant =
            ConferenceFixture.getSecondParticipantInstance();

        if(secondParticipant != null)
            takeScreenshot(secondParticipant,
                fileName + "-participant.png");

        WebDriver thirdParticipant =
            ConferenceFixture.getThirdParticipantInstance();

        if(thirdParticipant != null)
            takeScreenshot(thirdParticipant,
                fileName + "-third.png");
    }

    /**
     * Takes screenshot for the supplied page.
     * @param driver the driver controlling the page.
     * @param fileName the destination screenshot file name.
     */
    private void takeScreenshot(WebDriver driver, String fileName)
    {
        SeleniumUtil.takeScreenshot(
                (TakesScreenshot) driver,
                outputHtmlSourceParentFolder.getAbsolutePath(),
                fileName);
    }

    /**
     * Saves html sources of owner and participant in the moment of failure.
     *
     * @param test which failed
     */
    private void saveHtmlSources(Test test)
    {
        String fileName = JUnitVersionHelper.getTestCaseClassName(test)
            + "." + JUnitVersionHelper.getTestCaseName(test);

        saveHtmlSource(ConferenceFixture.getOwnerInstance(),
            fileName + "-owner.html");

        WebDriver secondParticipant =
            ConferenceFixture.getSecondParticipantInstance();

        if(secondParticipant != null)
            saveHtmlSource(secondParticipant,
                fileName + "-participant.html");

        WebDriver thirdParticipant =
            ConferenceFixture.getThirdParticipantInstance();

        if(thirdParticipant != null)
            saveHtmlSource(thirdParticipant,
                fileName + "-third.html");
    }

    /**
     * Saves the html source of the supplied page.
     * @param driver the driver controlling the page.
     * @param fileName the destination html file name.
     */
    private void saveHtmlSource(WebDriver driver, String fileName)
    {
        try
        {
            FileUtils.openOutputStream(
                new File(outputHtmlSourceParentFolder, fileName))
                .write(driver.getPageSource().getBytes());
        }
        catch(Throwable t)
        {
            t.printStackTrace();
        }
    }

    /**
     * Saves the log from meet. Normally when clicked it is saved in Downloads
     * if we do not find it we skip it.
     */
    private void saveMeetDebugLog(String fileNamePrefix)
    {
        saveMeetDebugLog(ConferenceFixture.getOwner(),
            fileNamePrefix + "-meetlog-owner.json");

        WebDriver secondParticipant =
            ConferenceFixture.getSecondParticipantInstance();

        if(secondParticipant != null)
        {
            saveMeetDebugLog(secondParticipant,
                fileNamePrefix + "-meetlog-participant.json");
        }

        WebDriver thirdParticipant =
            ConferenceFixture.getThirdParticipantInstance();

        if(thirdParticipant != null)
        {
            saveMeetDebugLog(thirdParticipant,
                fileNamePrefix + "-meetlog-third.json");
        }
    }

    /**
     * Saves the log from meet. Normally when clicked it is saved in Downloads
     * if we do not find it we skip it.
     */
    private void saveMeetDebugLog(WebDriver driver, String fileName)
    {
        try
        {
            Object log = ((JavascriptExecutor) driver)
                .executeScript(
            "try{ "
            + "return JSON.stringify(APP.conference.getLogs(), null, '    ');"
            + "}catch (e) {}");

            if(log == null)
                return;

            FileUtils.write(
                new File(outputLogsParentFolder, fileName),
                (String)log);
        }
        catch (Exception e)
        {
            //e.printStackTrace();
        }
    }

    /**
     * Saves browser console logs.
     */
    private void saveBrowserLogs(String fileNamePrefix)
    {
        saveBrowserLogs(ConferenceFixture.getOwner(),
            fileNamePrefix, "-console-owner", ".log");

        WebDriver secondParticipant =
            ConferenceFixture.getSecondParticipantInstance();
        if(secondParticipant != null)
            saveBrowserLogs(secondParticipant,
                fileNamePrefix, "-console-secondParticipant", ".log");

        WebDriver thirdParticipant =
            ConferenceFixture.getThirdParticipantInstance();
        if(thirdParticipant != null)
            saveBrowserLogs(thirdParticipant,
                fileNamePrefix, "-console-thirdParticipant", ".log");
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
        catch (IOException e){}
    }

    /**
     * Saves browser console logs.
     */
    private void saveBrowserLogs(WebDriver driver,
        String fileNamePrefix, String suffix, String extension)
    {
        try
        {
            LogEntries logs = driver.manage().logs().get(LogType.BROWSER);

            BufferedWriter out = new BufferedWriter(new FileWriter(
                new File(outputLogsParentFolder,
                        fileNamePrefix + suffix + "-driver" + extension)));

            Iterator<LogEntry> iter = logs.iterator();
            while (iter.hasNext())
            {
                LogEntry e = iter.next();

                out.write(e.toString());
                out.newLine();
                out.newLine();
            }
            out.flush();
            out.close();

            if (ConferenceFixture.getBrowserType(driver)
                == ConferenceFixture.BrowserType.chrome)
            {
                FileUtils.copyFile(
                    new File(outputLogsParentFolder,
                            "chrome" + suffix + ".log"),
                    new File(outputLogsParentFolder,
                            fileNamePrefix + suffix + "-chrome" + extension)
                    );
            }
        }
        catch (IOException e)
        {
            // cannot create file or something
        }
    }
}
