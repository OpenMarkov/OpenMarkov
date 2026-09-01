package org.openmarkov.gui.dialog.common;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.openmarkov.core.exception.UnreachableException;
import org.openmarkov.gui.util.GUIUtils;
import org.openmarkov.java.swing.ComponentUtilities;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import java.awt.Window;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public final class OptionDialog<Option extends OptionDialog.ToOptionsDialog> extends BottomPanelButtonDialog {
    
    interface ToOptionsDialog {
        public default String buttonText() {
            return this.toString();
        }
    }
    
    Optional<Option> selectedOption = Optional.empty();
    
    public <T extends Enum<T> & ToOptionsDialog> OptionDialog(Window parent, String title, String message, @NotNull Class<? extends T> enumClass) {
        T[] values;
        try {
            values = (T[]) enumClass.getMethod("values").invoke(null);
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new UnreachableException(e);
        }
        this(parent, title, message, (Option[]) values);
    }
    
    public OptionDialog(Window parent, String title, String message, @NotNull Option @NotNull [] items) {
        this(parent, title, message, List.of(items));
    }
    
    public OptionDialog(Window parent, String title, String message, @NotNull Stream<@NotNull Option> items) {
        this(parent, title, message, items::iterator);
    }
    
    public OptionDialog(Window parent, String title, String message, @NotNull Iterable<@NotNull Option> items) {
        super(parent);
        this.setModal(true);
        this.setTitle(title);
        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        for (var item : items) {
            JButton itemButton = new JButton(item.buttonText());
            itemButton.addActionListener(_ -> {
                this.selectedOption = Optional.of(item);
                this.dispose();
            });
            this.addButtonToButtonsPanel(itemButton);
        }
        var buttons = new ArrayList<>(
                ComponentUtilities.findComponents(OptionDialog.this, JButton.class, _ -> true).toList());
        Collections.reverse(buttons);
        GUIUtils.assignButtonsToKeys((JComponent) this.getContentPane(), buttons, _ -> {
            this.selectedOption = Optional.empty();
            this.dispose();
        });
        this.getComponentsPanel().add(new JLabel(message));
        this.pack();
    }
    
    public @Nullable Option request() {
        this.selectedOption = Optional.empty();
        GUIUtils.showDialog(this);
        return this.selectedOption.orElse(null);
    }
    
    public @NotNull Option request(@NotNull Option defaultOnClose) {
        this.selectedOption = Optional.empty();
        GUIUtils.showDialog(this);
        return this.selectedOption.orElse(defaultOnClose);
    }
    
}