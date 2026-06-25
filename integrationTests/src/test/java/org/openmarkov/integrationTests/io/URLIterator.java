package org.openmarkov.integrationTests.io;

import bitbucket.NetsRepository;
import org.openmarkov.core.exception.ProbNetParserException;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.io.probmodel.reader.PGMXReader;
import org.openmarkov.io.probmodel.reader.PGMXReader_0_2;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.List;

public class URLIterator implements PGMXIterator {
    
    private String version;
    private String pathToNewFiles;
    // Attributes
    private File next;
    private List<URL> listURL;
    private int nextURLIndex;
    private PGMXCompound compound;
    
    private List<PGMXFilter> filters;
    
    // Constructor
    public URLIterator(String pathToNewFiles, String version, List<PGMXFilter>... filters) {
        this.version = version;
        this.pathToNewFiles = pathToNewFiles;
        this.filters = filters != null && filters.length == 1 ? filters[0] : null;
        NetsRepository repository = new NetsRepository();
        listURL = repository.getNetworks();
        nextURLIndex = 0;
        next = null;
    }
    
    @Override
    public PGMXCompound next() throws IOException, ProbNetParserException {
        URL url = listURL.get(nextURLIndex++);
        String networkName = url.getPath();
        
        File file = new File(networkName);
        String newName = pathToNewFiles + file.getName() + "-" + version;
        
        // Copy network to a file
        try (
                InputStream infile = url.openStream();
                OutputStream outfile = new FileOutputStream(newName);
        ) {
            byte[] buffer = new byte[1024];
            while (infile.read(buffer, 0, 1024) > 0) {
                outfile.write(buffer);
            }
        } catch (IOException e) {
            System.out.println("Error opening network " + networkName);
        }
        
        PGMXReader_0_2 pgmxReader = new PGMXReader_0_2();
        PGMXReader.NetworkAndEvidence probNetInfo = null;
        ProbNet probNet = null;
        probNetInfo = pgmxReader.read(url);
            compound = new PGMXCompound(new File(newName));
        probNet = probNetInfo.probNet();
        return compound;
    }
    
    @Override
    public boolean hasNext() {
        return nextURLIndex < listURL.size();
    }
}

