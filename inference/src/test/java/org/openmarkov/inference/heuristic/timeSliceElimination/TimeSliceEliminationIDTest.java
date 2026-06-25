package org.openmarkov.inference.heuristic.timeSliceElimination;//package org.openmarkov.inference.heuristic.timeSliceElimination;
//
//import java.util.List;
//
//import org.openmarkov.core.exception.NotEvaluableNetworkException;
//import org.openmarkov.core.inference.InferenceAlgorithm;
//import org.openmarkov.core.inference.InferenceAlgorithmIDTest;
//import org.openmarkov.core.inference.heuristic.EliminationHeuristic;
//import org.openmarkov.core.inference.heuristic.HeuristicFactory;
//import org.openmarkov.core.model.network.ProbNet;
//import org.openmarkov.core.model.network.Variable;
//import org.openmarkov.inference.variableElimination.VariableElimination;
//
//public class TimeSliceEliminationIDTest extends InferenceAlgorithmIDTest{
//
//	@Override
//	public InferenceAlgorithm buildInferenceAlgorithm(ProbNet probNet)
//			throws NotEvaluableNetworkException {
//		InferenceAlgorithm inferenceAlgorithm = new VariableElimination(probNet);
//		inferenceAlgorithm.setHeuristicFactory(new HeuristicFactory() {
//
//			@Override
//			public EliminationHeuristic getHeuristic(ProbNet probNet, List<List<Variable>> variables) {
//				return new TimeSliceElimination(probNet, variables);
//			}
//		});
//		return inferenceAlgorithm;
//	}
//
//}