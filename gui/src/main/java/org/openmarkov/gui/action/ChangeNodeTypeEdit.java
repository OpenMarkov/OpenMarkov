package org.openmarkov.gui.action;

import org.jetbrains.annotations.NotNull;
import org.openmarkov.core.action.base.MultiStepEdit;
import org.openmarkov.core.action.base.PNEdit;
import org.openmarkov.core.action.core.SetPotentialEdit;
import org.openmarkov.core.action.core.VariableTypeEdit;
import org.openmarkov.core.exception.ConstraintViolatedException;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.plugin.PotentialUtils;

import java.util.Arrays;
import java.util.List;

public class ChangeNodeTypeEdit extends MultiStepEdit {
    
    private final @NotNull Node node;
    private final @NotNull NodeType newNodeType;
    
    public ChangeNodeTypeEdit(@NotNull Node node, @NotNull NodeType newNodeType) {
        super(node.getProbNet());
        this.node = node;
        this.newNodeType = newNodeType;
    }
    
    
    @Override protected void doMultiStepEdit(StepExecuter stepExecuter) throws DoEditException {
        stepExecuter.execute(new SetNodeTypeEdit(this.node, this.newNodeType));
        VariableType[] availableVariableTypes = VariableType.of(this.newNodeType);
        VariableType oldVariableType = this.node.getVariable().getVariableType();
        boolean variableTypeIsValid = Arrays.stream(availableVariableTypes).toList().contains(oldVariableType);
        if (!variableTypeIsValid && availableVariableTypes.length>0){
            stepExecuter.execute(new VariableTypeEdit(this.node, availableVariableTypes[0], false));
        }
        Potential oldPotential = this.node.getPotential();
        List<Class<? extends Potential>> availablePotentials = PotentialUtils.getFilteredPotentialClasses(this.node);
        boolean potentialIsInvalid = oldPotential == null || !availablePotentials.contains(oldPotential.getClass());
        if(potentialIsInvalid) {
            Potential newPotential = PotentialUtils.generateDefaultPotential(this.probNet, this.node.getVariable(), this.newNodeType);
            stepExecuter.execute(new SetPotentialEdit(this.node, this.node.getPotential(), newPotential));
        }
    }
    
    private static final class SetNodeTypeEdit extends PNEdit {
        
        private final @NotNull Node node;
        private final @NotNull NodeType newNodeType;
        private final @NotNull NodeType oldNodeType;
        
        public SetNodeTypeEdit(Node node, NodeType newNodeType) {
            super(node.getProbNet());
            this.node = node;
            this.newNodeType = newNodeType;
            this.oldNodeType = node.getNodeType();
        }
        
        @Override protected void doEdit() throws DoEditException {
            this.node.setNodeType(this.newNodeType);
            try {
                this.probNet.checkConstraints();
            } catch (ConstraintViolatedException e) {
                throw new DoEditException.CannotDoEditException(e, this);
            }
        }
        
        @Override public void undo() {
            this.node.setNodeType(this.oldNodeType);
        }
        
        @Override public void redo() {
            this.node.setNodeType(this.newNodeType);
        }
        
        

    }
    
}
