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

public class TemporalOptions implements Cloneable, ClassLocalizable {

	private int horizon;

	private TransitionTime transition;

	public TemporalOptions() {
		// Number of slices now in the net
		horizon = 0; // Because a newly created net has 0 slices
		transition = TransitionTime.BEGINNING;
	}

	public TemporalOptions(TemporalOptions temporalOptions) {
		this.setHorizon(temporalOptions.horizon);
		this.setTransition(temporalOptions.getTransition());
	}

	public int getHorizon() {
		return horizon;
	}

	public void setHorizon(int horizon) {
		this.horizon = horizon;
	}

	public TransitionTime getTransition() {
		return transition;
	}

	public void setTransition(TransitionTime transition) {
		this.transition = transition;
	}

	@Override public TemporalOptions clone() {
		return new TemporalOptions(this);
	}
    
    public enum TransitionTime implements Localizable {
        BEGINNING, HALF, END;
        
        
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
