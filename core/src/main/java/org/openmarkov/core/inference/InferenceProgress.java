/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.inference;

import org.openmarkov.core.exception.InferenceStoppedException;

/**
 * How far an inference has got, and whether it has been asked to stop. The inference writes it from its thread
 * and whoever shows the progress reads it from another.
 *
 * @author Manuel Arias
 */
public class InferenceProgress {

    /** The parts of an inference that take time. */
    public enum Stage {
        OPTIMAL_STRATEGY, PROPAGATION
    }

    private volatile Stage stage = Stage.PROPAGATION;
    private volatile double fraction;
    private volatile boolean stopAsked;
    private volatile boolean followed;
    private volatile long startOfTheStage = System.currentTimeMillis();

    /** Says that somebody will show this progress, so it is worth measuring it well. */
    public void follow() {
        followed = true;
    }

    public boolean isFollowed() {
        return followed;
    }

    /** @return the milliseconds the current stage has taken so far */
    public long getMillisecondsInTheStage() {
        return System.currentTimeMillis() - startOfTheStage;
    }

    /** Begins a stage, with nothing of it done yet. */
    public void start(Stage stage) {
        this.stage = stage;
        this.fraction = 0;
        this.startOfTheStage = System.currentTimeMillis();
    }

    /** @param fraction the part of the current stage that is done, from 0 to 1; it never goes back */
    public void advanceTo(double fraction) {
        this.fraction = Math.max(this.fraction, Math.min(1, fraction));
    }

    public Stage getStage() {
        return stage;
    }

    public double getFraction() {
        return fraction;
    }

    /** Asks the inference to stop; it does so the next time it checks. */
    public void stop() {
        stopAsked = true;
    }

    /** @throws InferenceStoppedException if the inference has been asked to stop */
    public void checkNotStopped() {
        if (stopAsked) {
            throw new InferenceStoppedException();
        }
    }
}
