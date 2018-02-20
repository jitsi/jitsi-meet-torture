package org.jitsi.meet.test.mobile.base;

import org.jitsi.meet.test.base.*;
import org.jitsi.meet.test.mobile.*;
import org.openqa.selenium.*;

import java.util.*;

public class MobileParticipantFactory
    extends ParticipantFactory<MobileParticipantOptions>
{
    /**
     * The private constructor of the factory.
     *
     * @param config - A <tt>Properties</tt> instance holding configuration
     *               properties required to setup new participants.
     */
    public MobileParticipantFactory(Properties config)
    {
        super(config);
    }

    @Override
    public Participant<? extends WebDriver> createParticipant(
        String configPrefix,
        ParticipantOptions options)
    {
        MobileParticipantBuilder builder
            = MobileParticipantBuilder.createBuilder(
                config,
                options.getConfigPrefix(),
                options.getParticipantType());

        return builder.startNewDriver();
    }
}
