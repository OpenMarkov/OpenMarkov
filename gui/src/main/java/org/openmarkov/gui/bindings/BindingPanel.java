package org.openmarkov.gui.bindings;

import org.jetbrains.annotations.NotNull;
import org.openmarkov.gui.configuration.GUIColors;
import org.openmarkov.java.swing.ComponentUtilities;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.HashSet;

public final class BindingPanel extends JPanel {
    
    private final @NotNull MousePanel mousePanel;
    private final @NotNull KeyboardPanel keyboardPanel;
    private @NotNull Binding binding;
    private final @NotNull JLabel combinationDisplayLabel;
    
    public BindingPanel(@NotNull Binding binding) {
        super();
        this.binding = binding;
        this.mousePanel = new MousePanel();
        this.keyboardPanel = new KeyboardPanel();
        this.combinationDisplayLabel = new JLabel();
        this.combinationDisplayLabel.setText("Combination: " + this.binding.inputCombination());
        
        for (var input : binding.inputCombination().inputs().toList()) {
            this.markInput(input, true, false);
        }
        this.setLayout(new BorderLayout());
        this.add(this.combinationDisplayLabel, BorderLayout.NORTH);
        this.add(ComponentUtilities.joinComponents(FlowLayout.LEFT, this.keyboardPanel, this.mousePanel), BorderLayout.CENTER);
        
        this.mousePanel.addRegionClickListener(new MousePanel.RegionClickListener() {
            @Override public void onButtonRegionClicked(int mouseButton) {
                var buttonIsPresent = BindingPanel.this.binding.inputCombination()
                                                               .inputs()
                                                               .anyMatch(input -> input instanceof Input.Click click && click.mouseButton() == mouseButton);
                BindingPanel.this.markInput(new Input.Click(mouseButton, 1), !buttonIsPresent, true);
            }
            
            @Override public void onWheelRegionClicked(boolean up) {
                var previousWheel = BindingPanel.this.binding.inputCombination()
                                                             .inputs()
                                                             .filter(Input.MouseWheel.class::isInstance)
                                                             .map(Input.MouseWheel.class::cast)
                                                             .findFirst();
                if (previousWheel.isPresent()) {
                    BindingPanel.this.markInput(new Input.MouseWheel(false), false, true);
                    BindingPanel.this.markInput(new Input.MouseWheel(true), false, true);
                }
                BindingPanel.this.markInput(new Input.MouseWheel(up), previousWheel.isEmpty() || previousWheel.get()
                                                                                                              .up() != up, true);
            }
        });
        
        this.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                if (e.getClickCount() > 1) {
                    BindingPanel.this.binding.inputCombination()
                                             .inputs()
                                             .filter(Input.Click.class::isInstance)
                                             .map(Input.Click.class::cast)
                                             .map(click -> new Input.Click(click.mouseButton(), 1))
                                             .forEach(click -> BindingPanel.this.markInput(click, true, true));
                }
                BindingPanel.this.markInput(new Input.Click(e.getButton(), e.getClickCount()), true, true);
            }
        });
        this.addMouseWheelListener(e -> this.markInput(new Input.MouseWheel(e.getWheelRotation() < 0), true, true));
        this.addKeyListener(new KeyListener() {
            
            private final Collection<Integer> pressedKeys = new HashSet<>();
            
            @Override public void keyTyped(KeyEvent e) {
            }
            
            @Override public void keyPressed(KeyEvent e) {
                if (!this.pressedKeys.add(e.getKeyCode())) {
                    return;
                }
                var keyIsPresent = BindingPanel.this.binding.inputCombination()
                                                            .inputs()
                                                            .anyMatch(input -> input instanceof Input.Key(
                                                                    int key
                                                            ) && key == e.getKeyCode());
                BindingPanel.this.markInput(new Input.Key(e.getKeyCode()), !keyIsPresent, true);
                System.out.println("Pressed");
            }
            
            @Override public void keyReleased(KeyEvent e) {
                this.pressedKeys.remove(e.getKeyCode());
            }
        });
        for (var entry : this.keyboardPanel.buttonsByKeyCode().toList()) {
            var keyCode = entry.getKey();
            var buttons = entry.getValue();
            for (var button : buttons) {
                button.addActionListener(new ActionListener() {
                    @Override public void actionPerformed(ActionEvent e) {
                        boolean selected = !button.getBackground().equals(GUIColors.Bindings.BOUND.getColor());
                        BindingPanel.this.markInput(new Input.Key(keyCode), selected, true);
                    }
                });
            }
        }
        this.setFocusable(true);
        SwingUtilities.invokeLater(this::requestFocusInWindow);
    }
    
    public void setBinding(@NotNull Binding newBinding) {
        this.binding.inputCombination().inputs().forEach(combination -> this.markInput(combination, false, false));
        this.binding = newBinding;
        this.binding.inputCombination().inputs().forEach(combination -> this.markInput(combination, true, false));
    }
    
    private void markInput(Input input, boolean selected, boolean updateBinding) {
        if (updateBinding) {
            input = Binding.tryUpdateBinding(this.binding, input, selected, oInput -> this.markInput(oInput, false, true));
        }
        if (input == null) return;
        switch (input) {
            case Input.Click click -> this.mousePanel.markMouseButtonsAsPressed(click.mouseButton(), selected);
            case Input.Key key -> this.keyboardPanel.setKeyHighlight(key.key(), selected);
            case Input.MouseWheel mouseWheel -> this.mousePanel.markWheel(mouseWheel.up(), selected);
        }
        this.combinationDisplayLabel.setText("Combination: " + this.binding.inputCombination());
    }
    
}
