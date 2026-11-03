package org.openmarkov.io.probmodel;


import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.util.Objects;

public class VersionValidatorTest {
    
    private static String ABSOLUTE_PATH = new File(Objects.requireNonNull(VersionValidatorTest.class.getResource("/version.xsd"))
                                                          .getFile()).getParentFile().getAbsolutePath();
    
    private static void Version(String name) throws javax.xml.parsers.ParserConfigurationException, org.xml.sax.SAXException, java.io.IOException {
        DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document document = parser.parse(new File(ABSOLUTE_PATH + "\\" + name));
        
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        
        Source schemaFile = new StreamSource(new File(ABSOLUTE_PATH + "\\version.xsd"));
        Schema schema = factory.newSchema(schemaFile);
        
        Validator validator = schema.newValidator();
        validator.validate(new DOMSource(document));
    }
    
    
    @Test
    public void Validator() throws javax.xml.parsers.ParserConfigurationException, org.xml.sax.SAXException, java.io.IOException {
        for (String path : new File(ABSOLUTE_PATH).list()) {
            if (path.contains(".pgmx")) {
                VersionValidatorTest.Version(path);
            }
        }
    }
    
}
