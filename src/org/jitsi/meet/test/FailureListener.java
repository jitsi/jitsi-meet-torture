/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jitsi.meet.test;

import junit.framework.*;
import org.apache.commons.io.*;
import org.apache.tools.ant.taskdefs.optional.junit.*;
import org.openqa.selenium.*;

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

}
