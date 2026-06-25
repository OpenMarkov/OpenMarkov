/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network;

import org.jetbrains.annotations.NotNull;
import org.openmarkov.core.localize.Localizable;
import org.openmarkov.core.stringformat.LocalizationFormatter;
import org.openmarkov.java.enumUtils.EnumUtils;

import java.io.Serializable;

/**
 * Enumerates the domain types a variable can have: discrete finite states,
 * continuous numeric, or discretized (continuous domain mapped to intervals).
 *
 * @author manuel
 */
public enum VariableType implements Serializable, Localizable {
    FINITE_STATES,
    NUMERIC,
    DISCRETIZED,
    EVENT;
    
    /*
                <Continuous value="Numeric"/>
                <Discrete value="Finite states"/>
                <Discretized value="Discretized"/>
                <Event value="Event"/>
     */
    

	public String toString() {
        return switch (this){
            case FINITE_STATES -> "Finite states";
            case NUMERIC -> "Numeric";
            case DISCRETIZED -> "Discretized";
            case EVENT -> "Event";
        };
	}
    
    public String toXMLTag(){
        return EnumUtils.toCamelCase(this);
    }
    
    @Override public @NotNull String path() {
        return "";
    }
    
    @Override public @NotNull String localize(LocalizationFormatter formatter) {
        return this.toString();
    }
    
    public static VariableType[] of(NodeType nodeType){
       return switch (nodeType){
           case CHANCE, DECISION -> new VariableType[]{VariableType.FINITE_STATES, VariableType.DISCRETIZED, VariableType.NUMERIC};
           case UTILITY -> new VariableType[]{VariableType.NUMERIC};
           case EVENT -> new VariableType[]{VariableType.EVENT};
           case SV_SUM, SV_PRODUCT -> new VariableType[]{};
       };
    }
    
}
