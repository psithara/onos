/*
 * Copyright 2017-present Open Networking Foundation
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

package org.onlab.packet.dhcp;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.io.Resources;

import org.junit.Test;
import org.onlab.packet.DHCP6;
import org.onlab.packet.Deserializer;
import org.onlab.packet.Ip6Address;
import org.onlab.packet.MacAddress;
import org.onlab.packet.PacketTestUtils;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static org.junit.Assert.assertNull;

public class Dhcp6Test {
    private static final String SOLICIT = "dhcp6_solicit.bin";
    private static final String ADVERTISE = "dhcp6_advertise.bin";
    private static final String REQUEST = "dhcp6_request.bin";
    private static final String REPLY = "dhcp6_reply.bin";

    private static final int XID_1 = 13346301;
    private static final int XID_2 = 9807588;
    private static final int IA_ID = 1;
    private static final int T1_CLIENT = 3600;
    private static final int T2_CLIENT = 5400;
    private static final int T1_SERVER = 0;
    private static final int T2_SERVER = 0;
    private static final Ip6Address IA_ADDRESS = Ip6Address.valueOf("2000::201");
    private static final int PREFFERRED_LT_SERVER = 375;
    private static final int VALID_LT_SERVER = 600;
    private static final int PREFFERRED_LT_REQ = 7200;
    private static final int VALID_LT_REQ = 7500;
    private static final MacAddress CLIENT_MAC = MacAddress.valueOf("00:bb:00:00:00:01");
    private static final int CLIENT_DUID_TIME = 0x210016b4;


    private Deserializer<DHCP6> deserializer = DHCP6.deserializer();


    @Test
    public void testDeserializeBadInput() throws Exception {
        PacketTestUtils.testDeserializeBadInput(deserializer);
    }

    /**
     * Truncated a simple DHCPv6 payload.
     */
    @Test
    public void testDeserializeTruncated() throws Exception {
        ByteBuffer bb = ByteBuffer.allocate(4);
        bb.put(DHCP6.MsgType.REQUEST.value());
        bb.put(new byte[]{0x00, 0x00});
        PacketTestUtils.testDeserializeTruncated(deserializer, bb.array());
    }

    /**
     * Test DHCPv6 solicit message.
     *
     * @throws Exception exception while deserialize the DHCPv6 payload
     */
    @Test
    public void testDeserializeSolicit() throws Exception {
        byte[] data = Resources.toByteArray(Dhcp6RelayTest.class.getResource(SOLICIT));
        DHCP6 dhcp6 = DHCP6.deserializer().deserialize(data, 0, data.length);
        assertEquals(dhcp6.getMsgType(), DHCP6.MsgType.SOLICIT.value());
        assertEquals(dhcp6.getTransactionId(), XID_1);
        assertEquals(dhcp6.getOptions().size(), 4);

        // Client ID
        Dhcp6Option option = dhcp6.getOptions().get(0);
        assertTrue(option instanceof Dhcp6ClientIdOption);
        Dhcp6ClientIdOption clientIdOption = (Dhcp6ClientIdOption) option;
        assertEquals(clientIdOption.getCode(), DHCP6.OptionCode.CLIENTID.value());
        assertEquals(clientIdOption.getLength(), 14);
        assertEquals(clientIdOption.getDuid().getDuidType(), Dhcp6Duid.DuidType.DUID_LLT);
        assertEquals(clientIdOption.getDuid().getHardwareType(), 1);
        assertEquals(clientIdOption.getDuid().getDuidTime(), CLIENT_DUID_TIME);
        assertArrayEquals(clientIdOption.getDuid().getLinkLayerAddress(), CLIENT_MAC.toBytes());

        // ORO
        option = dhcp6.getOptions().get(1);
        assertEquals(option.getCode(), DHCP6.OptionCode.ORO.value());
        assertEquals(option.getLength(), 8);
        assertArrayEquals(option.getData(),
                          new byte[]{0, 23, 0, 24, 0, 39, 0, 31});

        // ELAPSED_TIME
        option = dhcp6.getOptions().get(2);
        assertEquals(option.getCode(), DHCP6.OptionCode.ELAPSED_TIME.value());
        assertEquals(option.getLength(), 2);
        assertArrayEquals(option.getData(),
                          new byte[]{0, 0});

        // IA NA
        option = dhcp6.getOptions().get(3);
        assertTrue(option instanceof Dhcp6IaNaOption);
        Dhcp6IaNaOption iaNaOption = (Dhcp6IaNaOption) option;
        assertEquals(iaNaOption.getCode(), DHCP6.OptionCode.IA_NA.value());
        assertEquals(iaNaOption.getLength(), 12);
        assertEquals(iaNaOption.getIaId(), IA_ID);
        assertEquals(iaNaOption.getT1(), T1_CLIENT);
        assertEquals(iaNaOption.getT2(), T2_CLIENT);
        assertEquals(iaNaOption.getOptions().size(), 0);

        assertArrayEquals(data, dhcp6.serialize());
    }

    /**
     * Test serialize solicit message.
     *
     * @throws Exception exception while serialize the DHCPv6 payload
     */
    @Test
    public void serializeSolicit() throws Exception {
        DHCP6 dhcp6 = new DHCP6();
        dhcp6.setMsgType(DHCP6.MsgType.SOLICIT.value());
        dhcp6.setTransactionId(XID_1);
        List<Dhcp6Option> options = Lists.newArrayList();

        // Client ID
        Dhcp6Duid duid = new Dhcp6Duid();
        duid.setDuidType(Dhcp6Duid.DuidType.DUID_LLT);
        duid.setHardwareType((short) 1);
        duid.setDuidTime(CLIENT_DUID_TIME);
        duid.setLinkLayerAddress(CLIENT_MAC.toBytes());
        Dhcp6ClientIdOption clientIdOption = new Dhcp6ClientIdOption();
        clientIdOption.setDuid(duid);
        options.add(clientIdOption);

        // Option request
        Dhcp6Option option = new Dhcp6Option();
        option.setCode(DHCP6.OptionCode.ORO.value());
        option.setLength((short) 8);
        option.setData(new byte[]{0, 23, 0, 24, 0, 39, 0, 31});
        options.add(option);

        // Elapsed Time
        option = new Dhcp6Option();
        option.setCode(DHCP6.OptionCode.ELAPSED_TIME.value());
        option.setLength((short) 2);
        option.setData(new byte[]{0, 0});
        options.add(option);

        // IA NA
        Dhcp6IaNaOption iaNaOption = new Dhcp6IaNaOption();
        iaNaOption.setIaId(IA_ID);
        iaNaOption.setT1(T1_CLIENT);
        iaNaOption.setT2(T2_CLIENT);
        iaNaOption.setOptions(Collections.emptyList());
        options.add(iaNaOption);
        dhcp6.setOptions(options);


        Dhcp6RelayOption relayOption = new Dhcp6RelayOption();
        relayOption.setPayload(dhcp6);

        assertArrayEquals(Resources.toByteArray(Dhcp6RelayTest.class.getResource(SOLICIT)),
                          dhcp6.serialize());
    }

    /**
     * Test deserialize advertise message.
     *
     * @throws Exception exception while deserialize the DHCPv6 payload
     */
    @Test
    public void deserializeAdvertise() throws Exception {
        byte[] data = Resources.toByteArray(getClass().getResource(ADVERTISE));


        DHCP6 dhcp6 = DHCP6.deserializer().deserialize(data, 0, data.length);
        assertEquals(dhcp6.getMsgType(), DHCP6.MsgType.ADVERTISE.value());
        assertEquals(dhcp6.getTransactionId(), XID_1);
        assertEquals(dhcp6.getOptions().size(), 3);

        // IA NA
        Dhcp6Option option = dhcp6.getOptions().get(0);
        assertTrue(option instanceof Dhcp6IaNaOption);
        Dhcp6IaNaOption iaNaOption = (Dhcp6IaNaOption) option;
        assertEquals(iaNaOption.getCode(), DHCP6.OptionCode.IA_NA.value());
        assertEquals(iaNaOption.getLength(), 40);
        assertEquals(iaNaOption.getIaId(), IA_ID);
        assertEquals(iaNaOption.getT1(), T1_SERVER);
        assertEquals(iaNaOption.getT2(), T2_SERVER);
        assertEquals(iaNaOption.getOptions().size(), 1);

        // IA Address (in IA NA)
        assertTrue(iaNaOption.getOptions().get(0) instanceof Dhcp6IaAddressOption);
        Dhcp6IaAddressOption iaAddressOption =
                (Dhcp6IaAddressOption) iaNaOption.getOptions().get(0);
        assertEquals(iaAddressOption.getIp6Address(), IA_ADDRESS);
        assertEquals(iaAddressOption.getPreferredLifetime(), PREFFERRED_LT_SERVER);
        assertEquals(iaAddressOption.getValidLifetime(), VALID_LT_SERVER);
        assertNull(iaAddressOption.getOptions());

        // Client ID
        option = dhcp6.getOptions().get(1);
        assertTrue(option instanceof Dhcp6ClientIdOption);
        Dhcp6ClientIdOption clientIdOption = (Dhcp6ClientIdOption) option;
        assertEquals(clientIdOption.getCode(), DHCP6.OptionCode.CLIENTID.value());
        assertEquals(clientIdOption.getLength(), 14);
        assertEquals(clientIdOption.getDuid().getDuidType(), Dhcp6Duid.DuidType.DUID_LLT);
        assertEquals(clientIdOption.getDuid().getHardwareType(), 1);
        assertEquals(clientIdOption.getDuid().getDuidTime(), CLIENT_DUID_TIME);
        assertArrayEquals(clientIdOption.getDuid().getLinkLayerAddress(), CLIENT_MAC.toBytes());

        // Server ID
        option = dhcp6.getOptions().get(2);
        assertEquals(option.getCode(), DHCP6.OptionCode.SERVERID.value());
        assertEquals(option.getLength(), 14);
        assertArrayEquals(option.getData(),
                          new byte[]{0, 1, 0, 1, 32, -1, -8, -17, 0, -103, 102, 0, 0, 1});

        assertArrayEquals(data, dhcp6.serialize());
    }

    /**
     * Test serialize advertise message.
     *
     * @throws Exception exception while serialize the DHCPv6 payload
     */
    @Test
    public void serializeAdvertise() throws Exception {
        DHCP6 dhcp6 = new DHCP6();
        dhcp6.setMsgType(DHCP6.MsgType.ADVERTISE.value());
        dhcp6.setTransactionId(XID_1);
        List<Dhcp6Option> options = Lists.newArrayList();

        // IA address
        Dhcp6IaAddressOption iaAddressOption = new Dhcp6IaAddressOption();
        iaAddressOption.setIp6Address(IA_ADDRESS);
        iaAddressOption.setPreferredLifetime(PREFFERRED_LT_SERVER);
        iaAddressOption.setValidLifetime(VALID_LT_SERVER);

        // IA NA
        Dhcp6IaNaOption iaNaOption = new Dhcp6IaNaOption();
        iaNaOption.setIaId(IA_ID);
        iaNaOption.setT1(T1_SERVER);
        iaNaOption.setT2(T2_SERVER);
        iaNaOption.setOptions(ImmutableList.of(iaAddressOption));
        options.add(iaNaOption);

        // Client ID
        Dhcp6Duid duid = new Dhcp6Duid();
        duid.setDuidType(Dhcp6Duid.DuidType.DUID_LLT);
        duid.setHardwareType((short) 1);
        duid.setDuidTime(CLIENT_DUID_TIME);
        duid.setLinkLayerAddress(CLIENT_MAC.toBytes());
        Dhcp6ClientIdOption clientIdOption = new Dhcp6ClientIdOption();
        clientIdOption.setDuid(duid);
        options.add(clientIdOption);

        // Server ID
        Dhcp6Option option = new Dhcp6Option();
        option.setCode(DHCP6.OptionCode.SERVERID.value());
        option.setLength((short) 14);
        option.setData(new byte[]{0, 1, 0, 1, 32, -1, -8, -17, 0, -103, 102, 0, 0, 1});
        options.add(option);

        dhcp6.setOptions(options);

        Dhcp6RelayOption relayOption = new Dhcp6RelayOption();
        relayOption.setPayload(dhcp6);

        assertArrayEquals(Resources.toByteArray(Dhcp6RelayTest.class.getResource(ADVERTISE)),
                          dhcp6.serialize());
    }

    /**
     * Test deserialize request message.
     *
     * @throws Exception exception while deserialize the DHCPv6 payload
     */
    @Test
    public void deserializeRequest() throws Exception {
        byte[] data = Resources.toByteArray(getClass().getResource(REQUEST));
        DHCP6 dhcp6 = DHCP6.deserializer().deserialize(data, 0, data.length);
        assertEquals(dhcp6.getMsgType(), DHCP6.MsgType.REQUEST.value());
        assertEquals(dhcp6.getTransactionId(), XID_2);
        assertEquals(dhcp6.getOptions().size(), 5);

        // Client ID
        Dhcp6Option option = dhcp6.getOptions().get(0);
        assertTrue(option instanceof Dhcp6ClientIdOption);
        Dhcp6ClientIdOption clientIdOption = (Dhcp6ClientIdOption) option;
        assertEquals(clientIdOption.getCode(), DHCP6.OptionCode.CLIENTID.value());
        assertEquals(clientIdOption.getLength(), 14);
        assertEquals(clientIdOption.getDuid().getDuidType(), Dhcp6Duid.DuidType.DUID_LLT);
        assertEquals(clientIdOption.getDuid().getHardwareType(), 1);
        assertEquals(clientIdOption.getDuid().getDuidTime(), CLIENT_DUID_TIME);
        assertArrayEquals(clientIdOption.getDuid().getLinkLayerAddress(), CLIENT_MAC.toBytes());

        // Server ID
        option = dhcp6.getOptions().get(1);
        assertEquals(option.getCode(), DHCP6.OptionCode.SERVERID.value());
        assertEquals(option.getLength(), 14);
        assertArrayEquals(option.getData(),
                          new byte[]{0, 1, 0, 1, 32, -1, -8, -17, 0, -103, 102, 0, 0, 1});

        // Option Request
        option = dhcp6.getOptions().get(2);
        assertEquals(option.getCode(), DHCP6.OptionCode.ORO.value());
        assertEquals(option.getLength(), 8);
        assertArrayEquals(option.getData(), new byte[]{0, 23, 0, 24, 0, 39, 0, 31});

        // ELAPSED_TIME
        option = dhcp6.getOptions().get(3);
        assertEquals(option.getCode(), DHCP6.OptionCode.ELAPSED_TIME.value());
        assertEquals(option.getLength(), 2);
        assertArrayEquals(option.getData(),
                          new byte[]{0, 0});

        // IA NA
        option = dhcp6.getOptions().get(4);
        assertTrue(option instanceof Dhcp6IaNaOption);
        Dhcp6IaNaOption iaNaOption = (Dhcp6IaNaOption) option;
        assertEquals(iaNaOption.getCode(), DHCP6.OptionCode.IA_NA.value());
        assertEquals(iaNaOption.getLength(), 40);
        assertEquals(iaNaOption.getIaId(), IA_ID);
        assertEquals(iaNaOption.getT1(), T1_CLIENT);
        assertEquals(iaNaOption.getT2(), T2_CLIENT);
        assertEquals(iaNaOption.getOptions().size(), 1);

        // IA Address (in IA NA)
        assertTrue(iaNaOption.getOptions().get(0) instanceof Dhcp6IaAddressOption);
        Dhcp6IaAddressOption iaAddressOption =
                (Dhcp6IaAddressOption) iaNaOption.getOptions().get(0);
        assertEquals(iaAddressOption.getIp6Address(), IA_ADDRESS);
        assertEquals(iaAddressOption.getPreferredLifetime(), PREFFERRED_LT_REQ);
        assertEquals(iaAddressOption.getValidLifetime(), VALID_LT_REQ);
        assertNull(iaAddressOption.getOptions());

        assertArrayEquals(data, dhcp6.serialize());
    }

    /**
     * Test serialize request message.
     *
     * @throws Exception exception while serialize the DHCPv6 payload
     */
    @Test
    public void serializeRequest() throws Exception {
        DHCP6 dhcp6 = new DHCP6();
        dhcp6.setMsgType(DHCP6.MsgType.REQUEST.value());
        dhcp6.setTransactionId(XID_2);
        List<Dhcp6Option> options = Lists.newArrayList();

        // Client ID
        Dhcp6Duid duid = new Dhcp6Duid();
        duid.setDuidType(Dhcp6Duid.DuidType.DUID_LLT);
        duid.setHardwareType((short) 1);
        duid.setDuidTime(CLIENT_DUID_TIME);
        duid.setLinkLayerAddress(CLIENT_MAC.toBytes());
        Dhcp6ClientIdOption clientIdOption = new Dhcp6ClientIdOption();
        clientIdOption.setDuid(duid);
        options.add(clientIdOption);

        // Server ID
        Dhcp6Option option = new Dhcp6Option();
        option.setCode(DHCP6.OptionCode.SERVERID.value());
        option.setLength((short) 14);
        option.setData(new byte[]{0, 1, 0, 1, 32, -1, -8, -17, 0, -103, 102, 0, 0, 1});
        options.add(option);

        // Option request
        option = new Dhcp6Option();
        option.setCode(DHCP6.OptionCode.ORO.value());
        option.setLength((short) 8);
        option.setData(new byte[]{0, 23, 0, 24, 0, 39, 0, 31});
        options.add(option);

        // Elapsed Time
        option = new Dhcp6Option();
        option.setCode(DHCP6.OptionCode.ELAPSED_TIME.value());
        option.setLength((short) 2);
        option.setData(new byte[]{0, 0});
        options.add(option);

        // IA address
        Dhcp6IaAddressOption iaAddressOption = new Dhcp6IaAddressOption();
        iaAddressOption.setIp6Address(IA_ADDRESS);
        iaAddressOption.setPreferredLifetime(PREFFERRED_LT_REQ);
        iaAddressOption.setValidLifetime(VALID_LT_REQ);

        // IA NA
        Dhcp6IaNaOption iaNaOption = new Dhcp6IaNaOption();
        iaNaOption.setIaId(IA_ID);
        iaNaOption.setT1(T1_CLIENT);
        iaNaOption.setT2(T2_CLIENT);
        iaNaOption.setOptions(ImmutableList.of(iaAddressOption));
        options.add(iaNaOption);

        dhcp6.setOptions(options);

        Dhcp6RelayOption relayOption = new Dhcp6RelayOption();
        relayOption.setPayload(dhcp6);

        assertArrayEquals(Resources.toByteArray(Dhcp6RelayTest.class.getResource(REQUEST)),
                          dhcp6.serialize());
    }

    /**
     * Test deserialize relay message with reply message.
     *
     * @throws Exception exception while deserialize the DHCPv6 payload
     */
    @Test
    public void deserializeReply() throws Exception {
        byte[] data = Resources.toByteArray(getClass().getResource(REPLY));

        DHCP6 dhcp6 = DHCP6.deserializer().deserialize(data, 0, data.length);
        assertEquals(dhcp6.getMsgType(), DHCP6.MsgType.REPLY.value());
        assertEquals(dhcp6.getTransactionId(), XID_2);
        assertEquals(dhcp6.getOptions().size(), 3);

        // IA NA
        Dhcp6Option option = dhcp6.getOptions().get(0);
        assertTrue(option instanceof Dhcp6IaNaOption);
        Dhcp6IaNaOption iaNaOption = (Dhcp6IaNaOption) option;
        assertEquals(iaNaOption.getCode(), DHCP6.OptionCode.IA_NA.value());
        assertEquals(iaNaOption.getLength(), 40);
        assertEquals(iaNaOption.getIaId(), IA_ID);
        assertEquals(iaNaOption.getT1(), T1_SERVER);
        assertEquals(iaNaOption.getT2(), T2_SERVER);
        assertEquals(iaNaOption.getOptions().size(), 1);

        // IA Address (in IA NA)
        assertTrue(iaNaOption.getOptions().get(0) instanceof Dhcp6IaAddressOption);
        Dhcp6IaAddressOption iaAddressOption =
                (Dhcp6IaAddressOption) iaNaOption.getOptions().get(0);
        assertEquals(iaAddressOption.getIp6Address(), IA_ADDRESS);
        assertEquals(iaAddressOption.getPreferredLifetime(), PREFFERRED_LT_SERVER);
        assertEquals(iaAddressOption.getValidLifetime(), VALID_LT_SERVER);
        assertNull(iaAddressOption.getOptions());

        // Client ID
        option = dhcp6.getOptions().get(1);
        assertTrue(option instanceof Dhcp6ClientIdOption);
        Dhcp6ClientIdOption clientIdOption = (Dhcp6ClientIdOption) option;
        assertEquals(clientIdOption.getCode(), DHCP6.OptionCode.CLIENTID.value());
        assertEquals(clientIdOption.getLength(), 14);
        assertEquals(clientIdOption.getDuid().getDuidType(), Dhcp6Duid.DuidType.DUID_LLT);
        assertEquals(clientIdOption.getDuid().getHardwareType(), 1);
        assertEquals(clientIdOption.getDuid().getDuidTime(), CLIENT_DUID_TIME);
        assertArrayEquals(clientIdOption.getDuid().getLinkLayerAddress(), CLIENT_MAC.toBytes());

        // Server ID
        option = dhcp6.getOptions().get(2);
        assertEquals(option.getCode(), DHCP6.OptionCode.SERVERID.value());
        assertEquals(option.getLength(), 14);
        assertArrayEquals(option.getData(),
                          new byte[]{0, 1, 0, 1, 32, -1, -8, -17, 0, -103, 102, 0, 0, 1});

        assertArrayEquals(data, dhcp6.serialize());
    }

    @Test
    public void serializeReply() throws Exception {
        DHCP6 dhcp6 = new DHCP6();
        dhcp6.setMsgType(DHCP6.MsgType.REPLY.value());
        dhcp6.setTransactionId(XID_2);
        List<Dhcp6Option> options = Lists.newArrayList();

        // IA address
        Dhcp6IaAddressOption iaAddressOption = new Dhcp6IaAddressOption();
        iaAddressOption.setIp6Address(IA_ADDRESS);
        iaAddressOption.setPreferredLifetime(PREFFERRED_LT_SERVER);
        iaAddressOption.setValidLifetime(VALID_LT_SERVER);

        // IA NA
        Dhcp6IaNaOption iaNaOption = new Dhcp6IaNaOption();
        iaNaOption.setIaId(IA_ID);
        iaNaOption.setT1(T1_SERVER);
        iaNaOption.setT2(T2_SERVER);
        iaNaOption.setOptions(ImmutableList.of(iaAddressOption));
        options.add(iaNaOption);

        // Client ID
        Dhcp6Duid duid = new Dhcp6Duid();
        duid.setDuidType(Dhcp6Duid.DuidType.DUID_LLT);
        duid.setHardwareType((short) 1);
        duid.setDuidTime(CLIENT_DUID_TIME);
        duid.setLinkLayerAddress(CLIENT_MAC.toBytes());
        Dhcp6ClientIdOption clientIdOption = new Dhcp6ClientIdOption();
        clientIdOption.setDuid(duid);
        options.add(clientIdOption);

        // Server ID
        Dhcp6Option option = new Dhcp6Option();
        option.setCode(DHCP6.OptionCode.SERVERID.value());
        option.setLength((short) 14);
        option.setData(new byte[]{0, 1, 0, 1, 32, -1, -8, -17, 0, -103, 102, 0, 0, 1});
        options.add(option);

        dhcp6.setOptions(options);

        Dhcp6RelayOption relayOption = new Dhcp6RelayOption();
        relayOption.setPayload(dhcp6);

        assertArrayEquals(Resources.toByteArray(Dhcp6RelayTest.class.getResource(REPLY)),
                          dhcp6.serialize());
    }
}
