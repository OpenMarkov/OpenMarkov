package org.openmarkov.gui.dialog;

import org.openmarkov.core.exception.UnrecoverableException;
import org.openmarkov.gui.dialog.common.BottomPanelButtonDialog;
import org.openmarkov.gui.toolplugin.SaveLogToolPlugin;
import org.openmarkov.gui.window.MainGUI;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.net.URI;


/**
 * Dialog displayed when an unexpected or unreachable exception occurs, showing the full
 * stack trace and offering a button to copy it to the clipboard for developer reporting.
 */
public final class UnexpectedThrowableDialog extends BottomPanelButtonDialog {
    
    private static final float TITLE_FONT_SIZE = 14.0f;
    
    public UnexpectedThrowableDialog(Throwable e, OMExceptionHandler.ExceptionType exceptionType) {
        super(null);
        this.setTitle(switch (exceptionType) {
            case UNREACHABLE -> "Unpredicted error";
            case EXPECTED -> "Error";
            case RUNTIME -> "Unexpected error";
        });
        final String defaultLogFileName = SaveLogToolPlugin.generateDefaultLogFileName();
        this.getComponentsPanel().setLayout(new BoxLayout(this.getComponentsPanel(), BoxLayout.Y_AXIS));
        JLabel titleLabel = new JLabel("An " + this.getTitle().toLowerCase() + " has occurred.");
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setFont(titleLabel.getFont().deriveFont(UnexpectedThrowableDialog.TITLE_FONT_SIZE));
        this.getComponentsPanel().add(titleLabel);
        JLabel subTitleLabel = new JLabel("Please, send this to the developers of OpenMarkov.");
        subTitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        subTitleLabel.setFont(subTitleLabel.getFont().deriveFont(UnexpectedThrowableDialog.TITLE_FONT_SIZE));
        this.getComponentsPanel().add(subTitleLabel);
        
        JTextArea areaWithThrowableDescription = new JTextArea(UnexpectedThrowableDialog.stringifyThrowable(e));
        areaWithThrowableDescription.setEditable(false);
        this.getComponentsPanel().add(new JScrollPane(areaWithThrowableDescription));
        
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        var closeButton = new JButton("Close");
        closeButton.addActionListener(e1 -> this.dispose());
        this.setCancelButton(closeButton);
        var copyButton = new JButton("Copy stacktrace");
        copyButton.addActionListener(e1 -> Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
                new StringSelection(UnexpectedThrowableDialog.stringifyThrowable(e)), null));
        this.addButtonToButtonsPanel(copyButton);
        
        var saveLog = new JButton("Save log");
        saveLog.addActionListener(_ -> {
            try {
                SaveLogToolPlugin.requestSaveLog(this, defaultLogFileName);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Could not save the log file", JOptionPane.ERROR_MESSAGE);
            }
        });
        this.addButtonToButtonsPanel(saveLog);
        
        
        var reportButton = new JButton("Report bug");
        reportButton.addActionListener(e1 -> {
            try {
                Desktop.getDesktop()
                       .browse(URI.create("https://github.com/OpenMarkov/OpenMarkov/issues/new?template=report-a-bug.yml"));
            } catch (IOException ex) {
                throw new UnrecoverableException(ex);
            }
        });
        this.addButtonToButtonsPanel(reportButton);
        this.pack();
        if (this.getHeight() > 500) {
            this.setSize(new Dimension(this.getWidth(), 500));
        }
        if (this.getWidth() > 800) {
            this.setSize(new Dimension(800, this.getHeight()));
        }
        var usedWindow = KeyboardFocusManager
                .getCurrentKeyboardFocusManager()
                .getActiveWindow();
        if (usedWindow == null) {
            usedWindow = MainGUI.INSTANCE;
        }
        this.setLocationRelativeTo(usedWindow);
    }
    
    private static String stringifyThrowable(Throwable e) {
        StringBuilder sb = new StringBuilder();
        sb.append(e.toString());
        for (var staceElement : e.getStackTrace()) {
            sb.append("\n\tat ").append(staceElement);
        }
        return sb.toString();
    }
    
}
