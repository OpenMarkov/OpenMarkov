package org.openmarkov.gui.desnets;

import org.openmarkov.core.inference.MonteCarloOptions;
import org.openmarkov.core.model.network.EqualCriterion;
import org.openmarkov.gui.window.OMTabbedPane;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

/**
 * Presents DES simulation results from each decision criteria and gives the option to save them in a .csv file
 *
 * @author cmyago - 14/01/2023
 * @version 1
 */
public class DESResultsWindow {
    public static final Dimension DIMENSION = new Dimension(1200, 400);
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.####");

    /**
     * Presents the DES simulation results in a table
     *
     * @param simulationResults DES simulation results
     * @param usingDiscount     whether each criterion has a discount or not
     */
    public static void presentResults(double simulationTime, SimulationSummaryResults simulationResults, boolean[] usingDiscount, JPanel psaPanel) {

        JPanel resultsPanel = new JPanel(new BorderLayout());

        JTextPane textPane = new JTextPane();
        textPane.setContentType("text/html");
        textPane.setEditable(false);
        textPane.setText(generateTable(simulationResults, usingDiscount));

        //FIXME use StringDatabase
        JPanel btnPanel = new JPanel();
        JButton btnExportCsv = new JButton("Export as CSV");
        JButton btnCopyHtml = new JButton("Copy to clipboard as HTML");
        btnPanel.add(btnExportCsv);
        btnPanel.add(btnCopyHtml);
        btnExportCsv.addActionListener(evt -> saveCsv(simulationResults));
        btnCopyHtml.addActionListener(evt ->
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
                        new StringSelection(textPane.getText()),
                        null
                )
        );

        JScrollPane scrollPane = new JScrollPane(textPane);
        scrollPane.setPreferredSize(DIMENSION);

        resultsPanel.add(scrollPane, BorderLayout.CENTER);
        resultsPanel.add(btnPanel, BorderLayout.SOUTH);

        Component dialogContent = resultsPanel;

        if (psaPanel != null) {
            OMTabbedPane tabbedPane = new OMTabbedPane();
            tabbedPane.addTab("Simulation Results", resultsPanel);
            tabbedPane.addTab("Probabilistic Sensitivity Analysis", psaPanel);
            dialogContent = tabbedPane;
        }

