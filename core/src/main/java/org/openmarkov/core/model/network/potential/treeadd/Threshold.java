/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.potential.treeadd;

import org.openmarkov.core.localize.ClassLocalizable;

/**
 * A threshold is defined by a float value and a boolean that indicates if the
 * value delimits a closed or an open interval on the left and on the right
 * side. There are two possibilities )[, ](
 * <p>
 * It is used by TreeADDBranch when its topVariable is a numeric variable
 *
 * @author myebra
 */
public class Threshold implements Cloneable, ClassLocalizable {

	private final double limit;
	private boolean belongsToLeft; // if true --&gt; ](; if false --&gt; )[;

	public Threshold(double limit, boolean belongsToLeft) {
		this.limit = limit;
		this.belongsToLeft = belongsToLeft;
	}

	public Threshold(Threshold threshold) {
		this.limit = threshold.limit;
		this.belongsToLeft = threshold.belongsToLeft;
	}

	public double getLimit() {
		return this.limit;
	}

	public boolean belongsToLeft() {
		return belongsToLeft;
	}

	public void setBelongsToLeft(boolean belongsToLeft) {
		this.belongsToLeft = belongsToLeft;
	}

	/**
	 * @param value to check
	 * @return true if the value is above the limit value of the threshold object
	 **/
	public boolean isBelow(double value) {
		return value > this.limit || (value == this.limit && !belongsToLeft);
	}

	/**
	 * @param value to check
	 * @return true if the value is below the limit value of the threshold object
	 **/
	public boolean isAbove(double value) {
		return value < this.limit || (value == this.limit && belongsToLeft);
	}

	/**
	 * Compares the limit and the side it belongs to, which is everything a threshold is.
	 *
	 * <p>This used to take a Threshold rather than an Object, so it was an overload wearing the
	 * clothes of an override: no collection ever called it, and every {@code contains}, {@code remove}
	 * and {@code assertEquals} on thresholds silently fell back to comparing object identities. The
	 * body is unchanged; only the door it hangs on.
	 */
	@Override public boolean equals(Object object) {
		return object instanceof Threshold threshold
				&& limit == threshold.getLimit()
				&& belongsToLeft == threshold.belongsToLeft();
	}

	/**
	 * Consistent with {@link #equals}. Note that {@code belongsToLeft} is not final and
	 * {@link #setBelongsToLeft} can change it, so this hash can move; no hash-based collection of
	 * thresholds exists in the code today, and one must not hold a threshold that is still being
	 * edited.
	 */
	@Override public int hashCode() {
		return 31 * Double.hashCode(limit) + (belongsToLeft ? 1 : 0);
	}
    
    @Override protected Threshold clone() {
        return new Threshold(this);
    }
    
    @Override public String toString() {
        return this.localize();
    }
}
