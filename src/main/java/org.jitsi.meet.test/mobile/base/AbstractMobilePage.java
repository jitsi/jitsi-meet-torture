/*
 * Copyright @ Atlassian Pty Ltd
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
package org.jitsi.meet.test.mobile.base;

import io.appium.java_client.*;
import io.appium.java_client.pagefactory.*;

import org.openqa.selenium.*;
import org.openqa.selenium.support.*;

import java.util.*;
import java.util.concurrent.*;

/**
 * Base class for mobile page objects.
 */
public class AbstractMobilePage
{
    /**
     * Mobile driver instance.
     */
    protected final AppiumDriver<WebElement> driver;

    /**
     * Initializes with the given mobile driver instance.
     * @param driver <tt>AppiumDriver</tt> must not be <tt>null</tt>.
     */
    public AbstractMobilePage(AppiumDriver<WebElement> driver)
    {
        this.driver = Objects.requireNonNull(driver, "driver");
        this.initFields();
    }

    /**
     * Initializes any member fields annotated with <tt>@AndroidFindBy</tt> or
     * <tt>@iOSFindBy</tt>.
     */
    private void initFields()
    {
        PageFactory.initElements(
                new AppiumFieldDecorator(driver, 10, TimeUnit.SECONDS), this);
    }
}
