/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.inference;

import org.jetbrains.annotations.NotNull;
import org.openmarkov.core.localize.ClassLocalizable;
import org.openmarkov.core.localize.Localizable;
import org.openmarkov.core.stringformat.LocalizationFormatter;

public class MulticriteriaOptions implements Cloneable, ClassLocalizable {

	private Type multicriteriaType;
	private String mainUnit;
	private boolean unicriterionOptionsShowed = false;
	private boolean ceOptionsShowed = false;

	public MulticriteriaOptions() {
		this.mainUnit = " ";
		this.multicriteriaType = Type.UNICRITERION;
	}

	public MulticriteriaOptions(Type multicriteriaType, String mainUnit) {
		this.multicriteriaType = multicriteriaType;
		this.mainUnit = mainUnit;
	}

	public MulticriteriaOptions(MulticriteriaOptions multiCriteriaOptions) {
		this.multicriteriaType = multiCriteriaOptions.getMulticriteriaType();
		this.mainUnit = multiCriteriaOptions.getMainUnit();
		this.ceOptionsShowed = multiCriteriaOptions.isCeOptionsShowed();
		this.unicriterionOptionsShowed = multiCriteriaOptions.isUnicriterionOptionsShowed();
	}

	public Type getMulticriteriaType() {
		return multicriteriaType;
	}

	public void setMulticriteriaType(Type multicriteriaType) {
		this.multicriteriaType = multicriteriaType;
	}

	public String getMainUnit() {
		return mainUnit;
	}

	public void setMainUnit(String mainUnit) {
		this.mainUnit = mainUnit;
	}

	@Override public MulticriteriaOptions clone() {
		return new MulticriteriaOptions(this);
	}

	public boolean isUnicriterionOptionsShowed() {
		return unicriterionOptionsShowed;
	}

	public void setUnicriterionOptionsShowed(boolean unicriterionOptionsShowed) {
		this.unicriterionOptionsShowed = unicriterionOptionsShowed;
	}

	public boolean isCeOptionsShowed() {
		return ceOptionsShowed;
	}

	public void setCeOptionsShowed(boolean ceOptionsShowed) {
		this.ceOptionsShowed = ceOptionsShowed;
	}
    
    public enum Type implements Localizable {
        UNICRITERION, COST_EFFECTIVENESS;
        
        
        @Override public @NotNull String path() {
            return "";
        }
        
        @Override public @NotNull String localize(LocalizationFormatter formatter) {
            return super.name();
        }
        
        @Override public String toString() {
            return this.localize();
        }
    }
    
    @Override public String toString() {
        return this.localize();
    }
}
