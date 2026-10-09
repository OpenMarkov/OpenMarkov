/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.inference.common;

import org.openmarkov.core.exception.CannotNormalizePotentialException;
import org.openmarkov.core.exception.ConstraintViolatedException;
import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.NotEvaluableNetworkException;
import org.openmarkov.core.exception.ThereIsNoPotentialInNodeException;
import org.openmarkov.core.inference.InferenceProgress;
import org.openmarkov.core.inference.tasks.Propagation;
import org.openmarkov.core.localize.StringDatabase;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.TablePotential;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.GraphicsEnvironment;
import java.awt.Window;
import java.util.HashMap;

/**
 * Runs a propagation without leaving the application unresponsive: when it takes more than two seconds, a dialog
 * shows how far it has got and lets the user stop it.
 *
 * @author Manuel Arias
 */
public final class PropagationProgressDialog {

    /** How long a propagation may take before the dialog is shown. */
    static final int MILLISECONDS_BEFORE_SHOWING = 2000;

    private static final int MILLISECONDS_BETWEEN_UPDATES = 200;

    /** The part of a stage that must be done before the time left is estimated. */
    private static final double LEAST_FRACTION_TO_ESTIMATE = 0.02;

    private PropagationProgressDialog() {
    }

    /**
     * @param owner the window the dialog belongs to
     * @return the posterior values of the propagation, computed in another thread while this one waits
     * @throws org.openmarkov.core.exception.InferenceStoppedException if the user stops the propagation
     */
    public static HashMap<Variable, TablePotential> posteriorValues(Window owner, Propagation propagation,
                                                                    InferenceProgress progress)
            throws IncompatibleEvidenceException, ConstraintViolatedException, CannotNormalizePotentialException,
            NonProjectablePotentialException, NotEvaluableNetworkException.NotApplicableNetwork,
            ThereIsNoPotentialInNodeException {
        if (GraphicsEnvironment.isHeadless() || !SwingUtilities.isEventDispatchThread()) {
            return propagation.getPosteriorValues();
        }
        Object[] outcome = new Object[1];
        Thread worker = new Thread(() -> {
            try {
                outcome[0] = propagation.getPosteriorValues();
            } catch (Throwable failure) {
                outcome[0] = failure;
            }
        }, "propagation");
        worker.start();
        try {
            worker.join(MILLISECONDS_BEFORE_SHOWING);
            if (worker.isAlive()) {
                showUntilItEnds(owner, worker, progress);
                worker.join();
            }
        } catch (InterruptedException e) {
            progress.stop();
            Thread.currentThread().interrupt();
        }
        return resultOrFailure(outcome[0]);
    }

    @SuppressWarnings("unchecked")
    private static HashMap<Variable, TablePotential> resultOrFailure(Object outcome)
            throws IncompatibleEvidenceException, ConstraintViolatedException, CannotNormalizePotentialException,
            NonProjectablePotentialException, NotEvaluableNetworkException.NotApplicableNetwork,
            ThereIsNoPotentialInNodeException {
        switch (outcome) {
            case IncompatibleEvidenceException failure -> throw failure;
            case ConstraintViolatedException failure -> throw failure;
            case CannotNormalizePotentialException failure -> throw failure;
            case NonProjectablePotentialException failure -> throw failure;
            case NotEvaluableNetworkException.NotApplicableNetwork failure -> throw failure;
            case ThereIsNoPotentialInNodeException failure -> throw failure;
            case RuntimeException failure -> throw failure;
            case Error failure -> throw failure;
            case Throwable failure -> throw new IllegalStateException(failure);
            case null, default -> {
                return (HashMap<Variable, TablePotential>) outcome;
            }
        }
    }

    /** Shows the dialog and returns when the propagation ends, by itself or because the user stops it. */
    private static void showUntilItEnds(Window owner, Thread worker, InferenceProgress progress) {
        StringDatabase texts = StringDatabase.getUniqueInstance();
        JDialog dialog = new JDialog(owner, texts.getString("InferenceMode.Progress.Title"),
                                     Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setName("propagationProgressDialog");
        dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        JLabel stage = new JLabel(textOf(progress.getStage(), texts));
        JProgressBar bar = new JProgressBar(0, 100);
        bar.setStringPainted(true);
        JButton stop = new JButton(texts.getString("InferenceMode.Progress.Stop"));
        stop.setName("stopPropagationButton");
        stop.addActionListener(_ -> {
            progress.stop();
            stop.setEnabled(false);
            stage.setText(texts.getString("InferenceMode.Progress.Stopping"));
        });
        JLabel timeLeft = new JLabel(" ");
        timeLeft.setName("timeLeftLabel");
        JPanel content = new JPanel(new BorderLayout(0, 8));
        content.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        content.add(stage, BorderLayout.NORTH);
        content.add(bar, BorderLayout.CENTER);
        JPanel south = new JPanel(new BorderLayout(0, 6));
        south.add(timeLeft, BorderLayout.NORTH);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttons.add(stop);
        south.add(buttons, BorderLayout.SOUTH);
        content.add(south, BorderLayout.SOUTH);
        dialog.setContentPane(content);
        dialog.setSize(420, 175);
        dialog.setLocationRelativeTo(owner);
        Timer timer = new Timer(MILLISECONDS_BETWEEN_UPDATES, _ -> {
            if (!worker.isAlive()) {
                dialog.dispose();
                return;
            }
            bar.setValue((int) Math.round(100 * progress.getFraction()));
            timeLeft.setText(textOfTheTimeLeft(progress, texts));
            if (stop.isEnabled()) {
                stage.setText(textOf(progress.getStage(), texts));
            }
        });
        timer.start();
        try {
            dialog.setVisible(true);
        } finally {
            timer.stop();
        }
    }

    /**
     * @return the seconds left of a stage of which {@code fraction} has been done in {@code milliseconds}, or -1
     * while too little has been done to tell
     */
    static long secondsLeft(long milliseconds, double fraction) {
        if (fraction < LEAST_FRACTION_TO_ESTIMATE || fraction >= 1) {
            return -1;
        }
        return Math.round(milliseconds / 1000.0 * (1 - fraction) / fraction);
    }

    /** The time left is given only for the stage whose progress is measured by the work done. */
    private static String textOfTheTimeLeft(InferenceProgress progress, StringDatabase texts) {
        long seconds = progress.getStage() == InferenceProgress.Stage.OPTIMAL_STRATEGY
                ? secondsLeft(progress.getMillisecondsInTheStage(), progress.getFraction()) : -1;
        if (seconds < 0) {
            return " ";
        }
        return seconds < 90
                ? texts.getString("InferenceMode.Progress.SecondsLeft").replace("~", String.valueOf(seconds))
                : texts.getString("InferenceMode.Progress.MinutesLeft")
                       .replace("~", String.valueOf(Math.round(seconds / 60.0)));
    }

    private static String textOf(InferenceProgress.Stage stage, StringDatabase texts) {
        return texts.getString(switch (stage) {
            case OPTIMAL_STRATEGY -> "InferenceMode.Progress.OptimalStrategy";
            case PROPAGATION -> "InferenceMode.Progress.Propagation";
        });
    }
}
