/*
 * Copyright @ 2018 8x8 Pty Ltd
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
package org.jitsi.meet.test.util;

import org.openqa.selenium.*;

import java.io.*;
import java.util.*;

/**
 * Finds element by testId.
 */
public class ByTestId
    extends By
    implements Serializable
{
    private static final long serialVersionUID = 3911294438564637665L;

    private final String testId;

    private ByTestId(String testId)
    {
        if (testId == null)
        {
            throw new IllegalArgumentException("Cannot find elements when the testId is null.");
        }

        this.testId = testId;
    }

    /**
     * @param testId The value of the "data-testid" attribute to search for.
     * @return A By which locates elements by the value of the "data-testid" attribute.
     */
    public static By testId(String testId)
    {
        return new ByTestId(testId);
    }

    @Override
    public List<WebElement> findElements(SearchContext context)
    {
        return context.findElements(By.xpath(".//*[@data-testid = '" + testId + "']"));
    }

    @Override
    public WebElement findElement(SearchContext context)
    {
        return context.findElement(By.xpath(".//*[@data-testid = '" + testId + "']"));
    }

    @Override
    public String toString()
    {
        return "By.testId: " + testId;
    }
}
