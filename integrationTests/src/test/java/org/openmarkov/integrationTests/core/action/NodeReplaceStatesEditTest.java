package org.openmarkov.integrationTests.core.action;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.openmarkov.core.action.core.NodeReplaceStatesEdit;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.exception.ProbNetParserException;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.integrationTests.core.model.network.PotentialsCanBeClonedTest;
import org.openmarkov.io.probmodel.reader.PGMXReader;
import org.openmarkov.java.cloneUtils.CloneUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class NodeReplaceStatesEditTest {
    
    public record DataThatShouldBeEquals(Potential potential) {
    
    }
    
    
    @Test
    public void changingStateNumberAndUndoingRestoresState() throws ProbNetParserException, IOException, DoEditException {
        var networkUrl = NodeReplaceStatesEditTest.class.getResource("/NodeStatesReplaceTest.pgmx");
        var network = new PGMXReader().read(networkUrl).probNet();
        network.getPNESupport().setWithUndo(true);
        
        var nodes = network.getNodes();
        var childNodes = new ArrayList<>(nodes);
        var commonParent = network.getNode("CommonParent");
        childNodes.remove(commonParent);
        
        var originalLinks = new ArrayList<>(network.getLinks().stream().map(CloneUtils::safeClone).toList());
        
        var nodesToDataToRemain = nodes.stream()
                                       .collect(Collectors.toMap(n -> n, node ->
                                               new DataThatShouldBeEquals(node.getPotential().deepCopy(network))
                                       ));
        
        
        var newNodeStatesNum = commonParent.getVariable().getNumStates() + 1;
        var newStates = IntStream.range(0, newNodeStatesNum)
                                 .mapToObj(i -> new State("State " + (i + 1)))
                                 .toArray(State[]::new);
        
        new NodeReplaceStatesEdit(commonParent, newStates).executeEdit();
        
        network.getPNESupport().undo();
        
        for (var originalLink : originalLinks) {
            var currentLink = network.getLink(originalLink.getFrom(), originalLink.getTo(), originalLink.isDirected());
            assertEquals(originalLink, currentLink);
        }
        
        for (var nodeAndDataToRemain : nodesToDataToRemain.entrySet()) {
            var node = nodeAndDataToRemain.getKey();
            var dataToRemain = nodeAndDataToRemain.getValue();
            
            assertEquals(node.getPotential(), dataToRemain.potential);
        }
        
        System.out.println();
    }
    
}
