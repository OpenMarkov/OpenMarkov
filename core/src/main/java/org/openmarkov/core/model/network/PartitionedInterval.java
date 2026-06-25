/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network;

import org.openmarkov.core.localize.ClassLocalizable;

import java.io.Serializable;

/**
 * Defines a set of intervals
 *
 * @author fjdiez
 * @author manuel
 * @version 1.2 jlgozalo add method equals()
 * invariant belongsToLeftSide.length = limits.length
 * @since OpenMarkov 1.0
 */
public class PartitionedInterval implements Cloneable, Serializable, ClassLocalizable {
    
    // Attributes
    /**
     * This numbers delimits the set of subintervals
     */
    protected double[] limits;
    
    protected boolean[] belongsToLeftSide;
    
    /**
     * Number of sub-intervals
     */
    protected int numSubintervals;
    
    // Constructors
    
    /**
     * Condition: limits.size() == belongsToLeftSide.size()
     * Condition: limits[i] &#60;= limits[i+1]
     * Condition: if limits[i] == limits[i+1] then belongsToLeftSide[i] =
     * false and belongsToLeftSide[i+1] = true
     *
     * @param belongsToLeftSide Array pointing If belongs to left side
     * @param limits            Array of limits
     */
    public PartitionedInterval(double[] limits, boolean[] belongsToLeftSide) {
        this.limits = limits.clone();
        this.belongsToLeftSide = belongsToLeftSide.clone();
        numSubintervals = limits.length - 1;
    }
    
    /**
     * Creates a {@code PartitionedInterval} with only one Subinterval
     *
     * @param leftClosed  If left closed
     * @param min         Minimum
     * @param max         Maximum
     * @param rightClosed If right closed
     */
    public PartitionedInterval(boolean leftClosed, double min, double max, boolean rightClosed) {
        limits = new double[]{min, max};
        belongsToLeftSide = new boolean[]{leftClosed, rightClosed};
        numSubintervals = 1;
    }
    
    /**
     * Creates a {@code PartitionedInterval} from an Object[][] table
     *
     * @param values Values
     */
    public PartitionedInterval(Object[][] values) {
        int i;
        int numSubIntervals = values.length;
        double[] limits = new double[numSubIntervals + 1];
        boolean[] belongsToLeftSide = new boolean[numSubIntervals + 1];
        if (numSubIntervals > 1) {
            try {// id-name-symbol-value-separator-value-symbol
                for (i = 0; i < numSubIntervals; i++) {
                    belongsToLeftSide[i] = (values[i][2] == "[");
                    limits[i] = (Double) values[i][3];
                }
                limits[i] = (Double) values[i - 1][5];
                belongsToLeftSide[i] = (values[i - 1][6] == "]");
            } catch (NumberFormatException ex) {
                //TODO: set the actions to capture this exception if happens
            }
            this.limits = limits.clone();
            this.belongsToLeftSide = belongsToLeftSide.clone();
            numSubintervals = limits.length - 1;
        } else {
            boolean leftClosed = (values[0][2] != "[");
            boolean rightClosed = (values[0][6] == "]");
            double min = (Double) values[0][3];
            double max = (Double) values[0][5];
            this.limits = new double[]{min, max};
            this.belongsToLeftSide = new boolean[]{leftClosed, rightClosed};
            this.numSubintervals = 1;
            
        }
    }
    
    // Methods
    
    /**
     * @param number Number
     *
     * @return true if the value is included between the outside limits
     */
    public boolean contains(double number) {
        return (
                ((limits[0] < number) && (number < limits[limits.length - 1])) || (
                        (number == limits[0]) && !belongsToLeftSide[0]
                ) || (
                        (number == limits[limits.length - 1]) && belongsToLeftSide[limits.length - 1]
                )
        );
    }
    
    /**
     * @param number {@code double}
     *
     * @return The number of subinterval where is located the number (0, 1, ...)
     * or -1 if it is outside
     */
    public int indexOfSubinterval(double number) {
        if (number == Double.NEGATIVE_INFINITY && this.limits[0] == Double.NEGATIVE_INFINITY) {
            return 0;
        }
        if (number == Double.POSITIVE_INFINITY && this.limits[this.limits.length - 1] == Double.POSITIVE_INFINITY) {
            return this.limits.length - 1 - 1;
        }
        for (int i = 0; i < limits.length - 1; i++) {
            if (((limits[i] < number) && (number < limits[i + 1])) || ((number == limits[i]) && !belongsToLeftSide[i])
                    || ((number == limits[i + 1]) && belongsToLeftSide[i + 1])) {
                return i;
            }
        }
        return -1;
    }
    
    /**
     * This method remove the index-th subinterval.
     *
     * @param index {@code int}
     */
    public void removeSubinterval(int index) {
        double[] newLimits = new double[limits.length - 1];
        boolean[] newBelongsToLeftSide = new boolean[limits.length - 1];
        for (int i = 0; i < newLimits.length; i++) {
            if (i <= index) {
                newLimits[i] = limits[i];
                newBelongsToLeftSide[i] = belongsToLeftSide[i];
            } else {
                newLimits[i] = limits[i + 1];
                newBelongsToLeftSide[i] = belongsToLeftSide[i + 1];
            }
        }
        
        limits = newLimits.clone();
        belongsToLeftSide = newBelongsToLeftSide.clone();
        
    }
    
