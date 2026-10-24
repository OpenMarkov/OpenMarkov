package org.openmarkov.learning.algorithm.nbderived.spnb;

import org.openmarkov.core.action.base.ListPNEdit;
import org.openmarkov.core.action.base.PNEdit;
import org.openmarkov.core.action.base.linkEdits.AddLinkEdit;
import org.openmarkov.core.action.base.linkEdits.BaseLinkEdit;
import org.openmarkov.core.model.database.CaseDatabase;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.constraint.DistinctLinks;
import org.openmarkov.core.model.network.constraint.MaxNumParents;
import org.openmarkov.learning.metric.Metric;
import org.openmarkov.learning.core.algorithm.LearningAlgorithmType;
import org.openmarkov.learning.core.util.LearningEditProposal;
import org.openmarkov.learning.core.util.ModelNetUse;
import org.openmarkov.learning.core.util.ScoreEditMotivation;
import org.openmarkov.learning.metric.cmi.accuracy.Accuracy;
import org.openmarkov.learning.algorithm.nbderived.common.DiscriminativeAlgorithm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;


@LearningAlgorithmType(name = "Superparent naive bayes", discriminative = true, supportsUnobservedVariables = false,
		metrics = "Accuracy")
public class SuperParentNBAlgorithm extends DiscriminativeAlgorithm {
    
    
    /**
     *         SUPERPARENT ALGORITHM
     *             0. Initialize network to naive Bayes.
     *             1. Evaluate the current classifier.
     *             2. Consider making each node a SuperParent.  Let ASP be the SuperParent which increases accuracy the most.
     *             3. Consider an arc from ASP to each orphan. If the best such arc improves accuracy, keep it and go to 2.
     *             Else: Return the current classifier.
     *
     * The superparents, the orphans and the accuracy to beat are read from the network as it is, so that searching
     * changes nothing: they change when an edit is applied, undone or made by hand.
     */
    
    
    private boolean sameSP;
    
    /** The accuracy of each set of links between features that has been scored, keyed by those links. */
    private final Map<Set<String>, Double> accuracies = new HashMap<>();
    
    public SuperParentNBAlgorithm(ProbNet probNet, CaseDatabase caseDatabase, Metric metric, Double alpha) {
        super(probNet, caseDatabase, metric, alpha);
    }
    
