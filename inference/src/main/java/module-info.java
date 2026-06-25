open module org.openmarkov.inference {
	requires org.openmarkov.core;
	requires org.apache.logging.log4j;

	requires java.desktop;
	requires org.jetbrains.annotations;
	requires jeval;

	exports org.openmarkov.inference.heuristic.canoAndMoral;

	exports org.openmarkov.inference.algorithm.decompositionIntoSymmetricDANs;
	exports org.openmarkov.inference.algorithm.decompositionIntoSymmetricDANs.ceanalysis;
	exports org.openmarkov.inference.algorithm.decompositionIntoSymmetricDANs.core;
	exports org.openmarkov.inference.algorithm.decompositionIntoSymmetricDANs.evaluation;

	exports org.openmarkov.inference.algorithm.dlimidevaluation;
	exports org.openmarkov.inference.heuristic.hybridElimination;
	exports org.openmarkov.inference.heuristic.simpleElimination;
	exports org.openmarkov.inference.algorithm.huginPropagation;
	exports org.openmarkov.inference.algorithm.likelihoodWeighting;
	exports org.openmarkov.inference.algorithm.temporalevaluation.tasks;
	exports org.openmarkov.inference.algorithm.variableElimination;
	exports org.openmarkov.inference.algorithm.variableElimination.tasks;
	exports org.openmarkov.inference.algorithm.variableElimination.operation;
	exports org.openmarkov.inference.decisiontree.operation;
	exports org.openmarkov.inference.heuristic.minimalFillIn;
	exports org.openmarkov.inference.heuristic.weightedMinFill;
	exports org.openmarkov.inference.heuristic.minimalCliqueSize;
	exports org.openmarkov.inference.heuristic.rollout;
}
