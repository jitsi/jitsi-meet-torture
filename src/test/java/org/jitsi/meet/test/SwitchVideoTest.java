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

import org.jitsi.meet.test.base.*;
import org.jitsi.meet.test.web.*;

import org.testng.annotations.*;

/**
 * Tests switching video of participants.
 *
 * @author Damian Minkov
 * @author Pawel Domas
 */
public class SwitchVideoTest
    extends WebTestBase
{
    /**
     * Default constructor.
     */
    public SwitchVideoTest()
    {}

    /**
     * Constructs SwitchVideoTest with already allocated participants.
     * @deprecated see
     * {@link AbstractBaseTest#AbstractBaseTest(AbstractBaseTest)}
     */
    public SwitchVideoTest(AbstractBaseTest baseTest)
    {
        super(baseTest);
    }

    @Override
    public void setupClass()
    {
        super.setupClass();

        ensureTwoParticipants();
    }

    /**
     * Click on local video thumbnail and checks whether the large video
     * is the local one.
     */
    @Test
    public void participant1ClickOnLocalVideoAndTest()
    {
        getParticipant1()
            .getFilmstrip()
            .setLocalParticipantPin(true);

        String localVideoSrc = getParticipant1()
            .getFilmstrip()
            .getVideoSrcForLocalParticipant();

        getParticipant1()
            .getLargeVideo()
            .waitForVideoToDisplay(localVideoSrc);
    }

    /**
     * Clicks on the remote video thumbnail and checks whether the large video
     * is the remote one.
     */
    @Test(dependsOnMethods = {"participant1ClickOnLocalVideoAndTest"})
    public void participant1ClickOnRemoteVideoAndTest()
    {
        closeToolbarDialogs();

        getParticipant1()
            .getFilmstrip()
            .setRemoteParticipantPin(getParticipant2(), true);

        String removeVideoSrc = getParticipant1()
            .getFilmstrip()
            .getVideoSrcForRemoteParticipant(getParticipant2());

        getParticipant1()
            .getLargeVideo()
            .waitForVideoToDisplay(removeVideoSrc);
    }

    /**
     * Unpins remote video in participant1 and verifies that the operation
     * succeeded.
     */
    @Test(dependsOnMethods = {"participant1ClickOnRemoteVideoAndTest"})
    public void participant1UnpinRemoteVideoAndTest()
    {
        getParticipant1()
            .getFilmstrip()
            .setRemoteParticipantPin(getParticipant2(), false);
    }

    /**
     * Unpins remote video in participant2 and verifies that the operation
     * succeeded.
     */
    @Test(dependsOnMethods = { "participantClickOnRemoteVideoAndTest" })
    public void participant2UnpinRemoteVideo()
    {
        getParticipant2()
            .getFilmstrip()
            .setRemoteParticipantPin(getParticipant1(), false);
    }

    /**
     * Click on local video thumbnail and checks whether the large video
     * is the local one.
     */
    @Test(dependsOnMethods = {"participant1UnpinRemoteVideoAndTest"})
    public void participantClickOnLocalVideoAndTest()
    {
        getParticipant2().getFilmstrip().setLocalParticipantPin(true);

        String localVideoSrc = getParticipant2()
            .getFilmstrip()
            .getVideoSrcForLocalParticipant();

        getParticipant2()
            .getLargeVideo()
            .waitForVideoToDisplay(localVideoSrc);
    }

    /**
     * Clicks on the remote video thumbnail and checks whether the large video
     * is the remote one.
     */
    @Test(dependsOnMethods = { "participantClickOnLocalVideoAndTest" })
    public void participantClickOnRemoteVideoAndTest()
    {
        getParticipant2()
            .getFilmstrip()
            .setRemoteParticipantPin(getParticipant1(), true);

        String remoteVideoSrc = getParticipant2()
            .getFilmstrip()
            .getVideoSrcForRemoteParticipant(getParticipant1());

        getParticipant2()
            .getLargeVideo()
            .waitForVideoToDisplay(remoteVideoSrc);
    }

    /**
     * Ensures all participants do not have toolbar related dialogs open that
     * could obstruct clicking of the filmstrip.
     */
    private void closeToolbarDialogs()
    {
        getParticipant1().getInfoDialog().close();
        getParticipant1().getToolbar().closeOverflowMenu();

        getParticipant2().getInfoDialog().close();
        getParticipant2().getToolbar().closeOverflowMenu();
    }
}
