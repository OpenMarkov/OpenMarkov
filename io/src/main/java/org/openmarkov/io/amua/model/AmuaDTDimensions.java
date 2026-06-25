package org.openmarkov.io.amua.model;

import java.util.List;

/**
 * Holds the dimensional and analysis information for an AMUA decision tree.
 *
 * @author Hugo Manuel
 * @version 1.0
 */

public class AmuaDTDimensions {

    private List<AmuaDimensionInfo> dimensions; // name, symbols, decimals
    private int analysisType;
    private int objective;
    private int objectiveDim;
    private int costDim;
    private int effectDim;
    private String baseScenario;
    private double WTP;
    private int extendedDim;

    /**
     * Public constructor.
     */
    public AmuaDTDimensions(List<AmuaDimensionInfo> dimensions,
                            int analysisType,
                            int objective,
                            int objectiveDim,
                            int costDim,
                            int effectDim,
                            String baseScenario,
                            double WTP,
                            int extendedDim) {
        this.dimensions = dimensions;
        this.analysisType = analysisType;
        this.objective = objective;
        this.objectiveDim = objectiveDim;
        this.costDim = costDim;
        this.effectDim = effectDim;
        this.baseScenario = baseScenario;
        this.WTP = WTP;
        this.extendedDim = extendedDim;
    }

    // getters
    public List<AmuaDimensionInfo> getDimensions() {return dimensions;}
    public int getAnalysisType() {return analysisType;}
    public int getObjective() {return objective;}
    public int getObjectiveDim() {return objectiveDim;}
    public int getCostDim() {return costDim;}
    public int getEffectDim() {return effectDim;}
    public String getBaseScenario() {return baseScenario;}
    public double getWTP() {return WTP;}
    public int getExtendedDim() {return extendedDim;}

    // setters
    public void setDimensions(List<AmuaDimensionInfo> dimensions) {this.dimensions = dimensions;}
    public void setAnalysisType(int analysisType) {this.analysisType = analysisType;}
    public void setObjective(int objective) {this.objective = objective;}
    public void setObjectiveDim(int objectiveDim) {this.objectiveDim = objectiveDim;}
    public void setCostDim(int costDim) {this.costDim = costDim;}
    public void setEffectDim(int effectDim) {this.effectDim = effectDim;}
    public void setBaseScenario(String baseScenario) {this.baseScenario = baseScenario;}
    public void setWTP(double WTP) {this.WTP = WTP;}
    public void setExtendedDim(int extendedDim) {this.extendedDim = extendedDim;}
}