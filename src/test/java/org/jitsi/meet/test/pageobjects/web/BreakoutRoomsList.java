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
package org.jitsi.meet.test.pageobjects.web;

import org.jitsi.meet.test.util.*;
import org.jitsi.meet.test.web.*;
import org.openqa.selenium.*;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.interactions.*;

import java.util.*;
import java.util.logging.*;

/**
 * Represents the list of breakout rooms.
 */
public class BreakoutRoomsList
{
    /**
     * The id of the breakout rooms list element.
     */
    private final static String BREAKOUT_ROOMS_LIST_ID = "breakout-rooms-list";

    /**
     * Class name of breakout rooms.
     */
    private final static String BREAKOUT_ROOMS_CLASS = "breakout-room-container";

    /**
     * The participant used to interact with the breakout rooms list.
     */
    private final WebParticipant participant;

    /**
     * Initializes a new {@link BreakoutRoomsList} instance.
     *
     * @param participant the participant for this {@link BreakoutRoomsList}.
     */
    public BreakoutRoomsList(WebParticipant participant)
    {
        this.participant = participant;
    }

    public int getRoomsCount()
    {
        try
        {
            TestUtils.waitForDisplayedElementByID(participant.getDriver(), BREAKOUT_ROOMS_LIST_ID, 5);
        }
        catch(TimeoutException ex)
        {
        // if the list is missing return empty list of rooms
        Logger.getGlobal().log(Level.WARNING, "No breakout rooms");
        return 0;
    }
        return participant.getDriver().findElements(By.className(BREAKOUT_ROOMS_CLASS)).size();
    }

    public List<BreakoutRoom> getRooms()
 {
    List<BreakoutRoom> rooms = new ArrayList<>();

    try
    {
        TestUtils.waitForDisplayedElementByID(participant.getDriver(), BREAKOUT_ROOMS_LIST_ID, 5);
    }
    catch(TimeoutException ex)
    {
        // if the list is missing return empty list of rooms
        Logger.getGlobal().log(Level.WARNING, "No breakout rooms list ");
        return rooms;
    }

    List<WebElement> listElements = participant.getDriver().findElements(By.className(BREAKOUT_ROOMS_CLASS));

    listElements.forEach(el -> {
        rooms.add(new BreakoutRoom(el.findElement(By.tagName("span")).getText(), el.getAttribute("data-testid")));
    });

    return rooms;
}

    public List<BreakoutRoom> getBipMeetBreakoutRooms()
    {
        List<BreakoutRoom> rooms = new ArrayList<>();

        try
        {
            TestUtils.waitForDisplayedElementByID(participant.getDriver(), BREAKOUT_ROOMS_LIST_ID, 5);
        }
        catch(TimeoutException ex)
        {
            // if the list is missing return empty list of rooms
            Logger.getGlobal().log(Level.WARNING, "No breakout rooms list ");
            return rooms;
        }

        List<WebElement> listElements = participant.getDriver().findElements(By.className(BREAKOUT_ROOMS_CLASS));

        listElements.forEach(el -> {
            rooms.add(new BreakoutRoom(el.findElement(By.tagName("span")).getText(), el.getAttribute("data-testid")));
        });

        return rooms;
    }

    public void removeRooms()
    {
        getRooms().stream().forEach(r -> r.removeRoom());
    }

    public class BreakoutRoom
    {
        private final static String MORE_LABEL="breakoutRooms.actions.more";
        private final String title;
        private final String id;
        private final int count;

        public BreakoutRoom(String title, String id)
        {
            this.title = title;
            this.id = id;
            this.count = getCount();
        }

        public String getId()
        {
            return id;
        }

        public int getParticipantsCount()
        {
            return count;
        }

        public String getName()
        {
            return title.split(":")[0];
        }

        private int getCount()
        {
            String count = this.title.split(": ")[1];
            return Integer.parseInt(count);
        }

        public void collapse()
        {
            WebElement listItem = TestUtils.waitForElementBy(participant.getDriver(),
                    By.xpath("//div[@data-testid='" + id + "']"), 5);
            listItem.click();
        }

        public void joinRoom()
        {
            WebElement listItem = TestUtils.waitForElementBy(participant.getDriver(),
                    By.xpath("//div[@data-testid='" + id + "']"), 5);

            Actions hoverOnBreakoutRoomListItem = new Actions(participant.getDriver());
            hoverOnBreakoutRoomListItem.moveToElement(listItem);
            hoverOnBreakoutRoomListItem.perform();

            WebElement joinButton = TestUtils.waitForElementBy(participant.getDriver(),
                    ByTestId.testId("join-room-" + id), 2);

            joinButton.click();
        }

        private void openContextMenu()
        {
            WebElement listItem = TestUtils.waitForElementBy(participant.getDriver(),
                    By.xpath("//div[@data-testid='" + id + "']"), 5);

            Actions hoverOnBreakoutRoomListItem = new Actions(participant.getDriver());
            hoverOnBreakoutRoomListItem.moveToElement(listItem);
            hoverOnBreakoutRoomListItem.perform();

            String cssSelector = MeetUIUtils.getAccessibilityCSSSelector(MORE_LABEL);
            listItem.findElement(By.cssSelector(cssSelector)).click();
        }

        public void removeRoom()
        {
            openContextMenu();
            WebElement removeButton = TestUtils.waitForElementBy(participant.getDriver(),
                    By.id("remove-room-" + id), 2);

            removeButton.click();

        }

        public void closeRoom()
        {
            openContextMenu();
            WebElement closeButton = TestUtils.waitForElementBy(participant.getDriver(),
                    By.id("close-room-" + id), 2);

            closeButton.click();
        }
    }
}
