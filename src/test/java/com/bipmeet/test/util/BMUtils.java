package com.bipmeet.test.util;

import org.jitsi.meet.test.base.Participant;
import org.jitsi.meet.test.util.TestUtils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.By;

import java.util.Properties;

/**
 *  Class contains utility methods related with bip-meet application logic.
 */
public class BMUtils {


    public static final String ROOM_NAME_PREFIX_PNAME = "bip-meet.room_name";

    private static final String BIPMEET_SERVER_URL = "bipmeet.com";

    /**
     * If server is bip-meet instance, passes 3 selection page by continuing with browser
     *
     * @param participant
     */
    public static void clickContinueOnBrowserButton(Participant participant) {

        // Do this action only on bip-meet platform
        if(participant.getMeetUrl().getServerUrl().contains(BIPMEET_SERVER_URL) == false) {
            return;
        }

        WebDriver driver = participant.getDriver();
        TestUtils.click(driver,
                By.xpath("//span[text()=\"Continue on this browser\"]")
        );
    }

    /**
     * Sets participants displayName from set-username modal dialog
     *
     * @param participant
     * @param displayName
     */
    public static void setDisplayName(Participant participant, String displayName) {

        WebDriver driver = participant.getDriver();

        if (displayName == null) {
            displayName = System.getProperty("user.name");
        }

        String nameTextXPath = "//*[@id=\"modal-dialog-form\"]/div/div[3]/div/div/div/div/input";

        // fill in the dialog
        TestUtils.waitForElementByXPath(
                driver, nameTextXPath, 5);
        driver.findElement(By.xpath(nameTextXPath))
                .sendKeys(displayName);

        driver
                .findElement(By
                        .xpath("//*[@id=\"modal-dialog-ok-button\"]"))
                .click();
    }

    /**
     * Checks if conference room-name given as a config
     *
     * @param oldName generated random room name
     * @param config properties source for participant's configs
     * @return room name for conference
     */
    public static String getRoomName(String oldName, Properties config) {
        String newName = config.getProperty(ROOM_NAME_PREFIX_PNAME);
        if(newName != null && !newName.isEmpty())
            return newName;

        return oldName;
    }
}
