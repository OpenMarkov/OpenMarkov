package org.openmarkov.gui.dialog.common;

import org.jetbrains.annotations.Nullable;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class VariableExpressionAutoCompleter {
    
    private JTextComponent textComponent;
    private JPopupMenu autocompletePopupMenu;
    private JList<String> suggestionList;
    private DefaultListModel<String> listModel;
    
    private final ArrayList<String> variables;
    private final ArrayList<String> functions;
    
    VariableExpressionAutoCompleter() {
        this.variables = new ArrayList<>();
        this.functions = new ArrayList<>();
    }
    
    public ArrayList<String> getVariables() {
        return this.variables;
    }
    
    public ArrayList<String> getFunctions() {
        return this.functions;
    }
    
    public void setupOn(JTextComponent textComponent) {
        this.textComponent = textComponent;
        this.autocompletePopupMenu = new JPopupMenu();
        this.listModel = new DefaultListModel<>();
        this.suggestionList = new JList<>(this.listModel);
        this.suggestionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.suggestionList.setFocusable(false); // Keep focus on the text field
        
        // Wrap list in a scroll pane inside the popup
        JScrollPane scrollPane = new JScrollPane(this.suggestionList);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        this.autocompletePopupMenu.add(scrollPane);
        this.autocompletePopupMenu.setFocusable(false);
        
        // Listen for typing changes
        this.textComponent.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                checkAutocomplete(false);
            }
            
            public void removeUpdate(DocumentEvent e) {
                checkAutocomplete(false);
            }
            
            public void changedUpdate(DocumentEvent e) {
                checkAutocomplete(false);
            }
        });
        
        // Handle navigation keys (Up, Down, Enter, Escape)
        this.textComponent.addKeyListener((KeyPressed) e -> {
            if (e.getKeyCode() == KeyEvent.VK_SPACE && (e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) == InputEvent.CTRL_DOWN_MASK) {
                this.checkAutocomplete(true);
                e.consume();
                return;
            }
            
            if (this.autocompletePopupMenu.isVisible()) {
                int selectedIndex = this.suggestionList.getSelectedIndex();
                int size = this.suggestionList.getModel().getSize();
                
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_DOWN -> {
                        if (size > 0) {
                            this.suggestionList.setSelectedIndex((selectedIndex + 1) % size);
                            this.suggestionList.ensureIndexIsVisible(this.suggestionList.getSelectedIndex());
                            e.consume(); // Prevent moving caret
                        }
                    }
                    case KeyEvent.VK_UP -> {
                        if (size > 0) {
                            this.suggestionList.setSelectedIndex((selectedIndex - 1 + size) % size);
                            this.suggestionList.ensureIndexIsVisible(this.suggestionList.getSelectedIndex());
                            e.consume(); // Prevent moving caret
                        }
                    }
                    case KeyEvent.VK_RIGHT, KeyEvent.VK_LEFT -> this.checkAutocomplete(false);
                    case KeyEvent.VK_ENTER -> {
                        if (selectedIndex != -1) {
                            insertSelection();
                            e.consume(); // Prevent default action (like form submission)
                        }
                    }
                    case KeyEvent.VK_ESCAPE -> {
                        this.autocompletePopupMenu.setVisible(false);
                        e.consume();
                    }
                }
            }
        });
        
        // Double-click or click to select
        this.suggestionList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    insertSelection();
                }
            }
        });
    }
    
    private interface KeyPressed extends KeyListener {
        @Override default void keyTyped(KeyEvent e) {
        }
        
        @Override default void keyReleased(KeyEvent e) {
        }
    }
    
    enum AutoCompleteContext {
        ALL,
        VARIABLE,
        FUNCTION;
        
        boolean isContextForInput(String input) {
            return switch (this) {
                case ALL -> input.isEmpty();
                case VARIABLE -> input.startsWith("{");
                case FUNCTION -> input.matches("^[a-zA-Z]+$");
            };
        }
        
        public static @Nullable AutoCompleteContext of(String prefix) {
            return Arrays.stream(AutoCompleteContext.values())
                         .filter(context -> context.isContextForInput(prefix))
                         .findFirst()
                         .orElse(null);
        }
    }
    
    private void checkAutocomplete(boolean forceTrigger) {
        SwingUtilities.invokeLater(() -> {
            String text = this.textComponent.getText();
            int caretPos = this.textComponent.getCaretPosition();
            this.listModel.clear();
            System.out.println(caretPos);
            System.out.println(text);
            
            // Extract the word/token currently being typed before the caret
            String input = getInputBeforeCaret(text, caretPos);
            if (input.isEmpty() && !forceTrigger) {
                this.autocompletePopupMenu.setVisible(false);
                return;
            }
            
            System.out.println("Is use autocomplete");
            
            AutoCompleteContext context = AutoCompleteContext.of(input);
            if (context == null) {
                return;
            }
            
            var lowerCasedInput = input.toLowerCase();
            
            List<String> matches = new ArrayList<>(switch (context) {
                case ALL -> Stream.concat(
                        this.variables.stream().map(v -> "{" + v + "}"),
                        this.functions.stream().map(f -> f + "()")
                ).toList();
                case VARIABLE -> this.variables.stream()
                                               .filter(v -> v.toLowerCase().startsWith(lowerCasedInput.substring(1)))
                                               .map(v -> "{" + v + "}") // Format suggestion with brackets
                                               .toList();
                case FUNCTION -> this.functions.stream()
                                               .filter(f -> f.toLowerCase().startsWith(lowerCasedInput))
                                               .map(f -> f + "()") // Format suggestion with parentheses
                                               .toList();
            });
            matches.sort(Comparator.comparing(s -> {
                s = s.toLowerCase();
                if (s.contains("{")) {
                    return s.substring(s.indexOf('{') + "{".length());
                }
                return s;
            }));
            
            System.out.println("Matches are " + matches);
            
            if (!matches.isEmpty()) {
                for (String match : matches) {
                    this.listModel.addElement(match);
                }
                this.suggestionList.setSelectedIndex(0); // Auto-select first option
                
                // Position the popup exactly under the text cursor (caret)
                try {
                    Rectangle rect = this.textComponent.modelToView2D(caretPos).getBounds();
                    this.autocompletePopupMenu.setPreferredSize(new Dimension(200, Math.min(150, matches.size() * 20 + 10)));
                    this.autocompletePopupMenu.show(this.textComponent, rect.x, rect.y + rect.height);
                    this.textComponent.requestFocusInWindow(); // Keep cursor focus on textfield
                } catch (BadLocationException e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("Hiding auto complete");
                this.autocompletePopupMenu.setVisible(false);
            }
        });
    }
    
    private static String getInputBeforeCaret(String text, int caretPos) {
        // Look backwards to find the start of our current "word token"
        // Variables start with '{' and can contain spaces.
        // Functions start with a letter and stop at spaces or non-word chars.
        int start = caretPos;
        while (start > 0) {
            char c = text.charAt(start - 1);
            if (c == '{') {
                return text.substring(start - 1, caretPos);
            }
            // If we hit an ending curly brace, or punctuation/delimiters, stop looking back
            if (c == '}' || c == '(' || c == ')' || c == ',') {
                break;
            }
            // If we see a space, we only keep going backward if we are inside a variable context
            boolean hasUnclosedOpenBraceBefore = hasUnclosedOpenBraceBefore(text, start);
            if (c == ' ' && !hasUnclosedOpenBraceBefore) {
                break;
            }
            start--;
        }
        return text.substring(start, caretPos).trim();
    }
    
    private static boolean hasUnclosedOpenBraceBefore(String text, int pos) {
        for (int i = pos - 1; i >= 0; i--) {
            if (text.charAt(i) == '{') return true;
            if (text.charAt(i) == '}') return false;
        }
        return false;
    }
    
    private void insertSelection() {
        String selected = this.suggestionList.getSelectedValue();
        if (selected == null) return;
        
        String text = this.textComponent.getText();
        int caretPos = this.textComponent.getCaretPosition();
        String prefix = getInputBeforeCaret(text, caretPos);
        
        // Replace the prefix with the chosen autocomplete option
        int startPos = caretPos - prefix.length();
        String before = text.substring(0, startPos);
        String after = text.substring(caretPos);
        
        this.textComponent.setText(before + selected + after);
        
        // Reposition caret smartly (e.g., inside the function parens or after the variable)
        int newCaretPos = startPos + selected.length();
        if (selected.endsWith("()")) {
            newCaretPos--; // Place caret inside the parenthesis sum(|)
        }
        this.textComponent.setCaretPosition(newCaretPos);
        
        this.autocompletePopupMenu.setVisible(false);
    }
    
}
