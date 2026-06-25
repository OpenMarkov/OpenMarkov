/* Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.action;

import org.jetbrains.annotations.Nullable;
import org.openmarkov.core.action.base.PNEdit;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.gui.exception.OtherPropertyAlreadyExistsException;
import org.openmarkov.java.collectionsUtils.arrayUtils.ArrayUtils;
import org.openmarkov.java.collectionsUtils.arrayUtils.MapUtils;

import java.util.AbstractMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;

/**
 * {@code OtherPropertyEdit} is a simple edit that allow modify the additional properties of one node.
 *
 * @author agoni
 * @version 1.1 jrico. Simplified edit and corrected a bug where the "Rename", "Up" and "Down" operations would throw
 * exceptions.
 */

public class OtherPropertyEdit extends PNEdit {
    
    /** The last properties before the edition */
    private final Map<String, String> oldProperties;
    /** The properties after the edition */
    private Map<String, String> newProperties;
    /** The new property */
    private final String[] newProperty;
    /** The action to carry out */
    private final String otherPropertyAction;
    /** the index (in the table) associated to the property to edit */
    private final int propertyIndex;
    /** The node that the property belongs to */
    private final @Nullable Node node;
    /** The network that the property belongs to */
    
    /**
     * Creates a new {@code OtherPropertyEdit} to carry out the specified
     * action on the specified property.
     *
     * @param node                the node that will be edited.
     * @param otherPropertyAction the action to carry out
     * @param propertyIndex       the index (in the table) associated to the property to edit
     * @param newData             a new string for the property edited if the action is ADD or RENAME.
     */
    public OtherPropertyEdit(Node node, String otherPropertyAction, int propertyIndex, String[] newData) {
        super(node.getProbNet());
        this.node = node;
        this.oldProperties = new LinkedHashMap<>(node.getAdditionalProperties());
        this.propertyIndex = propertyIndex;
        this.newProperty = newData;
        this.otherPropertyAction = otherPropertyAction;
    }
    
    /**
     * Creates a new {@code OtherPropertyEdit} to carry out the specified
     * action on the specified property.
     *
     * @param probNet             network.
     * @param otherPropertyAction the action to carry out
     * @param propertyIndex       the index (in the table) associated to the property to edit
     * @param newData             a new string for the property edited if the action is ADD or RENAME.
     */
    public OtherPropertyEdit(ProbNet probNet, String otherPropertyAction, int propertyIndex, String[] newData) {
        super(probNet);
        this.node = null;
        this.oldProperties = new LinkedHashMap<>(probNet.getAdditionalProperties());
        this.propertyIndex = propertyIndex;
        this.newProperty = newData;
        this.otherPropertyAction = otherPropertyAction;
    }
    
    @Override protected void doEdit() throws OtherPropertyAlreadyExistsException {
        switch (this.otherPropertyAction) {
            case "ADD" -> {
                this.newProperties = new LinkedHashMap<>(this.oldProperties);
                this.newProperties.put(this.newProperty[0], this.newProperty[1]);
            }
            case "REMOVE" -> {
                this.newProperties = new LinkedHashMap<>(this.oldProperties);
                String key = this.oldProperties.keySet().toArray()[this.propertyIndex].toString();
                this.newProperties.remove(key);
            }
            case "RENAME" -> {
                var newPropertiesArray = MapUtils.mapToArray(this.oldProperties);
                String key = this.newProperty[0];
                var indexOfRepeatedKey = IntStream.range(0, newPropertiesArray.length)
                                                  .filter(i -> Objects.equals(newPropertiesArray[i].getKey(), key))
                                                  .findFirst();
                if (indexOfRepeatedKey.isPresent() && indexOfRepeatedKey.getAsInt() != this.propertyIndex) {
                    throw new OtherPropertyAlreadyExistsException(key, this);
                }
                newPropertiesArray[this.propertyIndex] = new AbstractMap.SimpleEntry<>(key, this.newProperty[1]);
                this.newProperties = ArrayUtils.arrayToLinkedMap(newPropertiesArray);
            }
            case "DOWN" -> {
                var newPropertiesArray = MapUtils.mapToArray(this.oldProperties);
                ArrayUtils.swapElements(newPropertiesArray, this.propertyIndex, this.propertyIndex + 1);
                this.newProperties = ArrayUtils.arrayToLinkedMap(newPropertiesArray);
            }
            case "UP" -> {
                var newPropertiesArray = MapUtils.mapToArray(this.oldProperties);
                ArrayUtils.swapElements(newPropertiesArray, this.propertyIndex, this.propertyIndex - 1);
                this.newProperties = ArrayUtils.arrayToLinkedMap(newPropertiesArray);
            }
        }
        this.setProperties(this.newProperties);
    }
    
    @Override public void redo() {
        this.setProperties(this.newProperties);
    }
    
    @Override public void undo() {
        this.setProperties(this.oldProperties);
    }
    
    private void setProperties(Map<String, String> newProperties) {
        if (this.node != null) {
            this.node.setAdditionalProperties(newProperties);
        } else if (this.probNet != null) {
            this.probNet.setAdditionalProperties(newProperties);
        }
    }
    
}
