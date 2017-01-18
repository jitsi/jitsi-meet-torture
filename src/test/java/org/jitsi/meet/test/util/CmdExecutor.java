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
package org.jitsi.meet.test.util;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.TimeoutException;

/**
 * Utility class for running command through the {@link ProcessBuilder}.
 * Note that instance can bu used only once.
 *
 * @author Pawel Domas
 */
public class CmdExecutor
{
    /**
     * The <tt>Exception</tt> thrown during command execution.
     */
    private Exception cmdException;

    /**
     * The {@link Thread} which executes the command.
     */
    private Thread executorThread;

    /**
     * The {@link Process} that executes the command.
     */
    private Process process;

    /**
     * The exit value returned by the process executing the command.
     */
    private Integer result;

    /**
     * A count down latch used to wait for the end of command execution.
     */
    private final CountDownLatch waitEndSignal = new CountDownLatch(1);

    /**
     * Executes a command through the {@link ProcessBuilder}.
     *
     * @param cmd the <tt>List<String></tt> which will be passed to
     * {@link ProcessBuilder} to build and execute the process.
     */
    public int executeCmd(final List<String> cmd)
        throws Exception
    {
        if (executorThread != null)
            throw new IllegalStateException();

        // this will fire a hook script, which needs to launch a browser that
        // will join our room
        executorThread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                ProcessBuilder pb = new ProcessBuilder(cmd);
                pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
                pb.redirectError(ProcessBuilder.Redirect.INHERIT);

                try
                {
                    process = pb.start();
                    result = process.waitFor();
                }
                catch (Exception e)
                {
                    cmdException = e;
                }
                finally
                {
                    waitEndSignal.countDown();
                }
            }
        });

        executorThread.start();

        if (!waitEndSignal.await(5, TimeUnit.SECONDS)
                && process != null && result == null)
        {
            System.err.println("Killing the process: " + cmd);

            process.destroy();

            throw new TimeoutException("Command execution timeout");
        }

        if (cmdException != null)
            throw new Exception(cmdException);

        return result;
    }
}
