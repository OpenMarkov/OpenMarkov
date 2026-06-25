/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.exception;


import org.jdom2.JDOMException;
import org.jetbrains.annotations.Nullable;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.java.exceptionUtils.ThrowableUtils;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

@SuppressWarnings("NonFinalFieldOfException")
//TODO: Not all, but many of the uses of this exceptions just show a dialog and then ignore the exception.
public abstract class ProbNetParserException extends UserInputException {
    
    protected ProbNetParserException() {
    }
    
    @Override public String toString() {
        return IBundledOpenMarkovException.toString(this);
    }
    
    private @Nullable String filename;
    private int lineNumber;
    
    public @Nullable String getFilename() {
        return filename;
    }
    
    public void setFilename(@Nullable String filename) {
        this.filename = filename;
    }
    
    public int getLineNumber() {
        return lineNumber;
    }
    
    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }
    
    @Override @Nullable public String getExceptionMessage() {
        String exceptionMessage = super.getExceptionMessage();
        if (filename != null) {
            exceptionMessage += " in file " + filename + " at line nº " + lineNumber;
        }
        return exceptionMessage;
    }
    
    public static final class MissingPotential extends ProbNetParserException {
        public MissingPotential(String potential) {
            this.potential = potential;
        }
        
        public final String potential;
    }
    
    public static final class CannotReadConstraint extends ProbNetParserException {
    }
    
    public static final class ProbabilisticNetworkTypeMissing extends ProbNetParserException {
    }
    
    public static final class ProbabilisticNetworkTypeNotRecognized extends ProbNetParserException {
        public ProbabilisticNetworkTypeNotRecognized(String networkType) {
            this.networkType = networkType;
        }
        
        public final String networkType;
    }
    
    public static final class WrongNumberOfStates extends ProbNetParserException {
        public WrongNumberOfStates(String variableName, int expectedLength, int actualLength) {
            this.variableName = variableName;
            this.expectedLength = expectedLength;
            this.actualLength = actualLength;
        }
        
        public final String variableName;
        public final int expectedLength;
        public final int actualLength;
    }
    
    public static final class MissingPropertiesOfContinuousVariable extends ProbNetParserException {
        public MissingPropertiesOfContinuousVariable(String variableName, ArrayList<String> missingProperties) {
            this.variableName = variableName;
            this.missingProperties = missingProperties;
        }
        
        public final String variableName;
        public final ArrayList<String> missingProperties;
    }
    
    public static final class MissingVariable extends ProbNetParserException {
        public MissingVariable(String variableName) {
            this.variableName = variableName;
        }
        
        public final String variableName;
    }
    
    public static final class SomeSubpotentialsArentLinkedToAnICIPotential extends ProbNetParserException {
        public SomeSubpotentialsArentLinkedToAnICIPotential(HashMap<String, TablePotential> subPotentials) {
            this.subPotentials = subPotentials;
        }
        
        public final HashMap<String, TablePotential> subPotentials;
    }
    
    public static final class MissingToken extends ProbNetParserException {
        public MissingToken(String token) {
            this.token = token;
        }
        
        public final String token;
    }
    
    public static final class MismatchedToken extends ProbNetParserException {
        public MismatchedToken(int expected, int found) {
            this.expected = expected + "";
            this.found = found + "";
        }
        
        public MismatchedToken(String expected, String found) {
            this.expected = expected;
            this.found = found;
        }
        
        public final String expected;
        public final String found;
    }
    
    public static final class MissingProbabilisticNetworkInformation extends ProbNetParserException {
    }
    
    public static final class PGMXInvalid extends ProbNetParserException {
        public PGMXInvalid(String message) {
            this.message = message;
        }
        
        public final String message;
    }
    
    public static final class XMLInvalid extends ProbNetParserException {
        public XMLInvalid(String netName, JDOMException originException) {
            this.netName = netName;
            this.originException = originException;
        }
        
        public final String netName;
        public final JDOMException originException;
    }
    
    public static final class CannotOpenFile extends ProbNetParserException {
        public CannotOpenFile(String filename) {
            this.filename = filename;
        }
        
        public final String filename;
    }
    
    public static final class WrongVersion extends ProbNetParserException {
        public WrongVersion(String version) {
            this.version = version;
        }
        
        public final String version;
    }
    
    public static final class BadlyStructuredFile extends ProbNetParserException {

        public BadlyStructuredFile(URL url, SAXParseException saxParseException) {
            ThrowableUtils.transferStackTrace(saxParseException, this);
            this.url = url;
            this.saxParseException = saxParseException;
        }

        public BadlyStructuredFile(URL url, IOException ioException) {
            ThrowableUtils.transferStackTrace(ioException, this);
            this.url = url;
            this.saxParseException = null;
        }

        public final URL url;
        public final @Nullable SAXParseException saxParseException;
    }
    
    public static final class CannotParseFile extends ProbNetParserException {
        public CannotParseFile(SAXException originException, URL url) {
            initCause(originException);
            this.originException = originException;
            this.url = url;
        }
        
        public final SAXException originException;
        public final URL url;
    }
}
