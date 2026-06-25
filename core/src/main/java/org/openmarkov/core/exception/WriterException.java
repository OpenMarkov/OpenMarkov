/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.exception;

import org.openmarkov.core.exception.IBundledOpenMarkovException;

import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.potential.canonical.ICIModelType;
import org.openmarkov.core.model.network.type.NetworkType;

import java.util.List;

//TODO
public abstract sealed class WriterException extends UserInputException {
    
    @Override public String toString() {
        return IBundledOpenMarkovException.toString(this);
    }
    
    public static final class CannotCreateFile extends WriterException {
        public CannotCreateFile(String filename) {
            this.filename = filename;
        }
        
        public final String filename;
    }
    
    public static final class UnknownNetworkType extends WriterException {
        public UnknownNetworkType(NetworkType networkType, List<Class<? extends NetworkType>> allowedTypes) {
            this.networkType = networkType;
            this.allowedTypes = allowedTypes;
        }
        
        public final NetworkType networkType;
        public final List<Class<? extends NetworkType>> allowedTypes;
    }
    
    public static final class ICIModelNotSupportedByElvira extends WriterException {
        public ICIModelNotSupportedByElvira(ICIModelType modelType) {
            this.modelType = modelType;
        }
        
        public final ICIModelType modelType;
    }
    
    public static final class TryingToWriteANullProbNet extends WriterException {
    }
    
    public static final class TryingToWriteAProbNetWithoutName extends WriterException {
        public TryingToWriteAProbNetWithoutName(ProbNet probNet) {
            this.probNet = probNet;
        }
        
        public final ProbNet probNet;
    }
}
