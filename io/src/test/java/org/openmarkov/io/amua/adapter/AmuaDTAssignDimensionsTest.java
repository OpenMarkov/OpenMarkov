package org.openmarkov.io.amua.adapter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.Criterion;
import org.openmarkov.io.amua.adatper.*;
import org.openmarkov.io.amua.model.*;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AmuaDTAssignDimensionsTest {

    Double defaultWTP;
    private List<Criterion> criteria;
    private AmuaDTUnicriteriaNode amuaUnicriteriaNode;
    private AmuaDTCENode amuaDTCENode;
    List<AmuaDimensionInfo> unicriteriaDimensions;
    List<AmuaDimensionInfo> ceDimensions;

    @BeforeEach
    void setup(){
        configCriteriaList();
        configUnicriteriaInfo();
        configCEInfo();
    }

    @Test
    void ceMissingCriterionThrows() {
        List<Criterion> wrongCriteria = List.of(new Criterion("Effectiveness", "QALY"));
        AmuaDTAssignDimensions dimAssign = new AmuaDTAssignDimensions();
        assertThrows(IllegalStateException.class, () -> dimAssign.assignDimensions(wrongCriteria, AmuaModel.COST_EFFECTIVENESS_DT, amuaDTCENode));
    }

    @Test
    void decisionNodeNotFoundThrows() {
        AmuaDTAssignDimensions dimAssign = new AmuaDTAssignDimensions();
        AmuaDTCENode nodeWithoutDecision = new AmuaDTCENode();
        assertThrows(IllegalArgumentException.class, () -> dimAssign.assignDimensions(criteria, AmuaModel.COST_EFFECTIVENESS_DT, nodeWithoutDecision));
    }

    @Test
    void unsupportedModelThrows() {
        AmuaDTAssignDimensions dimAssign = new AmuaDTAssignDimensions();
        assertThrows(NullPointerException.class, () -> dimAssign.assignDimensions(criteria, null, amuaDTCENode));
    }

    @Test
    void dimensionConstructorTest() {
        List<AmuaDimensionInfo> dimensions = new ArrayList<>();
        dimensions.add(new AmuaDimensionInfo("Cost", "$", 2));
        dimensions.add(new AmuaDimensionInfo("Effectiveness", "QALY", 3));

        int analysisType = 1;
        int objective = 2;
        int objectiveDim = 0;
        int costDim = 0;
        int effectDim = 1;
        String baseScenario = "Treatment A";
        double WTP = 100;
        int extendedDim = 0;

        AmuaDTDimensions result = new AmuaDTDimensions(
                dimensions,
                analysisType,
                objective,
                objectiveDim,
                costDim,
                effectDim,
                baseScenario,
                WTP,
                extendedDim
        );

        assertEquals(dimensions, result.getDimensions());
        assertEquals(analysisType, result.getAnalysisType());
        assertEquals(objective, result.getObjective());
        assertEquals(objectiveDim, result.getObjectiveDim());
        assertEquals(costDim, result.getCostDim());
        assertEquals(effectDim, result.getEffectDim());
        assertEquals(baseScenario, result.getBaseScenario());
        assertEquals(WTP, result.getWTP());
        assertEquals(extendedDim, result.getExtendedDim());
    }

    @Test
    void assignDimensionsUnicriteriaTest() {
        AmuaDTAssignDimensions dimAsssign = new AmuaDTAssignDimensions();
        AmuaDTDimensions result = dimAsssign.assignDimensions(criteria, AmuaModel.UNICRITERIA_DT, amuaUnicriteriaNode);

        assertEquals(unicriteriaDimensions.get(0).getName(), result.getDimensions().get(0).getName());
        assertEquals(unicriteriaDimensions.get(0).getSymbols(), result.getDimensions().get(0).getSymbols());
        assertEquals(unicriteriaDimensions.get(0).getDecimals(), result.getDimensions().get(0).getDecimals());
        assertEquals(AmuaConstants.ANALYSIS_TYPE_EV, result.getAnalysisType());
        assertEquals(AmuaConstants.OBJECTIVE_MAXIMIZE, result.getObjective());
        assertEquals(0, result.getCostDim());
        assertEquals(0, result.getEffectDim());
        assertEquals(0, result.getExtendedDim());
        assertNull(result.getBaseScenario());
        assertEquals(0, result.getWTP());
    }

    @Test
    void assignDimensionsCETest() {
        AmuaDTAssignDimensions dimAsssign = new AmuaDTAssignDimensions();
        AmuaDTDimensions result = dimAsssign.assignDimensions(criteria, AmuaModel.COST_EFFECTIVENESS_DT, amuaDTCENode);

        for (int i = 0; i < criteria.size(); i++) {
            assertEquals(ceDimensions.get(i).getName(), result.getDimensions().get(i).getName());
            assertEquals(ceDimensions.get(i).getSymbols(), result.getDimensions().get(i).getSymbols());
            assertEquals(ceDimensions.get(i).getDecimals(), result.getDimensions().get(i).getDecimals());
        }
        assertEquals(AmuaConstants.ANALYSIS_TYPE_CEA, result.getAnalysisType());
        assertEquals(AmuaConstants.OBJECTIVE_MAXIMIZE, result.getObjective());
        assertEquals(0, result.getCostDim());
        assertEquals(1, result.getEffectDim());
        assertEquals(0, result.getExtendedDim());
        assertNotNull(result.getBaseScenario());
        assertEquals(defaultWTP, result.getWTP());
    }

    private void configCriteriaList(){
        Criterion costCriterion = new Criterion("Cost", "$");
        costCriterion.setCECriterion(Criterion.CECriterion.Cost);

        Criterion effectivenessCriterion = new Criterion("Effectiveness", "QALY");
        effectivenessCriterion.setCECriterion(Criterion.CECriterion.Effectiveness);

        effectivenessCriterion.setUnicriterizationScale(100); // wtp
        defaultWTP = effectivenessCriterion.getUnicriterizationScale();

        criteria = List.of(costCriterion, effectivenessCriterion);
    }

    private void configUnicriteriaInfo(){
        amuaUnicriteriaNode = new AmuaDTUnicriteriaNode();
        unicriteriaDimensions = new ArrayList<>();
        unicriteriaDimensions.add(new AmuaDimensionInfo("Utility", "u", AmuaConstants.DEFAULT_DECIMALS));
    }

    private void configCEInfo(){
        configMinCENodeInfo();
        ceDimensions = new ArrayList<>();
        ceDimensions.add(new AmuaDimensionInfo("Cost", "$", AmuaConstants.DEFAULT_DECIMALS));
        ceDimensions.add(new AmuaDimensionInfo("Effectiveness", "QALY", AmuaConstants.DEFAULT_DECIMALS));
    }

    private void configMinCENodeInfo(){ // baseScenario
        amuaDTCENode = new AmuaDTCENode();
        amuaDTCENode.setType(0);
        List <AmuaDTCENode> childNodes = new ArrayList<>();
        AmuaDTCENode childNode1 = new AmuaDTCENode();
        AmuaDTCENode childNode2 = new AmuaDTCENode();
        childNode1.setPartialUtility(new AmuaCEvalue(15, 4));
        childNode2.setPartialUtility(new AmuaCEvalue(15, 3));
        childNode1.setType(1);
        childNode2.setType(1);
        childNode1.setName("First child");
        childNode2.setName("Second child");
        childNodes.add(childNode1);
        childNodes.add(childNode2);
        amuaDTCENode.setChildNodes(childNodes);
    }

}