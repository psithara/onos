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
package org.onosproject.bgpio.types.attr;

import java.util.Arrays;
import java.util.Objects;

import org.jboss.netty.buffer.ChannelBuffer;
import org.onosproject.bgpio.exceptions.BGPParseException;
import org.onosproject.bgpio.types.BGPErrorType;
import org.onosproject.bgpio.types.BGPValueType;
import org.onosproject.bgpio.util.Validation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.MoreObjects;

/**
 * Implements BGP unreserved bandwidth attribute.
 */
public class BgpLinkAttrUnRsrvdLinkBandwidth implements BGPValueType {

    protected static final Logger log = LoggerFactory
            .getLogger(BgpLinkAttrUnRsrvdLinkBandwidth.class);

    public static final int MAX_BANDWIDTH_LEN = 4;
    public static final int NO_OF_BITS = 8;
    public static final int NO_OF_PRIORITY = 8;

    public short sType;

    /* ISIS administrative group */
    private float[] maxUnResBandwidth;

    /**
     * Constructor to initialize the values.
     *
     * @param maxUnResBandwidth Maximum Unreserved bandwidth
     * @param sType returns the tag value
     */
    BgpLinkAttrUnRsrvdLinkBandwidth(float[] maxUnResBandwidth, short sType) {
        this.maxUnResBandwidth = Arrays.copyOf(maxUnResBandwidth,
                                               maxUnResBandwidth.length);
        this.sType = sType;
    }

    /**
     * Reads the BGP link attributes of Maximum link bandwidth.
     *
     * @param cb Channel buffer
     * @return object of type BgpLinkAttrMaxLinkBandwidth
     * @throws BGPParseException while parsing BgpLinkAttrMaxLinkBandwidth
     */
    public static BgpLinkAttrUnRsrvdLinkBandwidth read(ChannelBuffer cb,
                                                       short sType)
                                                               throws BGPParseException {
        float[] maxUnResBandwidth;
        short lsAttrLength = cb.readShort();

        if ((lsAttrLength != MAX_BANDWIDTH_LEN * NO_OF_PRIORITY)
                || (cb.readableBytes() < lsAttrLength)) {
            Validation.validateLen(BGPErrorType.UPDATE_MESSAGE_ERROR,
                                   BGPErrorType.ATTRIBUTE_LENGTH_ERROR,
                                   lsAttrLength);
        }

        maxUnResBandwidth = new float[NO_OF_PRIORITY];
        for (int i = 0; i < NO_OF_PRIORITY; i++) {
            maxUnResBandwidth[i] = ieeeToFloatRead(cb.readInt()) * NO_OF_BITS;
        }

        return new BgpLinkAttrUnRsrvdLinkBandwidth(maxUnResBandwidth, sType);
    }

    /**
     * Returns maximum unreserved bandwidth.
     *
     * @return unreserved bandwidth.
     */
    float[] getLinkAttrUnRsrvdLinkBandwidth() {
        return maxUnResBandwidth;
    }

    /**
     * Parse the IEEE floating point notation and returns it in normal float.
     *
     * @param iVal IEEE floating point number
     * @return normal float
     */
    static float ieeeToFloatRead(int  iVal) {
        iVal = (((iVal & 0xFF) << 24) | ((iVal & 0xFF00) << 8)
                | ((iVal & 0xFF0000) >> 8) | ((iVal >> 24) & 0xFF));

        return Float.intBitsToFloat(iVal);
    }

    @Override
    public short getType() {
        return this.sType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(maxUnResBandwidth);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof BgpLinkAttrUnRsrvdLinkBandwidth) {
            BgpLinkAttrUnRsrvdLinkBandwidth other = (BgpLinkAttrUnRsrvdLinkBandwidth) obj;
            return Objects.equals(maxUnResBandwidth, other.maxUnResBandwidth);
        }
        return false;
    }

    @Override
    public int write(ChannelBuffer cb) {
        // TODO This will be implemented in the next version
        return 0;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass()).omitNullValues()
                .add("maxUnResBandwidth", maxUnResBandwidth).toString();
    }
}
