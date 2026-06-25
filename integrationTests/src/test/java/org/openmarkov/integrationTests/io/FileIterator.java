package org.openmarkov.integrationTests.io;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileIterator implements PGMXIterator {
    
    // Attributes
    private List<File[]> subNodes;
    private List<Integer> subTreesIndexes;
    private boolean hasNext;
    private File nextFile;
    private PGMXCompound nextCompound;
    
    private List<PGMXFilter> filters;
    
    // Constructor
    
    /**
     * Constructor
     * @param rootFolder
     */
    public FileIterator(File rootFolder, List<PGMXFilter>... filters) {
        this.filters = filters != null && filters.length == 1 ? filters[0] : null;
        hasNext = false;
        nextFile = null;
        
        if (rootFolder.isDirectory()) {
            subNodes = new ArrayList<>();
            File[] rootChildren = rootFolder.listFiles();
            subNodes.add(rootChildren);
            
            subTreesIndexes = new ArrayList<>();
            subTreesIndexes.add(-1);
            
            nextFile = lookForNext();
        } else {
            PGMXCompound compound = new PGMXCompound(rootFolder);
            hasNext = matches(compound);
            if (hasNext) {
                nextFile = rootFolder;
                nextCompound = compound;
            } else {
                nextFile = null;
                nextCompound = null;
            }
        }
    }
    
    @Override public boolean hasNext() {
        return hasNext;
    }
    
    @Override public PGMXCompound next() {
        PGMXCompound aux = nextCompound;
        nextFile = nextFile == null ? nextFile : lookForNext();
        return aux;
    }
    
    /**
     * Looks for next file in the tree and updates <code>hasNext</code>
     * @return Next File element
     */
    private File lookForNext() {
        int treeDepth = subNodes == null ? 0 : subNodes.size();
        if (treeDepth == 0) {
            hasNext = false;
            nextFile = null;
        } else {
            int deepestDirectory = treeDepth - 1;
            File[] deepestNodes = subNodes.get(deepestDirectory);
            int deepestIndexesNode = subTreesIndexes.get(deepestDirectory);
            subTreesIndexes.set(deepestDirectory, ++deepestIndexesNode);
            if (deepestNodes == null || deepestNodes.length == 0 || (deepestIndexesNode + 1) > deepestNodes.length) {// Empty sub folder or finished folder
                subNodes.remove(deepestDirectory);
                subTreesIndexes.remove(deepestDirectory);
                nextFile = lookForNext();
            } else {
                File lastFile = deepestNodes[deepestIndexesNode];
                if (lastFile.isDirectory()) {
                    subNodes.add(lastFile.listFiles());
                    subTreesIndexes.add(-1);
                    nextFile = lookForNext();
                } else {
                    hasNext = true;
                    nextFile = lastFile;
                    nextCompound = new PGMXCompound(lastFile);
                }
            }
        }
        nextFile = nextFile == null ? null : matches(nextCompound) ? nextFile : lookForNext();
        return nextFile;
    }
    
    private boolean matches(PGMXCompound compound) {
        boolean match = true;
        if (filters != null) {
            int i;
            int size = filters.size();
            for (i = 0; i < size && filters.get(i).meetsCondition(compound); i++) ;
            match = i == size;
        }
        return match;
    }
}
