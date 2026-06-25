/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.io.probmodel.reader;

import org.jdom2.Element;
import org.openmarkov.core.exception.ProbNetParserException;
import org.openmarkov.core.io.ProbNetInfo;
import org.openmarkov.core.io.ProbNetReader;
import org.openmarkov.core.io.format.annotation.FormatType;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.io.probmodel.strings.XMLAttributes;
import org.openmarkov.io.probmodel.writer.PGMXWriter_0_2;
import org.openmarkov.io.probmodel.writer.PGMXWriter_1_0;
import org.openmarkov.io.xmlbif.XMLBIFReader;

import java.io.IOException;
import java.net.URL;
import java.util.List;

/**
 * @author Manuel Arias
 */
@FormatType(name = "PGMXReader", extensions = {"pgmx", "xml"}, description = "ProbModelXML")
public class PGMXReader implements ProbNetReader {
    
    @Override public ProbNetInfo read(URL networkSource) throws IOException, ProbNetParserException {
        Element root = PGMXReader_0_2.getRootElement(networkSource);
        String formatVersion = root.getAttributeValue(XMLAttributes.FORMAT_VERSION.toString());
        PGMXReader_0_2 reader = ReaderFactory.getReader(formatVersion);
        NetworkAndEvidence networkAndEvidence = reader.read(networkSource);
        return new ProbNetInfo(networkAndEvidence.probNet, networkAndEvidence.evidence, this, switch (reader) {
            case PGMXReader_1_0 ignored -> new PGMXWriter_1_0();
            case XMLBIFReader ignored -> null;
            case PGMXReader_0_2 ignored -> new PGMXWriter_0_2();
        });
    }
    
    public record NetworkAndEvidence(ProbNet probNet, List<EvidenceCase> evidence) {
    }
    
}