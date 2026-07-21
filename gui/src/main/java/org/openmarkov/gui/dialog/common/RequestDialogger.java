/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.common;


import com.google.gson.reflect.TypeToken;
import org.jetbrains.annotations.NotNull;
import org.openmarkov.core.localize.StringDatabase;
import org.openmarkov.gui.loader.element.IconBind;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Fluent builder for modal request dialogs that collect user input, validate it,
 * transform it into a typed result, and dispatch callbacks on OK or Cancel.
 *
 * @param <Content> the Swing container type used as the dialog's content area
 * @param <T>       the type of the result produced from user input
 */
public final class RequestDialogger<Content extends Container, T> {
    
    private final Window owner;
    
    private final JDialog dialog;
    
    private final Content content;
    
    private final Tokenized<T, Function<Content, T>> processInput;
    private final ArrayList<Tokenized<?, Consumer<T>>> onOk;
    private final ArrayList<Runnable> onCancel;
    
    private final ArrayList<BiConsumer<Content, Validator>> validations;
    
    private final JButton okButton;
    private final JButton cancelButton;
    
    private RequestDialogger(Window owner, JDialog dialog, Content content, Tokenized<T, Function<Content, T>> processInput, ArrayList<BiConsumer<Content, Validator>> validations, JButton okButton, JButton cancelButton, ArrayList<Tokenized<?, Consumer<T>>> onOk, ArrayList<Runnable> onCancel) {
        this.owner = owner;
        this.dialog = dialog;
        this.content = content;
        this.processInput = processInput;
        this.validations = validations;
        this.okButton = okButton;
        this.cancelButton = cancelButton;
        this.onOk = onOk;
        this.onCancel = onCancel;
    }
    
    /**
     * Creates a new request dialog wrapping the given content panel.
     *
     * @param owner   the parent window for modality
     * @param content the container holding the input components
     * @param <Content> the content container type
     * @return a configured {@link RequestDialogger} ready for further customisation
     */
    public static <Content extends Container> RequestDialogger<Content, Boolean> of(Window owner, Content content) {
        RequestDialogger<Content, Boolean> requestDialogger = new RequestDialogger<>(owner, new JDialog(owner), content, new Tokenized<>(new TypeToken<Boolean>() {
        }, (ignored) -> true), new ArrayList<>(), new JButton(), new JButton(), new ArrayList<Tokenized<?, Consumer<Boolean>>>(), new ArrayList<Runnable>());
        requestDialogger.initialize();
        return requestDialogger;
    }
    
    public static RequestDialogger<JPanel, Boolean> of(Window owner, Component... components) {
        var panel = new JPanel();
        for (var component : components) {
            panel.add(component);
        }
        return RequestDialogger.of(owner, panel);
    }
    
    
    private void reloadButtonListeners() {
        Arrays.stream(this.okButton.getActionListeners()).toList().forEach(this.okButton::removeActionListener);
        Arrays.stream(this.cancelButton.getActionListeners()).toList().forEach(this.cancelButton::removeActionListener);
        this.okButton.addActionListener(e -> {
            if (this.validateOverUI()) {
                var value = this.processInput.value.apply(this.content);
                this.onOk.forEach(action -> action.value.accept(value));
                this.dialog.setVisible(false);
            }
        });
        ActionListener cancelAction = e -> {
            this.onCancel.forEach(Runnable::run);
            this.dialog.setVisible(false);
        };
        this.cancelButton.addActionListener(cancelAction);
        this.dialog.getRootPane().registerKeyboardAction(cancelAction, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                                                         JComponent.WHEN_IN_FOCUSED_WINDOW);
    }
    
