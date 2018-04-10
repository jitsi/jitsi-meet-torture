/*
 * Copyright @ 2015 - current Atlassian Pty Ltd
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
package org.jitsi.meet.test.base;

import java.lang.reflect.*;
import java.util.*;

/**
 * A typed {@link ParticipantHelper} which injects specific {@link Participant}
 * and {@link ParticipantFactory} types into the test hierarchy.
 *
 * @param <P> the {@link Participant}'s type to be injected.
 * @param <F> the {@link ParticipantFactory}'s type to be injected.
 */
public class TypedParticipantHelper
    <P extends Participant, F extends ParticipantFactory<P>>
    extends ParticipantHelper<P>
{
    /**
     * The {@link ParticipantFactory} class to be used by this instance.
     */
    private final Class<F> factoryClass;

    /**
     * Creates new {@link TypedParticipantHelper}.
     *
     * @param config the test config from which {@link ParticipantOptions} will
     * be read.
     * @param factoryClass the participant's factory class to be injected into
     * the test hierarchy.
     */
    TypedParticipantHelper(Properties config, Class<F> factoryClass)
    {
        super(config);

        this.factoryClass = factoryClass;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ParticipantFactory<P> createFactory()
    {
        try
        {
            return factoryClass.getConstructor().newInstance();
        }
        catch (InstantiationException
                | IllegalAccessException
                | InvocationTargetException
                | NoSuchMethodException e)
        {
            throw new RuntimeException(e);
        }
    }
}
