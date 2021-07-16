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
package org.jitsi.meet.test.pageobjects.mobile.base;

import io.appium.java_client.pagefactory.*;

import org.jitsi.meet.test.mobile.*;
import org.jitsi.meet.test.pageobjects.base.*;
import org.openqa.selenium.support.*;

import java.lang.reflect.*;
import java.time.*;
import java.util.*;

/**
 * Base class for mobile page objects.
 */
public class AbstractMobilePage
{
    /**
     * Mobile driver instance.
     */
    protected final MobileParticipant participant;

    /**
     * Initializes with the given mobile driver instance.
     * @param participant <tt>MobileParticipant</tt> must not be <tt>null</tt>.
     */
    public AbstractMobilePage(MobileParticipant participant)
    {
        this.participant = Objects.requireNonNull(participant, "participant");
        this.initFields();
    }

    /**
     * Initializes any member fields annotated with <tt>@AndroidFindBy</tt> or
     * <tt>@iOSFindBy</tt>.
     */
    private void initFields()
    {
        PageFactory.initElements(
                new AppiumFieldDecorator(
                        participant.getDriver(), Duration.ofSeconds(10)),
                this);
        initTestHintFields();
    }

    /**
     * Goes through the class hierarchy and try to initialise {@link TestHint}
     * fields.
     */
    private void initTestHintFields()
    {
        for (Class classIn = getClass();
            classIn != Object.class;
            classIn = classIn.getSuperclass())
        {
            initTestHintFields(classIn);
        }
    }

    private void initTestHintFields(Class classIn)
    {
        for (Field field : classIn.getDeclaredFields())
        {
            TestHintLocator[] locators
                = field.getAnnotationsByType(TestHintLocator.class);
            TestHintLocator locator = locators.length > 0 ? locators[0] : null;

            if (locator != null)
            {
                String id = locator.id();
                try
                {
                    field.setAccessible(true);
                    field.set(this, new TestHint(participant.getDriver(), id));
                }
                catch (IllegalAccessException e)
                {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
