package org.openmarkov.cleanup;

import org.openmarkov.core.localize.StringDatabase;
import org.openmarkov.core.localize.spi.LocalizeResourcesProvider;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * See the method {@link ReformatClassLocalizationBundles#main(String[])}, which is the purpose of this class.
 *
 * @author jrico
 */
class ReformatClassLocalizationBundles {
    
    /**
     * Searches for every class localization file and reformats it, meaning it sorts the Localizations alphabetically by
     * class name.
     */
    public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException {
        var classLocalizationFiles = StringDatabase.getBundleProviders().flatMap(provider -> {
            var url = provider.getClass().getResource(provider.getRootOfResources() + "/localize");
            return Arrays.stream(new File(url.getPath()).listFiles())
                         .map(File::toPath)
                         .map(Path::toAbsolutePath)
                         .map(path -> path.toString().replace("target\\classes", "src\\main\\resources"))
                         .distinct()
                         .map(File::new)
                         .filter(File::exists)
                         .filter(file -> file.getName().endsWith(".xml"))
                         .filter(file -> {
                             try {
                                 return "ClassLocalizations".equals(LocalizeResourcesProvider.getFirstElementTagName(new FileInputStream(file)));
                             } catch (IOException e) {
                                 return false;
                             }
                         });
        }).toList();
        for (File inputFile : classLocalizationFiles) {
            
            // Parse XML document
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(inputFile);
            doc.getDocumentElement().normalize();
            
            var originalLocalizations = doc.getElementsByTagName("Localization");
            
            // Collect items into a list
            String localizationElements = IntStream
                    .range(0, originalLocalizations.getLength())
                    .mapToObj(i -> (Element) originalLocalizations.item(i))
                    .sorted(Comparator.comparing(e -> e.getAttribute("class")))
                    .map(element -> {
                        var attrs = element.getAttributes();
                        var attrMap = IntStream
                                .range(0, attrs.getLength())
                                .mapToObj(attrs::item)
                                .collect(Collectors.toMap(Node::getNodeName, Node::getNodeValue, (x, y) -> y, LinkedHashMap::new));
                        var localizedClassName = attrMap.remove("class");
                        String emptyStartElement = IntStream.range(0, "    <Localization ".length())
                                                            .mapToObj(ignored -> " ")
                                                            .collect(Collectors.joining());
                        
                        var order = Arrays.asList("value", "short", "medium", "long", "verbose");
                        
                        String otherAttrsAsString;
                        if (attrMap.size() == 1) {
                            var firstEntry = attrMap.entrySet().stream().findFirst().get();
                            otherAttrsAsString = " " + firstEntry.getKey() + "=\"" + firstEntry.getValue() + "\"";
                        } else {
                            otherAttrsAsString = attrMap.entrySet()
                                                        .stream()
                                                        .sorted(Comparator.comparing(entry -> order.contains(entry.getKey()) ? order.indexOf(entry.getKey()) : order.size()))
                                                        .map(entry -> "\n" + emptyStartElement + entry.getKey() + "=\"" + entry.getValue() + "\"")
                                                        .collect(Collectors.joining());
                        }
                        
                        
                        return "    <Localization class=\""
                                + localizedClassName + "\""
                                + otherAttrsAsString
                                + "/>";
                    })
                    .collect(Collectors.joining("\n"));
            
            String out = """
                    <?xml version="1.0" encoding="UTF-8"?>
                    <ClassLocalizations>
                    """ +
                    localizationElements + "\n</ClassLocalizations>";
            Files.write(inputFile.toPath(), out.getBytes());
        }
    }
}
