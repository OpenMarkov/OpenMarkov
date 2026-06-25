package org.openmarkov.integrationTests.io;

import org.openmarkov.core.exception.ParserException;
import org.openmarkov.core.exception.WriterException;
import org.openmarkov.core.io.ProbNetWriter;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.io.probmodel.reader.PGMXReader;
import org.openmarkov.io.probmodel.reader.PGMXReader_0_2;
import org.openmarkov.io.probmodel.writer.PGMXWriter_0_2;
import org.openmarkov.io.probmodel.writer.PGMXWriter_1_0;

import java.io.*;
import java.net.URL;
import java.util.List;


/**
 * Auxiliar class for tests
 */
// TODO Replace usages of Strings with the "version" by the enum Version
public class PGMXCompound {
    private final String V0_2 = "0.2.0";
    private final String V0_7 = "0.7.0";
    
    private PGMXReader.NetworkAndEvidence probNetInfo;
    private ProbNet probNet;
    private List<EvidenceCase> evidenceCases;
    private File file;
    private String version;
    private URL url;
    private String fileName;
    
    private boolean errorReading;
    private boolean triedToRead;
    private boolean errorWriting;
    private boolean fileOrigin;
    private boolean urlOrigin;
    
    public PGMXCompound(File file) {
        setFile(file);
        fileOrigin = true;
        urlOrigin = false;
    }
    
    public PGMXCompound(URL url, String fileName) {
        this.url = url;
        this.fileName = fileName;
        fileOrigin = false;
        urlOrigin = true;
    }
    
    public void setFile(File file) {
        this.file = file;
        initialize();
    }
    
    public void initialize() {
        triedToRead = false;
        errorWriting = false;
        errorReading = false;
        probNet = null;
        evidenceCases = null;
    }
    
    public File getFile() {
        return file;
    }
    
    public boolean wasExceptionThrownWhileReading() {
        return errorReading;
    }
    
    public boolean wasExceptionThrownWhileWriting() {
        return errorWriting;
    }
    
    public PGMXReader.NetworkAndEvidence getProbNetInfo() throws ParserException, IOException {
        if (probNetInfo == null) {
            readProbNetInfoIfNecessary();
        }
        return probNetInfo;
    }
    
    public ProbNet getProbNet() throws ParserException, IOException {
        if (probNet == null) {
            readProbNetInfoIfNecessary();
        }
        return probNet;
    }
    
    public List<EvidenceCase> getEvidence() throws ParserException, IOException {
        if (evidenceCases == null) {
            readProbNetInfoIfNecessary();
        }
        return evidenceCases;
    }
    
    public String getVersion() throws ParserException, IOException {
        if (version == null) {
            readProbNetInfoIfNecessary();
        }
        return version;
    }
    
    private void readProbNetInfoIfNecessary() throws IOException, ParserException {
        if (triedToRead) {
            return;
        }
        triedToRead = true;
        errorReading = false;
        PGMXReader_0_2 pgmxReader = new PGMXReader_0_2();
        String absolutePath = file.getAbsolutePath();
        if (fileOrigin) {
            this.probNetInfo = pgmxReader.read(file.toURI().toURL());
            this.version = PGMXReader_0_2.getVersion(file.toURI().toURL());
        } else {
            this.probNetInfo = pgmxReader.read(file.toURI().toURL());
            this.version = PGMXReader_0_2.getVersion(file.toURI().toURL());
        }
        this.probNet = probNetInfo.probNet();
        this.evidenceCases = probNetInfo.evidence();
    }
    
    public void writeProbNetInfo(String fileName, String version) {
        if (!fileOrigin) {
            File baseDir = new File(System.getProperty("java.io.tmpdir"));
            File tempDir = new File(baseDir, fileName + version);
            if (tempDir.mkdir()) {
                file = tempDir;
            }
            return;
        }
        try {
            ProbNetWriter writer = version.equals(V0_2) ? new PGMXWriter_0_2() : new PGMXWriter_1_0();
            writer.write(fileName, probNet, evidenceCases);
            file = new File(fileName);
            triedToRead = false;
            errorReading = false;
            errorWriting = false;
        } catch (WriterException e) {
            System.out.println("Error writing " + fileName + "\nVersion: " + version);
            errorWriting = true;
        }
    }
    
    private static void copyFileUsingStream(InputStream source, OutputStream destination) throws IOException {
        try {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = source.read(buffer)) > 0) {
                destination.write(buffer, 0, length);
            }
        } finally {
            source.close();
            destination.close();
        }
    }
    
    public boolean isErrorWriting() {
        return errorWriting;
    }
    
    public boolean isErrorReading() {
        return errorReading;
    }
    
}


