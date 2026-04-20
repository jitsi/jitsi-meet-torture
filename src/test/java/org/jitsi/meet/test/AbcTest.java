package org.jitsi.meet.test;

import org.jitsi.meet.test.web.WebTestBase;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.Assert;
import org.testng.annotations.Test;
import java.net.URL;

public class AbcTest extends WebTestBase {

    @Test
    public void testSeleniumGrid() throws Exception {
        // Selenium Grid hub URL
        String hubURL = System.getProperty("hub", "http://localhost:4444/wd/hub");

        // Set up Chrome capabilities
        ChromeOptions options = new ChromeOptions();
        options.setCapability(CapabilityType.BROWSER_NAME, "chrome");

        // Create RemoteWebDriver instance
        WebDriver driver = new RemoteWebDriver(new URL(hubURL), options);

        // Add your test logic here
        driver.get("http://selenium.dev");
        Assert.assertEquals(driver.getTitle(), "Selenium");

        // Close the driver after the test
        driver.quit();
    }
}
