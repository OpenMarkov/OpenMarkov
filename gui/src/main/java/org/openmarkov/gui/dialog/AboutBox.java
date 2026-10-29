/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog;

import org.openmarkov.core.exception.UnreachableException;
import org.openmarkov.gui.loader.element.ImageLoader;
import org.openmarkov.core.localize.StringDatabase;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serial;
import java.util.Objects;
import java.util.Properties;

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
    /**
     * Version and date of the build, written by Maven
     */
    private static final String BUILD_PROPERTIES = "/gui/build.properties";
    private final BorderLayout borderLayoutAboutBox = new BorderLayout();
    private final JPanel jPanelAboutText = new JPanel();
    private final JPanel jPanelAboutButton = new JPanel();
    private final JLabel jLabelLogo = new JLabel();
    private final JLabel jLabelProduct = new JLabel();
    private final JLabel jLabelVersion = new JLabel();
    private final JLabel jLabelCopyright = new JLabel();
    private final JLabel jLabelLicense = new JLabel();
    private final JLabel jLabelAdvertisement = new JLabel();
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
        Properties build = readBuildProperties();
        String date = build.getProperty("date");
        String product = stringDatabase.getString("AboutBox.Product.Text");
        String version = stringDatabase.getFormattedString("AboutBox.Version.Text", build.getProperty("version"), date);
        String copyright = stringDatabase.getFormattedString("AboutBox.Copyright.Text", date.substring(0, 4));
        String license = stringDatabase.getString("AboutBox.License.Text");
        String advertisement = stringDatabase.getString("AboutBox.Advertisement.Text");
        String openMarkovLogoImage = stringDatabase.getString("AboutBox.OpenMarkovLogoImage.URL");

        getContentPane().setLayout(borderLayoutAboutBox);
        // the splash screen at half its size
        Image splash = ImageLoader.load(AboutBox.class.getResource(openMarkovLogoImage),
                                        ImageLoader.ImageOptions.SkipAutoScale).getImage();
        Image logo = splash.getScaledInstance(splash.getWidth(null) / 2, splash.getHeight(null) / 2,
                                              Image.SCALE_SMOOTH);
        jLabelLogo.setHorizontalAlignment(SwingConstants.CENTER);
        jLabelLogo.setIcon(ImageLoader.createImage(logo, ImageLoader.ImageOptions.SkipAutoScale));
        this.getContentPane().add(jLabelLogo, BorderLayout.NORTH);
        // set the items and add to the panel and then to the box
        setTextInLabelAligned(jLabelProduct, product, SwingConstants.CENTER);
        jLabelProduct.setFont(new Font("", Font.PLAIN, 16));
        setTextInLabelAligned(jLabelVersion, version, SwingConstants.CENTER);
        setTextInLabelAligned(jLabelCopyright, copyright, SwingConstants.CENTER);
        setTextInLabelAligned(jLabelLicense, license, SwingConstants.CENTER);
        setTextInLabelAligned(jLabelAdvertisement, advertisement, SwingConstants.CENTER);
        jPanelAboutText.setLayout(gridLayoutText);
        jPanelAboutText.setBorder(BorderFactory.createEmptyBorder(10, 20, 0, 20));
        gridLayoutText.setColumns(1);
        gridLayoutText.setRows(5);
        jPanelAboutText.add(jLabelProduct);
        jPanelAboutText.add(jLabelVersion);
        jPanelAboutText.add(jLabelCopyright);
        jPanelAboutText.add(jLabelLicense);
        jPanelAboutText.add(jLabelAdvertisement);
        // the authors and the disclaimer, below the lines of the same height, as wide as the logo without the margins
        int paragraphWidth = splash.getWidth(null) / 2 - 40;
        JPanel jPanelDisclaimer = new JPanel(new BorderLayout(0, 10));
        jPanelDisclaimer.setBorder(BorderFactory.createEmptyBorder(10, 20, 0, 20));
        jPanelDisclaimer.add(authorsLabel(paragraphWidth), BorderLayout.NORTH);
        jPanelDisclaimer.add(Disclaimer.labelWithTitle(paragraphWidth), BorderLayout.CENTER);
        JPanel jPanelAboutTextAndDisclaimer = new JPanel(new BorderLayout());
        jPanelAboutTextAndDisclaimer.add(jPanelAboutText, BorderLayout.CENTER);
        jPanelAboutTextAndDisclaimer.add(jPanelDisclaimer, BorderLayout.SOUTH);
        this.getContentPane().add(jPanelAboutTextAndDisclaimer, BorderLayout.CENTER);
        // set the OK button and action associated
        jButtonOK.setText(stringDatabase.getString("AboutBox.OK.Text"));
        jButtonOK.addActionListener(this);
        jPanelAboutButton.setLayout(flowLayoutButtons);
        jPanelAboutButton.add(jButtonOK);
        this.getContentPane().add(jPanelAboutButton, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(parent);
        this.setVisible(true);
        anAboutBox = this;
    }

    /**
     * The authors of OpenMarkov, opened by their title in bold, as a centred paragraph of the given width, in pixels,
     * whose lines do not break inside a name.
     */
    static JLabel authorsLabel(int width) {
        StringDatabase strings = StringDatabase.getUniqueInstance();
        String names = strings.getString("AboutBox.Authors.Text").replace(" ", "&nbsp;").replace(",&nbsp;", ", ");
        return Disclaimer.wrapped("<html><div style='text-align: center'><b>" + strings.getString("AboutBox.Authors.Title")
                + ".</b> " + names + "</div></html>", width);
    }

    /**
     * The version of OpenMarkov ({@code version}) and the date of the build ({@code date}, yyyy-MM-dd).
     */
    static Properties readBuildProperties() {
        var properties = new Properties();
        try (InputStream input = AboutBox.class.getResourceAsStream(BUILD_PROPERTIES)) {
            properties.load(Objects.requireNonNull(input, BUILD_PROPERTIES));
        } catch (IOException e) {
            throw new UnreachableException(e);
        }
        return properties;
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
