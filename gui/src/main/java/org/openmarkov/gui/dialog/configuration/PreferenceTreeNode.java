/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
/**
 *
 */

package org.openmarkov.gui.dialog.configuration;

import org.openmarkov.core.exception.UnrecoverableException;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import java.util.ArrayList;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * PreferenceTreeNode is defining the node behaviours in a Preferences Tree
 *
 * @author jlgozalo
 * @version 1.0 28 Aug 2009
 */
@SuppressWarnings("serial") public class PreferenceTreeNode extends DefaultMutableTreeNode {
    final Preferences pref;
    String nodeName;
    String[] childrenNames;
    
    public PreferenceTreeNode(Preferences pref) throws BackingStoreException {
        this.pref = pref;
        childrenNames = pref.childrenNames();
    }
    
    public Preferences getPrefObject() {
        return pref;
    }
    
    @Override public boolean isLeaf() {
        return ((childrenNames == null) || (childrenNames.length == 0));
    }
    
    @Override public int getChildCount() {
        return childrenNames.length;
    }
    
    /**
     * Removes child at the given index, used to hide a child in the displayed tree.
     *
     * @param childIndex the zero-based index of the child to remove
     */
    public void removeChildAt(int childIndex) {
        if (childIndex < childrenNames.length) {
            ArrayList<String> newChildrenNames = new ArrayList<String>();
            for (int i = 0; i < childrenNames.length; i++) {
                if (i != childIndex) {
                    newChildrenNames.add(childrenNames[i]);
                }
            }
            String[] newChildrenNames2 = new String[childrenNames.length - 1];
            for (int i = 0; i < newChildrenNames.size(); i++) {
                newChildrenNames2[i] = newChildrenNames.get(i);
            }
            this.childrenNames = newChildrenNames2;
        }
    }
    
    @Override public TreeNode getChildAt(int childIndex) {
        try {
            return new PreferenceTreeNode(pref.node(childrenNames[childIndex]));
        } catch (BackingStoreException e) {
            throw new UnrecoverableException(e);
        }
    }
    
    public String toString() {
        String name = pref.name();
        if ((name == null) || (name.isEmpty())) { // if root node
            name = "System Preferences";
            if (pref.isUserNode())
                name = "User Preferences";
        }
        return name;
    }
}