    private void initialize() {
        this.reloadButtonListeners();
        this.dialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
        this.dialog.setLocationRelativeTo(this.owner);
        this.dialog.setResizable(true);
        this.dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        this.dialog.getRootPane().setDefaultButton(this.okButton);
        this.dialog.addWindowListener(new WindowListener() {
            @Override public void windowOpened(WindowEvent e) {
            }
            
            @Override public void windowClosing(WindowEvent e) {
                RequestDialogger.this.onCancel.forEach(Runnable::run);
                RequestDialogger.this.dialog.setVisible(false);
            }
            
            @Override public void windowClosed(WindowEvent e) {
            }
            
            @Override public void windowIconified(WindowEvent e) {
            }
            
            @Override public void windowDeiconified(WindowEvent e) {
            }
            
            @Override public void windowActivated(WindowEvent e) {
            }
            
            @Override public void windowDeactivated(WindowEvent e) {
            }
        });
        this.okButton.setIcon(IconBind.ACCEPT_ENABLED.icon());
        this.okButton.setText(StringDatabase.getUniqueInstance().getString("OKCancelHorizontalDialog.jButtonOK.Text"));
        this.okButton.setMnemonic(StringDatabase.getUniqueInstance()
                                                .getString("OKCancelHorizontalDialog.jButtonOK.Mnemonic")
                                                .charAt(0));
        
        this.cancelButton.setIcon(IconBind.REMOVE_ENABLED.icon());
        this.cancelButton.setText(StringDatabase.getUniqueInstance()
                                                .getString("OKCancelHorizontalDialog.jButtonCancel.Text"));
        this.cancelButton.setMnemonic(StringDatabase.getUniqueInstance()
                                                    .getString("OKCancelHorizontalDialog.jButtonCancel.Mnemonic")
                                                    .charAt(0));
        
        var buttonsPanel = new JPanel();
        
        
        buttonsPanel.add(this.okButton);
        buttonsPanel.add(this.cancelButton);
        
        var visualPanel = new JPanel();
        visualPanel.setLayout(new BorderLayout());
        visualPanel.add(new JScrollPane(this.content), BorderLayout.PAGE_START);
        visualPanel.add(buttonsPanel, BorderLayout.PAGE_END);
        this.dialog.setContentPane(visualPanel);
        this.dialog.pack();
    }
    
    
    /**
     * Runs all registered validations and shows error messages if any fail.
     *
     * @return {@code true} if all validations passed
     */
    public boolean validateOverUI() {
        var validator = new Validator();
        for (var validation : this.validations) {
            validation.accept(this.content, validator);
        }
        if (!validator.isValid()) {
            var errors = validator.errors;
            if (errors.size() > 1) {
                var errorsAsString = String.join("\n", errors.stream().map(error -> "- " + error).toList());
                JOptionPane.showMessageDialog(this.owner, errorsAsString, "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this.owner, validator.errors.get(0), "Error", JOptionPane.ERROR_MESSAGE);
            }
            return false;
        }
        return true;
    }
    
    /**
     * Adds a validation step that is executed before the OK action.
     *
     * @param validator a consumer that inspects the content and records errors via {@link Validator}
     * @return this builder for chaining
     */
    public RequestDialogger<Content, T> validating(BiConsumer<Content, Validator> validator) {
        this.validations.add(validator);
        return this;
    }
    
    /**
     * Transforms the dialog's result type by applying a mapping function to the content.
     *
     * @param token    type token for the new result type
     * @param function function that extracts the result from the content container
     * @param <T>      the new result type
     * @return a new builder with the mapped result type
     */
    public <T> RequestDialogger<Content, T> mapInputAs(TypeToken<T> token, Function<Content, @NotNull T> function) {
        ArrayList<Tokenized<?, Consumer<T>>> newOnOK = this.onOk.stream()
                                                                .filter(oldOnOk -> oldOnOk.t.isAssignableFrom(token))
                                                                .map(oldOnOk -> new Tokenized<>(oldOnOk.t, (Consumer<T>) oldOnOk.value))
                                                                .collect(Collectors.toCollection(ArrayList::new));
        RequestDialogger<Content, T> mapped = new RequestDialogger<>(this.owner, this.dialog, this.content, new Tokenized<>(token, function), this.validations, this.okButton, this.cancelButton, newOnOK, this.onCancel);
        mapped.reloadButtonListeners();
        return mapped;
    }
    
    /**
     * Registers a callback to run when the user confirms the dialog.
     *
     * @param onOk callback that receives the processed input value
     * @return this builder for chaining
     */
    public RequestDialogger<Content, T> onOk(Consumer<T> onOk) {
        this.onOk.add(new Tokenized<>(this.processInput.t, onOk));
        return this;
    }
    
    /**
     * Registers a callback to run when the user cancels the dialog.
     *
     * @param onCancel action to execute on cancellation
     * @return this builder for chaining
     */
    public RequestDialogger<Content, T> onCancel(Runnable onCancel) {
        this.onCancel.add(onCancel);
        return this;
    }
    
    /**
     * Sets the title of the dialog window.
     *
     * @param title the dialog title
     * @return this builder for chaining
     */
    public RequestDialogger<Content, T> withTitle(String title) {
        this.dialog.setTitle(title);
        return this;
    }
    
    /**
     * Shows the dialog modally, blocking until the user confirms or cancels.
     */
    public void request() {
        this.dialog.setVisible(true);
    }
    
    
    /**
     * Accumulates validation errors that are displayed to the user when the OK button is pressed.
     */
    public static class Validator {
        private final ArrayList<String> errors = new ArrayList<>();
        
        public void addError(String error) {
            this.errors.add(error);
        }
        
        public boolean addErrorWhen(boolean when, String error) {
            if (when) {
                this.errors.add(error);
            }
            return when;
        }
        
        private boolean isValid() {
            return this.errors.isEmpty();
        }
    }
    
    record Tokenized<T, U>(TypeToken<T> t, U value) {
    }
    
    
}
