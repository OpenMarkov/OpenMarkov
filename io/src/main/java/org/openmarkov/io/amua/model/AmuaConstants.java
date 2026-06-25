package org.openmarkov.io.amua.model;

/**
 * Defines constant values used in AMUA decision tree processing.
 *
 * @author Hugo Manuel
 * @version 1.0
 */

public final class AmuaConstants {
    private AmuaConstants() {}

    public static final int DEFAULT_DECIMALS = 4;

    // Analysis types
    public static final int ANALYSIS_TYPE_EV = 0; // Expected Value
    public static final int ANALYSIS_TYPE_CEA = 1; // Cost-Effectiveness Analysis
    public static final int ANALYSIS_TYPE_CBA = 2; // Cost-Benefit Analysis
    public static final int ANALYSIS_TYPE_CEEA = 3; // Cost-Effectiveness Extended Analysis

    // Objectives
    public static final int OBJECTIVE_MAXIMIZE = 0;
    public static final int OBJECTIVE_MINIMIZE = 1;
}