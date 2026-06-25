package org.openmarkov.integrationTests.integrationTests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.openmarkov.core.exception.*;
import org.openmarkov.core.io.ProbNetInfo;
import org.openmarkov.core.model.network.*;
import org.openmarkov.core.model.network.potential.DeltaPotential;
import org.openmarkov.core.model.network.potential.GTablePotential;
import org.openmarkov.inference.algorithm.temporalevaluation.tasks.TemporalEvaluation;
import org.openmarkov.inference.algorithm.variableElimination.tasks.VECEAnalysis;
import org.openmarkov.inference.algorithm.variableElimination.tasks.VECEPSA;
import org.openmarkov.io.probmodel.reader.PGMXReader;
import org.openmarkov.io.probmodel.reader.PGMXReader_0_2;

//import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

@Disabled("The network '21-gene-190909-psa.pgmx' is missing.")
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class mid21gene {
    
    
    // Delta parameter for Assert.Equals methods
    private final double deltaEquals = Math.pow(10, -4);
    
    private final int C_TEMPORAL_HORIZON = 600;
    
    private List<CEA_Scenario_Result> cea_scenario_results;
    
    private ProbNet probNet;
    private EvidenceCase preResolutionEvidence;
    
    @BeforeEach public void setUp() throws ParserException, URISyntaxException, FileNotFoundException {
        //Configurator.setRootLevel(Level.DEBUG);
        
        String networkName = "/networks/mid/21-gene-190909-psa.pgmx";
        
        // Load the network: ID-decide-test
        PGMXReader_0_2 pgmxReader = new PGMXReader_0_2();
        PGMXReader.NetworkAndEvidence probNetInfo = null;
        probNetInfo = pgmxReader.read(getClass().getClassLoader().getResource(networkName));
        assert probNetInfo != null;
        this.probNet = probNetInfo.probNet();

//		this.probNet.getInferenceOptions().getTemporalOptions().setHorizon(C_TEMPORAL_HORIZON);
//		this.probNet.setCycleLength(new CycleLength(CycleLength.Unit.MONTH));
        
        
        if (probNetInfo.evidence().size() != 0) {
            this.preResolutionEvidence = probNetInfo.evidence().get(0);
        }
        
        cea_scenario_results = new ArrayList<>();
    }
    
    @Disabled
    @Test
    public void prueba() throws NonProjectablePotentialException, IncompatibleEvidenceException, NotEvaluableNetworkException.NotApplicableNetwork, ConstraintViolatedException {
        //Node dec_21g = probNet.getNode("Dec: 21g");
        
        //State stado = dec_21g.getVariable().getState("no");
        //DeltaPotential potencial = ((DeltaPotential) dec_21g.getPotentials().get(0));
        //	potencial.setValue(0);
        
        evaluateScenario("demo");
    }
    
    
    @Test
    public void getCEA4Scenarios() throws NonProjectablePotentialException, IncompatibleEvidenceException, NotEvaluableNetworkException.NotApplicableNetwork, ConstraintViolatedException {
        // L-L-NC
        setScenario("AO low", "21g low", "no");
        evaluateScenario("L-L-NC");
        
        // L-L-C
        setScenario("AO low", "21g low", "yes");
        evaluateScenario("L-L-C");
        
        // L-I-NC
        setScenario("AO low", "21g int", "no");
        evaluateScenario("L-I-NC");
        
        // L-I-C
        setScenario("AO low", "21g int", "yes");
        evaluateScenario("L-I-C");
        
        // L-H-NC
        setScenario("AO low", "21g high", "no");
        evaluateScenario("L-H-NC");
        
        // L-H-C
        setScenario("AO low", "21g high", "yes");
        evaluateScenario("L-H-C");
        
        // L-N-NC
        setScenario("AO low", "21g N/A", "no");
        evaluateScenario("L-N-NC");
        
        // L-N-C
        setScenario("AO low", "21g N/A", "yes");
        evaluateScenario("L-N-C");
        
        // I-L-NC
        setScenario("AO int", "21g low", "no");
        evaluateScenario("I-L-NC");
        
        // I-L-C
        setScenario("AO int", "21g low", "yes");
        evaluateScenario("I-L-C");
        
        // I-I-NC
        setScenario("AO int", "21g int", "no");
        evaluateScenario("I-I-NC");
        
        // I-I-C
        setScenario("AO int", "21g int", "yes");
        evaluateScenario("I-I-C");
        
        // I-H-Nc
        setScenario("AO int", "21g high", "no");
        evaluateScenario("I-H-NC");
        
        // I-H-C
        setScenario("AO int", "21g high", "yes");
        evaluateScenario("I-H-C");
        
        // I-N-NC
        setScenario("AO int", "21g N/A", "no");
        evaluateScenario("I-N-NC");
        
        // I-N-C
        setScenario("AO int", "21g N/A", "yes");
        evaluateScenario("I-N-C");
        
        
        // H-L-NC
        setScenario("AO high", "21g low", "no");
        evaluateScenario("H-L-NC");
        
        // H-L-C
        setScenario("AO high", "21g low", "yes");
        evaluateScenario("H-L-C");
        
        // H-I-NC
        setScenario("AO high", "21g int", "no");
        evaluateScenario("H-I-NC");
        
        // H-I-C
        setScenario("AO high", "21g int", "yes");
        evaluateScenario("H-I-C");
        
        // H-H-NC
        setScenario("AO high", "21g high", "no");
        evaluateScenario("H-H-NC");
        
        // H-H-C
        setScenario("AO high", "21g high", "yes");
        evaluateScenario("H-H-C");
        
        // H-N-NC
        setScenario("AO high", "21g N/A", "no");
        evaluateScenario("H-N-NC");
        
        // H-N-C
        setScenario("AO high", "21g N/A", "yes");
        evaluateScenario("H-N-C");
        
//        printToExcel();
        
    }
  
    /* Commented out because it requires Apache POI library
    private void printToExcel() throws IOException {
        // Abstract output file
        File resultFile = new File("results.xlsx");
        LogManager.getLogger().debug("Output file: " + resultFile.getAbsolutePath());
        
        // OOXML Excel workbook
        Workbook workbook = new XSSFWorkbook();
        
        // Excel sheet
        Sheet sheet = workbook.createSheet("Results");
        
        // Heading row
        int rowNumber = 0;
        Row row = sheet.createRow(rowNumber);
        row.createCell(0).setCellValue("Scenario name");
        row.createCell(1).setCellValue("Cost");
        row.createCell(2).setCellValue("Effectiveness");
        row.createCell(3).setCellValue("Life time");
        
        rowNumber++;
        for (CEA_Scenario_Result result : cea_scenario_results) {
            
            row = sheet.createRow(rowNumber);
            row.createCell(0).setCellValue(result.scenario);
            row.createCell(1).setCellValue(result.cost);
            row.createCell(2).setCellValue(result.effectiveness);
            row.createCell(3).setCellValue(result.lifeTime);
            rowNumber++;
        }
        
        FileOutputStream outputStream = null;
        outputStream = new FileOutputStream(resultFile);
        workbook.write(outputStream);
        workbook.close();
    }*/
    
    private void evaluateScenario(String scenarioName)
            throws NonProjectablePotentialException, IncompatibleEvidenceException, NotEvaluableNetworkException.NotApplicableNetwork, ConstraintViolatedException {
        
    	System.out.println("Scenario: " + scenarioName);
        VECEAnalysis veceAnalysis = new VECEAnalysis(probNet);
        veceAnalysis.setPreResolutionEvidence(preResolutionEvidence);
        CEP cep = veceAnalysis.getCEP();
        double costs = cep.getCost(0);
        double effectiveness = cep.getEffectiveness(0);
        System.out.println("Cost: " + costs);
        System.out.println("Effectiveness: " + effectiveness);
        
        
        // Gets life_time
		/*
		VETemporalEvolution veTemporalEvolution = new VETemporalEvolution(probNet, probNet.getVariable("Life time", 0));
		HashMap<Variable, TablePotential> result = veTemporalEvolution.getTemporalEvolution();
		double life_time = 0;
		for (TablePotential potential : result.values()) {
			life_time += potential.getValues()[0];
		}
		LogManager.getLogger().info("Life years: " + life_time);
		*/
        
        CEA_Scenario_Result cea_scenario_result = new CEA_Scenario_Result();
        cea_scenario_result.scenario = scenarioName;
        cea_scenario_result.cost = costs;
        cea_scenario_result.effectiveness = effectiveness;
        //cea_scenario_result.lifeTime = life_time;
        cea_scenario_results.add(cea_scenario_result);
    }
    
    
    @Disabled
    @Test
    public void test() throws NonProjectablePotentialException,
            IncompatibleEvidenceException, NotEvaluableNetworkException.NotApplicableNetwork, ConstraintViolatedException {
        
        evaluateScenario("Demo");
    }
    
    private void setScenario(String str_ao_risk, String str_gen_risk, String chemo_dec)
            throws IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther {
        Node dec_21g = probNet.getNode("Dec: 21g");
        Node dec_chemo = probNet.getNode("Dec: chemo");
        Node ao_risk = probNet.getNode("AO risk");
        Node gen_risk = probNet.getNode("21g risk");
        
        Finding ao_finding = new Finding(ao_risk.getVariable(), ao_risk.getVariable().getState(str_ao_risk));
        Finding gen_finding = new Finding(gen_risk.getVariable(), gen_risk.getVariable().getState(str_gen_risk));
        
        ((DeltaPotential) dec_chemo.getPotentials().get(0)).setValue(dec_chemo.getVariable().getState(chemo_dec));
        
        if (str_gen_risk.equals("21g N/A")) {
            ((DeltaPotential) dec_21g.getPotentials().get(0)).setValue(dec_21g.getVariable().getState("no"));
        } else {
            ((DeltaPotential) dec_21g.getPotentials().get(0)).setValue(dec_21g.getVariable().getState("yes"));
        }
        
        preResolutionEvidence.removeFinding(ao_risk.getVariable());
        preResolutionEvidence.removeFinding(gen_risk.getVariable());
        preResolutionEvidence.addFinding(ao_finding);
        preResolutionEvidence.addFinding(gen_finding);
    }
    
    @Disabled
    @Test
    public void psa_test() throws NonProjectablePotentialException, IncompatibleEvidenceException, NotEvaluableNetworkException.NotApplicableNetwork, ConstraintViolatedException {
        for (int i = 1; i <= 5; i++) {
            long startTime, endTime;
            int numSim = 1000;
            VECEPSA vecepsa = new VECEPSA(probNet);
            vecepsa.setNumSimulations(numSim);
            vecepsa.setUseMultithreading(true);
            System.out.println("Iteration: " + i);
            System.out.println("Starting PSA with " + numSim + " simulations and multithreading");
            startTime = System.nanoTime();
            ArrayList<GTablePotential> cepPotentials = (ArrayList<GTablePotential>) vecepsa.getCEPPotentials();
            endTime = System.nanoTime();
            System.out.println("Total time: " + (startTime - endTime) + " ns.");
        }
        
    }
    
    @Disabled
    @Test
    public void temporalEvaluation() throws NonProjectablePotentialException, IncompatibleEvidenceException, NotEvaluableNetworkException.NotApplicableNetwork, ConstraintViolatedException {
        TemporalEvaluation temporalEvaluation = null;
        long startTime, endTime;
        System.out.println("Starting temporal evaluation");
        startTime = System.nanoTime();
        temporalEvaluation = new TemporalEvaluation(probNet);
        temporalEvaluation.setPreResolutionEvidence(preResolutionEvidence);
        GTablePotential atemporalUtility = (GTablePotential) temporalEvaluation.getAtemporalUtility();
        endTime = System.nanoTime();
        System.out.println("Total time: " + (startTime - endTime) + " ns.");
        
        System.out.println("Starting VECEAnalysis");
        startTime = System.nanoTime();
        VECEAnalysis veceAnalysis = new VECEAnalysis(probNet);
        veceAnalysis.setPreResolutionEvidence(preResolutionEvidence);
        CEP cep = veceAnalysis.getCEP();
        endTime = System.nanoTime();
        System.out.println("Total time: " + (startTime - endTime) + " ns.");
        
        
    }
    
    private class CEA_Scenario_Result {
        String scenario;
        double cost;
        double effectiveness;
        double lifeTime;
        
    }
    
}
