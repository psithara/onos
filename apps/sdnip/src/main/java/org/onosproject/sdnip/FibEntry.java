/*
 * Copyright 2015 Open Networking Laboratory
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
package org.onosproject.sdnip;

import org.onlab.packet.IpAddress;
import org.onlab.packet.IpPrefix;
import org.onlab.packet.MacAddress;

/**
 * An entry in the Forwarding Information Base (FIB).
 */
public class FibEntry {

    private final IpPrefix prefix;
    private final IpAddress nextHopIp;
    private final MacAddress nextHopMac;

    /**
     * Creates a new FIB entry.
     *
     * @param prefix IP prefix of the FIB entry
     * @param nextHopIp IP address of the next hop
     * @param nextHopMac MAC address of the next hop
     */
    public FibEntry(IpPrefix prefix, IpAddress nextHopIp, MacAddress nextHopMac) {
        this.prefix = prefix;
        this.nextHopIp = nextHopIp;
        this.nextHopMac = nextHopMac;
    }

    /**
     * Returns the IP prefix of the FIB entry.
     *
     * @return the IP prefix
     */
    public IpPrefix prefix() {
        return prefix;
    }

    /**
     * Returns the IP address of the next hop.
     *
     * @return the IP address
     */
    public IpAddress nextHopIp() {
        return nextHopIp;
    }

    /**
     * Returns the MAC address of the next hop.
     *
     * @return the MAC address
     */
    public MacAddress nextHopMac() {
        return nextHopMac;
    }
}
