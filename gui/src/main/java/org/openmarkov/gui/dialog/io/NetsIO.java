/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.io;

import org.openmarkov.core.developmentStaticAnalysis.ToCheck;
import org.openmarkov.core.exception.ProbNetParserException;
import org.openmarkov.core.exception.UnreachableException;
import org.openmarkov.core.exception.UnrecoverableException;
import org.openmarkov.core.exception.WriterException;
import org.openmarkov.core.io.ProbNetInfo;
import org.openmarkov.core.io.ProbNetReader;
import org.openmarkov.core.io.ProbNetWriter;
import org.openmarkov.core.io.format.annotation.FormatManager;
import org.openmarkov.core.io.format.annotation.NoReaderForFileException;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.gui.exception.CorruptNetworkFile;
import org.openmarkov.gui.window.edition.networkEditorPanel.NetworkEditorPanel;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

/**
 * This class contains some routines to load and to save nets.
 *
 * @author jmendoza
 * @version 1.1 - jlgozalo - Catch block deleted form OpenNetwork (not required)
 * private constructor added
 */
public class NetsIO {
    
    // private constructor for a class with only static members
    private NetsIO() {
    
    }
    
    //	/**
    //	 * Saves a network in a file.
    //	 *
    //	 * @param network
    //	 *            - network to save in the file
    //	 * @param evidence
    //	 *            - list of evidence cases
    //	 * @param fileName
    //	 *            - file where the network is going to be saved
    //	 * @throws NotRecognisedNetworkFileExtensionException
    //	 *             - if file extension is not recognised
    //	 * @throws CanNotWriteNetworkToFileException
    //	 *             - if an I/O error has happened
    //	 */
    //	public static void saveNetworkFile(ProbNet network, List<EvidenceCase> evidence, String fileName)
    //			throws NotRecognisedNetworkFileExtensionException, CanNotWriteNetworkToFileException {
    //		String fileExtension = getFileExtension(fileName);
    //		FormatManager formatManager = FormatManager.getInstance();
    //		ProbNetWriter probNetWriter = formatManager.getProbNetWriter(fileExtension);
    //		try {
    //			probNetWriter.writeProbNet(fileName, network, evidence);
    //			/*
    //			 * if (fileExtension.contentEquals("elv")) {
    //			 * //ElviraWriter.getUniqueInstance().writeProbNet(fileName,
    //			 * network); } else if (fileExtension.contentEquals("xml")) {
    //			 * //XMLWriter.getUniqueInstance().writeProbNet(fileName, network);
    //			 * } else if (fileExtension.contentEquals("pgmx")) {
    //			 * PGMXWriter0_2.getUniqueInstance().writeProbNet(fileName, network); }
    //			 * else if (fileExtension.contentEquals("bif")) {
    //			 * //HuginWriter.getUniqueInstance().writeProbNet(fileName,
    //			 * network); } else { throw new
    //			 * NotRecognisedNetworkFileExtensionException(fileName); } } catch
    //			 * (IOException ex) { throw new
    //			 * CanNotWriteNetworkToFileException(fileName); }
    //			 */
    //		} catch (WriterException ex) {
    //			throw new CanNotWriteNetworkToFileException(fileName);
    //		}
    //	}
    
    /**
     * Saves a network in a file.
     *
     * @param network    - network to save in the file
     * @param evidence   - list of evidence cases
     * @param fileName   - file where the network is going to be saved
     */
    public static void saveNetworkFile(NetworkEditorPanel networkPanel, String fileName) throws WriterException {
        ProbNet network = networkPanel.getProbNet();
        List<EvidenceCase> evidence = networkPanel.getEditorPanel().getEvidenceManager().getEvidence();
        
        String fileExtension = getFileExtension(fileName);
        ProbNetWriter probNetWriter = networkPanel.getWriter();
        try {
            probNetWriter.write(fileName, network, evidence);
        } catch (WriterException.UnknownNetworkType e) {
            if (fileExtension.equals("elv")) {
                new File(fileName).delete();
            }
            throw e;
        }
    }
    
    
    //	/**
    //	 * Saves a network in a file.
    //	 *
    //	 * @param network
    //	 *            - network to save in the file
    //	 * @param fileName
    //	 *            - file where the network is going to be saved
    //	 * @throws NotRecognisedNetworkFileExtensionException
    //	 *             - if file extension is not recognised
    //	 * @throws CanNotWriteNetworkToFileException
    //	 *             - if an I/O error has happened
    //	 */
    //	public static void saveNetworkFile(ProbNet network, String fileName)
    //			throws NotRecognisedNetworkFileExtensionException, CanNotWriteNetworkToFileException {
    //
    //		saveNetworkFile(network, new ArrayList<EvidenceCase>(), fileName);
    //	}
    
    private static String getFileExtension(String fileName) {
        
        String fileExtension = null;
        int i = fileName.lastIndexOf('.');
        if ((i > 0) && (i < (fileName.length() - 1))) {
            fileExtension = fileName.substring(i + 1).toLowerCase();
        }
        
        return fileExtension;
        
    }
    
    /**
     * Opens a network saved in a file and returns the object that contains its
     * information.
     *
     * @param fileName file where the network is saved.
     *
     * @return an ProbNetInfo object with the information of the network.
     */
    @ToCheck(reasonKind = {ToCheck.ReasonKind.CODE_QUALITY, ToCheck.ReasonKind.EXCEPTIONS_REWORK},
            reasonDescription = "Reading a network file should always throw the exceptions " +
                    "ProbNetParserException.BadlyStructuredFile and CorruptNetworkFile. However, these exceptions are thrown " +
                    "in 'getProbNetReader' instead of 'loadProbNetInfo', meaning someplaces read ProbNet files without " +
                    "the awareness these exceptions give." +
                    "\n" +
                    "If it this leads to a reworks of the ProbNetReader interface (where loadProbNetInfo comes from), it" +
                    " is likely we want it to receive an URL to the file instead of a String containing the filename. " +
                    "Duplicated methods should be avoided if doing this, as the current implementation duplicates some."
    )
    public static ProbNetInfo openNetworkFile(String fileName) throws IOException, ProbNetParserException, NoReaderForFileException, CorruptNetworkFile {
        return NetsIO.openNetworkURL(new File(fileName).toURI().toURL());
    }
    
    /**
     * Opens a network from a URL.
     *
     * @param url The full url of the file to be opened file where the network
     *            is saved.
     *
     * @return an ProbNetInfo object with the information of the network.
     */
    public static ProbNetInfo openNetworkURL(URL url) throws IOException, ProbNetParserException, NoReaderForFileException, CorruptNetworkFile {
        String networkName = url.getPath();
        networkName = networkName.substring(networkName.lastIndexOf('/') + 1);
        FormatManager formatManager = FormatManager.getInstance();
        ProbNetReader probNetReader = formatManager.getProbNetReader(url);
        try {
            return probNetReader.read(url);
        } catch (UnrecoverableException | UnreachableException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new CorruptNetworkFile(url, e);
        }
    }
    
}
