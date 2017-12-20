package org.jitsi.meet.test.mobile;

import org.jitsi.meet.test.*;
import org.jitsi.meet.test.mobile.base.*;

import org.openqa.selenium.*;
import org.testng.annotations.*;

/**
 *
 */
public class HybridTest
    extends AbstractBaseTest
{
    private MobileParticipant ios;
    private MobileParticipant android;

    @BeforeTest
    public void setUpParticipants()
    {
        this.android = createMobile("mobile.android");
        this.ios = createMobile("mobile.ios");
    }

    @Test
    public void joinMobileAndWebTest()
    {
        ConferenceFixture.startOwner(null);

        ios.joinConference(ConferenceFixture.currentRoomName);
        android.joinConference(ConferenceFixture.currentRoomName);
    }

    @AfterTest
    public void cleanUp()
    {
        WebDriver owner = ConferenceFixture.getOwnerInstance();
        if (owner != null)
        {
            ConferenceFixture.quit(owner);
        }
    }
}
