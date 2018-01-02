/*
 * Copyright 2015 James F. Bowring and CIRDLES.org.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 * This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
 * See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
 * Any modifications to this file will be lost upon recompilation of the source schema. 
 * Generated on: 2015.10.25 at 07:31:08 AM EDT 
*/


package org.cirdles.squid.prawn;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for RunTableEntryParameterNames.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="RunTableEntryParameterNames"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="label"/&gt;
 *     &lt;enumeration value="amu"/&gt;
 *     &lt;enumeration value="trim_amu"/&gt;
 *     &lt;enumeration value="autocenter_offset_amu"/&gt;
 *     &lt;enumeration value="count_time_sec"/&gt;
 *     &lt;enumeration value="delay_sec"/&gt;
 *     &lt;enumeration value="collector_focus"/&gt;
 *     &lt;enumeration value="centering_time_sec"/&gt;
 *     &lt;enumeration value="centering_frequency"/&gt;
 *     &lt;enumeration value="detector_selection"/&gt;
 *     &lt;enumeration value="mc_lm_pos"/&gt;
 *     &lt;enumeration value="mc_hm_pos"/&gt;
 *     &lt;enumeration value="sc_reference"/&gt;
 *     &lt;enumeration value="sc_detector"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
//@XmlType(name = "RunTableEntryParameterNames")
@XmlEnum
public enum RunTableEntryParameterNames implements Serializable{

    /**
     *
     */
    @XmlEnumValue("label")
    LABEL("label"),

    /**
     *
     */
    @XmlEnumValue("amu")
    AMU("amu"),

    /**
     *
     */
    @XmlEnumValue("trim_amu")
    TRIM_AMU("trim_amu"),

    /**
     *
     */
    @XmlEnumValue("autocenter_offset_amu")
    AUTOCENTER_OFFSET_AMU("autocenter_offset_amu"),

    /**
     *
     */
    @XmlEnumValue("count_time_sec")
    COUNT_TIME_SEC("count_time_sec"),

    /**
     *
     */
    @XmlEnumValue("delay_sec")
    DELAY_SEC("delay_sec"),

    /**
     *
     */
    @XmlEnumValue("collector_focus")
    COLLECTOR_FOCUS("collector_focus"),

    /**
     *
     */
    @XmlEnumValue("centering_time_sec")
    CENTERING_TIME_SEC("centering_time_sec"),

    /**
     *
     */
    @XmlEnumValue("centering_frequency")
    CENTERING_FREQUENCY("centering_frequency"),

    /**
     *
     */
    @XmlEnumValue("detector_selection")
    DETECTOR_SELECTION("detector_selection"),

    /**
     *
     */
    @XmlEnumValue("mc_lm_pos")
    MC_LM_POS("mc_lm_pos"),

    /**
     *
     */
    @XmlEnumValue("mc_hm_pos")
    MC_HM_POS("mc_hm_pos"),

    /**
     *
     */
    @XmlEnumValue("sc_reference")
    SC_REFERENCE("sc_reference"),

    /**
     *
     */
    @XmlEnumValue("sc_detector")
    SC_DETECTOR("sc_detector");
    private final String value;

    RunTableEntryParameterNames(String v) {
        value = v;
    }

    /**
     *
     * @return
     */
    public String value() {
        return value;
    }

    /**
     *
     * @param v
     * @return
     */
    public static RunTableEntryParameterNames fromValue(String v) {
        for (RunTableEntryParameterNames c: RunTableEntryParameterNames.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
