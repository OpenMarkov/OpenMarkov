package org.openmarkov.java.swing;

import io.github.jorgericovivas.rust_essentials.tuples.Tuple2Record;
import io.github.jorgericovivas.rust_essentials.tuples.Tuples;
import org.jetbrains.annotations.Nullable;

import javax.swing.Box;
import javax.swing.JPanel;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;

public class SimplifiedGridBagConstraint {
    
    private final GridBagConstraints constraints;
    private final JPanel panel;
    
    private final int columns;
    private final HashSet<Tuple2Record<Integer, Integer>> takenCells;
    
    private Insets globalInsets;
    private Fill globalFill;
    private Anchor globalAnchor;
    
    private @Nullable Integer gridwidth;
    private @Nullable Integer gridheight;
    private @Nullable Insets insets;
    private @Nullable Fill fill;
    private @Nullable Anchor anchor;
    
    
    public SimplifiedGridBagConstraint(JPanel panel, GridBagConstraints constraints, int columns) {
        this.panel = panel;
        this.constraints = constraints;
        this.columns = columns;
        this.globalInsets = constraints.insets;
        this.takenCells = new HashSet<>();
        this.globalFill = Arrays.stream(Fill.values())
                                .filter(fill -> fill.toGridBagConstant() == constraints.fill)
                                .findFirst()
                                .orElse(Fill.values()[0]);
        this.globalAnchor = Arrays.stream(Anchor.values())
                                  .filter(anchor -> anchor.toGridBagConstant() == constraints.fill)
                                  .findFirst()
                                  .orElse(Anchor.values()[0]);
        this.constraints.gridx = Math.max(this.constraints.gridx, 0);
        this.constraints.gridy = Math.max(this.constraints.gridy, 0);
    }
    
    public SimplifiedGridBagConstraint gridwidth(int width) {
        this.gridwidth = width;
        return this;
    }
    
    public SimplifiedGridBagConstraint gridheight(int height) {
        this.gridheight = height;
        return this;
    }
    
    public SimplifiedGridBagConstraint weightx(double weightx) {
        this.constraints.weightx = weightx;
        return this;
    }
    
    public SimplifiedGridBagConstraint weighty(double weighty) {
        this.constraints.weighty = weighty;
        return this;
    }
    
    public SimplifiedGridBagConstraint gridx(int column) {
        this.constraints.gridx = column;
        return this;
    }
    
    public SimplifiedGridBagConstraint gridy(int row) {
        this.constraints.gridy = row;
        return this;
    }
    
    public SimplifiedGridBagConstraint anchor(Anchor anchor) {
        this.anchor = anchor;
        return this;
    }
    
    public SimplifiedGridBagConstraint globalAnchor(Anchor anchor) {
        this.globalAnchor = anchor;
        return this;
    }
    
    public SimplifiedGridBagConstraint insets(Insets insets) {
        this.insets = insets;
        return this;
    }
    
    public SimplifiedGridBagConstraint globalInsets(Insets insets) {
        this.globalInsets = insets;
        return this;
    }
    
    public SimplifiedGridBagConstraint globalFill(Fill fill) {
        this.globalFill = fill;
        return this;
    }
    
    public SimplifiedGridBagConstraint addCorrectionGlue() {
        this.weighty(1).weightx(1).add(Box.createGlue());
        return this;
    }
    
    public SimplifiedGridBagConstraint add(Component component) {
        this.constraints.insets = Optional.ofNullable(this.insets).orElse(this.globalInsets);
        this.constraints.gridwidth = Optional.ofNullable(this.gridwidth).orElse(1);
        this.constraints.gridheight = Optional.ofNullable(this.gridheight).orElse(1);
        this.constraints.fill = Optional.ofNullable(this.fill).orElse(this.globalFill).toGridBagConstant();
        this.constraints.anchor = Optional.ofNullable(this.anchor).orElse(this.globalAnchor).toGridBagConstant();
        
        this.insets = null;
        this.gridwidth = null;
        this.gridheight = null;
        this.fill = null;
        this.anchor = null;
        
        while (!this.canWriteToCell()) {
            this.advanceCell();
        }
        for (int x = 0; x < this.constraints.gridwidth; x++) {
            for (int y = 0; y < this.constraints.gridheight; y++) {
                this.takenCells.add(Tuples.record(this.constraints.gridx + x, this.constraints.gridy + y));
            }
        }
        this.panel.add(component, this.constraints);
        return this;
    }
    
    private boolean canWriteToCell() {
        if (this.constraints.gridx < 0 || this.constraints.gridy < 0 || this.constraints.gridx + this.constraints.gridwidth > this.columns) {
            return false;
        }
        
        for (int x = 0; x < this.constraints.gridwidth; x++) {
            for (int y = 0; y < this.constraints.gridheight; y++) {
                Tuple2Record<Integer, Integer> cell = Tuples.record(this.constraints.gridx + x, this.constraints.gridy + y);
                if (this.takenCells.contains(cell)) {
                    return false;
                }
            }
        }
        return true;
    }
    
    private void advanceCell() {
        if (this.constraints.gridx + 1 < this.columns) {
            this.constraints.gridx += 1;
        } else {
            this.constraints.gridx = 0;
            this.constraints.gridy += 1;
        }
    }
    
    public enum Fill {
        
        NONE, BOTH, HORIZONTAL, VERTICAL;
        
        private int toGridBagConstant() {
            return switch (this) {
                case NONE -> GridBagConstraints.NONE;
                case BOTH -> GridBagConstraints.BOTH;
                case HORIZONTAL -> GridBagConstraints.HORIZONTAL;
                case VERTICAL -> GridBagConstraints.VERTICAL;
            };
        }
    }
    
    public enum Anchor {
        CENTER, NORTH, NORTHEAST, EAST, SOUTHEAST, SOUTH, SOUTHWEST, WEST, NORTHWEST;
        
        private int toGridBagConstant() {
            return switch (this) {
                case CENTER -> GridBagConstraints.CENTER;
                case NORTH -> GridBagConstraints.NORTH;
                case NORTHEAST -> GridBagConstraints.NORTHEAST;
                case EAST -> GridBagConstraints.EAST;
                case SOUTHEAST -> GridBagConstraints.SOUTHEAST;
                case SOUTH -> GridBagConstraints.SOUTH;
                case SOUTHWEST -> GridBagConstraints.SOUTHWEST;
                case WEST -> GridBagConstraints.WEST;
                case NORTHWEST -> GridBagConstraints.NORTHWEST;
            };
        }
    }
    
}
