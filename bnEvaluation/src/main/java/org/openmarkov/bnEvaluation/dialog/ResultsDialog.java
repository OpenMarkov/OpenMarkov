/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.bnEvaluation.dialog;

import org.apache.commons.io.FilenameUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openmarkov.bnEvaluation.SplitSet;
import org.openmarkov.bnEvaluation.export.ExcelExporter;
import org.openmarkov.bnEvaluation.measures.MeasureMatrix;
import org.openmarkov.bnEvaluation.measures.MeasuresSet;
import org.openmarkov.bnEvaluation.view.OverwriteAwareFileChooser;
import org.openmarkov.bnEvaluation.view.ScoresTableStyler;
import org.openmarkov.core.exception.UnrecoverableException;
import org.openmarkov.gui.configuration.LocalPreferences;
import org.openmarkov.gui.dialog.common.BottomPanelButtonDialog;
import org.openmarkov.gui.dialog.common.DialogBase;
import org.openmarkov.gui.util.JTableGeneration;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * This class is the frame in which the results are presented.
 *
 * @author evillar
 * @version 1.0 evillar
 **/

public final class ResultsDialog extends BottomPanelButtonDialog {
    
    private MeasuresSet measures = null;
    private SplitSet splitSet = null;
    private final ResultKind resultKind;
    
    enum ResultKind{
        Measures,
        SplitSet
    }
    
    public ResultsDialog(Frame owner, MeasuresSet measures) {
        super(owner);
        this.setTitle("Evaluation results");
        this.setMinimumSize(new Dimension(600, 400));
        this.setLocationRelativeTo(owner);
        this.measures = measures;
        resultKind = ResultKind.Measures;
        initialize();
    }
    
    public ResultsDialog(Frame owner, SplitSet splitSet) {
        super(owner);
        this.setTitle("Split Dataset Info");
        this.setMinimumSize(new Dimension(600, 400));
        this.setLocationRelativeTo(owner);
        this.splitSet = splitSet;
        resultKind = ResultKind.SplitSet;
        initialize();
    }
    
    private void initialize() {
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        JPanel mainPanel = new JPanel();
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        // add components with a BoxLayout
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
        this.add(mainPanel);
        mainPanel.add(getResultsPanel());
        mainPanel.add(getMetaInformationPanel());
        JButton exportButton = new JButton("Export to Excel file");
        JButton closeButton = DialogBase.generateGenericCancelButton();
        closeButton.setText("Close");
        exportButton.addActionListener(e -> {
            try {
                exportButtonActionPerformed();
            } catch (IOException ex) {
                throw new UnrecoverableException(ex);
            }
        });
        addButtonToButtonsPanel(exportButton);
        setCancelButton(closeButton);
        this.pack();
    }
    
    
    /**
     * This method returns the results panel
     *
     * @return JPanel
     */
    private JPanel getResultsPanel() {
        JPanel resultsPanel = new JPanel();
        String title = switch (resultKind) {
            case Measures -> "Evaluation";
            case SplitSet -> "Split Set";
        };
        resultsPanel.setBorder(BorderFactory.createTitledBorder(title + " results:"));
        resultsPanel.setLayout(new BoxLayout(resultsPanel, BoxLayout.PAGE_AXIS));
        resultsPanel.add(switch (resultKind){
            case Measures -> generateTableResultsMeasurePanel();
            case SplitSet -> generateTableResultsDatasetSplitPanel();
        });
        return resultsPanel;
    }
    