    public SuperParentNBAlgorithm(ProbNet probNet, CaseDatabase caseDatabase, Metric metric, Double alpha, boolean sp) {
        this(probNet, caseDatabase, metric, alpha);
        this.sameSP = sp;
    }
    
    
    @Override public void init(ModelNetUse modelNetUse) {
        if (metric instanceof Accuracy) {
            ((Accuracy) metric).setClassVariable(this.classVariableName);
            ((Accuracy) metric).setAugmentedNet(true);
            ((Accuracy) metric).setAlpha(alpha);
            
        }
        accuracies.clear();
        
        setRelationsForRootVariable();
        MaxNumParents maxNumParentsConstraint = new MaxNumParents(2);
        //this.probNet.addConstraint(new NoCycle(), true);
        this.probNet.addConstraint(maxNumParentsConstraint);
        this.probNet.removeConstraint(new DistinctLinks());
        if (metric instanceof Accuracy) {
            // Step 1: the classifier to beat is the naive Bayes that the search starts from
            ((Accuracy) metric).resetCache();
            accuracies.put(linksBetweenFeatures(List.of()), metric.getScore());
        }
    }
    
    
    /**
     * Method to obtain the edit with the highest associated score.
     *
     * @param learnedNet net to learn.
     * @return {@code PNEdit} edit with the highest associated score.
     */
    @Override
    protected LearningEditProposal getOptimalEdit(boolean onlyAllowedEdits,
                                                  boolean onlyPositiveEdits) {
        List<Node> superParents = getNonRootNodes().stream()
                .filter(node -> node.getChildren().stream().anyMatch(child -> child != getRootNode())).toList();
        List<Node> orphans = getNonRootNodes().stream()
                .filter(node -> node.getParents().stream().allMatch(parent -> parent == getRootNode())).toList();
        if (this.sameSP && !superParents.isEmpty()) {
            // With a unique superparent there is a single step
            return null;
        }
        double currentAccuracy = currentAccuracy();
        // The superparent is the candidate with the highest accuracy; what it adds must beat the current classifier
        while (true) {
            double bestParentScore = Double.NEGATIVE_INFINITY;
            double bestPartialScore = currentAccuracy;
            BaseLinkEdit bestEdit = null;
            Node bestParent = null;
            AddLinkEdit bestParentLink = null;

            for (Node nodeParent : subtractListFromNonRootNode(superParents)) {
                AddLinkEdit addLink = new AddLinkEdit(probNet, getRootNode().getVariable(), nodeParent.getVariable(), true);
                double addScore = metric.getScore(addLink);

                if (!isEditAlreadyConsidered(addLink) && !isBlocked(addLink)
                        && (!onlyAllowedEdits || addLink.getNodeFrom() == getRootNode() || isAllowed(addLink))
                        && addScore >= bestParentScore
                ) {
                    bestParentScore = addScore;
                    bestParent = nodeParent;
                    bestParentLink = addLink;
                }
            }

            if (bestParent == null || orphans.isEmpty()) {
                return null;
            }
            final Node selectedParent = bestParent;
            // A node cannot be its own parent
            List<Node> children = orphans.stream().filter(orphan -> orphan != selectedParent).toList();
            if (this.sameSP) {
                if (!(bestParentScore > currentAccuracy || !onlyPositiveEdits)) {
                    return null;
                }
                // sameSP mode: connect bestParent to ALL orphans via a compound edit
                List<PNEdit> linkEdits = children.stream()
                        .map(orphan -> (PNEdit) new AddLinkEdit(probNet, selectedParent.getVariable(),
                                orphan.getVariable(), true))
                        .toList();
                ListPNEdit compoundEdit = new ListPNEdit(probNet, linkEdits);
                // The next search offers the next superparent
                markEditAsConsidered(bestParentLink);
                if (isBlockedCompound(linkEdits)) {
                    continue;
                }
                if (!linkEdits.isEmpty() && (!onlyAllowedEdits || isAllowed(compoundEdit))) {
                    accuracies.put(linksBetweenFeatures(linkEdits), bestParentScore);
                    return new LearningEditProposal(compoundEdit, new ScoreEditMotivation(bestParentScore));
                }
                return null;
            }
            for (Node nodeChild : children) {
                AddLinkEdit addLink = new AddLinkEdit(probNet, bestParent.getVariable(), nodeChild.getVariable(), true);
                double addScore = metric.getScore(addLink);

                if (!isEditAlreadyConsidered(addLink) && !isBlocked(addLink)
                        && (!onlyAllowedEdits || addLink.getNodeFrom() == getRootNode() || isAllowed(addLink))
                        && (addScore > bestPartialScore || !onlyPositiveEdits)
                ) {
                    bestEdit = addLink;
                    bestPartialScore = addScore;
                }
            }
            if (bestEdit == null) {
                return null;
            }
            markEditAsConsidered(bestEdit);
            accuracies.put(linksBetweenFeatures(List.of(bestEdit)), bestPartialScore);
            return LearningEditProposal.scored(bestEdit, bestPartialScore);
        }
    }
    
    /** The accuracy of the network as it is: the one recorded when it was scored, or computed now. */
    private double currentAccuracy() {
        if (!(metric instanceof Accuracy accuracy)) {
            return 0.0;
        }
        return accuracies.computeIfAbsent(linksBetweenFeatures(List.of()), links -> {
            List<Variable[]> childAndParent = new ArrayList<>();
            for (Node node : getNonRootNodes()) {
                node.getParents().stream().filter(parent -> parent != getRootNode())
                    .forEach(parent -> childAndParent.add(new Variable[]{node.getVariable(), parent.getVariable()}));
            }
            return accuracy.computeAugmentedNetAccuracy(childAndParent);
        });
    }
    
    /** The links between features of the network, together with {@code added}, written as «parent->child». */
    private Set<String> linksBetweenFeatures(List<? extends PNEdit> added) {
        Set<String> links = new TreeSet<>();
        for (Node node : getNonRootNodes()) {
            node.getParents().stream().filter(parent -> parent != getRootNode())
                .forEach(parent -> links.add(parent.getName() + "->" + node.getName()));
        }
        for (PNEdit edit : added) {
            BaseLinkEdit link = (BaseLinkEdit) edit;
            links.add(link.getVariableFrom().getName() + "->" + link.getVariableTo().getName());
        }
        return links;
    }
    
    /** Whether the compound edit made of {@code linkEdits} has been blocked. */
    private boolean isBlockedCompound(List<PNEdit> linkEdits) {
        return getBlockedEdits().stream().anyMatch(blocked -> blocked.getEdit() instanceof ListPNEdit compound
                && compound.getEdits().toList().equals(linkEdits));
    }
    
    
    private List<Node> subtractListFromNonRootNode(Collection<Node> listToSubtract) {
        return getNonRootNodes().stream().filter(n -> !listToSubtract.contains(n)).collect(Collectors.toList());
    }
}
