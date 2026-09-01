package org.openmarkov.gui.license;

import org.jetbrains.annotations.NotNull;
import org.openmarkov.core.logging.OpenMarkovLogger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public record LicenseHolder(@NotNull String group, @NotNull String artifactID, @NotNull String version,
                            @NotNull List<License> licenses) {
    
    @Override
    public @NotNull String toString() {
        return descriptor();
    }
    
    public String descriptor() {
        return LicenseHolder.descriptor(this.group, this.artifactID, this.version);
    }
    
    public static String descriptor(@NotNull String group, @NotNull String artifactID, @NotNull String version) {
        return group + "." + artifactID + ":" + version;
    }
    
    public static final Class<?> RESOURCE_RESOLVER;
    
    static {
        Class<?> resourceResolver = LicenseHolder.class;
        try {
            resourceResolver = Class.forName("org.openmarkov.full.OpenMarkov");
        } catch (ClassNotFoundException e) {
            OpenMarkovLogger.LOGGER.debug("The default license resolver is not set to OpenMarkov's full module", e);
        }
        RESOURCE_RESOLVER = resourceResolver;
    }
    
    private static final Map<String, List<License>> MANUALLY_MAPPED_HOLDERS = Map.of(
            "colt.colt:1.2.0",
            List.of(new License("CERN - European Organization for Nuclear Research", "https://dst.lbl.gov/ACSSoftware/colt/license.html", null, "/third-party-manually-added-licenses/CERN - European Organization for Nuclear Research.txt", null)),
            "concurrent.concurrent:1.3.4",
            List.of(new License("TECHNOLOGY LICENSE FROM SUN MICROSYSTEMS INC TO DOUG LEA", "https://gee.cs.oswego.edu/dl/classes/EDU/oswego/cs/dl/util/sun-u.c.license.pdf", null, "/third-party-manually-added-licenses/TECHNOLOGY LICENSE FROM SUN MICROSYSTEMS INC TO DOUG LEA.txt", null)),
            "org.easytesting.fest-util:1.2.5",
            List.of(new License("Apache 2.0", "https://github.com/alexruiz/fest-util/blob/f45859f64b14932ac37699c6068dc9b01b1244d9/LICENSE.txt", null, "/third-party-manually-added-licenses/apache-2.0 - license-2.0.txt", null))
    );
    
    private static final Map<String, License> MANUALLY_MAPPED_LICENSE_FROM_URL = Map.of(
            "http://jgrapht.sourceforge.net/LGPL.html",
            new License("LGPL", "https://opensource.org/license/lgpl-2-1", null, "/third-party-manually-added-licenses/LGPL.txt", null),
            "http://www.gnu.org/licenses/lgpl.txt",
            new License("LGPL", "https://opensource.org/license/lgpl-2-1", null, "/third-party-manually-added-licenses/LGPL.txt", null),
            "https://www.gnu.org/licenses/old-licenses/lgpl-2.1",
            new License("LGPL", "https://opensource.org/license/lgpl-2-1", null, "/third-party-manually-added-licenses/LGPL.txt", null),
            "http://www1.fpl.fs.fed.us/optimization.html",
            new License("FPL Statistics Group - Disclaimer of warranties", "https://web.archive.org/web/20220629085714/https://www1.fpl.fs.fed.us/optimization.html", null, "/third-party-manually-added-licenses/FPL Statistics Group - Disclaimer of warranties.txt", null),
            "https://github.com/vlsi/jgraphx-publish/LICENSE",
            new License("Vladimir Sitnikov's license", "https://github.com/vlsi/jgraphx-publish/blob/3a880fdd76d5d5b052645ea1d724119a1fdc0379/LICENSE", null, "/third-party-manually-added-licenses/Vladimir Sitnikov.txt", null),
            "http://www.gnu.org/copyleft/lesser.html",
            new License("GNU LESSER GENERAL PUBLIC LICENSE", "https://www.gnu.org/licenses/lgpl-3.0.html#license-text", null, "/third-party-manually-added-licenses/GNU Lesser.txt", null)
    );
    
    
    public static final List<LicenseHolder> LICENSE_HOLDERS = LicenseHolder.getLicenseHolders();
    
    @SuppressWarnings("ProhibitedExceptionCaught")
    private static List<LicenseHolder> getLicenseHolders() {
        Document licensesDOM = null;
        try {
            licensesDOM = DocumentBuilderFactory.newInstance()
                                                .newDocumentBuilder()
                                                .parse(LicenseHolder.RESOURCE_RESOLVER.getResourceAsStream("/third-party-licenses/licenses.xml"));
        } catch (SAXException | ParserConfigurationException | IOException | IllegalArgumentException e) {
            OpenMarkovLogger.LOGGER.debug("Could not resolve the third party licenses", e);
            return Collections.emptyList();
        }
        var dependencies = LicenseHolder.getElementByTagName(licensesDOM.getDocumentElement(), "dependencies");
        return LicenseHolder.childElementsStreamOf(dependencies).map(dependency -> {
                                String groupId = LicenseHolder.getElementByTagName(dependency, "groupId").getTextContent();
                                String artifactID = LicenseHolder.getElementByTagName(dependency, "artifactId").getTextContent();
                                String version = LicenseHolder.getElementByTagName(dependency, "version").getTextContent();
                                
                                String descriptor = LicenseHolder.descriptor(groupId, artifactID, version);
                                List<License> licenses;
                                if (MANUALLY_MAPPED_HOLDERS.containsKey(descriptor)) {
                                    licenses = MANUALLY_MAPPED_HOLDERS.get(descriptor);
                                } else {
                                    licenses = LicenseHolder.childElementsStreamOf(LicenseHolder.getElementByTagName(dependency, "licenses"))
                                                            .map(license -> {
                                                                String url = LicenseHolder.getElementByTagName(license, "url")
                                                                                          .getTextContent();
                                                                String name = LicenseHolder.getElementByTagName(license, "name")
                                                                                           .getTextContent();
                                                                String distribution = null;
                                                                try {
                                                                    distribution = LicenseHolder.getElementByTagName(license, "distribution")
                                                                                                .getTextContent();
                                                                } catch (NullPointerException _) {
                                                                }
                                                                
                                                                String resource = null;
                                                                try {
                                                                    resource = "/third-party-licenses/" + LicenseHolder.getElementByTagName(license, "file")
                                                                                                                       .getTextContent();
                                                                } catch (NullPointerException _) {
                                                                }
                                                                
                                                                String comments = null;
                                                                try {
                                                                    comments = LicenseHolder.getElementByTagName(license, "comments")
                                                                                            .getTextContent();
                                                                } catch (NullPointerException _) {
                                                                }
                                                                
                                                                return new License(name, url, distribution, resource, comments);
                                                            })
                                                            .toList();
                                }
                                licenses = licenses.stream()
                                                   .map(license -> {
                                                       if (MANUALLY_MAPPED_LICENSE_FROM_URL.containsKey(license.URL)) {
                                                           return MANUALLY_MAPPED_LICENSE_FROM_URL.get(license.URL);
                                                       }
                                                       return license;
                                                   })
                                                   .sorted(Comparator.comparing(License::name)).toList();
                                LicenseHolder licenseHolder = new LicenseHolder(groupId, artifactID, version, licenses);
                                licenses.forEach(license -> license.setHolder(licenseHolder));
                                return licenseHolder;
                            })
                            .sorted(Comparator.comparing((LicenseHolder licenseHolder) -> licenseHolder.group)
                                              .thenComparing(licenseHolder -> licenseHolder.artifactID))
                            .filter(licenseHolder -> !"org.openmarkov".equalsIgnoreCase(licenseHolder.group))
                            .toList();
    }
    
    private static Element getElementByTagName(Element element, String tagName) {
        return (Element) element.getElementsByTagName(tagName).item(0);
    }
    
    private static Stream<Node> childStreamOf(Node element) {
        var childNodes = element.getChildNodes();
        return IntStream.range(0, childNodes.getLength()).mapToObj(childNodes::item);
    }
    
    private static Stream<Element> childElementsStreamOf(Node element) {
        return LicenseHolder.childStreamOf(element).filter(Element.class::isInstance).map(Element.class::cast);
    }
    
}


