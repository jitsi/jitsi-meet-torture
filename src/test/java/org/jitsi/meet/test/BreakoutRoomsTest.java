/*
 * Copyright @ 2021 8x8 International
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

import org.jitsi.meet.test.base.JitsiMeetUrl;
import org.jitsi.meet.test.pageobjects.web.BreakoutRoomsList;
import org.jitsi.meet.test.pageobjects.web.ParticipantsPane;
import org.jitsi.meet.test.util.TestUtils;
import org.jitsi.meet.test.web.WebParticipant;
import org.jitsi.meet.test.web.WebTestBase;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.testng.SkipException;
import org.testng.annotations.Test;

import java.util.*;
import java.util.logging.*;

import static org.testng.Assert.*;
import static org.jitsi.meet.test.pageobjects.web.ParticipantsPane.PARTICIPANT_ITEM;

/**
 * Tests the Breakout rooms functionality.
 */
public class BreakoutRoomsTest
    extends WebTestBase
{
    /**
     * The default name of the main room name.
     */
    private final static String MAIN_ROOM_NAME = "Main room";

    /**
     * The id of the breakout rooms list element.
     */
    private final static String BREAKOUT_ROOMS_LIST_ID = "breakout-rooms-list";

    /**
     * The class of the list elements.
     */
    private final static String LIST_ITEM_CONTAINER = "list-item-container";

    /**
     * The participants.
     */
    private WebParticipant participant1;
    private WebParticipant participant2;
    private WebParticipant participant3;

    @Override
    public void setupClass()
    {
        super.setupClass();

        ensureOneParticipant();
        participant1 = getParticipant1();

        try
        {
            TestUtils.waitForCondition(participant1.getDriver(), 2,
                (ExpectedCondition<Boolean>) d -> participant1.isModerator());
        }
        catch(TimeoutException e)
        {
            cleanupClass();
            Logger.getGlobal().info("Skipping as anonymous participants are not moderators.");
            throw new SkipException("Skipping as anonymous participants are not moderators.");
        }

        try
        {
            TestUtils.waitForCondition(participant1.getDriver(), 2,
                    (ExpectedCondition<Boolean>) d -> participant1.supportsBreakoutRooms());
        }
        catch(TimeoutException e)
        {
            cleanupClass();
            Logger.getGlobal().info("Skipping as breakout rooms are not supported.");
            throw new SkipException("Skipping as breakout rooms are not supported.");
        }

        ensureTwoParticipants();
        participant2 = getParticipant2();
    }

    @Test
    public void testAddBreakoutRoom()
    {
        ParticipantsPane pane = participant1.getParticipantsPane();
        BreakoutRoomsList roomsList = participant1.getBreakoutRoomsList();
        pane.open();

        // there should be no breakout rooms initially
        TestUtils.waitForCondition(participant1.getDriver(), 5, (ExpectedCondition<Boolean>) d ->
                roomsList.getRoomsCount() == 0);

        // add one breakout room
        pane.addBreakoutRoom();

        // there should be one breakout room with no participants
        TestUtils.waitForCondition(participant1.getDriver(), 5, (ExpectedCondition<Boolean>) d ->
                roomsList.getRoomsCount() == 1 && roomsList.getRooms().get(0).getParticipantsCount() == 0);

        // second participant should also see one breakout room
        participant2.getParticipantsPane().open();
        TestUtils.waitForCondition(participant2.getDriver(), 5, (ExpectedCondition<Boolean>) d ->
                participant2.getBreakoutRoomsList().getRoomsCount() == 1);
    }

    @Test(dependsOnMethods = { "testAddBreakoutRoom" })
    public void testJoinRoom()
    {
        BreakoutRoomsList roomsList = participant1.getBreakoutRoomsList();

        // there should be one breakout room
        TestUtils.waitForCondition(participant1.getDriver(), 5, (ExpectedCondition<Boolean>) d ->
                roomsList.getRoomsCount() == 1);

        // join the room
        roomsList.getRooms().get(0).joinRoom();

        // the participant should see the main room as the only breakout room
        TestUtils.waitForCondition(participant1.getDriver(), 5, (ExpectedCondition<Boolean>) d ->
                roomsList.getRoomsCount() == 1
                        && roomsList.getRooms().get(0).getName().trim().equals(MAIN_ROOM_NAME));

        // the second participant should see one participant in the breakout room
        TestUtils.waitForCondition(participant2.getDriver(), 5, (ExpectedCondition<Boolean>) d ->
                participant2.getBreakoutRoomsList().getRooms().get(0).getParticipantsCount() == 1);
    }

    @Test(dependsOnMethods = { "testJoinRoom" })
    public void testLeaveRoom()
    {
        ParticipantsPane pane = participant1.getParticipantsPane();
        BreakoutRoomsList roomsList = participant1.getBreakoutRoomsList();

        // leave room
        pane.leaveBreakoutRoom();

        // there should be one breakout room and that should not be the main room
        TestUtils.waitForCondition(participant1.getDriver(), 5, (ExpectedCondition<Boolean>) d ->
                {
                    List<BreakoutRoomsList.BreakoutRoom> rooms = roomsList.getRooms();
                    return rooms.size() == 1 && !(rooms.get(0).getName().trim().equals(MAIN_ROOM_NAME));
                });

        // the second participant should see no participants in the breakout room
        TestUtils.waitForCondition(participant2.getDriver(), 5, (ExpectedCondition<Boolean>) d ->
            {
                List<BreakoutRoomsList.BreakoutRoom> rooms = participant2.getBreakoutRoomsList().getRooms();
                return rooms.size() == 1 && rooms.get(0).getParticipantsCount() == 0;
            });
    }

    @Test(dependsOnMethods = { "testLeaveRoom" })
    public void testRemoveRoom()
    {
        BreakoutRoomsList roomsList = participant1.getBreakoutRoomsList();

        // remove the room
        BreakoutRoomsList.BreakoutRoom room = roomsList.getRooms().get(0);
        room.removeRoom();

        // there should be no breakout rooms
        TestUtils.waitForCondition(participant1.getDriver(), 5,
                (ExpectedCondition<Boolean>) d -> roomsList.getRoomsCount() == 0);

        // the second participant should also see no breakout rooms
        TestUtils.waitForCondition(participant2.getDriver(), 5, (ExpectedCondition<Boolean>) d ->
                participant2.getBreakoutRoomsList().getRoomsCount() == 0);
    }

    @Test(dependsOnMethods = { "testRemoveRoom" })
    public void testAutoAssign()
    {
        ensureThreeParticipants();
        participant3 = getParticipant3();

        ParticipantsPane pane = participant1.getParticipantsPane();
        BreakoutRoomsList roomsList = participant1.getBreakoutRoomsList();

        // create two rooms
        pane.addBreakoutRoom();
        pane.addBreakoutRoom();

        // there should be two breakout rooms
        TestUtils.waitForCondition(participant1.getDriver(), 5,
                (ExpectedCondition<Boolean>) d -> roomsList.getRoomsCount() == 2);

        // auto assign participants to rooms
        pane.autoAssignToBreakoutRooms();

        // each room should have one participant
        TestUtils.waitForCondition(participant1.getDriver(), 5,
                (ExpectedCondition<Boolean>) d -> {
                    List<BreakoutRoomsList.BreakoutRoom> rooms = roomsList.getRooms();
                    return rooms.get(0).getParticipantsCount() == 1
                            && rooms.get(1).getParticipantsCount() == 1;
                });

        // the second participant should see one participant in the main room
        TestUtils.waitForCondition(participant2.getDriver(), 5, (ExpectedCondition<Boolean>) d ->
                {
                    BreakoutRoomsList.BreakoutRoom room = participant2.getBreakoutRoomsList().getRooms().get(0);
                    return room.getName().trim().equals(MAIN_ROOM_NAME) && room.getParticipantsCount() == 1;
                });
    }

    @Test(dependsOnMethods = { "testAutoAssign" })
    public void testCloseRoom()
    {
        participant3.getParticipantsPane().open();
        BreakoutRoomsList roomsList = participant1.getBreakoutRoomsList();

        // there should be two non-empty breakout rooms
        TestUtils.waitForCondition(participant1.getDriver(), 5, (ExpectedCondition<Boolean>) d ->
                {
                    List<BreakoutRoomsList.BreakoutRoom> rooms = roomsList.getRooms();
                    return rooms.size() == 2
                            && rooms.get(0).getParticipantsCount() == 1
                            && rooms.get(1).getParticipantsCount() == 1;
                });

        // close the first room
        BreakoutRoomsList.BreakoutRoom room = roomsList.getRooms().get(0);
        room.closeRoom();

        // there should be two rooms and first one should be empty
        TestUtils.waitForCondition(participant1.getDriver(), 5, (ExpectedCondition<Boolean>) d ->
                roomsList.getRoomsCount() == 2
                        && roomsList.getRooms().get(0).getParticipantsCount() == 0);

        // there should be two participants in the main room, either p2 or p3 got moved to the main room
        List<WebParticipant> participants = Arrays.asList(participant2, participant3);
        participants.forEach(p ->
        {
            TestUtils.waitForCondition(p.getDriver(), 5, (ExpectedCondition<Boolean>) d ->
            {
                BreakoutRoomsList.BreakoutRoom pRoom = p.getBreakoutRoomsList().getRooms().get(0);
                if (pRoom.getName().trim().equals(MAIN_ROOM_NAME))
                {
                    return pRoom.getParticipantsCount() == 2;
                }
                return pRoom.getParticipantsCount() == 0;
            }, 500);
        });
    }

    @Test(dependsOnMethods = { "testCloseRoom" })
    public void testSendParticipantToRoom()
    {
        hangUpAllParticipants();
        // because the participants rejoin so fast, the meeting is not properly ended,
        // so the previous breakout rooms would still be there.
        // To avoid this issue we use a different meeting
        JitsiMeetUrl url = getJitsiMeetUrl()
                .setRoomName("random-room-name")
                .appendConfig("config.startWithAudioMuted=true");
        ensureTwoParticipants(url, url);

        ParticipantsPane pane = participant1.getParticipantsPane();
        pane.open();
        BreakoutRoomsList roomsList = participant1.getBreakoutRoomsList();

        // there should be no breakout rooms
        TestUtils.waitForCondition(participant1.getDriver(), 5,
                (ExpectedCondition<Boolean>) d -> roomsList.getRoomsCount() == 0);

        // add one breakout room
        pane.addBreakoutRoom();

        // there should be one empty room
        TestUtils.waitForCondition(participant1.getDriver(), 5,
                (ExpectedCondition<Boolean>) d -> {
                    List<BreakoutRoomsList.BreakoutRoom> rooms = roomsList.getRooms();
                    return rooms.size() == 1
                            && rooms.get(0).getParticipantsCount() == 0;
                });

        // send the second participant to the first breakout room
        pane.sendParticipantToBreakoutRoom(participant2, roomsList.getRooms().get(0).getName().trim());

        // there should be one room with one participant
        TestUtils.waitForCondition(participant1.getDriver(), 5,
                (ExpectedCondition<Boolean>) d -> {
                    List<BreakoutRoomsList.BreakoutRoom> rooms = roomsList.getRooms();
                    return rooms.size() == 1
                            && rooms.get(0).getParticipantsCount() == 1;
                });
    }

    @Test(dependsOnMethods = { "testSendParticipantToRoom" })
    public void testCollapseRoom()
    {
        BreakoutRoomsList roomsList = participant1.getBreakoutRoomsList();
        boolean visible = true;

        // there should be one breakout room with one participant
        TestUtils.waitForCondition(participant1.getDriver(), 5,
                (ExpectedCondition<Boolean>) d -> {
                    List<BreakoutRoomsList.BreakoutRoom> rooms = roomsList.getRooms();
                    return rooms.size() == 1
                            && rooms.get(0).getParticipantsCount() == 1;
                });

        // get id of the breakout room participant
        String participant2Id = participant1.getDriver().findElement(By.id(BREAKOUT_ROOMS_LIST_ID))
                .findElements(By.className(LIST_ITEM_CONTAINER))
                .stream().filter(el -> !el.getAttribute("id").equals("")).findFirst()
                .map(el -> el.getAttribute("id")).orElse("");
        participant2Id = participant2Id.substring(PARTICIPANT_ITEM.length());

        // check participant 2 is visible in the pane initially
        TestUtils.waitForDisplayedElementByID(participant1.getDriver(),
                PARTICIPANT_ITEM + participant2Id, 5);

        // collapse the first
        roomsList.getRooms().get(0).collapse();
        try
        {
            TestUtils.waitForDisplayedElementByID(participant1.getDriver(),
                    PARTICIPANT_ITEM + participant2Id, 3);
        }
        catch (TimeoutException e)
        {
            visible = false;
        }
        assertFalse(visible, "Participant 2 should no longer be visible");

        // the collapsed room should still have one participant
        TestUtils.waitForCondition(participant1.getDriver(), 5,
                (ExpectedCondition<Boolean>) d -> roomsList.getRooms().get(0).getParticipantsCount() == 1);
    }
}
