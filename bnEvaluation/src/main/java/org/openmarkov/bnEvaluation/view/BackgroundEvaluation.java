/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.bnEvaluation.view;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

/**
 * Runs a heavy computation off the event-dispatch thread under a modal progress dialog with a
 * progress bar and a Cancel button, so the interface stays responsive instead of freezing (the
 * concern addressed by phase F4). The result is delivered back on the event-dispatch thread; errors
 * are shown as a message and cancellation is silent.
 *
 * <p>The progress dialog is modal, so the calling code blocks until it closes, but the event-dispatch
 * thread keeps pumping events meanwhile (Swing's modal event loop): the bar animates, Cancel works
 * and other windows repaint while the {@link SwingWorker} does the work on a background thread.</p>
 *
 * @author Manuel Arias
 */
public final class BackgroundEvaluation {

    /** The work to run off the event-dispatch thread. */
    @FunctionalInterface
    public interface Work<T> {
        /**
         * @param progress handle to report progress and to check for cancellation
         * @return the computed result
         * @throws Exception any failure; it is surfaced to the user as an error message
         */
        T run(Progress progress) throws Exception;
    }

    /** Handle the work uses to report progress and to check whether the user asked to cancel. */
    public interface Progress {
        /**
         * Reports determinate progress. A {@code total} of zero or less leaves the bar indeterminate.
         *
         * @param completed units of work done so far
         * @param total     total units of work
         */
        void report(int completed, int total);

        /** @return whether the user asked to cancel; long loops should check it and stop. */
        boolean isCancelled();
    }

    private BackgroundEvaluation() {
    }

    /**
     * Runs {@code work} in the background under a modal progress dialog owned by {@code owner}. On
     * success {@code onSuccess} is invoked on the event-dispatch thread with the result; on failure a
     * message dialog is shown; on cancellation nothing further happens.
     *
     * @param owner     window that owns the modal progress dialog
     * @param title     progress dialog title (names the running task)
     * @param work      the computation to run off the event-dispatch thread
     * @param onSuccess consumer of the result, run on the event-dispatch thread
     * @param <T>       type of the computed result
     */
    public static <T> void run(Window owner, String title, Work<T> work, Consumer<T> onSuccess) {
        JDialog dialog = new JDialog(owner, title, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setIndeterminate(true);
        progressBar.setStringPainted(true);
        JButton cancelButton = new JButton("Cancel");

        JPanel content = new JPanel(new BorderLayout(0, 10));
        content.setBorder(new EmptyBorder(15, 15, 15, 15));
        content.setPreferredSize(new Dimension(360, 90));
        content.add(progressBar, BorderLayout.CENTER);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttons.add(cancelButton);
        content.add(buttons, BorderLayout.SOUTH);
        dialog.setContentPane(content);
        dialog.pack();
        dialog.setLocationRelativeTo(owner);

        SwingWorker<T, Void> worker = new SwingWorker<>() {
            @Override protected T doInBackground() throws Exception {
                return work.run(new Progress() {
                    @Override public void report(int completed, int total) {
                        if (total > 0) {
                            int percent = (int) (100L * Math.max(0, completed) / total);
                            setProgress(Math.max(0, Math.min(100, percent)));
                        }
                    }

                    // The work runs on this background thread, which cancel(true) interrupts.
                    @Override public boolean isCancelled() {
                        return Thread.currentThread().isInterrupted();
                    }
                });
            }

            @Override protected void done() {
                dialog.dispose();
                if (isCancelled()) {
                    return;
                }
                try {
                    onSuccess.accept(get());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (CancellationException e) {
                    // cancelled between iterations; nothing to show
                } catch (ExecutionException e) {
                    Throwable cause = (e.getCause() != null) ? e.getCause() : e;
                    JOptionPane.showMessageDialog(owner, cause.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        worker.addPropertyChangeListener(event -> {
            if ("progress".equals(event.getPropertyName())) {
                progressBar.setIndeterminate(false);
                progressBar.setValue((Integer) event.getNewValue());
            }
        });
        cancelButton.addActionListener(event -> {
            cancelButton.setEnabled(false);
            worker.cancel(true);
        });

        worker.execute();
        dialog.setVisible(true);
    }
}
