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
import org.openqa.selenium.*;
import org.openqa.selenium.logging.*;
import org.testng.*;

import java.io.*;
import java.lang.management.*;
import java.util.*;

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
            = new File("target/surefire-reports/screenshots");

        outputHtmlSourceParentFolder
            = new File("target/surefire-reports/html-sources");
        outputHtmlSourceParentFolder.mkdirs();

        createLogsFolder();
    }

    @Override
    public void onFinish(ITestContext iTestContext)
    {}

    @Override
    public void onTestFailure(ITestResult testResult)
    {
        if(testResult.getThrowable() != null)
        {
            System.err.println("TestFailure:");
            testResult.getThrowable().printStackTrace();
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
     * Takes screenshot of owner and participant.
     *
     * @param fileName the filename to use.
     */
    private void takeScreenshots(
        String fileName, AbstractBaseTest testInstance)
    {
        takeScreenshot(testInstance.participant1.getDriver(),
            fileName + "-owner.png");

        if(testInstance.participant2 != null)
            takeScreenshot(testInstance.participant2.getDriver(),
                fileName + "-participant.png");

        if(testInstance.participant3 != null)
            takeScreenshot(testInstance.participant3.getDriver(),
                fileName + "-third.png");
    }

    /**
     * Takes screenshot for the supplied page.
     * @param driver the driver controlling the page.
     * @param fileName the destination screenshot file name.
     */
    private void takeScreenshot(WebDriver driver, String fileName)
    {
        TakesScreenshot takesScreenshot = (TakesScreenshot) driver;

        if(takesScreenshot == null)
        {
            System.err.println("No driver to take screenshot from! FileName:"
                + fileName);
            return;
        }

        File scrFile = takesScreenshot.getScreenshotAs(OutputType.FILE);
        File destFile = new File(outputScreenshotsParentFolder, fileName);
        try
        {
            FileUtils.copyFile(scrFile, destFile);
        } catch (IOException ioe)
        {
            throw new RuntimeException(ioe);
        }
    }

    /**
     * Saves html sources of owner and participant in the moment of failure.
     *
     * @param fileName the filename to use.
     */
    private void saveHtmlSources(String fileName, AbstractBaseTest testInstance)
    {
        saveHtmlSource(testInstance.participant1.getDriver(),
            fileName + "-owner.html");

        if(testInstance.participant2 != null)
            saveHtmlSource(testInstance.participant2.getDriver(),
                fileName + "-participant.html");

        if(testInstance.participant3 != null)
            saveHtmlSource(testInstance.participant3.getDriver(),
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
    private void saveMeetDebugLog(
        String fileNamePrefix, AbstractBaseTest testInstance)
    {
        saveMeetDebugLog(testInstance.participant1.getDriver(),
            fileNamePrefix + "-meetlog-owner.json");

        if(testInstance.participant2 != null)
        {
            saveMeetDebugLog(testInstance.participant2.getDriver(),
                fileNamePrefix + "-meetlog-participant.json");
        }

        if(testInstance.participant3 != null)
        {
            saveMeetDebugLog(testInstance.participant3.getDriver(),
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
    private void saveBrowserLogs(
        String fileNamePrefix, AbstractBaseTest testInstance)
    {
        saveBrowserLogs(
            testInstance.participant1.getDriver(),
            fileNamePrefix, "-console-owner", ".log",
            testInstance.participant1.getType());

        if(testInstance.participant2 != null)
            saveBrowserLogs(
                testInstance.participant2.getDriver(),
                fileNamePrefix, "-console-secondParticipant", ".log",
                testInstance.participant2.getType());

        if(testInstance.participant3 != null)
            saveBrowserLogs(
                testInstance.participant3.getDriver(),
                fileNamePrefix, "-console-thirdParticipant", ".log",
                testInstance.participant3.getType());
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
        String fileNamePrefix, String suffix, String extension,
        ParticipantFactory.ParticipantType type)
    {
        try
        {
            if (type == ParticipantFactory.ParticipantType.firefox)
            {
                // not currently supported in FF
                // https://github.com/SeleniumHQ/selenium/issues/2910
                return;
            }

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

            if (type == ParticipantFactory.ParticipantType.chrome)
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

    /**
     * Creates the logs folder.
     * @return the logs folder.
     */
    public static String createLogsFolder()
    {
        if(outputLogsParentFolder == null)
        {
            outputLogsParentFolder = new File("target/surefire-reports/logs");
            outputLogsParentFolder.mkdirs();
        }

        return outputLogsParentFolder.getAbsolutePath();
    }
}
