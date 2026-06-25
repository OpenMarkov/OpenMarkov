/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog;

import org.openmarkov.gui.loader.element.ImageLoader;
import org.openmarkov.core.localize.StringDatabase;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serial;

/**
 * Class to show an About Box window for the OpenMarkov Project
 *
 * @author jlgozalo
 * @version 1.0 17/03/2009
 */
public class AboutBox extends JDialog implements ActionListener {
    // TODO to be consider to re-write as an standard dialog using the
    //  OpenMarkov dialog resource bundle
    /**
     * default id
     */
    @Serial
    private static final long serialVersionUID = -2926600957370532009L;
    private String version = "0.4.0-SNAPSHOT";
    private final BorderLayout borderLayoutAboutBox = new BorderLayout();
    private final JPanel jPanelAboutText = new JPanel();
    private final JPanel jPanelAboutButton = new JPanel();
    private ImageIcon lineSeparator;
    private final JLabel jLabelLogo = new JLabel();
    private final JLabel jLabelProduct = new JLabel();
    private final JLabel jLabelVersion = new JLabel();
    private final JLabel jLabelCopyright = new JLabel();
    private final JLabel jLabelCopyright2 = new JLabel();
    private final JLabel jLabelAuthors = new JLabel();
    private final JLabel jLabelLineSeparators = new JLabel();
    private final JLabel jLabelAdvertisement = new JLabel();
    private final JLabel jLabelTrademark = new JLabel();
    private final JButton jButtonOK = new JButton();
    private final GridLayout gridLayoutText = new GridLayout();
    private final FlowLayout flowLayoutButtons = new FlowLayout();
    /**
     * String database
     */
    private final StringDatabase stringDatabase = StringDatabase.getUniqueInstance();
    /**
     * AboutBox visual components
     */
    private AboutBox anAboutBox = null;
    
    /**
     * constructor on a open window
     */
    public AboutBox() {
        this(null);
    }
    
