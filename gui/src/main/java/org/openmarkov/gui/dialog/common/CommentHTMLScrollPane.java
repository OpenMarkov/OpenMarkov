/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.common;

import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.exception.UnrecoverableException;
import org.openmarkov.gui.dialog.CommentListener;

import javax.swing.*;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.Serial;
import java.util.HashSet;

/**
 * Comment HTML Box Scroll Pane This class encapsulate all the behaviour as a
 * single component so the programmer must not be worried about internal
 * initialization of the components
 *
 * @author jlgozalo
 * @version 1.0 based on definition made by alberto
 */
public class CommentHTMLScrollPane extends JScrollPane implements MouseListener {
    /**
     *
     */
    @Serial
    private static final long serialVersionUID = -8678529566501560594L;
    private static final int HTML_COMMENT_HEIGHT = 10;
    private static final int HTML_COMMENT_WIDTH = 30;
    private JTextPane jTextPaneCommentHTML = null;
    private HTMLTextEditor hTMLTextEditor = null;
    /**
     * Listener to the comment changes.
     */
    private final HashSet<CommentListener> commentListeners = new HashSet<CommentListener>();
    private String title = "";
    private boolean isChanged = false;
    private boolean isEmpty = true;
    private boolean isEditable = true;
    
    /**
     * Double Click Selector for the HTML Comment area
     */
    private final MouseListener doubleClickSelector = new MouseAdapter() {
        @Override public void mouseClicked(MouseEvent e) {
        }
    };
    
    /**
     * This method initialises this instance.
     */
    public CommentHTMLScrollPane() {
        initialize();
    }
    
    /**
     * This method initialises this instance.
     */
    public CommentHTMLScrollPane(final String title) {
        this.title = title;
        initialize();
    }
    
    /**
     * @param title the title to set
     */
    public void setTitle(final String title) {
        this.title = title;
        if (hTMLTextEditor != null) {
            hTMLTextEditor.setTitle(title);
        }
    }
    
    /**
     * This method configures the dialog box.
     */
    private void initialize() {
        setBorder(BorderFactory.createLineBorder(SystemColor.activeCaption, 2));
        setViewportView(getJTextPaneCommentHTML());
        setSize(HTML_COMMENT_WIDTH, HTML_COMMENT_HEIGHT);
        hTMLTextEditor = new HTMLTextEditor(null, "");
        hTMLTextEditor.setTitle(title);
        hTMLTextEditor.setVisible(false);
    }
    
    /**
     * This method initialises jTextPaneCommentHTML
     *
     * @return the JTextPane with the HTML Comment
     */
    private JTextPane getJTextPaneCommentHTML() {
        if (jTextPaneCommentHTML == null) {
            jTextPaneCommentHTML = new JTextPane();
            jTextPaneCommentHTML.setEditable(false);
            jTextPaneCommentHTML.setSize(new Dimension(HTML_COMMENT_WIDTH, HTML_COMMENT_HEIGHT));
            jTextPaneCommentHTML.setText("");
            jTextPaneCommentHTML.addMouseListener(doubleClickSelector);
            jTextPaneCommentHTML.addMouseListener(this);
        }
        return jTextPaneCommentHTML;
    }
    
    /**
     * This method set the foreground of jTextPaneCommentHTML
     *
     * @param text the foreground to put in the comment
     */
    public void setCommentHTMLTextPaneText(String text) {
        /*
        Country is used in Locale.java just to set the LocaleExtensions as
        LocaleExtensions.CALENDAR_JAPANESE or LocaleExtensions.NUMBER_THAI
        But it cannot be set up as null or empty. So we will not use the "real country"
        but just the same string as the language.
         */
        
        // creates the HTML object
        HTMLEditorKit editorKit = new HTMLEditorKit();
        try {
            getJTextPaneCommentHTML().setEditorKit(editorKit);
            getJTextPaneCommentHTML().setContentType("text/html");
            getJTextPaneCommentHTML().setText(text);
            getJTextPaneCommentHTML().setCaretPosition(0);
        } catch (IllegalArgumentException ignored) {
        }
    }
    
    /**
     * This method get the foreground of jTextPaneCommentHTML
     *
     * @return the foreground of the comment
     */
    public String getCommentText() {
        String text = "";
        if (jTextPaneCommentHTML == null) {
            // do nothing
        } else {
            text = jTextPaneCommentHTML.getText();
        }
        return text;
    }
    
    /**
     * public method to set the document hTMLTextEditor in the JTextPane
     *
     * @param doc - the HTML document to put in the hTMLTextEditor
     */
    
    /**
     * initialize the HTMLTextEditor component
     */
    private HTMLTextEditor getHTMLTextEditor() {
        if (hTMLTextEditor == null) {
            hTMLTextEditor = new HTMLTextEditor(null, jTextPaneCommentHTML.getText());
            hTMLTextEditor.setTitle(title);
            hTMLTextEditor.setVisible(true);
        }
        return hTMLTextEditor;
    }
    
    @Override public void mouseClicked(MouseEvent e) {
        // TODO Auto-generated method stub
        if (!((e.getClickCount() == 2) && (isEditable))) {
            return;
        }
        String comment = jTextPaneCommentHTML.getText() != null ? jTextPaneCommentHTML.getText() : "";
        hTMLTextEditor = new HTMLTextEditor(null, comment);
        hTMLTextEditor.setTitle(title);
        hTMLTextEditor.setVisible(true);
        if (hTMLTextEditor.getOkButtonStatus()) {
            setCommentHTMLTextPaneText(hTMLTextEditor.getCommentText());
            isChanged = true;
            isEmpty = hTMLTextEditor.getCommentText().trim().replaceAll("[\r\n]", "").isEmpty();
            try {
                notifyCommentChanged();
            } catch (DoEditException ex) {
                throw new UnrecoverableException(ex);
            }
        }
    }
    
    private void notifyCommentChanged() throws DoEditException {
        for (CommentListener listener : commentListeners) {
            listener.commentHasChanged();
        }
    }
    
    @Override public void mouseEntered(MouseEvent e) {
        // TODO Auto-generated method stub
    }
    
    @Override public void mouseExited(MouseEvent e) {
        // TODO Auto-generated method stub
    }
    
    @Override public void mousePressed(MouseEvent e) {
        // TODO Auto-generated method stub
    }
    
    @Override public void mouseReleased(MouseEvent e) {
        // TODO Auto-generated method stub
    }
    
    /**
     * Registers a listener to be notified when the comment foreground changes.
     *
     * @param newCommentListener the listener to add
     */
    public void addCommentListener(CommentListener newCommentListener) {
        commentListeners.add(newCommentListener);
    }
    
    public boolean isChanged() {
        return isChanged;
    }
    
    public boolean isEmpty() {
        return isEmpty;
    }
    
    public void setEditable(boolean isEditable) {
        this.isEditable = isEditable;
    }
}
