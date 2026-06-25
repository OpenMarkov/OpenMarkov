package org.openmarkov.io.amua;

import org.openmarkov.core.model.network.*;
import org.openmarkov.core.model.decisiontree.*;
import org.openmarkov.io.amua.adatper.*;
import org.openmarkov.io.amua.model.*;
import org.openmarkov.io.amua.writer.*;

import java.io.File;
import java.util.List;

/**
 * @author Hugo Manuel
 * @version 3.0
 */

public class AmuaExporter {

    private final DecisionTreeNode<?> treeNode;
    private final List<Criterion> criteria;
    private AmuaModel amuaModel;
    private AmuaDTNode<?> amuaTreeNode;
    private AmuaDTDimensions amuaDimInfo;

    private boolean isValidDT;
    private boolean hasBeenValidatedDT;
    private String validationErrorMessage;


    /**
     * Constructs an AmuaWriter for the given decision tree.
     *
     * @param treeNode root node of the decision tree in OpenMarkov format.
     * @throws IllegalArgumentException if the root node or its network is null.
     */
    public AmuaExporter(DecisionTreeNode<?> treeNode) {

        if (treeNode == null) {
            throw new IllegalArgumentException("The decision tree root node cannot be null.");
        }

        ProbNet probNet = treeNode.getNetwork();
        if (probNet == null) {
            throw new IllegalArgumentException("The decision tree has no associated ProbNet.");
        }

        this.treeNode = treeNode;
        this.criteria = probNet.getDecisionCriteria();

    }


    /**
     * Writes the decision tree to a file in Amua format.
     * Automatically validates if the tree is compatible before writing it
     *
     * @param outputFile destination file.
     * @throws Exception if validation fails or writing fails.
     */
    public void writeAmuaDT(File outputFile) throws Exception {
        if (!hasBeenValidatedDT) {
            isValidDT = isValidDTForAmua();
        }

        if (!isValidDT) {
            throw new IllegalStateException("NOT VALID: \n" + validationErrorMessage);
        }

        AmuaDTWriter writer = new AmuaDTWriter(amuaTreeNode, amuaDimInfo, amuaModel, outputFile);
        writer.writeDT();
    }


    /**
     * Checks whether the current decision tree is valid for Amua export.
     * If is valid, initializes internal attributes for export.
     *
     * @return true if valid, false otherwise
     */
    public boolean isValidDTForAmua() {
        try { // if no exception is thrown is valid
            AmuaDTValidator validator = new AmuaDTValidator(criteria);
            amuaModel = validator.determineAmuaDTType(treeNode);

            AmuaDTConverter converter = new AmuaDTConverter(amuaModel);
            amuaTreeNode = converter.convertToAmuaTree(treeNode);

            AmuaDTAssignDimensions dimAssigner = new AmuaDTAssignDimensions();
            amuaDimInfo = dimAssigner.assignDimensions(criteria, amuaModel, amuaTreeNode);

            hasBeenValidatedDT = true;
            isValidDT = true;
            return true;
        } catch (IllegalStateException e) {
            hasBeenValidatedDT = true;
            isValidDT = false;
            validationErrorMessage = e.getMessage();
            return false;
        }
    }


    /**
     * This message indicates whether the decision tree is valid for Amua export.
     *
     * @return the validation error message
     */
    public String getValidationErrorMessage() {
        return validationErrorMessage;
    }

    public boolean getIsValidDT() {return isValidDT;}

    public boolean getHasBeenValidatedDT() {return hasBeenValidatedDT;}
}