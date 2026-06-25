package org.openmarkov.integrationTests.integrationTests.testOpenMarkovException;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.openmarkov.core.exception.IOpenMarkovException;
import org.openmarkov.core.localize.StringDatabase;
import org.openmarkov.core.testTags.TestConfig;

import java.io.CharConversionException;
import java.nio.file.FileSystemException;

import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@SuppressWarnings({"TypeMayBeWeakened", "DuplicateStringLiteralInspection"})
public class TestSuperExceptions {
    
    @Test
    @Tag(TestConfig.DisabledInParallel)
    public void testTurnExceptionToSuperException() {
        // The 'IntegrationTestsException_en.xml' stringBundle has an exception defined for CharConversionException.
        IOpenMarkovException knownException = IOpenMarkovException.of(
                new CharConversionException("A character conversion exception"));
        
        // It must contain the title from 'CharConversionException' in the stringBundle
        assertTrue(() -> knownException.toString().contains(
                StringDatabase.getUniqueInstance().getString("CharConversionException.title")));
        // It must contain the message from 'CharConversionException' in the stringBundle
        assertTrue(() -> knownException.toString().contains(
                StringDatabase.getUniqueInstance().getString("CharConversionException.message")));
        
        // The 'IntegrationTestsException_en.xml' stringBundle does not have an exception defined for FileSystemException,
        // but it does for IOException.
        IOpenMarkovException parentKnownException = IOpenMarkovException.of(
                new FileSystemException("A file system exception"));
        
        // It must contain the title from 'CharConversionException' in the stringBundle
        assertTrue(() -> parentKnownException.toString().contains(
                StringDatabase.getUniqueInstance().getString("IOException.title")));
        // It must contain the message from 'CharConversionException' in the stringBundle
        assertTrue(() -> parentKnownException.toString().contains(
                StringDatabase.getUniqueInstance().getString("IOException.message")));
        
        // The 'IntegrationTestsException_en.xml' stringBundle does not have an exception defined for NullPointerException.
        IOpenMarkovException unknownException = IOpenMarkovException.of(
                new NullPointerException("The error is detailed here"));
        
        var s = unknownException.toString();
        
        // It must contain the message from 'UnlocalizedJavaError' in the stringBundle.
        assertTrue(() -> unknownException.toString().contains(
                StringDatabase.getUniqueInstance().getString("UnlocalizedJavaException.title")));
        // It must specify the original exception was a NullPointerException.
        assertTrue(() -> unknownException.toString().contains(
                "java.lang.NullPointerException"));
        // It must show a Stack trace.
        assertTrue(() -> unknownException.toString().contains(
                "\tat "));
        
        /*
        TrialException2 trialException = new TrialException2("OpenMarkov", "Bug is unknown");
        trialException.showDialog(null);
        throw trialException;
        */
        
        /*
        UnbundledTrialException2 unbundledException = new UnbundledTrialException2("OpenMarkov", "Bug is unknown");
        unbundledException.showOnce(null);
        throw unbundledException;
        */
        
        
        /*

        String programName = "OpenMarkov", netName = "Mynet";
        
        TrialException2 trialException = new TrialException2(programName, netName);
        System.out.println(trialException);
        throw trialException;
       
        */
        
        
        //throw trialException;
        /*
        SubTrialException2 subTrialException = new SubTrialException2("OpenMarkov", "Bug is unknown", 404);
        
        
        LOGGER.error(subTrialException);
        subTrialException.showDialogOnce(null);
        throw subTrialException;
        */
    }
    

}