        JOptionPane.showMessageDialog(
                null,
                dialogContent,
                String.format("Finished in %.2f seconds", simulationTime),
                JOptionPane.PLAIN_MESSAGE
        );

    }

    private static void saveCsv(SimulationSummaryResults simulationResults) {
        String csv = csv(simulationResults);

        File exportDir = new File(MonteCarloOptions.DESNET_RESULTS_DIRECTORY, MonteCarloOptions.EXPORT_DIRECTORY);
        if (!exportDir.exists()) exportDir.mkdir();
        JFileChooser fileChooser = new JFileChooser(exportDir);
        fileChooser.setDialogTitle("Choose CSV file for output");
        fileChooser.setFileFilter(new FileNameExtensionFilter("CSV file", "csv"));
        if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            try {
                String chosenFile = fileChooser.getSelectedFile().getAbsolutePath();
                if (!chosenFile.toLowerCase().endsWith(".csv")) chosenFile += ".csv";
                Files.write(Paths.get(chosenFile), csv.getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Could not save CSV: " + e, "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /** The options of the decision, in the order of the results. */
    private static List<String> options(SimulationSummaryResults simulationResults) {
        List<HashMap<String, CriteriaValues>> seriesList = simulationResults.getSeriesList();
        return seriesList.isEmpty() ? List.of() : List.copyOf(seriesList.get(0).keySet());
    }

    /** The results as comma-separated values: a row of titles and then a row for each series. */
    static String csv(SimulationSummaryResults simulationResults) {
        List<String> interventions = options(simulationResults);
        List<EqualCriterion> criteria = simulationResults.getCriteria();
        StringBuilder sb = new StringBuilder();
        sb.append("SERIES");
        // TODO: skip discounted values when there is no discount.
        interventions.forEach(intervention -> {
            criteria.forEach(criterion -> {
                sb.append(',').append(csvField("Mean " + criterion.getCriterionName() + " for " + intervention));
                sb.append(',').append(csvField("Disc. Mean " + criterion.getCriterionName() + " for " + intervention));
            });
        });
        sb.append("\n");

        List<HashMap<String, CriteriaValues>> seriesList = simulationResults.getSeriesList();
        for (int seriesNum = 0; seriesNum < seriesList.size(); seriesNum++) {
            sb.append(seriesNum);
            for (String intervention : interventions) {
                for (EqualCriterion criterion : criteria) {
                    SimulationResults values = seriesList.get(seriesNum).get(intervention).getSimulationResults(criterion);
                    sb.append(',').append(values.getMean()).append(',').append(values.getDiscountedMean());
                }
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    /** The field as CSV writes it: in double quotes, with its own doubled, if it has a comma, a quote or a line break. */
    private static String csvField(String field) {
        if (field.contains(",") || field.contains("\"") || field.contains("\n") || field.contains("\r"))
            return '"' + field.replace("\"", "\"\"") + '"';
        return field;
    }

    static String generateTable(SimulationSummaryResults simulationResults, boolean[] usingDiscount) {
        try {
            int discountedCount = 0;
            for (boolean hasDiscount : usingDiscount) if (hasDiscount) ++discountedCount;

            List<String> interventions = options(simulationResults);
            List<EqualCriterion> criteria = simulationResults.getCriteria();
            StringBuilder sb = new StringBuilder(
                    String.format("<html><style>%s</style><table><thead><tr><th rowspan=\"3\">Series #<br><small>(%d simulations each)</small></th>",
                            loadResourceAsText("resultStyle.css"),
                            simulationResults.getMonteCarloOptions().getNumSimulations()
                    )
            );

            for (String intervention : interventions) {
                sb.append("<th colspan=\"")
                        .append(usingDiscount.length + discountedCount)
                        .append("\">")
                        .append(simulationResults.getDecisionVariable().getName())
                        .append(": ")
                        .append(intervention)
                        .append("</th>");
            }
            sb.append("</tr><tr>");
            for (int intervNum = 0; intervNum < interventions.size(); intervNum++) {
                for (int criterionNum = 0; criterionNum < usingDiscount.length; criterionNum++) {
                    String criterionName = criteria.get(criterionNum).getCriterionName();
                    if (usingDiscount[criterionNum])
                        sb.append("<th colspan=\"2\">").append(criterionName);
                    else
                        sb.append("<th colspan=\"1\" rowspan=\"2\">").append(criterionName); //.append(" Mean");
                    sb.append("</th>");
                }
            }
            sb.append("</tr><tr>");

            int counter = Math.max(0, discountedCount);
            counter *= Math.max(0, interventions.size());
            while (--counter >= 0) sb.append("<th>Mean</th><th>Disc. Mean</th>");
            sb.append("</tr></thead><tbody>");
            boolean evenRow = false;
            List<HashMap<String, CriteriaValues>> seriesList = simulationResults.getSeriesList();
            for (int seriesNum = 0; seriesNum < seriesList.size(); seriesNum++) {
                String td = String.format("<td %s>", evenRow ? "class=even" : "");
                evenRow = !evenRow;
                sb.append("<tr>");

                sb.append(td).append(seriesNum).append("</td>");
                for (String intervention : interventions) {
                    for (int criterionNum = 0; criterionNum < usingDiscount.length; criterionNum++) {
                        SimulationResults values = seriesList.get(seriesNum).get(intervention)
                                                             .getSimulationResults(criteria.get(criterionNum));
                        sb.append(td)
                                .append(DECIMAL_FORMAT.format(values.getMean()))
                                .append("</td>");
                        if (usingDiscount[criterionNum])
                            sb.append(td)
                                    .append(DECIMAL_FORMAT.format(values.getDiscountedMean()))
                                    .append("</td>");
                    }
                }

                sb.append("</tr>");
            }
            sb.append("</tbody></table></html>");
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "Could not generate results table: " + e;
        }
    }

    private static String loadResourceAsText(String resourceFileName) {
        try (InputStream is = DESResultsWindow.class.getClassLoader().getResourceAsStream(resourceFileName)) {
            return new Scanner(is, StandardCharsets.UTF_8.name()).useDelimiter("\\A").next();
        } catch (Exception e) {
            return e.toString();
        }
    }

}

