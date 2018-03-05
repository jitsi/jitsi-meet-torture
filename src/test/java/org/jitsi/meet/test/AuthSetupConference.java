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

import org.jitsi.meet.test.util.*;
import org.jitsi.meet.test.web.*;

import org.openqa.selenium.*;
import org.testng.annotations.*;

import java.io.*;
import java.util.*;

/**
 * Setup conference with Shibboleth authentication.
 * 1. Clicks "authenticate" button in warning dialog.
 * 2. Redirect to Identity provider selection page.
 * 3. Selects configured identity provider.
 * 3. Redirected to Identity Provider login page.
 * 4. Login and get redirected back to Jitsi-meet room.
 *
 * Redirection is automatically done by Meet and Shibboleth(not by tests).
 *
 * Configuration is done through .properties file specified in
 * {@link #PROPERTIES_FILE_PNAME} system property(-D argument).
 *
 * @author Pawel Domas
 */
public class AuthSetupConference
    extends WebTestBase
{
    /**
     * The name of system property which specifies the name of .properties file
     * which contains authentication properties.
     */
    public static final String PROPERTIES_FILE_PNAME
            = "jitsi-meet.auth.properties";

    /**
     * Base for authentication property names.
     */
    private static final String PNAME_BASE = "org.jitsi.meet.test.auth.";

    /**
     * Property for the id of input element used to enter IdP name.
     */
    public static final String IDP_SELECT_LIST_ID
            = PNAME_BASE + "idp_select_list_id";

    /**
     * Property specifies the name of Shibboleth Identity Provider.
     */
    public static final String IDP_NAME
            = PNAME_BASE + "idp_name";

    /**
     * Property for the name of submit button which confirms IdP name.
     */
    public static final String IDP_SUBMIT_BTN_NAME
            = PNAME_BASE + "idp_submit_btn_name";

    /**
     * Property for the name of username input field on IdP login page.
     */
    public static final String USERNAME_INPUT_ID
            = PNAME_BASE + "username_input_id";

    /**
     * The property specifies the name of password input field on IdP login
     * page.
     */
    public static final String PASSWORD_INPUT_ID
            = PNAME_BASE + "password_input_id";

    /**
     * The property specifies username used to login.
     */
    public static final String USERNAME = PNAME_BASE + "username";

    /**
     * The property which specifies user password.
     */
    public static final String PASSWORD = PNAME_BASE + "password";

    /**
     * Property specifies name of the login button used to confirm
     * the credentials.
     */
    public static final String LOGIN_BTN_NAME = PNAME_BASE + "login_btn_name";

    @Override
    public boolean skipTestByDefault()
    {
        return true;
    }

    @Test
    public void authenticate()
    {
        String authenticationProperties
            = System.getProperty(PROPERTIES_FILE_PNAME);

        if (authenticationProperties == null
                || authenticationProperties.isEmpty())
        {
            throw new RuntimeException(
                    "Authentication properties filename " +
                            "'jitsi-meet.auth.properties' not configured");
        }

        // Identity Provider selection page
        String idpSelectListId;
        String idpName;
        String idpSubmitBtnName;

        // Identity provider login page
        String usernameInputId;
        String passwordInputId;
        String loginBtnName;
        String username;
        String password;

        try
        {
            Properties properties
                = PropertiesUtils.loadPropertiesFile(authenticationProperties);

            properties.list(System.out);

            idpSelectListId = properties.getProperty(IDP_SELECT_LIST_ID);
            idpName = properties.getProperty(IDP_NAME);
            idpSubmitBtnName = properties.getProperty(IDP_SUBMIT_BTN_NAME);

            usernameInputId = properties.getProperty(USERNAME_INPUT_ID);
            passwordInputId = properties.getProperty(PASSWORD_INPUT_ID);
            loginBtnName = properties.getProperty(LOGIN_BTN_NAME);
            username = properties.getProperty(USERNAME);
            password = properties.getProperty(PASSWORD);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }

        WebDriver participant1 = joinFirstParticipant().getDriver();

        String authButtonName = "jqi_state0_" +
                "buttonspandatai18ndialogIamHostIamthehostspan";

        TestUtils.waitForElementByXPath(
            participant1, "//button[@name='" + authButtonName + "']", 15);

        WebElement authButton
            = participant1.findElement(By.name(authButtonName));

        authButton.click();

        // Redirected to Idp selection
        shibbolethIdpSelect(
            participant1, idpSelectListId, idpName, idpSubmitBtnName);

        // IdpLogin
        shibbolethIdpLogin(
            participant1, usernameInputId, passwordInputId, loginBtnName,
            username,  password);
    }

    private void shibbolethIdpSelect(
        WebDriver driver,
        String idpSelectListId,
        String idpName,
        String idSubmitBtnName)
    {
        TestUtils.waitForElementByXPath(
            driver,
            "//input[@id='" + idpSelectListId + "']",
            15);

        WebElement idpInput = driver.findElement(By.id(idpSelectListId));
        idpInput.sendKeys(idpName);

        WebElement submit = driver.findElement(By.name(idSubmitBtnName));
        submit.click();
    }

    private void shibbolethIdpLogin(
        WebDriver driver,
        String usernameInputId,
        String passwordInputId,
        String loginBtnName,
        String username,
        String password)
    {
        TestUtils.waitForElementByXPath(
            driver,
            "//input[@id='" + usernameInputId + "']",
            15);

        WebElement usernameElem
            = driver.findElement(By.id(usernameInputId));
        WebElement passwordElem
            = driver.findElement(By.id(passwordInputId));

        usernameElem.sendKeys(username);

        passwordElem.sendKeys(password);

        WebElement submit = driver.findElement(By.name(loginBtnName));

        submit.click();
    }
}
