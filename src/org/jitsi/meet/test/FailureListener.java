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
import org.openqa.selenium.*;

import org.jitsi.meet.test.util.*;

import java.io.*;

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
     * Creates a screenshot named by the class and name of the failure.
     * @param test the test
     * @param t the assertion error
     */
    @Override
    public void addFailure(Test test, AssertionFailedError t)
    {
        try
        {
            takeScreenshots(test);

            saveHtmlSources(test);
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
        try
        {
            takeScreenshots(test);

            saveHtmlSources(test);

            saveMeetDebugLog();
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

        // skip output so we do not print in console
        //super.setOutput(out);
    }

    /**
     * Takes screenshot of owner and participant.
     *
     * @param test which failed
     */
    private void takeScreenshots(Test test)
    {
        takeScreenshot(ConferenceFixture.getOwner(),
            JUnitVersionHelper.getTestCaseClassName(test)
                + "." +JUnitVersionHelper.getTestCaseName(test)
                + "-owner.png");

        WebDriver secondParticipant =
            ConferenceFixture.getSecondParticipantInstance();

        if(secondParticipant != null)
            takeScreenshot(secondParticipant,
                JUnitVersionHelper.getTestCaseClassName(test)
                    + "." + JUnitVersionHelper.getTestCaseName(test)
                    + "-participant.png");
    }

    /**
     * Takes screenshot for the supplied page.
     * @param driver the driver controlling the page.
     * @param fileName the destination screenshot file name.
     */
    private void takeScreenshot(WebDriver driver, String fileName)
    {
        TakesScreenshot takesScreenshot = (TakesScreenshot) driver;

        File scrFile = takesScreenshot.getScreenshotAs(OutputType.FILE);
        File destFile = new File(outputScreenshotsParentFolder, fileName);
        try
        {
            //System.err.println("Took screenshot " + destFile);
            FileUtils.copyFile(scrFile, destFile);
            //System.err.println("Saved screenshot " + destFile);
        } catch (IOException ioe)
        {
            throw new RuntimeException(ioe);
        }
    }

    /**
     * Saves html sources of owner and participant in the moment of failure.
     *
     * @param test which failed
     */
    private void saveHtmlSources(Test test)
    {
        saveHtmlSource(ConferenceFixture.getOwner(),
            JUnitVersionHelper.getTestCaseClassName(test)
                + "." + JUnitVersionHelper.getTestCaseName(test)
                + "-owner.html");

        WebDriver secondParticipant =
            ConferenceFixture.getSecondParticipantInstance();

        if(secondParticipant != null)
            saveHtmlSource(secondParticipant,
                JUnitVersionHelper.getTestCaseClassName(test)
                    + "." + JUnitVersionHelper.getTestCaseName(test)
                    + "-participant.html");
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
    private void saveMeetDebugLog()
    {
        saveMeetDebugLog(ConferenceFixture.getOwner(), "meetlog-owner.json");

        WebDriver secondParticipant =
            ConferenceFixture.getSecondParticipantInstance();

        if(secondParticipant != null)
        {
            saveMeetDebugLog(secondParticipant, "meetlog-participant.json");
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
            WebElement el = driver.findElement(By.id("downloadlog"));
            el.click();

            TestUtils.waits(1000);

            FileUtils.moveFile(
                new File(
                    new File(System.getProperty("user.home"), "Downloads"),
                    "meetlog.json"),
                new File(outputHtmlSourceParentFolder, fileName));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
