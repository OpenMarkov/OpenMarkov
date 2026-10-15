/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.dialog.treeadd;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.treeadd.TreeADDBranch;
import org.openmarkov.core.model.network.potential.treeadd.TreeADDPotential;
import org.openmarkov.core.model.network.type.DESNetworkType;
import org.openmarkov.core.testTags.TestSpeed;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * In a network of events, «Health» depends on «Stage» and «Sex» and, when it is a parent of itself, on its own value;
 * its tree asks first for «Stage». The menu of the tree («Change root variable»), the menu of a branch («Add
 * subtree») and the leaf («Add variables to potential») offer the same variables: «Sex» and, only when «Health» is a
 * parent of itself, «Health». «Change root variable» never offered «Health» (error 245).
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class TheVariableOfANodeThatIsItsOwnParentIsOfferedByTheThreeMenusTest {

    private TreeADDEditorPanel editor;
    private TreeADDPotential tree;

    private void editorOfHealth(boolean parentOfItself) {
        Variable health = new Variable("Health", "good", "bad");
        Variable stage = new Variable("Stage", "early", "late");
        Variable sex = new Variable("Sex", "male", "female");
        ProbNet net = new ProbNet(DESNetworkType.getUniqueInstance());
        for (Variable variable : List.of(health, stage, sex)) {
            net.addNode(variable, NodeType.CHANCE);
        }
        net.addLink(stage, health, true);
        net.addLink(sex, health, true);
        List<Variable> variables = new ArrayList<>(List.of(health, stage, sex));
        if (parentOfItself) {
            net.addLink(health, health, true);
            variables.add(health);
        }
        List<TreeADDBranch> branches = new ArrayList<>();
        for (State state : stage.getStates()) {
            TablePotential leaf = new TablePotential(List.of(health), PotentialRole.CONDITIONAL_PROBABILITY);
            branches.add(new TreeADDBranch(List.of(state), stage, leaf, variables));
        }
        tree = new TreeADDPotential(variables, stage, PotentialRole.CONDITIONAL_PROBABILITY, branches);
        Node node = net.getNode(health);
        node.setPotential(tree);
        editor = new TreeADDEditorPanel(new TreeADDCellRenderer(net), node, tree, false);
    }

    private static List<String> namesIn(JMenu menu) {
        return Arrays.stream(menu.getMenuComponents()).map(item -> ((JMenuItem) item).getText()).toList();
    }

    private List<String> offeredByTheTree() {
        editor.setContextualMenuTreeADD(null, tree);
        return namesIn(editor.changeRootVariable);
    }

    private List<String> offeredByTheBranch() {
        TreeADDBranch early = tree.getBranches().getFirst();
        editor.setContextualMenuBranch(null, early, new TreePath(new Object[] { tree, early }));
        return namesIn(editor.addSubtree);
    }

    private List<String> offeredByTheLeaf() {
        return tree.getBranches().getFirst().getAddableVariables().stream().map(Variable::getName).toList();
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void theThreeMenusOfferTheSameVariables(boolean parentOfItself) {
        editorOfHealth(parentOfItself);
        List<String> expected = parentOfItself ? List.of("Sex", "Health") : List.of("Sex");

        assertEquals(expected, offeredByTheTree());
        assertEquals(expected, offeredByTheBranch());
        assertEquals(expected, offeredByTheLeaf());
    }
}