    /**
     * @return numSubintervals. {@code int}
     */
    public int getNumSubintervals() {
        return numSubintervals = limits.length - 1;
    }
    
    /**
     * @return limits. {@code double[]}
     */
    public double[] getLimits() {
        return limits;
    }
    
    /**
     * @param index Index
     *
     * @return limit. {@code double}
     */
    public double getLimit(int index) {
        return limits[index];
    }
    
    /**
     * @return belongsToLeftSide. {@code boolean[]}
     */
    public boolean[] getBelongsToLeftSide() {
        return belongsToLeftSide;
    }
    
    /**
     * @param index {@code int}
     *
     * @return belongsToLeftSide. {@code boolean}
     */
    public boolean getBelongsToLeftSide(int index) {
        return belongsToLeftSide[index];
    }
    
    /**
     * @param index {@code int}
     *
     * @return belongsTo. {@code String}
     */
    public String getBelongsTo(int index) {
        return belongsToLeftSide[index] ? "left" : "right";
    }
    
    /**
     * @return min
     */
    public double getMin() {
        return limits[0];
    }
    
    /**
     * @return max
     */
    public double getMax() {
        return limits[getNumSubintervals()];
    }
    
    /**
     * @return leftClosed
     */
    public boolean isLeftClosed() {
        return !belongsToLeftSide[0];
    }
    
    /**
     * @return rightClosed
     */
    public boolean isRightClosed() {
        return belongsToLeftSide[getNumSubintervals()];
    }
    
    /**
     * @param indexOfLimit         Index of limit
     * @param newLimit             New limit
     * @param newBelongsToLeftSide If new limit belongs to left side
     *                             Condition: newLimit &#60; limit[indexOfLimit+1] &#x26;&#x26; newLimit &#62;
     *                             limit[indexOfLimit-1]
     *                             Condition: if limit[indexOfLimit-1] = newLimit then
     *                             belongsToLeftSide[indexOfLimit] = true &#x26;&#x26;
     *                             belongsToLeftSide[indexOfLimit-1] = false
     *                             Condition: if limit[indexOfLimit+1] = newLimit then
     *                             belongsToLeftSide[indexOfLimit] = false &#x26;&#x26;
     *                             belongsToLeftSide[indexOfLimit+1] = true
     */
    public void changeLimit(int indexOfLimit, double newLimit, boolean newBelongsToLeftSide) {
        limits[indexOfLimit] = newLimit;
        belongsToLeftSide[indexOfLimit] = newBelongsToLeftSide;
    }
    
    /**
     * Convert a PartitionedInterval in an array of arrays of objects with the
     * same elements.
     *
     * @return an array of arrays of objects that has the same elements.
     */
    public Object[][] convertToTableFormat() {
        
        int i;
        int numColumns = 6; // name-symbol-value-separator-value-symbol
        
        int numIntervals = getNumSubintervals();
        double[] limits = getLimits();
        boolean[] belongsToLeftSide = getBelongsToLeftSide();
        Object[][] data = new Object[numIntervals][numColumns];
        for (i = 0; i < numIntervals; i++) {
            //for (i = numIntervals-1; i <=0; i--) {
            data[i][0] = ""; // name
            data[i][1] = (belongsToLeftSide[i] ? "(" : "["); // low interval
            // symbol
            data[i][2] = limits[i]; // low interval value
            data[i][3] = ","; // separator ","
            data[i][4] = limits[i + 1]; // high interval value
            data[i][5] = (belongsToLeftSide[i + 1] ? "]" : ")"); // high
            // interval
            // symbol
        }
        return data;
        
    }
    
    /**
     * print a readable format of the Partitioned Interval
     */
    public String toString() {
        String out = "Partitioned Interval \n  > numSubIntervals = " + getNumSubintervals() + "\n";
        for (int i = 0; i < getNumSubintervals(); i++) {
            out += "   > interval[" + i + "]="
                    + (!belongsToLeftSide[i] ? "[" : "(")
                    + limits[i]
                    + ","
                    + limits[i + 1]
                    + (!belongsToLeftSide[i + 1] ? ")" : "]") +
                    "\n";
        }
        return out;
    }
    
    /**
     * Indicates whether some other object is "equal to" this one. The graph
     * which the node belongs to is not compared.
     *
     * @param obj object to compare with this one. It must be a NodeProperties
     *            instance.
     */
    @Override public boolean equals(Object obj) {
        
        PartitionedInterval otherInterval;
        
        boolean result = true;
        if (obj instanceof PartitionedInterval) {
            otherInterval = (PartitionedInterval) obj;
            if ((numSubintervals == otherInterval.numSubintervals) && (
                    this.belongsToLeftSide.length == otherInterval.belongsToLeftSide.length
            ) && (this.limits.length == otherInterval.limits.length)) {
                for (int i = 0; result && (i < this.belongsToLeftSide.length); i++) {
                    result = this.belongsToLeftSide[i] == otherInterval.belongsToLeftSide[i];
                }
                for (int i = 0; result && (i < this.limits.length); i++) {
                    result = this.limits[i] == otherInterval.limits[i];
                }
            } else {
                result = false;
            }
        } else {
            result = false;
        }
        return result;
    }
    
    @Override public PartitionedInterval clone() {
        PartitionedInterval cloned = new PartitionedInterval(this.limits, this.belongsToLeftSide);
        cloned.numSubintervals = this.numSubintervals;
        return cloned;
    }
}
