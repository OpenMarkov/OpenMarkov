package org.openmarkov.inference.DES;

import org.openmarkov.core.inference.MonteCarloOptions;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
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

    private static List<String> interventions;
    private static List<String> criteria;

    /**
     * Presents the DES simulation results in a table
     *
     * @param simulationResults    DES simulation results
     * @param decisionVariableName name of the decision variable
     * @param interventionsCount   number of interventions
     * @param usingDiscount        whether each criterion has a discount or not
     */
    public static void presentResults(int numSims, double simulationTime, String simulationResults, String decisionVariableName, int interventionsCount, boolean[] usingDiscount, JPanel psaPanel) {

        JPanel resultsPanel = new JPanel(new BorderLayout());

        JTextPane textPane = new JTextPane();
        textPane.setContentType("text/html");
        textPane.setEditable(false);
        textPane.setText(generateTable(numSims, simulationResults, decisionVariableName, interventionsCount, usingDiscount));

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
            JTabbedPane tabbedPane = new JTabbedPane();
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

    private static void saveCsv(String simulationResults) {
        Scanner scanner = new Scanner(simulationResults);
        StringBuilder sb = new StringBuilder();
        sb.append("SERIES;");
        // TODO: skip discounted values when there is no discount.
        interventions.forEach(intervention -> {
            criteria.forEach(criterion -> {
                sb.append("Mean ")
                        .append(criterion)
                        .append(" for ")
                        .append(intervention)
                        .append(",");
                sb.append("Disc. Mean ")
                        .append(criterion)
                        .append(" for ")
                        .append(intervention)
                        .append(",");
            });
        });
        sb.append("\n");

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            line = line.replaceAll(";ICER.+", "")
                    .replaceAll("[a-zA-Z].*?;", "")
                    .replaceAll(",", ".")
                    .replaceAll(";+", ";")
                    .replaceAll(";", ",");
            sb.append(line).append("\n");
        }

        File exportDir = new File(MonteCarloOptions.DESNET_RESULTS_DIRECTORY, MonteCarloOptions.EXPORT_DIRECTORY);
        if (!exportDir.exists()) exportDir.mkdir();
        JFileChooser fileChooser = new JFileChooser(exportDir);
        fileChooser.setDialogTitle("Choose CSV file for output");
        fileChooser.setFileFilter(new FileNameExtensionFilter("CSV file", "csv"));
        if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            try {
                String chosenFile = fileChooser.getSelectedFile().getAbsolutePath();
                if (!chosenFile.toLowerCase().endsWith(".csv")) chosenFile += ".csv";
                Files.write(Paths.get(chosenFile), sb.toString().getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Could not save CSV: " + e, "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static String generateTable(int numSims, String simulationResults, String decisionVariableName, int interventionsCount, boolean[] usingDiscount) {
        try {
            int discountedCount = 0;
            for (boolean hasDiscount : usingDiscount) if (hasDiscount) ++discountedCount;

            Scanner scanner = new Scanner(simulationResults);
            StringBuilder sb = new StringBuilder(
                    String.format("<html><style>%s</style><table><thead><tr><th rowspan=\"3\">Series #<br><small>(%d simulations each)</small></th>",
                            loadResourceAsText("resultStyle.css"),
                            numSims
                    )
            );

            interventions = new ArrayList<>();
            criteria = new ArrayList<>();

            String line = scanner.nextLine();
            String[] parts = line.split(";");
            int index = 2;
            for (int intervNum = 0; intervNum < interventionsCount; intervNum++) {
                interventions.add(parts[index]);
                sb.append("<th colspan=\"")
                        .append(usingDiscount.length + discountedCount)
                        .append("\">")
                        .append(decisionVariableName)
                        .append(": ")
                        .append(parts[index])
                        .append("</th>");
                index += usingDiscount.length * 5 + 2;
            }
            sb.append("</tr><tr>");
            for (int intervNum = 0; intervNum < interventionsCount; intervNum++) {
                index = 3;
                for (boolean hasDiscount : usingDiscount) {
                    if (intervNum == 0) criteria.add(parts[index]);
                    if (hasDiscount)
                        sb.append("<th colspan=\"2\">").append(parts[index]);
                    else
                        sb.append("<th colspan=\"1\" rowspan=\"2\">").append(parts[index]); //.append(" Mean");
                    sb.append("</th>");
                    index += 5;
                }
            }
            sb.append("</tr><tr>");

            int counter = Math.max(0, discountedCount);
            counter *= Math.max(0, interventionsCount);
            while (--counter >= 0) sb.append("<th>Mean</th><th>Disc. Mean</th>");
            sb.append("</tr></thead><tbody>");
            boolean evenRow = false;
            while (true) {
                String td = String.format("<td %s>", evenRow ? "class=even" : "");
                evenRow = !evenRow;
                sb.append("<tr>");

                parts = line.split(";");
                sb.append(td).append(parts[1]).append("</td>");
                index = 5;
                for (int intervNum = 0; intervNum < interventionsCount; intervNum++) {
                    for (boolean hasDiscount : usingDiscount) {
                        sb.append(td)
                                .append(formatDouble(parts[index]));
                        index += 2;
                        sb.append("</td>");
                        if (hasDiscount)
                            sb.append(td)
                                    .append(formatDouble(parts[index]))
                                    .append("</td>");
                        index += 3;
                    }
                    index += 2;
                }

                sb.append("</tr>");
                if (scanner.hasNextLine()) line = scanner.nextLine();
                else break;
            }
            sb.append("</tbody></table></html>");
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "Could not generate results table: " + e;
        }
    }

    private static String formatDouble(String string) {
        try {
            return DECIMAL_FORMAT.format(Double.parseDouble(string));
        } catch (Exception e) {
            return string;
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

