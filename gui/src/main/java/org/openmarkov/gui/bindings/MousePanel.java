package org.openmarkov.gui.bindings;

import org.jetbrains.annotations.Nullable;
import org.openmarkov.gui.util.GUIUtils;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * A visual representation of the user's mouse: left/right/middle buttons,
 * scroll wheel (with last-scroll direction), and side (thumb) buttons.
 * <p>
 * The panel can be driven two ways, and both keep the drawing in sync:
 * <ul>
 *   <li>Externally, by calling {@link #markMouseButtonsAsPressed(int, boolean)}
 *       and {@link #markWheel(boolean, boolean)} - e.g. from an app-wide
 *       AWTEventListener that reports real mouse activity anywhere in the app.</li>
 *   <li>Directly, by clicking on the drawing itself: the regions for each
 *       button/wheel direction are hit-tested against real mouse clicks on
 *       this panel, and registered {@link RegionClickListener}s are notified.</li>
 * </ul>
 * Note: this only sees events inside this Java application - not system-wide
 * clicks made in other programs. That needs a native hook (e.g. JNativeHook),
 * which is out of scope here.
 */
public class MousePanel extends JPanel {
    
    /** Notified when the user clicks directly on one of the drawn regions. */
    public interface RegionClickListener {
        /** @param mouseButton one of MouseEvent.BUTTON1/2/3, or 4 (back) / 5 (forward) */
        default void onButtonRegionClicked(int mouseButton) {
        }
        
        /** @param up true if the "scroll up" icon was clicked, false for "scroll down" */
        default void onWheelRegionClicked(boolean up) {
        }
    }
    
    private boolean leftPressed, rightPressed, middlePressed, backPressed, forwardPressed;
    private int scrollFlash = 0; // -1 = scrolled up flash, +1 = scrolled down flash, 0 = none
    private final Timer scrollFlashTimer;
    
    private static final Color BODY_COLOR = new Color(230, 230, 233);
    private static final Color OUTLINE_COLOR = new Color(90, 90, 95);
    private static final Color PRESSED_COLOR = new Color(120, 170, 240);
    private static final Color WHEEL_COLOR = new Color(60, 60, 65);
    private static final Color WHEEL_FLASH_COLOR = new Color(120, 170, 240);
    
    // ---- Shared geometry -------------------------------------------------
    // Computed once and reused by both paintComponent() and hit-testing, so
    // the clickable areas can never drift out of sync with what's drawn.
    private static final double BODY_X = 40, BODY_Y = 20, BODY_W = 140, BODY_H = 260;
    private static final double BUTTON_H = BODY_H * 0.38;
    
    private Area leftButtonArea;
    private Area rightButtonArea;
    private RoundRectangle2D.Double wheelArea;
    private Ellipse2D.Double scrollUpHitArea;
    private Ellipse2D.Double scrollDownHitArea;
    private RoundRectangle2D.Double backArea;
    private RoundRectangle2D.Double forwardArea;
    
    private final List<RegionClickListener> regionClickListeners = new ArrayList<>();
    
    // Tracks what's currently held down via a direct click on the panel, so
    // mouseReleased can find the same target and decide whether to fire a click.
    private @Nullable Integer pressedButtonCode = null;
    private @Nullable Boolean pressedWheelUp = null;
    
    public MousePanel() {
        this.setPreferredSize(new Dimension(220, 320));
        this.setOpaque(false);
        
        this.scrollFlashTimer = new Timer(450, e -> {
            this.scrollFlash = 0;
            this.repaint();
        });
        this.scrollFlashTimer.setRepeats(false);
        
        this.computeRegions();
        this.installClickHandling();
    }
    
    public void addRegionClickListener(RegionClickListener listener) {
        this.regionClickListeners.add(listener);
    }
    
    public void removeRegionClickListener(RegionClickListener listener) {
        this.regionClickListeners.remove(listener);
    }
    
    public void markMouseButtonsAsPressed(int button, boolean pressed) {
        switch (button) {
            case MouseEvent.BUTTON1 -> this.leftPressed = pressed;
            case MouseEvent.BUTTON2 -> this.middlePressed = pressed;
            case MouseEvent.BUTTON3 -> this.rightPressed = pressed;
            case 4 -> this.backPressed = pressed;
            case 5 -> this.forwardPressed = pressed;
            default -> {
                return;
            }
        }
        this.repaint();
    }
    
    public void markWheel(boolean up, boolean pressed) {
        this.scrollFlash = pressed ? (up ? -1 : 1) : 0;
        this.repaint();
    }
    
    // ---- Region geometry --------------------------------------------------
    
    private void computeRegions() {
        Area bodyArea = new Area(
                new RoundRectangle2D.Double(BODY_X, BODY_Y, BODY_W, BODY_H, 110, 150));
        
        this.leftButtonArea = new Area(new Rectangle2D.Double(BODY_X, BODY_Y, BODY_W / 2, BUTTON_H));
        this.leftButtonArea.intersect(bodyArea);
        this.rightButtonArea = new Area(new Rectangle2D.Double(BODY_X + BODY_W / 2, BODY_Y, BODY_W / 2, BUTTON_H));
        this.rightButtonArea.intersect(bodyArea);
        
        double wheelW = 16, wheelH = 26;
        double wheelX = BODY_X + BODY_W / 2 - wheelW / 2;
        double wheelY = BODY_Y + 22;
        this.wheelArea = new RoundRectangle2D.Double(wheelX, wheelY, wheelW, wheelH, 8, 8);
        
        double chevronCx = BODY_X + BODY_W / 2;
        double hitR = 11; // a bit larger than the drawn badge (r=9), for easier clicking
        this.scrollUpHitArea = new Ellipse2D.Double(chevronCx - hitR, wheelY - 8 - hitR, hitR * 2, hitR * 2);
        this.scrollDownHitArea = new Ellipse2D.Double(chevronCx - hitR, wheelY + wheelH + 8 - hitR, hitR * 2, hitR * 2);
        
        double sideW = 10, sideH = 30;
        double sideX = BODY_X - sideW + 3;
        this.backArea = new RoundRectangle2D.Double(sideX, BODY_Y + 100, sideW, sideH, 6, 6);
        this.forwardArea = new RoundRectangle2D.Double(sideX, BODY_Y + 140, sideW, sideH, 6, 6);
    }
    
    /** @return MouseEvent.BUTTON1/2/3, 4 (back), 5 (forward), or null if the point hits nothing. */
    private Integer hitTestButton(Point p) {
        if (this.leftButtonArea.contains(p)) return MouseEvent.BUTTON1;
        if (this.rightButtonArea.contains(p)) return MouseEvent.BUTTON3;
        if (this.wheelArea.contains(p)) return MouseEvent.BUTTON2;
        if (this.backArea.contains(p)) return 4;
        if (this.forwardArea.contains(p)) return 5;
        return null;
    }
    
    /** @return true if "scroll up" was hit, false for "scroll down", null for neither. */
    private Boolean hitTestWheelDirection(Point p) {
        if (this.scrollUpHitArea.contains(p)) return Boolean.TRUE;
        if (this.scrollDownHitArea.contains(p)) return Boolean.FALSE;
        return null;
    }
    
    // ---- Click handling -----------------------------------------------
    
    private void installClickHandling() {
        MouseAdapter adapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                MousePanel.this.pressedButtonCode = null;
                MousePanel.this.pressedWheelUp = null;
                if (MousePanel.this.hitTestWheelDirection(e.getPoint()) instanceof Boolean pressedWheel) {
                    MousePanel.this.pressedWheelUp = pressedWheel;
                    return;
                }
                if (MousePanel.this.hitTestButton(e.getPoint()) instanceof Integer pressedButton) {
                    MousePanel.this.pressedButtonCode = pressedButton;
                    return;
                }
                GUIUtils.redispatchMouseEventToParent(e, MousePanel.this);
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                if (MousePanel.this.hitTestWheelDirection(e.getPoint()) instanceof Boolean pressedWheel && MousePanel.this.pressedWheelUp == pressedWheel) {
                    MousePanel.this.fireWheelRegionClicked(MousePanel.this.pressedWheelUp);
                    MousePanel.this.pressedWheelUp = null;
                    return;
                }
                if (MousePanel.this.hitTestButton(e.getPoint()) instanceof Integer pressedButton && pressedButton.equals(MousePanel.this.pressedButtonCode)) {
                    MousePanel.this.fireButtonRegionClicked(pressedButton);
                    MousePanel.this.pressedButtonCode = null;
                    return;
                }
                if (getParent() instanceof Container parent) {
                    parent.dispatchEvent(SwingUtilities.convertMouseEvent(MousePanel.this, e, parent));
                }
            }
            
            @Override public void mouseMoved(MouseEvent e) {
                boolean overRegion = MousePanel.this.hitTestButton(e.getPoint()) != null
                        || MousePanel.this.hitTestWheelDirection(e.getPoint()) != null;
                MousePanel.this.setCursor(overRegion ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
            }
            
            @Override public void mouseDragged(MouseEvent e) {
                if (MousePanel.this.hitTestWheelDirection(e.getPoint()) instanceof Boolean pressedWheel && MousePanel.this.pressedWheelUp != pressedWheel) {
                    MousePanel.this.setCursor(Cursor.getDefaultCursor());
                    MousePanel.this.pressedWheelUp = null;
                    MousePanel.this.pressedButtonCode = null;
                }
                if (MousePanel.this.hitTestButton(e.getPoint()) instanceof Integer pressedButton && !pressedButton.equals(MousePanel.this.pressedButtonCode)) {
                    MousePanel.this.setCursor(Cursor.getDefaultCursor());
                    MousePanel.this.pressedWheelUp = null;
                    MousePanel.this.pressedButtonCode = null;
                }
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                MousePanel.this.setCursor(Cursor.getDefaultCursor());
                MousePanel.this.pressedWheelUp = null;
                MousePanel.this.pressedButtonCode = null;
            }
        };
        this.addMouseListener(adapter);
        this.addMouseMotionListener(adapter);
    }
    
    private void fireButtonRegionClicked(int button) {
        for (RegionClickListener l : this.regionClickListeners) {
            l.onButtonRegionClicked(button);
        }
    }
    
    private void fireWheelRegionClicked(boolean up) {
        for (RegionClickListener l : this.regionClickListeners) {
            l.onWheelRegionClicked(up);
        }
    }
    
    // ---- Drawing --------------------------------------------------------
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        RoundRectangle2D.Double body =
                new RoundRectangle2D.Double(BODY_X, BODY_Y, BODY_W, BODY_H, 110, 150);
        
        // Body (fill first, buttons/wheel drawn on top so they look embedded)
        g2.setColor(BODY_COLOR);
        g2.fill(body);
        
        g2.setColor(this.leftPressed ? PRESSED_COLOR : BODY_COLOR);
        g2.fill(this.leftButtonArea);
        g2.setColor(this.rightPressed ? PRESSED_COLOR : BODY_COLOR);
        g2.fill(this.rightButtonArea);
        
        // Seam down the middle between left/right buttons
        g2.setColor(OUTLINE_COLOR);
        g2.setStroke(new BasicStroke(1.5f));
        g2.draw(new Line2D.Double(BODY_X + BODY_W / 2, BODY_Y + 6, BODY_X + BODY_W / 2, BODY_Y + BUTTON_H));
        
        // Outline
        g2.setStroke(new BasicStroke(2.5f));
        g2.draw(body);
        g2.draw(this.leftButtonArea);
        g2.draw(this.rightButtonArea);
        
        // Scroll wheel (button only - up/down scroll are drawn as separate
        // chevron icons above and below it, not inside the wheel itself)
        g2.setColor(this.middlePressed ? PRESSED_COLOR : WHEEL_COLOR);
        g2.fill(this.wheelArea);
        g2.setColor(this.middlePressed ? OUTLINE_COLOR.darker() : OUTLINE_COLOR);
        g2.setStroke(new BasicStroke(this.middlePressed ? 3f : 2f));
        g2.draw(this.wheelArea);
        
        double chevronCx = BODY_X + BODY_W / 2;
        this.drawChevron(g2, chevronCx, this.wheelArea.y - 8, -1, this.scrollFlash == -1);  // north of wheel: scroll up
        this.drawChevron(g2, chevronCx, this.wheelArea.y + this.wheelArea.height + 8, 1, this.scrollFlash == 1); // south: scroll down
        
        // Side (thumb) buttons
        g2.setColor(this.backPressed ? PRESSED_COLOR : BODY_COLOR);
        g2.fill(this.backArea);
        g2.setColor(this.forwardPressed ? PRESSED_COLOR : BODY_COLOR);
        g2.fill(this.forwardArea);
        g2.setColor(OUTLINE_COLOR);
        g2.setStroke(new BasicStroke(2f));
        g2.draw(this.backArea);
        g2.draw(this.forwardArea);
        
        // Labels
        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 11f));
        g2.setColor(new Color(70, 70, 75));
        this.drawCentered(g2, "L", BODY_X + BODY_W * 0.25, BODY_Y + BUTTON_H * 0.6);
        this.drawCentered(g2, "R", BODY_X + BODY_W * 0.75, BODY_Y + BUTTON_H * 0.6);
        
        g2.dispose();
    }
    
    /**
     * A small chevron icon, separate from the wheel graphic, sitting just
     * north (direction < 0, scroll up) or south (direction > 0, scroll down)
     * of it. Drawn faint at rest, and highlighted when that direction was
     * the most recent scroll event.
     */
    private void drawChevron(Graphics2D g2, double cx, double cy, int direction, boolean active) {
        if (active) {
            double r = 9;
            g2.setColor(WHEEL_FLASH_COLOR);
            g2.fill(new Ellipse2D.Double(cx - r, cy - r, r * 2, r * 2));
            g2.setColor(WHEEL_FLASH_COLOR.darker());
            g2.setStroke(new BasicStroke(1.5f));
            g2.draw(new Ellipse2D.Double(cx - r, cy - r, r * 2, r * 2));
        }
        
        Path2D.Double chevron = new Path2D.Double();
        double halfWidth = active ? 6 : 5, halfHeight = active ? 3.5 : 3;
        if (direction < 0) { // pointing up
            chevron.moveTo(cx - halfWidth, cy + halfHeight);
            chevron.lineTo(cx, cy - halfHeight);
            chevron.lineTo(cx + halfWidth, cy + halfHeight);
        } else { // pointing down
            chevron.moveTo(cx - halfWidth, cy - halfHeight);
            chevron.lineTo(cx, cy + halfHeight);
            chevron.lineTo(cx + halfWidth, cy - halfHeight);
        }
        g2.setColor(active ? Color.WHITE : new Color(170, 170, 175));
        g2.setStroke(new BasicStroke(active ? 3f : 2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.draw(chevron);
    }
    
    private void drawCentered(Graphics2D g2, String text, double cx, double cy) {
        FontMetrics fm = g2.getFontMetrics();
        int w = fm.stringWidth(text);
        g2.drawString(text, (float) (cx - w / 2.0), (float) (cy + fm.getAscent() / 2.0 - 2));
    }
    
}