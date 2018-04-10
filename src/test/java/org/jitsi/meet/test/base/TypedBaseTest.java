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

import java.util.*;

/**
 * A test class which will inject {@link ParticipantFactory} type and
 * consequently the {@link Participant}'s type which it produces.
 *
 * @param <P> the participant's type compatible with this test.
 * @param <F> the factory's type which produces {@code P} type participants.
 */
public class TypedBaseTest
    <P extends Participant, F extends ParticipantFactory<P>>
    extends AbstractBaseTest<P>
{
    /**
     * Stores reference to the factory's class.
     */
    private final Class<F> factoryClass;

    /**
     * Creates new {@link TypedBaseTest}.
     *
     * @param factoryClass the factory's class which will be created by
     * {@link TypedParticipantHelper}.
     */
    public TypedBaseTest(Class<F> factoryClass)
    {
        this.factoryClass = factoryClass;
    }

    /**
     * A copy constructor which creates new {@link TypedBaseTest}.
     *
     * @param baseTest the base test from which internal state will be obtained.
     * @param factoryClass the factory class to be used by the new instance.
     *
     * @deprecated see
     * {@link AbstractBaseTest#AbstractBaseTest(AbstractBaseTest)}
     */
    protected TypedBaseTest(AbstractBaseTest<P> baseTest, Class<F> factoryClass)
    {
        super(baseTest);

        this.factoryClass = factoryClass;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ParticipantHelper<P> createParticipantHelper(Properties config)
    {
        return new TypedParticipantHelper<>(config, factoryClass);
    }
}
