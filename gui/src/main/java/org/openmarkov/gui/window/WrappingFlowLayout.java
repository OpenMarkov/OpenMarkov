/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.window;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;

/**
 * A {@link FlowLayout} that asks for the height of all the rows it needs at the current width of its
 * container, so that the components that do not fit in the first row are shown in the next ones.
 *
 * @author Manuel Arias
 */
final class WrappingFlowLayout extends FlowLayout {

    WrappingFlowLayout(int align) {
        super(align);
    }

    @Override public Dimension preferredLayoutSize(Container target) {
        synchronized (target.getTreeLock()) {
            Insets insets = target.getInsets();
            int maxWidth = target.getWidth() == 0 ? Integer.MAX_VALUE
                    : target.getWidth() - (insets.left + insets.right + getHgap() * 2);
            int rowWidth = 0;
            int rowHeight = 0;
            int width = 0;
            int height = 0;
            for (Component component : target.getComponents()) {
                if (component.isVisible()) {
                    Dimension size = component.getPreferredSize();
                    if (rowWidth > 0 && rowWidth + size.width > maxWidth) {
                        width = Math.max(width, rowWidth);
                        height += rowHeight + getVgap();
                        rowWidth = 0;
                        rowHeight = 0;
                    }
                    rowWidth += (rowWidth > 0 ? getHgap() : 0) + size.width;
                    rowHeight = Math.max(rowHeight, size.height);
                }
            }
            width = Math.max(width, rowWidth);
            height += rowHeight;
            return new Dimension(width + insets.left + insets.right + getHgap() * 2,
                                 height + insets.top + insets.bottom + getVgap() * 2);
        }
    }
}
