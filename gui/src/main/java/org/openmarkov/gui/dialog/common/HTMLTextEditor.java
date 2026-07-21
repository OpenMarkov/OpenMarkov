/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.common;

//import com.hexidec.ekit.EkitCore;
//import com.hexidec.ekit.compoment.ExtendedHTMLDocument;
//import com.hexidec.ekit.compoment.ExtendedHTMLEditorKit;
import org.openmarkov.gui.loader.element.OpenMarkovLogoIcon;
import org.openmarkov.core.localize.StringDatabase;


import javax.swing.*;
import java.awt.*;
import java.io.Serial;

/**
 * initialises a HTML Editor for the different comments in EditNodeDialog
 *
 * @author Alberto Manuel Ruiz Lafuente UCLM 2008
 * @version 1.1 jlgozalo - javadocs, undo variables and localize methods
 * @version 1.2 jrico - Replaced JavaFX's HTMLEditor with the custom implementation of {@link SimpleHTMLEditor}.
 */
public class HTMLTextEditor extends JDialog {
	
	private static final int INITIAL_WIDTH_MIN = 500;
	private static final int INITIAL_WIDTH_MAX = 1200;
	private static final int INITIAL_HEIGHT_MIN = 325;
	private static final int INITIAL_HEIGHT_MAX = 500;
	private SimpleHTMLEditor htmlEditor;
	
	@Serial
	private static final long serialVersionUID = 7066844472238575449L;
	
    
    private boolean okButton = false;
	
    /**
     * Document to keep the original document foreground for undo
	 * and the new one if it is accepted after the edition.
	 * <p>
	 * TODO: Previous comment can be misleading, as it doesn't
	 *  clarify if it holds the new comment or the previous
	 *  comment, although the Ok button updates this, meaning
	 *  is likely to hold the new comment rather than the old one.
	 */
    private String commentText;
	
	/**
	 * HTMLTextEditor dialog constructor
	 *
	 * @param owner         The frame where the dialog belongs to
	 * @param updateComment the comment to update
	 */
	
	//TODO: owner is always null.
	public HTMLTextEditor(final Frame owner, final String updateComment) {
		super(owner);
        this.commentText = updateComment; //to be used for undo
        this.initialize(owner, updateComment);
	}

	/**
	 * Initializes the dialog
	 */
	private void initialize(Frame owner, final String updateComment) {
		//this.setLocation(owner.getLocation());
		this.setLocation(new Point(240, 250));
		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.setModalityType(Dialog.DEFAULT_MODALITY_TYPE);
		
		this.setVisible(false);
		this.setTitle(StringDatabase.getUniqueInstance().getString("HTMLTextEditor.Title.Text"));
		
        this.htmlEditor = new SimpleHTMLEditor(updateComment);

		this.setContentPane(this.createContentPane());
		this.setIconImage(OpenMarkovLogoIcon.getUniqueInstance().
				getOpenMarkovLogoIconImage16());
		this.pack();
		int initialWidth = this.getSize().width;
		initialWidth = Math.max(HTMLTextEditor.INITIAL_WIDTH_MIN, initialWidth);
		initialWidth = Math.min(HTMLTextEditor.INITIAL_WIDTH_MAX, initialWidth);
		int initialHeight = this.getSize().height;
		initialHeight = Math.max(HTMLTextEditor.INITIAL_HEIGHT_MIN, initialHeight);
		initialHeight = Math.min(HTMLTextEditor.INITIAL_HEIGHT_MAX, initialHeight);
		this.setSize(new Dimension(initialWidth, initialHeight));
		this.htmlEditor.focusOnEditor();
		this.setMinimumSize(this.htmlEditor.minimumDimensions());
	}

	/**
	 * This method initializes the contents of the panel.
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel createContentPane() {
        JPanel jContentPane = new JPanel();
		jContentPane.setLayout(new BorderLayout());
		jContentPane.add(this.htmlEditor, BorderLayout.CENTER);
		var confirmationPanel = new JPanel();
		confirmationPanel.setLayout(new FlowLayout());
		confirmationPanel.add(this.createOkButtonUI());
		confirmationPanel.add(this.createCancelButtonUI());
		jContentPane.add(confirmationPanel, BorderLayout.PAGE_END);
		return jContentPane;
	}

	/**
	 * This method initialises jButtonAcceptHTML
	 *
	 * @return javax.swing.JButton
	 */
	private JButton createOkButtonUI() {
		JButton jButtonAcceptHTML = new JButton(StringDatabase.getUniqueInstance().getString("HTMLTextEditor.jButtonAcceptHTML.Text"));
		jButtonAcceptHTML.addActionListener(e -> {
            //update the commentText
            this.commentText = this.htmlEditor.getHTMLContent();
            this.setVisible(false);
            this.okButton = true;
        });
		return jButtonAcceptHTML;
	}

	/**
	 * This method initialises jButtonCancelHTML
	 *
	 * @return javax.swing.JButton
	 */
	private JButton createCancelButtonUI() {
        JButton jButtonCancelHTML = new JButton(StringDatabase.getUniqueInstance().getString("HTMLTextEditor.jButtonCancelHTML.Text"));
		jButtonCancelHTML.addActionListener(e -> this.setVisible(false));
		return jButtonCancelHTML;
	}

	/**
     * Method to return the document foreground
	 *
	 * @return String with the Text
	 */
	public final String getCommentText() {
		return commentText;
	}



	/**
	 * Returns whether the user confirmed the dialog by pressing OK.
	 *
	 * @return {@code true} if the OK button was pressed
	 */
	public final boolean getOkButtonStatus() {
		return okButton;
	}
	
	
	
	
	/**
	 * ToolBar elements for the dialog
	 */
	public static final String TOOLBAR_OPENMARKOV_SINGLE = "CT|CP|PS|SP|UN|RE|SP|BL|IT|UD|SP|UC|SP|SR|SP|FO";

	
}