    /**
     * constructor on a parent JFrame
     *
     * @param parent the parent frame for centering the dialog, or {@code null}
     */
    public AboutBox(JFrame parent) {
        super(parent, "", true);
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.setTitle(stringDatabase.getString("AboutBox.Title.Text"));
        String product = stringDatabase.getString("AboutBox.Product.Text");
        version = stringDatabase.getString("AboutBox.Version.Text") + " " + version;
        String copyright = stringDatabase.getString("AboutBox.Copyright.Text");
        String copyright2 = stringDatabase.getString("AboutBox.Copyright.AllRightsReserved.Text");
        String authors = stringDatabase.getString("AboutBox.Authors.Text");
        String advertisement = stringDatabase.getString("AboutBox.Advertisement.Text");
        String trademark = stringDatabase.getString("AboutBox.Trademark.Text");
        String openMarkovLogoImage = stringDatabase.getString("AboutBox.OpenMarkovLogoImage.URL");
        String lineSeparatorImage = stringDatabase.getString("AboutBox.LineSeparatorImage.URL");
        
        // look for the images to show in the box
        ImageIcon openMarkovLogo = ImageLoader.load(openMarkovLogoImage);
        lineSeparator = ImageLoader.load(lineSeparatorImage);
        // put the title of the box
        setTitle(product);
        // mark the layout and the size for the About box
        getContentPane().setLayout(borderLayoutAboutBox);
        /**
         * size of the window and position
         */
        int height = openMarkovLogo.getIconHeight() + 200;
        int width = openMarkovLogo.getIconWidth();
        int x = getParent().getX();
        int y = getParent().getY();
        x = x + ((getParent().getWidth() - width) / 2);
        y = y + ((getParent().getHeight() - height) / 2);
        this.setBounds(x, y, width, height);
        // set the logo and add to the top of the box
        jLabelLogo.setHorizontalAlignment(SwingConstants.CENTER);
        jLabelLogo.setIcon(openMarkovLogo);
        jLabelLogo.setHorizontalTextPosition(SwingConstants.CENTER);
        jLabelLogo.setText(stringDatabase.getString("AboutBox.jLabelLogo.BlankSpace.Text"));
        this.getContentPane().add(jLabelLogo, BorderLayout.NORTH);
        // set the items and add to the panel and then to the box
        setTextInLabelAligned(jLabelProduct, product, SwingConstants.CENTER);
        jLabelProduct.setHorizontalAlignment(SwingConstants.CENTER);
        jLabelProduct.setHorizontalTextPosition(SwingConstants.LEADING);
        jLabelProduct.setFont(new Font("", Font.PLAIN, 16));
        setTextInLabelAligned(jLabelVersion, version, SwingConstants.CENTER);
        setTextInLabelAligned(jLabelCopyright, copyright, SwingConstants.CENTER);
        setTextInLabelAligned(jLabelCopyright2, copyright2, SwingConstants.CENTER);
        setTextInLabelAligned(jLabelLineSeparators, stringDatabase.getString("AboutBox.jLabelLogo.BlankSpace.Text"),
                              SwingConstants.CENTER);
        jLabelLineSeparators.setHorizontalAlignment(SwingConstants.CENTER);
        jLabelLineSeparators.setHorizontalTextPosition(SwingConstants.CENTER);
        jLabelLineSeparators.setIcon(lineSeparator);
        setTextInLabelAligned(jLabelAdvertisement, advertisement, SwingConstants.LEFT);
        setTextInLabelAligned(jLabelTrademark, trademark, SwingConstants.LEFT);
        jPanelAboutText.setLayout(gridLayoutText);
        gridLayoutText.setColumns(1);
        gridLayoutText.setRows(8);
        jPanelAboutText.add(jLabelProduct);
        jPanelAboutText.add(jLabelVersion);
        jPanelAboutText.add(jLabelCopyright);
        jPanelAboutText.add(jLabelCopyright2);
        jPanelAboutText.add(jLabelAuthors);
        jPanelAboutText.add(jLabelLineSeparators);
        jPanelAboutText.add(jLabelAdvertisement);
        jPanelAboutText.add(jLabelTrademark);
        this.getContentPane().add(jPanelAboutText, BorderLayout.CENTER);
        // set the OK button and action associated
        jButtonOK.setText(stringDatabase.getString("AboutBox.OK.Text"));
        jButtonOK.addActionListener(this);
        jPanelAboutButton.setLayout(flowLayoutButtons);
        jPanelAboutButton.add(jButtonOK);
        this.getContentPane().add(jPanelAboutButton, BorderLayout.SOUTH);
        
        this.setVisible(true);
        anAboutBox = this;
    }
    
    /**
     * align a foreground in a label with a center alignment
     */
    private static void setTextInLabelAligned(JLabel theLabel, String theText, int alignment) {
        theLabel.setHorizontalAlignment(alignment);
        theLabel.setText(theText);
    }
    
    /**
     * singleton for AboutBox
     *
     * @return anAboutBox dialog
     */
    public AboutBox getUniqueInstance() {
        return getUniqueInstance(null);
    }
    
    /**
     * singleton for AboutBox
     *
     * @param parent the parent for the AboutBox frame
     * @return anAboutBox dialog
     */
    private AboutBox getUniqueInstance(JFrame parent) {
        if (anAboutBox == null) { // singleton
            new AboutBox(parent);
        } else { // it is already created and not visible
            this.setVisible(true);
        }
        return anAboutBox;
    }
    
    /**
     * Close the dialog on a button event. Really, this action only hides the
     * dialog to be reused if required.
     *
     * @param actionEvent ActionEvent
     */
    @Override public void actionPerformed(ActionEvent actionEvent) {
        if (actionEvent.getActionCommand().equals(stringDatabase.getString("AboutBox.OK.Text"))) {
            this.setVisible(false);
        }
    }
}
