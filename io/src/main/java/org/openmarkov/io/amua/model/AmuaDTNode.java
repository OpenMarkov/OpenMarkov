package org.openmarkov.io.amua.model;

import java.util.ArrayList;
import java.util.List;


/**
 * Base class for a node in an Amua decision tree.
 *
 * @param <T> Type of value stored in the node (Double for unicriteria, AmuaCEvalue for CE trees)
 * @author Hugo Manuel
 * @version 1.0
 */

public abstract class AmuaDTNode<T> {

    private int index;
    private int type;
    private int level;

    private double probability;

    private String name;

    private int xPos;
    private int yPos;
    private int parentX;
    private int parentY;

    private List<AmuaDTNode<?>> childNodes = new ArrayList<>();
    private AmuaDTNode<?> parentNode;

    private boolean collapsed = false; // default
    private boolean visible = true; // default
    private boolean hasCost;
    private boolean hasVarUpdates = false; // default

    private int width = 24; // default
    private int height = 24; // default

    protected T cost;
    protected T payoff;

    protected T partialUtility;

    public final int margin = 48;

    /** Default constructor. */
    public AmuaDTNode() {}

    // getters and setters...

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public double getProbability() {
        return probability;
    }

    public void setProb(double probability) {
        this.probability = probability;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getXPos() {
        return xPos;
    }

    public void setXPos(int xPos) {
        this.xPos = xPos;
    }

    public int getYPos() {
        return yPos;
    }

    public void setYPos(int yPos) {
        this.yPos = yPos;
    }

    public int getParentX() {
        return parentX;
    }

    public void setParentX(int parentX) {
        this.parentX = parentX;
    }

    public int getParentY() {
        return parentY;
    }

    public void setParentY(int parentY) {
        this.parentY = parentY;
    }

    public List<AmuaDTNode<?>> getChildNodes() {
        return childNodes;
    }

    public void setChildNodes(List<? extends AmuaDTNode<?>> childNodes) {
        this.childNodes = new ArrayList<>(childNodes);
    }

    public AmuaDTNode<?> getParentNode() {
        return parentNode;
    }

    public void setParentNode(AmuaDTNode<?> parentNode) {
        this.parentNode = parentNode;
    }

    public boolean isCollapsed() {
        return collapsed;
    }

    public void setCollapsed(boolean collapsed) {
        this.collapsed = collapsed;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isHasCost() {
        return hasCost;
    }

    public void setHasCost(boolean hasCost) {
        this.hasCost = hasCost;
    }

    public boolean isHasVarUpdates() {
        return hasVarUpdates;
    }

    public void setHasVarUpdates(boolean hasVarUpdates) {
        this.hasVarUpdates = hasVarUpdates;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public T getCost() { return cost; }
    public T getPayoff() { return payoff; }

    public void setCost(T cost) { this.cost = cost; }
    public void setPayoff(T payoff) { this.payoff = payoff; }

    public T getPartialUtility() { return partialUtility; }
    public void setPartialUtility(T partialUtility) { this.partialUtility = partialUtility; }

    public int getMargin() { return margin; }
}