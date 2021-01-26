/*
 * Copyright @ 2020 8x8, Inc.
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

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * A fluid wrapper around the blip shell script (typically found under the
 * scripts folder) that can be used to emulate network blips on AWS shards.
 */
public class Blip
    implements Callable<Void>
{
    public final static String BLIP_SCRIPT_PNAME = "org.jitsi.meet.test.util.blip_script";

    private long duration;
    private float maxDisruptedPct;
    private Set<String> bridgeIPs;

    /**
     * Factory method.
     *
     * @param duration the duration to run the blip for.
     * @return the new instance.
     */
    public static Blip randomBlipsFor(long duration)
    {
        Blip blip = new Blip();
        blip.duration = duration;
        return blip;
    }

    @Override
    public Void call()
        throws Exception
    {
        final String blipScript = System.getProperty(BLIP_SCRIPT_PNAME);
        File file = new File(blipScript);
        if (file.canExecute())
        {
            // For usage see ./scripts/blip.sh.
            String[] command = {
                blipScript,
                "--duration=" + duration,
                "--max-disrupted-pct=" + maxDisruptedPct,
                "--bridge-ips=" + String.join(",", bridgeIPs)
            };

            ProcessBuilder pb = new ProcessBuilder(command).redirectErrorStream(true);
            Process process = pb.start();
            try (BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream())))
            {
                while (true)
                {
                    String line = in.readLine();
                    if (line == null)
                        break;
                    System.out.println(line);
                }
            }

            int exitStatus = process.waitFor();

            if (exitStatus != 0)
            {
                throw new Exception("The blip script has failed.");
            }
        }
        else
        {
            throw new Exception("Could not find or could not execute the blip script.");
        }

        return null;
    }

    /**
     * Sets the max bridges to disrupt (as a percentage).
     *
     * @param maxDisruptedPct the max bridge to disrupt (as a percentage).
     * @return this instance for fluid syntax.
     */
    public Blip withMaxDisruptedPct(float maxDisruptedPct)
    {
        this.maxDisruptedPct = maxDisruptedPct;
        return this;
    }

    public Blip theseBridges(Set<String> bridgeIPs)
    {
        this.bridgeIPs = bridgeIPs;
        return this;
    }
}
