

package org.openmarkov.core.model.network;

import java.util.Objects;


/**
 * A criterion has a name and the units of measure, subclass equal due to I don't know how equality should be treated in other netorks
 * TODO Implement equal method in Criterion or change the name of this class
 *
 * @author cmyago
 * @version 1.0 implemented comparable
 * @version 2.0 25/04/2020 implements equal method. I need compare equality no order. Refactored from ComparableCriterion
 * 04/01/2023 FIXME to be merged with Criterion
 */
public class EqualCriterion extends Criterion {

	/**
	 * Constant with the default unit of a criterion
	 * Copied from Criterion because it is private
	 */
	private final static String defaultUnit = "---";


	/**
	 * Constructor with only one parameter
	 *
	 * @param criterionName Name of the criterion
	 */
	public EqualCriterion(String criterionName) {
		super(criterionName, defaultUnit);
	}



	public EqualCriterion(Criterion  criterion) {
		super(criterion);
	}


	/**
	 * Indicates whether some other object is "equal to" this one.
	 * Two EqualCriterion are equal if they have the same name; anything that is not an
	 * EqualCriterion, including null, is answered with false instead of an exception.
	 */
	@Override
	public boolean equals(Object obj) {
		return (obj instanceof EqualCriterion other)
				&& Objects.equals(this.getCriterionName(), other.getCriterionName());
	}

	/**
	 * Derived from the name, so that equal criteria share it and different names
	 * (almost always) do not.
	 */
	@Override
	public int hashCode() {
		return Objects.hashCode(getCriterionName());
	}


}
