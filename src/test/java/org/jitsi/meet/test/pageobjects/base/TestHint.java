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
package org.jitsi.meet.test.pageobjects.base;

import io.appium.java_client.*;

import org.openqa.selenium.*;

/**
 * Class is a direct mapping to the corresponding component in jitsi-meet
 * features/test/components/TestHint. It is a way for Jitsi Meet to
 * communicate some of the app's internal state to the torture, which is not
 * normally expressed by the UI. On the Jitsi Meet side a (text) value will be
 * put under a key which is then obtained here.
 *
 * The class also deals with some differences on how keys and values are being
 * feed by the react-native to the accessibility UI layer. In particular it
 * deals with the fact that there's no consistent way for defining keys and
 * values on both Android and iOS platforms. More info on the cause can be found
 * in the TestHint's component description in Jitsi Meet.
 */
public class TestHint
{
    /**
     * The mobile participant's driver.
     */
    private final AppiumDriver<MobileElement> driver;

    /**
     * The test hint's id (key).
     */
    private final String id;

    /**
     * Creates new {@code TestHint}.
     *
     * @param driver - The driver which will be used to search for elements.
     * @param id - The test hint's id (key) which identifies a hint in the app's
     * scope.
     */
    public TestHint(AppiumDriver<MobileElement> driver, String id)
    {
        this.driver = driver;
        this.id = id;
    }

    /**
     * Clicks on the {@link TestHint} which should execute the 'onPress' handler
     * bound on the JavaScript side.
     */
    public void click()
    {
        getElement().click();
    }

    /**
     * Finds the underlying {@link MobileElement} for this {@link TestHint}
     * instance.
     *
     * @return {@link MobileElement}
     * @throws NoSuchElementException if {@link MobileElement} could not be
     * found on the current screen.
     */
    private MobileElement getElement()
    {
        return useAccessibilityForId()
            ? driver.findElementByAccessibilityId(id)
            : driver.findElementById(id);
    }

    /**
     * Returns the current value of this test hint instance.
     *
     * @return a text value this {@link TestHint} or <tt>null</tt> if
     * the underlying UI element can not be found.
     *
     * @throws NoSuchElementException if the underlying UI element is not found
     */
    public String getValue()
    {
        return getElement().getText();
    }

    /**
     * On Android the {@link TestHint}'s key is defined in
     * the react-native's 'accessibilityLabel', but on iOS the react-native's
     * 'testID' attribute is used. That's because 'testID' does not work as
     * expected on Android and the 'accessibilityLabel' works in a different way
     * on iOS than on Android...
     *
     * @return {@code true} if {@link TestHint} should be located by
     * the accessibility id or {@code false} if the id based search should be
     * used.
     */
    private boolean useAccessibilityForId()
    {
        Platform driversPlatform = driver.getCapabilities().getPlatform();

        // FIXME This looks like a bug, but I don't want to deal with this right
        // now.
        // Not sure if it's only when running appium from source or does it
        // sometimes report Android as LINUX platform. Also iOS is translated to
        // MAC ???
        return driversPlatform.is(Platform.ANDROID)
            || driversPlatform.is(Platform.LINUX);
    }
}
