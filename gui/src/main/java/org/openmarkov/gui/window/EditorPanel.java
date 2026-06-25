/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.window;

import org.openmarkov.gui.configuration.GUIColors;
import org.openmarkov.gui.graphics.BackgroundedElement;
import org.openmarkov.gui.graphics.BoxedElement;
import org.openmarkov.gui.graphics.Paintable;
import org.openmarkov.gui.graphics.TextBox;
import org.openmarkov.gui.window.edition.networkEditorPanel.NetworkEditorPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;

/**
 * This class represents the content panel of a tab.
 *
 * @author jmendoza
 * @version 1.2 jrico - Added a prototype for toasts.
 */
public abstract class EditorPanel extends JPanel {
    /**
     * Static field for serializable class.
     */
    private static final long serialVersionUID = 6808692603537287168L;
    
    protected JScrollPane scrollPanel;
    
    private static final double ZOOM_SPEED_ON_WHEEL = 0.2;
    
    public EditorPanel() {
        this.scrollPanel = new JScrollPane(this);
        this.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                var selectedToast = EditorPanel.this.toasts
                        .stream()
                        .filter(toast -> toast.rect.getBounds().contains(e.getPoint()))
                        .findFirst();
                if (selectedToast.isPresent()) {
                    EditorPanel.this.toasts.remove(selectedToast.get());
                    EditorPanel.this.repaint();
                    e.consume();
                    return;
                }
            }
        });
        this.addMouseWheelListener(e -> {
            if (e.isControlDown()) {
                this.setZoom(this.getZoom() + EditorPanel.ZOOM_SPEED_ON_WHEEL * (-e.getWheelRotation()));
            } else {
                this.getParent().dispatchEvent(e);
            }
        });
        this.scrollPanel.getVerticalScrollBar().addAdjustmentListener(e -> this.repaint());
        this.scrollPanel.getHorizontalScrollBar().addAdjustmentListener(e -> this.repaint());
    }
    
    public JScrollPane getScrollPanel() {
        return this.scrollPanel;
    }
    
    /**
     * Prepares the frame for closing
     */
    public boolean close() {
        MainGUI.INSTANCE.mainPanel.getNetworksTabPanel().remove(this);
        if (MainGUI.INSTANCE.mainPanel.getNetworksTabPanel().getTabCount() == 0) {
            MainGUI.INSTANCE.mainPanel.setToolBarPanel(NetworkEditorPanel.WorkingMode.EDITION);
            MainGUI.INSTANCE.mainPanel.getMainPanelMenuAssistant().updateOptionsAllNetworkClosed();
        }
        return true;
    }
    
    public abstract double getZoom();
    
    public abstract void setZoom(double zoom);
    
    private boolean avoidPaintRecursion = false;
    
    @Override public final void paint(Graphics g) {
        if (this.avoidPaintRecursion) {
            return;
        }
        this.avoidPaintRecursion = true;
        Graphics2D graphics2D = (Graphics2D) g.create();
        graphics2D = (Graphics2D) graphics2D.create();
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        super.paint(graphics2D.create());
        this.doPaint((Graphics2D) graphics2D.create());
        this.internalPaint((Graphics2D) graphics2D.create());
        this.avoidPaintRecursion = false;
    }
    
    static class Toast {
        String text;
        Rectangle rect;
        
        public Toast(String text) {
            this.text = text;
        }
    }
    
    private final java.util.List<Toast> toasts = new ArrayList<>();
    
    public void addToast(String content) {
        this.toasts.add(new Toast(content));
    }
    
    private void internalPaint(Graphics2D graphics2D) {
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        var scroll = this.scrollPanel;
        var viewPosition = scroll.getViewport().getViewPosition();
        var y = scroll.getHeight() - 20 + viewPosition.y;
        for (Toast toast : this.toasts) {
            Paintable textBox = new BackgroundedElement<>(BoxedElement.of(new TextBox(toast.text)), GUIColors.Graphics.DEFAULT_BACKGROUND_COLOR.getColor());
            toast.rect = textBox.paint(graphics2D, scroll.getWidth() + viewPosition.x - textBox.dimensions(graphics2D).width - 20, y - textBox.dimensions(graphics2D).height);
            y -= (textBox.dimensions(graphics2D).height + 30);
        }
    }
    
    
    protected abstract void doPaint(Graphics2D graphics2D);
    
}
