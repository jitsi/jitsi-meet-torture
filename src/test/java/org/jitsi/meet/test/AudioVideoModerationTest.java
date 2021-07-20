package org.jitsi.meet.test;


import org.jitsi.meet.test.pageobjects.web.AVModerationMenu;
import org.jitsi.meet.test.pageobjects.web.ParticipantsPane;
import org.jitsi.meet.test.util.TestUtils;
import org.jitsi.meet.test.web.WebParticipant;
import org.jitsi.meet.test.web.WebTestBase;
import org.testng.annotations.Test;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Tests the A-V moderation functionality.
 *
 * @author Calin Chitu
 */
public class AudioVideoModerationTest extends WebTestBase
{
    /**
     * The participant.
     */
    private WebParticipant participant1;
    private WebParticipant participant2;
    private WebParticipant participant3;

    @Override
    public void setupClass()
    {
        super.setupClass();

        ensureThreeParticipants();
        participant1 = getParticipant1();
        participant2 = getParticipant2();
        participant3 = getParticipant3();
    }

    /**
     * Opens the context menu from the participants pane
     * and disables moderation
     */
    @Test
    public void dontAllowAttendeesToUnmute()
    {

        ParticipantsPane participantsPane = participant1.getParticipantsPane();
        AVModerationMenu avModerationMenu = participant1.getAVModerationMenu();

        participantsPane.open();
        TestUtils.waitMillis(5000);

        assertTrue(participant1.isModerator(), "Participant 1 must be moderator");
        assertFalse(getParticipant2().isModerator(), "Participant 2 must not be moderator");
        assertFalse(getParticipant3().isModerator(), "Participant 3 must not be moderator");

        TestUtils.waitMillis(5000);

        participantsPane.clickContextMenuButton();

        TestUtils.waitMillis(5000);

        avModerationMenu.clickStopModeration();

        TestUtils.waitMillis(2000);

        participant2.getToolbar().clickAudioMuteButton();
        TestUtils.waitMillis(2000);
        // check that the kicked participant sees the notification
        assertTrue(
                getParticipant2().getNotifications().hasAudioModerationNotification(),
                "The second participant should see a warning that was kicked.");
        TestUtils.waitMillis(2000);
        assertTrue(
                getParticipant3().getNotifications().hasAudioModerationNotification(),
                "The third participant should see a warning that was kicked.");
        TestUtils.waitMillis(2000);
    }
}
