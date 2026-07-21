package org.openmarkov.cleanup;

import org.jetbrains.annotations.NotNull;
import org.openmarkov.core.localize.StringDatabase;
import org.openmarkov.core.localize.spi.LocalizeResourcesProvider;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * See the method {@link RemoveLabel#main(String[])}, which is the purpose of this class.
 *
 * @author jrico
 */
class RemoveLabel {
    
    /**
     * Searches for every class localization file and reformats it, meaning it sorts the Localizations alphabetically by
     * class name.
     */
    public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException {
        var normalLocalizationFile = StringDatabase
                .getBundleProviders()
                .flatMap(provider -> {
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
                                         return !"ClassLocalizations".equals(LocalizeResourcesProvider.getFirstElementTagName(new FileInputStream(file)));
                                     } catch (IOException e) {
                                         return false;
                                     }
                                 });
                })
                .toList();
        
        
        for (File inputFile : normalLocalizationFile) {
            
            // Parse XML document
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(inputFile);
            Element root = doc.getDocumentElement();
            
            ArrayDeque<AbstractMap.SimpleEntry<List<String>, Element>> toVisit = new ArrayDeque<>();
            toVisit.add(new AbstractMap.SimpleEntry<>(new ArrayList<>(), root));
            
            while (!toVisit.isEmpty()) {
                var next = toVisit.pop();
                Element node = next.getValue();
                List<String> namesStack = next.getKey();
                String elementName = node.getNodeName();
                namesStack.add(elementName);
                //System.out.println("Visiting " + String.join(".", namesStack));
                
                if (elementName.equals("Label")) {
                    var value = node.getAttribute("value");
                    var parent = (Element) node.getParentNode();
                    if (!parent.hasAttribute("value")) {
                        parent.removeChild(node);
                        parent.setAttribute("value", value);
                    } else {
                        System.out.println("Parent with value and Label: " + pathOfElement(parent).stream()
                                                                                                  .collect(Collectors.joining("/")) + ", file: " + inputFile.getName());
                    }
                } else {
                    elementsOf(node)
                            .forEach(child -> toVisit.add(new AbstractMap.SimpleEntry<>(new ArrayList<>(namesStack), child)));
                }
            }
            
            try {
                writeDocumentToFile(doc, inputFile);
            } catch (TransformerException e) {
                System.err.println(e);
                e.printStackTrace();
            }
            //Files.write(inputFile.toPath(), out.getBytes());
        }
    }
    
    public static void writeDocumentToFile(Document doc, File output) throws TransformerException {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.transform(new DOMSource(doc), new StreamResult(output));
    }
    
    static ArrayList<String> pathOfElement(Element element) {
        ArrayList<String> path = new ArrayList<>();
        while (element != null) {
            path.add(element.getNodeName());
            if (element.getParentNode() instanceof Element parent) {
                element = parent;
            } else {
                break;
            }
        }
        Collections.reverse(path);
        return path;
    }
    
    private static @NotNull ArrayList<Node> nodesOf(Element root) {
        ArrayList<Node> children = new ArrayList<>(root.getChildNodes().getLength());
        for (int childIndex = 0; childIndex < root.getChildNodes().getLength(); childIndex++) {
            Node child = root.getChildNodes().item(childIndex);
            children.add(child);
        }
        return children;
    }
    
    private static @NotNull ArrayList<Element> elementsOf(Element root) {
        return nodesOf(root).stream().filter(node -> node instanceof Element)
                            .map(node -> (Element) node)
                            .collect(Collectors.toCollection(ArrayList::new));
    }
    
    
}