    /**
     * This method returns a JPanel with the metainformation
     *
     * @return JPanel
     */
    private JPanel getMetaInformationPanel() {
        JPanel informationPanel = new JPanel();
        informationPanel.setPreferredSize(new Dimension(600, 100));
        informationPanel.setBorder(BorderFactory.createTitledBorder("Information"));
        // informationPanel components
        JTextPane textMetaInformation = new JTextPane();
        textMetaInformation.setText(switch (resultKind){
            case Measures -> measures.getMeasureInformation();
            case SplitSet -> splitSet.getTitle();
        });
        JScrollPane jScrollPaneMetaInf = new JScrollPane(textMetaInformation);
        jScrollPaneMetaInf.setAutoscrolls(true);
        jScrollPaneMetaInf.setPreferredSize(new Dimension(550, 50));
        // group layout
        GroupLayout layout = new GroupLayout(informationPanel);
        informationPanel.setLayout(layout);
        layout.setHorizontalGroup(layout.createSequentialGroup()
                                        .addComponent(jScrollPaneMetaInf)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED,
                                                         GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(layout.createParallelGroup()
                                      .addComponent(jScrollPaneMetaInf));
        return informationPanel;
    }
    
    /**
     * This method returns a panel that contains a table
     * with the results returned from dividing a set.
     *
     * @return JPanel
     */
    private JPanel generateTableResultsDatasetSplitPanel() {
        JPanel jPanel = new JPanel();
        jPanel.setLayout(new BoxLayout(jPanel, BoxLayout.PAGE_AXIS));
        // components for the panel are created
        JTable jTableSplit = splitSet.toTable();
        jPanel.add(fittingScrollPane(jTableSplit));
        return jPanel;
    }
    
    /**
     * This method returns a panel that contains the tables with the results
     * returned when a network is evaluated
     *
     * @return JPanel
     */
    private JPanel generateTableResultsMeasurePanel() {
        JPanel jPanel = new JPanel();
        jPanel.setLayout(new BoxLayout(jPanel, BoxLayout.PAGE_AXIS));
        // JTabbedPane with multiples tabbeds
        JTabbedPane tabbedPane = new JTabbedPane();
        jPanel.add(tabbedPane);
        // matrix confusion and indicators jtables
        MeasureMatrix measureMatrix = measures.getMeasureMatrix();
        
        if (measureMatrix != null) {
            JPanel tabMatrix = new JPanel();
            tabMatrix.setLayout(new BoxLayout(tabMatrix, BoxLayout.PAGE_AXIS));
            tabbedPane.addTab("Confusion matrix", tabMatrix);
            JTable jTablaMatrix = measureMatrix.matrixToTable();
            tabMatrix.add(fittingScrollPane(jTablaMatrix));

            JPanel tabIndicators = new JPanel();
            tabIndicators.setLayout(new BoxLayout(tabIndicators, BoxLayout.PAGE_AXIS));
            tabbedPane.addTab("Indicators", tabIndicators);
            JTable jTableIndicators = measureMatrix.indicatorsToTable();
            addIndicatorHeaderTooltips(jTableIndicators);
            tabIndicators.add(fittingScrollPane(jTableIndicators));
        }
        // scores jtables
        if (measures.getNumMeasuresValue() > 0) {
            JPanel tabScores = new JPanel();
            tabScores.setLayout(new BoxLayout(tabScores, BoxLayout.PAGE_AXIS));
            tabScores.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
            tabbedPane.addTab("Scores", tabScores);
            JTable jTableScores = measures.scoresToTable();
            ScoresTableStyler.style(jTableScores, ResultsDialog::scoreRowTooltip);
            tabScores.add(fittingScrollPane(jTableScores));
        }
        if (measureMatrix != null) {
            if (measureMatrix.getShowIndividualProb()) {
                JPanel tabIndvProb = new JPanel();
                tabIndvProb.setLayout(new BoxLayout(tabIndvProb, BoxLayout.PAGE_AXIS));
                tabbedPane.addTab("Probabilities", tabIndvProb);
                JTable jTableIndivProb = measureMatrix.probToTable();
                tabIndvProb.add(fittingScrollPane(jTableIndivProb));
            }
        }
        return jPanel;
    }
    
    /**
     * This method manages the export of results, either to
     * an Excel file or by copying to the clipboard.
     */
    private void exportButtonActionPerformed() throws IOException {
        // Name for the spreadsheet created.
        Workbook workbook = switch (resultKind) {
            case Measures -> new ExcelExporter(measures).export();
            case SplitSet -> {
                //splitSet.toExcel()
                Workbook resWorkbook = new XSSFWorkbook();
                JTableGeneration.saveTableToSheet(splitSet.toTable(), resWorkbook.createSheet("Dataset Split info"));
                yield resWorkbook;
            }
        };
        
        OverwriteAwareFileChooser omFileChooser = new OverwriteAwareFileChooser();
        omFileChooser.setCurrentDirectory(LocalPreferences.LATEST_OPEN_DIRECTORY.get());
        // Choose where to save the file
        String nameDefect = switch (resultKind){
            case Measures -> "EvaluationNet";
            case SplitSet -> "SplitSet";
        };
        omFileChooser.setSelectedFile(new File(FilenameUtils.removeExtension(nameDefect + ".xlsx")));
        omFileChooser.setDialogTitle("SaveDialog.Title");
        int optionChosen = omFileChooser.showSaveDialog(this);
        if (optionChosen != JFileChooser.APPROVE_OPTION) {
            return;
        }
        String fileName = omFileChooser.getSelectedFile().getAbsolutePath();
        if (!fileName.endsWith(".xlsx")) {
            fileName += ".xlsx";
        }
        FileOutputStream outputStream = new FileOutputStream(fileName);
        workbook.write(outputStream);
        workbook.close();
        dispose(); // Closes the dialog
    }
    
    /**
     * Returns a JScrollPane sized to fit the table content, capped at 350 px height.
     */
    private static JScrollPane fittingScrollPane(JTable table) {
        JScrollPane sp = new JScrollPane(table);
        sp.setAutoscrolls(true);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(table.getBackground());
        JTableHeader header = table.getTableHeader();
        int headerH = (header != null && header.isVisible()) ? header.getPreferredSize().height : 0;
        int contentH = table.getRowCount() * table.getRowHeight() + headerH;
        sp.setPreferredSize(new Dimension(500, Math.min(contentH + 4, 350)));
        return sp;
    }


    /**
     * Installs tooltips on the column headers of the Indicators table.
     */
    private static void addIndicatorHeaderTooltips(JTable table) {
        TableCellRenderer defaultRenderer = table.getTableHeader().getDefaultRenderer();
        table.getTableHeader().setDefaultRenderer((t, value, isSelected, hasFocus, row, col) -> {
            Component c = defaultRenderer.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
            if (c instanceof JComponent jc) {
                jc.setToolTipText(indicatorHeaderTooltip(value != null ? value.toString() : ""));
            }
            return c;
        });
    }

    private static String indicatorHeaderTooltip(String header) {
        return switch (header) {
            case "TP rate"   -> "<html>True Positive rate (sensitivity / recall):<br>"
                                + "proportion of actual positives correctly classified</html>";
            case "FP rate"   -> "<html>False Positive rate (1 − specificity):<br>"
                                + "proportion of actual negatives incorrectly classified as positive</html>";
            case "F Measure" -> "<html>F-measure (F1-score):<br>"
                                + "harmonic mean of Precision and Recall</html>";
            default          -> null;
        };
    }

    private static String scoreRowTooltip(String label) {
        if (label == null) return null;
        if (label.contains("LOGLIKELIHOOD") && label.contains("score"))
            return "<html>Log-Likelihood score:<br>"
                   + "logarithm of the probability of observing the dataset given the model</html>";
        if (label.contains("LOGLIKELIHOOD") && label.contains("Loss"))
            return "<html>Log-Likelihood Loss:<br>"
                   + "average negative log-likelihood per case (lower is better)</html>";
        if (label.startsWith("BAYES"))
            return "<html>Bayesian score:<br>"
                   + "marginal likelihood of the network structure under a Bayesian Dirichlet prior</html>";
        if (label.startsWith("AIC"))
            return "<html>AIC (Akaike Information Criterion):<br>"
                   + "log-likelihood penalised by the number of free parameters</html>";
        if (label.startsWith("ENTROPY"))
            return "<html>Entropy score:<br>"
                   + "total conditional entropy of the network structure</html>";
        if (label.startsWith("BDE"))
            return "<html>BDe (Bayesian Dirichlet equivalent):<br>"
                   + "likelihood score with an equivalent sample size prior</html>";
        if (label.startsWith("K2"))
            return "<html>K2:<br>"
                   + "scoring function from the K2 structure-learning algorithm (Cooper & Herskovits, 1992)</html>";
        if (label.startsWith("MDL"))
            return "<html>MDL (Minimum Description Length):<br>"
                   + "information-theoretic measure balancing model fit and complexity</html>";
        return null;
    }

}