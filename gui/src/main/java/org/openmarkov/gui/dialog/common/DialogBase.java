/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.common;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.openmarkov.core.localize.StringDatabase;
import org.openmarkov.gui.loader.element.IconBind;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.Serial;

/**
 * This class implements a dialog where a the programmer can set a default
 * button and a cancel button.
 *
 * @author jmendoza
 * @version 1.0 jmendoza
 */
public class DialogBase extends JDialog {
    
    @Serial
    private static final long serialVersionUID = 6121463474893584183L;
    
    private JButton jButtonCancel = null;

	/**
	 * Constructor that invokes its superclass constructor and registers a
	 * listener that executes the 'click' of the cancel button when the key ESC
	 * is pressed.
	 *
	 * @param owner window that owns the dialog box.
	 */
	public DialogBase(Window owner) {
		super(owner);
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		setName("DialogBase");
		addWindowListener(new WindowAdapter() {
			@Override public void windowClosing(WindowEvent e) {
                if (DialogBase.this.jButtonCancel != null) {
                    DialogBase.this.jButtonCancel.doClick();
				}
			}
		});
        ActionListener listener = evt -> {
            if (this.jButtonCancel != null) {
                this.jButtonCancel.doClick();
            }
        };
		getRootPane().registerKeyboardAction(listener, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
				JComponent.WHEN_IN_FOCUSED_WINDOW);
	}

	/**
	 * Sets the button whose 'click' will be called when the key ENTER is
	 * pressed.
	 *
	 * @param defaultButton button invoked when the key ENTER is pressed.
	 */
	public void setDefaultButton(JButton defaultButton) {
		getRootPane().setDefaultButton(defaultButton);
	}

	/**
	 * Sets the button whose 'click' will be called when the key ESC is pressed.
	 *
	 * @param button button invoked when the key ESC is pressed.
	 */
	public void setCancelButton(JButton button) {
        this.jButtonCancel = button;
    }
    
    public @Nullable JButton getCancelButton() {
        return this.jButtonCancel;
    }
    
    /**
     * Creates a standard Cancel button pre-configured with icon, localized foreground,
     * mnemonic, and an action listener that disposes the enclosing window.
     *
     * @return a ready-to-use Cancel button
     */
    public static @NotNull JButton generateGenericCancelButton() {
        var jButtonCancel = new JButton();
        jButtonCancel.setName("jButtonCancel");
        jButtonCancel.setIcon(IconBind.REMOVE_ENABLED.icon());
        jButtonCancel.setText(StringDatabase.getUniqueInstance()
                                            .getString("OKCancelHorizontalDialog.jButtonCancel.Text"));
        jButtonCancel.setMnemonic(StringDatabase.getUniqueInstance()
                                                .getString("OKCancelHorizontalDialog.jButtonCancel.Mnemonic")
                                                .charAt(0));
        jButtonCancel.addActionListener(e -> {
            var container = jButtonCancel.getParent();
            while (container != null && !(container instanceof Window)) {
                container = container.getParent();
            }
            if (container instanceof Window window) {
                window.setVisible(false);
                window.dispose();
            }
        });
        return jButtonCancel;
	}
}
